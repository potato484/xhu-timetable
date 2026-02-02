package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import vip.mystery0.xhu.timetable.shared.domain.model.Term

interface TermRepository {
    val selectedTerm: StateFlow<Term?>

    fun getTermList(): Flow<List<Term>>

    fun getCurrentTerm(): Flow<Term?>

    fun getSelectedTerm(studentId: String): Flow<Term?>

    suspend fun selectTerm(studentId: String, termYear: Int, termIndex: Int)

    suspend fun refreshTermList(): Result<List<Term>>
}
