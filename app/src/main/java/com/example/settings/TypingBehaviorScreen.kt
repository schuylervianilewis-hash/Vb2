package com.example.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SkyBlueBorder
import com.example.ui.theme.SkyBluePrimary

/**
 * TypingBehaviorScreen: Merged settings for Haptics, Audio, Gestures,
 * Auto-Correction, Smart Multiply, and Symbol behaviors.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingBehaviorScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { GeneralSettingsManager(context) }
    var settings by remember { mutableStateOf(manager.load()) }

    fun updateSettings(newSettings: GeneralSettings) {
        settings = newSettings
        manager.save(newSettings)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Typing Behavior & Rules", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SkyBluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Haptic & Sound Feedback
            SettingsSectionHeader(title = "Haptics & Audio")

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Vibrate on keypress
                    SettingsToggleRow(
                        title = "Vibrate on Keypress",
                        subtitle = "Tactile haptic pulse on each key strike",
                        icon = Icons.Default.Vibration,
                        checked = settings.vibrateOnKeyPress,
                        onCheckedChange = { updateSettings(settings.copy(vibrateOnKeyPress = it)) }
                    )

                    if (settings.vibrateOnKeyPress) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Vibration Strength",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${settings.vibrationStrengthMs} ms",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBluePrimary
                                )
                            }
                            Slider(
                                value = settings.vibrationStrengthMs.toFloat(),
                                onValueChange = { updateSettings(settings.copy(vibrationStrengthMs = it.toInt())) },
                                valueRange = 5f..50f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = SkyBluePrimary,
                                    activeTrackColor = SkyBluePrimary
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Sound on keypress
                    SettingsToggleRow(
                        title = "Sound on Keypress",
                        subtitle = "Auditory click feedback on key strikes",
                        icon = Icons.Default.VolumeUp,
                        checked = settings.soundOnKeyPress,
                        onCheckedChange = { updateSettings(settings.copy(soundOnKeyPress = it)) }
                    )

                    if (settings.soundOnKeyPress) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Click Volume",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${settings.soundVolume}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBluePrimary
                                )
                            }
                            Slider(
                                value = settings.soundVolume.toFloat(),
                                onValueChange = { updateSettings(settings.copy(soundVolume = it.toInt())) },
                                valueRange = 10f..100f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = SkyBluePrimary,
                                    activeTrackColor = SkyBluePrimary
                                )
                            )
                        }
                    }
                }
            }

            // Section 2: Gestures & Fast Input
            SettingsSectionHeader(title = "Gestures & Fast Input")

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsToggleRow(
                        title = "Spacebar Cursor Glide",
                        subtitle = "Slide horizontally across spacebar to position cursor",
                        icon = Icons.Default.Swipe,
                        checked = settings.spacebarCursorGlide,
                        onCheckedChange = { updateSettings(settings.copy(spacebarCursorGlide = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingsToggleRow(
                        title = "Backspace Swipe Delete",
                        subtitle = "Slide left from backspace to quickly erase words",
                        icon = Icons.Default.Backspace,
                        checked = settings.backspaceSwipeDelete,
                        onCheckedChange = { updateSettings(settings.copy(backspaceSwipeDelete = it)) }
                    )
                }
            }

            // Section 3: Smart Typing & Character Rules
            SettingsSectionHeader(title = "Smart Typing & Math Rules")

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsToggleRow(
                        title = "Auto-Capitalization",
                        subtitle = "Automatically capitalize the first word of sentences",
                        icon = Icons.Default.TextFields,
                        checked = settings.autoCapitalization,
                        onCheckedChange = { updateSettings(settings.copy(autoCapitalization = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingsToggleRow(
                        title = "Double-Space Period",
                        subtitle = "Double-tap spacebar to quickly insert a period and space",
                        icon = Icons.Default.SpaceBar,
                        checked = settings.doubleSpacePeriod,
                        onCheckedChange = { updateSettings(settings.copy(doubleSpacePeriod = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingsToggleRow(
                        title = "Smart Multiply Morph (x -> ×)",
                        subtitle = "Converts 'x' between numbers into multiplication '×' (e.g. 5x5 -> 5×5)",
                        icon = Icons.Default.Close,
                        checked = settings.smartMultiplyMorph,
                        onCheckedChange = { updateSettings(settings.copy(smartMultiplyMorph = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingsToggleRow(
                        title = "Auto-Space After Punctuation",
                        subtitle = "Automatically inserts a space after commas and periods",
                        icon = Icons.Default.FormatQuote,
                        checked = settings.autoSpaceAfterPunctuation,
                        onCheckedChange = { updateSettings(settings.copy(autoSpaceAfterPunctuation = it)) }
                    )
                }
            }
        }
    }
}
