package vip.mystery0.xhu.timetable.shared.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import vip.mystery0.xhu.timetable.db.DataPartition
import vip.mystery0.xhu.timetable.shared.domain.model.TimetableItem
import vip.mystery0.xhu.timetable.shared.domain.repository.CourseRepository
import vip.mystery0.xhu.timetable.shared.domain.repository.CustomCourseRepository
import vip.mystery0.xhu.timetable.shared.network.model.PracticalCourse

data class TimetableAggregation(
    val items: List<TimetableItem>,
    val practicalCourses: List<PracticalCourse>,
)

interface AggregationUseCase {
    fun aggregateTimetable(partition: DataPartition, week: Int): Flow<TimetableAggregation>
}

class AggregationUseCaseImpl(
    private val courseRepository: CourseRepository,
    private val customCourseRepository: CustomCourseRepository,
) : AggregationUseCase {

    override fun aggregateTimetable(partition: DataPartition, week: Int): Flow<TimetableAggregation> =
        combine(
            courseRepository.getCourses(partition),
            customCourseRepository.getCustomCourses(partition),
        ) { courseData, customCourses ->
            val practicalCourses = courseData.practicalCourseList
                .sortedBy { it.courseName }

            if (week <= 0) {
                return@combine TimetableAggregation(
                    items = emptyList(),
                    practicalCourses = practicalCourses,
                )
            }

            val itemsById = LinkedHashMap<String, TimetableItem>()

            courseData.courseList.forEach { course ->
                if (week !in course.weekList) return@forEach
                val item = TimetableItem.CourseItem(course)
                itemsById[item.id] = item
            }

            courseData.experimentCourseList.forEach { course ->
                if (week !in course.weekList) return@forEach
                val item = TimetableItem.ExperimentCourseItem(course)
                itemsById[item.id] = item
            }

            customCourses.forEach { course ->
                if (week !in course.weekList) return@forEach
                val item = TimetableItem.CustomCourseItem(course)
                itemsById[item.id] = item
            }

            val items = itemsById.values.sortedWith(
                compareBy<TimetableItem> { it.dayIndex }
                    .thenBy { it.startDayTime }
                    .thenBy { it.endDayTime },
            )

            TimetableAggregation(
                items = items,
                practicalCourses = practicalCourses,
            )
        }
}
