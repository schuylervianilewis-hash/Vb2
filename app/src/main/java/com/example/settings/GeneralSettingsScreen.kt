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
 * GeneralSettingsScreen: The General Preferences screen aligned with Phase 1 HeliBoard architecture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
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
                title = { Text("General Preferences", fontWeight = FontWeight.Bold) },
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
                                valueRange = 1f..60f,
                                steps = 11,
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
                        subtitle = "Auditory click effect on typing",
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
                                    text = "Sound Volume",
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
                                valueRange = 0f..100f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = SkyBluePrimary,
                                    activeTrackColor = SkyBluePrimary
                                )
                            )
                        }
                    }
                }
            }

            // Section 2: Typing Behaviors
            SettingsSectionHeader(title = "Typing Behaviors")

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Auto-capitalization
                    SettingsToggleRow(
                        title = "Auto-Capitalization",
                        subtitle = "Capitalize the first letter of each sentence",
                        icon = Icons.Default.FormatSize,
                        checked = settings.autoCapitalization,
                        onCheckedChange = { updateSettings(settings.copy(autoCapitalization = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Double space period
                    SettingsToggleRow(
                        title = "Double-Space Period",
                        subtitle = "Double-tapping spacebar inserts a period followed by a space",
                        icon = Icons.Default.SpaceBar,
                        checked = settings.doubleSpacePeriod,
                        onCheckedChange = { updateSettings(settings.copy(doubleSpacePeriod = it)) }
                    )
                }
            }

            // Section 3: Gestures
            SettingsSectionHeader(title = "Keyboard Gestures")

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Spacebar glide
                    SettingsToggleRow(
                        title = "Spacebar Cursor Glide",
                        subtitle = "Slide horizontally on the spacebar to move cursor",
                        icon = Icons.Default.TouchApp,
                        checked = settings.spacebarCursorGlide,
                        onCheckedChange = { updateSettings(settings.copy(spacebarCursorGlide = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Backspace swipe delete
                    SettingsToggleRow(
                        title = "Swipe-to-Delete on Backspace",
                        subtitle = "Swipe left from delete key to highlight and erase words",
                        icon = Icons.Default.Backspace,
                        checked = settings.backspaceSwipeDelete,
                        onCheckedChange = { updateSettings(settings.copy(backspaceSwipeDelete = it)) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = SkyBluePrimary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, SkyBlueBorder.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) SkyBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = SkyBluePrimary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
