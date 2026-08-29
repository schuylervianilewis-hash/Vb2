package com.example.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogTag
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
 * WelcomeOnboardingScreen: Ultra-lightweight, clean, minimalist menu list.
 * Stripped of fat cards and verbose descriptions.
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
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Minimalist Header
            Text(
                text = "Vian Board",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Enable in Settings
            SimpleSetupRow(
                title = "Enable in System Settings",
                icon = Icons.Default.Settings,
                isCompleted = isEnabledInSettings,
                actionText = if (isEnabledInSettings) "Enabled" else "Enable",
                onActionClick = {
                    LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "User tapped: Enable in Settings")
                    context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                }
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 2. Select Default
            SimpleSetupRow(
                title = "Select Default Keyboard",
                icon = Icons.Default.Keyboard,
                isCompleted = isSelectedAsDefault,
                actionText = if (isSelectedAsDefault) "Selected" else "Select",
                onActionClick = {
                    LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "User tapped: Select Default Keyboard")
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                }
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 3. Layout Tester
            SimpleNavRow(
                title = "Appearance & Layout Tester",
                icon = Icons.Default.Palette,
                onClick = onOpenAppearanceTester
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 4. Settings
            SimpleNavRow(
                title = "Settings",
                icon = Icons.Default.Settings,
                onClick = {
                    onboardingManager.setOnboardingCompleted(true)
                    onOpenSettings()
                }
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 5. Log Keeper
            SimpleNavRow(
                title = "Log Keeper",
                icon = Icons.Default.ListAlt,
                onClick = onOpenLogKeeper
            )
        }
    }
}

@Composable
fun SimpleNavRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SkyBluePrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SimpleSetupRow(
    title: String,
    icon: ImageVector,
    isCompleted: Boolean,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isCompleted) SkyBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(18.dp).padding(end = 4.dp)
            )
        } else {
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBluePrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(actionText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
