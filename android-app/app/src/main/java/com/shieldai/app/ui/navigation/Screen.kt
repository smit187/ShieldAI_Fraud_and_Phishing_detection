package com.shieldai.app.ui.navigation

/**
 * Navigation route definitions for the ShieldAI app.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Scan : Screen("scan")
    object ScanResult : Screen("scan_result/{scanId}") {
        fun createRoute(scanId: String) = "scan_result/$scanId"
    }
    object History : Screen("history")
    object Reports : Screen("reports")
    object SubmitReport : Screen("submit_report")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}
