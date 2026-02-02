package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import kotlinx.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.model.AccountContext
import vip.mystery0.xhu.timetable.platform.AppInfo

object HttpClientFactory {
    private const val TIMEOUT_MILLIS: Long = 30_000L
    private const val MAX_GET_ATTEMPTS: Int = 3

    val DefaultJson: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun create(
        accountContextProvider: suspend () -> AccountContext?,
        onUnauthorizedDebounced: suspend () -> Unit,
        appInfo: AppInfo,
        userAgent: String = "",
        log: (String) -> Unit = ::println,
        json: Json = DefaultJson,
    ): HttpClient = HttpClient {
        // Ktor 3 defaults may throw on non-2xx; we want to handle HTTP errors ourselves in decodeBody/ensureSuccess.
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MILLIS
            connectTimeoutMillis = TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
        }

        install(HttpRequestRetry) {
            maxRetries = (MAX_GET_ATTEMPTS - 1).coerceAtLeast(0)

            retryIf { request, response ->
                if (request.method != HttpMethod.Get) return@retryIf false
                response.status == HttpStatusCode.RequestTimeout || response.status.value >= 500
            }

            retryOnExceptionIf { request, cause ->
                if (request.method != HttpMethod.Get) return@retryOnExceptionIf false
                cause is IOException
            }

            delayMillis { retry ->
                val retryIndex = retry.coerceAtLeast(1)
                250L * (1L shl (retryIndex - 1))
            }
        }

        install(SessionInterceptor) {
            this.accountContextProvider = accountContextProvider
        }

        install(ServerApiPlugin) {
            this.appInfo = appInfo
            this.accountContextProvider = accountContextProvider
        }

        install(AuthInterceptor) {
            debounceWindow = 500.milliseconds
            onUnauthorized = { onUnauthorizedDebounced() }
        }

        if (userAgent.isNotBlank()) {
            install(UserAgent) {
                agent = userAgent
            }
        }

        install(SimpleHttpLogging) {
            logger = log
        }

        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }
}

private class SimpleHttpLoggingConfig {
    var logger: (String) -> Unit = ::println
    var logLevel: SimpleHttpLogLevel = SimpleHttpLogLevel.Info
    var logRequestBody: Boolean = false
    var logResponseBody: Boolean = false
}

private enum class SimpleHttpLogLevel {
    None,
    Info,
}

private val SimpleHttpLogging = createClientPlugin<SimpleHttpLoggingConfig>("SimpleHttpLogging", ::SimpleHttpLoggingConfig) {
    val logLevel = pluginConfig.logLevel
    val logFn = pluginConfig.logger
    val logRequestBody = pluginConfig.logRequestBody
    val logResponseBody = pluginConfig.logResponseBody
    val startMarkKey = AttributeKey<TimeMark>("SimpleHttpLoggingStartMark")

    onRequest { request, content ->
        if (logLevel == SimpleHttpLogLevel.None) return@onRequest

        request.attributes.put(startMarkKey, TimeSource.Monotonic.markNow())
        logFn("HTTP -> ${request.method.value} ${request.url}")

        if (logRequestBody) {
            logFn("HTTP -> body: $content")
        }
    }

    onResponse { response ->
        if (logLevel == SimpleHttpLogLevel.None) return@onResponse

        val start = if (response.call.attributes.contains(startMarkKey)) {
            response.call.attributes[startMarkKey]
        } else {
            null
        }
        val elapsed: Duration? = start?.elapsedNow()
        val elapsedSuffix = if (elapsed == null) "" else " (${elapsed.inWholeMilliseconds}ms)"

        logFn(
            "HTTP <- ${response.status.value} ${response.call.request.method.value} ${response.call.request.url}$elapsedSuffix",
        )

        if (logResponseBody) {
            logFn("HTTP <- response body logging disabled by default")
        }
    }
}
