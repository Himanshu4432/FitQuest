package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

object FitQuestScoring {

    const val MAX_STEPS_POINTS = 250
    const val MAX_RUN_CYCLE_POINTS = 250
    const val MAX_SPORTS_POINTS = 250
    const val MAX_WATER_POINTS = 100
    const val MAX_CHALLENGE_POINTS = 150
    const val MAX_DAILY_SCORE = 1000

    fun todayDateKey(nowMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(nowMillis))
    }

    fun dateKeyDaysAgo(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun calculateScoreBreakdown(log: DailyLog, profile: UserProfile): ScoreBreakdown {
        val stepGoal = profile.dailyStepGoal.coerceAtLeast(1000L).toDouble()
        val runGoal = profile.dailyRunGoalKm.coerceAtLeast(0.5)
        val sportsGoal = profile.dailySportsGoalMin.coerceAtLeast(5L).toDouble()
        val waterGoal = profile.dailyWaterGoalMl.coerceAtLeast(500L).toDouble()

        val stepsPts = ((log.steps / stepGoal).coerceIn(0.0, 1.0) * MAX_STEPS_POINTS).roundToInt()
        val runPts = ((log.runningKm / runGoal).coerceIn(0.0, 1.0) * MAX_RUN_CYCLE_POINTS).roundToInt()
        val sportsPts = ((log.sportsMinutes / sportsGoal).coerceIn(0.0, 1.0) * MAX_SPORTS_POINTS).roundToInt()
        val waterPts = ((log.waterMl / waterGoal).coerceIn(0.0, 1.0) * MAX_WATER_POINTS).roundToInt()
        val challengePts = (min(log.completedChallengeCount.toInt(), 2) * 75).coerceAtMost(MAX_CHALLENGE_POINTS)

        return ScoreBreakdown(
            stepsPoints = stepsPts,
            runCyclePoints = runPts,
            sportsPoints = sportsPts,
            waterPoints = waterPts,
            challengePoints = challengePts
        )
    }

    /**
     * Level formula: Level 1 starts at 0 XP. Each level requires 500 + (level - 1) * 150 XP.
     */
    fun xpRequiredForLevel(level: Int): Long {
        var total = 0L
        for (lvl in 1 until level.coerceAtLeast(1)) {
            total += 500L + (lvl - 1) * 150L
        }
        return total
    }

    fun levelFromXp(totalXp: Long): Int {
        var lvl = 1
        while (lvl < 200 && totalXp >= xpRequiredForLevel(lvl + 1)) {
            lvl++
        }
        return lvl
    }

    fun levelProgressFraction(totalXp: Long): Float {
        val currentLevel = levelFromXp(totalXp)
        val floor = xpRequiredForLevel(currentLevel)
        val ceiling = xpRequiredForLevel(currentLevel + 1)
        val span = (ceiling - floor).coerceAtLeast(1L)
        return ((totalXp - floor).toFloat() / span.toFloat()).coerceIn(0f, 1f)
    }

    fun xpIntoCurrentLevel(totalXp: Long): Pair<Long, Long> {
        val currentLevel = levelFromXp(totalXp)
        val floor = xpRequiredForLevel(currentLevel)
        val ceiling = xpRequiredForLevel(currentLevel + 1)
        return (totalXp - floor).coerceAtLeast(0L) to (ceiling - floor).coerceAtLeast(1L)
    }

    fun titleForLevel(level: Int): String = when {
        level >= 25 -> "FitQuest Legend"
        level >= 18 -> "Apex Athlete"
        level >= 12 -> "Fitness Warrior"
        level >= 8 -> "Kinetic Striker"
        level >= 5 -> "Trailblazer"
        level >= 3 -> "Active Strider"
        else -> "Rising Challenger"
    }

    fun calculateActivityXp(
        type: ActivityType,
        durationSeconds: Long,
        distanceKm: Double
    ): Int {
        val minutes = (durationSeconds / 60.0).coerceAtLeast(1.0)
        val baseXp = (minutes * 4.0).roundToInt()
        val distanceBonus = if (type.tracksDistance) (distanceKm * 18.0).roundToInt() else 0
        val sportBonus = if (type.contributesToSports) 20 else 0
        return (baseXp + distanceBonus + sportBonus).coerceIn(20, 600)
    }

    fun calculateWaterXp(amountMl: Int): Int = when {
        amountMl >= 500 -> 25
        amountMl >= 250 -> 15
        else -> 10
    }

    fun evaluateNewlyUnlockedBadges(
        profile: UserProfile,
        todayLog: DailyLog,
        joinedAnyChallenge: Boolean
    ): List<AchievementDefinition> {
        val owned = profile.unlockedBadges.toSet()
        val newlyEarned = mutableListOf<AchievementDefinition>()

        fun check(id: String, condition: Boolean) {
            if (condition && id !in owned) {
                FITQUEST_ACHIEVEMENTS.firstOrNull { it.id == id }?.let { newlyEarned.add(it) }
            }
        }

        check("first_5k", profile.longestRunKm >= 5.0 || todayLog.runningKm >= 5.0)
        check("streak_7", profile.currentStreak >= 7L || profile.bestStreak >= 7L)
        check("hydration_hero", todayLog.waterMl >= profile.dailyWaterGoalMl)
        check("weekend_warrior", profile.totalActivities >= 5L)
        check("steps_100k", profile.totalStepsLifetime >= 100_000L)
        check("sports_allstar", todayLog.sportsMinutes >= 60L)
        check("perfect_1000", todayLog.healthScore >= 1000L || profile.bestDailyScore >= 1000L)
        check("social_challenger", joinedAnyChallenge)

        return newlyEarned
    }

    fun badgeProgress(
        badgeId: String,
        profile: UserProfile,
        todayLog: DailyLog,
        joinedAnyChallenge: Boolean
    ): Float = when (badgeId) {
        "first_5k" -> (maxOf(profile.longestRunKm, todayLog.runningKm) / 5.0).toFloat().coerceIn(0f, 1f)
        "streak_7" -> (maxOf(profile.currentStreak, profile.bestStreak) / 7f).coerceIn(0f, 1f)
        "hydration_hero" -> (todayLog.waterMl.toFloat() / profile.dailyWaterGoalMl.coerceAtLeast(500L)).coerceIn(0f, 1f)
        "weekend_warrior" -> (profile.totalActivities / 5f).coerceIn(0f, 1f)
        "steps_100k" -> (profile.totalStepsLifetime / 100_000f).coerceIn(0f, 1f)
        "sports_allstar" -> (todayLog.sportsMinutes / 60f).coerceIn(0f, 1f)
        "perfect_1000" -> (maxOf(todayLog.healthScore, profile.bestDailyScore) / 1000f).coerceIn(0f, 1f)
        "social_challenger" -> if (joinedAnyChallenge) 1f else 0f
        else -> 0f
    }

    fun motivationalMessage(score: Int, streak: Long): String = when {
        score >= 950 -> "Legendary performance! You're dominating today's quest!"
        score >= 750 -> "On fire! Keep that $streak-day streak blazing to 1,000 pts!"
        score >= 500 -> "Halfway past the summit — a quick run or hydration boost levels you up!"
        score >= 250 -> "Strong momentum! Log a quick sport or glass of water to climb ranks."
        else -> "Fresh day, fresh XP! Take your first step to ignite today's score."
    }

    fun weeklyResetCountdown(): String {
        val now = Calendar.getInstance()
        val nextMonday = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        val diffMillis = (nextMonday.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
        val days = diffMillis / (1000L * 60 * 60 * 24)
        val hours = (diffMillis / (1000L * 60 * 60)) % 24
        return "${days}d ${hours}h"
    }
}
