package com.example.ime.settings

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import com.example.R
import com.example.ime.toolbar.ToolbarPreferences
import com.example.ime.toolbar.ToolbarTool

class ToolbarSettingsActivity : Activity() {

    private lateinit var prefs: ToolbarPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toolbar_settings)

        prefs = ToolbarPreferences(this)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        val switchHidePinned = findViewById<Switch>(R.id.switchHidePinned)
        switchHidePinned.isChecked = prefs.hidePinnedWhenExpanded
        switchHidePinned.setOnCheckedChangeListener { _, isChecked ->
            prefs.hidePinnedWhenExpanded = isChecked
        }

        populatePinnedTools()
        populateExpandedTools()
    }

    private fun populatePinnedTools() {
        val container = findViewById<LinearLayout>(R.id.containerPinnedTools)
        container.removeAllViews()

        val activePinned = prefs.getPinnedTools().toMutableList()
        val allTools = ToolbarTool.values().toList()
        // Put active pinned tools first in their ordered sequence, followed by inactive tools
        val orderedTools = activePinned + allTools.filter { !activePinned.contains(it) }

        val inflater = LayoutInflater.from(this)
        for (i in orderedTools.indices) {
            val tool = orderedTools[i]
            val itemView = inflater.inflate(R.layout.item_toolbar_tool_setting, container, false)
            val ivIcon = itemView.findViewById<ImageView>(R.id.ivToolIcon)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvToolTitle)
            val switchTool = itemView.findViewById<Switch>(R.id.switchTool)
            val btnUp = itemView.findViewById<TextView>(R.id.btnMoveUp)
            val btnDown = itemView.findViewById<TextView>(R.id.btnMoveDown)

            ivIcon.setImageResource(tool.iconResId)
            tvTitle.setText(tool.titleResId)
            val isEnabled = activePinned.contains(tool)
            switchTool.isChecked = isEnabled

            // Visual dimming for disabled tools
            btnUp.visibility = if (isEnabled && i > 0) View.VISIBLE else View.INVISIBLE
            btnDown.visibility = if (isEnabled && i < activePinned.size - 1) View.VISIBLE else View.INVISIBLE

            btnUp.setOnClickListener {
                val curIdx = activePinned.indexOf(tool)
                if (curIdx > 0) {
                    java.util.Collections.swap(activePinned, curIdx, curIdx - 1)
                    prefs.setPinnedTools(activePinned)
                    populatePinnedTools()
                }
            }

            btnDown.setOnClickListener {
                val curIdx = activePinned.indexOf(tool)
                if (curIdx >= 0 && curIdx < activePinned.size - 1) {
                    java.util.Collections.swap(activePinned, curIdx, curIdx + 1)
                    prefs.setPinnedTools(activePinned)
                    populatePinnedTools()
                }
            }

            switchTool.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!activePinned.contains(tool)) activePinned.add(tool)
                } else {
                    activePinned.remove(tool)
                }
                prefs.setPinnedTools(activePinned)
                populatePinnedTools()
            }

            container.addView(itemView)
        }
    }

    private fun populateExpandedTools() {
        val container = findViewById<LinearLayout>(R.id.containerExpandedTools)
        container.removeAllViews()

        val activeExpanded = prefs.getExpandedTools().toMutableList()
        val allTools = ToolbarTool.values().toList()
        // Put active expanded tools first in their ordered sequence, followed by inactive tools
        val orderedTools = activeExpanded + allTools.filter { !activeExpanded.contains(it) }

        val inflater = LayoutInflater.from(this)
        for (i in orderedTools.indices) {
            val tool = orderedTools[i]
            val itemView = inflater.inflate(R.layout.item_toolbar_tool_setting, container, false)
            val ivIcon = itemView.findViewById<ImageView>(R.id.ivToolIcon)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvToolTitle)
            val switchTool = itemView.findViewById<Switch>(R.id.switchTool)
            val btnUp = itemView.findViewById<TextView>(R.id.btnMoveUp)
            val btnDown = itemView.findViewById<TextView>(R.id.btnMoveDown)

            ivIcon.setImageResource(tool.iconResId)
            tvTitle.setText(tool.titleResId)
            val isEnabled = activeExpanded.contains(tool)
            switchTool.isChecked = isEnabled

            btnUp.visibility = if (isEnabled && i > 0) View.VISIBLE else View.INVISIBLE
            btnDown.visibility = if (isEnabled && i < activeExpanded.size - 1) View.VISIBLE else View.INVISIBLE

            btnUp.setOnClickListener {
                val curIdx = activeExpanded.indexOf(tool)
                if (curIdx > 0) {
                    java.util.Collections.swap(activeExpanded, curIdx, curIdx - 1)
                    prefs.setExpandedTools(activeExpanded)
                    populateExpandedTools()
                }
            }

            btnDown.setOnClickListener {
                val curIdx = activeExpanded.indexOf(tool)
                if (curIdx >= 0 && curIdx < activeExpanded.size - 1) {
                    java.util.Collections.swap(activeExpanded, curIdx, curIdx + 1)
                    prefs.setExpandedTools(activeExpanded)
                    populateExpandedTools()
                }
            }

            switchTool.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!activeExpanded.contains(tool)) activeExpanded.add(tool)
                } else {
                    activeExpanded.remove(tool)
                }
                prefs.setExpandedTools(activeExpanded)
                populateExpandedTools()
            }

            container.addView(itemView)
        }
    }
}
