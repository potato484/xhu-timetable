package vip.mystery0.xhu.timetable.shared.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.koin.compose.koinInject
import vip.mystery0.xhu.timetable.shared.ui.base.NetworkStatusViewModel
import vip.mystery0.xhu.timetable.shared.ui.base.OfflineBanner
import vip.mystery0.xhu.timetable.shared.ui.profile.ProfileScreen
import vip.mystery0.xhu.timetable.shared.ui.timetable.TimetableViewModel
import vip.mystery0.xhu.timetable.shared.ui.timetable.TodayCourseScreen
import vip.mystery0.xhu.timetable.shared.ui.timetable.WeekCourseScreen
import vip.mystery0.xhu.timetable.shared.ui.timetable.CalendarScreen

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    MainScreenContent(modifier = modifier)
}

@Composable
private fun MainScreenContent(
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Today) }
    val timetableViewModel = koinInject<TimetableViewModel>()
    val networkStatusViewModel = koinInject<NetworkStatusViewModel>()
    val isOnline by networkStatusViewModel.isOnline.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OfflineBanner(isOffline = !isOnline)
            when (selectedTab) {
                MainTab.Today -> TodayCourseScreen(
                    viewModel = timetableViewModel,
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToCalendar = { selectedTab = MainTab.Calendar },
                )
                MainTab.Week -> WeekCourseScreen(
                    viewModel = timetableViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
                MainTab.Calendar -> CalendarScreen(
                    viewModel = timetableViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
                MainTab.Profile -> ProfileScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

private enum class MainTab(val label: String, val icon: ImageVector) {
    Today("今日", Icons.Default.Home),
    Week("本周", Icons.Default.List),
    Calendar("月历", Icons.Default.DateRange),
    Profile("我的", Icons.Default.Person),
}
