
package com.lifegraph.app.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.lifegraph.app.model.*
import com.lifegraph.app.viewmodel.LifeGraphViewModel
import com.lifegraph.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: LifeGraphViewModel,
    onAddHabit: () -> Unit,
    onMoodTracker: () -> Unit,
    onAnalytics: () -> Unit,
    onProfile: () -> Unit
) {

    val habitsWithLogs by viewModel.habitsWithLogs.collectAsState()
    val productivityScore by viewModel.productivityScore.collectAsState()
    val todayMood by viewModel.todayMood.collectAsState()

    Scaffold(
        containerColor = PureBlack,

        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddHabit,
                icon = { Icon(Icons.Default.Add,null) },
                text = { Text("New Habit") },
                containerColor = PurpleAccent,
                contentColor = Color.White
            )
        },

        bottomBar = {
            BottomNavigationBar(
                currentRoute = "dashboard",
                onDashboard = {},
                onAnalytics = onAnalytics,
                onProfile = onProfile
            )
        }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack)
                .padding(padding)
        ) {

            item {
                DashboardHeader(
                    greeting = viewModel.greeting,
                    date = viewModel.todayFormatted,
                    onProfile = onProfile
                )
            }

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    ProductivityCard(
                        score = productivityScore,
                        modifier = Modifier.weight(1f)
                    )

                    MoodCard(
                        todayMood = todayMood,
                        onClick = onMoodTracker,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),

                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        "Today's Habits",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "${habitsWithLogs.count { it.isCompletedToday }}/${habitsWithLogs.size}",
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            items(habitsWithLogs, key = { it.habit.id }) { habit ->

                HabitCard(
                    habitWithLog = habit,
                    onToggle = {
                        viewModel.toggleHabit(
                            habit.habit.id,
                            habit.habit.targetValue
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardHeader(
    greeting:String,
    date:String,
    onProfile:() -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),

        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column {

            Text(
                greeting,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                date,
                color = TextMuted
            )
        }

        IconButton(
            onClick = onProfile,
            modifier = Modifier
                .size(42.dp)
                .background(
                    PurpleAccent.copy(alpha = 0.2f),
                    CircleShape
                )
        ) {

            Icon(
                Icons.Default.Person,
                null,
                tint = PurpleAccent
            )
        }
    }
}

@Composable
fun ProductivityCard(
    score:Float,
    modifier:Modifier = Modifier
) {

    val animated by animateFloatAsState(
        score,
        animationSpec = tween(1000),
        label = ""
    )

    Card(
        modifier,
        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = GlassCard
        ),

        border = BorderStroke(
            1.dp,
            GlassBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Productivity",
                color = TextMuted
            )

            Spacer(Modifier.height(12.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {

                Canvas(Modifier.size(80.dp)) {

                    val stroke = 10.dp.toPx()

                    drawArc(
                        color = Color.DarkGray,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(stroke)
                    )

                    drawArc(
                        brush = Brush.linearGradient(
                            listOf(
                                PurpleAccent,
                                BlueAccent
                            )
                        ),
                        startAngle = 135f,
                        sweepAngle = (animated/100f)*270f,
                        useCenter = false,
                        style = Stroke(stroke)
                    )
                }

                Text(
                    "${animated.toInt()}%",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MoodCard(
    todayMood: MoodLog?,
    onClick:()->Unit,
    modifier:Modifier
) {

    Card(
        modifier.clickable { onClick() },

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = GlassCard
        ),

        border = BorderStroke(1.dp,GlassBorder)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Mood",
                color = TextMuted
            )

            Spacer(Modifier.height(12.dp))

            Text(
                todayMood?.moodType?.emoji ?: "🙂",
                fontSize = 36.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                todayMood?.moodType?.label ?: "Tap to log",
                color = TextSecondary
            )
        }
    }
}

@Composable
fun HabitCard(
    habitWithLog: HabitWithLog,
    onToggle: () -> Unit
) {

    val habit = habitWithLog.habit
    val done = habitWithLog.isCompletedToday

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = GlassCard
        ),

        border = BorderStroke(1.dp,GlassBorder)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                habit.iconEmoji,
                fontSize = 24.sp
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {

                Text(
                    habit.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    "🔥 ${habit.streak} day streak",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = onToggle
            ) {

                Icon(
                    if(done) Icons.Default.Check else Icons.Outlined.Circle,
                    null,
                    tint = if(done) GreenDone else TextMuted
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute:String,
    onDashboard:()->Unit,
    onAnalytics:()->Unit,
    onProfile:()->Unit
){

    NavigationBar(
        containerColor = GlassCard
    ){

        NavigationBarItem(
            selected = currentRoute=="dashboard",
            onClick = onDashboard,
            icon = { Icon(Icons.Default.Home,null) },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute=="analytics",
            onClick = onAnalytics,
            icon = { Icon(Icons.Default.BarChart,null) },
            label = { Text("Analytics") }
        )

        NavigationBarItem(
            selected = currentRoute=="profile",
            onClick = onProfile,
            icon = { Icon(Icons.Default.Person,null) },
            label = { Text("Profile") }
        )
    }
}

