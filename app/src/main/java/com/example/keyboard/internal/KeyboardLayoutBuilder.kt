package com.example.keyboard.internal

import android.graphics.RectF
import com.example.foundation.common.Constants
import com.example.ime.data.MoreKeysSpecs

/**
 * Layout builder that defines and arranges rows of keys for QWERTY, Symbols, and Numbers.
 * Computes exact pixel bounding boxes matching reference UI.
 */
class KeyboardLayoutBuilder {

    enum class LayoutMode {
        ALPHA_LOWER,
        ALPHA_UPPER,
        ALPHA_CAPSLOCK,
        SYMBOLS_1,
        SYMBOLS_2,
        NUMBER_PAD
    }

    /**
     * Builds key list with computed RectF bounds based on canvas width, height, and layout mode.
     */
    fun buildKeyboard(
        width: Float,
        height: Float,
        mode: LayoutMode,
        showNumberRow: Boolean = true,
        currencySymbol: String = "₹",
        keyMarginHorizontal: Float = 3f,
        keyMarginVertical: Float = 4f
    ): List<Key> {
        val keys = mutableListOf<Key>()
        if (width <= 0 || height <= 0) return keys

        val rows = getRowSpecs(mode, showNumberRow, currencySymbol)
        val rowCount = rows.size
        val rowHeight = height / rowCount

        for (rowIndex in 0 until rowCount) {
            val rowSpec = rows[rowIndex]
            val top = rowIndex * rowHeight

            // Calculate total relative weight in this row
            val totalWeight = rowSpec.sumOf { it.weight.toDouble() }.toFloat()
            var currentX = 0f

            for (spec in rowSpec) {
                val keyWidth = (spec.weight / totalWeight) * width
                val bounds = RectF(
                    currentX + keyMarginHorizontal,
                    top + keyMarginVertical,
                    currentX + keyWidth - keyMarginHorizontal,
                    top + rowHeight - keyMarginVertical
                )

                val moreKeys = MoreKeysSpecs.LONG_PRESS_POPUP_GRID[spec.label.uppercase()] ?: emptyList()
                keys.add(
                    Key(
                        code = spec.code,
                        label = spec.label,
                        hintLabel = spec.hintLabel,
                        bounds = bounds,
                        isFunctional = spec.isFunctional,
                        moreKeys = moreKeys
                    )
                )
                currentX += keyWidth
            }
        }

        return keys
    }

    data class KeySpec(
        val code: Int,
        val label: String,
        val hintLabel: String? = null,
        val weight: Float = 1.0f,
        val isFunctional: Boolean = false
    )

