package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.content.TextContent
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.encodedPath
import vip.mystery0.xhu.timetable.platform.AppInfo
import kotlinx.datetime.Clock
import vip.mystery0.xhu.timetable.model.AccountContext

class ServerApiPluginConfig {
    var appInfo: AppInfo? = null
    var sessionHeaderName: String = "sessionToken"
    var accountContextProvider: suspend () -> AccountContext? = { null }
}

val ServerApiPlugin = createClientPlugin<ServerApiPluginConfig>("ServerApiPlugin", ::ServerApiPluginConfig) {
    val appInfo = pluginConfig.appInfo ?: error("AppInfo must be provided")
    val sessionHeaderName = pluginConfig.sessionHeaderName
    val accountContextProvider = pluginConfig.accountContextProvider

    on(SendingRequest) { request, content ->
        // Ensure session token is present in headers before signing.
        // Some server endpoints verify signature using the session token as the sign key.
        val explicitToken = request.headers[sessionHeaderName]
        if (explicitToken.isNullOrBlank()) {
            val tokenFromContext = runCatching { accountContextProvider() }
                .getOrNull()
                ?.token
                ?.takeIf { it.isNotBlank() }
            if (tokenFromContext != null) {
                request.headers.remove(sessionHeaderName)
                request.headers.append(sessionHeaderName, tokenFromContext)
            }
        }

        // Debug: show whether session token is attached (do NOT print token itself).
        val attachedToken = request.headers[sessionHeaderName]
        val tokenMark = if (attachedToken.isNullOrBlank()) {
            "none"
        } else {
            // hash the token so we can compare before/after login without leaking it.
            val hash8 = runCatching { attachedToken.sha256().take(8) }.getOrDefault("????????")
            "len=${attachedToken.length} hash=$hash8"
        }
        println("AUTH hdr: ${request.method.value} ${request.url.encodedPath} $sessionHeaderName=$tokenMark")

        val bodyForSign = extractBodyForSign(content)
        val contentType = content.contentType?.toString() ?: request.headers[HttpHeaders.ContentType]
        val body = bodyForSign.text
        val signTime = Clock.System.now()
        // IMPORTANT:
        // - Ktor may use chunked transfer and omit Content-Length.
        // - Server-side sign verification expects the same "content-length" value as the request header (or "0" when absent).
        //   Do NOT derive it from body bytes here, otherwise signature may mismatch and cause HTTP 401.
        val contentLength = content.contentLength
            ?: request.headers[HttpHeaders.ContentLength]?.toLongOrNull()
            ?: 0L

        val map = linkedMapOf(
            "method" to request.method.value.uppercase(),
            "url" to request.url.encodedPath.substringBefore("?"),
            "body" to body,
            "content-type" to (contentType ?: "empty"),
            "content-length" to contentLength.toString(),
            "signTime" to signTime.toEpochMilliseconds().toString(),
            "clientVersionName" to appInfo.versionName(),
            "clientVersionCode" to appInfo.versionCode(),
        )
        val sortMap = map.keys.sorted().associateWith { map[it] }
        val signKey = request.headers[sessionHeaderName] ?: signTime.toEpochMilliseconds().toString()
        val salt = "$signKey:XhuTimeTable".md5().uppercase()
        val sign = "$sortMap:$salt".sha256().uppercase()

        if (request.url.encodedPath.endsWith("/login")) {
            val sanitizedBody = body
                .replace(Regex("\"password\"\\s*:\\s*\"[^\"]*\""), "\"password\":\"***\"")
                .replace(Regex("\"publicKey\"\\s*:\\s*\"[^\"]*\""), "\"publicKey\":\"***\"")
                .replace(Regex("\"clientPublicKey\"\\s*:\\s*\"[^\"]*\""), "\"clientPublicKey\":\"***\"")
            println(
                "LOGIN sign debug: outgoing=${content::class.simpleName} " +
                    "contentType=${contentType ?: "null"} " +
                    "contentLength=${content.contentLength ?: "null"} " +
                    "bodyBytes=${bodyForSign.lengthBytes} " +
                    "ua=${request.headers[HttpHeaders.UserAgent] ?: "null"}",
            )
            println(
                "LOGIN sign debug: signTime=${signTime.toEpochMilliseconds()} " +
                    "sign=$sign body=$sanitizedBody",
            )
        }

        request.header("sign", sign)
        request.header("signTime", signTime.toEpochMilliseconds().toString())
        request.header("deviceId", appInfo.deviceId())
        request.header("clientVersionName", appInfo.versionName())
        request.header("clientVersionCode", appInfo.versionCode())
    }
}

private data class BodyForSign(
    val text: String,
    val lengthBytes: Long,
)

private fun extractBodyForSign(body: OutgoingContent): BodyForSign {
    if (body is TextContent) {
        val text = body.text
        return BodyForSign(
            text = text,
            lengthBytes = text.encodeToByteArray().size.toLong(),
        )
    }
    if (body is OutgoingContent.ByteArrayContent) {
        // Ktor may serialize JSON as ByteArrayContent (engine-dependent). Ensure signature covers the real body.
        val bytes = runCatching { body.bytes() }.getOrDefault(byteArrayOf())
        return BodyForSign(
            text = runCatching { bytes.decodeToString() }.getOrDefault(""),
            lengthBytes = bytes.size.toLong(),
        )
    }
    return BodyForSign(text = "", lengthBytes = 0L)
}
