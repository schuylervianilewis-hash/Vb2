package com.example.ime.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import com.example.R

class LayoutCustomizationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_customization)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<LinearLayout>(R.id.cardAppearance).setOnClickListener {
            startActivity(Intent(this, AppearanceSettingsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardToolbar).setOnClickListener {
            startActivity(Intent(this, ToolbarSettingsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardDesktopShortcuts).setOnClickListener {
            startActivity(Intent(this, DesktopShortcutsSettingsActivity::class.java))
        }
    }
}
