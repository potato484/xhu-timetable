package vip.mystery0.xhu.timetable.shared.ui.coursecolor

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseColorRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomCourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.TermRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.UserRepository
import vip.mystery0.xhu.timetable.shared.ui.base.MviViewModel
import vip.mystery0.xhu.timetable.shared.ui.designsystem.CourseColorPool
import vip.mystery0.xhu.timetable.db.DataPartition

class CourseColorViewModel(
    private val userRepository: UserRepository,
    private val termRepository: TermRepository,
    private val courseRepository: CourseRepository,
    private val customCourseRepository: CustomCourseRepository,
    private val courseColorRepository: CourseColorRepository,
) : MviViewModel<CourseColorUiState, CourseColorEvent, CourseColorEffect>(CourseColorUiState()) {

    init {
        observeCourses()
    }

    private fun observeCourses() {
        combine(
            userRepository.currentAccountContext,
            termRepository.selectedTerm,
        ) { ctx, term ->
            if (ctx == null || term == null) {
                return@combine flowOf(emptyList<CourseColorItem>() to emptyMap<String, String>())
            }

            val partition = DataPartition(
                studentId = ctx.studentId,
                termYear = term.termYear,
                termIndex = term.termIndex,
            )

            combine(
                courseRepository.getCourses(partition),
                customCourseRepository.getCustomCourses(partition),
                courseColorRepository.observeColorMap(ctx.studentId),
            ) { courseData, customCourses, colorMap ->
                val courseNames = mutableSetOf<String>()
                courseData.courseList.forEach { courseNames.add(it.courseName) }
                courseData.experimentCourseList.forEach { courseNames.add(it.courseName) }
                customCourses.forEach { courseNames.add(it.courseName) }

                val items = courseNames.sorted().map { name ->
                    val customColorHex = colorMap[name]
                    val color = if (customColorHex != null) {
                        CourseColorPool.fromHex(customColorHex) ?: CourseColorPool.hash(name)
                    } else {
                        CourseColorPool.hash(name)
                    }
                    CourseColorItem(
                        courseName = name,
                        color = color,
                        isCustom = customColorHex != null,
                    )
                }
                items to colorMap
            }
        }.flatMapLatest { it }
            .onEach { (items, _) ->
                val query = currentState.searchQuery
                val filtered = if (query.isBlank()) items else items.filter { it.courseName.contains(query, ignoreCase = true) }
                setState { copy(courses = filtered, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    override fun handleEvent(event: CourseColorEvent) {
        when (event) {
            CourseColorEvent.Load -> { /* Already observing */ }
            is CourseColorEvent.Search -> search(event.query)
            is CourseColorEvent.SetColor -> setColor(event.courseName, event.color)
            is CourseColorEvent.ResetColor -> resetColor(event.courseName)
            CourseColorEvent.ResetAllColors -> resetAllColors()
        }
    }

    private fun search(query: String) {
        setState { copy(searchQuery = query) }
        observeCourses()
    }

    private fun setColor(courseName: String, color: Color) {
        viewModelScope.launch {
            val ctx = userRepository.currentAccountContext.value ?: return@launch
            val colorHex = CourseColorPool.toHex(color)
            courseColorRepository.setCourseColor(ctx.studentId, courseName, colorHex)
            emitEffect(CourseColorEffect.ShowMessage("颜色已更新"))
        }
    }

    private fun resetColor(courseName: String) {
        viewModelScope.launch {
            val ctx = userRepository.currentAccountContext.value ?: return@launch
            courseColorRepository.removeCourseColor(ctx.studentId, courseName)
            emitEffect(CourseColorEffect.ShowMessage("已恢复默认颜色"))
        }
    }

    private fun resetAllColors() {
        viewModelScope.launch {
            val ctx = userRepository.currentAccountContext.value ?: return@launch
            courseColorRepository.clearAllColors(ctx.studentId)
            emitEffect(CourseColorEffect.ShowMessage("已重置所有课程颜色"))
        }
    }
}
