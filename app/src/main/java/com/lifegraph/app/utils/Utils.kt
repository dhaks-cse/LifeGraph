package com.lifegraph.app.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    private val ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE
    private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d")
    private val DAY_FORMAT = DateTimeFormatter.ofPattern("EEE")
    private val FULL_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d")

    fun today(): String = LocalDate.now().format(ISO_FORMAT)

    fun yesterday(): String = LocalDate.now().minusDays(1).format(ISO_FORMAT)

    fun daysAgo(n: Long): String = LocalDate.now().minusDays(n).format(ISO_FORMAT)

    fun displayDate(isoDate: String): String = runCatching {
        LocalDate.parse(isoDate, ISO_FORMAT).format(DISPLAY_FORMAT)
    }.getOrDefault(isoDate)

    fun dayLabel(isoDate: String): String = runCatching {
        LocalDate.parse(isoDate, ISO_FORMAT).format(DAY_FORMAT)
    }.getOrDefault(isoDate)

    fun fullDate(isoDate: String): String = runCatching {
        LocalDate.parse(isoDate, ISO_FORMAT).format(FULL_FORMAT)
    }.getOrDefault(isoDate)

    fun last7Days(): List<String> = (6 downTo 0).map { daysAgo(it.toLong()) }

    fun daysBetween(from: String, to: String): Long = runCatching {
        ChronoUnit.DAYS.between(
            LocalDate.parse(from, ISO_FORMAT),
            LocalDate.parse(to, ISO_FORMAT)
        )
    }.getOrDefault(0L)
}

object ScoreUtils {
    fun productivityColor(score: Float): androidx.compose.ui.graphics.Color = when {
        score >= 70f -> androidx.compose.ui.graphics.Color(0xFF10B981) // green
        score >= 40f -> androidx.compose.ui.graphics.Color(0xFFF59E0B) // amber
        else -> androidx.compose.ui.graphics.Color(0xFFF43F5E)          // red
    }

    fun productivityLabel(score: Float): String = when {
        score >= 90f -> "Outstanding! 🏆"
        score >= 70f -> "Great work! 🎉"
        score >= 50f -> "Keep going! 💪"
        score >= 30f -> "You can do it! ⚡"
        else -> "Let's start! 🌱"
    }

    fun streakLabel(streak: Int): String = when {
        streak >= 30 -> "🔥 $streak day streak — Legendary!"
        streak >= 14 -> "🔥 $streak day streak — On fire!"
        streak >= 7 -> "🔥 $streak day streak — Amazing!"
        streak >= 3 -> "🔥 $streak day streak — Building up!"
        streak == 1 -> "🔥 1 day — Just started!"
        else -> "Start your streak today"
    }
}
