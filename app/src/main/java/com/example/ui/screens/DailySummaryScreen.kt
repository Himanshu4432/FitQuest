package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyLog
import com.example.data.FitQuestScoring
import com.example.data.ScoreCardSharer
import com.example.data.UserProfile
import com.example.ui.components.AnimatedHealthScoreRing
import com.example.ui.components.SportyCard
import com.example.ui.theme.CoralEnergy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.OnAccentDark
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletQuest
import java.util.Locale

@Composable
fun DailySummaryScreen(
    profile: UserProfile,
    todayLog: DailyLog,
    dailyLogs: List<DailyLog>,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val breakdown = FitQuestScoring.calculateScoreBreakdown(todayLog, profile)
    val yesterdayKey = FitQuestScoring.dateKeyDaysAgo(1)
    val yesterdayScore = dailyLogs.firstOrNull { it.dateKey == yesterdayKey }?.healthScore?.toInt() ?: 0
    val deltaFromYesterday = breakdown.total - yesterdayScore

    val comparisonText = when {
        yesterdayScore == 0 && breakdown.total > 0 -> "Awesome momentum — you've set today's benchmark!"
        deltaFromYesterday > 0 -> "You beat yesterday's score by +$deltaFromYesterday pts!"
        deltaFromYesterday == 0 -> "Right on pace with yesterday — push a little more to beat it!"
        else -> "${-deltaFromYesterday} pts behind yesterday — a quick walk or water boost closes the gap!"
    }

    val goals = listOf(
        Triple("Steps Goal (${profile.dailyStepGoal})", todayLog.steps >= profile.dailyStepGoal, "${todayLog.steps} steps"),
        Triple("Hydration Goal (${String.format(Locale.US, "%.1fL", profile.dailyWaterGoalMl / 1000.0)})", todayLog.waterMl >= profile.dailyWaterGoalMl, String.format(Locale.US, "%.1fL", todayLog.waterMl / 1000.0)),
        Triple("Running / Cycling (${String.format(Locale.US, "%.1f km", profile.dailyRunGoalKm)})", todayLog.runningKm >= profile.dailyRunGoalKm, String.format(Locale.US, "%.1f km", todayLog.runningKm)),
        Triple("Sports & Workouts (${profile.dailySportsGoalMin}m)", todayLog.sportsMinutes >= profile.dailySportsGoalMin, "${todayLog.sportsMinutes} min"),
        Triple("Daily Challenge", todayLog.completedChallengeCount >= 1L, "${todayLog.completedChallengeCount} done")
    )

    val logsByDate = remember(dailyLogs) { dailyLogs.associateBy { it.dateKey } }
    val last7 = remember(logsByDate, breakdown.total) {
        (6 downTo 0).map { ago ->
            val key = FitQuestScoring.dateKeyDaysAgo(ago)
            val score = if (ago == 0) breakdown.total else (logsByDate[key]?.healthScore?.toInt() ?: 0)
            key.takeLast(2) to score
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("summary_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Summary", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    Text(todayLog.dateKey, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Surface(
                    color = ElectricLime.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "+${todayLog.xpEarnedToday} XP",
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ElectricLime,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Final Health Score + Encouraging Comparison
        item {
            SportyCard(accentBorder = ElectricLime.copy(alpha = 0.6f)) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AnimatedHealthScoreRing(score = breakdown.total, maxScore = 1000, size = 200.dp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = ElectricCyan.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(comparisonText, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    }
                }
            }
        }

        // 1,000-Point Activity Breakdown Chart
        item {
            SportyCard {
                Text("1,000-POINT SCORE BREAKDOWN", style = MaterialTheme.typography.labelMedium, color = TextSecondary, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(12.dp))
                ScoreCategoryBar("Walking & Steps", breakdown.stepsPoints, 250, ElectricLime)
                Spacer(modifier = Modifier.height(10.dp))
                ScoreCategoryBar("Running / Cycling", breakdown.runCyclePoints, 250, SolarAmber)
                Spacer(modifier = Modifier.height(10.dp))
                ScoreCategoryBar("Sports & Workouts", breakdown.sportsPoints, 250, CoralEnergy)
                Spacer(modifier = Modifier.height(10.dp))
                ScoreCategoryBar("Water Goal", breakdown.waterPoints, 100, ElectricCyan)
                Spacer(modifier = Modifier.height(10.dp))
                ScoreCategoryBar("Daily Challenges", breakdown.challengePoints, 150, VioletQuest)
            }
        }

        // Completed vs Missed Daily Goals
        item {
            SportyCard {
                val hitCount = goals.count { it.second }
                Text("GOALS CHECKLIST ($hitCount / ${goals.size})", style = MaterialTheme.typography.labelMedium, color = TextSecondary, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(12.dp))
                goals.forEach { (title, completed, progressText) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = if (completed) "Completed" else "In progress",
                            tint = if (completed) ElectricLime else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (completed) TextPrimary else TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = progressText,
                            fontFamily = MonoFontFamily,
                            fontSize = 12.sp,
                            color = if (completed) ElectricLime else TextSecondary
                        )
                    }
                }
            }
        }

        // Comparison With Previous 7 Days
        item {
            SportyCard {
                Text("7-DAY SCORE COMPARISON", style = MaterialTheme.typography.labelMedium, color = TextSecondary, letterSpacing = 1.2.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val gap = 10.dp.toPx()
                    val bw = (size.width - gap * 6) / 7f
                    last7.forEachIndexed { idx, (_, sc) ->
                        val x = idx * (bw + gap)
                        val h = size.height * (sc / 1000f).coerceIn(0.06f, 1f)
                        drawRoundRect(
                            color = ObsidianCardElevated,
                            topLeft = Offset(x, 0f),
                            size = Size(bw, size.height),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        drawRoundRect(
                            brush = if (idx == 6) {
                                Brush.verticalGradient(listOf(ElectricLime, ElectricCyan))
                            } else {
                                Brush.verticalGradient(listOf(ElectricCyan.copy(alpha = 0.65f), ElectricCyan.copy(alpha = 0.25f)))
                            },
                            topLeft = Offset(x, size.height - h),
                            size = Size(bw, h),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    last7.forEachIndexed { idx, (label, _) ->
                        Text(
                            text = if (idx == 6) "Today" else label,
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp,
                            color = if (idx == 6) ElectricLime else TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Share Daily Score Image Card CTA
        item {
            Button(
                onClick = {
                    ScoreCardSharer.shareDailyScoreCard(
                        context = context,
                        profile = profile,
                        todayLog = todayLog,
                        breakdown = breakdown
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("share_daily_score_card_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnAccentDark)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Daily Score as Image Card", style = MaterialTheme.typography.labelLarge, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ScoreCategoryBar(label: String, points: Int, maxPoints: Int, accent: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(
                text = "$points / $maxPoints pts",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = accent
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { (points.toFloat() / maxPoints.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = accent,
            trackColor = ObsidianCardElevated
        )
    }
}
