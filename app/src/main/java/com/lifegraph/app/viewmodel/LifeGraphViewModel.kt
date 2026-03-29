package com.lifegraph.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lifegraph.app.data.repository.LifeGraphRepository
import com.lifegraph.app.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LifeGraphViewModel(
    private val repository: LifeGraphRepository
) : ViewModel() {

    // ─── Dashboard State ──────────────────────────────────────────────────────

    private val _habitsWithLogs = MutableStateFlow<List<HabitWithLog>>(emptyList())
    val habitsWithLogs: StateFlow<List<HabitWithLog>> = _habitsWithLogs.asStateFlow()

    private val _productivityScore = MutableStateFlow(0f)
    val productivityScore: StateFlow<Float> = _productivityScore.asStateFlow()

    private val _todayMood = MutableStateFlow<MoodLog?>(null)
    val todayMood: StateFlow<MoodLog?> = _todayMood.asStateFlow()

    // ─── Analytics State ──────────────────────────────────────────────────────

    private val _weeklyStats = MutableStateFlow<List<DailyStats>>(emptyList())
    val weeklyStats: StateFlow<List<DailyStats>> = _weeklyStats.asStateFlow()

    private val _recentMoods = MutableStateFlow<List<MoodLog>>(emptyList())
    val recentMoods: StateFlow<List<MoodLog>> = _recentMoods.asStateFlow()

    // ─── Profile State ────────────────────────────────────────────────────────

    private val _totalHabits = MutableStateFlow(0)
    val totalHabits: StateFlow<Int> = _totalHabits.asStateFlow()

    private val _bestStreak = MutableStateFlow(0)
    val bestStreak: StateFlow<Int> = _bestStreak.asStateFlow()

    private val _averageProductivity = MutableStateFlow(0f)
    val averageProductivity: StateFlow<Float> = _averageProductivity.asStateFlow()

    // ─── UI State ─────────────────────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val greeting: String
        get() {
            val hour = java.time.LocalTime.now().hour
            return when {
                hour < 12 -> "Good Morning ☀️"
                hour < 17 -> "Good Afternoon 🌤️"
                else -> "Good Evening 🌙"
            }
        }

    val todayFormatted: String
        get() = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    init {
        loadDashboard()
        loadAnalytics()
        loadProfileStats()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            repository.getHabitsWithTodayLogs().collect { list ->
                _habitsWithLogs.value = list
                _productivityScore.value = repository.getTodayProductivityScore()
            }
        }
        viewModelScope.launch {
            repository.getRecentMoods().collect { moods ->
                _recentMoods.value = moods
            }
        }
        viewModelScope.launch {
            _todayMood.value = repository.getTodayMood()
        }
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _weeklyStats.value = repository.getWeeklyDailyStats()
        }
    }

    private fun loadProfileStats() {
        viewModelScope.launch {
            _totalHabits.value = repository.getTotalActiveHabits()
            _bestStreak.value = repository.getBestStreak()
            _averageProductivity.value = repository.getAverageProductivityScore()
        }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    fun addHabit(
        name: String,
        targetValue: Int,
        targetUnit: String,
        iconEmoji: String,
        colorHex: String,
        reminderTime: String?
    ) {
        viewModelScope.launch {
            repository.insertHabit(
                Habit(
                    name = name,
                    targetValue = targetValue,
                    targetUnit = targetUnit,
                    iconEmoji = iconEmoji,
                    colorHex = colorHex,
                    reminderTime = reminderTime
                )
            )
            loadProfileStats()
        }
    }

    fun toggleHabit(habitId: Long, targetValue: Int) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(
                habitId = habitId,
                date = LocalDate.now().toString(),
                targetValue = targetValue
            )
            _productivityScore.value = repository.getTodayProductivityScore()
            loadAnalytics()
        }
    }

    fun saveMood(mood: MoodType) {
        viewModelScope.launch {
            repository.saveMood(mood)
            _todayMood.value = repository.getTodayMood()
            loadAnalytics()
        }
    }

    fun archiveHabit(habitId: Long) {
        viewModelScope.launch {
            repository.archiveHabit(habitId)
            loadProfileStats()
        }
    }

    fun refreshAll() {
        loadDashboard()
        loadAnalytics()
        loadProfileStats()
    }

    class Factory(private val repository: LifeGraphRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LifeGraphViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LifeGraphViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
