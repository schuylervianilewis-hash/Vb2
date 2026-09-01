package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.logger.LogKeeper
import com.example.ui.logger.LogKeeperScreen
import com.example.ui.settings.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.welcome.WelcomeScreen

enum class ScreenState {
    WELCOME,
    SETTINGS_ROOT,
    SETTINGS_APPEARANCE,
    SETTINGS_UTILITY,
    SETTINGS_DICTIONARY,
    SETTINGS_SECURITY_VAULT,
    SETTINGS_PERSONAL_VAULT,
    SETTINGS_VOICE_INPUT,
    SETTINGS_SIDEBAR,
    SETTINGS_BACKUP_RESTORE,
    LOG_KEEPER
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogKeeper.logComponentStart("MainActivity")
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8FAFC)
                ) {
                    var currentScreen by remember { mutableStateOf(ScreenState.WELCOME) }
                    val backStack = remember { mutableStateListOf<ScreenState>() }

                    fun navigateTo(screen: ScreenState) {
                        backStack.add(currentScreen)
                        currentScreen = screen
                    }

                    fun navigateBack() {
                        if (backStack.isNotEmpty()) {
                            currentScreen = backStack.removeAt(backStack.size - 1)
                        } else {
                            currentScreen = ScreenState.WELCOME
                        }
                    }

                    when (currentScreen) {
                        ScreenState.WELCOME -> {
                            WelcomeScreen(
                                onOpenSettings = { navigateTo(ScreenState.SETTINGS_ROOT) },
                                onOpenLogKeeper = { navigateTo(ScreenState.LOG_KEEPER) }
                            )
                        }
                        ScreenState.SETTINGS_ROOT -> {
                            SettingsRootScreen(
                                onNavigateBack = { navigateBack() },
                                onNavigateToAppearance = { navigateTo(ScreenState.SETTINGS_APPEARANCE) },
                                onNavigateToUtility = { navigateTo(ScreenState.SETTINGS_UTILITY) },
                                onNavigateToDictionary = { navigateTo(ScreenState.SETTINGS_DICTIONARY) },
                                onNavigateToSecurityVault = { navigateTo(ScreenState.SETTINGS_SECURITY_VAULT) },
                                onNavigateToPersonalVault = { navigateTo(ScreenState.SETTINGS_PERSONAL_VAULT) },
                                onNavigateToVoiceInput = { navigateTo(ScreenState.SETTINGS_VOICE_INPUT) },
                                onNavigateToSidebar = { navigateTo(ScreenState.SETTINGS_SIDEBAR) },
                                onNavigateToBackupRestore = { navigateTo(ScreenState.SETTINGS_BACKUP_RESTORE) },
                                onNavigateToLogKeeper = { navigateTo(ScreenState.LOG_KEEPER) }
                            )
                        }
                        ScreenState.SETTINGS_APPEARANCE -> {
                            AppearanceSettingsScreen(onNavigateBack = { navigateBack() })
                        }
                        ScreenState.SETTINGS_UTILITY -> {
                            UtilitySettingsScreen(onNavigateBack = { navigateBack() })
                        }
                        ScreenState.SETTINGS_DICTIONARY -> {
                            DictionarySettingsScreen(onNavigateBack = { navigateBack() })
                        }
                        ScreenState.SETTINGS_SECURITY_VAULT -> {
                            SecurityVaultSettingsScreen(onNavigateBack = { navigateBack() })
                        }
                        ScreenState.SETTINGS_PERSONAL_VAULT -> {
                            PersonalVaultSettingsScreen(onNavigateBack = { navigateBack() })
                        }
                        ScreenState.SETTINGS_VOICE_INPUT -> {
                            VoiceInputSettingsScreen(onNavigateBack = { navigateBack() })
                        }
                        ScreenState.SETTINGS_SIDEBAR -> {
                            SidebarSettingsScreen(onNavigateBack = { navigateBack() })
                        }
                        ScreenState.SETTINGS_BACKUP_RESTORE -> {
                            BackupRestoreSettingsScreen(onNavigateBack = { navigateBack() })
                        }
                        ScreenState.LOG_KEEPER -> {
                            LogKeeperScreen(onNavigateBack = { navigateBack() })
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        LogKeeper.logComponentStop("MainActivity")
        super.onDestroy()
    }
}
