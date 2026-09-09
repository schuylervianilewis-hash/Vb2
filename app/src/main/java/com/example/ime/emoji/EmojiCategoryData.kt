package com.example.ime.emoji

import android.content.Context
import com.example.R
import org.json.JSONArray

data class EmojiCategory(
    val id: String,
    val name: String,
    val iconResId: Int,
    val emojis: List<String>
)

object EmojiCategoryData {

    private const val PREFS_NAME = "vian_emoji_prefs"
    private const val KEY_RECENTS = "recent_emojis"
    private const val MAX_RECENTS = 40

    val DEFAULT_RECENTS = listOf(
        "😀", "😂", "🥰", "😍", "👍", "❤️", "🔥", "✨",
        "😊", "🙏", "🎉", "🤣", "😭", "🥺", "😎", "🙌"
    )

    val SMILEYS = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣",
        "🥲", "🥹", "😊", "😇", "🙂", "🙃", "😉", "😌",
        "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛",
        "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🥸",
        "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕",
        "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢",
        "😭", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵",
        "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔",
        "🫣", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬",
        "🫠", "🙄", "😯", "😦", "😧", "😮", "😲", "🥱",
        "😴", "🤤", "😪", "😵", "😵‍💫", "🤐", "🥴", "🤢",
        "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠", "😈",
        "👿", "👹", "👺", "🤡", "💩", "👻", "💀", "☠️",
        "👽", "👾", "🤖", "🎃"
    )

    val PEOPLE = listOf(
        "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏",
        "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈", "👉",
        "👆", "🖕", "👇", "☝️", "🫵", "👍", "👎", "✊",
        "👊", "🤛", "🤜", "👏", "🙌", "🫶", "👐", "🤲",
        "🤝", "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿",
        "🦵", "🦶", "👂", "🦻", "👃", "🧠", "🫀", "🫁",
        "🦷", "🦴", "👀", "👁️", "👅", "👄", "🫦", "👶",
        "🧒", "👦", "👧", "🧑", "👱", "👨", "🧔", "👩",
        "🧓", "👴", "👵", "🙍", "🙎", "🙅", "🙆", "💁",
        "🙋", "🧏", "🙇", "🤦", "🤷", "🧑‍⚕️", "👨‍⚕️", "👩‍⚕️"
    )

    val ANIMALS = listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
        "🐻‍❄️", "🐨", "🐯", "🦁", "🐮", "🐷", "🐽", "🐸",
        "🐵", "🙈", "🙉", "🙊", "🐒", "🐔", "🐧", "🐦",
        "🐤", "🐣", "🐥", "🦆", "🦅", "🦉", "🦇", "🐺",
        "🐗", "🐴", "🦄", "🐝", "🪱", "🐛", "🦋", "🐌",
        "🐞", "🐜", "🪰", "🪲", "🪳", "🦟", "🦗", "🕷️",
        "🕸️", "🦂", "🐢", "🐍", "🦎", "🦖", "🦕", "🐙",
        "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬",
        "🐳", "🐋", "🦈", "🦭", "🐊", "🐅", "🐆", "🦓",
        "🦍", "🦧", "🦣", "🐘", "🦛", "🦏", "🐪", "🐫",
        "🦒", "🦘", "🦬", "🐃", "🐂", "🐄", "🐎", "🐖"
    )

    val FOOD = listOf(
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇",
        "🍓", "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥",
        "🥝", "🍅", "🍆", "🥑", "🥦", "🥬", "🥒", "🌶️",
        "🫑", "🌽", "🥕", "🫒", "🧄", "🧅", "🥔", "🍠",
        "🥐", "🥯", "🍞", "🥖", "🥨", "🧀", "🥚", "🍳",
        "🧈", "🥞", "🧇", "🥓", "🥩", "🍗", "🍖", "🦴",
        "🌭", "🍔", "🍟", "🍕", "🫓", "🥪", "🥙", "🧆",
        "🌮", "🌯", "🫔", "🥗", "🥘", "🫕", "🍲", "🫙",
        "🍜", "🍝", "🍠", "🍢", "🍣", "🍤", "🍥", "🥮",
        "🍡", "🥟", "🥠", "🥡", "🍦", "🍧", "🍨", "🍩",
        "🍪", "🎂", "🍰", "🧁", "🥧", "🍫", "🍬", "🍭"
    )

    val TRAVEL = listOf(
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑",
        "🚒", "🚐", "🛻", "🚚", "🚛", "🚜", "🦯", "🦽",
        "🦼", "🛴", "🚲", "🛵", "🏍️", "🛺", "🚨", "🚔",
        "🚍", "🚘", "🚖", "🚡", "🚠", "🚟", "🚃", "🚋",
        "🚞", "🚝", "🚄", "🚅", "🚈", "🚂", "🚆", "🚇",
        "🚊", "🚉", "✈️", "🛫", "🛬", "🛩️", "💺", "🛰️",
        "🚀", "🛸", "🚁", "🛶", "⛵", "🚤", "🛥️", "🛳️",
        "⛴️", "🚢", "⚓", "🛟", "⛽", "🚧", "🚦", "🚥"
    )

    val ACTIVITIES = listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉",
        "🥏", "🎱", "🪀", "🏓", "🏸", "🏒", "🏑", "🥍",
        "🏏", "🪃", "🥅", "⛳", "🪁", "🏹", "🎣", "🤿",
        "🥊", "🥋", "🎽", "🛹", "🛼", "🛷", "⛸️", "🥌",
        "🎿", "⛷️", "🏂", "🪂", "🏋️", "🤼", "🤸", "🤺",
        "🧗", "🤾", "🏌️", "🏇", "🧘", "🏄", "🏊", "🤽",
        "🚣", "🧗", "🚵", "🚴", "🏆", "🥇", "🥈", "🥉"
    )

    val OBJECTS = listOf(
        "⌚", "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️",
        "🖲️", "🕹️", "🗜️", "💽", "💾", "💿", "📀", "📼",
        "📷", "📸", "📹", "🎥", "📽️", "🎞️", "📞", "☎️",
        "📟", "📠", "📺", "📻", "🎙️", "🎚️", "🎛️", "🧭",
        "⏱️", "⏲️", "⏰", "🕰️", "⌛", "⏳", "📡", "🔋",
        "🪫", "🔌", "💡", "🔦", "🕯️", "🪔", "🧯", "🛢️",
        "💸", "💵", "💴", "💶", "💷", "🪙", "💰", "💳",
        "💎", "⚖️", "🪜", "🧰", "🪛", "🔧", "🔨", "⚒️",
        "🛠️", "⛏️", "🪚", "🔩", "⚙️", "🪤", "🧱", "⛓️"
    )

    val SYMBOLS = listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
        "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖",
        "💘", "💝", "💟", "☮️", "✝️", "☪️", "🕉️", "☸️",
        "✡️", "🔯", "🕎", "☯️", "☦️", "🛐", "⛎", "♈",
        "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐",
        "♑", "♒", "♓", "🆔", "⚛️", "🉑", "📴", "📳",
        "🈶", "🈚", "🈸", "🈺", "🈷️", "✴️", "❇️", "©️",
        "®️", "™️", "🔟", "💯", "🔠", "🔡", "🔢", "🔣"
    )

    val FLAGS = listOf(
        "🏁", "🚩", "🎌", "🏴", "🏳️", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️",
        "🇺🇸", "🇬🇧", "🇨🇦", "🇦🇺", "🇫🇷", "🇩🇪", "🇮🇹", "🇪🇸",
        "🇯🇵", "🇰🇷", "🇨🇳", "🇮🇳", "🇧🇷", "🇲🇽", "🇷🇺", "🇿🇦"
    )

    fun getCategories(context: Context): List<EmojiCategory> {
        val recents = getRecentEmojis(context)
        return listOf(
            EmojiCategory("recent", "Recent", R.drawable.ic_emoji_recent, recents),
            EmojiCategory("smileys", "Smileys", R.drawable.ic_emoji_smileys, SMILEYS),
            EmojiCategory("people", "People", R.drawable.ic_emoji_people, PEOPLE),
            EmojiCategory("animals", "Animals", R.drawable.ic_emoji_food, ANIMALS),
            EmojiCategory("food", "Food", R.drawable.ic_emoji_food, FOOD),
            EmojiCategory("travel", "Travel", R.drawable.ic_emoji_travel, TRAVEL),
            EmojiCategory("activities", "Activities", R.drawable.ic_emoji_activities, ACTIVITIES),
            EmojiCategory("objects", "Objects", R.drawable.ic_emoji_objects, OBJECTS),
            EmojiCategory("symbols", "Symbols", R.drawable.ic_emoji_symbols, SYMBOLS),
            EmojiCategory("flags", "Flags", R.drawable.ic_emoji_flags, FLAGS)
        )
    }

    fun getRecentEmojis(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_RECENTS, null) ?: return DEFAULT_RECENTS
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            if (list.isEmpty()) DEFAULT_RECENTS else list
        } catch (e: Exception) {
            DEFAULT_RECENTS
        }
    }

    fun recordEmojiUse(context: Context, emoji: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getRecentEmojis(context).toMutableList()
        current.remove(emoji)
        current.add(0, emoji)
        if (current.size > MAX_RECENTS) {
            current.removeAt(current.lastIndex)
        }
        val arr = JSONArray()
        current.forEach { arr.put(it) }
        prefs.edit().putString(KEY_RECENTS, arr.toString()).apply()
    }
}
