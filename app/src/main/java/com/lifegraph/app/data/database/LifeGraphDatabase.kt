package com.lifegraph.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.lifegraph.app.model.Habit
import com.lifegraph.app.model.HabitLog
import com.lifegraph.app.model.MoodLog

@Database(
    entities = [Habit::class, HabitLog::class, MoodLog::class],
    version = 1,
    exportSchema = false
)
abstract class LifeGraphDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun moodLogDao(): MoodLogDao

    companion object {
        @Volatile
        private var INSTANCE: LifeGraphDatabase? = null

        fun getDatabase(context: Context): LifeGraphDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeGraphDatabase::class.java,
                    "lifegraph_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
