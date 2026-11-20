package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FitQuestRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun handleStreamError(error: Throwable, uid: String, op: OperationType, path: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }

    // --- REAL-TIME STREAMS ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    handleStreamError(error, userId, OperationType.GET, path)
                    throw error
                }
        )
    }

    fun observeDailyLogs(userId: String): Flow<List<DailyLog>> = flow {
        val path = "users/$userId/daily_logs"
        emitAll(
            db.collection("users").document(userId).collection("daily_logs")
                .snapshots()
                .map { snap ->
                    snap.toObjects(DailyLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.dateKey }
                }
                .catch { error ->
                    handleStreamError(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeActivities(userId: String): Flow<List<ActivityLog>> = flow {
        val path = "users/$userId/activities"
        emitAll(
            db.collection("users").document(userId).collection("activities")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ActivityLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    handleStreamError(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeWaterLogs(userId: String): Flow<List<WaterLog>> = flow {
        val path = "users/$userId/water_logs"
        emitAll(
            db.collection("users").document(userId).collection("water_logs")
                .snapshots()
                .map { snap ->
                    snap.toObjects(WaterLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    handleStreamError(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeFriends(userId: String): Flow<List<FriendLink>> = flow {
        val path = "users/$userId/friends"
        emitAll(
            db.collection("users").document(userId).collection("friends")
                .snapshots()
                .map { snap ->
                    snap.toObjects(FriendLink::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.friendUsername.lowercase() }
                }
                .catch { error ->
                    handleStreamError(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeLeaderboard(userId: String): Flow<List<PublicProfile>> = flow {
        val path = "public_profiles"
        emitAll(
            db.collection("public_profiles")
                .whereEqualTo("publicProfile", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PublicProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    handleStreamError(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeChallenges(userId: String): Flow<List<FitnessChallenge>> {
        val publicFlow = db.collection("challenges")
            .whereEqualTo("publicChallenge", true)
            .snapshots()
            .map { snap ->
                snap.toObjects(FitnessChallenge::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                handleStreamError(error, userId, OperationType.LIST, "challenges")
                throw error
            }

        val joinedFlow = db.collection("challenges")
            .whereArrayContains("participantIds", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(FitnessChallenge::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                handleStreamError(error, userId, OperationType.LIST, "challenges")
                throw error
            }

        return combine(publicFlow, joinedFlow) { pub, joined ->
            (pub + joined)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
    }

    fun observeFeedPosts(userId: String): Flow<List<FeedPost>> = flow {
        val path = "feed_posts"
        emitAll(
            db.collection("feed_posts")
                .whereEqualTo("publicFeed", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(FeedPost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    handleStreamError(error, userId, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // --- ONE-SHOT CRASH-SAFE MUTATIONS ---

    suspend fun saveInitialOrUpdatedProfile(
        username: String,
        avatarId: String,
        interests: List<String>,
        dailyStepGoal: Long,
        dailyWaterGoalMl: Long,
        dailyRunGoalKm: Double,
        dailySportsGoalMin: Long,
        region: String,
        publicProfile: Boolean,
        notificationsEnabled: Boolean,
        onboardingCompleted: Boolean
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val cleanName = username.trim().take(24).let { if (it.length < 3) "${it}Fit".take(24) else it }
            val cleanAvatar = avatarId.ifBlank { "bolt" }.take(30)
            val cleanRegion = region.trim().ifBlank { "Local" }.take(40)
            val safeInterests = interests.take(10)
            val safeStepGoal = dailyStepGoal.coerceIn(1000L, 100000L)
            val safeWaterGoal = dailyWaterGoalMl.coerceIn(500L, 10000L)
            val safeRunGoal = dailyRunGoalKm.coerceIn(0.5, 100.0)
            val safeSportsGoal = dailySportsGoalMin.coerceIn(5L, 360L)
            val today = FitQuestScoring.todayDateKey()

            val userRef = db.collection("users").document(uid)
            path = userRef.path
            val existingUser = userRef.get().await()

            if (!existingUser.exists()) {
                val createMap = mapOf(
                    "userId" to uid,
                    "username" to cleanName,
                    "avatarId" to cleanAvatar,
                    "interests" to safeInterests,
                    "dailyStepGoal" to safeStepGoal,
                    "dailyWaterGoalMl" to safeWaterGoal,
                    "dailyRunGoalKm" to safeRunGoal,
                    "dailySportsGoalMin" to safeSportsGoal,
                    "totalXp" to 100L,
                    "level" to 1L,
                    "currentStreak" to 1L,
                    "bestStreak" to 1L,
                    "longestRunKm" to 0.0,
                    "bestDailyScore" to 0L,
                    "totalActivities" to 0L,
                    "totalStepsLifetime" to 0L,
                    "unlockedBadges" to emptyList<String>(),
                    "activeDates" to listOf(today),
                    "lastActiveDate" to today,
                    "onboardingCompleted" to onboardingCompleted,
                    "publicProfile" to publicProfile,
                    "notificationsEnabled" to notificationsEnabled,
                    "region" to cleanRegion,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(createMap).await()
            } else {
                val updateMap = mapOf(
                    "username" to cleanName,
                    "avatarId" to cleanAvatar,
                    "interests" to safeInterests,
                    "dailyStepGoal" to safeStepGoal,
                    "dailyWaterGoalMl" to safeWaterGoal,
                    "dailyRunGoalKm" to safeRunGoal,
                    "dailySportsGoalMin" to safeSportsGoal,
                    "onboardingCompleted" to onboardingCompleted,
                    "publicProfile" to publicProfile,
                    "notificationsEnabled" to notificationsEnabled,
                    "region" to cleanRegion,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.update(updateMap).await()
            }

            // Mirror non-PII public profile for leaderboards & friend search
            val pubRef = db.collection("public_profiles").document(uid)
            path = pubRef.path
            val existingPub = pubRef.get().await()
            if (!existingPub.exists()) {
                val pubCreate = mapOf(
                    "userId" to uid,
                    "username" to cleanName,
                    "usernameLower" to cleanName.lowercase(),
                    "avatarId" to cleanAvatar,
                    "region" to cleanRegion,
                    "totalXp" to 100L,
                    "weeklyXp" to 100L,
                    "weeklyRunningKm" to 0.0,
                    "weeklySteps" to 0L,
                    "weeklySportsMinutes" to 0L,
                    "todayHealthScore" to 0L,
                    "level" to 1L,
                    "currentStreak" to 1L,
                    "badgeCount" to 0L,
                    "unlockedBadges" to emptyList<String>(),
                    "publicProfile" to publicProfile,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                pubRef.set(pubCreate).await()
            } else {
                pubRef.update(
                    mapOf(
                        "username" to cleanName,
                        "usernameLower" to cleanName.lowercase(),
                        "avatarId" to cleanAvatar,
                        "region" to cleanRegion,
                        "publicProfile" to publicProfile,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertDailyProgressAndStats(
        currentProfile: UserProfile,
        currentDailyLog: DailyLog?,
        addedSteps: Long = 0L,
        addedWaterMl: Long = 0L,
        addedRunningKm: Double = 0.0,
        addedSportsMin: Long = 0L,
        addedCompletedChallenges: Long = 0L,
        xpDelta: Long = 0L,
        activityIncrement: Long = 0L,
        newLongestRunKm: Double? = null,
        joinedAnyChallenge: Boolean = false
    ): Result<List<AchievementDefinition>> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val today = FitQuestScoring.todayDateKey()

            val nextSteps = ((currentDailyLog?.steps ?: 0L) + addedSteps).coerceIn(0L, 200000L)
            val nextWater = ((currentDailyLog?.waterMl ?: 0L) + addedWaterMl).coerceIn(0L, 20000L)
            val nextRun = ((currentDailyLog?.runningKm ?: 0.0) + addedRunningKm).coerceIn(0.0, 300.0)
            val nextSports = ((currentDailyLog?.sportsMinutes ?: 0L) + addedSportsMin).coerceIn(0L, 1440L)
            val nextChals = ((currentDailyLog?.completedChallengeCount ?: 0L) + addedCompletedChallenges).coerceIn(0L, 20L)

            val projectedLog = DailyLog(
                dateKey = today,
                steps = nextSteps,
                waterMl = nextWater,
                runningKm = nextRun,
                sportsMinutes = nextSports,
                completedChallengeCount = nextChals
            )
            val breakdown = FitQuestScoring.calculateScoreBreakdown(projectedLog, currentProfile)
            val newHealthScore = breakdown.total.toLong()

            // Streak calculation
            val activeDatesList = (currentProfile.activeDates + today).distinct().takeLast(366)
            val yesterday = FitQuestScoring.dateKeyDaysAgo(1)
            val nextStreak = when (currentProfile.lastActiveDate) {
                today -> currentProfile.currentStreak.coerceAtLeast(1L)
                yesterday -> currentProfile.currentStreak + 1L
                null -> 1L
                else -> 1L
            }.coerceAtMost(10000L)
            val nextBestStreak = maxOf(currentProfile.bestStreak, nextStreak)
            val nextLongestRun = maxOf(currentProfile.longestRunKm, newLongestRunKm ?: 0.0, nextRun).coerceAtMost(500.0)
            val nextBestScore = maxOf(currentProfile.bestDailyScore, newHealthScore).coerceAtMost(1000L)
            val nextTotalActivities = (currentProfile.totalActivities + activityIncrement).coerceAtMost(100000L)
            val nextLifetimeSteps = (currentProfile.totalStepsLifetime + addedSteps).coerceAtMost(1_000_000_000L)

            val previewProfile = currentProfile.copy(
                currentStreak = nextStreak,
                bestStreak = nextBestStreak,
                longestRunKm = nextLongestRun,
                bestDailyScore = nextBestScore,
                totalActivities = nextTotalActivities,
                totalStepsLifetime = nextLifetimeSteps
            )
            val previewLogWithScore = projectedLog.copy(healthScore = newHealthScore)
            val newlyUnlocked = FitQuestScoring.evaluateNewlyUnlockedBadges(
                profile = previewProfile,
                todayLog = previewLogWithScore,
                joinedAnyChallenge = joinedAnyChallenge
            )
            val badgeBonusXp = newlyUnlocked.sumOf { it.xpBonus.toLong() }
            val totalXpGain = (xpDelta + badgeBonusXp).coerceAtLeast(0L)
            val nextXpToday = ((currentDailyLog?.xpEarnedToday ?: 0L) + totalXpGain).coerceIn(0L, 50000L)
            val nextTotalXp = (currentProfile.totalXp + totalXpGain).coerceIn(0L, 10_000_000L)
            val nextLevel = FitQuestScoring.levelFromXp(nextTotalXp).toLong()
            val nextBadges = (currentProfile.unlockedBadges + newlyUnlocked.map { it.id }).distinct().take(30)

            // 1. Write DailyLog
            val dailyRef = db.collection("users").document(uid).collection("daily_logs").document(today)
            path = dailyRef.path
            val dailySnap = dailyRef.get().await()
            if (!dailySnap.exists()) {
                dailyRef.set(
                    mapOf(
                        "dateKey" to today,
                        "steps" to nextSteps,
                        "waterMl" to nextWater,
                        "runningKm" to nextRun,
                        "sportsMinutes" to nextSports,
                        "completedChallengeCount" to nextChals,
                        "healthScore" to newHealthScore,
                        "xpEarnedToday" to nextXpToday,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                dailyRef.update(
                    mapOf(
                        "steps" to nextSteps,
                        "waterMl" to nextWater,
                        "runningKm" to nextRun,
                        "sportsMinutes" to nextSports,
                        "completedChallengeCount" to nextChals,
                        "healthScore" to newHealthScore,
                        "xpEarnedToday" to nextXpToday,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // 2. Update private UserProfile
            val userRef = db.collection("users").document(uid)
            path = userRef.path
            userRef.update(
                mapOf(
                    "totalXp" to nextTotalXp,
                    "level" to nextLevel,
                    "currentStreak" to nextStreak,
                    "bestStreak" to nextBestStreak,
                    "longestRunKm" to nextLongestRun,
                    "bestDailyScore" to nextBestScore,
                    "totalActivities" to nextTotalActivities,
                    "totalStepsLifetime" to nextLifetimeSteps,
                    "unlockedBadges" to nextBadges,
                    "activeDates" to activeDatesList,
                    "lastActiveDate" to today,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // 3. Sync PublicProfile for leaderboards
            val pubRef = db.collection("public_profiles").document(uid)
            path = pubRef.path
            val pubSnap = pubRef.get().await()
            if (pubSnap.exists()) {
                val curWeeklyXp = pubSnap.getLong("weeklyXp") ?: 0L
                val curWeeklyRun = pubSnap.getDouble("weeklyRunningKm") ?: 0.0
                val curWeeklySteps = pubSnap.getLong("weeklySteps") ?: 0L
                val curWeeklySports = pubSnap.getLong("weeklySportsMinutes") ?: 0L

                pubRef.update(
                    mapOf(
                        "totalXp" to nextTotalXp,
                        "weeklyXp" to (curWeeklyXp + totalXpGain).coerceIn(0L, 1_000_000L),
                        "weeklyRunningKm" to (curWeeklyRun + addedRunningKm).coerceIn(0.0, 2000.0),
                        "weeklySteps" to (curWeeklySteps + addedSteps).coerceIn(0L, 2_000_000L),
                        "weeklySportsMinutes" to (curWeeklySports + addedSportsMin).coerceIn(0L, 10080L),
                        "todayHealthScore" to newHealthScore,
                        "level" to nextLevel,
                        "currentStreak" to nextStreak,
                        "badgeCount" to nextBadges.size.toLong(),
                        "unlockedBadges" to nextBadges,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            Result.success(newlyUnlocked)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun recordWaterLog(amountMl: Int, xpEarned: Int): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("water_logs").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "amountMl" to amountMl.coerceIn(50, 5000).toLong(),
                    "xpEarned" to xpEarned.coerceIn(0, 500).toLong(),
                    "dateKey" to FitQuestScoring.todayDateKey(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteWaterLog(logId: String): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(logId.isNotBlank())
            val docRef = db.collection("users").document(uid).collection("water_logs").document(logId)
            path = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun recordActivityLog(
        type: ActivityType,
        durationSeconds: Long,
        distanceKm: Double,
        calories: Long,
        stepsEstimated: Long,
        xpEarned: Long,
        personalBest: Boolean
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("activities").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "activityType" to type.name,
                    "durationSeconds" to durationSeconds.coerceIn(1L, 86400L),
                    "distanceKm" to distanceKm.coerceIn(0.0, 500.0),
                    "calories" to calories.coerceIn(0L, 20000L),
                    "stepsEstimated" to stepsEstimated.coerceIn(0L, 200000L),
                    "xpEarned" to xpEarned.coerceIn(0L, 10000L),
                    "personalBest" to personalBest,
                    "dateKey" to FitQuestScoring.todayDateKey(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun createChallenge(
        title: String,
        description: String,
        type: ChallengeType,
        metric: ChallengeMetric,
        targetValue: Long,
        rewardXp: Long,
        durationDays: Long,
        publicChallenge: Boolean,
        teamName: String = "",
        invitedFriendUid: String? = null
    ): Result<Unit> {
        var path = "challenges"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim().take(80).let { if (it.length < 3) "FitQuest Challenge" else it }
            val cleanDesc = description.trim().take(200)
            val cleanTeam = teamName.trim().take(40)
            val participants = buildList {
                add(uid)
                if (!invitedFriendUid.isNullOrBlank() && invitedFriendUid != uid) {
                    add(invitedFriendUid)
                }
            }
            val docRef = db.collection("challenges").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "creatorId" to uid,
                    "title" to cleanTitle,
                    "description" to cleanDesc,
                    "challengeType" to type.name,
                    "metricType" to metric.name,
                    "targetValue" to targetValue.coerceIn(1L, 1_000_000L),
                    "rewardXp" to rewardXp.coerceIn(10L, 10_000L),
                    "durationDays" to durationDays.coerceIn(1L, 30L),
                    "status" to "ACTIVE",
                    "publicChallenge" to publicChallenge,
                    "participantIds" to participants,
                    "combinedProgress" to 0L,
                    "teamName" to cleanTeam,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun joinChallenge(challenge: FitnessChallenge): Result<Unit> {
        val path = "challenges/${challenge.id}"
        return try {
            val uid = requireUserId()
            if (uid in challenge.participantIds) return Result.success(Unit)
            val nextParticipants = (challenge.participantIds + uid).distinct().take(50)
            db.collection("challenges").document(challenge.id)
                .update(
                    mapOf(
                        "participantIds" to nextParticipants,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addChallengeProgress(challenge: FitnessChallenge, delta: Long): Result<Boolean> {
        val path = "challenges/${challenge.id}"
        return try {
            requireUserId()
            if (challenge.status != "ACTIVE") return Result.success(false)
            val nextProgress = (challenge.combinedProgress + delta.coerceAtLeast(1L))
                .coerceAtMost(10_000_000L)
            val reachedGoal = nextProgress >= challenge.targetValue
            val nextStatus = if (reachedGoal) "COMPLETED" else "ACTIVE"
            db.collection("challenges").document(challenge.id)
                .update(
                    mapOf(
                        "combinedProgress" to nextProgress,
                        "status" to nextStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(reachedGoal)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addFriend(profile: PublicProfile): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            if (profile.userId == uid || profile.userId.isBlank()) return Result.success(Unit)
            val docRef = db.collection("users").document(uid)
                .collection("friends").document(profile.userId)
            path = docRef.path
            docRef.set(
                mapOf(
                    "friendUid" to profile.userId,
                    "friendUsername" to profile.username.take(24).let { if (it.length < 3) "Athlete" else it },
                    "friendAvatarId" to profile.avatarId.ifBlank { "bolt" }.take(30),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun removeFriend(friendUid: String): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("friends").document(friendUid)
            path = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun publishFeedPost(
        authorProfile: UserProfile,
        message: String,
        category: String,
        xpEarned: Long
    ): Result<Unit> {
        var path = "feed_posts"
        return try {
            val uid = requireUserId()
            val cleanMsg = message.trim().take(200).let { if (it.length < 3) "Completed a FitQuest session!" else it }
            val safeCategory = if (category in listOf("RUN", "SPORT", "WATER", "BADGE", "CHALLENGE", "STREAK")) {
                category
            } else "SPORT"
            val docRef = db.collection("feed_posts").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "authorId" to uid,
                    "authorUsername" to authorProfile.username.take(24).let { if (it.length < 3) "Athlete" else it },
                    "authorAvatarId" to authorProfile.avatarId.ifBlank { "bolt" }.take(30),
                    "message" to cleanMsg,
                    "category" to safeCategory,
                    "xpEarned" to xpEarned.coerceIn(0L, 10000L),
                    "publicFeed" to true,
                    "fireCount" to 0L,
                    "clapCount" to 0L,
                    "boltCount" to 0L,
                    "reactedUserIds" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun reactToFeedPost(post: FeedPost, reactionField: String): Result<Unit> {
        val path = "feed_posts/${post.id}"
        return try {
            val uid = requireUserId()
            val nextReactors = (post.reactedUserIds + uid).distinct().take(100)
            val updates = mutableMapOf<String, Any>(
                "reactedUserIds" to nextReactors,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            when (reactionField) {
                "fireCount" -> updates["fireCount"] = (post.fireCount + 1L).coerceAtMost(100000L)
                "clapCount" -> updates["clapCount"] = (post.clapCount + 1L).coerceAtMost(100000L)
                "boltCount" -> updates["boltCount"] = (post.boltCount + 1L).coerceAtMost(100000L)
            }
            db.collection("feed_posts").document(post.id).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
