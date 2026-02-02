package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

internal data class ApiMessage(
    val code: Int,
    val message: String,
)

private fun isSuccessApiCode(code: Int): Boolean = code == 0 || code == 200

private fun Json.tryParseApiMessage(raw: String): ApiMessage? =
    runCatching { decodeFromString<ApiErrorMessage>(raw) }
        .getOrNull()
        ?.let { ApiMessage(code = it.code, message = it.message) }

private fun Json.tryExtractDataElement(raw: String) =
    runCatching { parseToJsonElement(raw) }.getOrNull()
        ?.let { it as? JsonObject }
        ?.get("data")

private fun String.toHumanSnippet(maxLen: Int = 200): String {
    val clean = replace(Regex("\\s+"), " ").trim()
    return if (clean.length <= maxLen) clean else clean.take(maxLen) + "…"
}

/**
 * Decodes responses that may be either:
 * - a direct payload `T`
 * - an envelope `{ code, message, data }` where `data` is `T`
 *
 * If the server returns an error code (non-0) or non-2xx HTTP status, throws [ApiException].
 */
internal suspend inline fun <reified T> HttpResponse.decodeBody(
    json: Json = HttpClientFactory.DefaultJson,
): T {
    val raw = bodyAsText()

    // HTTP-level error
    if (!status.isSuccess()) {
        val method = call.request.method.value
        val url = call.request.url.toString()
        val snippet = raw.toHumanSnippet(maxLen = 600)
        // Always log a short snippet for debugging (token/sign issues often surface here).
        println("HTTP !! ${status.value} $method $url body: $snippet")
        val apiMsg = json.tryParseApiMessage(raw)
        val message = apiMsg?.message?.takeIf { it.isNotBlank() }
            ?: raw.toHumanSnippet()
            .ifBlank { "请求失败：${status.value} ${status.description}" }
        throw ApiException(
            code = status.value,
            apiCode = apiMsg?.code,
            httpStatus = status.value,
            message = message,
        )
    }

    // 1) direct payload
    runCatching { return json.decodeFromString<T>(raw) }

    // 2) ApiResponse<T> envelope
    runCatching {
        val wrapped = json.decodeFromString<ApiResponse<T>>(raw)
        val data = wrapped.data
        if (isSuccessApiCode(wrapped.code) && data != null) return data
        throw ApiException(
            code = wrapped.code,
            apiCode = wrapped.code,
            httpStatus = status.value,
            message = wrapped.message.ifBlank { "请求失败" },
        )
    }

    // 3) loose `{ data: T }` envelope (without code/message)
    val dataElement = json.tryExtractDataElement(raw)
    if (dataElement != null) {
        runCatching { return json.decodeFromJsonElement<T>(dataElement) }
    }

    // 4) `{ code, message }` without data (treat as error)
    json.tryParseApiMessage(raw)?.let {
        throw ApiException(
            code = it.code,
            apiCode = it.code,
            httpStatus = status.value,
            message = it.message,
        )
    }

    throw SerializationException("Unexpected response body: ${raw.toHumanSnippet()}")
}

/**
 * Similar to [decodeBody] but for endpoints where callers don't care about the payload.
 * Still validates HTTP status and `{ code, message }` style errors.
 */
internal suspend fun HttpResponse.ensureSuccess(
    json: Json = HttpClientFactory.DefaultJson,
) {
    val raw = bodyAsText()

    if (!status.isSuccess()) {
        val apiMsg = json.tryParseApiMessage(raw)
        val message = apiMsg?.message?.takeIf { it.isNotBlank() }
            ?: raw.toHumanSnippet()
            .ifBlank { "请求失败：${status.value} ${status.description}" }
        throw ApiException(
            code = status.value,
            apiCode = apiMsg?.code,
            httpStatus = status.value,
            message = message,
        )
    }

    // If server uses `{code,message}` even on 2xx, treat non-0 as error.
    json.tryParseApiMessage(raw)?.let {
        if (!isSuccessApiCode(it.code)) {
            throw ApiException(
                code = it.code,
                apiCode = it.code,
                httpStatus = status.value,
                message = it.message,
            )
        }
    }
}
