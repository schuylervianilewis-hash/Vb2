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

## Entry 072
- **Timestamp**: 2026-09-03T02:42:00-07:00
- **Summary**: Implemented 2-row 10-item comma long-press popup menu (5 items per row) with 2D grid selection and placeholder actions.
- **Exact Files Touched**:
  - `/app/src/main/res/drawable/ic_log_keeper.xml`
  - `/app/src/main/res/drawable/ic_personal_vault.xml`
  - `/app/src/main/res/drawable/ic_one_hand.xml`
  - `/app/src/main/res/drawable/ic_floating_keyboard.xml`
  - `/app/src/main/java/com/example/ime/keyboard/KeyPopupWindow.kt`
  - `/app/src/main/java/com/example/ime/keyboard/VianKeyboardView.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/BLUEPRINT.md`
  - `/receipts/RECEIPTS_003.md`
- **What was actually done**:
  1. Created vector drawables for missing tools: Log Keeper (`ic_log_keeper`), Personal Vault (`ic_personal_vault`), One Hand (`ic_one_hand`), and Floating Keyboard (`ic_floating_keyboard`).
  2. Extended `KeyPopupWindow.kt` with `PopupMode.GRID` supporting configurable row and column counts (5 cols x 2 rows), 2D selection (`touchXOnScreen`, `touchYOnScreen`), icon tinting, and software shadow layer.
  3. Updated `VianKeyboardView.kt` to trigger the 2-row grid popup on comma long-press with 10 exact items: Settings, Emoji, Clipboard, Log Keeper, Desktop Shortcuts (Row 1); Voice Input, One Hand, Floating Keyboard, Personal Vault, Security Vault (Row 2). Added 2D coordinate tracking in `ACTION_MOVE` and action dispatch in `ACTION_UP`.
  4. Updated `VianBoardService.kt` with `handleCommaPopupAction(item)`: launches Settings, commits Emoji, pastes Clipboard, shows Log Keeper entry count, and displays "Coming soon" toasts for placeholder features.
  5. Updated `BLUEPRINT.md` interaction matrix to document the 10-item 2-row comma popup specification.
- **How it was verified**: Local build verified with `compile_applet` (Build succeeded).
- **Deviation**: None. Followed exact user specifications.
- **Follow-up**: Ready for on-device manual QA testing.

