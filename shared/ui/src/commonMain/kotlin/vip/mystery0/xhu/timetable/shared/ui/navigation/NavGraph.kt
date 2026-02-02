package vip.mystery0.xhu.timetable.shared.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.koin.compose.koinInject
import vip.mystery0.xhu.timetable.shared.ui.about.AboutScreen
import vip.mystery0.xhu.timetable.shared.ui.account.AccountScreen
import vip.mystery0.xhu.timetable.shared.ui.account.AccountViewModel
import vip.mystery0.xhu.timetable.shared.ui.auth.LoginScreen
import vip.mystery0.xhu.timetable.shared.ui.background.BackgroundScreen
import vip.mystery0.xhu.timetable.shared.ui.background.BackgroundViewModel
import vip.mystery0.xhu.timetable.shared.ui.coursecolor.CourseColorScreen
import vip.mystery0.xhu.timetable.shared.ui.coursecolor.CourseColorViewModel
import vip.mystery0.xhu.timetable.shared.ui.feedback.FeedbackScreen
import vip.mystery0.xhu.timetable.shared.ui.feedback.FeedbackViewModel
import vip.mystery0.xhu.timetable.shared.ui.init.InitScreen
import vip.mystery0.xhu.timetable.shared.ui.init.InitViewModel
import vip.mystery0.xhu.timetable.shared.ui.init.SplashImageScreen
import vip.mystery0.xhu.timetable.shared.ui.init.SplashImageViewModel
import vip.mystery0.xhu.timetable.shared.ui.main.MainScreen
import vip.mystery0.xhu.timetable.shared.ui.notice.NoticeScreen
import vip.mystery0.xhu.timetable.shared.ui.notice.NoticeViewModel
import vip.mystery0.xhu.timetable.shared.ui.examscore.ExamListScreen
import vip.mystery0.xhu.timetable.shared.ui.examscore.ScoreListScreen
import vip.mystery0.xhu.timetable.shared.ui.examscore.ExamScoreViewModel
import vip.mystery0.xhu.timetable.shared.ui.schoolinfo.FreeRoomScreen
import vip.mystery0.xhu.timetable.shared.ui.schoolinfo.FreeRoomViewModel
import vip.mystery0.xhu.timetable.shared.ui.schoolinfo.SchoolCalendarScreen
import vip.mystery0.xhu.timetable.shared.ui.schoolinfo.SchoolCalendarViewModel
import vip.mystery0.xhu.timetable.shared.ui.schoolinfo.SchoolTimetableScreen
import vip.mystery0.xhu.timetable.shared.ui.schoolinfo.SchoolTimetableViewModel
import vip.mystery0.xhu.timetable.shared.ui.settings.ClassSettingsScreen
import vip.mystery0.xhu.timetable.shared.ui.settings.ClassSettingsViewModel
import vip.mystery0.xhu.timetable.shared.ui.settings.CustomUiScreen
import vip.mystery0.xhu.timetable.shared.ui.settings.CustomUiViewModel
import vip.mystery0.xhu.timetable.shared.ui.settings.SettingsScreen
import vip.mystery0.xhu.timetable.shared.ui.settings.SettingsViewModel

val LocalNavController = compositionLocalOf<NavController?> { null }