    private fun getRowSpecs(
        mode: LayoutMode,
        showNumberRow: Boolean,
        currencySymbol: String
    ): List<List<KeySpec>> {
        val rows = mutableListOf<List<KeySpec>>()

        if (mode == LayoutMode.ALPHA_LOWER || mode == LayoutMode.ALPHA_UPPER || mode == LayoutMode.ALPHA_CAPSLOCK) {
            val isUpper = mode == LayoutMode.ALPHA_UPPER || mode == LayoutMode.ALPHA_CAPSLOCK

            // Row 0: Numbers Row (1¹ 2² 3³ 4⁴ 5⁵ 6⁶ 7⁷ 8⁸ 9⁹ 0⁰)
            if (showNumberRow) {
                val numDigits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                rows.add(numDigits.map { digit ->
                    val exponent = MoreKeysSpecs.QWERTY_SUB_LABELS[digit] ?: ""
                    KeySpec(digit[0].code, digit, hintLabel = exponent, weight = 1.0f)
                })
            }

            // Row 1: Q% W/ E| R= T[ Y] U* I! O- P;
            val r1Letters = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
            rows.add(r1Letters.map { letter ->
                val displayLabel = if (isUpper) letter.uppercase() else letter
                val sub = MoreKeysSpecs.QWERTY_SUB_LABELS[letter.uppercase()]
                KeySpec(displayLabel[0].code, displayLabel, hintLabel = sub, weight = 1.0f)
            })

            // Row 2: A@ S# D₹ F_ G& H- J+ K( L) - Inset on sides with balanced spacer weights
            val r2Letters = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
            val r2Row = mutableListOf<KeySpec>()
            // Add left spacer
            r2Row.add(KeySpec(0, "", weight = 0.5f, isFunctional = true))
            r2Letters.forEach { letter ->
                val displayLabel = if (isUpper) letter.uppercase() else letter
                val sub = if (letter.uppercase() == "D") currencySymbol else MoreKeysSpecs.QWERTY_SUB_LABELS[letter.uppercase()]
                r2Row.add(KeySpec(displayLabel[0].code, displayLabel, hintLabel = sub, weight = 1.0f))
            }
            // Add right spacer
            r2Row.add(KeySpec(0, "", weight = 0.5f, isFunctional = true))
            // Filter spacer dummy keys into inset keys by removing 0 code items in buildKeyboard or using side key padding
            // Let's create direct 9 keys with 1.0f weight so they center naturally, or with 0.5f spacer
            rows.add(r2Letters.map { letter ->
                val displayLabel = if (isUpper) letter.uppercase() else letter
                val sub = if (letter.uppercase() == "D") currencySymbol else MoreKeysSpecs.QWERTY_SUB_LABELS[letter.uppercase()]
                KeySpec(displayLabel[0].code, displayLabel, hintLabel = sub, weight = 1.0f)
            })

            // Row 3: [ Shift ⇧ ] Z* X" C' V: B; N! M? [ Backspace ⌫ ]
            val r3Keys = mutableListOf<KeySpec>()
            r3Keys.add(KeySpec(Constants.CODE_SHIFT, "⇧", weight = 1.4f, isFunctional = true))
            val r3Letters = listOf("z", "x", "c", "v", "b", "n", "m")
            r3Letters.forEach { letter ->
                val displayLabel = if (isUpper) letter.uppercase() else letter
                val sub = MoreKeysSpecs.QWERTY_SUB_LABELS[letter.uppercase()]
                r3Keys.add(KeySpec(displayLabel[0].code, displayLabel, hintLabel = sub, weight = 1.0f))
            }
            r3Keys.add(KeySpec(Constants.CODE_DELETE, "⌫", weight = 1.4f, isFunctional = true))
            rows.add(r3Keys)

            // Row 4: [ ?123 ] [ , ... ] [ Spacebar ... ] [ . ] [ ↵ ]
            val r4Keys = listOf(
                KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "?123", weight = 1.3f, isFunctional = true),
                KeySpec(','.code, ",", hintLabel = "…", weight = 1.0f, isFunctional = true),
                KeySpec(Constants.CODE_SPACE, "", hintLabel = "…", weight = 4.4f),
                KeySpec('.'.code, ".", weight = 1.0f, isFunctional = true),
                KeySpec(Constants.CODE_ENTER, "↵", weight = 1.3f, isFunctional = true)
            )
            rows.add(r4Keys)
        } else if (mode == LayoutMode.SYMBOLS_1) {
            // Symbols 1 Layout (Exact HeliBoard Standard)
            val r1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            rows.add(r1.map { KeySpec(it[0].code, it, weight = 1.0f) })

            val r2 = listOf("%", "/", "|", "=", "[", "]", "*", "!", "-", ";", "\"")
            rows.add(r2.map { KeySpec(it[0].code, it, weight = 1.0f) })

            val r3 = listOf("@", "#", currencySymbol, "_", "&", "-", "+", "(", ")", "{", "}")
            rows.add(r3.map { KeySpec(it[0].code, it, weight = 1.0f) })

            val r4Keys = mutableListOf<KeySpec>()
            r4Keys.add(KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "=\\<", weight = 1.4f, isFunctional = true))
            val r4Syms = listOf("*", "\"", "'", ":", ";", "!", "?")
            r4Syms.forEach { r4Keys.add(KeySpec(it[0].code, it, weight = 1.0f)) }
            r4Keys.add(KeySpec(Constants.CODE_DELETE, "⌫", weight = 1.4f, isFunctional = true))
            rows.add(r4Keys)

