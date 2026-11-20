package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AchievementDefinition
import com.example.data.FITQUEST_AVATARS
import com.example.ui.theme.CoralEnergy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.OnAccentDark
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WorkoutCompletionReward
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun avatarIconFor(avatarId: String): ImageVector = when (avatarId) {
    "runner" -> Icons.AutoMirrored.Filled.DirectionsRun
    "flame" -> Icons.Filled.LocalFireDepartment
    "hydro" -> Icons.Filled.WaterDrop
    "titan" -> Icons.Filled.Shield
    "apex" -> Icons.Filled.EmojiEvents
    else -> Icons.Filled.Bolt
}

fun badgeIconFor(iconKey: String): ImageVector = when (iconKey) {
    "run" -> Icons.AutoMirrored.Filled.DirectionsRun
    "flame" -> Icons.Filled.LocalFireDepartment
    "water" -> Icons.Filled.WaterDrop
    "trophy" -> Icons.Filled.EmojiEvents
    "steps" -> Icons.Outlined.DirectionsWalk
    "sports" -> Icons.Filled.SportsSoccer
    "crown" -> Icons.Filled.MilitaryTech
    "group" -> Icons.Filled.Groups
    else -> Icons.Filled.Star
}

@Composable
fun AthleteAvatar(
    avatarId: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    ringBorder: Boolean = false
) {
    val spec = FITQUEST_AVATARS.firstOrNull { it.id == avatarId } ?: FITQUEST_AVATARS.first()
    val brush = Brush.linearGradient(listOf(Color(spec.primaryHex), Color(spec.secondaryHex)))
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush)
            .then(
                if (ringBorder) Modifier.border(2.5.dp, TextPrimary.copy(alpha = 0.85f), CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = avatarIconFor(avatarId),
            contentDescription = "${spec.label} avatar",
            tint = OnAccentDark,
            modifier = Modifier.size(size * 0.56f)
        )
    }
}

@Composable
fun SportyCard(
    modifier: Modifier = Modifier,
    accentBorder: Color = ObsidianBorder,
    containerColor: Color = ObsidianCard,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, accentBorder.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
fun AnimatedHealthScoreRing(
    score: Int,
    maxScore: Int = 1000,
    size: Dp = 216.dp,
    modifier: Modifier = Modifier
) {
    val targetFraction = (score.toFloat() / maxScore.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "health_score_arc"
    )
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "health_score_int"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 18.dp.toPx()
            val inset = strokePx / 2f + 6.dp.toPx()
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            // Outer ambient glow halo
            drawArc(
                color = ElectricLime.copy(alpha = 0.10f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx + 10.dp.toPx(), cap = StrokeCap.Round)
            )

            // Track
            drawArc(
                color = ObsidianCardElevated,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Vibrant Lime -> Electric Cyan Sweep
            if (animatedFraction > 0.005f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(ElectricLime, ElectricCyan, ElectricLime)
                    ),
                    startAngle = 135f,
                    sweepAngle = 270f * animatedFraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "DAILY SCORE",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                letterSpacing = 1.6.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$animatedScore",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                color = TextPrimary
            )
            Text(
                text = "/ $maxScore",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun XpProgressBar(
    progress: Float,
    currentLevelXp: Long,
    levelSpanXp: Long,
    level: Int,
    levelTitle: String,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "xp_bar"
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = "Level XP",
                    tint = ElectricLime,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Level $level — $levelTitle",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
            }
            Text(
                text = "$currentLevelXp / $levelSpanXp XP",
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                color = ElectricLime
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(ObsidianCardElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animProgress.coerceAtLeast(0.03f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.horizontalGradient(listOf(ElectricLime, ElectricCyan)))
            )
        }
    }
}

@Composable
fun WorkoutRewardDialog(
    reward: WorkoutCompletionReward,
    onDismiss: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "reward_burst")
    val pulse by infinite.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "burst_pulse"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = ObsidianCard,
            border = BorderStroke(1.5.dp, ElectricLime.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val c = center
                        for (i in 0 until 12) {
                            val angle = (i * 30.0) * PI / 180.0
                            val r1 = 36.dp.toPx()
                            val r2 = (46.dp.toPx()) * pulse
                            drawLine(
                                color = if (i % 2 == 0) ElectricLime else ElectricCyan,
                                start = Offset(
                                    c.x + (cos(angle) * r1).toFloat(),
                                    c.y + (sin(angle) * r1).toFloat()
                                ),
                                end = Offset(
                                    c.x + (cos(angle) * r2).toFloat(),
                                    c.y + (sin(angle) * r2).toFloat()
                                ),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ElectricLime, ElectricCyan))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "XP Earned",
                            tint = OnAccentDark,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "+${reward.xpEarned} XP Earned!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ElectricLime
                )
                if (reward.personalBest) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = SolarAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, SolarAmber)
                    ) {
                        Text(
                            text = "NEW PERSONAL BEST!",
                            style = MaterialTheme.typography.labelMedium,
                            color = SolarAmber,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${reward.activityType.displayName} Complete",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val mins = (reward.durationSeconds / 60).coerceAtLeast(1)
                val distLabel = if (reward.distanceKm >= 0.05) {
                    " • ${String.format(Locale.US, "%.2f", reward.distanceKm)} km"
                } else ""
                Text(
                    text = "${mins}m$distLabel • ${reward.calories} kcal",
                    fontFamily = MonoFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("dismiss_workout_reward_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricLime,
                        contentColor = OnAccentDark
                    )
                ) {
                    Text("Claim XP", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun BadgeCelebrationDialog(
    badge: AchievementDefinition,
    unlocked: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = ObsidianCard,
            border = BorderStroke(1.5.dp, if (unlocked) SolarAmber else ObsidianBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(
                            if (unlocked) Brush.linearGradient(listOf(SolarAmber, ElectricLime))
                            else Brush.linearGradient(listOf(ObsidianCardElevated, ObsidianBorder))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badgeIconFor(badge.iconKey),
                        contentDescription = badge.title,
                        tint = if (unlocked) OnAccentDark else TextSecondary,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (unlocked) "ACHIEVEMENT UNLOCKED!" else "LOCKED BADGE",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (unlocked) SolarAmber else TextSecondary,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = ElectricLime.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "+${badge.xpBonus} Bonus XP  •  Goal: ${badge.targetLabel}",
                        fontFamily = MonoFontFamily,
                        fontSize = 12.sp,
                        color = ElectricLime,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_badge_dialog_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (unlocked) ElectricLime else ObsidianCardElevated,
                        contentColor = if (unlocked) OnAccentDark else TextPrimary
                    )
                ) {
                    Text(if (unlocked) "Awesome!" else "Got It")
                }
            }
        }
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CoralEnergy.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, CoralEnergy.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = "Error",
                tint = CoralEnergy,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
        }
    }
}
