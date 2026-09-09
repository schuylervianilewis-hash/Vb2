package com.example.ime.quicknotes

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

/**
 * Lightweight, zero-idle persistence store for user Quick Notes.
 * Completely distinct from ClipboardStorage and Android ClipboardManager.
 * Zero background services, zero listeners.
 */
class QuickNotesStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "vian_quick_notes_store"
        private const val KEY_PINNED = "pinned_notes"
        private const val KEY_NOTES = "saved_notes"

        // Settings key for clipboard transfer behavior (default: copy)
        const val PREFS_SETTINGS = "vian_notes_settings"
        const val KEY_MOVE_MODE = "pref_clipboard_move_mode" // "copy" or "cut"
        const val DEFAULT_MOVE_MODE = "copy"
    }

    fun getPinnedNotes(): List<String> = loadList(KEY_PINNED)

    fun getSavedNotes(): List<String> = loadList(KEY_NOTES)

    fun addNote(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val pinned = getPinnedNotes()
        if (pinned.contains(trimmed)) return

        val notes = getSavedNotes().toMutableList()
        notes.remove(trimmed)
        notes.add(0, trimmed)
        saveList(KEY_NOTES, notes)
    }

    fun updateNote(oldText: String, newText: String) {
        val trimmedNew = newText.trim()
        if (trimmedNew.isEmpty()) {
            deleteNote(oldText)
            return
        }

        val pinned = getPinnedNotes().toMutableList()
        val notes = getSavedNotes().toMutableList()

        if (pinned.contains(oldText)) {
            val idx = pinned.indexOf(oldText)
            pinned[idx] = trimmedNew
            saveList(KEY_PINNED, pinned)
        } else if (notes.contains(oldText)) {
            val idx = notes.indexOf(oldText)
            notes[idx] = trimmedNew
            saveList(KEY_NOTES, notes)
        } else {
            notes.add(0, trimmedNew)
            saveList(KEY_NOTES, notes)
        }
    }

    fun togglePin(text: String) {
        val pinned = getPinnedNotes().toMutableList()
        val notes = getSavedNotes().toMutableList()

        if (pinned.contains(text)) {
            pinned.remove(text)
            if (!notes.contains(text)) {
                notes.add(0, text)
            }
        } else {
            pinned.add(0, text)
            notes.remove(text)
        }

        saveList(KEY_PINNED, pinned)
        saveList(KEY_NOTES, notes)
    }

    fun deleteNote(text: String) {
        val pinned = getPinnedNotes().toMutableList()
        val notes = getSavedNotes().toMutableList()

        pinned.remove(text)
        notes.remove(text)

        saveList(KEY_PINNED, pinned)
        saveList(KEY_NOTES, notes)
    }

    fun isPinned(text: String): Boolean {
        return getPinnedNotes().contains(text)
    }

    fun isMoveModeCut(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        return sp.getString(KEY_MOVE_MODE, DEFAULT_MOVE_MODE) == "cut"
    }

    fun setMoveMode(context: Context, mode: String) {
        val sp = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_MOVE_MODE, mode).apply()
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
