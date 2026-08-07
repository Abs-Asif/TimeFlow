package com.dev.timeflow.View.Navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.timeflow.View.Screens.CalenderScreen
import com.dev.timeflow.View.Screens.onBoarding.FeatureScreen
import com.dev.timeflow.View.Screens.onBoarding.NotificationScreen
import com.dev.timeflow.View.Screens.onBoarding.WelcomeScreen

@Composable
fun NavGraph(modifier: Modifier = Modifier, startDest : String) {
    val navController = rememberNavController()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
    ) { p ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            NavHost(
                modifier = modifier
                    .fillMaxSize()
                    .padding(p),
                navController = navController,
                startDestination = startDest,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                composable(route = Routes.CalendarScreen.route) {
                    CalenderScreen()
                }

                composable(route = Routes.WelcomeScreen.route) {
                    WelcomeScreen(
                        onNavigate = {
                            navController.navigate(Routes.ShowFeaturesScreen.route)
                        }
                    )
                }

                composable(route = Routes.ShowFeaturesScreen.route) {
                    FeatureScreen(
                        onNavigate = {
                            navController.navigate(Routes.NotificationScreen.route)
                        }
                    )
                }

                composable(route = Routes.NotificationScreen.route) {
                    NotificationScreen(
                        onNavigate = {
                            navController.navigate(Routes.CalendarScreen.route)
                        }
                    )
                }
            }
        }
    }
}
