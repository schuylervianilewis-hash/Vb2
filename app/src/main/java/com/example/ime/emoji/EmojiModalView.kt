package com.example.ime.emoji

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.foundation.common.Constants
import com.example.foundation.utils.ResourceUtils

/**
 * HeliBoard-matching Docked Emoji View.
 * Contains:
 * 1. Top Scrollable Category Tab Bar (Recent, Smileys, People, Animals, Food, Travel, Activities, Objects, Symbols, Flags, Kaomoji)
 * 2. 8-Column High Performance Native Emoji Grid
 * 3. Bottom 4-Button Unified Control Bar [ABC] [     SPACE     ] [ ⌫ ] [ ↵ ]
 */
class EmojiModalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface EmojiActionListener {
        fun onEmojiSelected(emoji: String)
        fun onBackToAlpha()
        fun onSpacePressed()
        fun onDeletePressed()
        fun onEnterPressed()
    }

    var listener: EmojiActionListener? = null

    private val categoryScrollView = HorizontalScrollView(context)
    private val categoryTabBar = LinearLayout(context)
    private val contentScrollView = ScrollView(context)
    private val emojiGridLayout = GridLayout(context)
    private val bottomBar = LinearLayout(context)

    private var selectedCategoryIndex = 1 // Default to Smileys

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#E8ECEF")) // Matching keyboard off-white slate
        setupViews()
    }

    private fun setupViews() {
        // 1. Top Category Bar (Height: 38dp)
        categoryScrollView.apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ResourceUtils.dpToPx(context, 38f).toInt())
            isHorizontalScrollBarEnabled = false
            setBackgroundColor(Color.parseColor("#DDE2E6"))
        }

        categoryTabBar.apply {
            orientation = HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(ResourceUtils.dpToPx(context, 4f).toInt(), 0, ResourceUtils.dpToPx(context, 4f).toInt(), 0)
        }

        val categories = listOf(
            "🕒" to "Recent",
            "😀" to "Smileys",
            "🧗" to "People",
            "🐕" to "Animals",
            "☕" to "Food",
            "🚗" to "Travel",
            "🏆" to "Activities",
            "💡" to "Objects",
            "🔣" to "Symbols",
            "🚩" to "Flags",
            ":-)" to "Kaomoji"
        )

        categories.forEachIndexed { index, (icon, _) ->
            val tab = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(ResourceUtils.dpToPx(context, 38f).toInt(), LayoutParams.MATCH_PARENT).apply {
                    setMargins(ResourceUtils.dpToPx(context, 2f).toInt(), 0, ResourceUtils.dpToPx(context, 2f).toInt(), 0)
                }
                text = icon
                gravity = Gravity.CENTER
                textSize = 17f
                setTextColor(if (index == selectedCategoryIndex) Color.parseColor("#1976D2") else Color.parseColor("#37474F"))
                setBackgroundColor(if (index == selectedCategoryIndex) Color.parseColor("#FFFFFF") else Color.TRANSPARENT)

                setOnClickListener {
                    selectCategory(index)
                }
            }
            categoryTabBar.addView(tab)
        }

        categoryScrollView.addView(categoryTabBar)
        addView(categoryScrollView)

        // 2. High Density Emoji Grid (8 columns)
        contentScrollView.apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
            isVerticalScrollBarEnabled = true
        }

        emojiGridLayout.apply {
            columnCount = 8
            layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(ResourceUtils.dpToPx(context, 4f).toInt(), ResourceUtils.dpToPx(context, 4f).toInt(), ResourceUtils.dpToPx(context, 4f).toInt(), ResourceUtils.dpToPx(context, 4f).toInt())
        }
        contentScrollView.addView(emojiGridLayout)
        addView(contentScrollView)

        // 3. Mandatory 4-Button Bottom Control Bar: [ABC] [ SPACEBAR ] [ ⌫ ] [ ↵ ]
        bottomBar.apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ResourceUtils.dpToPx(context, 46f).toInt())
            setBackgroundColor(Color.parseColor("#E8ECEF"))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(ResourceUtils.dpToPx(context, 4f).toInt(), ResourceUtils.dpToPx(context, 3f).toInt(), ResourceUtils.dpToPx(context, 4f).toInt(), ResourceUtils.dpToPx(context, 3f).toInt())
        }

        // Button 1: ABC (Return to QWERTY)
        val abcBtn = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(ResourceUtils.dpToPx(context, 54f).toInt(), LayoutParams.MATCH_PARENT).apply {
                setMargins(ResourceUtils.dpToPx(context, 2f).toInt(), 0, ResourceUtils.dpToPx(context, 2f).toInt(), 0)
            }
            text = "ABC"
            gravity = Gravity.CENTER
            textSize = 14f
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.parseColor("#202124"))
            setBackgroundColor(Color.parseColor("#DDE2E6"))
            setOnClickListener { listener?.onBackToAlpha() }
        }
        bottomBar.addView(abcBtn)

        // Button 2: Wide Spacebar
        val spaceBtn = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f).apply {
                setMargins(ResourceUtils.dpToPx(context, 2f).toInt(), 0, ResourceUtils.dpToPx(context, 2f).toInt(), 0)
            }
            text = ""
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#FFFFFF"))
            setOnClickListener { listener?.onSpacePressed() }
        }
        bottomBar.addView(spaceBtn)

        // Button 3: Backspace (⌫)
        val backspaceBtn = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(ResourceUtils.dpToPx(context, 50f).toInt(), LayoutParams.MATCH_PARENT).apply {
                setMargins(ResourceUtils.dpToPx(context, 2f).toInt(), 0, ResourceUtils.dpToPx(context, 2f).toInt(), 0)
            }
            text = "⌫"
            gravity = Gravity.CENTER
            textSize = 18f
            setTextColor(Color.parseColor("#202124"))
            setBackgroundColor(Color.parseColor("#DDE2E6"))
            setOnClickListener { listener?.onDeletePressed() }
        }
        bottomBar.addView(backspaceBtn)

        // Button 4: Enter (↵)
        val enterBtn = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(ResourceUtils.dpToPx(context, 50f).toInt(), LayoutParams.MATCH_PARENT).apply {
                setMargins(ResourceUtils.dpToPx(context, 2f).toInt(), 0, ResourceUtils.dpToPx(context, 2f).toInt(), 0)
            }
            text = "↵"
            gravity = Gravity.CENTER
            textSize = 18f
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.parseColor("#FFFFFF"))
            setBackgroundColor(Color.parseColor("#78909C")) // Slate-blue action accent
            setOnClickListener { listener?.onEnterPressed() }
        }
        bottomBar.addView(enterBtn)

        addView(bottomBar)

        // Initial category load
        selectCategory(selectedCategoryIndex)
    }

    private fun selectCategory(index: Int) {
        selectedCategoryIndex = index

        for (i in 0 until categoryTabBar.childCount) {
            val tab = categoryTabBar.getChildAt(i) as? TextView ?: continue
            if (i == index) {
                tab.setTextColor(Color.parseColor("#1976D2"))
                tab.setBackgroundColor(Color.parseColor("#FFFFFF"))
            } else {
                tab.setTextColor(Color.parseColor("#37474F"))
                tab.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        val items = when (index) {
            0 -> EmojiData.getRecents(context)
            1 -> EmojiData.SMILEYS
            2 -> EmojiData.PEOPLE
            3 -> EmojiData.ANIMALS
            4 -> EmojiData.FOOD
            5 -> EmojiData.TRAVEL
            6 -> EmojiData.ACTIVITIES
            7 -> EmojiData.OBJECTS
            8 -> EmojiData.SYMBOLS
            9 -> EmojiData.FLAGS
            10 -> EmojiData.KAOMOJI
            else -> EmojiData.SMILEYS
        }

        emojiGridLayout.removeAllViews()
        val isKaomoji = index == 10
        emojiGridLayout.columnCount = if (isKaomoji) 2 else 8

        val cellHeight = ResourceUtils.dpToPx(context, 40f).toInt()
        for (item in items) {
            val tv = TextView(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = cellHeight
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                text = item
                gravity = Gravity.CENTER
                textSize = if (item.length > 2) 13f else 22f
                setOnClickListener {
                    EmojiData.addRecent(context, item)
                    listener?.onEmojiSelected(item)
                }
            }
            emojiGridLayout.addView(tv)
        }

        contentScrollView.scrollTo(0, 0)
    }
}
