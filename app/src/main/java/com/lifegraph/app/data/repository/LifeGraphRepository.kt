package com.lifegraph.app.data.repository

import com.lifegraph.app.data.database.DateScore
import com.lifegraph.app.data.database.HabitDao
import com.lifegraph.app.data.database.HabitLogDao
import com.lifegraph.app.data.database.MoodLogDao
import com.lifegraph.app.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class LifeGraphRepository(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val moodLogDao: MoodLogDao
) {

    // ─── Habits ──────────────────────────────────────────────────────────────

    fun getAllActiveHabits(): Flow<List<Habit>> = habitDao.getAllActiveHabits()

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun archiveHabit(habitId: Long) = habitDao.archiveHabit(habitId)

    suspend fun getTotalActiveHabits(): Int = habitDao.getTotalActiveHabits()

    suspend fun getBestStreak(): Int = habitDao.getBestStreak()

    // ─── HabitLogs ───────────────────────────────────────────────────────────

    suspend fun toggleHabitCompletion(habitId: Long, date: String, targetValue: Int) {
        val existing = habitLogDao.getLogForDate(habitId, date)
        if (existing == null || !existing.completed) {
            habitLogDao.insertLog(
                HabitLog(
                    habitId = habitId,
                    date = date,
                    completed = true,
                    completedValue = targetValue
                )
            )
            recalculateStreak(habitId)
        } else {
            habitLogDao.insertLog(existing.copy(completed = false, completedValue = 0))
            recalculateStreak(habitId)
        }
    }

    private suspend fun recalculateStreak(habitId: Long) {
        val logs = habitLogDao.getAllLogsForHabit(habitId)
        val logMap = logs.associate { it.date to it.completed }

        var streak = 0
        var date = LocalDate.now()

        // If today is not completed, start checking from yesterday
        if (logMap[date.toString()] != true) {
            date = date.minusDays(1)
        }

        while (logMap[date.toString()] == true) {
            streak++
            date = date.minusDays(1)
        }

        habitDao.updateStreak(habitId, streak)
    }

    fun getHabitsWithTodayLogs(): Flow<List<HabitWithLog>> {
        val today = LocalDate.now().toString()
        return habitDao.getAllActiveHabits().map { habits ->
            habits.map { habit ->
                val log = habitLogDao.getLogForDate(habit.id, today)
                HabitWithLog(habit, log)
            }
        }
    }

    suspend fun getTodayProductivityScore(): Float {
        val today = LocalDate.now().toString()
        val completed = habitLogDao.getCompletedCountForDate(today)
        val total = habitDao.getTotalActiveHabits()
        return if (total > 0) (completed.toFloat() / total.toFloat()) * 100f else 0f
    }

    suspend fun getWeeklyProductivityScores(): List<DateScore> {
        val startDate = LocalDate.now().minusDays(6).toString()
        return habitLogDao.getProductivityScores(startDate)
    }

    suspend fun getHabitCompletionRate(habitId: Long): Float {
        val logs = habitLogDao.getRecentLogs(habitId, 30)
        if (logs.isEmpty()) return 0f
        return logs.count { it.completed }.toFloat() / logs.size.toFloat() * 100f
    }

    // ─── Mood ─────────────────────────────────────────────────────────────────

    fun getRecentMoods(): Flow<List<MoodLog>> = moodLogDao.getRecentMoods()

    suspend fun getTodayMood(): MoodLog? = moodLogDao.getMoodForDate(LocalDate.now().toString())

    suspend fun saveMood(mood: MoodType, note: String = "") {
        moodLogDao.insertMood(
            MoodLog(
                date = LocalDate.now().toString(),
                moodType = mood,
                note = note
            )
        )
    }

    suspend fun getMoodsFromDate(startDate: String): List<MoodLog> =
        moodLogDao.getMoodsFromDate(startDate)

    // ─── Analytics ───────────────────────────────────────────────────────────

    suspend fun getWeeklyDailyStats(): List<DailyStats> {
        val result = mutableListOf<DailyStats>()
        val productivityScores = getWeeklyProductivityScores()
        val scoreMap = productivityScores.associate { it.date to it.score }
        val startDate = LocalDate.now().minusDays(6).toString()
        val moodList = getMoodsFromDate(startDate)
        val moodMap = moodList.associate { it.date to it.moodType.score }

        for (i in 6 downTo 0) {
            val date = LocalDate.now().minusDays(i.toLong()).toString()
            val score = scoreMap[date] ?: 0f
            val moodScore = moodMap[date] ?: 0
            val completed = habitLogDao.getCompletedCountForDate(date)
            val total = habitLogDao.getTotalLogsForDate(date)
            result.add(DailyStats(date, score, completed, total, moodScore))
        }
        return result
    }

    suspend fun getAverageProductivityScore(): Float {
        val scores = getWeeklyProductivityScores()
        return if (scores.isEmpty()) 0f else scores.map { it.score }.average().toFloat()
    }
}