val NavGraphBuilder: NavGraphBuilder.() -> Unit = {
    composable<Route.Init> {
        val navController = LocalNavController.current
        val viewModel = koinInject<InitViewModel>()
        InitScreen(
            viewModel = viewModel,
            onNavigateToLogin = { navController?.replaceTo<Route.Init, Route.Login>(Route.Login) },
            onNavigateToMain = { navController?.replaceTo<Route.Init, Route.Main>(Route.Main) },
            onNavigateToSplash = { path, id ->
                navController?.replaceTo<Route.Init, Route.SplashImage>(Route.SplashImage(path, id))
            },
        )
    }
    composable<Route.SplashImage> { backStackEntry ->
        val route: Route.SplashImage = backStackEntry.toRoute()
        val navController = LocalNavController.current
        val viewModel = koinInject<SplashImageViewModel>()
        SplashImageScreen(
            viewModel = viewModel,
            splashFilePath = route.splashFilePath,
            splashId = route.splashId,
            onNavigateToMain = { navController?.replaceTo<Route.SplashImage, Route.Main>(Route.Main) },
        )
    }
    composable<Route.Login> {
        LoginScreen()
    }
    composable<Route.LoginFromManager> { backStackEntry ->
        val route: Route.LoginFromManager = backStackEntry.toRoute()
        LoginScreen(fromAccountManager = route.fromAccountManager)
    }
    composable<Route.Main> {
        MainScreen()
    }
    composable<Route.Settings> {
        val navController = LocalNavController.current
        val viewModel = koinInject<SettingsViewModel>()
        SettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
            onNavigateToClassSettings = { navController?.navigateTo(Route.ClassSettings) },
            onNavigateToCustomUi = { navController?.navigateTo(Route.CustomUi) },
            onNavigateToBackground = { navController?.navigateTo(Route.Background) },
            onNavigateToCourseColor = { navController?.navigateTo(Route.CourseColor) },
            onNavigateToAbout = { navController?.navigateTo(Route.About) },
        )
    }
    composable<Route.ClassSettings> {
        val navController = LocalNavController.current
        val viewModel = koinInject<ClassSettingsViewModel>()
        ClassSettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }
    composable<Route.CustomUi> {
        val navController = LocalNavController.current
        val viewModel = koinInject<CustomUiViewModel>()
        CustomUiScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }
    composable<Route.Notice> {
        val navController = LocalNavController.current
        val viewModel = koinInject<NoticeViewModel>()
        NoticeScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }
    composable<Route.Background> {
        val navController = LocalNavController.current
        val viewModel = koinInject<BackgroundViewModel>()
        BackgroundScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }
    composable<Route.AccountManagement> {
        val navController = LocalNavController.current
        val viewModel = koinInject<AccountViewModel>()
        AccountScreen(
            viewModel = viewModel,
            onNavigateToLogin = { navController?.navigateTo(Route.LoginFromManager()) },
            onNavigateBack = { navController?.popBackStack() },
        )
    }
    composable<Route.About> {
        val navController = LocalNavController.current
        AboutScreen(
            appName = "西瓜课表",
            versionName = "2.0.0",
            versionCode = 707,
            onNavigateBack = { navController?.popBackStack() },
        )
    }
    composable<Route.Feedback> {
        val navController = LocalNavController.current
        val viewModel = koinInject<FeedbackViewModel>()
        FeedbackScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }
    composable<Route.SchoolCalendar> {
        val navController = LocalNavController.current
        val viewModel = koinInject<SchoolCalendarViewModel>()
        SchoolCalendarScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }
    composable<Route.SchoolTimetable> {
        val navController = LocalNavController.current
        val viewModel = koinInject<SchoolTimetableViewModel>()
        SchoolTimetableScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }
    composable<Route.FreeRoom> {
        val navController = LocalNavController.current
        val viewModel = koinInject<FreeRoomViewModel>()
        FreeRoomScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }
    composable<Route.CourseColor> {
        val navController = LocalNavController.current
        val viewModel = koinInject<CourseColorViewModel>()
        CourseColorScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }

    composable<Route.QueryExam> {
        val navController = LocalNavController.current
        val viewModel = koinInject<ExamScoreViewModel>()
        ExamListScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }

    composable<Route.QueryScore> {
        val navController = LocalNavController.current
        val viewModel = koinInject<ExamScoreViewModel>()
        ScoreListScreen(
            viewModel = viewModel,
            onNavigateBack = { navController?.popBackStack() },
        )
    }

    composable<Route.QueryExpScore> {
        val navController = LocalNavController.current
        val viewModel = koinInject<ExamScoreViewModel>()
        ScoreListScreen(
            viewModel = viewModel,
            initialTabIndex = 1,
            onNavigateBack = { navController?.popBackStack() },
        )
    }
}

inline fun <reified T : Route> NavController.navigateTo(route: T) {
    navigate(route)
}

inline fun <reified F : Any, reified T : Route> NavController.replaceTo(target: T) {
    navigate(target) {
        popUpTo(F::class) {
            inclusive = true
        }
    }
}
