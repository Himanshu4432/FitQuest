package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.DailyLog
import com.example.data.FitQuestRepository
import com.example.data.FitQuestScoring
import com.example.data.StepSensorManager
import com.example.data.signOutFitQuest
import com.example.ui.components.BadgeCelebrationDialog
import com.example.ui.components.ErrorBanner
import com.example.ui.components.WorkoutRewardDialog
import com.example.ui.screens.ActivityTrackingScreen
import com.example.ui.screens.CompeteScreen
import com.example.ui.screens.DailySummaryScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.OnboardingWizardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.WaterTrackerScreen
import com.example.ui.screens.WelcomeAuthScreen
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.FitQuestTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.OnAccentDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.FitQuestViewModel
import com.example.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            FitQuestTheme {
                FitQuestRoot()
            }
        }
    }
}

private enum class FitQuestTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    ACTIVITY("Activity", Icons.AutoMirrored.Filled.DirectionsRun, Icons.AutoMirrored.Outlined.DirectionsRun),
    COMPETE("Compete", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    REWARDS("Rewards", Icons.Filled.MilitaryTech, Icons.Outlined.MilitaryTech),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

private enum class OverlayScreen {
    NONE, WATER_TRACKER, DAILY_SUMMARY
}

@Composable
fun FitQuestRoot() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    if (user == null) {
        WelcomeAuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        val uid = user.uid
        val suggestedName = (user.displayName ?: user.email?.substringBefore("@") ?: "FitAthlete")
            .replace(" ", "")
            .take(20)
        key(uid) {
            FitQuestAuthenticatedShell(
                currentUserId = uid,
                suggestedUsername = suggestedName
            )
        }
    }
}

