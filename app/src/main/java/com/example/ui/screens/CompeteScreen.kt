package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChallengeMetric
import com.example.data.ChallengeType
import com.example.data.FeedPost
import com.example.data.FitQuestScoring
import com.example.data.FitnessChallenge
import com.example.data.FriendLink
import com.example.data.LeaderboardCategory
import com.example.data.LeaderboardScope
import com.example.data.PublicProfile
import com.example.data.ScoreCardSharer
import com.example.data.UserProfile
import com.example.ui.components.AthleteAvatar
import com.example.ui.components.SportyCard
import com.example.ui.theme.CoralEnergy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.MedalBronze
import com.example.ui.theme.MedalGold
import com.example.ui.theme.MedalSilver
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
fun CompeteScreen(
    currentUserId: String,
    profile: UserProfile,
    leaderboard: List<PublicProfile>,
    challenges: List<FitnessChallenge>,
    friends: List<FriendLink>,
    feedPosts: List<FeedPost>,
    onCreateChallenge: (
        title: String,
        description: String,
        type: ChallengeType,
        metric: ChallengeMetric,
        targetValue: Long,
        rewardXp: Long,
        durationDays: Long,
        publicChallenge: Boolean,
        teamName: String
    ) -> Unit,
    onJoinChallenge: (FitnessChallenge) -> Unit,
    onLogChallengeProgress: (FitnessChallenge, Long) -> Unit,
    onAddFriend: (PublicProfile) -> Unit,
    onChallengeFriend: (String, String) -> Unit,
    onPostUpdate: (String) -> Unit,
    onReactPost: (FeedPost, String) -> Unit
) {
    var selectedSegment by remember { mutableIntStateOf(0) } // 0=Leaderboard, 1=Challenges, 2=Friends
    val segments = listOf("Leaderboard", "Challenges", "Friends")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Header + 3-way Segmented Switcher
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Compete", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                Surface(
                    color = ElectricCyan.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = "Reset countdown", tint = ElectricCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Resets in ${FitQuestScoring.weeklyResetCountdown()}",
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                segments.forEachIndexed { idx, label ->
                    val active = idx == selectedSegment
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) ElectricLime else Color.Transparent)
                            .clickable { selectedSegment = idx }
                            .padding(vertical = 10.dp)
                            .testTag("compete_tab_${label.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (active) OnAccentDark else TextSecondary
                        )
                    }
                }
            }
        }

        when (selectedSegment) {
            0 -> LeaderboardSection(
                currentUserId = currentUserId,
                profile = profile,
                leaderboard = leaderboard,
                friendIds = friends.map { it.friendUid }.toSet()
            )
            1 -> ChallengesSection(
                currentUserId = currentUserId,
                profile = profile,
                challenges = challenges,
                onCreateChallenge = onCreateChallenge,
                onJoinChallenge = onJoinChallenge,
                onLogChallengeProgress = onLogChallengeProgress
            )
            else -> FriendsSocialSection(
                currentUserId = currentUserId,
                profile = profile,
                allPublicAthletes = leaderboard,
                friends = friends,
                feedPosts = feedPosts,
                onAddFriend = onAddFriend,
                onChallengeFriend = onChallengeFriend,
                onPostUpdate = onPostUpdate,
                onReactPost = onReactPost
            )
        }
    }
}

