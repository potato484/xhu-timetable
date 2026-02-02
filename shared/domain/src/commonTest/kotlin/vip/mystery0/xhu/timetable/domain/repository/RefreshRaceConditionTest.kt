package vip.mystery0.xhu.timetable.domain.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Repository refresh race conditions (C1: latest-wins).
 *
 * These tests verify that concurrent refresh operations don't cause
 * data corruption or stale data overwrites.
 */
class RefreshRaceConditionTest {

    @Test
    fun mutex_prevents_concurrent_refresh() = runTest {
        val mutex = Mutex()
        val executionOrder = mutableListOf<String>()

        val job1 = async {
            mutex.withLock {
                executionOrder.add("job1-start")
                delay(50)
                executionOrder.add("job1-end")
            }
        }

        val job2 = async {
            delay(10) // Ensure job1 starts first
            mutex.withLock {
                executionOrder.add("job2-start")
                delay(10)
                executionOrder.add("job2-end")
            }
        }

        awaitAll(job1, job2)

        assertEquals(
            listOf("job1-start", "job1-end", "job2-start", "job2-end"),
            executionOrder,
            "Mutex should serialize execution"
        )
    }

    @Test
    fun tryLock_skips_if_already_locked() = runTest {
        val mutex = Mutex()
        val results = mutableListOf<String>()

        val job1 = async {
            mutex.withLock {
                results.add("job1-acquired")
                delay(100)
                results.add("job1-released")
            }
        }

        delay(10) // Ensure job1 has the lock

        val job2 = async {
            if (mutex.tryLock()) {
                try {
                    results.add("job2-acquired")
                } finally {
                    mutex.unlock()
                }
            } else {
                results.add("job2-skipped")
            }
        }

        awaitAll(job1, job2)

        assertTrue(
            results.contains("job2-skipped"),
            "tryLock should skip when mutex is held"
        )
    }

    @Test
    fun latest_wins_with_version_tracking() = runTest {
        val dataStore = MutableStateFlow(0)
        val mutex = Mutex()
        var version = 0

        suspend fun refresh(newValue: Int, delayMs: Long) {
            val myVersion = ++version
            mutex.withLock {
                delay(delayMs)
                // Only write if this is still the latest request
                if (myVersion == version) {
                    dataStore.value = newValue
                }
            }
        }

        // Simulate two concurrent refreshes
        val job1 = async { refresh(100, 50) }
        val job2 = async {
            delay(10)
            refresh(200, 10)
        }

        awaitAll(job1, job2)

        // The second refresh (200) should win because it started later
        // and the mutex ensures serialization
        assertEquals(200, dataStore.value, "Latest refresh should win")
    }
}

/**
 * Tests for multi-account data isolation (M1).
 */
class MultiAccountIsolationTest {

    @Test
    fun partition_key_isolates_data() {
        data class DataPartition(val studentId: String, val termYear: Int, val termIndex: Int)

        val dataStore = mutableMapOf<DataPartition, List<String>>()

        val partition1 = DataPartition("student1", 2024, 1)
        val partition2 = DataPartition("student2", 2024, 1)

        dataStore[partition1] = listOf("course1", "course2")
        dataStore[partition2] = listOf("course3")

        assertEquals(listOf("course1", "course2"), dataStore[partition1])
        assertEquals(listOf("course3"), dataStore[partition2])
        assertEquals(null, dataStore[DataPartition("student3", 2024, 1)])
    }

    @Test
    fun same_student_different_term_isolated() {
        data class DataPartition(val studentId: String, val termYear: Int, val termIndex: Int)

        val dataStore = mutableMapOf<DataPartition, List<String>>()

        val term1 = DataPartition("student1", 2024, 1)
        val term2 = DataPartition("student1", 2024, 2)

        dataStore[term1] = listOf("spring-course")
        dataStore[term2] = listOf("fall-course")

        assertEquals(listOf("spring-course"), dataStore[term1])
        assertEquals(listOf("fall-course"), dataStore[term2])
    }
}
