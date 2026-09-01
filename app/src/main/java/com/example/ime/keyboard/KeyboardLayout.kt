package com.example.ime.keyboard

import android.graphics.RectF

enum class KeyboardMode {
    CHARACTERS,
    SYMBOLS_1,
    SYMBOLS_2
}

enum class ShiftState {
    OFF,
    ON,
    CAPS_LOCK
}

class KeyboardLayout {

    var mode: KeyboardMode = KeyboardMode.CHARACTERS
    var shiftState: ShiftState = ShiftState.OFF

    val keys = mutableListOf<KeyData>()
    val toolbarKeys = mutableListOf<KeyData>()

    var suggestions: List<String> = listOf("The", "and", "to")

    fun buildLayout(width: Float, height: Float, theme: KeyboardTheme, density: Float, bottomInsetPx: Float = 0f) {
        keys.clear()
        toolbarKeys.clear()
        if (width <= 0 || height <= 0) return

        val horizontalGapPx = theme.horizontalGapDp * density
        val verticalGapPx = theme.verticalGapDp * density
        val paddingHorizontalPx = 4f * density
        val paddingVerticalPx = 4f * density

        val availableWidth = width - (paddingHorizontalPx * 2)

        // 1. Build Top Toolbar
        val toolbarHeightPx = theme.toolbarHeightDp * density
        buildToolbar(paddingHorizontalPx, paddingVerticalPx, availableWidth, toolbarHeightPx, density)

        // 2. Build Keyboard Grid
        val keyboardStartY = paddingVerticalPx + toolbarHeightPx + (2f * density)
        val availableKeyboardHeight = height - keyboardStartY - paddingVerticalPx - bottomInsetPx

        val rowDefinitions = getRowDefinitions(mode, shiftState)
        val rowCount = rowDefinitions.size
        val totalVerticalGaps = (rowCount - 1) * verticalGapPx
        val keyHeight = (availableKeyboardHeight - totalVerticalGaps) / rowCount

        var currentY = keyboardStartY

        for (row in rowDefinitions) {
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val totalGaps = (row.size - 1) * horizontalGapPx
            val widthForKeys = availableWidth - totalGaps

            var currentX = paddingHorizontalPx

            for (key in row) {
                val keyWidth = (key.weight / totalWeight) * widthForKeys
                key.bounds.set(
                    currentX,
                    currentY,
                    currentX + keyWidth,
                    currentY + keyHeight
                )
                keys.add(key)
                currentX += keyWidth + horizontalGapPx
            }

            currentY += keyHeight + verticalGapPx
        }
    }

    private fun buildToolbar(startX: Float, startY: Float, totalWidth: Float, height: Float, density: Float) {
        // Toolbar: [ > ] [ Suggestion 1 ] [ Suggestion 2 ] [ Suggestion 3 ] [ ⬚ ] [ 📋 ]
        val expandBtnWidth = 36f * density
        val toolBtnWidth = 36f * density
        val spacing = 4f * density

        var currentX = startX

        // 1. Expand action button
        val expandKey = KeyData(
            code = -101,
            label = "›",
            type = KeyType.ACTION_EXPAND,
            weight = 1f,
            bounds = RectF(currentX, startY, currentX + expandBtnWidth, startY + height)
        )
        toolbarKeys.add(expandKey)
        currentX += expandBtnWidth + spacing

        // 2. Trailing utility icons
        val rightIconsTotalWidth = (toolBtnWidth * 2) + spacing
        val suggestionAreaWidth = totalWidth - expandBtnWidth - spacing - rightIconsTotalWidth - spacing
        val eachSugWidth = (suggestionAreaWidth - (spacing * (suggestions.size - 1).coerceAtLeast(0))) / suggestions.size.coerceAtLeast(1)

        for (sug in suggestions) {
            val sugKey = KeyData(
                code = -200,
                label = sug,
                type = KeyType.SUGGESTION,
                weight = 1f,
                bounds = RectF(currentX, startY, currentX + eachSugWidth, startY + height)
            )
            toolbarKeys.add(sugKey)
            currentX += eachSugWidth + spacing
        }

        // 3. Selection action
        val selectionKey = KeyData(
            code = -102,
            label = "⬚",
            type = KeyType.ACTION_SELECTION,
            weight = 1f,
            bounds = RectF(currentX, startY, currentX + toolBtnWidth, startY + height)
        )
        toolbarKeys.add(selectionKey)
        currentX += toolBtnWidth + spacing

        // 4. Clipboard action
        val clipboardKey = KeyData(
            code = -103,
            label = "📋",
            type = KeyType.ACTION_CLIPBOARD,
            weight = 1f,
            bounds = RectF(currentX, startY, currentX + toolBtnWidth, startY + height)
        )
        toolbarKeys.add(clipboardKey)
    }

