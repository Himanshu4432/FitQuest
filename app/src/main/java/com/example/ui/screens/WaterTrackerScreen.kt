package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyLog
import com.example.data.FitQuestScoring
import com.example.data.UserProfile
import com.example.data.WaterLog
import com.example.ui.components.SportyCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDim
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
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaterTrackerScreen(
    profile: UserProfile,
    todayLog: DailyLog,
    waterLogs: List<WaterLog>,
    onAddWater: (Int) -> Unit,
    onDeleteLog: (WaterLog) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val todayKey = FitQuestScoring.todayDateKey()
    val todayWaterLogs = waterLogs.filter { it.dateKey == todayKey }
    val goalMl = profile.dailyWaterGoalMl.coerceAtLeast(500L)
    val progress = (todayLog.waterMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f)
    val todayWaterXp = todayWaterLogs.sumOf { it.xpEarned }
    val remainingMl = (goalMl - todayLog.waterMl).coerceAtLeast(0L)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("water_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hydration Quest", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    Text("Stay hydrated to earn up to 100 daily points", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Surface(
                    color = ElectricCyan.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "+$todayWaterXp XP",
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Animated Water Bottle Card
        item {
            SportyCard(accentBorder = ElectricCyan.copy(alpha = 0.55f)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedWaterBottle(
                        fillFraction = progress,
                        currentMl = todayLog.waterMl,
                        goalMl = goalMl
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Goal progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% of daily goal",
                        style = MaterialTheme.typography.titleSmall,
                        color = ElectricCyan
                    )
                    Text(
                        text = if (remainingMl > 0) "${remainingMl}ml left" else "Goal Smashed!",
                        fontFamily = MonoFontFamily,
                        fontSize = 12.sp,
                        color = if (remainingMl > 0) TextSecondary else ElectricLime
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = ElectricCyan,
                    trackColor = ObsidianCardElevated
                )
            }
        }

        // Tap-to-Add Buttons (+150ml, +250ml, +500ml)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WaterAddButton(
                    amountMl = 150,
                    xp = 10,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("water_add_150_button"),
                    onClick = { onAddWater(150) }
                )
                WaterAddButton(
                    amountMl = 250,
                    xp = 15,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("water_add_250_button"),
                    onClick = { onAddWater(250) }
                )
                WaterAddButton(
                    amountMl = 500,
                    xp = 25,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("water_add_500_button"),
                    onClick = { onAddWater(500) }
                )
            }
        }

        // Hydration Streak & Friendly Reminder
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SportyCard(modifier = Modifier.weight(1f), accentBorder = SolarAmber.copy(alpha = 0.45f)) {
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = "Hydration streak", tint = SolarAmber)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${profile.currentStreak} Days", fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("Hydration Streak", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                SportyCard(modifier = Modifier.weight(1f), accentBorder = ElectricCyan.copy(alpha = 0.45f)) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = "Reminder", tint = ElectricCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        if (remainingMl > 0) "Sip 250ml!" else "Hydrated!",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        if (profile.notificationsEnabled) "Smart reminders active" else "Enable in Profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Today's Water History
        item {
            Text(
                text = "TODAY'S LOGS (${todayWaterLogs.size})",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 1.2.sp
            )
        }

        if (todayWaterLogs.isEmpty()) {
            item {
                SportyCard {
                    Text(
                        text = "Tap +150ml, +250ml, or +500ml above to fill your bottle and earn hydration XP!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(todayWaterLogs, key = { it.id }) { log ->
                val timeStr = log.createdAt?.toDate()?.let {
                    SimpleDateFormat("h:mm a", Locale.US).format(it)
                } ?: SimpleDateFormat("h:mm a", Locale.US).format(Date())

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("+${log.amountMl} ml", fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(timeStr, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Text(
                            text = "+${log.xpEarned} XP",
                            fontFamily = MonoFontFamily,
                            fontSize = 12.sp,
                            color = ElectricLime
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { onDeleteLog(log) },
                            modifier = Modifier.testTag("delete_water_log_${log.id}")
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete log", tint = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedWaterBottle(
    fillFraction: Float,
    currentMl: Long,
    goalMl: Long
) {
    val animatedFill by animateFloatAsState(
        targetValue = fillFraction.coerceIn(0.04f, 1f),
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "bottle_fill"
    )
    val infinite = rememberInfiniteTransition(label = "water_wave")
    val wavePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "wave_phase"
    )
    val bubbleRise by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart),
        label = "bubble_rise"
    )

    Box(
        modifier = Modifier
            .width(172.dp)
            .height(238.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val capWidth = w * 0.42f
            val capHeight = 20.dp.toPx()
            val neckHeight = 14.dp.toPx()
            val bodyTop = capHeight + neckHeight
            val bodyRect = Rect(12.dp.toPx(), bodyTop, w - 12.dp.toPx(), h - 6.dp.toPx())

            // Bottle Cap
            drawRoundRect(
                color = ElectricCyan,
                topLeft = Offset((w - capWidth) / 2f, 2.dp.toPx()),
                size = Size(capWidth, capHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Bottle Body Path
            val bottlePath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = bodyRect,
                        topLeft = CornerRadius(34.dp.toPx(), 34.dp.toPx()),
                        topRight = CornerRadius(34.dp.toPx(), 34.dp.toPx()),
                        bottomLeft = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        bottomRight = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                )
            }

            // Bottle Dark Glass Fill
            drawPath(bottlePath, color = ObsidianCardElevated.copy(alpha = 0.7f))

            // Clipped Animated Liquid
            clipPath(bottlePath) {
                val waterHeight = bodyRect.height * animatedFill
                val waterTopY = bodyRect.bottom - waterHeight

                val wavePath = Path().apply {
                    moveTo(bodyRect.left, bodyRect.bottom)
                    lineTo(bodyRect.left, waterTopY)
                    val steps = 36
                    val dx = bodyRect.width / steps
                    for (i in 0..steps) {
                        val x = bodyRect.left + i * dx
                        val amp = 6.dp.toPx()
                        val y = waterTopY + sin(wavePhase + i * 0.35f) * amp
                        lineTo(x, y)
                    }
                    lineTo(bodyRect.right, bodyRect.bottom)
                    close()
                }

                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(ElectricCyan, ElectricCyanDim, Color(0xFF005B8F)),
                        startY = waterTopY,
                        endY = bodyRect.bottom
                    )
                )

                // Rising bubbles inside liquid
                if (animatedFill > 0.12f) {
                    val bubbleSpecs = listOf(0.28f to 0.0f, 0.54f to 0.35f, 0.74f to 0.68f)
                    bubbleSpecs.forEach { (xRatio, offsetPhase) ->
                        val localT = (bubbleRise + offsetPhase) % 1f
                        val bx = bodyRect.left + bodyRect.width * xRatio
                        val by = bodyRect.bottom - waterHeight * localT
                        if (by > waterTopY + 12f) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.38f * (1f - localT)),
                                radius = (3.5f + (1f - localT) * 3f).dp.toPx(),
                                center = Offset(bx, by)
                            )
                        }
                    }
                }
            }

            // Bottle Neon Outline
            drawPath(
                path = bottlePath,
                color = ElectricCyan.copy(alpha = 0.75f),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Text(
                text = String.format(Locale.US, "%.2fL", currentMl / 1000.0),
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = TextPrimary
            )
            Text(
                text = "/ ${String.format(Locale.US, "%.1fL", goalMl / 1000.0)}",
                fontFamily = MonoFontFamily,
                fontSize = 13.sp,
                color = TextPrimary.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun WaterAddButton(
    amountMl: Int,
    xp: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(66.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ObsidianCard,
            contentColor = TextPrimary
        ),
        border = BorderStroke(1.2.dp, ElectricCyan.copy(alpha = 0.7f)),
        contentPadding = PaddingValues(6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "+${amountMl}ml",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ElectricCyan
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(12.dp))
                Text("+$xp XP", style = MaterialTheme.typography.labelSmall, color = ElectricLime)
            }
        }
    }
}
