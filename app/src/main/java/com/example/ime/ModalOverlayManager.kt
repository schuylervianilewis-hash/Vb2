package com.example.ime

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.foundation.common.Constants
import com.example.foundation.utils.ResourceUtils
import com.example.ime.emoji.EmojiModalView

/**
 * ModalOverlayManager hosts in-keyboard modal sheets (Emoji, Clipboard, Prompts, Vault)
 * seamlessly inside the docked keyboard frame with the Unified 4-Button Bottom Bar [ABC] [SPACE] [⌫] [↵].
 */
class ModalOverlayManager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class ModalType {
        NONE,
        CLIPBOARD,
        PROMPT_LIST,
        DESKTOP_NAV,
        EMOJI,
        VAULT,
        VOICE_INPUT
    }

    interface ModalActionListener {
        fun onDismissModal()
        fun onBottomBarAction(code: Int)
        fun onPasteItem(content: String)
    }

    var listener: ModalActionListener? = null
    var currentModal: ModalType = ModalType.NONE
        private set

    private val contentContainer = FrameLayout(context)

    init {
        setBackgroundColor(Color.parseColor("#E8ECEF"))
        setupLayout()
        visibility = GONE
    }

    private fun setupLayout() {
        contentContainer.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(contentContainer)
    }

    fun showModal(modalType: ModalType, customContentView: View? = null) {
        currentModal = modalType
        contentContainer.removeAllViews()

        if (modalType == ModalType.EMOJI) {
            val emojiView = EmojiModalView(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                listener = object : EmojiModalView.EmojiActionListener {
                    override fun onEmojiSelected(emoji: String) {
                        this@ModalOverlayManager.listener?.onPasteItem(emoji)
                    }

                    override fun onBackToAlpha() {
                        dismiss()
                    }

                    override fun onSpacePressed() {
                        this@ModalOverlayManager.listener?.onBottomBarAction(Constants.CODE_SPACE)
                    }

                    override fun onDeletePressed() {
                        this@ModalOverlayManager.listener?.onBottomBarAction(Constants.CODE_DELETE)
                    }

                    override fun onEnterPressed() {
                        this@ModalOverlayManager.listener?.onBottomBarAction(Constants.CODE_ENTER)
                    }
                }
            }
            contentContainer.addView(emojiView)
        } else if (customContentView != null) {
            contentContainer.addView(customContentView)
        } else {
            // Default styled modal container
            val tv = TextView(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                gravity = Gravity.CENTER
                textSize = 16f
                setTextColor(Color.parseColor("#202124"))
                text = "Vian Board — ${modalType.name.replace('_', ' ')}"
            }
            contentContainer.addView(tv)
        }

        visibility = VISIBLE
    }

    fun dismiss() {
        currentModal = ModalType.NONE
        contentContainer.removeAllViews()
        visibility = GONE
        listener?.onDismissModal()
    }

    fun isModalShowing(): Boolean = visibility == VISIBLE
}
