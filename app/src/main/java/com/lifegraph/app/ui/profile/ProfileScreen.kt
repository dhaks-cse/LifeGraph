package com.lifegraph.app.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifegraph.app.ui.dashboard.BottomNavigationBar
import com.lifegraph.app.ui.dashboard.ProductivityCard
import com.lifegraph.app.ui.theme.*
import com.lifegraph.app.viewmodel.LifeGraphViewModel
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: LifeGraphViewModel,
    onDashboard: () -> Unit,
    onAnalytics: () -> Unit
) {
    val totalHabits by viewModel.totalHabits.collectAsState()
    val bestStreak by viewModel.bestStreak.collectAsState()
    val averageProductivity by viewModel.averageProductivity.collectAsState()
    val habitsWithLogs by viewModel.habitsWithLogs.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "profile",
                onDashboard = onDashboard,
                onAnalytics = onAnalytics,
                onProfile = {}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile header
            ProfileHeader()

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    emoji = "📋",
                    value = "$totalHabits",
                    label = "Total\nHabits",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    emoji = "🔥",
                    value = "$bestStreak",
                    label = "Best\nStreak",
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    emoji = "📊",
                    value = "${averageProductivity.toInt()}%",
                    label = "Avg\nScore",
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Habits Summary
            if (habitsWithLogs.isNotEmpty()) {
                Text(
                    "Active Habits",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                habitsWithLogs.forEach { habitWithLog ->
                    HabitSummaryRow(
                        emoji = habitWithLog.habit.iconEmoji,
                        name = habitWithLog.habit.name,
                        streak = habitWithLog.habit.streak,
                        colorHex = habitWithLog.habit.colorHex,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Badges / Achievements section
            Text(
                "Achievements",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            AchievementsRow(
                bestStreak = bestStreak,
                totalHabits = totalHabits,
                avgScore = averageProductivity,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(top = 52.dp, bottom = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        Brush.linearGradient(listOf(Indigo500, Purple500)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🌟", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "My Growth Journey",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Keep building great habits!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HabitSummaryRow(
    emoji: String,
    name: String,
    streak: Int,
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🔥", fontSize = 16.sp)
                Text(
                    "$streak",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                )
            }
        }
    }
}

@Composable
private fun AchievementsRow(
    bestStreak: Int,
    totalHabits: Int,
    avgScore: Float,
    modifier: Modifier = Modifier
) {
    data class Achievement(val emoji: String, val title: String, val desc: String, val unlocked: Boolean)

    val achievements = listOf(
        Achievement("🏆", "First Habit", "Created your first habit", totalHabits >= 1),
        Achievement("🔥", "Week Warrior", "7-day streak on any habit", bestStreak >= 7),
        Achievement("💯", "Perfect Day", "100% productivity score", avgScore >= 100f),
        Achievement("🌟", "Habit Master", "5 habits at once", totalHabits >= 5),
        Achievement("⚡", "Consistent", "30-day streak", bestStreak >= 30),
        Achievement("🎯", "On Target", "Avg 70%+ score", avgScore >= 70f)
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        achievements.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { badge ->
                    AchievementBadge(
                        emoji = badge.emoji,
                        title = badge.title,
                        desc = badge.desc,
                        unlocked = badge.unlocked,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    emoji: String,
    title: String,
    desc: String,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (unlocked) emoji else "🔒",
                fontSize = 24.sp,
                modifier = if (!unlocked) Modifier.alpha(0.4f) else Modifier
            )
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (unlocked)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                )
                Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (unlocked) 1f else 0.4f
                    )
                )
            }
        }
    }
}


