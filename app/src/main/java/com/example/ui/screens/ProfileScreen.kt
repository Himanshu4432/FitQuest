package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityLog
import com.example.data.DailyLog
import com.example.data.FITQUEST_ACHIEVEMENTS
import com.example.data.FitQuestScoring
import com.example.data.UserProfile
import com.example.ui.components.AthleteAvatar
import com.example.ui.components.SportyCard
import com.example.ui.components.badgeIconFor
import com.example.ui.theme.CoralEnergy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.OnAccentDark
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun ProfileScreen(
    profile: UserProfile,
    rank: Int,
    dailyLogs: List<DailyLog>,
    activities: List<ActivityLog>,
    onUpdateSettings: (
        username: String,
        avatarId: String,
        stepGoal: Long,
        waterGoalMl: Long,
        runGoalKm: Double,
        sportsGoalMin: Long,
        region: String,
        publicProfile: Boolean,
        notificationsEnabled: Boolean
    ) -> Unit,
    onOpenDailySummary: () -> Unit,
    onSignOut: () -> Unit
) {
    var chartMode by remember { mutableIntStateOf(0) } // 0 = Weekly (7d), 1 = Monthly (30d)
    var showEditGoalsDialog by remember { mutableStateOf(false) }

    val level = FitQuestScoring.levelFromXp(profile.totalXp)
    val levelTitle = FitQuestScoring.titleForLevel(level)
    val logsByDate = remember(dailyLogs) { dailyLogs.associateBy { it.dateKey } }

    val totalWaterLiters = remember(dailyLogs) { dailyLogs.sumOf { it.waterMl } / 1000.0 }
    val totalWorkoutMinutes = remember(activities) { activities.sumOf { (it.durationSeconds / 60L).coerceAtLeast(1L) } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Athlete Identity Card
        item {
            SportyCard(accentBorder = ElectricLime.copy(alpha = 0.55f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AthleteAvatar(avatarId = profile.avatarId, size = 68.dp, ringBorder = true)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("@${profile.username}", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                        Text("Level $level — $levelTitle", style = MaterialTheme.typography.titleSmall, color = ElectricLime)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Rank #$rank", fontFamily = MonoFontFamily, fontSize = 12.sp, color = ElectricCyan)
                            Text("•", color = TextSecondary)
                            Text("${String.format(Locale.US, "%,d", profile.totalXp)} Lifetime XP", fontFamily = MonoFontFamily, fontSize = 12.sp, color = SolarAmber)
                        }
                    }
                    IconButton(
                        onClick = { showEditGoalsDialog = true },
                        modifier = Modifier.testTag("edit_profile_goals_button")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit profile & goals", tint = ElectricLime)
                    }
                }
            }
        }

        // 2. Hydration & Workout Statistics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatTile("Lifetime Steps", String.format(Locale.US, "%,d", profile.totalStepsLifetime), ElectricLime, Modifier.weight(1f))
                ProfileStatTile("Water Logged", String.format(Locale.US, "%.1f L", totalWaterLiters), ElectricCyan, Modifier.weight(1f))
                ProfileStatTile("Active Time", "${totalWorkoutMinutes}m", SolarAmber, Modifier.weight(1f))
            }
        }

        // 3. Streak Calendar
        item {
            SportyCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = SolarAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Streak Calendar", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    Text(
                        text = "${profile.currentStreak}d current • ${profile.bestStreak}d best",
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        color = SolarAmber
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                StreakCalendarGrid(activeDates = profile.activeDates.toSet())
            }
        }

        // 4. Weekly & Monthly Activity Score Chart
        item {
            SportyCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Health Score History", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianCardElevated)
                            .padding(3.dp)
                    ) {
                        listOf("7D", "30D").forEachIndexed { idx, label ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (chartMode == idx) ElectricLime else Color.Transparent)
                                    .clickable { chartMode = idx }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("chart_mode_${label.lowercase()}")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (chartMode == idx) OnAccentDark else TextSecondary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                val daysCount = if (chartMode == 0) 7 else 30
                val series = remember(logsByDate, daysCount) {
                    (daysCount - 1 downTo 0).map { ago ->
                        val key = FitQuestScoring.dateKeyDaysAgo(ago)
                        val score = logsByDate[key]?.healthScore?.toInt() ?: 0
                        key.takeLast(2) to score
                    }
                }
                ScoreHistoryBarChart(series = series)
            }
        }

        // 5. Earned Badges Strip
        item {
            val earned = FITQUEST_ACHIEVEMENTS.filter { it.id in profile.unlockedBadges }
            SportyCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Earned Badges (${earned.size})", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = SolarAmber, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (earned.isEmpty()) {
                    Text("Complete runs, hydrate, or maintain streaks to collect trophies!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        earned.take(6).forEach { badge ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(SolarAmber, ElectricLime))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(badgeIconFor(badge.iconKey), contentDescription = badge.title, tint = OnAccentDark, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
        }

        // 6. Settings, Privacy, Notifications & Daily Summary
        item {
            SportyCard {
                Text("PREFERENCES & PRIVACY", style = MaterialTheme.typography.labelMedium, color = TextSecondary, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Public, contentDescription = "Public Profile", tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Public Leaderboard Profile", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Allow friends & leaderboards to discover you", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = profile.publicProfile,
                        onCheckedChange = { enabled ->
                            onUpdateSettings(
                                profile.username,
                                profile.avatarId,
                                profile.dailyStepGoal,
                                profile.dailyWaterGoalMl,
                                profile.dailyRunGoalKm,
                                profile.dailySportsGoalMin,
                                profile.region,
                                enabled,
                                profile.notificationsEnabled
                            )
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = OnAccentDark, checkedTrackColor = ElectricLime),
                        modifier = Modifier.testTag("privacy_public_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = SolarAmber)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hydration & Streak Reminders", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Friendly nudges to complete daily quests", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = profile.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateSettings(
                                profile.username,
                                profile.avatarId,
                                profile.dailyStepGoal,
                                profile.dailyWaterGoalMl,
                                profile.dailyRunGoalKm,
                                profile.dailySportsGoalMin,
                                profile.region,
                                profile.publicProfile,
                                enabled
                            )
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = OnAccentDark, checkedTrackColor = ElectricCyan),
                        modifier = Modifier.testTag("notifications_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onOpenDailySummary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_daily_summary_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = OnAccentDark)
                ) {
                    Icon(Icons.Filled.Assessment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Today's Daily Summary", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("sign_out_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CoralEnergy)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = CoralEnergy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", color = CoralEnergy, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (showEditGoalsDialog) {
        EditGoalsDialog(
            profile = profile,
            onDismiss = { showEditGoalsDialog = false },
            onSave = { name, steps, water, run, sports ->
                showEditGoalsDialog = false
                onUpdateSettings(
                    name,
                    profile.avatarId,
                    steps,
                    water,
                    run,
                    sports,
                    profile.region,
                    profile.publicProfile,
                    profile.notificationsEnabled
                )
            }
        )
    }
}

@Composable
private fun ProfileStatTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ObsidianCard,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = accent)
            Spacer(modifier = Modifier.height(3.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun StreakCalendarGrid(activeDates: Set<String>) {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
    val monthPrefix = sdf.format(cal.time)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val todayDay = cal.get(Calendar.DAY_OF_MONTH)

    val weeks = (1..daysInMonth).toList().chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                week.forEach { day ->
                    val key = String.format(Locale.US, "%s-%02d", monthPrefix, day)
                    val isActive = key in activeDates
                    val isToday = day == todayDay
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    isActive -> ElectricLime
                                    isToday -> ElectricCyan.copy(alpha = 0.25f)
                                    else -> ObsidianCardElevated
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$day",
                            fontFamily = MonoFontFamily,
                            fontWeight = if (isActive || isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isActive) OnAccentDark else TextSecondary
                        )
                    }
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ScoreHistoryBarChart(series: List<Pair<String, Int>>) {
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            if (series.isEmpty()) return@Canvas
            val barCount = series.size
            val gap = if (barCount > 10) 3.dp.toPx() else 10.dp.toPx()
            val barWidth = ((size.width - gap * (barCount - 1)) / barCount).coerceAtLeast(4f)
            series.forEachIndexed { idx, (_, score) ->
                val x = idx * (barWidth + gap)
                val norm = (score / 1000f).coerceIn(0.05f, 1f)
                val barHeight = size.height * norm
                val y = size.height - barHeight
                drawRoundRect(
                    color = ObsidianCardElevated,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(ElectricLime, ElectricCyan)),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }
        if (series.size <= 7) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                series.forEach { (dayLabel, _) ->
                    Text(
                        text = dayLabel,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditGoalsDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, Long, Long, Double, Long) -> Unit
) {
    var name by remember { mutableStateOf(profile.username) }
    var steps by remember { mutableLongStateOf(profile.dailyStepGoal) }
    var water by remember { mutableLongStateOf(profile.dailyWaterGoalMl) }
    var run by remember { mutableDoubleStateOf(profile.dailyRunGoalKm) }
    var sports by remember { mutableLongStateOf(profile.dailySportsGoalMin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = { Text("Edit Profile & Daily Goals", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 24) name = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Steps Goal: $steps", fontFamily = MonoFontFamily, fontSize = 12.sp, color = ElectricLime)
                Slider(value = steps.toFloat(), onValueChange = { steps = ((it / 500f).roundToLong() * 500L) }, valueRange = 3000f..20000f)
                Text("Water Goal: ${String.format(Locale.US, "%.1fL", water / 1000.0)}", fontFamily = MonoFontFamily, fontSize = 12.sp, color = ElectricCyan)
                Slider(value = water.toFloat(), onValueChange = { water = ((it / 250f).roundToLong() * 250L) }, valueRange = 1000f..4500f)
                Text("Run Goal: ${String.format(Locale.US, "%.1f km", run)}", fontFamily = MonoFontFamily, fontSize = 12.sp, color = SolarAmber)
                Slider(value = run.toFloat(), onValueChange = { run = ((it * 2f).roundToLong() / 2.0) }, valueRange = 1f..15f)
                Text("Sports Goal: $sports min", fontFamily = MonoFontFamily, fontSize = 12.sp, color = CoralEnergy)
                Slider(value = sports.toFloat(), onValueChange = { sports = ((it / 5f).roundToLong() * 5L) }, valueRange = 15f..120f)
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim().ifBlank { profile.username }, steps, water, run, sports) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnAccentDark)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