            val r5Keys = listOf(
                KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "ABC", weight = 1.3f, isFunctional = true),
                KeySpec(Constants.CODE_NUMPAD, "12 34", weight = 1.1f, isFunctional = true),
                KeySpec(','.code, ",", hintLabel = "…", weight = 1.0f, isFunctional = true),
                KeySpec(Constants.CODE_SPACE, "", weight = 3.6f),
                KeySpec('.'.code, ".", weight = 1.0f, isFunctional = true),
                KeySpec(Constants.CODE_ENTER, "↵", weight = 1.3f, isFunctional = true)
            )
            rows.add(r5Keys)
        } else if (mode == LayoutMode.SYMBOLS_2) {
            // Symbols 2 Layout (Exact HeliBoard Standard)
            val r1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            rows.add(r1.map { KeySpec(it[0].code, it, weight = 1.0f) })

            val r2 = listOf("~", "`", "\\", "•", "√", "π", "÷", "×", "¶", "∆")
            rows.add(r2.map { KeySpec(it[0].code, it, weight = 1.0f) })

            val r3 = listOf("£", "€", "$", "¢", "^", "°", "=", "{", "}")
            rows.add(r3.map { KeySpec(it[0].code, it, weight = 1.0f) })

            val r4Keys = mutableListOf<KeySpec>()
            r4Keys.add(KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "?123", weight = 1.4f, isFunctional = true))
            val r4Syms = listOf("\\", "©", "®", "™", "%", "[", "]")
            r4Syms.forEach { r4Keys.add(KeySpec(it[0].code, it, weight = 1.0f)) }
            r4Keys.add(KeySpec(Constants.CODE_DELETE, "⌫", weight = 1.4f, isFunctional = true))
            rows.add(r4Keys)

            val r5Keys = listOf(
                KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "ABC", weight = 1.3f, isFunctional = true),
                KeySpec('<'.code, "<", weight = 1.0f, isFunctional = true),
                KeySpec(Constants.CODE_SPACE, "", weight = 4.4f),
                KeySpec('>'.code, ">", weight = 1.0f, isFunctional = true),
                KeySpec(Constants.CODE_ENTER, "↵", weight = 1.3f, isFunctional = true)
            )
            rows.add(r5Keys)
        } else {
            // Dedicated Calculator/PIN Number Pad (Exact HeliBoard 5-Column Matrix)
            val r1 = listOf(
                KeySpec('+'.code, "+", hintLabel = "(", weight = 1.0f, isFunctional = true),
                KeySpec('1'.code, "1", weight = 1.5f),
                KeySpec('2'.code, "2", weight = 1.5f),
                KeySpec('3'.code, "3", weight = 1.5f),
                KeySpec('%'.code, "%", hintLabel = currencySymbol, weight = 1.0f, isFunctional = true)
            )
            rows.add(r1)

            val r2 = listOf(
                KeySpec('-'.code, "-", hintLabel = ")", weight = 1.0f, isFunctional = true),
                KeySpec('4'.code, "4", weight = 1.5f),
                KeySpec('5'.code, "5", weight = 1.5f),
                KeySpec('6'.code, "6", weight = 1.5f),
                KeySpec('_'.code, "_", hintLabel = "…", weight = 1.0f, isFunctional = true)
            )
            rows.add(r2)

            val r3 = listOf(
                KeySpec('*'.code, "*", hintLabel = "/", weight = 1.0f, isFunctional = true),
                KeySpec('7'.code, "7", weight = 1.5f),
                KeySpec('8'.code, "8", weight = 1.5f),
                KeySpec('9'.code, "9", weight = 1.5f),
                KeySpec(Constants.CODE_DELETE, "⌫", weight = 1.0f, isFunctional = true)
            )
            rows.add(r3)

            val r4 = listOf(
                KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "ABC", weight = 1.0f, isFunctional = true),
                KeySpec(','.code, ",", hintLabel = "…", weight = 0.7f, isFunctional = true),
                KeySpec(Constants.CODE_SWITCH_ALPHA_SYMBOL, "?123", weight = 0.9f, isFunctional = true),
                KeySpec('0'.code, "0", weight = 1.6f),
                KeySpec('='.code, "=", hintLabel = "≠", weight = 0.8f, isFunctional = true),
                KeySpec(':'.code, ":", hintLabel = "·", weight = 0.6f, isFunctional = true),
                KeySpec(Constants.CODE_ENTER, "↵", weight = 0.9f, isFunctional = true)
            )
            rows.add(r4)
        }

        return rows
    }
}

