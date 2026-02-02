package vip.mystery0.xhu.timetable.shared.ui.timetable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vip.mystery0.xhu.timetable.shared.network.model.PracticalCourse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PracticalCourseSheet(
    open: Boolean,
    courses: List<PracticalCourse>,
    courseColorMap: Map<String, String>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!open) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        if (courses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("暂无实践课程")
            }
            return@ModalBottomSheet
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "header") {
                Text(
                    text = "实践课程列表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            items(
                items = courses,
                key = { "${it.courseName}_${it.teacher}_${it.weekStr}" },
            ) { course ->
                val color = courseColorFor(course.courseName, courseColorMap)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = course.courseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = color,
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (course.teacher.isNotBlank()) {
                            Text(
                                text = course.teacher,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            if (course.weekStr.isNotBlank()) {
                                Text(
                                    text = course.weekStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (course.credit > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${course.credit} 学分",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

