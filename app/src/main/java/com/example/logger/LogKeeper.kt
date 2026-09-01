package com.example.logger

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Debug
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

enum class LogLevel {
    INFO,
    WARN,
    ERROR,
    LIFECYCLE,
    MEMORY
}

data class LogEntry(
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val memoryUsageMb: Double
)

data class ComponentStatus(
    val name: String,
    val isRunning: Boolean,
    val lastStateChange: String
)

object LogKeeper {
    private const val PREFS_NAME = "vian_log_keeper_prefs"
    private const val KEY_MASTER_SWITCH = "log_keeper_enabled"
    private const val MAX_LOG_ENTRIES = 300

    private var appContext: Context? = null
    private var prefs: SharedPreferences? = null

    private val logEntries = CopyOnWriteArrayList<LogEntry>()
    private val componentRegistry = java.util.concurrent.ConcurrentHashMap<String, ComponentStatus>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    var isEnabled: Boolean = true
        private set

    fun initialize(context: Context) {
        appContext = context.applicationContext
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isEnabled = prefs?.getBoolean(KEY_MASTER_SWITCH, true) ?: true
        logEvent("LogKeeper", "LogKeeper initialized. Master switch: $isEnabled", LogLevel.INFO)
    }

    fun setMasterSwitch(enabled: Boolean) {
        isEnabled = enabled
        prefs?.edit()?.putBoolean(KEY_MASTER_SWITCH, enabled)?.apply()
        logEvent("LogKeeper", "LogKeeper Master Switch toggled to $enabled", LogLevel.INFO)
    }

    fun getMemorySnapshotMb(): Double {
        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()).toDouble() / (1024 * 1024)
        return String.format(Locale.US, "%.2f", usedMem).toDouble()
    }

    fun getTotalMemoryMb(): Double {
        val runtime = Runtime.getRuntime()
        return String.format(Locale.US, "%.2f", runtime.totalMemory().toDouble() / (1024 * 1024)).toDouble()
    }

    fun getMaxMemoryMb(): Double {
        val runtime = Runtime.getRuntime()
        return String.format(Locale.US, "%.2f", runtime.maxMemory().toDouble() / (1024 * 1024)).toDouble()
    }

    fun logEvent(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        if (!isEnabled && level != LogLevel.ERROR) return

        val now = dateFormat.format(Date())
        val memMb = getMemorySnapshotMb()
        val entry = LogEntry(
            timestamp = now,
            level = level,
            tag = tag,
            message = message,
            memoryUsageMb = memMb
        )

        logEntries.add(entry)
        if (logEntries.size > MAX_LOG_ENTRIES) {
            // Trim oldest
            while (logEntries.size > MAX_LOG_ENTRIES) {
                logEntries.removeAt(0)
            }
        }
    }

    fun logComponentStart(componentName: String) {
        val now = dateFormat.format(Date())
        componentRegistry[componentName] = ComponentStatus(
            name = componentName,
            isRunning = true,
            lastStateChange = now
        )
        logEvent(
            tag = "LIFECYCLE",
            message = "STARTED component: $componentName (Heap: ${getMemorySnapshotMb()} MB)",
            level = LogLevel.LIFECYCLE
        )
    }

    fun logComponentStop(componentName: String) {
        val now = dateFormat.format(Date())
        componentRegistry[componentName] = ComponentStatus(
            name = componentName,
            isRunning = false,
            lastStateChange = now
        )
        logEvent(
            tag = "LIFECYCLE",
            message = "CLOSED component: $componentName (Heap: ${getMemorySnapshotMb()} MB)",
            level = LogLevel.LIFECYCLE
        )
    }

    fun logError(component: String, errorCode: String, errorDetails: String) {
        logEvent(
            tag = "ERROR",
            message = "[$component] Code: $errorCode | Details: $errorDetails",
            level = LogLevel.ERROR
        )
    }

    fun getLogs(): List<LogEntry> {
        return logEntries.toList().reversed()
    }

    fun getActiveComponents(): List<ComponentStatus> {
        return componentRegistry.values.toList()
    }

    fun clearLogs() {
        logEntries.clear()
        logEvent("LogKeeper", "All in-memory logs cleared by user", LogLevel.INFO)
    }

    fun exportLogsToFile(context: Context): File? {
        return try {
            val exportDir = File(context.cacheDir, "logs")
            if (!exportDir.exists()) exportDir.mkdirs()
            val file = File(exportDir, "vian_board_logs_${System.currentTimeMillis()}.txt")
            FileWriter(file).use { writer ->
                writer.write("=== VIAN BOARD RESOURCE & ERROR LOG ===\n")
                writer.write("Exported at: ${dateFormat.format(Date())}\n")
                writer.write("Heap Used: ${getMemorySnapshotMb()} MB / Total: ${getTotalMemoryMb()} MB / Max: ${getMaxMemoryMb()} MB\n")
                writer.write("\n--- ACTIVE & RECENT COMPONENTS ---\n")
                for (comp in getActiveComponents()) {
                    writer.write("• ${comp.name}: ${if (comp.isRunning) "RUNNING" else "STOPPED"} (Last changed: ${comp.lastStateChange})\n")
                }
                writer.write("\n--- LOG ENTRIES (${logEntries.size}) ---\n")
                for (entry in logEntries) {
                    writer.write("[${entry.timestamp}] [${entry.level}] [${entry.tag}] (${entry.memoryUsageMb} MB) ${entry.message}\n")
                }
            }
            file
        } catch (e: Exception) {
            logError("LogKeeper", "EXPORT_FAILED", e.message ?: "Unknown error")
            null
        }
    }
}
