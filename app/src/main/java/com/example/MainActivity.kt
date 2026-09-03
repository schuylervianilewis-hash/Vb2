package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import com.example.ime.settings.SettingsActivity
import com.example.logger.LogKeeper

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogKeeper.logComponentStart("MainActivity")
        setContentView(R.layout.activity_main)

        setupViews()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnEnableIme).setOnClickListener {
            LogKeeper.logEvent("Onboarding", "User clicked Open System Settings")
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        findViewById<Button>(R.id.btnSelectIme).setOnClickListener {
            LogKeeper.logEvent("Onboarding", "User clicked Switch Input Method")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }
    }

    override fun onDestroy() {
        LogKeeper.logComponentStop("MainActivity")
        super.onDestroy()
    }
}
