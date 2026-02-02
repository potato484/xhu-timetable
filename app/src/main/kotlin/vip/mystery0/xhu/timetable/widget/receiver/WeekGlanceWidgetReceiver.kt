package vip.mystery0.xhu.timetable.widget.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import vip.mystery0.xhu.timetable.widget.WeekGlanceAppWidget

class WeekGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = WeekGlanceAppWidget()
}
