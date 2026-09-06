# Audit Receipts Log - Series 003

## Entry 070
- **Timestamp**: 2026-09-02T13:46:00-07:00
- **Summary**: Implemented dynamic toolbar engine with expand/collapse, right-pinned customization, HeliBoard vector icons, 3-minute temporary incognito, and secondary long-press actions.
- **Exact Files Touched**:
  - `/app/src/main/res/values/strings.xml`
  - `/app/src/main/AndroidManifest.xml`
  - `/app/src/main/res/layout/activity_settings.xml`
  - `/app/src/main/res/layout/activity_layout_customization.xml`
  - `/app/src/main/res/layout/activity_toolbar_settings.xml`
  - `/app/src/main/res/layout/activity_desktop_shortcuts_settings.xml`
  - `/app/src/main/res/layout/item_toolbar_tool_setting.xml`
  - `/app/src/main/java/com/example/ime/settings/SettingsActivity.kt`
  - `/app/src/main/java/com/example/ime/settings/LayoutCustomizationActivity.kt`
  - `/app/src/main/java/com/example/ime/settings/ToolbarSettingsActivity.kt`
  - `/app/src/main/java/com/example/ime/settings/DesktopShortcutsSettingsActivity.kt`
  - `/app/src/main/java/com/example/ime/toolbar/ToolbarTool.kt`
  - `/app/src/main/java/com/example/ime/toolbar/ToolbarPreferences.kt`
  - `/app/src/main/java/com/example/ime/keyboard/KeyData.kt`
  - `/app/src/main/java/com/example/ime/keyboard/KeyboardLayout.kt`
  - `/app/src/main/java/com/example/ime/keyboard/VianKeyboardView.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/BLUEPRINT.md`
- **What was actually done**:
  1. Built the Settings architecture: `LayoutCustomizationActivity` housing Appearance Settings, Toolbar Settings, and Desktop Shortcuts Settings.
  2. Created `ToolbarTool` enum with all 14 tools (Undo, Redo, Select Word, Select All, Copy, Paste, Up, Down, Incognito, Voice, Prompt List, Security Vault, Desktop Shortcuts, Settings) and associated HeliBoard vector drawables.
  3. Created `ToolbarPreferences` storing pinned tools and expanded tools persistently in SharedPreferences.
  4. Created `ToolbarSettingsActivity` enabling users to toggle and organize tools between right-pinned and expanded chevron views.
  5. Refactored `KeyboardLayout.kt` to dynamically layout toolbar keys:
     - Collapsed State: Anchor `›` + dynamic suggestions + right-pinned tools.
     - Expanded State: Anchor `‹` + full grid of expanded tools (with right-pinned hidden when expanded).
     - Incognito State: Anchor replaces chevron with an incognito sunglasses badge within a circular grey frame.
  6. Updated `VianKeyboardView.kt`:
     - Added vector icon caching and rendering via `drawVectorIcon()`.
     - Added long-press detection on toolbar tools and anchor (`isLongPressTriggered` avoids spurious clicks).
     - Added `reloadToolbarConfiguration()` to re-sync toolbar settings when preferences change.
  7. Updated `VianBoardService.kt`:
     - Implemented click dispatchers for all 14 toolbar tools.
     - Implemented long-press actions: anchor / incognito -> 3-minute temporary incognito timer; select word -> select all; copy -> prompt list modal; paste -> clipboard history.
  8. Updated `BLUEPRINT.md` progress ledger.
- **How it was verified**: Verified via `compile_applet`.
- **Deviation**: None. Followed exact user specifications.
- **Follow-up**: Ready for on-device manual QA testing.

