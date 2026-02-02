package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import vip.mystery0.xhu.timetable.shared.domain.model.ExpScore
import vip.mystery0.xhu.timetable.shared.domain.model.Score
import vip.mystery0.xhu.timetable.shared.domain.model.toDomainExpScore
import vip.mystery0.xhu.timetable.shared.domain.model.toDomainScore
import vip.mystery0.xhu.timetable.shared.network.ScoreApi
import vip.mystery0.xhu.timetable.shared.network.model.GpaResponse
import vip.mystery0.xhu.timetable.shared.network.model.PageResult

class ScoreRepositoryImpl(
    private val scoreApi: ScoreApi,
    private val userRepository: UserRepository,
) : ScoreRepository {

    private data class CacheKey(
        val studentId: String,
        val year: Int,
        val term: Int,
    )

    private val refreshMutexes = mutableMapOf<CacheKey, Mutex>()
    private val refreshMutexGlobal = Mutex()

    private val scoreCache = MutableStateFlow<Map<CacheKey, List<Score>>>(emptyMap())
    private val gpaCache = MutableStateFlow<Map<CacheKey, Gpa>>(emptyMap())
    private val expScoreCache = MutableStateFlow<Map<CacheKey, List<ExpScore>>>(emptyMap())

    private suspend fun getRefreshMutex(key: CacheKey): Mutex =
        refreshMutexGlobal.withLock {
            refreshMutexes.getOrPut(key) { Mutex() }
        }

    override fun getScores(year: Int, term: Int): Flow<List<Score>> {
        val selectedKeyFlow = userRepository.currentAccountContext
            .map { ctx -> ctx?.let { CacheKey(it.studentId, year, term) } }

        return combine(selectedKeyFlow, scoreCache) { key, cacheMap ->
            if (key == null) emptyList() else cacheMap[key].orEmpty()
        }
    }

    override fun getGpa(year: Int, term: Int): Flow<Gpa?> {
        val selectedKeyFlow = userRepository.currentAccountContext
            .map { ctx -> ctx?.let { CacheKey(it.studentId, year, term) } }

        return combine(selectedKeyFlow, gpaCache) { key, cacheMap ->
            if (key == null) null else cacheMap[key]
        }
    }

    override fun getExpScores(year: Int, term: Int): Flow<List<ExpScore>> {
        val selectedKeyFlow = userRepository.currentAccountContext
            .map { ctx -> ctx?.let { CacheKey(it.studentId, year, term) } }

        return combine(selectedKeyFlow, expScoreCache) { key, cacheMap ->
            if (key == null) emptyList() else cacheMap[key].orEmpty()
        }
    }

    override suspend fun refresh(year: Int, term: Int): Result<Unit> {
        val ctx = userRepository.currentAccountContext.value ?: return Result.failure(noAccountError())
        val key = CacheKey(ctx.studentId, year, term)
        val mutex = getRefreshMutex(key)
        return mutex.withLock { doRefreshAll(key) }
    }

    override suspend fun refreshGpa(year: Int, term: Int): Result<Gpa> {
        val ctx = userRepository.currentAccountContext.value ?: return Result.failure(noAccountError())
        val key = CacheKey(ctx.studentId, year, term)
        val mutex = getRefreshMutex(key)
        return mutex.withLock { doRefreshGpa(key) }
    }

    private suspend fun doRefreshAll(key: CacheKey): Result<Unit> {
        return withContext(userRepository.accountCoroutineContext()) {
            try {
                val scores = fetchAllScores(key.year, key.term)
                val gpa = fetchGpa(key.year, key.term)
                val expScores = scoreApi.experimentScoreList(key.year, key.term).map { it.toDomainExpScore() }

                scoreCache.value = scoreCache.value.toMutableMap().apply { put(key, scores) }.toMap()
                gpaCache.value = gpaCache.value.toMutableMap().apply { put(key, gpa) }.toMap()
                expScoreCache.value = expScoreCache.value.toMutableMap().apply { put(key, expScores) }.toMap()

                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    private suspend fun doRefreshScores(key: CacheKey): Result<List<Score>> {
        return withContext(userRepository.accountCoroutineContext()) {
            try {
                val scores = fetchAllScores(key.year, key.term)
                scoreCache.value = scoreCache.value.toMutableMap().apply { put(key, scores) }.toMap()
                Result.success(scores)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    private suspend fun doRefreshGpa(key: CacheKey): Result<Gpa> {
        return withContext(userRepository.accountCoroutineContext()) {
            try {
                val gpa = fetchGpa(key.year, key.term)
                gpaCache.value = gpaCache.value.toMutableMap().apply { put(key, gpa) }.toMap()
                Result.success(gpa)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    private suspend fun doRefreshExpScores(key: CacheKey): Result<List<ExpScore>> {
        return withContext(userRepository.accountCoroutineContext()) {
            try {
                val expScores = scoreApi.experimentScoreList(key.year, key.term).map { it.toDomainExpScore() }
                expScoreCache.value = expScoreCache.value.toMutableMap().apply { put(key, expScores) }.toMap()
                Result.success(expScores)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchAllScores(year: Int, term: Int): List<Score> {
        val result = mutableListOf<Score>()
        var pageIndex = 0

        while (true) {
            val page: PageResult<vip.mystery0.xhu.timetable.shared.network.model.ScoreResponse> =
                scoreApi.scoreList(year = year, term = term, index = pageIndex, size = 100)

            result += page.items.map { it.toDomainScore() }

            // 服务端分页采用 {current,total,items,hasNext}；客户端请求 index 仍使用 0-based.
            // 为避免服务端 current 基准变化导致偏移错误，这里仅依赖 hasNext 自增翻页。
            if (!page.hasNext || page.items.isEmpty()) break
            pageIndex += 1
        }

        return result
    }

    private suspend fun fetchGpa(year: Int, term: Int): Gpa {
        val response: GpaResponse = scoreApi.gpa(year = year, term = term)
        return Gpa(gpa = response.gpa, totalCredit = response.totalCredit)
    }

    private fun noAccountError(): IllegalStateException = IllegalStateException("No active account")
}
