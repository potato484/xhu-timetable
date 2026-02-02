package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import vip.mystery0.xhu.timetable.shared.domain.model.Exam

interface ExamRepository {
    fun getExams(year: Int, term: Int): Flow<List<Exam>>

    fun getTomorrowExams(year: Int, term: Int): Flow<List<Exam>>

    suspend fun refresh(year: Int, term: Int): Result<List<Exam>>
}

