package com.example.ui.screens

import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.MainViewModel

@Composable
fun AppNavigator(viewModel: MainViewModel, activity: FragmentActivity) {
    val navController = rememberNavController()
    
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()
    val biometricsEnabled by viewModel.biometricsEnabled.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    // Determine startup destination based on security preference and first launch
    val startDestination = remember(isFirstLaunch, biometricsEnabled, isAuthenticated) {
        when {
            isFirstLaunch -> "onboarding"
            biometricsEnabled && !isAuthenticated -> "auth"
            else -> "home"
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            OnboardingScreen(
                viewModel = viewModel,
                onOnboardingComplete = {
                    val target = if (viewModel.biometricsEnabled.value) "auth" else "home"
                    navController.navigate(target) {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("auth") {
            AuthScreen(
                activity = activity,
                onAuthenticated = {
                    viewModel.authenticate()
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                activity = activity
            )
        }
    }
}

