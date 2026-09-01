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

    fun buildLayout(width: Float, height: Float, theme: KeyboardTheme, density: Float) {
        keys.clear()
        if (width <= 0 || height <= 0) return

        val horizontalGapPx = theme.horizontalGapDp * density
        val verticalGapPx = theme.verticalGapDp * density
        val paddingHorizontalPx = 6f * density
        val paddingVerticalPx = 6f * density

        val availableWidth = width - (paddingHorizontalPx * 2)
        val availableHeight = height - (paddingVerticalPx * 2)

        val rowDefinitions = getRowDefinitions(mode, shiftState)
        val rowCount = rowDefinitions.size
        val totalVerticalGaps = (rowCount - 1) * verticalGapPx
        val keyHeight = (availableHeight - totalVerticalGaps) / rowCount

        var currentY = paddingVerticalPx

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

    private fun getRowDefinitions(mode: KeyboardMode, shiftState: ShiftState): List<List<KeyData>> {
        return when (mode) {
            KeyboardMode.CHARACTERS -> getQwertyRows(shiftState)
            KeyboardMode.SYMBOLS_1 -> getSymbols1Rows()
            KeyboardMode.SYMBOLS_2 -> getSymbols2Rows()
        }
    }

    private fun getQwertyRows(shiftState: ShiftState): List<List<KeyData>> {
        val isCaps = shiftState != ShiftState.OFF
        val r1Chars = if (isCaps) listOf("Q","W","E","R","T","Y","U","I","O","P") else listOf("q","w","e","r","t","y","u","i","o","p")
        val r1Hints = listOf("1","2","3","4","5","6","7","8","9","0")
        val r2Chars = if (isCaps) listOf("A","S","D","F","G","H","J","K","L") else listOf("a","s","d","f","g","h","j","k","l")
        val r2Hints = listOf("@","#","$","%","&","*","-","+","=")
        val r3Chars = if (isCaps) listOf("Z","X","C","V","B","N","M") else listOf("z","x","c","v","b","n","m")
        val r3Hints = listOf("!","\"","'",":",";","/", "?")

        val row1 = r1Chars.mapIndexed { idx, ch ->
            KeyData(code = ch[0].code, label = ch, hintLabel = r1Hints.getOrNull(idx), type = KeyType.CHARACTER, weight = 1.0f)
        }

        val row2 = r2Chars.mapIndexed { idx, ch ->
            KeyData(code = ch[0].code, label = ch, hintLabel = r2Hints.getOrNull(idx), type = KeyType.CHARACTER, weight = 1.0f)
        }

        val shiftLabel = when (shiftState) {
            ShiftState.CAPS_LOCK -> "⇪"
            ShiftState.ON -> "▲"
            ShiftState.OFF -> "⇧"
        }

        val row3 = mutableListOf<KeyData>()
        row3.add(KeyData(code = -1, label = shiftLabel, type = KeyType.SHIFT, weight = 1.5f))
        for (i in r3Chars.indices) {
            row3.add(KeyData(code = r3Chars[i][0].code, label = r3Chars[i], hintLabel = r3Hints.getOrNull(i), type = KeyType.CHARACTER, weight = 1.0f))
        }
        row3.add(KeyData(code = -2, label = "⌫", type = KeyType.DELETE, weight = 1.5f))

        val row4 = listOf(
            KeyData(code = -3, label = "?123", type = KeyType.SYMBOLS_TOGGLE, weight = 1.5f),
            KeyData(code = ','.code, label = ",", hintLabel = "⚙", type = KeyType.COMMA, weight = 1.0f),
            KeyData(code = 32, label = "Vian Board", type = KeyType.SPACE, weight = 4.5f),
            KeyData(code = '.'.code, label = ".", hintLabel = "…", type = KeyType.PERIOD, weight = 1.0f),
            KeyData(code = -4, label = "↵", type = KeyType.ENTER, weight = 1.5f)
        )

        return listOf(row1, row2, row3, row4)
    }

    private fun getSymbols1Rows(): List<List<KeyData>> {
        val r1 = listOf("1","2","3","4","5","6","7","8","9","0").map {
            KeyData(code = it[0].code, label = it, type = KeyType.CHARACTER, weight = 1.0f)
        }
        val r2 = listOf("@","#","$","%","&","*","-","+","(",")").map {
            KeyData(code = it[0].code, label = it, type = KeyType.CHARACTER, weight = 1.0f)
        }
        val row3 = mutableListOf<KeyData>()
        row3.add(KeyData(code = -5, label = "=\\<", type = KeyType.SYMBOLS_MORE_TOGGLE, weight = 1.5f))
        val r3 = listOf("!","\"","'",":",";","/","?")
        for (ch in r3) {
            row3.add(KeyData(code = ch[0].code, label = ch, type = KeyType.CHARACTER, weight = 1.0f))
        }
        row3.add(KeyData(code = -2, label = "⌫", type = KeyType.DELETE, weight = 1.5f))

        val row4 = listOf(
            KeyData(code = -3, label = "ABC", type = KeyType.SYMBOLS_TOGGLE, weight = 1.5f),
            KeyData(code = ','.code, label = ",", type = KeyType.COMMA, weight = 1.0f),
            KeyData(code = 32, label = "Space", type = KeyType.SPACE, weight = 4.5f),
            KeyData(code = '.'.code, label = ".", type = KeyType.PERIOD, weight = 1.0f),
            KeyData(code = -4, label = "↵", type = KeyType.ENTER, weight = 1.5f)
        )

        return listOf(r1, r2, row3, row4)
    }

    private fun getSymbols2Rows(): List<List<KeyData>> {
        val r1 = listOf("~","`","|","•","√","π","÷","×","§","Δ").map {
            KeyData(code = it[0].code, label = it, type = KeyType.CHARACTER, weight = 1.0f)
        }
        val r2 = listOf("£","€","¥","¢","^","°","=","{","}","\\").map {
            KeyData(code = it[0].code, label = it, type = KeyType.CHARACTER, weight = 1.0f)
        }
        val row3 = mutableListOf<KeyData>()
        row3.add(KeyData(code = -6, label = "?123", type = KeyType.SYMBOLS_MORE_TOGGLE, weight = 1.5f))
        val r3 = listOf("%","©","®","™","✓","[","]")
        for (ch in r3) {
            row3.add(KeyData(code = ch[0].code, label = ch, type = KeyType.CHARACTER, weight = 1.0f))
        }
        row3.add(KeyData(code = -2, label = "⌫", type = KeyType.DELETE, weight = 1.5f))

        val row4 = listOf(
            KeyData(code = -3, label = "ABC", type = KeyType.SYMBOLS_TOGGLE, weight = 1.5f),
            KeyData(code = '<'.code, label = "<", type = KeyType.CHARACTER, weight = 1.0f),
            KeyData(code = 32, label = "Space", type = KeyType.SPACE, weight = 4.5f),
            KeyData(code = '>'.code, label = ">", type = KeyType.CHARACTER, weight = 1.0f),
            KeyData(code = -4, label = "↵", type = KeyType.ENTER, weight = 1.5f)
        )

        return listOf(r1, r2, row3, row4)
    }

    fun findKeyAt(x: Float, y: Float): KeyData? {
        return keys.firstOrNull { it.bounds.contains(x, y) }
    }
}
