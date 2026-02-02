package vip.mystery0.xhu.timetable.db

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Atomic transaction interface for partition-level replace-all operations.
 *
 * DESIGN NOTES:
 * - Uses BEGIN IMMEDIATE to prevent writer deadlocks
 * - Per-partition Mutex ensures in-process serialization
 * - REQUIRES WAL mode on SqlDriver for non-blocking concurrent reads
 *
 * @see SqlDelightAtomicTransaction for implementation
 */
interface AtomicTransaction {
    suspend fun <T> replaceAll(
        partition: DataPartition,
        tableName: String,
        block: suspend () -> T,
    ): T

    suspend fun <T> replaceAllByKey(
        key: String,
        tableName: String,
        block: suspend () -> T,
    ): T
}

/**
 * SQLite implementation using BEGIN IMMEDIATE + Mutex.
 *
 * IMPORTANT: This bypasses SQLDelight's Transacter abstraction intentionally
 * to use BEGIN IMMEDIATE (vs default DEFERRED) for write locking. This class
 * MUST be the only write entry point for partitioned tables.
 *
 * PREREQUISITE: Configure SqlDriver with WAL mode:
 * ```
 * driver.execute(null, "PRAGMA journal_mode=WAL", 0)
 * ```
 */
class SqlDelightAtomicTransaction(
    private val driver: SqlDriver,
    private val ioContext: CoroutineContext,
) : AtomicTransaction {

    private val partitionMutexes = mutableMapOf<DataPartition, Mutex>()
    private val keyMutexes = mutableMapOf<String, Mutex>()
    private val globalMutex = Mutex()

    private suspend fun getMutex(partition: DataPartition): Mutex {
        globalMutex.withLock {
            return partitionMutexes.getOrPut(partition) { Mutex() }
        }
    }

    private suspend fun getMutexByKey(key: String): Mutex {
        globalMutex.withLock {
            return keyMutexes.getOrPut(key) { Mutex() }
        }
    }

    override suspend fun <T> replaceAll(
        partition: DataPartition,
        tableName: String,
        block: suspend () -> T,
    ): T {
        val mutex = getMutex(partition)
        return mutex.withLock {
            executeTransaction(block)
        }
    }

    override suspend fun <T> replaceAllByKey(
        key: String,
        tableName: String,
        block: suspend () -> T,
    ): T {
        val mutex = getMutexByKey(key)
        return mutex.withLock {
            executeTransaction(block)
        }
    }

    private suspend fun <T> executeTransaction(block: suspend () -> T): T {
        return withContext(ioContext) {
            driver.execute(null, "BEGIN IMMEDIATE TRANSACTION", 0)
            try {
                val result = block()
                driver.execute(null, "COMMIT TRANSACTION", 0)
                result
            } catch (e: Throwable) {
                driver.execute(null, "ROLLBACK TRANSACTION", 0)
                throw e
            }
        }
    }
}
