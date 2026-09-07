// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.keyboard.clipboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import helium314.keyboard.keyboard.KeyboardActionListener
import helium314.keyboard.keyboard.KeyboardElement
import helium314.keyboard.keyboard.KeyboardLayoutSet
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.keyboard.MainKeyboardView
import helium314.keyboard.keyboard.internal.KeyVisualAttributes
import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.Constants
import helium314.keyboard.latin.database.PromptDao
import helium314.keyboard.latin.database.PromptEntry
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.ResourceUtils

class PromptAdapter(
    val clipboardLayoutParams: ClipboardLayoutParams,
    val onSelect: (String) -> Unit
) : RecyclerView.Adapter<PromptAdapter.ViewHolder>() {

    var promptDao: PromptDao? = null
    var pinnedIconResId = 0
    var itemBackgroundId = 0

    override fun getItemCount(): Int = promptDao?.count ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.clipboard_entry_key, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prompt = promptDao?.getEntry(position) ?: return
        holder.bind(prompt)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private val titleView: TextView = itemView.findViewById(R.id.clipboard_entry_text_content)
        private val pinnedIcon: ImageView = itemView.findViewById(R.id.clipboard_entry_pinned_icon)
        private val imageView: ImageView = itemView.findViewById(R.id.clipboard_entry_image_content)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            imageView.visibility = View.GONE
            if (itemBackgroundId != 0) {
                itemView.setBackgroundResource(itemBackgroundId)
            }
            if (pinnedIconResId != 0) {
                pinnedIcon.setImageResource(pinnedIconResId)
            }
        }

        fun bind(entry: PromptEntry) {
            itemView.tag = entry.id
            titleView.text = entry.text
            pinnedIcon.visibility = if (entry.isPinned) View.VISIBLE else View.GONE
            clipboardLayoutParams.setItemProperties(itemView)
        }

        override fun onClick(view: View) {
            val id = view.tag as? Long ?: return
            val entry = promptDao?.getEntryContent(id) ?: return
            onSelect(entry.text)
        }

        override fun onLongClick(view: View): Boolean {
            val id = view.tag as? Long ?: return false
            val entry = promptDao?.getEntryContent(id) ?: return false
            val popup = PopupMenu(view.context, view)
            val isPinned = entry.isPinned
            popup.menu.add(0, 1, 0, if (isPinned) "📌 Unpin" else "📌 Pin")
            popup.menu.add(0, 2, 1, "🗑️ Delete")
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    1 -> {
                        promptDao?.togglePinned(id)
                        notifyDataSetChanged()
                        true
                    }
                    2 -> {
                        val pos = absoluteAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            promptDao?.removeEntry(pos)
                            notifyItemRemoved(pos)
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
            return true
        }
    }
}

@SuppressLint("CustomViewStyleable")
class PromptHistoryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = R.attr.clipboardHistoryViewStyle
) : LinearLayout(context, attrs, defStyle), PromptDao.Listener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val clipboardLayoutParams = ClipboardLayoutParams(context)
    private val pinIconId: Int
    private val keyBackgroundId: Int

    private lateinit var promptRecyclerView: ClipboardHistoryRecyclerView
    private lateinit var placeholderView: TextView
    private lateinit var promptAdapter: PromptAdapter

    lateinit var keyboardActionListener: KeyboardActionListener
    private lateinit var promptDao: PromptDao
    private var mainKeyboardView: MainKeyboardView? = null
    private var kls: KeyboardLayoutSet? = null

    init {
        orientation = VERTICAL
        val clipboardViewAttr = context.obtainStyledAttributes(attrs,
            R.styleable.ClipboardHistoryView, defStyle, R.style.ClipboardHistoryView)
        pinIconId = clipboardViewAttr.getResourceId(R.styleable.ClipboardHistoryView_iconPinnedClip, 0)
        clipboardViewAttr.recycle()
        val keyboardViewAttr = context.obtainStyledAttributes(attrs, R.styleable.KeyboardView, defStyle, R.style.KeyboardView)
        keyBackgroundId = keyboardViewAttr.getResourceId(R.styleable.KeyboardView_keyBackground, 0)
        keyboardViewAttr.recycle()
        fitsSystemWindows = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val res = context.resources
        val width = ResourceUtils.getKeyboardWidth(context, Settings.getValues()) + paddingLeft + paddingRight
        val height = ResourceUtils.getSecondaryKeyboardHeight(res, Settings.getValues()) + paddingTop + paddingBottom
        setMeasuredDimension(width, height)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        placeholderView = findViewById(R.id.prompt_empty_view)
        promptRecyclerView = findViewById(R.id.prompt_list)
        promptRecyclerView.apply {
            val colCount = resources.getInteger(R.integer.config_clipboard_keyboard_col_count)
            layoutManager = StaggeredGridLayoutManager(colCount, StaggeredGridLayoutManager.VERTICAL)
            clipboardLayoutParams.setListProperties(this)
        }
        mainKeyboardView = findViewById(R.id.bottom_row_keyboard)
    }

    fun startPromptHistory(
        actionListener: KeyboardActionListener,
        keyVisualAttributes: KeyVisualAttributes?,
        kls: KeyboardLayoutSet?,
        onCommitText: (String) -> Unit
    ) {
        this.keyboardActionListener = actionListener
        this.promptDao = PromptDao.getInstance(context)
        this.kls = kls
        promptDao.listener = this

        promptAdapter = PromptAdapter(clipboardLayoutParams) { selectedPrompt ->
            onCommitText(selectedPrompt)
            keyboardActionListener.onCodeInput(KeyCode.ALPHA, Constants.NOT_A_COORDINATE, Constants.NOT_A_COORDINATE, false)
        }.apply {
            promptDao = this@PromptHistoryView.promptDao
            pinnedIconResId = pinIconId
            itemBackgroundId = keyBackgroundId
        }

        promptRecyclerView.adapter = promptAdapter
        updateEmptyView()

        mainKeyboardView?.let {
            it.setKeyboardActionListener(actionListener)
            kls?.getKeyboard(KeyboardElement.CLIPBOARD_BOTTOM_ROW)?.let { kb ->
                it.setKeyboard(kb)
            }
        }
    }

    private fun updateEmptyView() {
        if (promptDao.count == 0) {
            placeholderView.visibility = View.VISIBLE
            promptRecyclerView.visibility = View.GONE
        } else {
            placeholderView.visibility = View.GONE
            promptRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onPromptInserted(position: Int) {
        promptAdapter.notifyItemInserted(position)
        updateEmptyView()
    }

    override fun onPromptsRemoved(position: Int, count: Int) {
        promptAdapter.notifyItemRangeRemoved(position, count)
        updateEmptyView()
    }

    override fun onPromptMoved(oldPosition: Int, newPosition: Int) {
        promptAdapter.notifyItemMoved(oldPosition, newPosition)
        updateEmptyView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        promptAdapter.notifyDataSetChanged()
    }
}