@Composable
private fun LeaderboardSection(
    currentUserId: String,
    profile: UserProfile,
    leaderboard: List<PublicProfile>,
    friendIds: Set<String>
) {
    var scope by remember { mutableStateOf(LeaderboardScope.GLOBAL) }
    var category by remember { mutableStateOf(LeaderboardCategory.OVERALL) }

    val filtered = remember(leaderboard, scope, category, friendIds, currentUserId, profile.region) {
        val scoped = when (scope) {
            LeaderboardScope.FRIENDS -> leaderboard.filter { it.userId == currentUserId || it.userId in friendIds }
            LeaderboardScope.LOCAL -> leaderboard.filter { it.userId == currentUserId || it.region.equals(profile.region, ignoreCase = true) }
            LeaderboardScope.GLOBAL -> leaderboard
        }
        when (category) {
            LeaderboardCategory.OVERALL -> scoped.sortedByDescending { it.weeklyXp * 10 + it.todayHealthScore }
            LeaderboardCategory.RUNNING -> scoped.sortedByDescending { it.weeklyRunningKm }
            LeaderboardCategory.STEPS -> scoped.sortedByDescending { it.weeklySteps }
            LeaderboardCategory.SPORTS -> scoped.sortedByDescending { it.weeklySportsMinutes }
        }
    }

    fun metricDisplay(p: PublicProfile): String = when (category) {
        LeaderboardCategory.OVERALL -> "${p.weeklyXp} XP"
        LeaderboardCategory.RUNNING -> String.format(Locale.US, "%.1f km", p.weeklyRunningKm)
        LeaderboardCategory.STEPS -> String.format(Locale.US, "%,d steps", p.weeklySteps)
        LeaderboardCategory.SPORTS -> "${p.weeklySportsMinutes} min"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Scope & Category Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LeaderboardScope.entries.forEach { s ->
                        FilterChip(
                            selected = scope == s,
                            onClick = { scope = s },
                            label = { Text(s.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan,
                                selectedLabelColor = OnAccentDark,
                                containerColor = ObsidianCard,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("lb_scope_${s.name.lowercase()}")
                        )
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeaderboardCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricLime.copy(alpha = 0.22f),
                                selectedLabelColor = ElectricLime,
                                containerColor = ObsidianCard,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("lb_cat_${cat.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Top 3 Podium
        if (filtered.isNotEmpty()) {
            item {
                SportyCard(accentBorder = SolarAmber.copy(alpha = 0.45f)) {
                    Text(
                        text = "WEEKLY PODIUM",
                        style = MaterialTheme.typography.labelMedium,
                        color = SolarAmber,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        PodiumColumn(
                            rank = 2,
                            athlete = filtered.getOrNull(1),
                            metric = filtered.getOrNull(1)?.let { metricDisplay(it) } ?: "—",
                            medalColor = MedalSilver,
                            barHeight = 68.dp
                        )
                        PodiumColumn(
                            rank = 1,
                            athlete = filtered.getOrNull(0),
                            metric = filtered.getOrNull(0)?.let { metricDisplay(it) } ?: "—",
                            medalColor = MedalGold,
                            barHeight = 92.dp
                        )
                        PodiumColumn(
                            rank = 3,
                            athlete = filtered.getOrNull(2),
                            metric = filtered.getOrNull(2)?.let { metricDisplay(it) } ?: "—",
                            medalColor = MedalBronze,
                            barHeight = 52.dp
                        )
                    }
                }
            }

            // Full Ranked List
            itemsIndexed(filtered, key = { _, item -> item.userId }) { index, athlete ->
                val rank = index + 1
                val isMe = athlete.userId == currentUserId
                val trendUp = athlete.todayHealthScore >= 250L || athlete.currentStreak >= 2L

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isMe) ElectricLime.copy(alpha = 0.14f) else ObsidianCard,
                    border = BorderStroke(
                        width = if (isMe) 1.5.dp else 1.dp,
                        color = if (isMe) ElectricLime else ObsidianBorder
                    ),
                    modifier = Modifier.testTag("leaderboard_row_$rank")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(34.dp)
                        ) {
                            Text(
                                text = "#$rank",
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = when (rank) {
                                    1 -> MedalGold
                                    2 -> MedalSilver
                                    3 -> MedalBronze
                                    else -> TextSecondary
                                }
                            )
                            Icon(
                                imageVector = if (trendUp) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = if (trendUp) "Rank up" else "Rank steady",
                                tint = if (trendUp) ElectricLime else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        AthleteAvatar(avatarId = athlete.avatarId, size = 42.dp, ringBorder = isMe)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isMe) "${athlete.username} (You)" else athlete.username,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isMe) ElectricLime else TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Lvl ${athlete.level} • ${athlete.todayHealthScore}/1000 today",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = metricDisplay(athlete),
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElectricCyan
                        )
                    }
                }
            }
        } else {
            item {
                SportyCard {
                    Text("No ranked athletes in this filter yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Invite friends or switch to Global to compare scores!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    rank: Int,
    athlete: PublicProfile?,
    metric: String,
    medalColor: Color,
    barHeight: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        if (athlete != null) {
            AthleteAvatar(avatarId = athlete.avatarId, size = if (rank == 1) 56.dp else 46.dp, ringBorder = rank == 1)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = athlete.username,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = metric,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                color = medalColor
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ObsidianCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Open Spot", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(medalColor.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = medalColor
            )
        }
    }
}

@Composable
private fun ChallengesSection(
    currentUserId: String,
    profile: UserProfile,
    challenges: List<FitnessChallenge>,
    onCreateChallenge: (String, String, ChallengeType, ChallengeMetric, Long, Long, Long, Boolean, String) -> Unit,
    onJoinChallenge: (FitnessChallenge) -> Unit,
    onLogChallengeProgress: (FitnessChallenge, Long) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf<ChallengeType?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filtered = remember(challenges, selectedFilter) {
        if (selectedFilter == null) challenges
        else challenges.filter { ChallengeType.fromRaw(it.challengeType) == selectedFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter chips + Create Challenge CTA
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricLime,
                            selectedLabelColor = OnAccentDark,
                            containerColor = ObsidianCard,
                            labelColor = TextSecondary
                        )
                    )
                    ChallengeType.entries.forEach { t ->
                        FilterChip(
                            selected = selectedFilter == t,
                            onClick = { selectedFilter = t },
                            label = { Text(t.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan,
                                selectedLabelColor = OnAccentDark,
                                containerColor = ObsidianCard,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("challenge_filter_${t.name.lowercase()}")
                        )
                    }
                }

                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("create_challenge_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnAccentDark)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Challenge or Team Quest", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Quick Preset Launchers if no challenges exist yet
        if (filtered.isEmpty()) {
            item {
                SportyCard(accentBorder = ElectricCyan.copy(alpha = 0.5f)) {
                    Text("Kick Off a Quest!", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Launch an instant Daily, Weekly, or Team challenge into the live cloud arena:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                onCreateChallenge(
                                    "Walk 8,000 Steps Today",
                                    "Hit 8,000 daily steps to earn bonus XP and boost your score!",
                                    ChallengeType.DAILY,
                                    ChallengeMetric.STEPS,
                                    8000L,
                                    150L,
                                    1L,
                                    true,
                                    ""
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("preset_daily_challenge_button"),
                            border = BorderStroke(1.dp, ElectricLime)
                        ) {
                            Text("8K Steps", color = ElectricLime, style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = {
                                onCreateChallenge(
                                    "Run 20 km Weekly Club",
                                    "Complete 20 km of running across 5 active days this week.",
                                    ChallengeType.WEEKLY,
                                    ChallengeMetric.RUNNING_KM,
                                    20L,
                                    400L,
                                    7L,
                                    true,
                                    "Neon Pacers"
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("preset_weekly_challenge_button"),
                            border = BorderStroke(1.dp, ElectricCyan)
                        ) {
                            Text("20K Weekly", color = ElectricCyan, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { ch ->
                val isParticipant = currentUserId in ch.participantIds
                val isCompleted = ch.status == "COMPLETED"
                val metric = ChallengeMetric.fromRaw(ch.metricType)
                val type = ChallengeType.fromRaw(ch.challengeType)
                val progressFrac = (ch.combinedProgress.toFloat() / ch.targetValue.coerceAtLeast(1L)).coerceIn(0f, 1f)
                val stepDelta = when (metric) {
                    ChallengeMetric.STEPS -> 2000L
                    ChallengeMetric.WATER_ML -> 500L
                    ChallengeMetric.RUNNING_KM -> 5L
                    ChallengeMetric.ACTIVE_MINUTES -> 20L
                    ChallengeMetric.XP -> 150L
                }

                SportyCard(
                    accentBorder = if (isCompleted) ElectricLime else ObsidianBorder,
                    modifier = Modifier.testTag("challenge_card_${ch.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = VioletQuest.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = if (ch.teamName.isNotBlank()) "${type.label} • ${ch.teamName}" else type.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = ElectricLime.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "+${ch.rewardXp} XP",
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = ElectricLime,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(ch.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    if (ch.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ch.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progressFrac },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = if (isCompleted) ElectricLime else ElectricCyan,
                        trackColor = ObsidianCardElevated
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${ch.combinedProgress} / ${ch.targetValue} ${metric.unit}",
                            fontFamily = MonoFontFamily,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${ch.durationDays}d • ${ch.participantIds.size} joined",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        when {
                            isCompleted -> {
                                Surface(
                                    color = ElectricLime.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Completed", color = ElectricLime, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                            isParticipant -> {
                                Button(
                                    onClick = { onLogChallengeProgress(ch, stepDelta) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnAccentDark)
                                ) {
                                    Text("+$stepDelta ${metric.unit}", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { onJoinChallenge(ch) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = OnAccentDark)
                                ) {
                                    Text("Join Challenge", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = { ScoreCardSharer.shareInviteLink(context, profile.username, ch.title) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Invite Friends", tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Invite", color = TextPrimary)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateChallengeDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, type, metric, target, xp, days, team ->
                showCreateDialog = false
                onCreateChallenge(title, desc, type, metric, target, xp, days, true, team)
            }
        )
    }
}

@Composable
private fun CreateChallengeDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, ChallengeType, ChallengeMetric, Long, Long, Long, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ChallengeType.TEAM) }
    var selectedMetric by remember { mutableStateOf(ChallengeMetric.STEPS) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = { Text("New FitQuest Challenge", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 60) title = it },
                    label = { Text("Challenge Title") },
                    placeholder = { Text("e.g., Weekend 25K Steps Squad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_challenge_title_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 160) description = it },
                    label = { Text("Goal Details") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChallengeType.entries.forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t.label) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChallengeMetric.entries.forEach { m ->
                        FilterChip(
                            selected = selectedMetric == m,
                            onClick = { selectedMetric = m },
                            label = { Text(m.label) }
                        )
                    }
                }
                if (selectedType == ChallengeType.TEAM) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { if (it.length <= 32) teamName = it },
                        label = { Text("Team Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = when (selectedMetric) {
                        ChallengeMetric.STEPS -> if (selectedType == ChallengeType.DAILY) 8000L else 50000L
                        ChallengeMetric.RUNNING_KM -> if (selectedType == ChallengeType.DAILY) 5L else 20L
                        ChallengeMetric.ACTIVE_MINUTES -> if (selectedType == ChallengeType.DAILY) 30L else 180L
                        ChallengeMetric.WATER_ML -> 2500L
                        ChallengeMetric.XP -> 1000L
                    }
                    val days = if (selectedType == ChallengeType.DAILY) 1L else 7L
                    val xp = if (selectedType == ChallengeType.DAILY) 150L else 350L
                    onCreate(
                        title.ifBlank { "${selectedType.label} ${selectedMetric.label} Quest" },
                        description.ifBlank { "Compete together and earn +$xp XP!" },
                        selectedType,
                        selectedMetric,
                        target,
                        xp,
                        days,
                        teamName
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnAccentDark),
                modifier = Modifier.testTag("confirm_create_challenge_button")
            ) {
                Text("Launch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun FriendsSocialSection(
    currentUserId: String,
    profile: UserProfile,
    allPublicAthletes: List<PublicProfile>,
    friends: List<FriendLink>,
    feedPosts: List<FeedPost>,
    onAddFriend: (PublicProfile) -> Unit,
    onChallengeFriend: (String, String) -> Unit,
    onPostUpdate: (String) -> Unit,
    onReactPost: (FeedPost, String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var newPostText by remember { mutableStateOf("") }
    val friendUidSet = remember(friends) { friends.map { it.friendUid }.toSet() }
    val athleteByUid = remember(allPublicAthletes) { allPublicAthletes.associateBy { it.userId } }

    val searchMatches = remember(searchQuery, allPublicAthletes, currentUserId, friendUidSet) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) emptyList()
        else allPublicAthletes.filter {
            it.userId != currentUserId &&
                it.userId !in friendUidSet &&
                it.usernameLower.contains(q)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Invite Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search athletes by username...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = ElectricCyan) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friend_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedButton(
                    onClick = { ScoreCardSharer.shareInviteLink(context, profile.username) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("invite_friends_link_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElectricLime)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invite Friends with Shareable Link", color = ElectricLime, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Search Results
        if (searchMatches.isNotEmpty()) {
            item {
                Text("DISCOVER ATHLETES", style = MaterialTheme.typography.labelMedium, color = ElectricCyan)
            }
            items(searchMatches, key = { it.userId }) { match ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AthleteAvatar(avatarId = match.avatarId, size = 42.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("@${match.username}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text("Lvl ${match.level} • ${match.todayHealthScore} pts today", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Button(
                            onClick = { onAddFriend(match) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnAccentDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_friend_${match.userId}")
                        ) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                }
            }
        }

        // My Friends Roster
        item {
            Text(
                text = "MY SQUAD (${friends.size})",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 1.2.sp
            )
        }

        if (friends.isEmpty()) {
            item {
                SportyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Groups, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Build your fitness squad", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text("Search usernames above or share your invite link to battle friends!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        } else {
            items(friends, key = { it.friendUid }) { friend ->
                val livePub = athleteByUid[friend.friendUid]
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AthleteAvatar(avatarId = livePub?.avatarId ?: friend.friendAvatarId, size = 44.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("@${livePub?.username ?: friend.friendUsername}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                text = if (livePub != null) {
                                    "Lvl ${livePub.level} • ${livePub.todayHealthScore}/1000 • ${livePub.badgeCount} badges"
                                } else "Connected friend",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        OutlinedButton(
                            onClick = { onChallengeFriend(friend.friendUid, livePub?.username ?: friend.friendUsername) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SolarAmber),
                            modifier = Modifier.testTag("challenge_friend_${friend.friendUid}")
                        ) {
                            Icon(Icons.Filled.SportsMartialArts, contentDescription = "Challenge", tint = SolarAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Battle", color = SolarAmber, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Social Activity Feed + Post Composer
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ACTIVITY FEED",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPostText,
                        onValueChange = { if (it.length <= 160) newPostText = it },
                        placeholder = { Text("Share a fitness win with friends...") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("feed_post_input")
                    )
                    Button(
                        onClick = {
                            if (newPostText.trim().length >= 3) {
                                onPostUpdate(newPostText.trim())
                                newPostText = ""
                            }
                        },
                        enabled = newPostText.trim().length >= 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnAccentDark),
                        modifier = Modifier.height(54.dp).testTag("feed_post_submit_button")
                    ) {
                        Text("Post")
                    }
                }
            }
        }

        if (feedPosts.isEmpty()) {
            item {
                SportyCard {
                    Text("No feed highlights yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("Complete a run, unlock a badge, or post an update above!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        } else {
            items(feedPosts.take(30), key = { it.id }) { post ->
                SportyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AthleteAvatar(avatarId = post.authorAvatarId, size = 40.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("@${post.authorUsername}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text(post.category, style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                        }
                        if (post.xpEarned > 0) {
                            Surface(color = ElectricLime.copy(alpha = 0.16f), shape = RoundedCornerShape(999.dp)) {
                                Text(
                                    text = "+${post.xpEarned} XP",
                                    fontFamily = MonoFontFamily,
                                    fontSize = 11.sp,
                                    color = ElectricLime,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(post.message, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReactionPill("🔥", post.fireCount) { onReactPost(post, "fireCount") }
                        ReactionPill("👏", post.clapCount) { onReactPost(post, "clapCount") }
                        ReactionPill("⚡", post.boltCount) { onReactPost(post, "boltCount") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReactionPill(emoji: String, count: Long, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = ObsidianCardElevated,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$count",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextPrimary
            )
        }
    }
}
