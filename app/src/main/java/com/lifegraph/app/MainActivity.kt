package com.lifegraph.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifegraph.app.data.database.LifeGraphDatabase
import com.lifegraph.app.data.repository.LifeGraphRepository
import com.lifegraph.app.navigation.Screen
import com.lifegraph.app.ui.addhabit.AddHabitScreen
import com.lifegraph.app.ui.analytics.AnalyticsScreen
import com.lifegraph.app.ui.dashboard.DashboardScreen
import com.lifegraph.app.ui.moodtracker.MoodTrackerScreen
import com.lifegraph.app.ui.onboarding.OnboardingScreen
import com.lifegraph.app.ui.profile.ProfileScreen
import com.lifegraph.app.ui.splash.SplashScreen
import com.lifegraph.app.ui.theme.LifeGraphTheme
import com.lifegraph.app.viewmodel.LifeGraphViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lifegraph_prefs")
private val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = LifeGraphDatabase.getDatabase(this)
        val repo = LifeGraphRepository(db.habitDao(), db.habitLogDao(), db.moodLogDao())

        setContent {
            LifeGraphTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()

                // Observe onboarding state
                val onboardingDone by produceState(initialValue = false) {
                    dataStore.data.map { prefs ->
                        prefs[ONBOARDING_DONE_KEY] ?: false
                    }.collect { value = it }
                }

                val viewModel: LifeGraphViewModel = viewModel(
                    factory = LifeGraphViewModel.Factory(repo)
                )

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onFinished = {
                                val destination = if (onboardingDone) {
                                    Screen.Dashboard.route
                                } else {
                                    Screen.Onboarding.route
                                }
                                navController.navigate(destination) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Onboarding.route) {
                        OnboardingScreen(
                            onFinished = {
                                scope.launch {
                                    dataStore.edit { prefs ->
                                        prefs[ONBOARDING_DONE_KEY] = true
                                    }
                                }
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Dashboard.route) {
                        DashboardScreen(
                            viewModel = viewModel,
                            onAddHabit = { navController.navigate(Screen.AddHabit.route) },
                            onMoodTracker = { navController.navigate(Screen.MoodTracker.route) },
                            onAnalytics = {
                                navController.navigate(Screen.Analytics.route) {
                                    launchSingleTop = true
                                }
                            },
                            onProfile = {
                                navController.navigate(Screen.Profile.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(Screen.AddHabit.route) {
                        AddHabitScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.MoodTracker.route) {
                        MoodTrackerScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Analytics.route) {
                        AnalyticsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            viewModel = viewModel,
                            onDashboard = {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onAnalytics = {
                                navController.navigate(Screen.Analytics.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