@Composable
private fun FitQuestAuthenticatedShell(
    currentUserId: String,
    suggestedUsername: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: FitQuestViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                FitQuestViewModel(FitQuestRepository(db), currentUserId)
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val dailyLogsState by viewModel.dailyLogsState.collectAsStateWithLifecycle()
    val activitiesState by viewModel.activitiesState.collectAsStateWithLifecycle()
    val waterLogsState by viewModel.waterLogsState.collectAsStateWithLifecycle()
    val friendsState by viewModel.friendsState.collectAsStateWithLifecycle()
    val leaderboardState by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val challengesState by viewModel.challengesState.collectAsStateWithLifecycle()
    val feedPostsState by viewModel.feedPostsState.collectAsStateWithLifecycle()
    val liveTracking by viewModel.liveTracking.collectAsStateWithLifecycle()
    val workoutReward by viewModel.workoutReward.collectAsStateWithLifecycle()
    val celebrationBadge by viewModel.celebrationBadge.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Real hardware step counter sync when permission is granted
    val stepSensorManager = remember(context) { StepSensorManager(context) }
    val hasActivityPermission = remember(context) {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }
    LaunchedEffect(hasActivityPermission, currentUserId) {
        if (hasActivityPermission && stepSensorManager.isHardwareSensorAvailable) {
            stepSensorManager.observeStepDeltas().collect { delta ->
                viewModel.logSteps(delta, fromSensor = true)
            }
        }
    }

    when (val pState = profileState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricLime)
            }
            return
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                ErrorBanner(message = pState.message)
            }
            return
        }

        is UiState.Success -> {
            val profile = pState.data
            if (profile == null || !profile.onboardingCompleted) {
                OnboardingWizardScreen(
                    suggestedUsername = suggestedUsername,
                    isSaving = isSaving,
                    onComplete = { username, avatarId, interests, steps, water, run, sports, notifs ->
                        viewModel.completeOnboarding(
                            username = username,
                            avatarId = avatarId,
                            interests = interests,
                            dailyStepGoal = steps,
                            dailyWaterGoalMl = water,
                            dailyRunGoalKm = run,
                            dailySportsGoalMin = sports,
                            notificationsEnabled = notifs
                        )
                    }
                )
                return
            }

            var activeTab by remember { mutableStateOf(FitQuestTab.HOME) }
            var overlay by remember { mutableStateOf(OverlayScreen.NONE) }

            val dailyLogs = (dailyLogsState as? UiState.Success)?.data ?: emptyList()
            val todayKey = FitQuestScoring.todayDateKey()
            val todayLog = dailyLogs.firstOrNull { it.dateKey == todayKey } ?: DailyLog(dateKey = todayKey)
            val activities = (activitiesState as? UiState.Success)?.data ?: emptyList()
            val waterLogs = (waterLogsState as? UiState.Success)?.data ?: emptyList()
            val friends = (friendsState as? UiState.Success)?.data ?: emptyList()
            val leaderboard = (leaderboardState as? UiState.Success)?.data ?: emptyList()
            val challenges = (challengesState as? UiState.Success)?.data ?: emptyList()
            val feedPosts = (feedPostsState as? UiState.Success)?.data ?: emptyList()

            val sortedGlobal = remember(leaderboard) {
                leaderboard.sortedByDescending { it.weeklyXp * 10 + it.todayHealthScore }
            }
            val userRank = (sortedGlobal.indexOfFirst { it.userId == currentUserId } + 1).let { if (it <= 0) 1 else it }
            val featuredChallenge = challenges.firstOrNull { it.status == "ACTIVE" && currentUserId in it.participantIds }
                ?: challenges.firstOrNull { it.status == "ACTIVE" }
            val joinedAnyChallenge = challenges.any { currentUserId in it.participantIds }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg)
            ) {
                val useNavRail = maxWidth >= 680.dp

                Scaffold(
                    containerColor = ObsidianBg,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = ObsidianCard,
                                contentColor = TextPrimary,
                                actionColor = ElectricLime
                            )
                        }
                    },
                    bottomBar = {
                        if (!useNavRail && overlay == OverlayScreen.NONE) {
                            NavigationBar(
                                containerColor = ObsidianSurface,
                                tonalElevation = 0.dp
                            ) {
                                FitQuestTab.entries.forEach { tab ->
                                    val selected = activeTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { activeTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = OnAccentDark,
                                            selectedTextColor = ElectricLime,
                                            indicatorColor = ElectricLime,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavRail && overlay == OverlayScreen.NONE) {
                            NavigationRail(
                                containerColor = ObsidianSurface
                            ) {
                                FitQuestTab.entries.forEach { tab ->
                                    val selected = activeTab == tab
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { activeTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = { Text(tab.label) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = OnAccentDark,
                                            selectedTextColor = ElectricLime,
                                            indicatorColor = ElectricLime,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (overlay) {
                                OverlayScreen.WATER_TRACKER -> {
                                    WaterTrackerScreen(
                                        profile = profile,
                                        todayLog = todayLog,
                                        waterLogs = waterLogs,
                                        onAddWater = { ml -> viewModel.logWater(ml) },
                                        onDeleteLog = { entry -> viewModel.deleteWaterEntry(entry) },
                                        onBack = { overlay = OverlayScreen.NONE }
                                    )
                                }

                                OverlayScreen.DAILY_SUMMARY -> {
                                    DailySummaryScreen(
                                        profile = profile,
                                        todayLog = todayLog,
                                        dailyLogs = dailyLogs,
                                        onBack = { overlay = OverlayScreen.NONE }
                                    )
                                }

                                OverlayScreen.NONE -> {
                                    when (activeTab) {
                                        FitQuestTab.HOME -> HomeDashboardScreen(
                                            profile = profile,
                                            todayLog = todayLog,
                                            friendRank = userRank,
                                            featuredChallenge = featuredChallenge,
                                            onOpenWaterTracker = { overlay = OverlayScreen.WATER_TRACKER },
                                            onQuickStartActivity = { type ->
                                                viewModel.selectActivityType(type)
                                                activeTab = FitQuestTab.ACTIVITY
                                            },
                                            onQuickAddSteps = { viewModel.logSteps(500L) },
                                            onOpenDailySummary = { overlay = OverlayScreen.DAILY_SUMMARY },
                                            onOpenCompete = { activeTab = FitQuestTab.COMPETE },
                                            onLogChallengeProgress = { ch ->
                                                if (currentUserId in ch.participantIds) {
                                                    viewModel.logChallengeProgress(ch, 1500L)
                                                } else {
                                                    viewModel.joinChallenge(ch)
                                                }
                                            }
                                        )

                                        FitQuestTab.ACTIVITY -> ActivityTrackingScreen(
                                            liveState = liveTracking,
                                            activities = activities,
                                            onSelectType = { viewModel.selectActivityType(it) },
                                            onStart = { viewModel.startLiveActivity() },
                                            onPause = { viewModel.pauseLiveActivity() },
                                            onResume = { viewModel.resumeLiveActivity() },
                                            onFinish = { share -> viewModel.finishLiveActivity(share) },
                                            onQuickLog = { type, mins, dist, share ->
                                                viewModel.quickLogActivity(type, mins, dist, share)
                                            }
                                        )

                                        FitQuestTab.COMPETE -> CompeteScreen(
                                            currentUserId = currentUserId,
                                            profile = profile,
                                            leaderboard = leaderboard,
                                            challenges = challenges,
                                            friends = friends,
                                            feedPosts = feedPosts,
                                            onCreateChallenge = { title, desc, type, metric, target, xp, days, pub, team ->
                                                viewModel.createChallenge(
                                                    title = title,
                                                    description = desc,
                                                    type = type,
                                                    metric = metric,
                                                    targetValue = target,
                                                    rewardXp = xp,
                                                    durationDays = days,
                                                    publicChallenge = pub,
                                                    teamName = team
                                                )
                                            },
                                            onJoinChallenge = { viewModel.joinChallenge(it) },
                                            onLogChallengeProgress = { ch, delta -> viewModel.logChallengeProgress(ch, delta) },
                                            onAddFriend = { viewModel.addFriend(it) },
                                            onChallengeFriend = { uid, name -> viewModel.challengeFriendToStepBattle(uid, name) },
                                            onPostUpdate = { msg -> viewModel.postSocialUpdate(msg) },
                                            onReactPost = { post, field -> viewModel.reactToPost(post, field) }
                                        )

                                        FitQuestTab.REWARDS -> RewardsScreen(
                                            profile = profile,
                                            todayLog = todayLog,
                                            joinedAnyChallenge = joinedAnyChallenge,
                                            onInspectBadge = { badge -> viewModel.showBadgePreview(badge) }
                                        )

                                        FitQuestTab.PROFILE -> ProfileScreen(
                                            profile = profile,
                                            rank = userRank,
                                            dailyLogs = dailyLogs,
                                            activities = activities,
                                            onUpdateSettings = { name, avatar, steps, water, run, sports, region, pub, notif ->
                                                viewModel.updateProfileSettings(
                                                    username = name,
                                                    avatarId = avatar,
                                                    dailyStepGoal = steps,
                                                    dailyWaterGoalMl = water,
                                                    dailyRunGoalKm = run,
                                                    dailySportsGoalMin = sports,
                                                    region = region,
                                                    publicProfile = pub,
                                                    notificationsEnabled = notif
                                                )
                                            },
                                            onOpenDailySummary = { overlay = OverlayScreen.DAILY_SUMMARY },
                                            onSignOut = {
                                                signOutFitQuest(
                                                    credentialManager = credentialManager,
                                                    scope = scope
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Animated Workout Completion Reward Dialog
            workoutReward?.let { reward ->
                WorkoutRewardDialog(
                    reward = reward,
                    onDismiss = { viewModel.dismissWorkoutReward() }
                )
            }

            // Animated Badge Unlock / Inspection Dialog
            celebrationBadge?.let { badge ->
                BadgeCelebrationDialog(
                    badge = badge,
                    unlocked = badge.id in profile.unlockedBadges,
                    onDismiss = { viewModel.dismissBadgeCelebration() }
                )
            }
        }
    }
}
