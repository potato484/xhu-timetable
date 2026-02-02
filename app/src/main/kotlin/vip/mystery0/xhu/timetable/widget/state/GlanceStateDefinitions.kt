package vip.mystery0.xhu.timetable.widget.state

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.glance.state.GlanceStateDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import vip.mystery0.xhu.timetable.widget.WidgetDataProvider
import java.io.File

class TodayGlanceStateDefinition : GlanceStateDefinition<TodayCourseStateGlance> {
    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<TodayCourseStateGlance> = TodayCourseDataStore()

    override fun getLocation(context: Context, fileKey: String): File {
        return File(context.cacheDir, "today_widget_state")
    }
}

class TodayCourseDataStore : DataStore<TodayCourseStateGlance> {
    private val dataFlow = MutableStateFlow(TodayCourseStateGlance.EMPTY)

    override val data: Flow<TodayCourseStateGlance> = flow {
        emit(WidgetDataProvider.getTodayState())
    }

    override suspend fun updateData(
        transform: suspend (t: TodayCourseStateGlance) -> TodayCourseStateGlance
    ): TodayCourseStateGlance = dataFlow.value
}

class WeekGlanceStateDefinition : GlanceStateDefinition<WeekCourseStateGlance> {
    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<WeekCourseStateGlance> = WeekCourseDataStore()

    override fun getLocation(context: Context, fileKey: String): File {
        return File(context.cacheDir, "week_widget_state")
    }
}

class WeekCourseDataStore : DataStore<WeekCourseStateGlance> {
    private val dataFlow = MutableStateFlow(WeekCourseStateGlance.EMPTY)

    override val data: Flow<WeekCourseStateGlance> = flow {
        emit(WidgetDataProvider.getWeekState())
    }

    override suspend fun updateData(
        transform: suspend (t: WeekCourseStateGlance) -> WeekCourseStateGlance
    ): WeekCourseStateGlance = dataFlow.value
}
