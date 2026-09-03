package com.example.ime.toolbar

enum class ToolbarTool(
    val id: String,
    val titleResId: Int,
    val iconResId: Int,
    val isDefaultExpanded: Boolean,
    val isDefaultPinnedRight: Boolean
) {
    SETTINGS("settings", com.example.R.string.tool_settings, com.example.R.drawable.ic_settings, true, false),
    CLIPBOARD("clipboard", com.example.R.string.tool_clipboard, com.example.R.drawable.ic_clipboard, true, false),
    TEXT_EDIT("text_edit", com.example.R.string.tool_text_edit, com.example.R.drawable.ic_text_edit, true, false),
    THEME("theme", com.example.R.string.tool_theme, com.example.R.drawable.ic_palette, true, false),
    EMOJI("emoji", com.example.R.string.tool_emoji, com.example.R.drawable.ic_emoji, true, false),
    NUMBER_ROW("number_row", com.example.R.string.tool_number_row, com.example.R.drawable.ic_number_row, true, false),
    CLEAR_CLIPBOARD("clear_clipboard", com.example.R.string.tool_clear_clipboard, com.example.R.drawable.ic_delete_sweep, true, false),
    INCOGNITO("incognito", com.example.R.string.tool_incognito, com.example.R.drawable.ic_incognito, true, false),
    ONE_HANDED("one_handed", com.example.R.string.tool_one_handed, com.example.R.drawable.ic_one_hand, true, false),
    FLOATING("floating", com.example.R.string.tool_floating, com.example.R.drawable.ic_floating_keyboard, true, false),
    UNDO("undo", com.example.R.string.tool_undo, com.example.R.drawable.ic_undo, true, false),
    REDO("redo", com.example.R.string.tool_redo, com.example.R.drawable.ic_redo, true, false),
    SELECT_WORD("select_word", com.example.R.string.tool_select_word, com.example.R.drawable.ic_select_word, true, true),
    SELECT_ALL("select_all", com.example.R.string.tool_select_all, com.example.R.drawable.ic_select_all, false, false),
    COPY("copy", com.example.R.string.tool_copy, com.example.R.drawable.ic_copy, true, true),
    PASTE("paste", com.example.R.string.tool_paste, com.example.R.drawable.ic_paste, true, true),
    UP("up", com.example.R.string.tool_up, com.example.R.drawable.ic_arrow_up, false, false),
    DOWN("down", com.example.R.string.tool_down, com.example.R.drawable.ic_arrow_down, false, false),
    VOICE("voice", com.example.R.string.tool_voice, com.example.R.drawable.ic_mic, true, false),
    LOG_KEEPER("log_keeper", com.example.R.string.tool_log_keeper, com.example.R.drawable.ic_log_keeper, true, false),
    PERSONAL_VAULT("personal_vault", com.example.R.string.tool_personal_vault, com.example.R.drawable.ic_personal_vault, false, false),
    SECURITY_VAULT("security_vault", com.example.R.string.tool_security_vault, com.example.R.drawable.ic_security_vault, false, false),
    PROMPT_LIST("prompt_list", com.example.R.string.tool_prompt_list, com.example.R.drawable.ic_prompt_list, false, false),
    DESKTOP_SHORTCUTS("desktop_shortcuts", com.example.R.string.tool_desktop_shortcuts, com.example.R.drawable.ic_desktop_shortcuts, false, false);

    companion object {
        fun fromId(id: String): ToolbarTool? = values().firstOrNull { it.id == id }
    }
}
