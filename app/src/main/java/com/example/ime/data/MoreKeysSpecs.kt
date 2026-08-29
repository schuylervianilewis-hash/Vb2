package com.example.ime.data

object MoreKeysSpecs {
    // Map of base keys to their secondary hint sub-label (top right)
    val QWERTY_SUB_LABELS: Map<String, String> = mapOf(
        "1" to "¹", "2" to "²", "3" to "³", "4" to "⁴", "5" to "⁵",
        "6" to "⁶", "7" to "⁷", "8" to "⁸", "9" to "⁹", "0" to "⁰",
        "Q" to "%", "W" to "/", "E" to "|", "R" to "=", "T" to "[",
        "Y" to "]", "U" to "*", "I" to "!", "O" to "-", "P" to ";",
        "A" to "@", "S" to "#", "D" to "₹", "F" to "_", "G" to "&",
        "H" to "-", "J" to "+", "K" to "(", "L" to ")",
        "Z" to "*", "X" to "\"", "C" to "'", "V" to ":", "B" to ";",
        "N" to "!", "M" to "?"
    )

    val LONG_PRESS_POPUP_GRID: Map<String, List<String>> = mapOf(
        "1" to listOf("¹", "½", "⅓", "¼", "⅛"),
        "2" to listOf("²", "⅔"),
        "3" to listOf("³", "¾", "⅜"),
        "4" to listOf("⁴"),
        "5" to listOf("⁵", "⅝"),
        "6" to listOf("⁶"),
        "7" to listOf("⁷", "⅞"),
        "8" to listOf("⁸"),
        "9" to listOf("⁹"),
        "0" to listOf("⁰", "°", "∅"),
        "Q" to listOf("%", "1"),
        "W" to listOf("/", "2"),
        "E" to listOf("|", "3", "è", "é", "ê", "ë", "ē", "ė", "ę", "€"),
        "R" to listOf("=", "4", "ř", "ŕ"),
        "T" to listOf("[", "5", "þ", "ť", "ţ"),
        "Y" to listOf("]", "6", "¥", "ý", "ÿ"),
        "U" to listOf("*", "7", "ū", "ú", "ù", "û", "ü", "ų", "ů"),
        "I" to listOf("!", "8", "ī", "í", "ì", "î", "ï", "į", "ı"),
        "O" to listOf("-", "9", "ō", "ó", "ò", "ô", "ö", "õ", "ø", "œ"),
        "P" to listOf(";", "0", "π", "¶"),
        // Exact 16-symbol HeliBoard 2-row grid for 'A' (Screenshot 011233)
        "A" to listOf("&", "%", "+", "\"", "-", ":", "'", "@", ";", "/", "(", ")", "#", "!", ",", "?"),
        "S" to listOf("#", "ß", "ś", "š", "ş", "$"),
        "D" to listOf("₹", "$", "€", "£", "¥", "₩", "₽", "¢", "₿"),
        "F" to listOf("_", "ð", "ď"),
        "G" to listOf("&", "ğ", "ģ"),
        "H" to listOf("-", "ħ"),
        "J" to listOf("+"),
        "K" to listOf("(", "ķ"),
        "L" to listOf(")", "ł", "ĺ", "ľ"),
        "Z" to listOf("*", "ž", "ź", "ż"),
        "X" to listOf("\"", "×"),
        "C" to listOf("'", "ç", "ć", "č"),
        "V" to listOf(":", "√"),
        "B" to listOf(";", "β"),
        "N" to listOf("!", "ñ", "ń", "ň"),
        "M" to listOf("?", "μ"),
        // Comma key popup menu containing Emoji, Settings, Clipboard, Log Keeper, etc.
        "," to listOf("😀", "⚙️", "📋", "🪵", "🌐", "🎙️", "📝"),
        "." to listOf("!", "?", ",", ";", ":", "-", "_", "@", "/", "#"),
        "?" to listOf("!", "¿", "‽"),
        "!" to listOf("?", "¡"),
        "$" to listOf("₹", "€", "£", "¥", "₩", "₽", "¢", "₿"),
        "₹" to listOf("$", "€", "£", "¥", "₩", "₽", "¢", "₿"),
        "€" to listOf("$", "₹", "£", "¥", "₩", "₽", "¢", "₿")
    )
}


