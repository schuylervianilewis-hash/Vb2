# Audit Receipts Log - Series 004

### Entry: 2026-09-08T14:15:00-07:00
- **Summary**: Implemented on-demand Clipboard and Emoji modals with Speed Island editing toolbar, category pill tabs, and unified 4-button modal bottom bar.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/ime/modal/ModalBottomBarView.kt`
  - `/app/src/main/java/com/example/ime/clipboard/ClipboardStorage.kt`
  - `/app/src/main/java/com/example/ime/clipboard/ClipboardCardsAdapter.kt`
  - `/app/src/main/java/com/example/ime/clipboard/VianClipboardModalView.kt`
  - `/app/src/main/java/com/example/ime/emoji/EmojiCategoryData.kt`
  - `/app/src/main/java/com/example/ime/emoji/EmojiGlyphAdapter.kt`
  - `/app/src/main/java/com/example/ime/emoji/VianEmojiModalView.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/app/src/main/res/layout/item_clipboard_card.xml`
  - `/app/src/main/res/layout/view_clipboard_modal.xml`
  - `/app/src/main/res/layout/item_emoji_tab_pill.xml`
  - `/app/src/main/res/layout/item_emoji_glyph.xml`
  - `/app/src/main/res/layout/view_emoji_modal.xml`
  - `/app/src/main/res/drawable/bg_clipboard_card.xml`
  - `/app/src/main/res/drawable/bg_emoji_tab_pill_selected.xml`
  - `/receipts/RECEIPTS_004.md`
- **What was actually done**:
  1. Created `ModalBottomBarView.kt`: Custom tactile 4-button bottom bar exclusively for modal views ([ABC], [Space], [⌫ Backspace], [↵ Enter]) honoring authentic HeliBoard 10dp radius keycaps, 1dp dark bottom bevel, repeat backspace handler, and responsive keypress haptics. Left the main keyboard's 5-button bottom bar intact.
  2. Built `ClipboardStorage.kt` providing local, zero-idle persistence for pinned clips and recent clips in SharedPreferences.
  3. Built `VianClipboardModalView.kt` with:
     - Speed Island editing toolbar replacing the suggestion bar: DPAD navigation arrows (Left, Right, Up, Down), divider, editing buttons (Select All, Cut, Copy, Paste), and Clear Unpinned history button.
     - 2-column pinned & recent snippet cards grid (`RecyclerView` + `GridLayoutManager(2)`).
     - Connected to `ModalBottomBarView`.
  4. Built `VianEmojiModalView.kt` with:
     - Horizontal pill-style category tab strip (replacing suggestion bar) with authentic HeliBoard category icons (Recent, Smileys, People, Animals, Food, Travel, Activities, Objects, Symbols, Flags).
     - Responsive emoji grid (`RecyclerView` + `GridLayoutManager(8)`).
     - Recent emojis tracker.
     - Connected to `ModalBottomBarView`.
  5. Integrated on-demand modal lifecycle in `VianBoardService.kt`:
     - Both modals are instantiated strictly on-demand as ephemeral overlays in `inputViewContainer` (FrameLayout) and removed entirely upon dismissal (`ABC` press, finishing input, or service destruction), preserving zero background footprint.
     - Linked `ToolbarTool.CLIPBOARD`, `ToolbarTool.EMOJI`, long-press Paste, and comma popup items ("Clipboard", "Emoji") to open the respective modals.
     - Enforced Security Scan Protocol: verified no `.keystore`, `.jks`, or `.p12` files remain committed in workspace.
- **How it was verified**: local build only (`compile_applet` passed with zero errors).
- **Deviation**: None. Followed user specification strictly: "Unified 4-Button Bottom Bar Component is only for clipboard and emoji modals."
- **Follow-up**: Ready for on-device manual QA testing.

### Entry: 2026-09-08T14:31:30-07:00
- **Summary**: Implemented auto clipboard capture during active IME sessions, word boundary selection on tap with Select All on long-press (both Android and desktop), and desktop physical keyboard shortcuts.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/receipts/RECEIPTS_004.md`
- **What was actually done**:
  1. Auto Clipboard Capture: Added `ClipboardManager.OnPrimaryClipChangedListener` to `VianBoardService`. Automatically registered on `onStartInputView()` and deregistered on `onFinishInputView()` and `onDestroy()`. Automatically captures new clips to `ClipboardStorage` while typing even when the clipboard modal is never opened. Also hooked immediate capture after copy/cut/paste actions.
  2. Select Word vs. Select All:
     - Short tap on `ToolbarTool.SELECT_WORD`: Executes algorithmic word-boundary scan via `getTextBeforeCursor`, `getTextAfterCursor`, and `getExtractedText` to select the exact word under the cursor, expanding to select all if already selected.
     - Long press on `ToolbarTool.SELECT_WORD`: Executes `performContextMenuAction(android.R.id.selectAll)` with Toast feedback.
  3. Desktop / Hardware Keyboard Integration:
     - Overrode `onKeyDown`, `onKeyUp`, and `onKeyLongPress` in `VianBoardService`.
     - `Ctrl+W`: Short tap selects word; holding down (or `Ctrl+Shift+W`) selects all.
     - `Ctrl+V`: Short tap pastes text; holding down (or `Ctrl+Shift+V`) opens the Clipboard Manager modal overlay.
     - `Ctrl+C` and `Ctrl+X`: Automatically captures copied/cut text into `ClipboardStorage`.
  4. Executed Credential Immunity Protocol: verified workspace clean, purged any build-generated `.keystore` files.
