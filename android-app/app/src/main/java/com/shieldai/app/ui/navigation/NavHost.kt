package com.shieldai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shieldai.app.data.local.TokenManager
import com.shieldai.app.ui.screens.auth.LoginScreen
import com.shieldai.app.ui.screens.auth.RegisterScreen
import com.shieldai.app.ui.screens.dashboard.DashboardScreen
import com.shieldai.app.ui.screens.history.HistoryScreen
import com.shieldai.app.ui.screens.profile.ProfileScreen
import com.shieldai.app.ui.screens.reports.SubmitReportScreen
import com.shieldai.app.ui.screens.scan.ScanScreen

@Composable
fun ShieldAINavHost(
    navController: NavHostController = rememberNavController(),
    tokenManager: TokenManager = hiltViewModel<NavigationViewModel>().tokenManager
) {
    val isLoggedIn by tokenManager.isLoggedIn.collectAsState(initial = false)
    val startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Flow
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main App Flow
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToReports = { navController.navigate(Screen.SubmitReport.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Scan.route) {
            ScanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SubmitReport.route) {
            SubmitReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
