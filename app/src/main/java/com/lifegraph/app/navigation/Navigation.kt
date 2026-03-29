package com.lifegraph.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object AddHabit : Screen("add_habit")
    object Analytics : Screen("analytics")
    object Profile : Screen("profile")
    object MoodTracker : Screen("mood_tracker")
}
