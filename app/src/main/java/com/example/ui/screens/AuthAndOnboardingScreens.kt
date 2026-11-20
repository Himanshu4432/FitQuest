package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.FITQUEST_AVATARS
import com.example.data.attemptAutoSignIn
import com.example.data.onGoogleSignInClicked
import com.example.ui.components.AthleteAvatar
import com.example.ui.components.ErrorBanner
import com.example.ui.components.SportyCard
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
import kotlin.math.roundToLong

@Composable
fun WelcomeAuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Hero background artwork
        Image(
            painter = painterResource(id = R.drawable.img_welcome_hero_1795197877020),
            contentDescription = "FitQuest glowing athletic sprinter hero art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(410.dp)
        )

        // Smooth dark vignette blending hero into content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to ObsidianBg.copy(alpha = 0.20f),
                        0.38f to ObsidianBg.copy(alpha = 0.72f),
                        0.55f to ObsidianBg,
                        1.0f to ObsidianBg
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Brand Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(ElectricLime, ElectricCyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "FitQuest emblem",
                        tint = OnAccentDark,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "FITQUEST",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "SOCIAL FITNESS ARENA",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime,
                        letterSpacing = 1.4.sp
                    )
                }
            }

            // Bottom Pitch + Sign-In CTA
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Surface(
                    color = ElectricLime.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, ElectricLime.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "EARN XP • CLIMB RANKS • KEEP STREAKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricLime,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Level Up\nYour Life.",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Turn every walk, run, match, and glass of water into XP. Score up to 1,000 daily health points, battle friends, and unlock epic badges.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(22.dp))

                // 3 Mini Feature Highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureMiniPill(Icons.AutoMirrored.Filled.DirectionsRun, "1,000 Pt Score", ElectricLime, Modifier.weight(1f))
                    FeatureMiniPill(Icons.Filled.WaterDrop, "Hydration XP", ElectricCyan, Modifier.weight(1f))
                    FeatureMiniPill(Icons.Filled.EmojiEvents, "Live Podium", SolarAmber, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                errorMessage?.let { msg ->
                    ErrorBanner(message = msg)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricLime,
                        contentColor = OnAccentDark
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = OnAccentDark,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign in with Google",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Synced securely across devices with your Google account",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FeatureMiniPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ObsidianCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextPrimary, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingWizardScreen(
    suggestedUsername: String,
    isSaving: Boolean,
    onComplete: (
        username: String,
        avatarId: String,
        interests: List<String>,
        dailyStepGoal: Long,
        dailyWaterGoalMl: Long,
        dailyRunGoalKm: Double,
        dailySportsGoalMin: Long,
        notificationsEnabled: Boolean
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var username by remember { mutableStateOf(suggestedUsername.ifBlank { "FitPlayer" }.take(20)) }
    var selectedAvatar by remember { mutableStateOf("bolt") }
    var selectedInterests by remember { mutableStateOf(setOf("Walking", "Running", "Hydration")) }
    var stepGoal by remember { mutableLongStateOf(10000L) }
    var waterGoalMl by remember { mutableLongStateOf(2500L) }
    var runGoalKm by remember { mutableDoubleStateOf(5.0) }
    var sportsGoalMin by remember { mutableLongStateOf(45L) }
    var activityPermGranted by remember { mutableStateOf(false) }
    var notifPermGranted by remember { mutableStateOf(false) }

    val activityPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> activityPermGranted = granted }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> notifPermGranted = granted }

    val allInterests = listOf("Walking", "Running", "Cycling", "Sports", "Hydration")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .widthIn(max = 600.dp)
    ) {
        // Header + Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                IconButton(
                    onClick = { step-- },
                    modifier = Modifier.testTag("onboarding_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous step", tint = TextPrimary)
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "STEP ${step + 1} OF 4",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricLime,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (step + 1) / 4f },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = ElectricLime,
                    trackColor = ObsidianCardElevated
                )
            }
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedContent(targetState = step, label = "onboarding_step") { currentStep ->
                when (currentStep) {
                    0 -> {
                        Column {
                            Text("Create Your Athlete", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Pick a username for leaderboards and choose your neon avatar emblem.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(22.dp))
                            OutlinedTextField(
                                value = username,
                                onValueChange = { if (it.length <= 24) username = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_username_input"),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricLime,
                                    unfocusedBorderColor = ObsidianBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedLabelColor = ElectricLime,
                                    unfocusedLabelColor = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(22.dp))
                            Text("Choose Avatar", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FITQUEST_AVATARS.forEach { option ->
                                    val selected = option.id == selectedAvatar
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(if (selected) ElectricLime.copy(alpha = 0.14f) else ObsidianCard)
                                            .border(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) ElectricLime else ObsidianBorder,
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                            .clickable { selectedAvatar = option.id }
                                            .padding(14.dp)
                                            .testTag("avatar_option_${option.id}")
                                    ) {
                                        AthleteAvatar(avatarId = option.id, size = 56.dp, ringBorder = selected)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) ElectricLime else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        Column {
                            Text("Your Fitness Quests", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Select the activities you enjoy so FitQuest can tailor your daily challenges.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(22.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                allInterests.forEach { interest ->
                                    val isChosen = interest in selectedInterests
                                    FilterChip(
                                        selected = isChosen,
                                        onClick = {
                                            selectedInterests = if (isChosen && selectedInterests.size > 1) {
                                                selectedInterests - interest
                                            } else {
                                                selectedInterests + interest
                                            }
                                        },
                                        label = {
                                            Text(
                                                interest,
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        },
                                        leadingIcon = if (isChosen) {
                                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElectricLime,
                                            selectedLabelColor = OnAccentDark,
                                            selectedLeadingIconColor = OnAccentDark,
                                            containerColor = ObsidianCard,
                                            labelColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.testTag("interest_chip_${interest.lowercase()}")
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Set Daily Goals", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                            Text(
                                "Your 1,000-point daily score scales fairly to your personal goals — consistency wins!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            GoalSliderCard(
                                title = "Daily Steps Goal",
                                valueLabel = "$stepGoal steps",
                                value = stepGoal.toFloat(),
                                range = 3000f..20000f,
                                accent = ElectricLime,
                                onChange = { stepGoal = ((it / 500f).roundToLong() * 500L) }
                            )
                            GoalSliderCard(
                                title = "Daily Water Goal",
                                valueLabel = String.format(Locale.US, "%.1f L", waterGoalMl / 1000.0),
                                value = waterGoalMl.toFloat(),
                                range = 1000f..4500f,
                                accent = ElectricCyan,
                                onChange = { waterGoalMl = ((it / 250f).roundToLong() * 250L) }
                            )
                            GoalSliderCard(
                                title = "Daily Running / Cycling Goal",
                                valueLabel = String.format(Locale.US, "%.1f km", runGoalKm),
                                value = runGoalKm.toFloat(),
                                range = 1f..15f,
                                accent = SolarAmber,
                                onChange = { runGoalKm = ((it * 2f).roundToLong() / 2.0) }
                            )
                            GoalSliderCard(
                                title = "Daily Sports & Workouts",
                                valueLabel = "$sportsGoalMin min",
                                value = sportsGoalMin.toFloat(),
                                range = 15f..120f,
                                accent = ElectricLime,
                                onChange = { sportsGoalMin = ((it / 5f).roundToLong() * 5L) }
                            )
                        }
                    }

                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Power Up Sensors", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                            Text(
                                "Optional permissions unlock automatic step tracking and friendly hydration & streak nudges.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            SportyCard(accentBorder = if (activityPermGranted) ElectricLime else ObsidianBorder) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.DirectionsRun,
                                        contentDescription = "Fitness Tracking",
                                        tint = ElectricLime,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Fitness & Step Tracking", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                        Text("Auto-sync hardware step counter", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    if (activityPermGranted) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = "Granted", tint = ElectricLime)
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                    activityPermLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                                                } else {
                                                    activityPermGranted = true
                                                }
                                            },
                                            modifier = Modifier.testTag("grant_activity_permission_button"),
                                            border = BorderStroke(1.dp, ElectricLime)
                                        ) {
                                            Text("Allow", color = ElectricLime)
                                        }
                                    }
                                }
                            }

                            SportyCard(accentBorder = if (notifPermGranted) ElectricCyan else ObsidianBorder) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.NotificationsActive,
                                        contentDescription = "Notifications",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Hydration & Streak Alerts", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                        Text("Gentle reminders to drink water & protect streaks", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    if (notifPermGranted) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = "Granted", tint = ElectricCyan)
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                } else {
                                                    notifPermGranted = true
                                                }
                                            },
                                            modifier = Modifier.testTag("grant_notification_permission_button"),
                                            border = BorderStroke(1.dp, ElectricCyan)
                                        ) {
                                            Text("Allow", color = ElectricCyan)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (step < 3) {
                    step++
                } else {
                    onComplete(
                        username.trim().ifBlank { "FitAthlete" },
                        selectedAvatar,
                        selectedInterests.toList(),
                        stepGoal,
                        waterGoalMl,
                        runGoalKm,
                        sportsGoalMin,
                        notifPermGranted
                    )
                }
            },
            enabled = !isSaving && (step != 0 || username.trim().length >= 3),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_next_button"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricLime,
                contentColor = OnAccentDark
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OnAccentDark, strokeWidth = 2.5.dp)
            } else {
                Text(
                    text = if (step < 3) "Continue" else "Start My FitQuest",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun GoalSliderCard(
    title: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    accent: Color,
    onChange: (Float) -> Unit
) {
    SportyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(
                text = valueLabel,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = accent
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = ObsidianCardElevated
            )
        )
    }
}
