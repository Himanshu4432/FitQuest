package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityType
import com.example.data.DailyLog
import com.example.data.FitQuestScoring
import com.example.data.FitnessChallenge
import com.example.data.UserProfile
import com.example.ui.components.AnimatedHealthScoreRing
import com.example.ui.components.AthleteAvatar
import com.example.ui.components.SportyCard
import com.example.ui.components.XpProgressBar
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
import com.example.ui.theme.VioletQuest
import java.util.Locale

@Composable
fun HomeDashboardScreen(
    profile: UserProfile,
    todayLog: DailyLog,
    friendRank: Int,
    featuredChallenge: FitnessChallenge?,
    onOpenWaterTracker: () -> Unit,
    onQuickStartActivity: (ActivityType) -> Unit,
    onQuickAddSteps: () -> Unit,
    onOpenDailySummary: () -> Unit,
    onOpenCompete: () -> Unit,
    onLogChallengeProgress: (FitnessChallenge) -> Unit
) {
    val breakdown = FitQuestScoring.calculateScoreBreakdown(todayLog, profile)
    val level = FitQuestScoring.levelFromXp(profile.totalXp)
    val levelTitle = FitQuestScoring.titleForLevel(level)
    val levelProgress = FitQuestScoring.levelProgressFraction(profile.totalXp)
    val (xpInLevel, xpSpan) = FitQuestScoring.xpIntoCurrentLevel(profile.totalXp)
    val motivation = FitQuestScoring.motivationalMessage(breakdown.total, profile.currentStreak)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Identity + Streak + Daily Summary Shortcut
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    AthleteAvatar(avatarId = profile.avatarId, size = 48.dp, ringBorder = true)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hey, ${profile.username}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Level $level • $levelTitle",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricLime
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Streak Pill
                    Surface(
                        color = SolarAmber.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, SolarAmber.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = "Daily streak",
                                tint = SolarAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile.currentStreak}d",
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SolarAmber
                            )
                        }
                    }

                    // Daily Summary Icon Button
                    Surface(
                        onClick = onOpenDailySummary,
                        color = ObsidianCard,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, ObsidianBorder),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("open_daily_summary_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Assessment,
                                contentDescription = "Open Daily Summary",
                                tint = ElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Hero Health Score Ring + XP Progress + Friends Rank
        item {
            SportyCard(
                accentBorder = ElectricLime.copy(alpha = 0.55f),
                containerColor = ObsidianCard
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onOpenCompete,
                        color = ElectricCyan.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("today_rank_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.EmojiEvents, contentDescription = "Rank", tint = ElectricCyan, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Today's Rank: #$friendRank",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                        }
                    }

                    Text(
                        text = "+${todayLog.xpEarnedToday} XP Today",
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ElectricLime
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedHealthScoreRing(
                        score = breakdown.total,
                        maxScore = 1000,
                        size = 212.dp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                XpProgressBar(
                    progress = levelProgress,
                    currentLevelXp = xpInLevel,
                    levelSpanXp = xpSpan,
                    level = level,
                    levelTitle = levelTitle
                )
            }
        }

        // 3. Quick Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickLogButton(
                    icon = Icons.Filled.WaterDrop,
                    label = "Log Water",
                    accent = ElectricCyan,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_log_water_button"),
                    onClick = onOpenWaterTracker
                )
                QuickLogButton(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    label = "Start Run",
                    accent = ElectricLime,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_start_run_button"),
                    onClick = { onQuickStartActivity(ActivityType.RUNNING) }
                )
                QuickLogButton(
                    icon = Icons.Filled.SportsSoccer,
                    label = "Log Sport",
                    accent = SolarAmber,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_log_sport_button"),
                    onClick = { onQuickStartActivity(ActivityType.FOOTBALL) }
                )
            }
        }

        // 4. Daily Activity Cards (2x2 Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "TODAY'S QUESTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DailyMetricCard(
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        title = "Steps",
                        valueText = String.format(Locale.US, "%,d", todayLog.steps),
                        goalText = "/ ${String.format(Locale.US, "%,d", profile.dailyStepGoal)}",
                        pointsText = "${breakdown.stepsPoints}/250 pts",
                        progress = (todayLog.steps.toFloat() / profile.dailyStepGoal.coerceAtLeast(1L)).coerceIn(0f, 1f),
                        accent = ElectricLime,
                        actionLabel = "+500",
                        onClick = onQuickAddSteps,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_steps_card")
                    )
                    DailyMetricCard(
                        icon = Icons.Filled.WaterDrop,
                        title = "Water",
                        valueText = String.format(Locale.US, "%.1fL", todayLog.waterMl / 1000.0),
                        goalText = "/ ${String.format(Locale.US, "%.1fL", profile.dailyWaterGoalMl / 1000.0)}",
                        pointsText = "${breakdown.waterPoints}/100 pts",
                        progress = (todayLog.waterMl.toFloat() / profile.dailyWaterGoalMl.coerceAtLeast(1L)).coerceIn(0f, 1f),
                        accent = ElectricCyan,
                        actionLabel = "Track",
                        onClick = onOpenWaterTracker,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_water_card")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DailyMetricCard(
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        title = "Running",
                        valueText = String.format(Locale.US, "%.1f", todayLog.runningKm),
                        goalText = "/ ${String.format(Locale.US, "%.1f km", profile.dailyRunGoalKm)}",
                        pointsText = "${breakdown.runCyclePoints}/250 pts",
                        progress = (todayLog.runningKm / profile.dailyRunGoalKm.coerceAtLeast(0.5)).toFloat().coerceIn(0f, 1f),
                        accent = SolarAmber,
                        actionLabel = "Go",
                        onClick = { onQuickStartActivity(ActivityType.RUNNING) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_running_card")
                    )
                    DailyMetricCard(
                        icon = Icons.Filled.SportsSoccer,
                        title = "Sports",
                        valueText = "${todayLog.sportsMinutes}",
                        goalText = "/ ${profile.dailySportsGoalMin} min",
                        pointsText = "${breakdown.sportsPoints}/250 pts",
                        progress = (todayLog.sportsMinutes.toFloat() / profile.dailySportsGoalMin.coerceAtLeast(1L)).coerceIn(0f, 1f),
                        accent = CoralEnergy,
                        actionLabel = "Play",
                        onClick = { onQuickStartActivity(ActivityType.FOOTBALL) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_sports_card")
                    )
                }
            }
        }

        // 5. Motivational Banner + Daily Summary CTA
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenDailySummary)
                    .testTag("motivational_summary_card"),
                shape = RoundedCornerShape(20.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, VioletQuest.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(VioletQuest.copy(alpha = 0.16f), ElectricCyan.copy(alpha = 0.08f))
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(VioletQuest.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = "Motivation", tint = ElectricLime, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = motivation,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to view your Daily Summary & Share Score Card →",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }

        // 6. Featured Daily Challenge Card
        item {
            SportyCard(accentBorder = ElectricCyan.copy(alpha = 0.45f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY CHALLENGE",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan,
                        letterSpacing = 1.2.sp
                    )
                    Surface(
                        color = ElectricLime.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = "+${featuredChallenge?.rewardXp ?: 150} XP",
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricLime,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (featuredChallenge != null) {
                    Text(
                        text = featuredChallenge.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = featuredChallenge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val frac = (featuredChallenge.combinedProgress.toFloat() / featuredChallenge.targetValue.coerceAtLeast(1L)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { frac },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = ElectricLime,
                        trackColor = ObsidianCardElevated
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${featuredChallenge.combinedProgress} / ${featuredChallenge.targetValue}",
                            fontFamily = MonoFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Button(
                            onClick = { onLogChallengeProgress(featuredChallenge) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricLime,
                                contentColor = OnAccentDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("featured_challenge_log_button")
                        ) {
                            Text("Log Progress", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    Text(
                        text = "Walk 8,000 steps or complete 20 mins of activity",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Head to Compete to join live community quests or challenge a friend!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenCompete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = OnAccentDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("browse_challenges_button")
                    ) {
                        Text("Explore Challenges", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun QuickLogButton(
    icon: ImageVector,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = accent, modifier = Modifier.size(19.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DailyMetricCard(
    icon: ImageVector,
    title: String,
    valueText: String,
    goalText: String,
    pointsText: String,
    progress: Float,
    accent: Color,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = ObsidianCard,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = accent, modifier = Modifier.size(20.dp))
                }
                Surface(
                    color = accent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = accent, modifier = Modifier.size(12.dp))
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = valueText,
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = goalText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = accent,
                trackColor = ObsidianCardElevated
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = pointsText,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                color = accent
            )
        }
    }
}