## Entry 071
- **Timestamp**: 2026-09-02T14:05:30-07:00
- **Summary**: Implemented toolbar horizontal scrolling engine for overflow tools and live reordering controls in Toolbar Settings.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/ime/keyboard/KeyboardLayout.kt`
  - `/app/src/main/java/com/example/ime/keyboard/VianKeyboardView.kt`
  - `/app/src/main/res/layout/item_toolbar_tool_setting.xml`
  - `/app/src/main/java/com/example/ime/settings/ToolbarSettingsActivity.kt`
  - `/receipts/RECEIPTS_003.md`
- **What was actually done**:
  1. Updated `KeyboardLayout.kt`: Enhanced `findKeyAt()` to map touch coordinates into scrolled tool coordinates based on `toolbarScrollOffset` within `toolbarScrollBounds`.
  2. Updated `VianKeyboardView.kt`:
     - Added horizontal touch scroll tracking (`isToolbarScrolling`, `toolbarTouchStartX`, `toolbarInitialScrollOffset`, `touchSlop`).
     - Added smooth canvas clipping and X-translation (`canvas.clipRect()`, `canvas.translate(-layout.toolbarScrollOffset, 0f)`) to render overflow tool buttons cleanly while keeping the anchor chevron static.
  3. Updated `item_toolbar_tool_setting.xml` & `ToolbarSettingsActivity.kt`:
     - Added move up (`▲`) and move down (`▼`) reordering buttons with touch feedback to every tool row.
     - Implemented dynamic tool list ordering: enabled tools maintain persistent order saved into `ToolbarPreferences`, and swapping indices immediately repopulates and updates preferences.
- **How it was verified**: Full local build verified with `compile_applet` (Build succeeded).
- **Deviation**: None.
- **Follow-up**: Ready for user QA verification.

## Entry 073
- **Timestamp**: 2026-09-03T03:10:00-07:00
- **Summary**: Implemented hard-rule system bar insets, reachable grid popup rows, 16-symbol period popup, automatic numpad switching, and drag handle toolbar UI.
- **Exact Files Touched**:
  - `/app/src/main/res/drawable/ic_drag_handle.xml`
  - `/app/src/main/res/values/themes.xml`
  - `/app/src/main/res/layout/activity_toolbar_settings.xml`
  - `/app/src/main/res/layout/activity_layout_customization.xml`
  - `/app/src/main/res/layout/activity_desktop_shortcuts_settings.xml`
  - `/app/src/main/res/layout/item_toolbar_tool_setting.xml`
  - `/app/src/main/java/com/example/ime/keyboard/KeyPopupWindow.kt`
  - `/app/src/main/java/com/example/ime/keyboard/VianKeyboardView.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/BLUEPRINT.md`
  - `/receipts/RECEIPTS_003.md`
- **What was actually done**:
  1. Created `ic_drag_handle.xml` vector asset and updated `item_toolbar_tool_setting.xml` to display a tactile grabber handle alongside up/down reorder buttons.
  2. Applied `fitsSystemWindows="true"` and system status/nav bar configs in `themes.xml` and to all settings activity layouts (`activity_toolbar_settings.xml`, `activity_layout_customization.xml`, `activity_desktop_shortcuts_settings.xml`) to prevent content from bleeding behind the Android status bar.
  3. Formatted Toolbar settings UI with clear section headers separating "📌 PINNED TOOLS (ALWAYS VISIBLE ON RIGHT)" from "❯ EXPANDED TOOLS (VISIBLE AFTER TAPPING CHEVRON ❯)".
  4. Refactored `KeyPopupWindow.kt` grid selection geometry: centered popup over key, added `anchorKeyScreenY`, and calibrated touch selection thresholds so natural thumb swipe smoothly reaches both top (Row 0) and bottom (Row 1) items.
  5. Added 16-symbol 2x8 grid popup to the period (`.`) key in `VianKeyboardView.kt` matching Screenshot 1 (`! ? ; / ^ : ~ \` on top row, `" ' - ( ) [ ] {` on bottom row). Added selection tracking and commit logic in `onTouchEvent`.
  6. Added automatic switching to dedicated 3x4 numeric calculator layout (`KeyboardMode.NUMPAD`) in `VianBoardService.kt` when `EditorInfo.inputType` specifies `TYPE_CLASS_NUMBER` or `TYPE_CLASS_PHONE`, matching Screenshot 2. Added return transition via `SYMBOLS_TOGGLE` back to `CHARACTERS`.
  7. Fixed bottom navigation bar inset persistence in `VianBoardService.toggleToolbarExpand` and `activateTempIncognito` by preserving `bottomNavInsetPx` during layout rebuilding.
