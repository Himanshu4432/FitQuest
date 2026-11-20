package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityLog
import com.example.data.ActivityType
import com.example.ui.components.SportyCard
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
import com.example.viewmodel.LiveTrackingState
import java.util.Locale
import kotlin.math.roundToInt

fun activityIconFor(type: ActivityType): ImageVector = when (type) {
    ActivityType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
    ActivityType.CYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
    ActivityType.FOOTBALL -> Icons.Filled.SportsSoccer
    ActivityType.CRICKET -> Icons.Filled.SportsCricket
    ActivityType.BADMINTON -> Icons.Filled.SportsTennis
    ActivityType.GYM -> Icons.Filled.FitnessCenter
    ActivityType.OTHER -> Icons.Filled.Bolt
}

private fun formatTimer(totalSec: Long): String {
    val hrs = totalSec / 3600
    val mins = (totalSec % 3600) / 60
    val secs = totalSec % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityTrackingScreen(
    liveState: LiveTrackingState,
    activities: List<ActivityLog>,
    onSelectType: (ActivityType) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: (Boolean) -> Unit,
    onQuickLog: (ActivityType, Int, Double, Boolean) -> Unit
) {
    var showQuickLogDialog by remember { mutableStateOf(false) }
    var shareAfterFinish by remember { mutableStateOf(true) }

    val pulseTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse_ring"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Activity Arena", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                    Text("Track live workouts or log sports for XP", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                OutlinedButton(
                    onClick = { showQuickLogDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElectricCyan),
                    modifier = Modifier.testTag("open_quick_log_activity_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quick Log", color = ElectricCyan, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Activity Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "CHOOSE ACTIVITY",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActivityType.entries.forEach { type ->
                        val selected = type == liveState.selectedType
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) ElectricLime else ObsidianCard,
                            border = BorderStroke(1.dp, if (selected) ElectricLime else ObsidianBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !liveState.active) { onSelectType(type) }
                                .testTag("activity_type_${type.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = activityIconFor(type),
                                    contentDescription = type.displayName,
                                    tint = if (selected) OnAccentDark else ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) OnAccentDark else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Tracker Console
        item {
            SportyCard(
                accentBorder = if (liveState.active) ElectricLime else ObsidianBorder
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(180.dp)) {
                        val r = (size.minDimension / 2f) * if (liveState.active && !liveState.paused) pulseScale else 1f
                        drawCircle(
                            color = if (liveState.active) ElectricLime.copy(alpha = 0.14f) else ObsidianCardElevated,
                            radius = r
                        )
                        drawCircle(
                            brush = Brush.sweepGradient(listOf(ElectricLime, ElectricCyan, ElectricLime)),
                            radius = r - 6.dp.toPx(),
                            style = Stroke(width = if (liveState.active) 5.dp.toPx() else 2.5.dp.toPx())
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = activityIconFor(liveState.selectedType),
                            contentDescription = liveState.selectedType.displayName,
                            tint = ElectricLime,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatTimer(liveState.elapsedSeconds),
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            color = TextPrimary,
                            modifier = Modifier.testTag("live_timer_text")
                        )
                        Text(
                            text = when {
                                liveState.active && liveState.paused -> "PAUSED"
                                liveState.active -> "TRACKING LIVE"
                                else -> "READY TO START"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (liveState.active) ElectricCyan else TextSecondary,
                            letterSpacing = 1.4.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Telemetry Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiveStatBox(
                        label = if (liveState.selectedType.tracksDistance) "Distance" else "Steps",
                        value = if (liveState.selectedType.tracksDistance) {
                            String.format(Locale.US, "%.2f km", liveState.distanceKm)
                        } else {
                            "${liveState.estimatedSteps}"
                        },
                        accent = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )
                    LiveStatBox(
                        label = "Calories",
                        value = "${liveState.estimatedCalories} kcal",
                        accent = SolarAmber,
                        modifier = Modifier.weight(1f)
                    )
                    LiveStatBox(
                        label = "Est. XP",
                        value = "+${liveState.estimatedXp}",
                        accent = ElectricLime,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-share completion to Friends Feed", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Switch(
                        checked = shareAfterFinish,
                        onCheckedChange = { shareAfterFinish = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnAccentDark,
                            checkedTrackColor = ElectricLime
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!liveState.active) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("start_activity_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricLime,
                            contentColor = OnAccentDark
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start ${liveState.selectedType.displayName}",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = if (liveState.paused) onResume else onPause,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("pause_resume_activity_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, ElectricCyan)
                        ) {
                            Icon(
                                imageVector = if (liveState.paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                contentDescription = null,
                                tint = ElectricCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (liveState.paused) "Resume" else "Pause", color = ElectricCyan)
                        }
                        Button(
                            onClick = { onFinish(shareAfterFinish) },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("finish_activity_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralEnergy,
                                contentColor = TextPrimary
                            )
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finish", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Recent Activity History
        item {
            Text(
                text = "ACTIVITY HISTORY (${activities.size})",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 1.2.sp
            )
        }

        if (activities.isEmpty()) {
            item {
                SportyCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No activities logged yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            "Tap Start Activity or Quick Log to earn your first workout XP!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(activities.take(25), key = { it.id }) { log ->
                val type = ActivityType.fromRaw(log.activityType)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, if (log.personalBest) SolarAmber.copy(alpha = 0.6f) else ObsidianBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricCyan.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(activityIconFor(type), contentDescription = type.displayName, tint = ElectricCyan)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(type.displayName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                if (log.personalBest) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Filled.Star, contentDescription = "Personal Best", tint = SolarAmber, modifier = Modifier.size(15.dp))
                                }
                            }
                            val mins = (log.durationSeconds / 60).coerceAtLeast(1)
                            val distPart = if (log.distanceKm >= 0.05) " • ${String.format(Locale.US, "%.1f", log.distanceKm)} km" else ""
                            Text(
                                text = "${log.dateKey} • ${mins}m$distPart • ${log.calories} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            color = ElectricLime.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "+${log.xpEarned} XP",
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ElectricLime,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showQuickLogDialog) {
        QuickLogActivityDialog(
            initialType = liveState.selectedType,
            onDismiss = { showQuickLogDialog = false },
            onConfirm = { type, mins, dist, share ->
                showQuickLogDialog = false
                onQuickLog(type, mins, dist, share)
            }
        )
    }
}

@Composable
private fun LiveStatBox(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = ObsidianCardElevated,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = accent
            )
        }
    }
}

@Composable
private fun QuickLogActivityDialog(
    initialType: ActivityType,
    onDismiss: () -> Unit,
    onConfirm: (ActivityType, Int, Double, Boolean) -> Unit
) {
    var durationMin by remember { mutableIntStateOf(30) }
    var distanceKm by remember { mutableDoubleStateOf(if (initialType.tracksDistance) 3.5 else 0.0) }
    var share by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = { Text("Quick Log ${initialType.displayName}", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Duration: $durationMin minutes", fontFamily = MonoFontFamily, color = ElectricLime)
                Slider(
                    value = durationMin.toFloat(),
                    onValueChange = { durationMin = it.roundToInt() },
                    valueRange = 5f..120f,
                    colors = SliderDefaults.colors(thumbColor = ElectricLime, activeTrackColor = ElectricLime)
                )
                if (initialType.tracksDistance) {
                    Text(
                        "Distance: ${String.format(Locale.US, "%.1f", distanceKm)} km",
                        fontFamily = MonoFontFamily,
                        color = ElectricCyan
                    )
                    Slider(
                        value = distanceKm.toFloat(),
                        onValueChange = { distanceKm = (it * 2).roundToInt() / 2.0 },
                        valueRange = 0.5f..25f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Share to Friends Feed", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Switch(checked = share, onCheckedChange = { share = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(initialType, durationMin, distanceKm, share) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnAccentDark),
                modifier = Modifier.testTag("confirm_quick_log_button")
            ) {
                Text("Log & Earn XP")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
