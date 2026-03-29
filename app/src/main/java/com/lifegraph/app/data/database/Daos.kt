package com.lifegraph.app.data.database

import androidx.room.*
import com.lifegraph.app.model.Habit
import com.lifegraph.app.model.HabitLog
import com.lifegraph.app.model.MoodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdDate DESC")
    fun getAllActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("UPDATE habits SET streak = :streak WHERE id = :habitId")
    suspend fun updateStreak(habitId: Long, streak: Int)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: Long)

    @Query("UPDATE habits SET isActive = 0 WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Long)

    @Query("SELECT COUNT(*) FROM habits WHERE isActive = 1")
    suspend fun getTotalActiveHabits(): Int

    @Query("SELECT MAX(streak) FROM habits")
    suspend fun getBestStreak(): Int
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLogForDate(habitId: Long, date: String): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentLogs(habitId: Long, limit: Int): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date >= :startDate ORDER BY date ASC")
    fun getLogsFromDate(habitId: Long, startDate: String): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog)

    @Query("SELECT COUNT(*) FROM habit_logs WHERE date = :date AND completed = 1")
    suspend fun getCompletedCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM habit_logs WHERE date = :date")
    suspend fun getTotalLogsForDate(date: String): Int

    @Query("""
        SELECT date, 
               CAST(SUM(CASE WHEN completed = 1 THEN 1 ELSE 0 END) AS FLOAT) / 
               CAST(COUNT(*) AS FLOAT) * 100 as score
        FROM habit_logs 
        WHERE date >= :startDate 
        GROUP BY date 
        ORDER BY date ASC
    """)
    suspend fun getProductivityScores(startDate: String): List<DateScore>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getAllLogsForHabit(habitId: Long): List<HabitLog>
}

data class DateScore(val date: String, val score: Float)

@Dao
interface MoodLogDao {
    @Query("SELECT * FROM mood_logs WHERE date = :date LIMIT 1")
    suspend fun getMoodForDate(date: String): MoodLog?

    @Query("SELECT * FROM mood_logs ORDER BY date DESC LIMIT 30")
    fun getRecentMoods(): Flow<List<MoodLog>>

    @Query("SELECT * FROM mood_logs WHERE date >= :startDate ORDER BY date ASC")
    suspend fun getMoodsFromDate(startDate: String): List<MoodLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodLog)
}
