package com.example.ime.toolbar

enum class ToolbarTool(
    val id: String,
    val titleResId: Int,
    val iconResId: Int,
    val isDefaultExpanded: Boolean,
    val isDefaultPinnedRight: Boolean
) {
    INCOGNITO("incognito", com.example.R.string.tool_incognito, com.example.R.drawable.ic_incognito, true, false),
    UNDO("undo", com.example.R.string.tool_undo, com.example.R.drawable.ic_undo, true, false),
    REDO("redo", com.example.R.string.tool_redo, com.example.R.drawable.ic_redo, true, false),
    SELECT_WORD("select_word", com.example.R.string.tool_select_word, com.example.R.drawable.ic_select_word, true, true),
    SELECT_ALL("select_all", com.example.R.string.tool_select_all, com.example.R.drawable.ic_select_all, false, false),
    COPY("copy", com.example.R.string.tool_copy, com.example.R.drawable.ic_copy, true, true),
    PASTE("paste", com.example.R.string.tool_paste, com.example.R.drawable.ic_paste, true, true),
    UP("up", com.example.R.string.tool_up, com.example.R.drawable.ic_arrow_up, false, false),
    DOWN("down", com.example.R.string.tool_down, com.example.R.drawable.ic_arrow_down, false, false),
    VOICE("voice", com.example.R.string.tool_voice, com.example.R.drawable.ic_mic, true, false),
    PROMPT_LIST("prompt_list", com.example.R.string.tool_prompt_list, com.example.R.drawable.ic_prompt_list, true, false),
    SECURITY_VAULT("security_vault", com.example.R.string.tool_security_vault, com.example.R.drawable.ic_security_vault, true, false),
    DESKTOP_SHORTCUTS("desktop_shortcuts", com.example.R.string.tool_desktop_shortcuts, com.example.R.drawable.ic_desktop_shortcuts, true, false),
    SETTINGS("settings", com.example.R.string.tool_settings, com.example.R.drawable.ic_settings, true, false);

    companion object {
        fun fromId(id: String): ToolbarTool? = values().firstOrNull { it.id == id }
    }
}
