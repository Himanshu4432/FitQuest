package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "Athlete",
    val avatarId: String = "bolt",
    val interests: List<String> = listOf("Walking", "Running", "Hydration"),
    val dailyStepGoal: Long = 10000L,
    val dailyWaterGoalMl: Long = 2500L,
    val dailyRunGoalKm: Double = 5.0,
    val dailySportsGoalMin: Long = 60L,
    val totalXp: Long = 0L,
    val level: Long = 1L,
    val currentStreak: Long = 1L,
    val bestStreak: Long = 1L,
    val longestRunKm: Double = 0.0,
    val bestDailyScore: Long = 0L,
    val totalActivities: Long = 0L,
    val totalStepsLifetime: Long = 0L,
    val unlockedBadges: List<String> = emptyList(),
    val activeDates: List<String> = emptyList(),
    val lastActiveDate: String? = null,
    val onboardingCompleted: Boolean = false,
    val publicProfile: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val region: String = "Local",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PublicProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "Athlete",
    val usernameLower: String = "athlete",
    val avatarId: String = "bolt",
    val region: String = "Local",
    val totalXp: Long = 0L,
    val weeklyXp: Long = 0L,
    val weeklyRunningKm: Double = 0.0,
    val weeklySteps: Long = 0L,
    val weeklySportsMinutes: Long = 0L,
    val todayHealthScore: Long = 0L,
    val level: Long = 1L,
    val currentStreak: Long = 0L,
    val badgeCount: Long = 0L,
    val unlockedBadges: List<String> = emptyList(),
    val publicProfile: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DailyLog(
    @DocumentId val id: String = "",
    val dateKey: String = "",
    val steps: Long = 0L,
    val waterMl: Long = 0L,
    val runningKm: Double = 0.0,
    val sportsMinutes: Long = 0L,
    val completedChallengeCount: Long = 0L,
    val healthScore: Long = 0L,
    val xpEarnedToday: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ActivityLog(
    @DocumentId val id: String = "",
    val activityType: String = "RUNNING",
    val durationSeconds: Long = 0L,
    val distanceKm: Double = 0.0,
    val calories: Long = 0L,
    val stepsEstimated: Long = 0L,
    val xpEarned: Long = 0L,
    val personalBest: Boolean = false,
    val dateKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WaterLog(
    @DocumentId val id: String = "",
    val amountMl: Long = 250L,
    val xpEarned: Long = 15L,
    val dateKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FriendLink(
    @DocumentId val id: String = "",
    val friendUid: String = "",
    val friendUsername: String = "",
    val friendAvatarId: String = "bolt",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FitnessChallenge(
    @DocumentId val id: String = "",
    val creatorId: String = "",
    val title: String = "",
    val description: String = "",
    val challengeType: String = "DAILY",
    val metricType: String = "STEPS",
    val targetValue: Long = 8000L,
    val rewardXp: Long = 150L,
    val durationDays: Long = 1L,
    val status: String = "ACTIVE",
    val publicChallenge: Boolean = true,
    val participantIds: List<String> = emptyList(),
    val combinedProgress: Long = 0L,
    val teamName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FeedPost(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorAvatarId: String = "bolt",
    val message: String = "",
    val category: String = "RUN",
    val xpEarned: Long = 0L,
    val publicFeed: Boolean = true,
    val fireCount: Long = 0L,
    val clapCount: Long = 0L,
    val boltCount: Long = 0L,
    val reactedUserIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class ActivityType(
    val displayName: String,
    val tracksDistance: Boolean,
    val metValue: Double,
    val kmPerMinuteEstimate: Double,
    val stepsPerMinuteEstimate: Int,
    val contributesToRunCycle: Boolean,
    val contributesToSports: Boolean
) {
    WALKING("Walking", true, 3.8, 0.085, 105, false, false),
    RUNNING("Running", true, 9.8, 0.165, 165, true, false),
    CYCLING("Cycling", true, 7.5, 0.32, 30, true, false),
    FOOTBALL("Football", true, 8.5, 0.11, 125, false, true),
    CRICKET("Cricket", false, 5.5, 0.04, 65, false, true),
    BADMINTON("Badminton", false, 7.0, 0.05, 95, false, true),
    GYM("Gym", false, 6.2, 0.0, 35, false, true),
    OTHER("Other", false, 5.0, 0.03, 50, false, true);

    companion object {
        fun fromRaw(raw: String): ActivityType =
            entries.firstOrNull { it.name == raw.uppercase() } ?: OTHER
    }
}

enum class ChallengeType(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    FRIEND_BATTLE("Friend Battle"),
    TEAM("Team");

    companion object {
        fun fromRaw(raw: String): ChallengeType =
            entries.firstOrNull { it.name == raw.uppercase() } ?: DAILY
    }
}

enum class ChallengeMetric(val label: String, val unit: String) {
    STEPS("Steps", "steps"),
    RUNNING_KM("Running", "km"),
    ACTIVE_MINUTES("Active Time", "min"),
    WATER_ML("Hydration", "ml"),
    XP("Combined XP", "XP");

    companion object {
        fun fromRaw(raw: String): ChallengeMetric =
            entries.firstOrNull { it.name == raw.uppercase() } ?: STEPS
    }
}

enum class LeaderboardScope(val label: String) {
    FRIENDS("Friends"),
    LOCAL("Local"),
    GLOBAL("Global")
}

enum class LeaderboardCategory(val label: String) {
    OVERALL("Overall Health"),
    RUNNING("Running"),
    STEPS("Steps"),
    SPORTS("Sports")
}

data class AvatarOption(
    val id: String,
    val label: String,
    val primaryHex: Long,
    val secondaryHex: Long
)

val FITQUEST_AVATARS = listOf(
    AvatarOption("bolt", "Volt", 0xFFA3FF12, 0xFF00D4FF),
    AvatarOption("runner", "Strider", 0xFF00D4FF, 0xFF4F7BFF),
    AvatarOption("flame", "Blaze", 0xFFFF6B35, 0xFFFFB800),
    AvatarOption("hydro", "Aqua", 0xFF00E5FF, 0xFF0099FF),
    AvatarOption("titan", "Titan", 0xFF9D5CFF, 0xFFFF4D6D),
    AvatarOption("apex", "Apex", 0xFFFFD147, 0xFFA3FF12)
)

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val xpBonus: Int,
    val iconKey: String,
    val targetLabel: String
)

val FITQUEST_ACHIEVEMENTS = listOf(
    AchievementDefinition("first_5k", "First 5K Run", "Complete a 5 km run in one session", 200, "run", "5.0 km"),
    AchievementDefinition("streak_7", "7-Day Streak", "Stay active for 7 consecutive days", 250, "flame", "7 days"),
    AchievementDefinition("hydration_hero", "Hydration Hero", "Reach 100% of your daily water goal", 120, "water", "100% goal"),
    AchievementDefinition("weekend_warrior", "Weekend Warrior", "Complete 5 total workouts or sports sessions", 180, "trophy", "5 sessions"),
    AchievementDefinition("steps_100k", "100K Steps Club", "Accumulate 100,000 lifetime steps", 350, "steps", "100K steps"),
    AchievementDefinition("sports_allstar", "Sports All-Star", "Log 60+ minutes of sports in a single day", 160, "sports", "60 mins"),
    AchievementDefinition("perfect_1000", "Perfect 1000 Day", "Hit the maximum 1,000 Daily Health Score", 400, "crown", "1000 pts"),
    AchievementDefinition("social_challenger", "Social Challenger", "Join or create a FitQuest competition", 150, "group", "1 challenge")
)

data class ScoreBreakdown(
    val stepsPoints: Int,
    val runCyclePoints: Int,
    val sportsPoints: Int,
    val waterPoints: Int,
    val challengePoints: Int
) {
    val total: Int
        get() = (stepsPoints + runCyclePoints + sportsPoints + waterPoints + challengePoints).coerceIn(0, 1000)
}