- **How it was verified**: local build only (`compile_applet` passed with zero errors).
- **Deviation**: None. Followed user specification and finalized discussion strictly.
- **Follow-up**: Ready for on-device and external keyboard QA verification.

### Entry: 2026-09-08T21:58:00-07:00
- **Summary**: Implemented Quick Notes module with separate zero-idle storage, 2-column staggered grid, compact on-demand long-press popups, translucent dialog editing activity, and Settings transfer mode option.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/ime/quicknotes/QuickNotesStorage.kt`
  - `/app/src/main/java/com/example/ime/quicknotes/QuickNotesCardsAdapter.kt`
  - `/app/src/main/java/com/example/ime/quicknotes/VianQuickNotesModalView.kt`
  - `/app/src/main/java/com/example/ime/quicknotes/QuickNoteEditActivity.kt`
  - `/app/src/main/java/com/example/ime/popup/CardLongPressPopup.kt`
  - `/app/src/main/java/com/example/ime/clipboard/ClipboardCardsAdapter.kt`
  - `/app/src/main/java/com/example/ime/clipboard/VianClipboardModalView.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/app/src/main/java/com/example/ime/settings/SettingsActivity.kt`
  - `/app/src/main/java/com/example/ime/settings/QuickNotesSettingsActivity.kt`
  - `/app/src/main/res/layout/item_quick_note_card.xml`
  - `/app/src/main/res/layout/view_quick_notes_modal.xml`
  - `/app/src/main/res/layout/view_card_longpress_popup.xml`
  - `/app/src/main/res/layout/dialog_quick_note_edit.xml`
  - `/app/src/main/res/layout/activity_quick_notes_settings.xml`
  - `/app/src/main/res/drawable/ic_add.xml`
  - `/app/src/main/res/drawable/ic_edit_pencil.xml`
  - `/app/src/main/AndroidManifest.xml`
  - `/receipts/RECEIPTS_004.md`
- **What was actually done**:
  1. QuickNotesStorage: Created dedicated zero-idle storage backed by private SharedPreferences (`vian_quick_notes_store`), completely detached from Android's `ClipboardManager`.
  2. Quick Notes Modal: Built `VianQuickNotesModalView` with Speed Island header (cursor navigation, `[ ➕ Add Note ]`, `[ ⬚ Select All ]`, `[ 📋 Paste to new note ]`, `[ 🗑️ Clear Notes ]`), 2-column vertical `StaggeredGridLayoutManager` for responsive multiline note cards (up to 6 lines, wrap_content height), and the Unified 4-Button Bottom Bar.
  3. Compact Long-Press Popups: Created `CardLongPressPopup` (anchored `PopupWindow` styled with HeliBoard elevation, card background, and vector icons).
     - In Clipboard modal: Long press card displays `[ 📌 Pin ]`, `[ 📝 To Notes ]`, and `[ 🗑️ Delete ]`.
     - In Quick Notes modal: Long press card displays `[ 📌 Pin ]`, `[ ✏️ Edit ]`, and `[ 🗑️ Delete ]`.
  4. Edit Dialog Activity: Implemented `QuickNoteEditActivity` with a translucent dialog theme. When opened, Android keeps the VianBoard IME active beneath the dialog so users can type into the note's `EditText`, with `[ Cancel ]` and `[ Save ]` actions.
  5. Settings Transfer Mode: Created `QuickNotesSettingsActivity` accessible from Settings -> Quick Notes, allowing users to configure whether sending items from clipboard executes a "Copy" (default, keeps in clipboard) or "Move" (cuts from clipboard).
  6. Service Integration: Connected `ToolbarTool.PROMPT_LIST` and long-press on `COPY` in `VianBoardService` to open the Quick Notes modal on-demand.
  7. Credential Immunity Protocol: verified workspace clean, purged any build-generated keystores.
- **How it was verified**: local build only (`compile_applet` passed with zero errors).
- **Deviation**: None. Followed user choices ("Option b. Copy. Implement") strictly.
- **Follow-up**: Ready for on-device manual QA testing.

---

### Entry: 2026-09-09T01:15:00-07:00
- **Summary**: Audited and confirmed CI build failure cause #29 (resource merger collision between .png and .webp mipmap icons), verified workspace sanitation, and verified local compilation for release export.
- **Exact Files Touched**:
  - `/receipts/RECEIPTS_004.md`
- **What was actually done**:
  1. Audited CI failure logs: Confirmed that `.github/workflows/build_apk.yml` workflow file was not the cause of the failure. The workflow successfully provisioned JDK 17, Gradle 9.3.1, generated the ephemeral keystore, and executed `gradle :app:assembleDebug --no-daemon`.
  2. Isolated root failure cause: Android Gradle Plugin resource merger threw `Duplicate resources` fatal errors because both `.png` and `.webp` versions of `ic_launcher` and `ic_launcher_round` were present in the remote repository's `mipmap-*` resource directories.
  3. Verified workspace resource sanitation: Confirmed that in the current repository workspace, all duplicate `.png` files have been excised and only singular `.webp` and adaptive XML definitions exist across all mipmap density buckets (`mipmap-hdpi`, `mipmap-mdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi`, `mipmap-xxxhdpi`, `mipmap-anydpi-v26`).
  4. Executed Credential Immunity & Security Scan: Confirmed zero `.keystore`, `.jks`, `.p12`, or exposed keys in the workspace; confirmed `.gitignore` contains all sensitive and artifact patterns.
  5. Verified compilation: Successfully executed `compile_applet` with zero errors.
- **How it was verified**: local build verified (`compile_applet` passed with zero errors).
- **Deviation**: None. Executed exact scope finalized during discussion.
- **Follow-up**: Repository is clean, verified, and ready for export to GitHub to run the next Actions build.

---

### Entry: 2026-09-09T14:02:00-07:00
- **Summary**: Implemented HeliBoard architectural and visual parity overhaul: Universal navigation bar inset system, functional key coloring for Comma & Period, typography & key proportions, suggestion strip borders/dividers with 3-dot auto-correct indicator, complete HeliBoard emoji assets migration with non-scrolling 10-tab header, and simplified 4-line cards with on-demand compact long-press popups.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/app/src/main/java/com/example/ime/keyboard/VianKeyboardView.kt`
  - `/app/src/main/java/com/example/ime/keyboard/KeyboardLayout.kt`
  - `/app/src/main/java/com/example/ime/keyboard/KeyboardTheme.kt`
  - `/app/src/main/java/com/example/ime/emoji/EmojiCategoryData.kt`
  - `/app/src/main/java/com/example/ime/emoji/VianEmojiModalView.kt`
  - `/app/src/main/res/layout/view_emoji_modal.xml`
  - `/app/src/main/res/layout/item_emoji_tab_pill.xml`
  - `/app/src/main/res/layout/item_clipboard_card.xml`
  - `/app/src/main/res/layout/item_quick_note_card.xml`
  - `/app/src/main/res/layout/view_card_longpress_popup.xml`
  - `/app/src/main/java/com/example/ime/clipboard/ClipboardCardsAdapter.kt`
  - `/app/src/main/java/com/example/ime/clipboard/VianClipboardModalView.kt`
  - `/app/src/main/java/com/example/ime/quicknotes/QuickNotesCardsAdapter.kt`
  - `/app/src/main/java/com/example/ime/quicknotes/VianQuickNotesModalView.kt`
  - `/app/src/main/assets/emoji/` (10 emoji text databases from HeliBoard)
  - `/app/src/main/res/drawable/` (HeliBoard vector drawables for categories, pin, edit, delete)
  - `/BLUEPRINT.md`
  - `/receipts/RECEIPTS_004.md`
