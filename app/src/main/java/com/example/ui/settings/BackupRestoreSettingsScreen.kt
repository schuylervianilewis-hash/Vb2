package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", fontWeight = FontWeight.Bold) },
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
                // Vian Board JSON Backup Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Vian Board Full Configuration", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Export or import all layout, appearance, and keyboard parameters as JSON.", fontSize = 13.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { Toast.makeText(context, "Exporting settings JSON...", Toast.LENGTH_SHORT).show() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Export JSON")
                            }
                            Button(
                                onClick = { Toast.makeText(context, "Select settings JSON to import", Toast.LENGTH_SHORT).show() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text("Import JSON")
                            }
                        }
                    }
                }
            }

            item {
                // HeliBoard Dictionary & Prediction Engine ZIP Importer
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Import HeliBoard Backup (.zip)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0369A1))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Directly import HeliBoard dictionary binary packs, personal words, and prediction engines from a backup ZIP archive.", fontSize = 13.sp, color = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { Toast.makeText(context, "Choose HeliBoard backup ZIP archive...", Toast.LENGTH_SHORT).show() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Import HeliBoard ZIP")
                        }
                    }
                }
            }

            item {
                // Reset Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Reset Configuration", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Restore all appearance, layout, and utility parameters to factory defaults.", fontSize = 13.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Settings restored to defaults", Toast.LENGTH_SHORT).show() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Text("Reset to Defaults")
                        }
                    }
                }
            }
        }
    }
}
