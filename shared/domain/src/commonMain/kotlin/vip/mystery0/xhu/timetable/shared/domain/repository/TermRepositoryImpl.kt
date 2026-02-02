package vip.mystery0.xhu.timetable.shared.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.platform.ioDispatcher
import vip.mystery0.xhu.timetable.settings.SettingKeys
import vip.mystery0.xhu.timetable.settings.SettingsStore
import vip.mystery0.xhu.timetable.shared.database.XhuTimetableDatabase
import vip.mystery0.xhu.timetable.shared.domain.model.Term
import vip.mystery0.xhu.timetable.shared.network.TermApi

class TermRepositoryImpl(
    private val termApi: TermApi,
    private val database: XhuTimetableDatabase,
    private val userRepository: UserRepository,
    private val settingsStore: SettingsStore,
) : TermRepository {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val refreshMutex = Mutex()

    private val _termList = MutableStateFlow<List<Term>>(emptyList())
    private val _currentTerm = MutableStateFlow<Term?>(null)
    private val _selectedTerm = MutableStateFlow<Term?>(null)

    override val selectedTerm: StateFlow<Term?> = _selectedTerm.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var selectedTermJob: Job? = null

    @Volatile
    private var refreshJob: Job? = null

    init {
        observeAccountAndLoadCache()
        observeSelectedTerm()
    }

    override fun getTermList(): Flow<List<Term>> =
        _termList.asStateFlow()

    override fun getCurrentTerm(): Flow<Term?> =
        _currentTerm.asStateFlow()

    override fun getSelectedTerm(studentId: String): Flow<Term?> =
        database.schemaQueries.selectSelectedTerm(studentId)
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .combine(_termList.asStateFlow()) { row, termList ->
                row?.let { dbRow ->
                    termList.firstOrNull {
                        it.termYear == dbRow.termYear.toInt() && it.termIndex == dbRow.termIndex.toInt()
                    }
                }
            }

    private fun observeAccountAndLoadCache() {
        scope.launch {
            userRepository.currentAccountContext
                .map { it?.studentId?.trim() }
                .distinctUntilChanged()
                .collect { studentId ->
                    if (studentId.isNullOrBlank()) {
                        refreshJob?.cancel()
                        refreshJob = null
                        _termList.value = emptyList()
                        _currentTerm.value = null
                        return@collect
                    }

                    // Account switched (or restored). Reset first to avoid leaking previous user's term list.
                    _termList.value = emptyList()
                    _currentTerm.value = null

                    val cached = loadTermListCache(studentId)
                    if (cached.isNotEmpty()) {
                        _termList.value = cached
                        val currentYear = settingsStore.get(SettingKeys.cachedCurrentTermYear(studentId))
                        val currentIndex = settingsStore.get(SettingKeys.cachedCurrentTermIndex(studentId))
                        _currentTerm.value = cached.firstOrNull { it.termYear == currentYear && it.termIndex == currentIndex }
                    }

                    // 切换/恢复账号后主动从服务器刷新学期列表，确保可选；并让 selectedTerm 能默认落到最新学期。
                    refreshJob?.cancel()
                    refreshJob = launch { refreshTermListFor(studentId) }
                }
        }
    }

    private fun observeSelectedTerm() {
        selectedTermJob?.cancel()
        selectedTermJob = scope.launch {
            userRepository.currentAccountContext
                .map { it?.studentId }
                .distinctUntilChanged()
                .flatMapLatest { studentId ->
                    if (studentId.isNullOrBlank()) {
                        flowOf(ResolvedSelectedTerm(studentId = null, selected = null, persistedYear = null, persistedIndex = null))
                    } else {
                        database.schemaQueries.selectSelectedTerm(studentId)
                            .asFlow()
                            .mapToOneOrNull(ioDispatcher)
                            .combine(_termList.asStateFlow()) { row, termList -> row to termList }
                            .combine(_currentTerm.asStateFlow()) { (row, termList), currentTerm ->
                                val persistedYear = row?.termYear?.toInt()
                                val persistedIndex = row?.termIndex?.toInt()
                                val persistedTerm = row?.let { dbRow ->
                                    termList.firstOrNull {
                                        it.termYear == dbRow.termYear.toInt() && it.termIndex == dbRow.termIndex.toInt()
                                    }
                                }

                                val latestTerm = termList.maxWithOrNull(
                                    compareBy<Term> { it.termYear }.thenBy { it.termIndex }
                                )
                                ResolvedSelectedTerm(
                                    studentId = studentId,
                                    selected = persistedTerm ?: currentTerm ?: latestTerm,
                                    persistedYear = persistedYear,
                                    persistedIndex = persistedIndex,
                                )
                            }
                    }
                }
                .collect { resolved ->
                    _selectedTerm.value = resolved.selected

                    val studentId = resolved.studentId ?: return@collect
                    val selected = resolved.selected ?: return@collect

                    val persistedYear = resolved.persistedYear
                    val persistedIndex = resolved.persistedIndex
                    val shouldPersist = persistedYear != selected.termYear || persistedIndex != selected.termIndex
                    if (shouldPersist) {
                        runCatching { selectTerm(studentId, selected.termYear, selected.termIndex) }
                    }
                }
        }
    }

    override suspend fun selectTerm(studentId: String, termYear: Int, termIndex: Int) {
        withContext(ioDispatcher) {
            database.schemaQueries.upsertSelectedTerm(
                studentId = studentId,
                termYear = termYear.toLong(),
                termIndex = termIndex.toLong(),
            )
        }
        _selectedTerm.value = _termList.value.firstOrNull {
            it.termYear == termYear && it.termIndex == termIndex
        }
    }

    override suspend fun refreshTermList(): Result<List<Term>> =
        refreshMutex.withLock { doRefreshTermList(userRepository.currentAccountContext.value?.studentId?.trim()) }

    private suspend fun refreshTermListFor(studentId: String): Result<List<Term>> =
        refreshMutex.withLock { doRefreshTermList(studentId) }

    private suspend fun doRefreshTermList(expectedStudentId: String?): Result<List<Term>> {
        return withContext(ioDispatcher) {
            try {
                val response = termApi.getCurrentTerm()

                val currentYear = response.nowYear
                val currentIndex = response.nowTerm
                val currentStartDate = response.startDate

                val enrollmentYear = resolveEnrollmentYear(expectedStudentId.orEmpty(), currentYear)
                    ?.coerceIn(currentYear - 10, currentYear)
                    ?: currentYear

                val termList = buildList {
                    for (year in enrollmentYear..currentYear) {
                        // 始终提供完整学年（第 1/2 学期）供用户选择。
                        // 服务器偶尔会在学期切换初期返回 nowTerm=1，导致第 2 学期缺失；
                        // 这里不再依赖 nowTerm 截断列表。
                        for (termIndex in 1..2) {
                            add(
                                Term(
                                    termYear = year,
                                    termIndex = termIndex,
                                    termName = displayName(year, termIndex),
                                    startDate = when {
                                        year == currentYear && termIndex == currentIndex -> currentStartDate
                                        termIndex == 1 -> LocalDate(year, 9, 1)
                                        else -> LocalDate(year + 1, 2, 1)
                                    },
                                )
                            )
                        }
                    }
                }.sortedWith(
                    compareByDescending<Term> { it.termYear }
                        .thenByDescending { it.termIndex }
                )

                if (!expectedStudentId.isNullOrBlank()) {
                    val activeStudentId = userRepository.currentAccountContext.value?.studentId?.trim()
                    if (activeStudentId != expectedStudentId) {
                        return@withContext Result.success(termList)
                    }
                }

                _termList.value = termList
                _currentTerm.value = termList.firstOrNull { it.termYear == currentYear && it.termIndex == currentIndex }

                persistTermListCache(expectedStudentId.orEmpty(), currentYear, currentIndex, termList)

                Result.success(termList)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    private suspend fun resolveEnrollmentYear(studentId: String, currentYear: Int): Int? {
        if (studentId.isBlank()) return null

        val gradeFromDb = withContext(ioDispatcher) {
            database.schemaQueries.selectUser(studentId).executeAsOneOrNull()?.xhuGrade?.toInt()
        }
        if (gradeFromDb != null && gradeFromDb in 2000..(currentYear + 1)) return gradeFromDb

        val yearFromStudentId = studentId.take(4).toIntOrNull()
        if (yearFromStudentId != null && yearFromStudentId in 2000..(currentYear + 1)) return yearFromStudentId

        return null
    }

    private fun displayName(year: Int, termIndex: Int): String = "${year}-${year + 1} 第${termIndex}学期"

    private suspend fun persistTermListCache(studentId: String, currentYear: Int, currentIndex: Int, termList: List<Term>) {
        if (studentId.isBlank()) return

        runCatching {
            val raw = json.encodeToString(termList.map { TermCacheItem.from(it) })
            settingsStore.set(SettingKeys.cachedTermList(studentId), raw)
            settingsStore.set(SettingKeys.cachedCurrentTermYear(studentId), currentYear)
            settingsStore.set(SettingKeys.cachedCurrentTermIndex(studentId), currentIndex)
        }
    }

    private suspend fun loadTermListCache(studentId: String): List<Term> {
        val raw = settingsStore.get(SettingKeys.cachedTermList(studentId))
        if (raw.isBlank()) return emptyList()

        val list = runCatching { json.decodeFromString<List<TermCacheItem>>(raw) }
            .getOrElse { emptyList() }

        return list.mapNotNull { it.toDomainOrNull() }
    }

    private data class ResolvedSelectedTerm(
        val studentId: String?,
        val selected: Term?,
        val persistedYear: Int?,
        val persistedIndex: Int?,
    )

    @Serializable
    private data class TermCacheItem(
        val termYear: Int,
        val termIndex: Int,
        val termName: String,
        val startDateIso: String,
    ) {
        fun toDomainOrNull(): Term? {
            val date = runCatching { LocalDate.parse(startDateIso) }.getOrNull() ?: return null
            return Term(
                termYear = termYear,
                termIndex = termIndex,
                termName = termName,
                startDate = date,
            )
        }

        companion object {
            fun from(term: Term): TermCacheItem {
                return TermCacheItem(
                    termYear = term.termYear,
                    termIndex = term.termIndex,
                    termName = term.termName,
                    startDateIso = term.startDate.toString(),
                )
            }
        }
    }
}
