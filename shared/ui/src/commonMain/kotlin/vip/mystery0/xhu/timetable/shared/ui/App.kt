package vip.mystery0.xhu.timetable.shared.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import vip.mystery0.xhu.timetable.shared.ui.auth.AuthEvent
import vip.mystery0.xhu.timetable.shared.ui.auth.AuthEventBus
import vip.mystery0.xhu.timetable.shared.ui.designsystem.XhuTimetableTheme
import vip.mystery0.xhu.timetable.shared.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.shared.ui.navigation.NavGraphBuilder
import vip.mystery0.xhu.timetable.shared.ui.navigation.Route
import vip.mystery0.xhu.timetable.shared.ui.navigation.replaceTo

@Composable
fun XhuTimetableApp() {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        AuthEventBus.events.collect { event ->
            when (event) {
                AuthEvent.SessionExpired -> {
                    navController.replaceTo<Route.Main, Route.Login>(Route.Login)
                }
            }
        }
    }

    XhuTimetableTheme {
        CompositionLocalProvider(LocalNavController provides navController) {
            NavHost(
                navController = navController,
                startDestination = Route.Init,
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(300),
                        initialOffset = { it / 5 },
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(300),
                        targetOffset = { it / 5 },
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(300),
                        initialOffset = { it / 5 },
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(300),
                        targetOffset = { it / 5 },
                    ) + fadeOut(animationSpec = tween(300))
                },
                builder = NavGraphBuilder,
            )
        }
    }
}