    private fun getRowDefinitions(mode: KeyboardMode, shiftState: ShiftState): List<List<KeyData>> {
        return when (mode) {
            KeyboardMode.CHARACTERS -> getQwerty5Rows(shiftState)
            KeyboardMode.SYMBOLS_1 -> getSymbols1Rows()
            KeyboardMode.SYMBOLS_2 -> getSymbols2Rows()
        }
    }

    private fun getQwerty5Rows(shiftState: ShiftState): List<List<KeyData>> {
        val isCaps = shiftState != ShiftState.OFF

        // Row 0: Numbers with superscripts & fractions
        val numChars = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val numHints = listOf("¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹", "⁰")
        val numMore = listOf(
            listOf("1", "¹", "½", "⅓", "¼", "⅛"),
            listOf("2", "²", "⅔", "⅖"),
            listOf("3", "³", "¾", "⅜"),
            listOf("4", "⁴", "⅘"),
            listOf("5", "⁵", "⅝"),
            listOf("6", "⁶"),
            listOf("7", "⁷", "⅞"),
            listOf("8", "⁸"),
            listOf("9", "⁹"),
            listOf("0", "⁰", "ⁿ", "∅")
        )

        val row0 = numChars.mapIndexed { idx, ch ->
            KeyData(
                code = ch[0].code,
                label = ch,
                hintLabel = numHints[idx],
                moreKeys = numMore[idx],
                type = KeyType.CHARACTER,
                weight = 1.0f
            )
        }

        // Row 1: Q-P with corner hint symbols
        val r1Chars = if (isCaps) listOf("Q","W","E","R","T","Y","U","I","O","P") else listOf("q","w","e","r","t","y","u","i","o","p")
        val r1Hints = listOf("%", "\\", "|", "=", "[", "]", "-", "!", "_", ";")
        val r1More = if (isCaps) {
            listOf(
                listOf("Q"),
                listOf("W"),
                listOf("E", "É", "È", "Ê", "Ë", "Ē", "Ę"),
                listOf("R"),
                listOf("T", "Þ"),
                listOf("Y", "Ý", "Ÿ"),
                listOf("U", "Ú", "Ù", "Û", "Ü", "Ū", "Ů"),
                listOf("I", "Í", "Ì", "Î", "Ï", "Ī", "Į"),
                listOf("O", "Ó", "Ò", "Ô", "Ö", "Õ", "Ō", "Œ", "Ø"),
                listOf("P")
            )
        } else {
            listOf(
                listOf("q"),
                listOf("w"),
                listOf("e", "é", "è", "ê", "ë", "ē", "ę"),
                listOf("r"),
                listOf("t", "þ"),
                listOf("y", "ý", "ÿ"),
                listOf("u", "ú", "ù", "û", "ü", "ū", "ů"),
                listOf("i", "í", "ì", "î", "ï", "ī", "į"),
                listOf("o", "ó", "ò", "ô", "ö", "õ", "ō", "œ", "ø"),
                listOf("p")
            )
        }

        val row1 = r1Chars.mapIndexed { idx, ch ->
            KeyData(
                code = ch[0].code,
                label = ch,
                hintLabel = r1Hints[idx],
                moreKeys = r1More[idx],
                type = KeyType.CHARACTER,
                weight = 1.0f
            )
        }

        // Row 2: A-L with corner hint symbols
        val r2Chars = if (isCaps) listOf("A","S","D","F","G","H","J","K","L") else listOf("a","s","d","f","g","h","j","k","l")
        val r2Hints = listOf("@", "#", "₹", "_", "&", "-", "+", "(", ")")
        val r2More = if (isCaps) {
            listOf(
                listOf("A", "Á", "À", "Â", "Ä", "Ã", "Å", "Ā", "Æ"),
                listOf("S", "ß", "Ś", "Š", "$"),
                listOf("D", "Ð", "Đ"),
                listOf("F"),
                listOf("G"),
                listOf("H"),
                listOf("J"),
                listOf("K"),
                listOf("L", "Ł")
            )
        } else {
            listOf(
                listOf("a", "á", "à", "â", "ä", "ã", "å", "ā", "æ"),
                listOf("s", "ß", "ś", "š", "$"),
                listOf("d", "ð", "đ"),
                listOf("f"),
                listOf("g"),
                listOf("h"),
                listOf("j"),
                listOf("k"),
                listOf("l", "ł")
            )
        }

        val row2 = r2Chars.mapIndexed { idx, ch ->
            KeyData(
                code = ch[0].code,
                label = ch,
                hintLabel = r2Hints[idx],
                moreKeys = r2More[idx],
                type = KeyType.CHARACTER,
                weight = 1.0f
            )
        }

        // Row 3: Shift, Z-M, Delete
        val shiftLabel = when (shiftState) {
            ShiftState.CAPS_LOCK -> "⇪"
            ShiftState.ON -> "▲"
            ShiftState.OFF -> "⇧"
        }

        val r3Chars = if (isCaps) listOf("Z","X","C","V","B","N","M") else listOf("z","x","c","v","b","n","m")
        val r3Hints = listOf("*", "\"", "'", ":", ";", "!", "?")
        val r3More = if (isCaps) {
            listOf(
                listOf("Z", "Ź", "Ż", "Ž"),
                listOf("X"),
                listOf("C", "Ç", "Ć", "Č"),
                listOf("V"),
                listOf("B"),
                listOf("N", "Ñ", "Ń"),
                listOf("M")
            )
        } else {
            listOf(
                listOf("z", "ź", "ż", "ž"),
                listOf("x"),
                listOf("c", "ç", "ć", "č"),
                listOf("v"),
                listOf("b"),
                listOf("n", "ñ", "ń"),
                listOf("m")
            )
        }

        val row3 = mutableListOf<KeyData>()
        row3.add(KeyData(code = -1, label = shiftLabel, type = KeyType.SHIFT, weight = 1.5f))
        for (i in r3Chars.indices) {
            row3.add(
                KeyData(
                    code = r3Chars[i][0].code,
                    label = r3Chars[i],
                    hintLabel = r3Hints[i],
                    moreKeys = r3More[i],
                    type = KeyType.CHARACTER,
                    weight = 1.0f
                )
            )
        }
        row3.add(KeyData(code = -2, label = "⌫", type = KeyType.DELETE, weight = 1.5f))

        // Row 4: ?123, Comma, Space, Period, Enter
        val row4 = listOf(
            KeyData(code = -3, label = "?123", type = KeyType.SYMBOLS_TOGGLE, weight = 1.4f),
            KeyData(
                code = ','.code,
                label = ",",
                hintLabel = "⚙",
                moreKeys = listOf(",", ";", ":", "⚙"),
                type = KeyType.COMMA,
                weight = 1.0f
            ),
            KeyData(code = 32, label = "", type = KeyType.SPACE, weight = 4.8f),
            KeyData(
                code = '.'.code,
                label = ".",
                hintLabel = "…",
                moreKeys = listOf(".", "…", "!", "?", "-", "_"),
                type = KeyType.PERIOD,
                weight = 1.0f
            ),
            KeyData(code = -4, label = "↵", type = KeyType.ENTER, weight = 1.6f)
        )

        return listOf(row0, row1, row2, row3, row4)
    }

