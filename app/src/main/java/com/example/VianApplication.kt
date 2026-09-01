package com.example

import android.app.Application
import com.example.logger.LogKeeper

class VianApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LogKeeper.initialize(this)
        LogKeeper.logComponentStart("VianApplication")

        // Uncaught exception crash catcher
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            LogKeeper.logError(
                component = "UncaughtCrashHandler",
                errorCode = "FATAL_EXCEPTION",
                errorDetails = "${throwable.javaClass.simpleName}: ${throwable.message}"
            )
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        LogKeeper.logEvent("MemoryTrim", "Trim level: $level")
    }
}
