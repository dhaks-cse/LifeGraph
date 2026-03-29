package com.lifegraph.app.ui.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.lifegraph.app.model.DailyStats
import com.lifegraph.app.model.MoodLog
import com.lifegraph.app.ui.theme.*
import com.lifegraph.app.viewmodel.LifeGraphViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: LifeGraphViewModel,
    onBack: () -> Unit
) {
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val recentMoods by viewModel.recentMoods.collectAsState()

    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f).toArgb()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Analytics", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Weekly Summary chips
            WeeklySummaryRow(weeklyStats)

            // Weekly Productivity Bar Chart
            ChartCard(title = "Weekly Productivity", emoji = "📊") {
                WeeklyProductivityChart(
                    stats = weeklyStats,
                    textColor = textColor,
                    gridColor = gridColor
                )
            }

            // Habit Consistency Line Chart
            ChartCard(title = "Habit Consistency", emoji = "📈") {
                HabitConsistencyChart(
                    stats = weeklyStats,
                    textColor = textColor,
                    gridColor = gridColor
                )
            }

            // Mood vs Productivity
            ChartCard(title = "Mood vs Productivity", emoji = "🧠") {
                MoodVsProductivityChart(
                    stats = weeklyStats,
                    moods = recentMoods,
                    textColor = textColor,
                    gridColor = gridColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WeeklySummaryRow(stats: List<DailyStats>) {
    val avgScore = if (stats.isEmpty()) 0f else stats.map { it.productivityScore }.average().toFloat()
    val totalCompleted = stats.sumOf { it.completedHabits }
    val bestDay = stats.maxByOrNull { it.productivityScore }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryChip(
            emoji = "📊",
            value = "${avgScore.toInt()}%",
            label = "Avg Score",
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            emoji = "✅",
            value = "$totalCompleted",
            label = "Completions",
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            emoji = "🏆",
            value = if (bestDay != null) "${bestDay.productivityScore.toInt()}%" else "–",
            label = "Best Day",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    emoji: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(emoji, fontSize = 20.sp)
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun WeeklyProductivityChart(
    stats: List<DailyStats>,
    textColor: Int,
    gridColor: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val dayLabels = getLast7DayLabels()

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setScaleEnabled(false)
                setPinchZoom(false)
                legend.isEnabled = false
                setExtraOffsets(0f, 8f, 0f, 8f)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    this.setTextColor(textColor)
                    textSize = 10f
                    valueFormatter = IndexAxisValueFormatter(dayLabels)
                }
                axisLeft.apply {
                    setDrawGridLines(true)
                    this.setGridColor(gridColor)
                    axisMinimum = 0f
                    axisMaximum = 100f
                    this.setTextColor(textColor)
                    textSize = 10f
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = dayLabels.mapIndexed { index, _ ->
                val score = stats.getOrNull(index)?.productivityScore ?: 0f
                BarEntry(index.toFloat(), score)
            }
            val dataSet = BarDataSet(entries, "Productivity").apply {
                color = primaryColor
                setDrawValues(false)
            }
            chart.data = BarData(dataSet).apply { barWidth = 0.5f }
            chart.animateY(600)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

@Composable
private fun HabitConsistencyChart(
    stats: List<DailyStats>,
    textColor: Int,
    gridColor: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val dayLabels = getLast7DayLabels()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setScaleEnabled(false)
                setPinchZoom(false)
                legend.apply {
                    isEnabled = true
                    this.setTextColor(textColor)
                }
                setExtraOffsets(0f, 8f, 0f, 8f)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    this.setTextColor(textColor)
                    textSize = 10f
                    valueFormatter = IndexAxisValueFormatter(dayLabels)
                }
                axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = 100f
                    this.setGridColor(gridColor)
                    this.setTextColor(textColor)
                    textSize = 10f
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = dayLabels.mapIndexed { i, _ ->
                val stat = stats.getOrNull(i)
                val rate = if ((stat?.totalHabits ?: 0) > 0)
                    (stat!!.completedHabits.toFloat() / stat.totalHabits.toFloat()) * 100f
                else 0f
                Entry(i.toFloat(), rate)
            }
            val dataSet = LineDataSet(entries, "Consistency %").apply {
                color = primaryColor
                setCircleColor(primaryColor)
                circleRadius = 4f
                lineWidth = 2.5f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = primaryColor
                fillAlpha = 30
            }
            chart.data = LineData(dataSet)
            chart.animateX(600)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

@Composable
private fun MoodVsProductivityChart(
    stats: List<DailyStats>,
    moods: List<MoodLog>,
    textColor: Int,
    gridColor: Int
) {
    val primaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val tertiaryColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val dayLabels = getLast7DayLabels()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setScaleEnabled(false)
                setPinchZoom(false)
                legend.apply {
                    isEnabled = true
                    this.setTextColor(textColor)
                }
                setExtraOffsets(0f, 8f, 0f, 8f)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    this.setTextColor(textColor)
                    textSize = 10f
                    valueFormatter = IndexAxisValueFormatter(dayLabels)
                }
                axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = 100f
                    this.setGridColor(gridColor)
                    this.setTextColor(textColor)
                    textSize = 10f
                }
                axisRight.apply {
                    isEnabled = true
                    axisMinimum = 0f
                    axisMaximum = 3f
                    this.setTextColor(textColor)
                    textSize = 10f
                }
            }
        },
        update = { chart ->
            val productivityEntries = stats.mapIndexed { i, stat ->
                Entry(i.toFloat(), stat.productivityScore)
            }
            val moodMap = moods.associate { it.date to it.moodType.score.toFloat() }
            val moodEntries = dayLabels.mapIndexed { i, _ ->
                val date = LocalDate.now().minusDays((6 - i).toLong()).toString()
                Entry(i.toFloat(), moodMap[date] ?: 0f)
            }

            val productivitySet = LineDataSet(productivityEntries, "Productivity %").apply {
                color = primaryColor
                setCircleColor(primaryColor)
                circleRadius = 4f
                lineWidth = 2.5f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            val moodSet = LineDataSet(moodEntries, "Mood (1-3)").apply {
                color = tertiaryColor
                setCircleColor(tertiaryColor)
                circleRadius = 4f
                lineWidth = 2.5f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                axisDependency = YAxis.AxisDependency.RIGHT
            }
            chart.data = LineData(productivitySet, moodSet)
            chart.animateX(600)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

private fun getLast7DayLabels(): Array<String> {
    val fmt = DateTimeFormatter.ofPattern("EEE")
    return (6 downTo 0).map { i ->
        LocalDate.now().minusDays(i.toLong()).format(fmt)
    }.toTypedArray()
}
