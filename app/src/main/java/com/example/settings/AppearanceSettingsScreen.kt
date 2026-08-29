package com.example.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Pin
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
import java.util.Locale

/**
 * AppearanceSettingsScreen: Clean, dedicated settings for Keyboard Sizing,
 * Keycap Geometry, Outline Borders, and Visual Styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
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
                title = { Text("Appearance & Key Styling", fontWeight = FontWeight.Bold) },
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
            // Section 1: Keyboard Sizing & Layout Dimensions
            SettingsSectionHeader(title = "Keyboard Dimensions & Layout")

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Height Scale
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Keyboard Height Scale",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.US, "%.2fx", settings.heightScale),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary
                            )
                        }
                        Text(
                            text = "Scales the vertical dimension of the keyboard surface",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.heightScale,
                            onValueChange = { updateSettings(settings.copy(heightScale = it)) },
                            valueRange = 0.8f..1.4f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = SkyBluePrimary,
                                activeTrackColor = SkyBluePrimary
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Bottom Inset Padding
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Bottom Inset Margin",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${settings.bottomInsetPaddingDp} dp",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary
                            )
                        }
                        Text(
                            text = "Extra clearance above system gesture / 3-button navigation bar",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.bottomInsetPaddingDp.toFloat(),
                            onValueChange = { updateSettings(settings.copy(bottomInsetPaddingDp = it.toInt())) },
                            valueRange = 0f..32f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = SkyBluePrimary,
                                activeTrackColor = SkyBluePrimary
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Dedicated Number Row
                    SettingsToggleRow(
                        title = "Dedicated Number Row",
                        subtitle = "Always show full 0-9 digits row on top of keyboard",
                        icon = Icons.Default.Pin,
                        checked = settings.showNumberRow,
                        onCheckedChange = { updateSettings(settings.copy(showNumberRow = it)) }
                    )
                }
            }

            // Section 2: Keycap Geometry & Button Shapes
            SettingsSectionHeader(title = "Keycap Geometry & Shapes")

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Corner Radius
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Key Corner Roundness",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${settings.keyCornerRadiusDp.toInt()} dp",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary
                            )
                        }
                        Text(
                            text = if (settings.keyCornerRadiusDp <= 1f) "Sharp rectangular (0dp)" else if (settings.keyCornerRadiusDp >= 14f) "Pill-shaped / Fully rounded" else "Rounded key corners",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.keyCornerRadiusDp,
                            onValueChange = { updateSettings(settings.copy(keyCornerRadiusDp = it)) },
                            valueRange = 0f..16f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = SkyBluePrimary,
                                activeTrackColor = SkyBluePrimary
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Horizontal Gap
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Horizontal Key Spacing",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f dp", settings.keyHorizontalGapDp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary
                            )
                        }
                        Text(
                            text = "Gap between horizontal adjacent keys",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.keyHorizontalGapDp,
                            onValueChange = { updateSettings(settings.copy(keyHorizontalGapDp = it)) },
                            valueRange = 0f..8f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = SkyBluePrimary,
                                activeTrackColor = SkyBluePrimary
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Vertical Gap
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Vertical Key Spacing",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f dp", settings.keyVerticalGapDp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary
                            )
                        }
                        Text(
                            text = "Gap between vertical keyboard rows",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.keyVerticalGapDp,
                            onValueChange = { updateSettings(settings.copy(keyVerticalGapDp = it)) },
                            valueRange = 0f..8f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = SkyBluePrimary,
                                activeTrackColor = SkyBluePrimary
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Key Outline Border
                    SettingsToggleRow(
                        title = "Key Outline Border",
                        subtitle = "Draw subtle contrast outline border around keycaps",
                        icon = Icons.Default.BorderColor,
                        checked = settings.keyOutlineEnabled,
                        onCheckedChange = { updateSettings(settings.copy(keyOutlineEnabled = it)) }
                    )

                    if (settings.keyOutlineEnabled) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Border Width",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = String.format(Locale.US, "%.2f dp", settings.keyBorderWidthDp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBluePrimary
                                )
                            }
                            Slider(
                                value = settings.keyBorderWidthDp,
                                onValueChange = { updateSettings(settings.copy(keyBorderWidthDp = it)) },
                                valueRange = 0.25f..2.5f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = SkyBluePrimary,
                                    activeTrackColor = SkyBluePrimary
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Reset button styling to default
                    OutlinedButton(
                        onClick = {
                            updateSettings(
                                settings.copy(
                                    keyCornerRadiusDp = 6f,
                                    keyHorizontalGapDp = 2.5f,
                                    keyVerticalGapDp = 3.5f,
                                    keyBorderWidthDp = 0.75f,
                                    keyOutlineEnabled = true
                                )
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Button Style to Default", color = SkyBluePrimary)
                    }
                }
            }
        }
    }
}