    private fun getSymbols1Rows(): List<List<KeyData>> {
        val r0 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map {
            KeyData(code = it[0].code, label = it, type = KeyType.CHARACTER, weight = 1.0f)
        }
        val r1 = listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/").map {
            val more = if (it == "$") listOf("$", "€", "£", "¥", "₹", "¢") else listOf(it)
            KeyData(code = it[0].code, label = it, moreKeys = more, type = KeyType.CHARACTER, weight = 1.0f)
        }
        val r2 = listOf("*", "\"", "'", ":", ";", "!", "?", "%", "~", "`").map {
            KeyData(code = it[0].code, label = it, type = KeyType.CHARACTER, weight = 1.0f)
        }

        val row3 = mutableListOf<KeyData>()
        row3.add(KeyData(code = -5, label = "=\\<", type = KeyType.SYMBOLS_MORE_TOGGLE, weight = 1.5f))
        val r3 = listOf("\\", "|", "<", ">", "{", "}", "[", "]")
        for (ch in r3) {
            row3.add(KeyData(code = ch[0].code, label = ch, type = KeyType.CHARACTER, weight = 1.0f))
        }
        row3.add(KeyData(code = -2, label = "⌫", type = KeyType.DELETE, weight = 1.5f))

        val row4 = listOf(
            KeyData(code = -3, label = "ABC", type = KeyType.SYMBOLS_TOGGLE, weight = 1.4f),
            KeyData(code = ','.code, label = ",", type = KeyType.COMMA, weight = 1.0f),
            KeyData(code = 32, label = "", type = KeyType.SPACE, weight = 4.8f),
            KeyData(code = '.'.code, label = ".", type = KeyType.PERIOD, weight = 1.0f),
            KeyData(code = -4, label = "↵", type = KeyType.ENTER, weight = 1.6f)
        )

