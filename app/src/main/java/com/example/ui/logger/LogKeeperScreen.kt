package com.example.ui.logger

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.logger.ComponentStatus
import com.example.logger.LogEntry
import com.example.logger.LogKeeper
import com.example.logger.LogLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogKeeperScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(LogKeeper.isEnabled) }
    var logs by remember { mutableStateOf(LogKeeper.getLogs()) }
    var components by remember { mutableStateOf(LogKeeper.getActiveComponents()) }
    var heapMb by remember { mutableStateOf(LogKeeper.getMemorySnapshotMb()) }
    var totalMb by remember { mutableStateOf(LogKeeper.getTotalMemoryMb()) }
    var maxMb by remember { mutableStateOf(LogKeeper.getMaxMemoryMb()) }
    var filterLevel by remember { mutableStateOf<LogLevel?>(null) }

    fun refresh() {
        logs = LogKeeper.getLogs()
        components = LogKeeper.getActiveComponents()
        heapMb = LogKeeper.getMemorySnapshotMb()
        totalMb = LogKeeper.getTotalMemoryMb()
        maxMb = LogKeeper.getMaxMemoryMb()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Log Keeper", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = {
                        val file = LogKeeper.exportLogsToFile(context)
                        if (file != null) {
                            val uri = try {
                                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            } catch (e: Exception) {
                                null
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Vian Board Resource Logs")
                                putExtra(Intent.EXTRA_TEXT, file.readText())
                                if (uri != null) {
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export Vian Board Logs"))
                        } else {
                            Toast.makeText(context, "Failed to export logs", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export Logs")
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
                // Master Switch Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Master Logging Switch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                if (isEnabled) "Active resource & error tracking" else "Logging paused",
                                fontSize = 13.sp,
                                color = if (isEnabled) Color(0xFF0284C7) else Color(0xFF64748B)
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = {
                                isEnabled = it
                                LogKeeper.setMasterSwitch(it)
                                refresh()
                            }
                        )
                    }
                }
            }

            item {
                // Live RAM & Memory Metric Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Runtime Memory (Heap)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF0369A1)
                            )
                            Text(
                                "$heapMb MB used",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0284C7)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Allocated: $totalMb MB", fontSize = 12.sp, color = Color(0xFF0284C7))
                            Text("Max Heap: $maxMb MB", fontSize = 12.sp, color = Color(0xFF0284C7))
                        }
                    }
                }
            }

            item {
                // Active Components Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Component Status Tracker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (components.isEmpty()) {
                            Text("No components registered yet", fontSize = 13.sp, color = Color(0xFF64748B))
                        } else {
                            components.forEach { comp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    if (comp.isRunning) Color(0xFF22C55E) else Color(0xFF94A3B8),
                                                    RoundedCornerShape(4.dp)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(comp.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                                    }
                                    Text(
                                        if (comp.isRunning) "RUNNING" else "CLOSED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (comp.isRunning) Color(0xFF16A34A) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Filter & Clear Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Log Stream (${logs.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    TextButton(onClick = {
                        LogKeeper.clearLogs()
                        refresh()
                    }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Logs", fontSize = 12.sp, color = Color(0xFFEF4444))
                    }
                }
            }

            val displayedLogs = if (filterLevel != null) {
                logs.filter { it.level == filterLevel }
            } else {
                logs
            }

            if (displayedLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No logs recorded yet", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }
                }
            } else {
                items(displayedLogs) { entry ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val badgeColor = when (entry.level) {
                                        LogLevel.ERROR -> Color(0xFFFEE2E2)
                                        LogLevel.WARN -> Color(0xFFFEF3C7)
                                        LogLevel.LIFECYCLE -> Color(0xFFE0E7FF)
                                        LogLevel.MEMORY -> Color(0xFFE0F2FE)
                                        LogLevel.INFO -> Color(0xFFF1F5F9)
                                    }
                                    val textColor = when (entry.level) {
                                        LogLevel.ERROR -> Color(0xFFDC2626)
                                        LogLevel.WARN -> Color(0xFFD97706)
                                        LogLevel.LIFECYCLE -> Color(0xFF4338CA)
                                        LogLevel.MEMORY -> Color(0xFF0369A1)
                                        LogLevel.INFO -> Color(0xFF475569)
                                    }
                                    Surface(
                                        color = badgeColor,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            entry.level.name,
                                            color = textColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(entry.tag, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                }
                                Text("${entry.memoryUsageMb} MB", fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                entry.message,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A),
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                entry.timestamp,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
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
