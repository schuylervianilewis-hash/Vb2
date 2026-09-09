package com.example.ime.clipboard

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

/**
 * Lightweight, zero-idle persistence store for clipboard history and pinned clips.
 * Holds user clips locally in SharedPreferences with no background services.
 */
class ClipboardStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "vian_clipboard_store"
        private const val KEY_PINNED = "pinned_items"
        private const val KEY_RECENT = "recent_items"
        private const val MAX_RECENT = 25
    }

    fun getPinnedClips(): List<String> = loadList(KEY_PINNED)

    fun getRecentClips(): List<String> = loadList(KEY_RECENT)

    fun addClip(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val pinned = getPinnedClips()
        if (pinned.contains(trimmed)) return

        val recent = getRecentClips().toMutableList()
        recent.remove(trimmed)
        recent.add(0, trimmed)
        if (recent.size > MAX_RECENT) {
            recent.removeAt(recent.lastIndex)
        }
        saveList(KEY_RECENT, recent)
    }

    fun togglePin(text: String) {
        val pinned = getPinnedClips().toMutableList()
        val recent = getRecentClips().toMutableList()

        if (pinned.contains(text)) {
            pinned.remove(text)
            if (!recent.contains(text)) {
                recent.add(0, text)
            }
        } else {
            pinned.add(0, text)
            recent.remove(text)
        }

        saveList(KEY_PINNED, pinned)
        saveList(KEY_RECENT, recent)
    }

    fun deleteClip(text: String) {
        val pinned = getPinnedClips().toMutableList()
        val recent = getRecentClips().toMutableList()

        pinned.remove(text)
        recent.remove(text)

        saveList(KEY_PINNED, pinned)
        saveList(KEY_RECENT, recent)
    }

    fun clearUnpinned() {
        saveList(KEY_RECENT, emptyList())
    }

    fun isPinned(text: String): Boolean {
        return getPinnedClips().contains(text)
    }

    private fun loadList(key: String): List<String> {
        val jsonStr = prefs.getString(key, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveList(key: String, list: List<String>) {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        prefs.edit().putString(key, jsonArray.toString()).apply()
    }
}
