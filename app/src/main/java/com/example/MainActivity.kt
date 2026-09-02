package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.FileProvider
import com.example.ime.keyboard.KeyboardTheme
import com.example.logger.LogKeeper

class MainActivity : Activity() {

    private lateinit var themeConfig: KeyboardTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogKeeper.logComponentStart("MainActivity")
        setContentView(R.layout.activity_main)

        themeConfig = KeyboardTheme.loadFromPrefs(this)
        setupViews()
    }

    private fun setupViews() {
        val btnEnableIme = findViewById<Button>(R.id.btnEnableIme)
        val btnSelectIme = findViewById<Button>(R.id.btnSelectIme)
        val btnLogs = findViewById<Button>(R.id.btnLogs)

        val tvKeyHeight = findViewById<TextView>(R.id.tvKeyHeight)
        val sbKeyHeight = findViewById<SeekBar>(R.id.sbKeyHeight)

        val tvKeyCornerRadius = findViewById<TextView>(R.id.tvKeyCornerRadius)
        val sbKeyCornerRadius = findViewById<SeekBar>(R.id.sbKeyCornerRadius)

        val tvKeyGap = findViewById<TextView>(R.id.tvKeyGap)
        val sbKeyGap = findViewById<SeekBar>(R.id.sbKeyGap)

        val swShowPopups = findViewById<Switch>(R.id.swShowPopups)
        val swShowHints = findViewById<Switch>(R.id.swShowHints)

        // 1. Setup Enable / Select buttons
        btnEnableIme.setOnClickListener {
            LogKeeper.logEvent("Onboarding", "User clicked Open System Settings")
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        btnSelectIme.setOnClickListener {
            LogKeeper.logEvent("Onboarding", "User clicked Switch Input Method")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }

        btnLogs.setOnClickListener {
            showLogsView()
        }

        // 2. Setup Sliders
        sbKeyHeight.progress = themeConfig.keyHeightDp.toInt()
        tvKeyHeight.text = "Key Height: ${sbKeyHeight.progress} dp"
        sbKeyHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvKeyHeight.text = "Key Height: $progress dp"
                    themeConfig = themeConfig.copy(keyHeightDp = progress.toFloat())
                    KeyboardTheme.saveToPrefs(this@MainActivity, themeConfig)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbKeyCornerRadius.progress = themeConfig.keyCornerRadiusDp.toInt()
        tvKeyCornerRadius.text = "Key Corner Radius: ${sbKeyCornerRadius.progress} dp"
        sbKeyCornerRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvKeyCornerRadius.text = "Key Corner Radius: $progress dp"
                    themeConfig = themeConfig.copy(keyCornerRadiusDp = progress.toFloat())
                    KeyboardTheme.saveToPrefs(this@MainActivity, themeConfig)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbKeyGap.progress = themeConfig.horizontalGapDp.toInt()
        tvKeyGap.text = "Horizontal Gap: ${sbKeyGap.progress} dp"
        sbKeyGap.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvKeyGap.text = "Horizontal Gap: $progress dp"
                    themeConfig = themeConfig.copy(horizontalGapDp = progress.toFloat(), verticalGapDp = progress.toFloat() + 1f)
                    KeyboardTheme.saveToPrefs(this@MainActivity, themeConfig)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        swShowPopups.isChecked = themeConfig.showPopups
        swShowPopups.setOnCheckedChangeListener { _, isChecked ->
            themeConfig = themeConfig.copy(showPopups = isChecked)
            KeyboardTheme.saveToPrefs(this, themeConfig)
        }

        swShowHints.isChecked = themeConfig.showHints
        swShowHints.setOnCheckedChangeListener { _, isChecked ->
            themeConfig = themeConfig.copy(showHints = isChecked)
            KeyboardTheme.saveToPrefs(this, themeConfig)
        }
    }

    private fun showLogsView() {
        setContentView(R.layout.activity_logs)

        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnExportLogs = findViewById<Button>(R.id.btnExportLogs)
        val btnClearLogs = findViewById<Button>(R.id.btnClearLogs)
        val tvMemoryStats = findViewById<TextView>(R.id.tvMemoryStats)
        val tvLogContent = findViewById<TextView>(R.id.tvLogContent)

        tvMemoryStats.text = "Heap: ${LogKeeper.getMemorySnapshotMb()} MB / Max: ${LogKeeper.getMaxMemoryMb()} MB"
        
        val logs = LogKeeper.getLogs()
        val sb = StringBuilder()
        for (log in logs) {
            sb.append("[${log.timestamp}] [${log.level}] (${log.memoryUsageMb} MB) ${log.message}\n")
        }
        tvLogContent.text = sb.toString()

        btnBack.setOnClickListener {
            setContentView(R.layout.activity_main)
            setupViews()
        }

        btnClearLogs.setOnClickListener {
            LogKeeper.clearLogs()
            tvLogContent.text = "Logs cleared."
        }

        btnExportLogs.setOnClickListener {
            val file = LogKeeper.exportLogsToFile(this)
            if (file != null) {
                val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Export Vian Logs"))
            } else {
                Toast.makeText(this, "Failed to export logs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        LogKeeper.logComponentStop("MainActivity")
        super.onDestroy()
    }
}
