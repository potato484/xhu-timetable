package vip.mystery0.xhu.timetable.shared.ui.examscore

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import vip.mystery0.xhu.timetable.shared.domain.model.Exam
import vip.mystery0.xhu.timetable.shared.domain.model.Term
import vip.mystery0.xhu.timetable.shared.domain.repository.ExamRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.Gpa
import vip.mystery0.xhu.timetable.shared.domain.repository.ScoreRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.ui.base.BaseViewModel
import vip.mystery0.xhu.timetable.shared.ui.base.ErrorHandler

class ExamScoreViewModel(
    private val userRepository: UserRepository,
    private val termRepository: TermRepository,
    private val examRepository: ExamRepository,
    private val scoreRepository: ScoreRepository,
) : BaseViewModel() {
    @Volatile
    private var overallGpaPrefetchJob: Job? = null

    @Volatile
    private var overallGpaPrefetchStudentId: String? = null

    private val overallGpaPrefetchedKeys = mutableSetOf<String>()

    private val _examUiState = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
    val examUiState: StateFlow<ExamUiState> = _examUiState.asStateFlow()

    private val _scoreUiState = MutableStateFlow<ScoreUiState>(ScoreUiState.Loading)
    val scoreUiState: StateFlow<ScoreUiState> = _scoreUiState.asStateFlow()

    private val _isExamRefreshing = MutableStateFlow(false)
    val isExamRefreshing: StateFlow<Boolean> = _isExamRefreshing.asStateFlow()

    private val _isScoreRefreshing = MutableStateFlow(false)
    val isScoreRefreshing: StateFlow<Boolean> = _isScoreRefreshing.asStateFlow()

    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message: SharedFlow<String> = _message.asSharedFlow()

    init {
        observeExams()
        observeScores()
    }

    private fun examStatusIndex(exam: Exam, now: Instant): Int {
        return when {
            now < exam.examStartTimeMills -> 0
            now > exam.examEndTimeMills -> 2
            else -> 1
        }
    }

    private fun observeExams() {
        viewModelScope.launch {
            combine(
                userRepository.currentAccountContext,
                termRepository.selectedTerm,
            ) { ctx, term ->
                if (ctx == null || term == null) {
                    return@combine flowOf(ExamUiState.EmptyTerm)
                }
                combine(
                    examRepository.getExams(term.termYear, term.termIndex),
                    examRepository.getTomorrowExams(term.termYear, term.termIndex),
                ) { exams, tomorrowExams ->
                    val now = Clock.System.now()
                    ExamUiState.Loaded(
                        exams = exams.sortedWith(
                            compareBy<Exam> { examStatusIndex(it, now) }
                                .thenBy { it.examStartTimeMills },
                        ),
                        tomorrowExams = tomorrowExams.sortedBy { it.examStartTimeMills },
                    )
                }
            }.flatMapLatest { it }.collect { _examUiState.value = it }
        }
    }

    private fun observeScores() {
        viewModelScope.launch {
            combine(
                userRepository.currentAccountContext,
                termRepository.selectedTerm,
            ) { ctx, term ->
                if (ctx == null || term == null) {
                    return@combine flowOf(ScoreUiState.EmptyTerm)
                }

                val overallTermsFlow = termRepository.getTermList()
                    .map { terms ->
                        terms.filter { it.isNotAfter(term) }
                            .sortedWith(compareBy<Term> { it.termYear }.thenBy { it.termIndex })
                    }
                    .distinctUntilChanged()

                val overallGpaFlow = overallTermsFlow
                    .flatMapLatest { terms ->
                        // 预取历史学期 GPA（用于“累计绩点/学分”），避免仅显示当前学期的情况。
                        startOverallGpaPrefetch(studentId = ctx.studentId, terms = terms)

                        if (terms.isEmpty()) return@flatMapLatest flowOf(null)

                        val flows = terms.map { t -> scoreRepository.getGpa(t.termYear, t.termIndex) }
                        combine(flows) { gpas: Array<Gpa?> ->
                            // 只有在所有学期的 GPA 都已加载后，才计算累计绩点/学分，避免展示“看起来等于当学期”的错误值。
                            if (gpas.any { it == null }) null else computeOverallGpa(gpas.toList())
                        }
                    }

                combine(
                    scoreRepository.getScores(term.termYear, term.termIndex),
                    scoreRepository.getGpa(term.termYear, term.termIndex),
                    scoreRepository.getExpScores(term.termYear, term.termIndex),
                    overallGpaFlow,
                ) { scores, gpa, expScores, overallGpa ->
                    ScoreUiState.Loaded(
                        scores = scores,
                        expScores = expScores.sortedWith(
                            compareBy<vip.mystery0.xhu.timetable.shared.domain.model.ExpScore> { it.courseName }
                                .thenBy { it.teachingClassName },
                        ),
                        gpa = gpa,
                        overallGpa = overallGpa,
                    )
                }
            }.flatMapLatest { it }.collect { _scoreUiState.value = it }
        }
    }

    private fun startOverallGpaPrefetch(
        studentId: String,
        terms: List<Term>,
    ) {
        if (terms.isEmpty()) return

        if (overallGpaPrefetchStudentId != studentId) {
            overallGpaPrefetchStudentId = studentId
            overallGpaPrefetchedKeys.clear()
        }

        overallGpaPrefetchJob?.cancel()
        overallGpaPrefetchJob = viewModelScope.launch {
            for (t in terms) {
                val key = "${studentId}_${t.termYear}_${t.termIndex}"
                if (overallGpaPrefetchedKeys.contains(key)) continue

                val result = scoreRepository.refreshGpa(t.termYear, t.termIndex)
                if (result.isSuccess) {
                    overallGpaPrefetchedKeys.add(key)
                }
            }
        }
    }

    private fun Term.isNotAfter(other: Term): Boolean {
        return termYear < other.termYear || (termYear == other.termYear && termIndex <= other.termIndex)
    }

    private fun computeOverallGpa(gpas: List<Gpa?>): Gpa? {
        val data = gpas.filterNotNull().filter { it.totalCredit > 0.0 }
        if (data.isEmpty()) return null

        val totalCredit = data.sumOf { it.totalCredit }
        if (totalCredit <= 0.0) return null

        val weighted = data.sumOf { it.gpa * it.totalCredit }
        return Gpa(
            gpa = weighted / totalCredit,
            totalCredit = totalCredit,
        )
    }

    fun onEvent(event: ExamScoreEvent) {
        when (event) {
            is ExamScoreEvent.RefreshExams -> refreshExams()
            is ExamScoreEvent.RefreshScores -> refreshScores()
        }
    }

    private fun refreshExams() {
        if (_isExamRefreshing.value) return
        viewModelScope.launch {
            if (_isExamRefreshing.value) return@launch
            _isExamRefreshing.value = true
            try {
                userRepository.currentAccountContext.value ?: run {
                    _message.tryEmit("请先登录")
                    return@launch
                }
                val term = termRepository.selectedTerm.value ?: run {
                    _message.tryEmit("请先选择学期")
                    return@launch
                }
                val result = examRepository.refresh(term.termYear, term.termIndex)
                result.exceptionOrNull()?.let { throwable ->
                    _message.tryEmit(ErrorHandler.getDisplayMessage(throwable))
                }
            } finally {
                _isExamRefreshing.value = false
            }
        }
    }

    private fun refreshScores() {
        if (_isScoreRefreshing.value) return
        viewModelScope.launch {
            if (_isScoreRefreshing.value) return@launch
            _isScoreRefreshing.value = true
            try {
                userRepository.currentAccountContext.value ?: run {
                    _message.tryEmit("请先登录")
                    return@launch
                }
                val term = termRepository.selectedTerm.value ?: run {
                    _message.tryEmit("请先选择学期")
                    return@launch
                }
                val result = scoreRepository.refresh(term.termYear, term.termIndex)
                result.exceptionOrNull()?.let { throwable ->
                    _message.tryEmit(ErrorHandler.getDisplayMessage(throwable))
                }
            } finally {
                _isScoreRefreshing.value = false
            }
        }
    }
}