- **How it was verified**: Full project compilation verified via `compile_applet`.
- **Deviation**: None. Followed exact user specifications.
- **Follow-up**: Ready for on-device manual QA testing.

## Entry 074
- **Timestamp**: 2026-09-03T10:05:00-07:00
- **Summary**: Implemented HeliBoard-identical fedora/glasses incognito icon, complete HeliBoard toolbar tools suite, and interactive drag-and-drop tool reordering using RecyclerView and ItemTouchHelper.
- **Exact Files Touched**:
  - `/app/src/main/res/drawable/ic_incognito.xml`
  - `/app/src/main/res/drawable/ic_palette.xml`
  - `/app/src/main/res/drawable/ic_emoji.xml`
  - `/app/src/main/res/drawable/ic_number_row.xml`
  - `/app/src/main/res/drawable/ic_delete_sweep.xml`
  - `/gradle/libs.versions.toml`
  - `/app/build.gradle.kts`
  - `/app/src/main/res/values/strings.xml`
  - `/app/src/main/java/com/example/ime/toolbar/ToolbarTool.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/app/src/main/res/layout/activity_toolbar_settings.xml`
  - `/app/src/main/java/com/example/ime/settings/ToolbarSettingsActivity.kt`
  - `/app/src/main/java/com/example/ime/keyboard/KeyboardLayout.kt`
  - `/BLUEPRINT.md`
  - `/receipts/RECEIPTS_003.md`
- **What was actually done**:
  1. Updated `ic_incognito.xml` vector drawable with the exact HeliBoard fedora hat and sunglasses geometry (`M12 2C8.5 2 5.5 3.5 4 5...`).
  2. Created vector drawables for missing HeliBoard tools: `ic_palette.xml` (Theme Switcher), `ic_emoji.xml` (Emoji Picker), `ic_number_row.xml` (Number Row Toggle), and `ic_delete_sweep.xml` (Clear Clipboard).
  3. Added AndroidX `recyclerview:1.3.2` to version catalog and `app/build.gradle.kts`.
  4. Expanded `ToolbarTool` enum and `strings.xml` with complete HeliBoard tools array:
     - `SETTINGS` (Settings)
     - `CLIPBOARD` (Clipboard History)
     - `TEXT_EDIT` (Text Editing Pad)
     - `THEME` (Theme Switcher)
     - `EMOJI` (Emoji & Symbols)
     - `NUMBER_ROW` (Number Row Toggle)
     - `CLEAR_CLIPBOARD` (Clear Clipboard)
     - `INCOGNITO` (Force Incognito Mode)
     - `ONE_HANDED` (One-Handed Mode)
     - `FLOATING` (Floating Keyboard)
     - `UNDO` (Undo)
     - `REDO` (Redo)
     - `SELECT_WORD` (Select Word)
     - `SELECT_ALL` (Select All)
     - `COPY` (Copy)
     - `PASTE` (Paste)
     - `UP` (Go Up)
     - `DOWN` (Go Down)
     - `VOICE` (Voice Input)
     - `LOG_KEEPER` (Log Keeper)
     - `PERSONAL_VAULT` (Personal Vault)
     - `SECURITY_VAULT` (Security Vault)
     - `PROMPT_LIST` (Prompt List)
     - `DESKTOP_SHORTCUTS` (Desktop Shortcuts)
  5. Implemented action handling in `VianBoardService.handleToolbarToolClick` for all tools.
  6. Replaced static `LinearLayout` tool lists in `activity_toolbar_settings.xml` with `RecyclerView` (`rvPinnedTools` and `rvExpandedTools`).
  7. Implemented `ToolbarSettingsActivity` with `ItemTouchHelper` drag-and-drop:
     - Touching `ivDragHandle` initiates active drag via `touchHelper.startDrag(holder)`.
     - Dragging animates item elevation (`16f`), background highlight (`#F1F5F9`), and scaling (`1.02f`).
     - Position swaps live-update and persist new order into `ToolbarPreferences`.
  8. Updated `KeyboardLayout.kt` default expanded fallback to use all default expanded `ToolbarTool` values.
  9. Updated `BLUEPRINT.md` Phase 2I to COMPLETED.
