package vip.mystery0.xhu.timetable.shared.ui.examscore

import vip.mystery0.xhu.timetable.shared.domain.model.Exam
import vip.mystery0.xhu.timetable.shared.domain.model.ExpScore
import vip.mystery0.xhu.timetable.shared.domain.model.Score
import vip.mystery0.xhu.timetable.shared.domain.repository.Gpa

sealed interface ExamUiState {
    data object Loading : ExamUiState
    data object EmptyTerm : ExamUiState
    data class Loaded(
        val exams: List<Exam>,
        val tomorrowExams: List<Exam>,
    ) : ExamUiState
}

sealed interface ScoreUiState {
    data object Loading : ScoreUiState
    data object EmptyTerm : ScoreUiState
    data class Loaded(
        val scores: List<Score>,
        val expScores: List<ExpScore>,
        val gpa: Gpa?,
        val overallGpa: Gpa?,
    ) : ScoreUiState
}

sealed interface ExamScoreEvent {
    data object RefreshExams : ExamScoreEvent
    data object RefreshScores : ExamScoreEvent
}
