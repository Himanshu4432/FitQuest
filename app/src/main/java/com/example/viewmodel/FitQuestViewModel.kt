package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AchievementDefinition
import com.example.data.ActivityLog
import com.example.data.ActivityType
import com.example.data.ChallengeMetric
import com.example.data.ChallengeType
import com.example.data.DailyLog
import com.example.data.FeedPost
import com.example.data.FitQuestRepository
import com.example.data.FitQuestScoring
import com.example.data.FitnessChallenge
import com.example.data.FriendLink
import com.example.data.PublicProfile
import com.example.data.UserProfile
import com.example.data.WaterLog
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class LiveTrackingState(
    val selectedType: ActivityType = ActivityType.RUNNING,
    val active: Boolean = false,
    val paused: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val distanceKm: Double = 0.0,
    val estimatedCalories: Long = 0L,
    val estimatedSteps: Long = 0L,
    val estimatedXp: Int = 0
)

data class WorkoutCompletionReward(
    val activityType: ActivityType,
    val durationSeconds: Long,
    val distanceKm: Double,
    val calories: Long,
    val xpEarned: Int,
    val personalBest: Boolean
)

class FitQuestViewModel(
    private val repository: FitQuestRepository,
    val currentUserId: String
) : ViewModel() {

    private fun <T> Flow<T>.toUiState(label: String): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing $label", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load $label"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val profileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId).toUiState("profile")

    val dailyLogsState: StateFlow<UiState<List<DailyLog>>> =
        repository.observeDailyLogs(currentUserId).toUiState("daily logs")

    val activitiesState: StateFlow<UiState<List<ActivityLog>>> =
        repository.observeActivities(currentUserId).toUiState("activities")

    val waterLogsState: StateFlow<UiState<List<WaterLog>>> =
        repository.observeWaterLogs(currentUserId).toUiState("water logs")

    val friendsState: StateFlow<UiState<List<FriendLink>>> =
        repository.observeFriends(currentUserId).toUiState("friends")

    val leaderboardState: StateFlow<UiState<List<PublicProfile>>> =
        repository.observeLeaderboard(currentUserId).toUiState("leaderboard")

    val challengesState: StateFlow<UiState<List<FitnessChallenge>>> =
        repository.observeChallenges(currentUserId).toUiState("challenges")

    val feedPostsState: StateFlow<UiState<List<FeedPost>>> =
        repository.observeFeedPosts(currentUserId).toUiState("feed")

    private val _liveTracking = MutableStateFlow(LiveTrackingState())
    val liveTracking: StateFlow<LiveTrackingState> = _liveTracking.asStateFlow()

    private val _workoutReward = MutableStateFlow<WorkoutCompletionReward?>(null)
    val workoutReward: StateFlow<WorkoutCompletionReward?> = _workoutReward.asStateFlow()

    private val _celebrationBadge = MutableStateFlow<AchievementDefinition?>(null)
    val celebrationBadge: StateFlow<AchievementDefinition?> = _celebrationBadge.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private var timerJob: Job? = null

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun dismissWorkoutReward() {
        _workoutReward.value = null
    }

    fun dismissBadgeCelebration() {
        _celebrationBadge.value = null
    }

    fun showBadgePreview(badge: AchievementDefinition) {
        _celebrationBadge.value = badge
    }

    private fun currentProfileOrNull(): UserProfile? =
        (profileState.value as? UiState.Success)?.data

    private fun currentTodayLogOrNull(): DailyLog? {
        val today = FitQuestScoring.todayDateKey()
        return (dailyLogsState.value as? UiState.Success)?.data?.firstOrNull { it.dateKey == today }
    }

    private fun hasJoinedAnyChallenge(): Boolean {
        val list = (challengesState.value as? UiState.Success)?.data ?: emptyList()
        return list.any { currentUserId in it.participantIds }
    }

    // --- ONBOARDING & PROFILE ---

    fun completeOnboarding(
        username: String,
        avatarId: String,
        interests: List<String>,
        dailyStepGoal: Long,
        dailyWaterGoalMl: Long,
        dailyRunGoalKm: Double,
        dailySportsGoalMin: Long,
        notificationsEnabled: Boolean
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveInitialOrUpdatedProfile(
                username = username,
                avatarId = avatarId,
                interests = interests,
                dailyStepGoal = dailyStepGoal,
                dailyWaterGoalMl = dailyWaterGoalMl,
                dailyRunGoalKm = dailyRunGoalKm,
                dailySportsGoalMin = dailySportsGoalMin,
                region = "Local",
                publicProfile = true,
                notificationsEnabled = notificationsEnabled,
                onboardingCompleted = true
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarMessage.value = "Welcome to FitQuest! +100 starter XP unlocked!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to save profile"
            }
        }
    }

    fun updateProfileSettings(
        username: String,
        avatarId: String,
        dailyStepGoal: Long,
        dailyWaterGoalMl: Long,
        dailyRunGoalKm: Double,
        dailySportsGoalMin: Long,
        region: String,
        publicProfile: Boolean,
        notificationsEnabled: Boolean
    ) {
        val existing = currentProfileOrNull() ?: return
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveInitialOrUpdatedProfile(
                username = username,
                avatarId = avatarId,
                interests = existing.interests,
                dailyStepGoal = dailyStepGoal,
                dailyWaterGoalMl = dailyWaterGoalMl,
                dailyRunGoalKm = dailyRunGoalKm,
                dailySportsGoalMin = dailySportsGoalMin,
                region = region,
                publicProfile = publicProfile,
                notificationsEnabled = notificationsEnabled,
                onboardingCompleted = true
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarMessage.value = "Profile & goals saved"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Could not update profile"
            }
        }
    }

    // --- STEPS & WATER TRACKING ---

    fun logSteps(addedSteps: Long, fromSensor: Boolean = false) {
        val profile = currentProfileOrNull() ?: return
        if (addedSteps <= 0L) return
        viewModelScope.launch {
            val xp = if (fromSensor) (addedSteps / 25L).coerceAtLeast(1L) else (addedSteps / 20L).coerceIn(5L, 120L)
            val res = repository.upsertDailyProgressAndStats(
                currentProfile = profile,
                currentDailyLog = currentTodayLogOrNull(),
                addedSteps = addedSteps,
                xpDelta = xp,
                joinedAnyChallenge = hasJoinedAnyChallenge()
            )
            res.onSuccess { newlyUnlocked ->
                if (!fromSensor) {
                    _snackbarMessage.value = "+$addedSteps steps logged! +$xp XP"
                }
                newlyUnlocked.firstOrNull()?.let { _celebrationBadge.value = it }
            }.onFailure { err ->
                if (!fromSensor) _snackbarMessage.value = err.localizedMessage ?: "Failed to log steps"
            }
        }
    }

    fun logWater(amountMl: Int) {
        val profile = currentProfileOrNull() ?: return
        val xp = FitQuestScoring.calculateWaterXp(amountMl)
        viewModelScope.launch {
            val writeLog = repository.recordWaterLog(amountMl, xp)
            if (writeLog.isFailure) {
                _snackbarMessage.value = writeLog.exceptionOrNull()?.localizedMessage ?: "Failed to log water"
                return@launch
            }
            val statsResult = repository.upsertDailyProgressAndStats(
                currentProfile = profile,
                currentDailyLog = currentTodayLogOrNull(),
                addedWaterMl = amountMl.toLong(),
                xpDelta = xp.toLong(),
                joinedAnyChallenge = hasJoinedAnyChallenge()
            )
            statsResult.onSuccess { newlyUnlocked ->
                _snackbarMessage.value = "+${amountMl}ml logged! +$xp XP"
                newlyUnlocked.firstOrNull()?.let { badge ->
                    _celebrationBadge.value = badge
                    if (profile.publicProfile) {
                        repository.publishFeedPost(
                            authorProfile = profile,
                            message = "Unlocked the ${badge.title} achievement!",
                            category = "BADGE",
                            xpEarned = badge.xpBonus.toLong()
                        )
                    }
                }
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to update daily hydration"
            }
        }
    }

    fun deleteWaterEntry(entry: WaterLog) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            val del = repository.deleteWaterLog(entry.id)
            if (del.isSuccess) {
                repository.upsertDailyProgressAndStats(
                    currentProfile = profile,
                    currentDailyLog = currentTodayLogOrNull(),
                    addedWaterMl = -entry.amountMl,
                    xpDelta = 0L,
                    joinedAnyChallenge = hasJoinedAnyChallenge()
                )
                _snackbarMessage.value = "Removed ${entry.amountMl}ml entry"
            } else {
                _snackbarMessage.value = "Could not delete water log"
            }
        }
    }

    // --- LIVE ACTIVITY TRACKING ---

    fun selectActivityType(type: ActivityType) {
        if (_liveTracking.value.active) return
        _liveTracking.update { it.copy(selectedType = type) }
    }

    fun startLiveActivity() {
        val type = _liveTracking.value.selectedType
        timerJob?.cancel()
        _liveTracking.value = LiveTrackingState(
            selectedType = type,
            active = true,
            paused = false,
            elapsedSeconds = 0L
        )
        startTicker()
    }

    fun pauseLiveActivity() {
        if (!_liveTracking.value.active) return
        timerJob?.cancel()
        _liveTracking.update { it.copy(paused = true) }
    }

    fun resumeLiveActivity() {
        if (!_liveTracking.value.active || !_liveTracking.value.paused) return
        _liveTracking.update { it.copy(paused = false) }
        startTicker()
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _liveTracking.update { state ->
                    val nextSec = state.elapsedSeconds + 1L
                    val mins = nextSec / 60.0
                    val dist = if (state.selectedType.tracksDistance) {
                        mins * state.selectedType.kmPerMinuteEstimate
                    } else 0.0
                    val cals = (mins * state.selectedType.metValue * 1.15).roundToLong()
                    val steps = (mins * state.selectedType.stepsPerMinuteEstimate).roundToLong()
                    val xp = FitQuestScoring.calculateActivityXp(state.selectedType, nextSec, dist)
                    state.copy(
                        elapsedSeconds = nextSec,
                        distanceKm = dist,
                        estimatedCalories = cals,
                        estimatedSteps = steps,
                        estimatedXp = xp
                    )
                }
            }
        }
    }

    fun finishLiveActivity(shareToFeed: Boolean = true) {
        timerJob?.cancel()
        val snapshot = _liveTracking.value
        val duration = snapshot.elapsedSeconds.coerceAtLeast(10L)
        _liveTracking.value = LiveTrackingState(selectedType = snapshot.selectedType)
        commitActivitySession(
            type = snapshot.selectedType,
            durationSeconds = duration,
            distanceKm = snapshot.distanceKm,
            calories = snapshot.estimatedCalories.coerceAtLeast(12L),
            stepsEstimated = snapshot.estimatedSteps,
            shareToFeed = shareToFeed
        )
    }

    fun quickLogActivity(
        type: ActivityType,
        durationMinutes: Int,
        distanceKm: Double,
        shareToFeed: Boolean = true
    ) {
        val durationSeconds = (durationMinutes.coerceIn(1, 360) * 60L)
        val safeDist = if (type.tracksDistance) distanceKm.coerceIn(0.0, 200.0) else 0.0
        val calories = (durationMinutes * type.metValue * 1.15).roundToLong().coerceAtLeast(10L)
        val steps = (durationMinutes * type.stepsPerMinuteEstimate).toLong()
        commitActivitySession(
            type = type,
            durationSeconds = durationSeconds,
            distanceKm = safeDist,
            calories = calories,
            stepsEstimated = steps,
            shareToFeed = shareToFeed
        )
    }

    private fun commitActivitySession(
        type: ActivityType,
        durationSeconds: Long,
        distanceKm: Double,
        calories: Long,
        stepsEstimated: Long,
        shareToFeed: Boolean
    ) {
        val profile = currentProfileOrNull() ?: return
        val xp = FitQuestScoring.calculateActivityXp(type, durationSeconds, distanceKm)
        val isRunPb = type == ActivityType.RUNNING && distanceKm > 0.1 && distanceKm > profile.longestRunKm
        val isFirstActivity = profile.totalActivities == 0L
        val personalBest = isRunPb || isFirstActivity
        val durationMinutes = (durationSeconds / 60L).coerceAtLeast(1L)

        viewModelScope.launch {
            val logResult = repository.recordActivityLog(
                type = type,
                durationSeconds = durationSeconds,
                distanceKm = distanceKm,
                calories = calories,
                stepsEstimated = stepsEstimated,
                xpEarned = xp.toLong(),
                personalBest = personalBest
            )
            if (logResult.isFailure) {
                _snackbarMessage.value = logResult.exceptionOrNull()?.localizedMessage ?: "Failed to save activity"
                return@launch
            }

            val statsResult = repository.upsertDailyProgressAndStats(
                currentProfile = profile,
                currentDailyLog = currentTodayLogOrNull(),
                addedSteps = stepsEstimated,
                addedRunningKm = if (type.contributesToRunCycle) distanceKm else 0.0,
                addedSportsMin = if (type.contributesToSports) durationMinutes else 0L,
                xpDelta = xp.toLong(),
                activityIncrement = 1L,
                newLongestRunKm = if (type == ActivityType.RUNNING) distanceKm else null,
                joinedAnyChallenge = hasJoinedAnyChallenge()
            )

            _workoutReward.value = WorkoutCompletionReward(
                activityType = type,
                durationSeconds = durationSeconds,
                distanceKm = distanceKm,
                calories = calories,
                xpEarned = xp,
                personalBest = personalBest
            )

            if (shareToFeed && profile.publicProfile) {
                val distText = if (type.tracksDistance && distanceKm >= 0.1) {
                    " (${String.format(java.util.Locale.US, "%.1f", distanceKm)} km)"
                } else {
                    " (${durationMinutes} min)"
                }
                val cat = if (type == ActivityType.RUNNING || type == ActivityType.CYCLING) "RUN" else "SPORT"
                repository.publishFeedPost(
                    authorProfile = profile,
                    message = "Just completed a ${type.displayName} session$distText!",
                    category = cat,
                    xpEarned = xp.toLong()
                )
            }

            statsResult.onSuccess { newlyUnlocked ->
                newlyUnlocked.firstOrNull()?.let { _celebrationBadge.value = it }
            }
        }
    }

    // --- CHALLENGES & SOCIAL ---

    fun createChallenge(
        title: String,
        description: String,
        type: ChallengeType,
        metric: ChallengeMetric,
        targetValue: Long,
        rewardXp: Long,
        durationDays: Long,
        publicChallenge: Boolean = true,
        teamName: String = "",
        invitedFriendUid: String? = null
    ) {
        val profile = currentProfileOrNull()
        viewModelScope.launch {
            val res = repository.createChallenge(
                title = title,
                description = description,
                type = type,
                metric = metric,
                targetValue = targetValue,
                rewardXp = rewardXp,
                durationDays = durationDays,
                publicChallenge = publicChallenge,
                teamName = teamName,
                invitedFriendUid = invitedFriendUid
            )
            res.onSuccess {
                _snackbarMessage.value = "Challenge \"$title\" is live!"
                if (profile != null) {
                    repository.upsertDailyProgressAndStats(
                        currentProfile = profile,
                        currentDailyLog = currentTodayLogOrNull(),
                        xpDelta = 25L,
                        joinedAnyChallenge = true
                    ).onSuccess { badges ->
                        badges.firstOrNull()?.let { _celebrationBadge.value = it }
                    }
                }
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to create challenge"
            }
        }
    }

    fun joinChallenge(challenge: FitnessChallenge) {
        val profile = currentProfileOrNull()
        viewModelScope.launch {
            val res = repository.joinChallenge(challenge)
            res.onSuccess {
                _snackbarMessage.value = "Joined \"${challenge.title}\"!"
                if (profile != null) {
                    repository.upsertDailyProgressAndStats(
                        currentProfile = profile,
                        currentDailyLog = currentTodayLogOrNull(),
                        xpDelta = 20L,
                        joinedAnyChallenge = true
                    ).onSuccess { badges ->
                        badges.firstOrNull()?.let { _celebrationBadge.value = it }
                    }
                }
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Could not join challenge"
            }
        }
    }

    fun logChallengeProgress(challenge: FitnessChallenge, delta: Long) {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            val res = repository.addChallengeProgress(challenge, delta)
            res.onSuccess { completedNow ->
                if (completedNow) {
                    repository.upsertDailyProgressAndStats(
                        currentProfile = profile,
                        currentDailyLog = currentTodayLogOrNull(),
                        addedCompletedChallenges = 1L,
                        xpDelta = challenge.rewardXp,
                        joinedAnyChallenge = true
                    ).onSuccess { badges ->
                        badges.firstOrNull()?.let { _celebrationBadge.value = it }
                    }
                    if (profile.publicProfile) {
                        repository.publishFeedPost(
                            authorProfile = profile,
                            message = "Conquered the \"${challenge.title}\" challenge!",
                            category = "CHALLENGE",
                            xpEarned = challenge.rewardXp
                        )
                    }
                    _snackbarMessage.value = "Challenge Completed! +${challenge.rewardXp} XP!"
                } else {
                    _snackbarMessage.value = "Progress logged on \"${challenge.title}\"!"
                }
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Could not log progress"
            }
        }
    }

    fun addFriend(athlete: PublicProfile) {
        viewModelScope.launch {
            repository.addFriend(athlete)
                .onSuccess { _snackbarMessage.value = "Added @${athlete.username} as a friend!" }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Could not add friend" }
        }
    }

    fun removeFriend(friendUid: String, username: String) {
        viewModelScope.launch {
            repository.removeFriend(friendUid)
                .onSuccess { _snackbarMessage.value = "Removed @$username" }
                .onFailure { _snackbarMessage.value = "Could not remove friend" }
        }
    }

    fun challengeFriendToStepBattle(friendUid: String, friendUsername: String) {
        createChallenge(
            title = "7-Day Step Battle vs @$friendUsername",
            description = "Head-to-head 7-day step showdown. First squad to 50,000 steps wins!",
            type = ChallengeType.FRIEND_BATTLE,
            metric = ChallengeMetric.STEPS,
            targetValue = 50000L,
            rewardXp = 350L,
            durationDays = 7L,
            publicChallenge = false,
            invitedFriendUid = friendUid
        )
    }

    fun postSocialUpdate(message: String, category: String = "SPORT") {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            repository.publishFeedPost(profile, message, category, 20L)
                .onSuccess { _snackbarMessage.value = "Shared to the FitQuest feed!" }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Could not post update" }
        }
    }

    fun reactToPost(post: FeedPost, reactionField: String) {
        viewModelScope.launch {
            repository.reactToFeedPost(post, reactionField)
                .onFailure { _snackbarMessage.value = "Could not send reaction" }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private companion object {
        const val TAG = "FitQuestVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
