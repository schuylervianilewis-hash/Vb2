package com.example.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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

/**
 * AppearanceTesterScreen displays the authentic docked keyboard layout at the bottom
 * with a realistic notepad / typing screen above, allowing real keyboard interactions
 * (QWERTY, Shift, ?123, =\<, morekeys, gestures).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceTesterScreen(
    onNavigateBack: () -> Unit
) {
    var typedText by remember { mutableStateOf("Type here using the keyboard below...") }
    var currentLayoutMode by remember { mutableStateOf(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER) }

    // References to embedded Android Views
    var keyboardViewRef by remember { mutableStateOf<MainKeyboardView?>(null) }
    var popupOverlayRef by remember { mutableStateOf<KeyPopupOverlayView?>(null) }
    var modalOverlayRef by remember { mutableStateOf<ModalOverlayManager?>(null) }

    fun switchLayout(mode: KeyboardLayoutBuilder.LayoutMode) {
        currentLayoutMode = mode
        keyboardViewRef?.setLayoutMode(mode)
    }

    LaunchedEffect(Unit) {
        LogKeeper.log(LogTag.NAVIGATION, LogLevel.INFO, "Navigated to AppearanceTesterScreen")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Keyboard Layout Preview",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { typedText = "" }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Clear")
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
            // Realistic full-height Notepad / Document Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (typedText.isEmpty()) "Tap any key below to start typing..." else typedText,
                    fontSize = 16.sp,
                    color = if (typedText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }

            // Authentic Docked Keyboard Area at Bottom (Real IME Dimensions: 40dp strip + 230dp keys)
            Surface(
                color = Color(0xFFE8ECEF),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp),
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
                        val generalSettings = com.example.settings.GeneralSettingsManager(ctx).load()
                        val mainKeyboard = MainKeyboardView(ctx).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setKeyStyling(
                                cornerRadiusDp = generalSettings.keyCornerRadiusDp,
                                horizontalGapDp = generalSettings.keyHorizontalGapDp,
                                verticalGapDp = generalSettings.keyVerticalGapDp,
                                borderWidthDp = generalSettings.keyBorderWidthDp,
                                outlineEnabled = generalSettings.keyOutlineEnabled
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
                                override fun onDismissModal() {}
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
                                        if (currentLayoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER) {
                                            switchLayout(KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER)
                                        } else if (currentLayoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER) {
                                            switchLayout(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER)
                                        }
                                    }
                                    Constants.CODE_SWITCH_ALPHA_SYMBOL -> {
                                        if (currentLayoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER || currentLayoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER) {
                                            switchLayout(KeyboardLayoutBuilder.LayoutMode.SYMBOLS_1)
                                        } else {
                                            switchLayout(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER)
                                        }
                                    }
                                    Constants.CODE_NUMPAD -> {
                                        switchLayout(KeyboardLayoutBuilder.LayoutMode.NUMBER_PAD)
                                    }
                                    else -> {
                                        if (key.label.startsWith("=") || key.label == "=\\<") {
                                            switchLayout(KeyboardLayoutBuilder.LayoutMode.SYMBOLS_2)
                                        } else if (key.label == "?123") {
                                            switchLayout(KeyboardLayoutBuilder.LayoutMode.SYMBOLS_1)
                                        } else if (key.label.isNotEmpty()) {
                                            if (typedText == "Type here using the keyboard below...") {
                                                typedText = ""
                                            }
                                            typedText += key.label
                                        }
                                    }
                                }
                            }
                            override fun onKeyLongPress(key: Key) {}
                            override fun onMoreKeySelected(candidate: String) {
                                when (candidate) {
                                    "📋" -> modalOverlay.showModal(ModalOverlayManager.ModalType.CLIPBOARD)
                                    "☺", "😀" -> modalOverlay.showModal(ModalOverlayManager.ModalType.EMOJI)
                                    else -> typedText += candidate
                                }
                            }
                            override fun onSpacebarSlide(deltaX: Float) {}
                            override fun onBackspaceSwipe(deltaX: Float) {}
                            override fun onBackspaceSwipeRelease() {}
                        }

                        keyboardViewRef = mainKeyboard
                        popupOverlayRef = popupOverlay
                        modalOverlayRef = modalOverlay

                        root
                    }
                )
            }
        }
    }
}
