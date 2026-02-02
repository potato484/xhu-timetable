package vip.mystery0.xhu.timetable.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import vip.mystery0.xhu.timetable.shared.network.AuthInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Tests for 401 Unauthorized handling with debouncing (H2).
 *
 * Requirements:
 * - Multiple concurrent 401 responses should trigger callback only once
 * - Debounce window prevents callback storm
 * - After debounce window, new 401 can trigger callback again
 */
class AuthInterceptorTest {

    @Test
    fun debounce_prevents_multiple_callbacks_with_real_interceptor() = runTest {
        val callbackCount = mutableListOf<Long>()

        val mockEngine = MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.Unauthorized)
        }

        val client = HttpClient(mockEngine) {
            install(AuthInterceptor) {
                debounceWindow = 500.milliseconds
                onUnauthorized = { callbackCount.add(System.currentTimeMillis()) }
            }
        }

        // Simulate 5 concurrent 401 responses
        val jobs = (1..5).map {
            async {
                delay(it * 10L)
                runCatching { client.get("https://test.com/api/$it") }
            }
        }
        awaitAll(*jobs.toTypedArray())

        assertEquals(1, callbackCount.size, "Only one callback should fire within debounce window")
        client.close()
    }

    @Test
    fun callback_fires_again_after_debounce_window_with_real_interceptor() = runTest {
        val callbackCount = mutableListOf<Int>()

        val mockEngine = MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.Unauthorized)
        }

        val client = HttpClient(mockEngine) {
            install(AuthInterceptor) {
                debounceWindow = 50.milliseconds
                onUnauthorized = { callbackCount.add(callbackCount.size + 1) }
            }
        }

        // First batch
        runCatching { client.get("https://test.com/api/1") }
        runCatching { client.get("https://test.com/api/2") }
        assertEquals(1, callbackCount.size)

        // Wait for debounce window
        delay(100)

        // Second batch
        runCatching { client.get("https://test.com/api/3") }
        runCatching { client.get("https://test.com/api/4") }
        assertEquals(2, callbackCount.size, "Callback should fire again after debounce window")

        client.close()
    }

    @Test
    fun debounce_prevents_multiple_callbacks() = runTest {
        val callbackCount = mutableListOf<Long>()
        val mutex = Mutex()
        var lastCallbackTime: TimeSource.Monotonic.ValueTimeMark? = null
        val debounceWindow = 500.milliseconds

        suspend fun onUnauthorized() {
            val now = TimeSource.Monotonic.markNow()
            val shouldHandle = mutex.withLock {
                val previous = lastCallbackTime
                val allowed = previous == null || previous.elapsedNow() >= debounceWindow
                if (allowed) {
                    lastCallbackTime = now
                }
                allowed
            }
            if (shouldHandle) {
                callbackCount.add(System.currentTimeMillis())
            }
        }

        // Simulate 5 concurrent 401 responses
        val jobs = (1..5).map {
            async {
                delay(it * 10L) // Stagger slightly
                onUnauthorized()
            }
        }
        awaitAll(*jobs.toTypedArray())

        assertEquals(1, callbackCount.size, "Only one callback should fire within debounce window")
    }

    @Test
    fun callback_fires_again_after_debounce_window() = runTest {
        val callbackCount = mutableListOf<Int>()
        val mutex = Mutex()
        var lastCallbackTime: TimeSource.Monotonic.ValueTimeMark? = null
        val debounceWindow = 50.milliseconds

        suspend fun onUnauthorized() {
            val now = TimeSource.Monotonic.markNow()
            val shouldHandle = mutex.withLock {
                val previous = lastCallbackTime
                val allowed = previous == null || previous.elapsedNow() >= debounceWindow
                if (allowed) {
                    lastCallbackTime = now
                }
                allowed
            }
            if (shouldHandle) {
                callbackCount.add(callbackCount.size + 1)
            }
        }

        // First batch
        onUnauthorized()
        onUnauthorized()
        assertEquals(1, callbackCount.size)

        // Wait for debounce window
        delay(100)

        // Second batch
        onUnauthorized()
        onUnauthorized()
        assertEquals(2, callbackCount.size, "Callback should fire again after debounce window")
    }

    @Test
    fun concurrent_401_from_different_requests() = runTest {
        val handledRequests = mutableListOf<String>()
        val mutex = Mutex()
        var lastCallbackTime: TimeSource.Monotonic.ValueTimeMark? = null
        val debounceWindow = 100.milliseconds

        suspend fun handleUnauthorized(requestId: String): Boolean {
            val now = TimeSource.Monotonic.markNow()
            return mutex.withLock {
                val previous = lastCallbackTime
                val allowed = previous == null || previous.elapsedNow() >= debounceWindow
                if (allowed) {
                    lastCallbackTime = now
                    handledRequests.add(requestId)
                }
                allowed
            }
        }

        // Simulate concurrent requests all getting 401
        val results = listOf(
            async { handleUnauthorized("courses") },
            async { handleUnauthorized("exams") },
            async { handleUnauthorized("scores") },
        ).awaitAll()

        // Only one should be handled
        assertEquals(1, handledRequests.size)
        assertEquals(1, results.count { it })
        assertEquals(2, results.count { !it })
    }
}
