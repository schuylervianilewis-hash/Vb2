package com.example.ime.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import com.example.R
import com.example.logger.LogViewerActivity

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<LinearLayout>(R.id.cardLayoutCustomization).setOnClickListener {
            startActivity(Intent(this, LayoutCustomizationActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardPromptList).setOnClickListener {
            // Prepared for Phase 4: Prompt List Management page
        }

        findViewById<LinearLayout>(R.id.cardSecurity).setOnClickListener {
            // Prepared for Phase 6: Security Settings page
        }

        findViewById<LinearLayout>(R.id.cardLogKeeper).setOnClickListener {
            startActivity(Intent(this, LogViewerActivity::class.java))
        }
    }
}
