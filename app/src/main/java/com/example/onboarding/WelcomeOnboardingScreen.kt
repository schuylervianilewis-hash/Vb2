package com.example.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.ListAlt
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
 * WelcomeOnboardingScreen: One-time first-launch setup flow.
 * Steps:
 * 1. Enable Vian Board in Android System Settings
 * 2. Select Vian Board as the Active/Default Input Method
 * 3. Go to Settings
 * FAB: Quick access to Log Keeper diagnostics
 */
@Composable
fun WelcomeOnboardingScreen(
    onboardingManager: OnboardingManager,
    onOpenSettings: () -> Unit = {},
    onOpenLogKeeper: () -> Unit
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
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenLogKeeper,
                icon = {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = "Log Keeper",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                },
                text = {
                    Text(
                        text = "Log Keeper",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                containerColor = SkyBluePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.5.dp, SkyBlueBorder),
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome to Vian Board",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ultra-lightweight, privacy-first keyboard with zero-overhead diagnostics and desktop micro-gestures.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Exactly 3 Steps Container
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Step 1: Enable in Settings
                OnboardingStepCard(
                    stepNumber = "1",
                    title = "Enable in System Settings",
                    subtitle = if (isEnabledInSettings) "Vian Board is enabled" else "Allow Vian Board in Language & Input",
                    icon = Icons.Default.Settings,
                    isCompleted = isEnabledInSettings,
                    actionButtonText = if (isEnabledInSettings) "Enabled" else "Enable",
                    onActionClick = {
                        LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "User tapped: Enable in Settings")
                        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    }
                )

                // Step 2: Select as Default
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

                // Step 3: Go to Settings
                OnboardingStepCard(
                    stepNumber = "3",
                    title = "Go to Settings",
                    subtitle = "Customize appearance, haptics, gestures, and tools",
                    icon = Icons.Default.Settings,
                    isCompleted = false,
                    actionButtonText = "Settings",
                    onActionClick = {
                        onboardingManager.setOnboardingCompleted(true)
                        onOpenSettings()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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
