package vip.mystery0.xhu.timetable.shared.ui.timetable

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlin.time.Duration.Companion.days
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.platform.todayBeijing
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseColorRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomCourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.SettingsRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.domain.usecase.AggregationUseCase
import vip.mystery0.xhu.timetable.shared.domain.usecase.TimetableAggregation
import vip.mystery0.xhu.timetable.shared.domain.util.WeekCalculator
import vip.mystery0.xhu.timetable.shared.ui.base.BaseViewModel
import vip.mystery0.xhu.timetable.shared.ui.base.ErrorHandler

class TimetableViewModel(
    private val userRepository: UserRepository,
    private val termRepository: TermRepository,
    private val settingsRepository: SettingsRepository,
    private val courseRepository: CourseRepository,
    private val customCourseRepository: CustomCourseRepository,
    private val courseColorRepository: CourseColorRepository,
    private val aggregationUseCase: AggregationUseCase,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<TimetableUiState>(TimetableUiState.Loading)
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message: SharedFlow<String> = _message.asSharedFlow()

    private val selectedWeek = MutableStateFlow(0)
    private val selectedDate = MutableStateFlow(Clock.System.todayBeijing())

    @Volatile
    private var latestTermStartDate: LocalDate? = null
    @Volatile
    private var latestTotalWeeks: Int = TOTAL_WEEKS
    @Volatile
    private var latestTermKey: String? = null
    @Volatile
    private var latestObservedStartDate: LocalDate? = null

    init {
        viewModelScope.launch {
            combine(
                userRepository.currentAccountContext,
                termRepository.selectedTerm,
            ) { ctx, term -> ctx to term }
                .flatMapLatest { (ctx, term) ->
                    if (ctx == null || term == null) {
                        return@flatMapLatest flowOf(TimetableUiState.EmptyTerm)
                    }

                    val termKey = "${ctx.studentId}_${term.termYear}_${term.termIndex}"
                    if (latestTermKey != termKey) {
                        latestTermKey = termKey
                        latestObservedStartDate = null
                        selectedWeek.value = 0
                        selectedDate.value = Clock.System.todayBeijing()
                    }

                    val partition = DataPartition(
                        studentId = ctx.studentId,
                        termYear = term.termYear,
                        termIndex = term.termIndex,
                    )

                    val startDateFlow = settingsRepository
                        .observeTermStartDateOverride(ctx.studentId, term.termYear, term.termIndex)
                        .map { it ?: term.startDate }
                        .distinctUntilChanged()
                        .onEach { startDate ->
                            // 修改开学日期后，默认回到“本周/今天”，避免仍停留在旧周次导致看起来没变化。
                            val last = latestObservedStartDate
                            if (last != null && last != startDate) {
                                selectedWeek.value = 0
                                selectedDate.value = Clock.System.todayBeijing()
                            }
                            latestObservedStartDate = startDate
                        }

                    val weekDayMarkersFlow = combine(
                        courseRepository.getCourses(partition),
                        customCourseRepository.getCustomCourses(partition),
                    ) { courseData, customCourses ->
                        buildSet {
                            courseData.courseList.forEach { course ->
                                course.weekList.forEach { week -> add(week to course.day.isoDayNumber) }
                            }
                            courseData.experimentCourseList.forEach { course ->
                                course.weekList.forEach { week -> add(week to course.day.isoDayNumber) }
                            }
                            customCourses.forEach { course ->
                                course.weekList.forEach { week -> add(week to course.day.isoDayNumber) }
                            }
                        }
                    }

                    combine(
                        startDateFlow,
                        selectedWeek,
                        selectedDate,
                        courseColorRepository.observeColorMap(ctx.studentId),
                        weekDayMarkersFlow,
                    ) { startDate, week, date, colorMap, markers -> Params(startDate, week, date, colorMap, markers) }
                        .flatMapLatest { (startDate, week, date, colorMap, markers) ->
                            val today = Clock.System.todayBeijing()
                            val currentWeekRaw = WeekCalculator.calculateWeekNumber(startDate, today)

                            val maxWeekFromData = markers.maxOfOrNull { it.first } ?: 0
                            val totalWeeks = maxOf(TOTAL_WEEKS, maxWeekFromData).coerceAtLeast(1)

                            // 默认周次：未开学 -> 第1周；已结束 -> 最后一周；学期中 -> 当前周。
                            val autoWeek = when {
                                currentWeekRaw <= 0 -> 1
                                currentWeekRaw > totalWeeks -> totalWeeks
                                else -> currentWeekRaw
                            }

                            val effectiveWeekRaw = if (week <= 0) autoWeek else week
                            val effectiveWeek = effectiveWeekRaw.coerceIn(1, totalWeeks)

                            latestTermStartDate = startDate
                            latestTotalWeeks = totalWeeks

                            aggregationUseCase.aggregateTimetable(partition, effectiveWeek)
                                .map { aggregation: TimetableAggregation ->
                                    val cacheStale = isCacheStale(partition)
                                    TimetableUiState.Loaded(
                                        items = aggregation.items,
                                        practicalCourses = aggregation.practicalCourses,
                                        courseColorMap = colorMap,
                                        weekDayWithCourses = markers,
                                        currentDate = today,
                                        selectedDate = date,
                                        currentWeek = currentWeekRaw,
                                        selectedWeek = effectiveWeek,
                                        totalWeeks = totalWeeks,
                                        termStartDate = startDate,
                                        cacheStaleWarning = cacheStale,
                                    )
                                }
                        }
                }
                .collect { _uiState.value = it }
        }
    }

    private data class Params(
        val startDate: LocalDate,
        val week: Int,
        val date: LocalDate,
        val colorMap: Map<String, String>,
        val markers: Set<Pair<Int, Int>>,
    )

    fun onEvent(event: TimetableEvent) {
        when (event) {
            is TimetableEvent.SelectWeek -> updateSelectedWeek(event.week)
            is TimetableEvent.SelectDate -> updateSelectedDate(event.date)
            is TimetableEvent.Refresh -> refresh()
        }
    }

    private fun updateSelectedDate(date: LocalDate) {
        selectedDate.value = date

        val termStartDate = latestTermStartDate ?: termRepository.selectedTerm.value?.startDate ?: return
        val week = WeekCalculator.calculateWeekNumber(termStartDate, date).coerceAtLeast(1)
        val maxWeeks = latestTotalWeeks.coerceAtLeast(1)
        selectedWeek.value = week.coerceIn(1, maxWeeks)
    }

    private fun updateSelectedWeek(week: Int) {
        val maxWeeks = latestTotalWeeks.coerceAtLeast(1)
        val clampedWeek = week.coerceIn(1, maxWeeks)
        selectedWeek.value = clampedWeek

        val termStartDate = latestTermStartDate ?: termRepository.selectedTerm.value?.startDate ?: return
        val startOfWeek = termStartDate.plus((clampedWeek - 1) * 7, DateTimeUnit.DAY)

        val targetDay = selectedDate.value.dayOfWeek
        val deltaDays = (targetDay.ordinal - startOfWeek.dayOfWeek.ordinal + 7) % 7
        selectedDate.value = startOfWeek.plus(deltaDays, DateTimeUnit.DAY)
    }

    private fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            try {
                val ctx = userRepository.currentAccountContext.value ?: run {
                    _message.tryEmit("请先登录")
                    return@launch
                }
                val term = termRepository.selectedTerm.value ?: run {
                    _message.tryEmit("请先选择学期")
                    return@launch
                }
                val partition = DataPartition(
                    studentId = ctx.studentId,
                    termYear = term.termYear,
                    termIndex = term.termIndex,
                )
                val courseResult = courseRepository.refresh(partition)
                val customCourseResult = customCourseRepository.refresh(partition)

                courseResult.exceptionOrNull()?.let { throwable ->
                    _message.tryEmit(ErrorHandler.getDisplayMessage(throwable))
                }
                customCourseResult.exceptionOrNull()?.let { throwable ->
                    _message.tryEmit(ErrorHandler.getDisplayMessage(throwable))
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun isCacheStale(partition: DataPartition): Boolean {
        val lastSync = courseRepository.getLastSyncAt(partition) ?: return true
        val now = Clock.System.now()
        return (now - lastSync) > CACHE_STALE_THRESHOLD
    }

    companion object {
        private val CACHE_STALE_THRESHOLD = 7.days
        private const val TOTAL_WEEKS = 20
    }
}