        return listOf(r0, r1, r2, row3, row4)
    }

    private fun getSymbols2Rows(): List<List<KeyData>> {
        val r0 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "§", "Δ").map {
            KeyData(code = it[0].code, label = it, type = KeyType.CHARACTER, weight = 1.0f)
        }
        val r1 = listOf("£", "€", "¥", "¢", "₹", "^", "°", "=", "{", "}").map {
            KeyData(code = it[0].code, label = it, type = KeyType.CHARACTER, weight = 1.0f)
        }
        val r2 = listOf("\\", "<", ">", "©", "®", "™", "✓", "[", "]", "¿").map {
            KeyData(code = it[0].code, label = it, type = KeyType.CHARACTER, weight = 1.0f)
        }

        val row3 = mutableListOf<KeyData>()
        row3.add(KeyData(code = -6, label = "?123", type = KeyType.SYMBOLS_MORE_TOGGLE, weight = 1.5f))
        val r3 = listOf("«", "»", "¡", "§", "±", "≠", "≤", "≥")
        for (ch in r3) {
            row3.add(KeyData(code = ch[0].code, label = ch, type = KeyType.CHARACTER, weight = 1.0f))
        }
        row3.add(KeyData(code = -2, label = "⌫", type = KeyType.DELETE, weight = 1.5f))

        val row4 = listOf(
            KeyData(code = -3, label = "ABC", type = KeyType.SYMBOLS_TOGGLE, weight = 1.4f),
            KeyData(code = '<'.code, label = "<", type = KeyType.CHARACTER, weight = 1.0f),
            KeyData(code = 32, label = "", type = KeyType.SPACE, weight = 4.8f),
            KeyData(code = '>'.code, label = ">", type = KeyType.CHARACTER, weight = 1.0f),
            KeyData(code = -4, label = "↵", type = KeyType.ENTER, weight = 1.6f)
        )

        return listOf(r0, r1, r2, row3, row4)
    }

    fun findKeyAt(x: Float, y: Float): KeyData? {
        return keys.firstOrNull { it.bounds.contains(x, y) }
            ?: toolbarKeys.firstOrNull { it.bounds.contains(x, y) }
    }
}
