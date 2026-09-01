package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.keyboard.KeyboardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme = remember { KeyboardTheme.loadFromPrefs(context) }

    var keyHeightDp by remember { mutableStateOf(currentTheme.keyHeightDp) }
    var keyCornerRadiusDp by remember { mutableStateOf(currentTheme.keyCornerRadiusDp) }
    var borderWidthDp by remember { mutableStateOf(currentTheme.borderWidthDp) }
    var horizontalGapDp by remember { mutableStateOf(currentTheme.horizontalGapDp) }
    var verticalGapDp by remember { mutableStateOf(currentTheme.verticalGapDp) }
    var showPopups by remember { mutableStateOf(currentTheme.showPopups) }
    var showHints by remember { mutableStateOf(currentTheme.showHints) }
    var testText by remember { mutableStateOf("") }

    fun syncTheme() {
        val updated = currentTheme.copy(
            keyHeightDp = keyHeightDp,
            keyCornerRadiusDp = keyCornerRadiusDp,
            borderWidthDp = borderWidthDp,
            horizontalGapDp = horizontalGapDp,
            verticalGapDp = verticalGapDp,
            showPopups = showPopups,
            showHints = showHints
        )
        KeyboardTheme.saveToPrefs(context, updated)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Layout Tester
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Layout Tester", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = testText,
                            onValueChange = { testText = it },
                            placeholder = { Text("Tap here to test keyboard appearance...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            item {
                // Key Styling & Roundness Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Key Styling & Roundness", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Key Height: ${keyHeightDp.toInt()} dp", fontSize = 13.sp, color = Color(0xFF475569))
                        Slider(
                            value = keyHeightDp,
                            onValueChange = { 
                                keyHeightDp = it
                                syncTheme()
                            },
                            valueRange = 40f..70f
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Key Corner Radius: ${keyCornerRadiusDp.toInt()} dp", fontSize = 13.sp, color = Color(0xFF475569))
                        Slider(
                            value = keyCornerRadiusDp,
                            onValueChange = { 
                                keyCornerRadiusDp = it
                                syncTheme()
                            },
                            valueRange = 0f..24f
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Border Width: ${borderWidthDp.toInt()} dp", fontSize = 13.sp, color = Color(0xFF475569))
                        Slider(
                            value = borderWidthDp,
                            onValueChange = { 
                                borderWidthDp = it
                                syncTheme()
                            },
                            valueRange = 0f..4f
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Horizontal Key Gap: ${horizontalGapDp.toInt()} dp", fontSize = 13.sp, color = Color(0xFF475569))
                        Slider(
                            value = horizontalGapDp,
                            onValueChange = { 
                                horizontalGapDp = it
                                syncTheme()
                            },
                            valueRange = 0f..10f
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Vertical Row Gap: ${verticalGapDp.toInt()} dp", fontSize = 13.sp, color = Color(0xFF475569))
                        Slider(
                            value = verticalGapDp,
                            onValueChange = { 
                                verticalGapDp = it
                                syncTheme()
                            },
                            valueRange = 0f..14f
                        )
                    }
                }
            }

            item {
                // Key Popups & Hints Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Feedback & Hint Sources", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Key Press Popups", fontSize = 14.sp, color = Color(0xFF1E293B))
                            Switch(
                                checked = showPopups, 
                                onCheckedChange = { 
                                    showPopups = it
                                    syncTheme()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Secondary Symbol Hints", fontSize = 14.sp, color = Color(0xFF1E293B))
                            Switch(
                                checked = showHints, 
                                onCheckedChange = { 
                                    showHints = it
                                    syncTheme()
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
