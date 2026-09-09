package com.example.ime.settings

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import com.example.R

enum class DesktopShortcutAction(val id: String, val title: String, val iconResId: Int) {
    SELECT_ALL("select_all", "Select All", R.drawable.ic_select_all),
    COPY("copy", "Copy", R.drawable.ic_copy),
    CUT("cut", "Cut", R.drawable.ic_cut),
    PASTE("paste", "Paste", R.drawable.ic_paste),
    UNDO("undo", "Undo", R.drawable.ic_undo),
    REDO("redo", "Redo", R.drawable.ic_redo),
    SELECT_WORD("select_word", "Select Word", R.drawable.ic_select_word),
    MOVE_UP("move_up", "Move Up", R.drawable.ic_arrow_up),
    MOVE_DOWN("move_down", "Move Down", R.drawable.ic_arrow_down);

    companion object {
        fun fromId(id: String): DesktopShortcutAction? = values().firstOrNull { it.id == id }
    }
}

class DesktopShortcutsSettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_desktop_shortcuts_settings)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        val prefs = getSharedPreferences("vian_shortcuts_prefs", Context.MODE_PRIVATE)
        val defaultList = listOf(
            DesktopShortcutAction.SELECT_ALL.id,
            DesktopShortcutAction.COPY.id,
            DesktopShortcutAction.CUT.id,
            DesktopShortcutAction.PASTE.id,
            DesktopShortcutAction.UNDO.id,
            DesktopShortcutAction.REDO.id
        )
        val saved = prefs.getString("active_shortcuts", defaultList.joinToString(",")) ?: ""
        val active = saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()

        val container = findViewById<LinearLayout>(R.id.containerShortcuts)
        container.removeAllViews()

        val inflater = LayoutInflater.from(this)
        for (action in DesktopShortcutAction.values()) {
            val itemView = inflater.inflate(R.layout.item_toolbar_tool_setting, container, false)
            val ivIcon = itemView.findViewById<ImageView>(R.id.ivToolIcon)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvToolTitle)
            val switchTool = itemView.findViewById<Switch>(R.id.switchTool)

            ivIcon.setImageResource(action.iconResId)
            tvTitle.text = action.title
            switchTool.isChecked = active.contains(action.id)

            switchTool.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!active.contains(action.id)) active.add(action.id)
                } else {
                    active.remove(action.id)
                }
                prefs.edit().putString("active_shortcuts", active.joinToString(",")).apply()
            }

            container.addView(itemView)
        }
    }
}