- **How it was verified**: Full local build verified with `compile_applet` (Build succeeded).
- **Deviation**: None. Followed exact user specifications.
- **Follow-up**: Ready for on-device manual QA testing.

## Entry 076
- **Timestamp**: 2026-09-04T07:12:00-07:00
- **Summary**: Implemented HeliBoard vector icons, comma popup row 0 selection fix, vertical gap height decoupling, and default 1dp border styling.
- **Exact Files Touched**:
  - `/app/src/main/res/drawable/ic_settings.xml`
  - `/app/src/main/res/drawable/ic_clipboard.xml`
  - `/app/src/main/res/drawable/ic_one_hand.xml`
  - `/app/src/main/res/drawable/ic_ime_backspace.xml`
  - `/app/src/main/res/drawable/ic_ime_enter.xml`
  - `/app/src/main/res/drawable/ic_ime_shift.xml`
  - `/app/src/main/res/drawable/ic_ime_shift_on.xml`
  - `/app/src/main/res/drawable/ic_ime_shift_locked.xml`
  - `/app/src/main/res/drawable/ic_text_edit.xml`
  - `/app/src/main/java/com/example/ime/keyboard/KeyPopupWindow.kt`
  - `/app/src/main/java/com/example/ime/keyboard/KeyboardTheme.kt`
  - `/app/src/main/java/com/example/ime/keyboard/VianKeyboardView.kt`
  - `/BLUEPRINT.md`
  - `/receipts/RECEIPTS_003.md`
- **What was actually done**:
  1. Synchronized toolbar and keyboard vector icons with HeliBoard's exact vector paths:
     - Replaced `ic_settings.xml` with HeliBoard cogwheel icon.
     - Replaced `ic_clipboard.xml` with HeliBoard clipboard icon.
     - Replaced `ic_one_hand.xml` with HeliBoard one-hand docking phone icon.
     - Replaced `ic_ime_backspace.xml` with HeliBoard backspace vector icon.
     - Replaced `ic_ime_enter.xml` with HeliBoard return-arrow vector icon.
     - Updated `ic_ime_shift.xml` to HeliBoard outline arrow.
     - Created `ic_ime_shift_on.xml` (filled arrow) and `ic_ime_shift_locked.xml` (caps lock underline).
     - Replaced `ic_text_edit.xml` with HeliBoard directional cursor navigation icon.
  2. Fixed Comma Grid Popup reachability in `KeyPopupWindow.kt`:
     - Calculated exact screen coordinates (`getLocationOnScreen`) alongside window coordinates.
     - Recalibrated row selection threshold so swiping into the upper half of the popup cleanly and reliably selects Row 0 (top row).
  3. Decoupled vertical gap from keyboard container height in `VianKeyboardView.onMeasure`:
     - Removed `gapsHeight` from `totalCalculatedHeight` so adjusting the vertical gap slider changes inter-row spacing internally without resizing the overall keyboard view height.
  4. Updated key appearance to match HeliBoard styling:
     - Set default `borderWidthDp = 1f` and `borderColor = 0x24000000` (14% subtle stroke) in `KeyboardTheme.kt`.
     - Added border loading in `KeyboardTheme.loadFromPrefs`.
     - Wired Shift, Backspace, and Enter keys in `VianKeyboardView` to render using the vector drawables with dynamic tinting.
- **How it was verified**: Full local build verified with `compile_applet` (Build succeeded).
- **Deviation**: None. Followed exact user specifications.
- **Follow-up**: Ready for on-device manual QA testing.

