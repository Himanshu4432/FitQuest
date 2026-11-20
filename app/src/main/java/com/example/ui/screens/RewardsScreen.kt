package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AchievementDefinition
import com.example.data.DailyLog
import com.example.data.FITQUEST_ACHIEVEMENTS
import com.example.data.FitQuestScoring
import com.example.data.UserProfile
import com.example.ui.components.SportyCard
import com.example.ui.components.XpProgressBar
import com.example.ui.components.badgeIconFor
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
import java.util.Locale

@Composable
fun RewardsScreen(
    profile: UserProfile,
    todayLog: DailyLog,
    joinedAnyChallenge: Boolean,
    onInspectBadge: (AchievementDefinition) -> Unit
) {
    val level = FitQuestScoring.levelFromXp(profile.totalXp)
    val levelTitle = FitQuestScoring.titleForLevel(level)
    val levelProgress = FitQuestScoring.levelProgressFraction(profile.totalXp)
    val (xpInLevel, xpSpan) = FitQuestScoring.xpIntoCurrentLevel(profile.totalXp)
    val unlockedSet = profile.unlockedBadges.toSet()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Trophy Room Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_trophy_banner_1795197878117),
                    contentDescription = "FitQuest Trophy Room glowing diamond champion cup",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(ObsidianBg.copy(alpha = 0.88f), ObsidianBg.copy(alpha = 0.25f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = SolarAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, SolarAmber)
                    ) {
                        Text(
                            text = "${unlockedSet.size} / ${FITQUEST_ACHIEVEMENTS.size} BADGES UNLOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            color = SolarAmber,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Column {
                        Text("Trophy Room", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                        Text(
                            text = "Lifetime XP: ${String.format(Locale.US, "%,d", profile.totalXp)} XP",
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElectricLime
                        )
                    }
                }
            }
        }

        // Level & XP Progression Card
        item {
            SportyCard(accentBorder = ElectricLime.copy(alpha = 0.5f)) {
                XpProgressBar(
                    progress = levelProgress,
                    currentLevelXp = xpInLevel,
                    levelSpanXp = xpSpan,
                    level = level,
                    levelTitle = levelTitle
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Next tier unlock: ${FitQuestScoring.titleForLevel(level + 3)} — keep logging workouts, water, and daily challenges!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Personal Milestones & Records
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PERSONAL MILESTONES",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MilestoneCard("Longest Run", String.format(Locale.US, "%.1f km", profile.longestRunKm), ElectricLime, Modifier.weight(1f))
                    MilestoneCard("Best Score", "${profile.bestDailyScore}/1000", ElectricCyan, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MilestoneCard("Best Streak", "${profile.bestStreak} days", SolarAmber, Modifier.weight(1f))
                    MilestoneCard("Workouts", "${profile.totalActivities}", ElectricLime, Modifier.weight(1f))
                }
            }
        }

        // Achievement Badges 2-column Grid
        item {
            Text(
                text = "ACHIEVEMENT BADGES",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 1.2.sp
            )
        }

        val rows = FITQUEST_ACHIEVEMENTS.chunked(2)
        items(rows.size) { rowIndex ->
            val pair = rows[rowIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { badge ->
                    val unlocked = badge.id in unlockedSet
                    val progress = FitQuestScoring.badgeProgress(
                        badgeId = badge.id,
                        profile = profile,
                        todayLog = todayLog,
                        joinedAnyChallenge = joinedAnyChallenge
                    )
                    BadgeTile(
                        badge = badge,
                        unlocked = unlocked,
                        progress = if (unlocked) 1f else progress,
                        onClick = { onInspectBadge(badge) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("badge_card_${badge.id}")
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MilestoneCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = ObsidianCard,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = accent
            )
        }
    }
}

@Composable
private fun BadgeTile(
    badge: AchievementDefinition,
    unlocked: Boolean,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (unlocked) ObsidianCard else ObsidianCard.copy(alpha = 0.65f),
        border = BorderStroke(
            width = if (unlocked) 1.5.dp else 1.dp,
            color = if (unlocked) SolarAmber.copy(alpha = 0.75f) else ObsidianBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(
                        if (unlocked) Brush.linearGradient(listOf(SolarAmber, ElectricLime))
                        else Brush.linearGradient(listOf(ObsidianCardElevated, ObsidianBorder))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (unlocked) badgeIconFor(badge.iconKey) else Icons.Filled.Lock,
                    contentDescription = badge.title,
                    tint = if (unlocked) OnAccentDark else TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = badge.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (unlocked) TextPrimary else TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(34.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = if (unlocked) SolarAmber else ElectricCyan,
                trackColor = ObsidianCardElevated
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (unlocked) "UNLOCKED • +${badge.xpBonus} XP" else "${(progress * 100).toInt()}% • +${badge.xpBonus} XP",
                fontFamily = MonoFontFamily,
                fontSize = 10.sp,
                color = if (unlocked) SolarAmber else ElectricLime
            )
        }
    }
}
