package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SettingsCategoryItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRootScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToUtility: () -> Unit,
    onNavigateToDictionary: () -> Unit,
    onNavigateToSecurityVault: () -> Unit,
    onNavigateToPersonalVault: () -> Unit,
    onNavigateToVoiceInput: () -> Unit,
    onNavigateToSidebar: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToLogKeeper: () -> Unit
) {
    val items = listOf(
        SettingsCategoryItem("Appearance", Icons.Default.Palette, onNavigateToAppearance),
        SettingsCategoryItem("Utility", Icons.Default.Widgets, onNavigateToUtility),
        SettingsCategoryItem("Dictionary & Prediction Engine", Icons.Default.Spellcheck, onNavigateToDictionary),
        SettingsCategoryItem("Security Vault", Icons.Default.Lock, onNavigateToSecurityVault),
        SettingsCategoryItem("Personal Vault", Icons.Default.Shield, onNavigateToPersonalVault),
        SettingsCategoryItem("Voice Input", Icons.Default.Mic, onNavigateToVoiceInput),
        SettingsCategoryItem("Sidebar Partnership", Icons.Default.ViewSidebar, onNavigateToSidebar),
        SettingsCategoryItem("Backup & Restore", Icons.Default.SettingsBackupRestore, onNavigateToBackupRestore),
        SettingsCategoryItem("Log Keeper", Icons.Default.MonitorHeart, onNavigateToLogKeeper)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items.size) { index ->
                val item = items[index]
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.onClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
