package vip.mystery0.xhu.timetable.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.FixedColorProvider
import kotlinx.datetime.isoDayNumber
import vip.mystery0.xhu.timetable.MainActivity
import vip.mystery0.xhu.timetable.widget.state.WeekCourseStateGlance
import vip.mystery0.xhu.timetable.widget.state.WeekGlanceStateDefinition
import vip.mystery0.xhu.timetable.widget.state.WidgetWeekItem
import java.text.DecimalFormat
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

class WeekGlanceAppWidget : GlanceAppWidget() {
    companion object {
        private val weekItemHeight = 48.dp
        private val dateItemWidth = 24.dp
        private val twoFormat = DecimalFormat("00")
    }

    override val stateDefinition: GlanceStateDefinition<WeekCourseStateGlance> =
        WeekGlanceStateDefinition()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val stateGlance = currentState<WeekCourseStateGlance>()
        val context = LocalContext.current
        val isDarkTheme = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val colors = WidgetTheme.getColors(isDarkTheme)

        Box(
            modifier = GlanceModifier
                .padding(8.dp)
                .fillMaxSize()
                .background(colors.surface),
        ) {
            Column {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        val dateString = stateGlance.date.let {
                            "${it.year}/${it.monthNumber}/${it.dayOfMonth}"
                        }
                        Text(
                            text = dateString,
                            style = TextStyle(
                                color = ColorProvider(colors.onSurface),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Text(
                            text = stateGlance.timeTitle,
                            style = TextStyle(
                                color = ColorProvider(colors.onSurface),
                                fontSize = 12.sp,
                            ),
                        )
                    }
                    Spacer(modifier = GlanceModifier.padding(8.dp).defaultWeight())
                    Text(
                        modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
                        text = "查看更多 >",
                        style = TextStyle(
                            color = ColorProvider(colors.onSurface),
                            fontSize = 12.sp,
                        ),
                    )
                }
                Spacer(modifier = GlanceModifier.height(8.dp))

                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    val firstDay = stateGlance.startDate
                    Text(
                        modifier = GlanceModifier.width(dateItemWidth),
                        text = "${twoFormat.format(firstDay.monthNumber)}\n月",
                        style = TextStyle(
                            color = ColorProvider(colors.onSurface),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                    for (i in 0..6) {
                        val thisDay = java.time.LocalDate.of(
                            firstDay.year,
                            firstDay.monthNumber,
                            firstDay.dayOfMonth
                        ).plusDays(i.toLong())
                        BuildDateItem(
                            week = thisDay.dayOfWeek.getDisplayName(
                                JavaTextStyle.SHORT,
                                Locale.CHINESE
                            ),
                            date = if (thisDay.dayOfMonth == 1) {
                                "${twoFormat.format(thisDay.monthValue)}月"
                            } else {
                                "${twoFormat.format(thisDay.dayOfMonth)}日"
                            },
                            isToday = stateGlance.date.dayOfWeek.isoDayNumber == i + 1,
                            colors = colors,
                        )
                    }
                }

                LazyColumn {
                    item {
                        Row {
                            Column(modifier = GlanceModifier.width(dateItemWidth)) {
                                for (time in 1..5) {
                                    BuildTimeItem(time = time, colors = colors)
                                }
                                BuildSingleTimeItem(colors = colors)
                            }
                            if (stateGlance.hasData) {
                                for (index in 0 until 7) {
                                    Column(modifier = GlanceModifier.defaultWeight()) {
                                        stateGlance.weekCourseList.getOrNull(index)?.forEach { sheet ->
                                            if (!sheet.isEmpty()) {
                                                BuildWeekItem(
                                                    backgroundColor = sheet.color,
                                                    itemStep = sheet.step,
                                                    title = sheet.showTitle,
                                                    textColor = sheet.textColor,
                                                    showMore = sheet.courseCount > 1,
                                                )
                                            } else {
                                                Spacer(
                                                    modifier = GlanceModifier
                                                        .fillMaxWidth()
                                                        .height(weekItemHeight * sheet.step),
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = "暂无数据",
                                        style = TextStyle(
                                            color = ColorProvider(colors.onSurface),
                                            fontSize = 14.sp,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BuildWeekItem(
        backgroundColor: Color,
        itemStep: Int,
        title: String,
        textColor: Color,
        showMore: Boolean,
    ) {
        Box(
            modifier = GlanceModifier
                .padding(1.dp)
                .fillMaxWidth()
                .height(weekItemHeight * itemStep),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(FixedColorProvider(backgroundColor)),
                text = title,
                style = TextStyle(
                    color = FixedColorProvider(textColor),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                ),
            )
        }
    }

    @Composable
    private fun RowScope.BuildDateItem(
        week: String,
        date: String,
        isToday: Boolean,
        colors: WidgetColors,
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = week,
                style = TextStyle(
                    color = ColorProvider(if (isToday) colors.primary else colors.onSurface),
                    fontSize = 10.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                )
            )
            Text(
                text = date,
                style = TextStyle(
                    color = ColorProvider(if (isToday) colors.primary else colors.onSurface),
                    fontSize = 10.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                )
            )
        }
    }

    @Composable
    private fun BuildTimeItem(time: Int, colors: WidgetColors) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(weekItemHeight * 2),
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(weekItemHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (2 * time - 1).toString(),
                    style = TextStyle(
                        color = ColorProvider(colors.onSurface),
                        fontSize = 10.sp
                    )
                )
            }
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(weekItemHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (2 * time).toString(),
                    style = TextStyle(
                        color = ColorProvider(colors.onSurface),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }

    @Composable
    private fun BuildSingleTimeItem(colors: WidgetColors) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(weekItemHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "11",
                style = TextStyle(
                    color = ColorProvider(colors.onSurface),
                    fontSize = 10.sp
                )
            )
        }
    }
}