- **What was actually done**:
  1. Universal Navigation Bar Inset System: Configured `VianBoardService` with `onConfigureWindow` edge-to-edge transparent navigation bar, `onComputeInsets` frame-level touchable region, and root `inputViewContainer` dynamic `ViewCompat.setOnApplyWindowInsetsListener` applying bottom navigation bar padding. Main keyboard layout and all on-demand modals (Clipboard, Quick Notes, Emoji) are permanently elevated above the system navigation bar with matching background continuity.
  2. Functional Key Coloring: Updated `KeyboardTheme.isActionKey` to classify `KeyType.COMMA` and `KeyType.PERIOD` as functional action keys, painting their backgrounds with `actionKeyColor` (`#CFD8DC`) and labels with `actionKeyTextColor` (`#37474F`), matching Shift, Delete, ?123, and Enter.
  3. Typography & Button Proportions: Upgraded `VianKeyboardView` paint typefaces to `sans-serif-medium` bold/medium, calibrated key corner radii (5dp), toolbar key spacing (3.5dp internal margin), and 18dp icon bounds to mirror HeliBoard's proportions.
  4. Suggestion Strip Dividers & Auto-Correct Dots: Implemented candidate vertical dividers, top/bottom borders, and the authentic HeliBoard 3-dot auto-correct indicator beneath the active prediction candidate.
  5. Emoji Library & Non-Scrolling Header: Migrated HeliBoard's complete set of 10 emoji asset files into `app/src/main/assets/emoji/`, implemented cached asset loading in `EmojiCategoryData`, replaced the horizontal scroll header with a fixed, evenly distributed 10-tab category strip (`layout_weight="1"`), and wired authentic category vector icons.
  6. Simplified Cards & Compact Long-Press Popups: Stripped out inline action buttons from `item_clipboard_card.xml` and `item_quick_note_card.xml`. Cards now feature clean multiline layouts (up to 4 lines with end ellipsis dots), a discreet corner pin badge, immediate commit on tap, and an on-demand compact popup menu on long press with authentic HeliBoard vector icons for Pin/Unpin, Note transfer/Edit, and Delete.
- **How it was verified**: Local build verified (`compile_applet` passed with zero errors).
- **Deviation**: None. Executed user instructions with strict fidelity.
- **Follow-up**: Verified clean build; ready for on-device testing.



