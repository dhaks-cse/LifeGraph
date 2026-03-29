package com.lifegraph.app.ui.addhabit

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifegraph.app.viewmodel.LifeGraphViewModel

private val HABIT_EMOJIS = listOf(
    "💪", "🏃", "📚", "💧", "🧘", "🎯", "✍️", "🍎",
    "😴", "🏋️", "🎸", "🌿", "💊", "🧠", "❤️", "⭐"
)

private val HABIT_COLORS = listOf(
    "#6366F1", "#A855F7", "#EC4899", "#EF4444",
    "#F97316", "#F59E0B", "#10B981", "#06B6D4",
    "#3B82F6", "#8B5CF6", "#14B8A6", "#84CC16"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    viewModel: LifeGraphViewModel,
    onBack: () -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("minutes") }
    var selectedEmoji by remember { mutableStateOf("⭐") }
    var selectedColor by remember { mutableStateOf("#6366F1") }
    var reminderTime by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var targetError by remember { mutableStateOf(false) }

    val units = listOf("minutes", "count", "pages", "glasses", "hours")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Habit",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Habit Name
            SectionCard(title = "Habit Name") {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = {
                        habitName = it
                        nameError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Morning run, Read 20 pages...") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Please enter a habit name") }
                    } else null,
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    leadingIcon = {
                        Text(selectedEmoji, fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                )
            }

            // Choose Icon
            SectionCard(title = "Choose Icon") {
                EmojiGrid(
                    emojis = HABIT_EMOJIS,
                    selectedEmoji = selectedEmoji,
                    onSelect = { selectedEmoji = it }
                )
            }

            // Choose Color
            SectionCard(title = "Accent Color") {
                ColorGrid(
                    colors = HABIT_COLORS,
                    selectedColor = selectedColor,
                    onSelect = { selectedColor = it }
                )
            }

            // Daily Target
            SectionCard(title = "Daily Target") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() }) {
                                targetValue = it
                                targetError = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Amount") },
                        isError = targetError,
                        supportingText = if (targetError) {
                            { Text("Required") }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        UnitSelector(
                            units = units,
                            selectedUnit = selectedUnit,
                            onSelect = { selectedUnit = it }
                        )
                    }
                }
            }

            // Reminder (optional)
            SectionCard(title = "Reminder Time (Optional)") {
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., 08:00") },
                    leadingIcon = {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            // Save button
            Button(
                onClick = {
                    nameError = habitName.isBlank()
                    targetError = targetValue.isBlank()
                    if (!nameError && !targetError) {
                        viewModel.addHabit(
                            name = habitName.trim(),
                            targetValue = targetValue.toIntOrNull() ?: 1,
                            targetUnit = selectedUnit,
                            iconEmoji = selectedEmoji,
                            colorHex = selectedColor,
                            reminderTime = reminderTime.takeIf { it.isNotBlank() }
                        )
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create Habit",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun EmojiGrid(
    emojis: List<String>,
    selectedEmoji: String,
    onSelect: (String) -> Unit
) {
    val rows = emojis.chunked(8)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (emoji == selectedEmoji)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelect(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorGrid(
    colors: List<String>,
    selectedColor: String,
    onSelect: (String) -> Unit
) {
    val rows = colors.chunked(6)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { hex ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color, CircleShape)
                            .then(
                                if (hex == selectedColor)
                                    Modifier.border(3.dp, Color.White, CircleShape)
                                else Modifier
                            )
                            .clickable { onSelect(hex) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitSelector(
    units: List<String>,
    selectedUnit: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.menuAnchor(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onSelect(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
