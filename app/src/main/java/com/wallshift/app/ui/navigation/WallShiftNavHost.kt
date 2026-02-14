package com.wallshift.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wallshift.app.ui.home.HomeScreen
import com.wallshift.app.ui.home.HomeViewModel
import com.wallshift.app.ui.onboarding.OnboardingScreen
import com.wallshift.app.ui.onboarding.OnboardingViewModel
import com.wallshift.app.ui.settings.SettingsScreen
import com.wallshift.app.ui.settings.SettingsViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SETTINGS = "settings"
}

@Composable
fun WallShiftNavHost() {
    val navController = rememberNavController()
    val splashViewModel: OnboardingViewModel = hiltViewModel()
    val isOnboarded by splashViewModel.isOnboarded.collectAsState(initial = null)

    val startDestination = when (isOnboarded) {
        true -> Routes.HOME
        false -> Routes.ONBOARDING
        null -> return // Loading state, don't render nav yet
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessLow),
            ) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
                initialOffset = { it / 4 },
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
            ) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                targetOffset = { it / 4 },
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessLow),
            ) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
                initialOffset = { it / 4 },
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
            ) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                targetOffset = { it / 4 },
            )
        },
    ) {
        composable(Routes.ONBOARDING) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onOnboardingComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
            )
        }

        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
