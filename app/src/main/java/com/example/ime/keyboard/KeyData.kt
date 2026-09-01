package com.example.ime.keyboard

import android.graphics.RectF

enum class KeyType {
    CHARACTER,
    SHIFT,
    DELETE,
    SYMBOLS_TOGGLE,
    SYMBOLS_MORE_TOGGLE,
    SPACE,
    ENTER,
    COMMA,
    PERIOD,
    VOICE_TOGGLE,
    ACTION
}

data class KeyData(
    val code: Int,
    var label: String,
    val hintLabel: String? = null,
    val type: KeyType = KeyType.CHARACTER,
    val weight: Float = 1.0f,
    val bounds: RectF = RectF(),
    var isPressed: Boolean = false
)
