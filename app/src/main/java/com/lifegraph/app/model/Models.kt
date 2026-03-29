package com.lifegraph.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetValue: Int,
    val targetUnit: String, // "minutes" or "count"
    val streak: Int = 0,
    val iconEmoji: String = "⭐",
    val colorHex: String = "#6366F1",
    val reminderTime: String? = null,
    val createdDate: String = LocalDate.now().toString(),
    val isActive: Boolean = true
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String,
    val completed: Boolean,
    val completedValue: Int = 0
)

@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val moodType: MoodType,
    val note: String = ""
)

enum class MoodType(val emoji: String, val label: String, val score: Int) {
    HAPPY("😊", "Happy", 3),
    NEUTRAL("😐", "Neutral", 2),
    SAD("😞", "Sad", 1)
}

data class HabitWithLog(
    val habit: Habit,
    val todayLog: HabitLog?
) {
    val isCompletedToday: Boolean get() = todayLog?.completed == true
    val todayProgress: Int get() = todayLog?.completedValue ?: 0
}

data class DailyStats(
    val date: String,
    val productivityScore: Float,
    val completedHabits: Int,
    val totalHabits: Int,
    val moodScore: Int
)
