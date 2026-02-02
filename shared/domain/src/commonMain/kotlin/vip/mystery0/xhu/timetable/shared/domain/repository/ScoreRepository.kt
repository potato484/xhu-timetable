package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import vip.mystery0.xhu.timetable.shared.domain.model.ExpScore
import vip.mystery0.xhu.timetable.shared.domain.model.Score

data class Gpa(
    val gpa: Double,
    val totalCredit: Double,
)

interface ScoreRepository {
    fun getScores(year: Int, term: Int): Flow<List<Score>>

    fun getGpa(year: Int, term: Int): Flow<Gpa?>

    fun getExpScores(year: Int, term: Int): Flow<List<ExpScore>>

    suspend fun refresh(year: Int, term: Int): Result<Unit>

    /**
     * Refresh GPA/credit summary for a specific term.
     * Used to compute overall GPA from enrollment to the selected term without loading full score lists.
     */
    suspend fun refreshGpa(year: Int, term: Int): Result<Gpa>
}
