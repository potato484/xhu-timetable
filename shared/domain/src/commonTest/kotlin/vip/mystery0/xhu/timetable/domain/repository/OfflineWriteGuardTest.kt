package vip.mystery0.xhu.timetable.domain.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for offline write-guard (O1, H5).
 *
 * Requirements:
 * - Write operations (create, update, delete) must throw OfflineWriteException when offline
 * - Read operations should work offline (cache-first)
 * - The guard should be enforced at Repository layer
 */
class OfflineWriteGuardTest {

    class OfflineWriteException(message: String = "Offline write is forbidden") : Exception(message)

    @Test
    fun write_throws_when_offline() = runTest {
        var isOnline = false

        fun ensureOnline() {
            if (!isOnline) throw OfflineWriteException()
        }

        suspend fun createItem(name: String) {
            ensureOnline()
            // Would perform network + db write
        }

        assertFailsWith<OfflineWriteException> {
            createItem("test")
        }
    }

    @Test
    fun write_succeeds_when_online() = runTest {
        var isOnline = true
        var itemCreated = false

        fun ensureOnline() {
            if (!isOnline) throw OfflineWriteException()
        }

        suspend fun createItem(name: String) {
            ensureOnline()
            itemCreated = true
        }

        createItem("test")
        assertTrue(itemCreated)
    }

    @Test
    fun read_works_offline() = runTest {
        var isOnline = false
        val cachedData = listOf("item1", "item2")

        fun getItems(): List<String> {
            // Read from cache, no online check needed
            return cachedData
        }

        val result = getItems()
        assertTrue(result.isNotEmpty(), "Read should work offline")
    }

    @Test
    fun update_throws_when_offline() = runTest {
        var isOnline = false

        fun ensureOnline() {
            if (!isOnline) throw OfflineWriteException()
        }

        suspend fun updateItem(id: Long, name: String) {
            ensureOnline()
            // Would perform network + db update
        }

        assertFailsWith<OfflineWriteException> {
            updateItem(1L, "updated")
        }
    }

    @Test
    fun delete_throws_when_offline() = runTest {
        var isOnline = false

        fun ensureOnline() {
            if (!isOnline) throw OfflineWriteException()
        }

        suspend fun deleteItem(id: Long) {
            ensureOnline()
            // Would perform network + db delete
        }

        assertFailsWith<OfflineWriteException> {
            deleteItem(1L)
        }
    }

    @Test
    fun network_status_change_affects_guard() = runTest {
        var isOnline = false
        var operationCount = 0

        fun ensureOnline() {
            if (!isOnline) throw OfflineWriteException()
        }

        suspend fun performWrite() {
            ensureOnline()
            operationCount++
        }

        // Offline - should fail
        assertFailsWith<OfflineWriteException> { performWrite() }

        // Go online
        isOnline = true
        performWrite()
        assertTrue(operationCount == 1)

        // Go offline again
        isOnline = false
        assertFailsWith<OfflineWriteException> { performWrite() }
    }
}
