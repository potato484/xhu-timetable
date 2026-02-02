package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import vip.mystery0.xhu.timetable.shared.domain.model.Exam
import vip.mystery0.xhu.timetable.shared.domain.model.toDomainExam
import vip.mystery0.xhu.timetable.shared.network.ExamApi
import vip.mystery0.xhu.timetable.shared.network.model.PageResult

class ExamRepositoryImpl(
    private val examApi: ExamApi,
    private val userRepository: UserRepository,
) : ExamRepository {

    private data class CacheKey(
        val studentId: String,
        val year: Int,
        val term: Int,
    )

    private val refreshMutexes = mutableMapOf<CacheKey, Mutex>()
    private val refreshMutexGlobal = Mutex()

    private val cache = MutableStateFlow<Map<CacheKey, List<Exam>>>(emptyMap())
    private val tomorrowCache = MutableStateFlow<Map<CacheKey, List<Exam>>>(emptyMap())

    private suspend fun getRefreshMutex(key: CacheKey): Mutex =
        refreshMutexGlobal.withLock {
            refreshMutexes.getOrPut(key) { Mutex() }
        }

    override fun getExams(year: Int, term: Int): Flow<List<Exam>> {
        val selectedKeyFlow = userRepository.currentAccountContext
            .map { ctx -> ctx?.let { CacheKey(it.studentId, year, term) } }

        return combine(selectedKeyFlow, cache) { key, cacheMap ->
            if (key == null) emptyList() else cacheMap[key].orEmpty()
        }
    }

    override fun getTomorrowExams(year: Int, term: Int): Flow<List<Exam>> {
        val selectedKeyFlow = userRepository.currentAccountContext
            .map { ctx -> ctx?.let { CacheKey(it.studentId, year, term) } }

        return combine(selectedKeyFlow, tomorrowCache) { key, cacheMap ->
            if (key == null) emptyList() else cacheMap[key].orEmpty()
        }
    }

    override suspend fun refresh(year: Int, term: Int): Result<List<Exam>> {
        val ctx = userRepository.currentAccountContext.value ?: return Result.failure(noAccountError())
        val key = CacheKey(ctx.studentId, year, term)
        val mutex = getRefreshMutex(key)
        return mutex.withLock { doRefresh(key) }
    }

    private suspend fun doRefresh(key: CacheKey): Result<List<Exam>> {
        return withContext(userRepository.accountCoroutineContext()) {
            try {
                val list = fetchAllExams(key.year, key.term)
                val tomorrowList = examApi.tomorrowExamList(key.year, key.term).map { it.toDomainExam() }
                cache.value = cache.value.toMutableMap().apply { put(key, list) }.toMap()
                tomorrowCache.value = tomorrowCache.value.toMutableMap().apply { put(key, tomorrowList) }.toMap()
                Result.success(list)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchAllExams(year: Int, term: Int): List<Exam> {
        val result = mutableListOf<Exam>()
        var pageIndex = 0

        while (true) {
            val page: PageResult<vip.mystery0.xhu.timetable.shared.network.model.ExamResponse> =
                examApi.examList(year = year, term = term, index = pageIndex, size = 100)

            result += page.items.map { it.toDomainExam() }

            if (!page.hasNext || page.items.isEmpty()) break
            pageIndex += 1
        }

        return result
    }

    private fun noAccountError(): IllegalStateException = IllegalStateException("No active account")
}