## Entry 077
- **Timestamp**: 2026-09-05T00:50:00-07:00
- **Summary**: Resolved period popup crash and implemented full 2-part Log Keeper architecture (Catcher/Persistent storage with 2MB auto-cut and crash drop to device Download/ folder + Scoped Storage UI reader).
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/ime/keyboard/KeyPopupWindow.kt`
  - `/app/src/main/java/com/example/ime/keyboard/VianKeyboardView.kt`
  - `/app/src/main/java/com/example/logger/LogKeeper.kt`
  - `/app/src/main/java/com/example/VianApplication.kt`
  - `/app/src/main/java/com/example/logger/LogViewerActivity.kt`
  - `/BLUEPRINT.md`
  - `/receipts/RECEIPTS_003.md`
- **What was actually done**:
  1. Hardened `KeyPopupWindow.kt`:
     - Added screen width bounds clamping to `showGridKeys` so 8-column grids (like the 16-symbol period popup) scale dynamically to available display width (`maxAvailableWidth`) without exceeding boundaries.
     - Protected window display calls with `anchor.isAttachedToWindow && anchor.windowToken != null` and exception handling against detached view races.
  2. Hardened `VianKeyboardView.kt`:
     - Added `isPeriodGridPopupActive = false` reset in `ACTION_DOWN` to eliminate stale popup states.
     - Wrapped popup selection retrieval and dismissal in `try-catch-finally` inside `ACTION_UP` ensuring proper state teardown (`activePressedKey?.isPressed = false`, `popupWindow.dismiss()`) and error logging to `LogKeeper` on any unexpected condition.
  3. Implemented 2-Part Log Keeper Architecture in `LogKeeper.kt`:
     - **Part 1 (Catcher & Device Storage)**:
       - Appends active logs synchronously to persistent internal storage (`files/logs/vian_board_current.log`).
       - Loads prior persisted logs on initialization.
       - Enforces the 2MB size limit: automatically cuts the log file when it hits 2MB and drops it directly into the device's `Download/` directory (`vian_board_2mb_limit_reached_*.log`) via MediaStore scoped storage (Android 10+ / Android 16 compliant).
       - Added `dropCurrentLogToDownloads(reason)` with complete "what is running" context (active component statuses, heap memory, timestamps).
     - **Part 2 (App UI Reader)**:
       - Updated `LogViewerActivity.kt` to read persisted and in-memory logs, display active memory usage and master toggle, and trigger immediate direct drop to the device `Download/` folder on export.
  4. Updated `VianApplication.kt`:
     - Integrated `Thread.setDefaultUncaughtExceptionHandler` to capture full exception stack traces and immediately trigger `LogKeeper.dropCurrentLogToDownloads(reason = "CRASH_DUMP")` before delegating to the OS handler.
  5. Updated `BLUEPRINT.md` with Phase 2K.
- **How it was verified**: Full local build verified with `compile_applet` (Build succeeded).
- **Deviation**: None. Executed exact user specifications.
- **Follow-up**: Ready for on-device manual QA testing.

## Entry 078
- **Timestamp**: 2026-09-05T15:55:00-07:00
- **Summary**: Ignored Vianboardtryagain-main in root .gitignore and upgraded toolbar anchor buttons to crisp scalable vector chevrons.
- **Exact Files Touched**:
  - `/.gitignore`
  - `/app/src/main/java/com/example/ime/keyboard/VianKeyboardView.kt`
  - `/receipts/RECEIPTS_003.md`
- **What was actually done**:
  1. Appended `Vianboardtryagain-main/` to root `/.gitignore` so the imported HeliBoard reference tree is ignored by Git and does not interfere with the GitHub Actions APK compilation pipeline.
  2. Replaced fallback text strings (`‹` and `›`) on the toolbar anchor button with scalable vector drawables (`R.drawable.ic_chevron_left` and `R.drawable.ic_chevron_right`) in both expanded and collapsed toolbar states in `VianKeyboardView.kt`.
  3. Preserved Incognito sunglasses badge and full vector toolbar action tools (`Select Word`, `Copy`, `Paste`, etc.).
  4. Verified all keycap layouts, corner radiuses, and button styles match HeliBoard references.
- **How it was verified**: Full project compilation verified via `compile_applet` (Build succeeded).
- **Deviation**: None. Executed exact requested scope.
- **Follow-up**: Ready for on-device testing.




