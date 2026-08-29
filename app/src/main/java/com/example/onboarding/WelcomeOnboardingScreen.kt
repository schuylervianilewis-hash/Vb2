package com.example.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogTag
import com.example.ui.theme.SkyBlueBorder
import com.example.ui.theme.SkyBluePrimary

/**
 * OnboardingManager: Tracks whether the first-time setup has been completed.
 */
class OnboardingManager(context: Context) {
    private val prefs = context.getSharedPreferences("vian_board_onboarding_prefs", Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("key_onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("key_onboarding_completed", completed).apply()
        LogKeeper.log(LogTag.SYSTEM, LogLevel.INFO, "Onboarding status updated: completed=$completed")
    }
}

/**
 * WelcomeOnboardingScreen: Clean, minimalist home/welcome dashboard.
 * Contains:
 * - IME Activation Steps
 * - Live In-Browser Appearance & Layout Tester
 * - Settings
 * - Log Keeper
 */
@Composable
fun WelcomeOnboardingScreen(
    onboardingManager: OnboardingManager,
    onOpenAppearanceTester: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenLogKeeper: () -> Unit = {}
) {
    val context = LocalContext.current
    var isEnabledInSettings by remember { mutableStateOf(false) }
    var isSelectedAsDefault by remember { mutableStateOf(false) }

    fun checkStatus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledList = imm.enabledInputMethodList
        val pkgName = context.packageName
        isEnabledInSettings = enabledList.any { it.packageName == pkgName }

        val currentIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
        isSelectedAsDefault = currentIme.contains(pkgName)
    }

    LaunchedEffect(Unit) {
        checkStatus()
        LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "Navigated to: WelcomeOnboardingScreen")
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.5.dp, SkyBlueBorder),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome to Vian Board",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ultra-lightweight, privacy-first keyboard with zero-overhead diagnostics and modular overlays.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clean list of items
            // 1. Step 1: Enable in Settings
            OnboardingStepCard(
                stepNumber = "1",
                title = "Enable in System Settings",
                subtitle = if (isEnabledInSettings) "Vian Board is enabled in system" else "Allow Vian Board in Language & Input",
                icon = Icons.Default.Settings,
                isCompleted = isEnabledInSettings,
                actionButtonText = if (isEnabledInSettings) "Enabled" else "Enable",
                onActionClick = {
                    LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "User tapped: Enable in Settings")
                    context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                }
            )

            // 2. Step 2: Select as Default Keyboard
            OnboardingStepCard(
                stepNumber = "2",
                title = "Select Default Keyboard",
                subtitle = if (isSelectedAsDefault) "Vian Board is active keyboard" else "Switch default input method to Vian Board",
                icon = Icons.Default.Keyboard,
                isCompleted = isSelectedAsDefault,
                actionButtonText = if (isSelectedAsDefault) "Selected" else "Select",
                onActionClick = {
                    LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "User tapped: Select Default Keyboard")
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                }
            )

            // 3. Live Appearance & Layout Tester Item
            WelcomeListItemCard(
                title = "Appearance & Layout Tester",
                subtitle = "Live in-browser preview of all layouts, popups & modals (no APK install needed)",
                icon = Icons.Default.Palette,
                badgeText = "Preview",
                onClick = onOpenAppearanceTester
            )

            // 4. Settings Item
            WelcomeListItemCard(
                title = "Vian Board Settings",
                subtitle = "Configure layouts, haptics, gestures, and preferences",
                icon = Icons.Default.Settings,
                onClick = {
                    onboardingManager.setOnboardingCompleted(true)
                    onOpenSettings()
                }
            )

            // 5. Log Keeper Item
            WelcomeListItemCard(
                title = "Log Keeper Diagnostics",
                subtitle = "Master On/Off switch, in-memory log buffer, and export",
                icon = Icons.Default.ListAlt,
                badgeText = "Audit",
                onClick = onOpenLogKeeper
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun WelcomeListItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, SkyBlueBorder.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, SkyBlueBorder.copy(alpha = 0.5f)),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SkyBluePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun OnboardingStepCard(
    stepNumber: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isCompleted: Boolean,
    actionButtonText: String,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isCompleted) SkyBlueBorder else SkyBlueBorder.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isCompleted) SkyBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isCompleted) SkyBluePrimary else SkyBlueBorder.copy(alpha = 0.5f)),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SkyBluePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isCompleted) {
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SkyBlueBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(actionButtonText, fontSize = 13.sp, color = SkyBluePrimary)
                }
            } else {
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBluePrimary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(actionButtonText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
