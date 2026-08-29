package com.example.ui

import android.content.Context
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogTag
import com.example.foundation.common.Constants
import com.example.foundation.utils.ResourceUtils
import com.example.ime.ModalOverlayManager
import com.example.ime.SuggestionStripView
import com.example.keyboard.internal.Key
import com.example.keyboard.internal.KeyPopupOverlayView
import com.example.keyboard.internal.KeyboardLayoutBuilder
import com.example.keyboard.internal.MainKeyboardView
import com.example.keyboard.internal.PointerTracker
import com.example.ui.theme.SkyBlueBorder
import com.example.ui.theme.SkyBluePrimary
import com.example.vault.ui.VaultOverlayView

enum class TesterLayoutMode {
    ALPHA_LOWER,
    ALPHA_UPPER,
    SYMBOLS_123,
    MORE_SYMBOLS,
    NUMPAD
}

/**
 * AppearanceTesterScreen allows inspecting and interacting with all keyboard layouts,
 * popups, and modal sheets directly inside the Google AI Studio browser preview
 * without enabling the Android IME in system settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceTesterScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var typedText by remember { mutableStateOf("Testing Vian Board layout...") }
    var currentLayoutMode by remember { mutableStateOf(TesterLayoutMode.ALPHA_LOWER) }
    var activeModalName by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf("Interactive Preview: Tap or long-press keys below") }

    // References to embedded Android Views
    var keyboardViewRef by remember { mutableStateOf<MainKeyboardView?>(null) }
    var popupOverlayRef by remember { mutableStateOf<KeyPopupOverlayView?>(null) }
    var modalOverlayRef by remember { mutableStateOf<ModalOverlayManager?>(null) }

    fun updateKeyboardLayout(mode: TesterLayoutMode) {
        currentLayoutMode = mode
        val kbView = keyboardViewRef ?: return
        val layoutMode = when (mode) {
            TesterLayoutMode.ALPHA_LOWER -> KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER
            TesterLayoutMode.ALPHA_UPPER -> KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER
            TesterLayoutMode.SYMBOLS_123 -> KeyboardLayoutBuilder.LayoutMode.SYMBOLS_1
            TesterLayoutMode.MORE_SYMBOLS -> KeyboardLayoutBuilder.LayoutMode.SYMBOLS_2
            TesterLayoutMode.NUMPAD -> KeyboardLayoutBuilder.LayoutMode.NUMBER_PAD
        }
        kbView.setLayoutMode(layoutMode)
    }

    LaunchedEffect(Unit) {
        LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "Navigated to AppearanceTesterScreen")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Appearance & Layout Tester", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Live in-browser preview • No IME enable needed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        typedText = ""
                        infoMessage = "Text cleared"
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Scrollable Control & Sandbox Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Typed Text Output Box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SkyBlueBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Live Typing Sandbox",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SkyBluePrimary
                            )
                            Text(
                                "${typedText.length} chars",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (typedText.isEmpty()) "Type something on the preview keyboard below..." else typedText,
                            fontSize = 15.sp,
                            fontWeight = if (typedText.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                            color = if (typedText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = infoMessage,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 1. Layout Mode Switcher Chips
                Text("1. Select Keyboard Layout", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentLayoutMode == TesterLayoutMode.ALPHA_LOWER,
                        onClick = { updateKeyboardLayout(TesterLayoutMode.ALPHA_LOWER) },
                        label = { Text("QWERTY (abc)") }
                    )
                    FilterChip(
                        selected = currentLayoutMode == TesterLayoutMode.ALPHA_UPPER,
                        onClick = { updateKeyboardLayout(TesterLayoutMode.ALPHA_UPPER) },
                        label = { Text("Shifted (ABC)") }
                    )
                    FilterChip(
                        selected = currentLayoutMode == TesterLayoutMode.SYMBOLS_123,
                        onClick = { updateKeyboardLayout(TesterLayoutMode.SYMBOLS_123) },
                        label = { Text("?123 (Symbols)") }
                    )
                    FilterChip(
                        selected = currentLayoutMode == TesterLayoutMode.MORE_SYMBOLS,
                        onClick = { updateKeyboardLayout(TesterLayoutMode.MORE_SYMBOLS) },
                        label = { Text("=\\< (More)") }
                    )
                    FilterChip(
                        selected = currentLayoutMode == TesterLayoutMode.NUMPAD,
                        onClick = { updateKeyboardLayout(TesterLayoutMode.NUMPAD) },
                        label = { Text("123 (Numpad)") }
                    )
                }

                // 2. Modals & Overlay Sheets
                Text("2. Preview In-Keyboard Modals", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            activeModalName = "Clipboard"
                            val clipboardView = LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL
                                setPadding(32, 24, 32, 24)
                                val title = TextView(context).apply {
                                    text = "📋 Clipboard History (Live Preview)"
                                    textSize = 16f
                                    setTextColor(AndroidColor.WHITE)
                                    setPadding(0, 0, 0, 16)
                                }
                                addView(title)
                                val sample1 = TextView(context).apply {
                                    text = "• Clean, privacy-first clipboard snippet"
                                    textSize = 14f
                                    setTextColor(AndroidColor.parseColor("#BAC2DE"))
                                    setPadding(0, 8, 0, 8)
                                    setOnClickListener {
                                        typedText += " Clean, privacy-first clipboard snippet"
                                        modalOverlayRef?.dismiss()
                                    }
                                }
                                addView(sample1)
                                val sample2 = TextView(context).apply {
                                    text = "• Tap any item to paste directly into sandbox"
                                    textSize = 14f
                                    setTextColor(AndroidColor.parseColor("#BAC2DE"))
                                    setPadding(0, 8, 0, 8)
                                    setOnClickListener {
                                        typedText += " Tap any item to paste directly into sandbox"
                                        modalOverlayRef?.dismiss()
                                    }
                                }
                                addView(sample2)
                            }
                            modalOverlayRef?.showModal(ModalOverlayManager.ModalType.CLIPBOARD, clipboardView)
                            infoMessage = "Showing Clipboard Modal"
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("📋 Clipboard", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            activeModalName = "Emoji"
                            val emojiView = LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL
                                setPadding(32, 24, 32, 24)
                                val title = TextView(context).apply {
                                    text = "☺ Emoji Picker Grid (Live Preview)"
                                    textSize = 16f
                                    setTextColor(AndroidColor.WHITE)
                                    setPadding(0, 0, 0, 16)
                                }
                                addView(title)
                                val emojis = listOf("😀", "😂", "🚀", "❤️", "👍", "🔥", "✨", "🎉", "💯", "🔒")
                                val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                                for (e in emojis) {
                                    val tv = TextView(context).apply {
                                        text = e
                                        textSize = 24f
                                        setPadding(12, 12, 12, 12)
                                        setOnClickListener {
                                            typedText += e
                                            modalOverlayRef?.dismiss()
                                        }
                                    }
                                    row.addView(tv)
                                }
                                addView(row)
                            }
                            modalOverlayRef?.showModal(ModalOverlayManager.ModalType.EMOJI, emojiView)
                            infoMessage = "Showing Emoji Picker Modal"
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("☺ Emoji", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            activeModalName = "Vault"
                            val vaultOverlay = VaultOverlayView(context).apply {
                                actionListener = object : VaultOverlayView.VaultOverlayActionListener {
                                    override fun onInjectText(text: String) {
                                        typedText += text
                                        modalOverlayRef?.dismiss()
                                    }
                                    override fun onDismissRequested() {
                                        modalOverlayRef?.dismiss()
                                    }
                                    override fun onSpaceClicked() { typedText += " " }
                                    override fun onDeleteClicked() {
                                        if (typedText.isNotEmpty()) typedText = typedText.dropLast(1)
                                    }
                                    override fun onEnterClicked() { typedText += "\n" }
                                }
                            }
                            modalOverlayRef?.showModal(ModalOverlayManager.ModalType.VAULT, vaultOverlay)
                            infoMessage = "Showing Security Vault Modal (Placeholder)"
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("🔒 Vault", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            activeModalName = "Prompts"
                            val promptView = LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL
                                setPadding(32, 24, 32, 24)
                                val title = TextView(context).apply {
                                    text = "📝 Prompts & Quick Snippets"
                                    textSize = 16f
                                    setTextColor(AndroidColor.WHITE)
                                    setPadding(0, 0, 0, 16)
                                }
                                addView(title)
                                val snippet = TextView(context).apply {
                                    text = "• Best regards,\n  Vian Board User"
                                    textSize = 14f
                                    setTextColor(AndroidColor.parseColor("#BAC2DE"))
                                    setPadding(0, 8, 0, 8)
                                    setOnClickListener {
                                        typedText += "\nBest regards,\nVian Board User"
                                        modalOverlayRef?.dismiss()
                                    }
                                }
                                addView(snippet)
                            }
                            modalOverlayRef?.showModal(ModalOverlayManager.ModalType.PROMPT_LIST, promptView)
                            infoMessage = "Showing Prompts Modal"
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("📝 Prompts", fontSize = 12.sp)
                    }

                    if (modalOverlayRef?.isModalShowing() == true) {
                        Button(
                            onClick = {
                                modalOverlayRef?.dismiss()
                                infoMessage = "Modal dismissed"
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("✖ Close Sheet", fontSize = 12.sp)
                        }
                    }
                }

                // 3. MoreKeys / Popup Previews
                Text("3. Test Long-Press Popups", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val kb = keyboardViewRef ?: return@OutlinedButton
                            val commaKey = kb.currentKeys.find { it.label == "," }
                            if (commaKey != null) {
                                kb.showMoreKeysPopup(commaKey)
                                infoMessage = "Showing Comma Menu: ☺ ⚙ 📋 🌐 ⤢ 🪵"
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Comma (,) Menu", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val kb = keyboardViewRef ?: return@OutlinedButton
                            val aKey = kb.currentKeys.find { it.label.equals("a", ignoreCase = true) }
                            if (aKey != null) {
                                kb.showMoreKeysPopup(aKey)
                                infoMessage = "Showing 2-Row Card on Key 'A'"
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Key 'A' 2-Row Card", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val kb = keyboardViewRef ?: return@OutlinedButton
                            val sKey = kb.currentKeys.find { it.label.equals("s", ignoreCase = true) }
                            if (sKey != null) {
                                kb.showMoreKeysPopup(sKey)
                                infoMessage = "Showing 2-Row Card on Key 'S'"
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Key 'S' 2-Row Card", fontSize = 12.sp)
                    }

                    if (popupOverlayRef?.isShowingMoreKeys() == true) {
                        Button(
                            onClick = {
                                popupOverlayRef?.dismissMoreKeys()
                                infoMessage = "Popup card dismissed"
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("✖ Close Popup", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Fixed Bottom Area: Interactive Keyboard Preview
            Card(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                AndroidView(
                    factory = { ctx ->
                        val root = FrameLayout(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }

                        val verticalContainer = LinearLayout(ctx).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        // 1. Suggestion Strip View
                        val suggestionStrip = SuggestionStripView(ctx).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                ResourceUtils.dpToPx(ctx, 40f).toInt()
                            )
                        }
                        verticalContainer.addView(suggestionStrip)

                        // 2. Main Keyboard View
                        val mainKeyboard = MainKeyboardView(ctx).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setLayoutMode(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER)
                        }
                        verticalContainer.addView(mainKeyboard)
                        root.addView(verticalContainer)

                        // 3. Popup Overlay View
                        val popupOverlay = KeyPopupOverlayView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        }
                        root.addView(popupOverlay)
                        mainKeyboard.popupOverlay = popupOverlay

                        // 4. Modal Overlay View
                        val modalOverlay = ModalOverlayManager(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                            listener = object : ModalOverlayManager.ModalActionListener {
                                override fun onDismissModal() {
                                    activeModalName = null
                                }
                                override fun onBottomBarAction(code: Int) {
                                    when (code) {
                                        Constants.CODE_SPACE -> typedText += " "
                                        Constants.CODE_DELETE -> if (typedText.isNotEmpty()) typedText = typedText.dropLast(1)
                                        Constants.CODE_ENTER -> typedText += "\n"
                                    }
                                }
                                override fun onPasteItem(content: String) {
                                    typedText += content
                                }
                            }
                        }
                        root.addView(modalOverlay)

                        // Connect keyboard actions to live typing sandbox
                        mainKeyboard.actionListener = object : PointerTracker.KeyboardActionListener {
                            override fun onKeyPress(key: Key) {}
                            override fun onKeyRelease(key: Key) {
                                when (key.code) {
                                    Constants.CODE_DELETE -> {
                                        if (typedText.isNotEmpty()) {
                                            typedText = typedText.dropLast(1)
                                        }
                                    }
                                    Constants.CODE_SPACE -> typedText += " "
                                    Constants.CODE_ENTER -> typedText += "\n"
                                    Constants.CODE_SHIFT -> {
                                        if (currentLayoutMode == TesterLayoutMode.ALPHA_LOWER) {
                                            updateKeyboardLayout(TesterLayoutMode.ALPHA_UPPER)
                                        } else if (currentLayoutMode == TesterLayoutMode.ALPHA_UPPER) {
                                            updateKeyboardLayout(TesterLayoutMode.ALPHA_LOWER)
                                        }
                                    }
                                    Constants.CODE_SWITCH_ALPHA_SYMBOL -> {
                                        if (currentLayoutMode == TesterLayoutMode.ALPHA_LOWER || currentLayoutMode == TesterLayoutMode.ALPHA_UPPER) {
                                            updateKeyboardLayout(TesterLayoutMode.SYMBOLS_123)
                                        } else {
                                            updateKeyboardLayout(TesterLayoutMode.ALPHA_LOWER)
                                        }
                                    }
                                    Constants.CODE_NUMPAD -> {
                                        updateKeyboardLayout(TesterLayoutMode.NUMPAD)
                                    }
                                    else -> {
                                        if (key.label.startsWith("=") || key.label == "=\\<") {
                                            updateKeyboardLayout(TesterLayoutMode.MORE_SYMBOLS)
                                        } else if (key.label == "?123") {
                                            updateKeyboardLayout(TesterLayoutMode.SYMBOLS_123)
                                        } else if (key.label.isNotEmpty()) {
                                            typedText += key.label
                                        }
                                    }
                                }
                            }
                            override fun onKeyLongPress(key: Key) {
                                infoMessage = "Long pressed: ${key.label}"
                            }
                            override fun onMoreKeySelected(candidate: String) {
                                when (candidate) {
                                    "⚙", "⚙️" -> infoMessage = "Settings action triggered"
                                    "📋" -> {
                                        modalOverlay.showModal(ModalOverlayManager.ModalType.CLIPBOARD)
                                        infoMessage = "Clipboard sheet opened"
                                    }
                                    "☺", "😀" -> {
                                        modalOverlay.showModal(ModalOverlayManager.ModalType.EMOJI)
                                        infoMessage = "Emoji sheet opened"
                                    }
                                    "🌐" -> infoMessage = "Language switched"
                                    "⤢" -> infoMessage = "Resize / One-handed mode"
                                    "🪵" -> infoMessage = "Log keeper opened"
                                    else -> typedText += candidate
                                }
                            }
                            override fun onSpacebarSlide(deltaX: Float) {
                                infoMessage = "Spacebar slide: ${deltaX.toInt()}px"
                            }
                            override fun onBackspaceSwipe(deltaX: Float) {
                                infoMessage = "Backspace swipe: ${deltaX.toInt()}px"
                            }
                            override fun onBackspaceSwipeRelease() {
                                infoMessage = "Backspace swipe released"
                            }
                        }

                        keyboardViewRef = mainKeyboard
                        popupOverlayRef = popupOverlay
                        modalOverlayRef = modalOverlay

                        root
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
