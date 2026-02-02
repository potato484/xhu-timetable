package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class AuthInterceptorConfig {
    var debounceWindow: Duration = 500.milliseconds
    var onUnauthorized: suspend (HttpResponse) -> Unit = {}
}

val AuthInterceptor = createClientPlugin<AuthInterceptorConfig>("AuthInterceptor", ::AuthInterceptorConfig) {
    val debounceWindow = pluginConfig.debounceWindow
    val onUnauthorizedCallback = pluginConfig.onUnauthorized
    val mutex = Mutex()
    var lastUnauthorizedAt: TimeMark? = null

    onResponse { response ->
        if (response.status != HttpStatusCode.Unauthorized) return@onResponse

        val shouldHandle = mutex.withLock {
            val previous = lastUnauthorizedAt
            val now = TimeSource.Monotonic.markNow()
            val allowed = previous == null || previous.elapsedNow() >= debounceWindow
            if (allowed) {
                lastUnauthorizedAt = now
            }
            allowed
        }

        if (shouldHandle) {
            try {
                onUnauthorizedCallback(response)
            } catch (_: Exception) {
                // Swallow callback exceptions to prevent crashing unrelated requests
            }
        }
    }
}
