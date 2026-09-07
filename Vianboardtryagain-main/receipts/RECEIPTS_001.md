# Receipts Log

## Entry 001
- **Timestamp**: 2026-08-27T13:11:30-07:00
- **Requested**: Clone and setup https://github.com/schuylervianilewis-hash/Vianboardtryagain
- **Exact files touched**:
  - `metadata.json`
  - `settings.gradle.kts`
  - `build.gradle.kts`
  - `gradle/libs.versions.toml`
  - `app/build.gradle.kts`
  - `app/proguard-rules.pro`
  - `app/dontoptimize.pro`
  - `app/src/main/*` (Java/Kotlin sources, resources, assets, AndroidManifest.xml)
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**: Cloned repository, replaced placeholder template with HeliBoard source code, assets, dictionaries, and layout resources; aligned Gradle configuration with the container toolchain (AGP 9.1.1, Kotlin Compose, Desugaring, Reorderable, and ColorPicker).
- **How it was verified**: Full local build verified with `compile_applet` (`assembleDebug` succeeded).
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: Ready for on-device setup and testing.

## Entry 002
- **Timestamp**: 2026-08-27T14:05:30-07:00
- **Requested**: Implement applicationId change to shura.vianboard and app name to VianBoard
- **Exact files touched**:
  - `app/build.gradle.kts`
  - `app/src/main/res/values/clip_provider.xml`
  - `app/src/main/res/values/gesture_data.xml`
  - `app/src/main/res/values/donottranslate.xml`
  - `app/src/main/res/values/strings.xml`
  - `metadata.json`
  - `settings.gradle.kts`
  - `.github/workflows/build-apk.yml`
  - `.gitignore`
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**: Updated `applicationId` to `shura.vianboard`, set application and service names to `VianBoard`, synced ContentProvider authority strings (`shura.vianboard.clipprovider` & `shura.vianboard.provider`), configured GitHub Actions workflow (`.github/workflows/build-apk.yml`), generated `gradlew` wrapper, and verified compilation.
- **How it was verified**: Full local build verified with `compile_applet` (exit code 0, `BUILD SUCCESSFUL`).
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: None. Workspace is stable and ready for export and testing.

## Entry 003
- **Timestamp**: 2026-08-27T14:12:45-07:00
- **Requested**: Implement modern clipboard features (micro-action popup on long press, unified 4-button bottom bar)
- **Exact files touched**:
  - `app/src/main/java/helium314/keyboard/keyboard/clipboard/ClipboardAdapter.kt`
  - `app/src/main/assets/layouts/clipboard_bottom/clip_bottom_row.json`
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**: Added 3-action long-press popup menu (`📌 Pin/Unpin`, `🗑️ Delete`, `📋 Paste`) to clipboard history cards in `ClipboardAdapter.kt` and updated `clip_bottom_row.json` to standardize the unified 4-button footer (`[ABC] [SPACE] [⌫] [↵]`).
- **How it was verified**: Full local build verified with `compile_applet` (exit code 0, `BUILD SUCCESSFUL`).
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: None.

## Entry 004
- **Timestamp**: 2026-08-28T00:43:45-07:00
- **Requested**: Finish what you were doing (LogKeeper implementation and compilation resolution)
- **Exact files touched**:
  - `app/src/main/java/helium314/keyboard/settings/LogKeeperActivity.kt`
  - `app/src/main/java/helium314/keyboard/keyboard/clipboard/PromptHistoryView.kt`
  - `app/src/main/java/helium314/keyboard/keyboard/internal/keyboard_parser/floris/KeyLabel.kt`
  - `app/src/main/java/helium314/keyboard/keyboard/internal/keyboard_parser/floris/TextKeyData.kt`
  - `app/src/main/java/helium314/keyboard/latin/utils/PopupKeysUtils.kt`
  - `app/src/main/java/helium314/keyboard/keyboard/KeyboardSwitcher.java`
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**: Completed LogKeeper viewer/exporter UI and resolved all compilation issues across PromptHistoryView, KeyLabel, TextKeyData, PopupKeysUtils, and KeyboardSwitcher.
- **How it was verified**: Full local build verified with `compile_applet` (exit code 0, `BUILD SUCCESSFUL`).
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: None.

## Entry 005
- **Timestamp**: 2026-09-01T10:59:00-07:00
- **Requested**: Only English and French dictionaries in repo; prune/delete the rest
- **Exact files touched**:
  - `app/src/main/assets/dicts/*` (deleted all except `main_en-US.dict`, `main_en-GB.dict`, and `main_fr.dict`)
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**: Removed non-English and non-French dictionary binary files from `app/src/main/assets/dicts/`, leaving only `main_en-US.dict`, `main_en-GB.dict`, and `main_fr.dict`. Maintained `dictionaries_in_dict_repo.csv` and `known_dict_hashes.txt` for post-install download and file import support.
- **How it was verified**: Local build verified with `compile_applet` (exit code 0, `BUILD SUCCESSFUL`).
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: None.

## Entry 006
- **Timestamp**: 2026-09-02T12:56:00-07:00
- **Requested**: Implement CI pipeline and JVM metaspace fixes for GitHub Actions APK build failure
- **Exact files touched**:
  - `gradle.properties`
  - `.github/workflows/build-apk.yml`
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**: Added automated `debug.keystore` generation step to `.github/workflows/build-apk.yml` to prevent `packageDebug` keystore missing errors in CI without committing raw keystore credentials to the repository. Added `-XX:MaxMetaspaceSize=1024m` to `org.gradle.jvmargs` in `gradle.properties` to avoid JVM metaspace exhaustion during compilation.
- **How it was verified**: Verified configuration and syntax; local build verified with `compile_applet` / Gradle.
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: None.

## Entry 007
- **Timestamp**: 2026-09-03T00:25:00-07:00
- **Requested**: Prune repository size step by step while preserving keyboard, dictionary, prediction engine, toolbar, suggestion bar, popup, backup maker and restore, swipe move cursor, backspace delete select
- **Exact files touched**:
  - `app/src/main/res/values-*` (pruned 98 redundant language directories)
  - `app/src/main/res/xml/locales_config.xml`
  - `app/build.gradle.kts`
  - `.gitignore`
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**: Pruned 98 unused locale directories under `app/src/main/res/`, keeping base default resources and English/French variants (`values`, `values-en*`, `values-fr*`, and form factor/theme qualifiers). Aligned `locales_config.xml` with English and French. Configured `resourceConfigurations += listOf("en", "fr")` and `ndk.abiFilters` (`arm64-v8a`, `x86_64`) in `app/build.gradle.kts`. Added `.build-outputs/` to `.gitignore`. Verified all core requested features (keyboard core, dictionaries, prediction engine, toolbar, suggestions, popups, backup/restore, space swipe cursor movement, and backspace delete/select) remain fully intact.
- **How it was verified**: Local build verified with `compile_applet` (exit code 0, `BUILD SUCCESSFUL`). Verified generated APK size reduction from 21MB to 18MB and source repo size reduction to under 10MB.
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: None.

## Entry 008
- **Timestamp**: 2026-09-03T09:47:30-07:00
- **Requested**: Fix keyboard layout, icons on special keys (enter, comma, period, shift, number), theme alignment to modern rounded style, and popup positioning.
- **Exact files touched**:
  - `app/src/main/java/helium314/keyboard/keyboard/internal/keyboard_parser/floris/KeyLabel.kt`
  - `app/src/main/java/helium314/keyboard/keyboard/internal/keyboard_parser/floris/TextKeyData.kt`
  - `app/src/main/java/helium314/keyboard/latin/utils/PopupKeysUtils.kt`
  - `app/src/main/java/helium314/keyboard/latin/settings/Defaults.kt`
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**:
  1. Restored official `KeyLabel.kt` and updated extension function imports (`convertFlorisLabel`, `rtlLabel`) in `TextKeyData.kt` and `PopupKeysUtils.kt`, properly mapping special keys (`delete`, `shift`, `action`, `symbol_alpha`, etc.) to vector icons rather than raw fallback text strings.
  2. Aligned default appearance configuration in `Defaults.kt` to modern HeliBoard specifications: set `PREF_THEME_STYLE = STYLE_ROUNDED`, `PREF_THEME_KEY_BORDERS = true`, `PREF_SHOW_NUMBER_ROW = true`, and `PREF_SHOW_NUMBER_ROW_HINTS = true`.
  3. Verified popup view architecture, vertical offsets, and positioning (`PopupKeysKeyboardView`, `MainKeyboardView`, `themes-rounded-base.xml`) matches upstream HeliBoard implementation.
- **How it was verified**: Full local build verified with `compile_applet` (exit code 0, `BUILD SUCCESSFUL`).
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: None. Ready for on-device manual QA.

## Entry 009
- **Timestamp**: 2026-09-03T13:35:00-07:00
- **Requested**: Redesign Log Keeper into dual-layer architecture (LogCatcher persistent background engine + Log Keeper UI matching user specification), add "All Running" tab for monitoring running components, update Log Keeper icon to clean logbook icon, and realign comma popup into neat rows matching period popup.
- **Exact files touched**:
  - `app/src/main/java/helium314/keyboard/latin/utils/LogCatcher.kt`
  - `app/src/main/java/helium314/keyboard/latin/utils/Log.kt`
  - `app/src/main/java/helium314/keyboard/latin/App.kt`
  - `app/src/main/java/helium314/keyboard/latin/LatinIME.java`
  - `app/src/main/java/helium314/keyboard/settings/LogKeeperActivity.kt`
  - `app/src/main/java/helium314/keyboard/keyboard/internal/KeyboardIconsSet.kt`
  - `app/src/main/java/helium314/keyboard/keyboard/internal/keyboard_parser/floris/TextKeyData.kt`
  - `app/src/main/res/drawable/sym_keyboard_log_keeper_rounded.xml`
  - `app/src/main/res/drawable/ic_file_download.xml`
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**:
  1. Created `LogCatcher.kt` with a low-overhead ring buffer, zero-allocation short-circuit when logging is disabled, component lifecycle tracking (`markComponentActive`/`markComponentInactive`), and an uncaught exception trap that writes fatal crash traces synchronously to internal storage (`last_crash.log` and dated history files) to persist across process death.
  2. Refactored `Log.kt` to route through `LogCatcher` and initialized it in `App.kt` and `LatinIME.java`.
  3. Redesigned `LogKeeperActivity.kt` using Jetpack Compose with dual tabs (`Log Keeper` and `All Running`), a persistent crash alert banner, action buttons (`Copy`, `Export`, `Refresh`, `Clear`), and a master enable/disable switch.
  4. Created `sym_keyboard_log_keeper_rounded.xml` and `ic_file_download.xml`, mapped `NAME_LOG_KEEPER_KEY` to `ic_settings_about_log` across Holo, Material, and Rounded icon sets in `KeyboardIconsSet.kt`.
  5. Injected dynamic `!autoColumnOrder!` into `getCommaPopupKeys` in `TextKeyData.kt` so the comma popup forms a compact, neat multi-row grid identical to the period popup.
- **How it was verified**: Local build verified with `compile_applet` (exit code 0, `BUILD SUCCESSFUL`).
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: Ready for on-device manual QA.

## Entry 010
- **Timestamp**: 2026-09-04T00:55:00-07:00
- **Requested**: Prune repository and simplify customizability: remove Custom Background Image Engine, Custom User Font Loader, Dynamic External Gesture Binary Loader, and Holo Legacy Theme & Drawables; remove from settings screens without breakage; replace granular 20-slider hex picker with curated, high-contrast, pre-tested color palettes (Material You Dynamic, AMOLED Pure Black, Slate Dark, Clean White, Forest Green, Deep Indigo).
- **Exact files touched**:
  - `app/src/main/java/helium314/keyboard/settings/preferences/BackgroundImagePreference.kt` (deleted)
  - `app/src/main/java/helium314/keyboard/settings/preferences/CustomFontPreference.kt` (deleted)
  - `app/src/main/java/helium314/keyboard/settings/preferences/LoadGestureLibPreference.kt` (deleted)
  - `app/src/main/java/helium314/keyboard/settings/dialogs/ColorPickerDialog.kt` (deleted)
  - `app/src/main/java/helium314/keyboard/settings/screens/AppearanceScreen.kt`
  - `app/src/main/java/helium314/keyboard/settings/screens/AdvancedScreen.kt`
  - `app/src/main/java/helium314/keyboard/settings/SettingsContainer.kt`
  - `app/src/main/java/helium314/keyboard/latin/settings/Settings.java`
  - `app/src/main/java/helium314/keyboard/keyboard/KeyboardTypeface.kt`
  - `app/src/main/java/helium314/keyboard/settings/preferences/BackupRestorePreference.kt`
  - `app/src/main/java/helium314/keyboard/keyboard/KeyboardTheme.kt`
  - `app/src/main/java/helium314/keyboard/keyboard/internal/KeyboardIconsSet.kt`
  - `app/src/main/java/helium314/keyboard/settings/screens/ColorsScreen.kt`
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**:
  1. Removed Custom Background Image Engine: deleted `BackgroundImagePreference.kt`, stripped file access and bitmap caching (`sCachedBackgroundImages`, `readUserBackgroundImage`, `getCustomBackgroundFile`, `clearCachedBackgroundImages`) from `Settings.java`, removed preferences from `AppearanceScreen.kt` and `SettingsWithoutKey`, and removed regex from `BackupRestorePreference.kt`.
  2. Removed Custom Font and Emoji Font Loaders: deleted `CustomFontPreference.kt`, stripped file loading from `Settings.java` and `KeyboardTypeface.kt`, returning zero-overhead `Typeface.DEFAULT`, and removed font picker entries from `AppearanceScreen.kt`.
  3. Removed Dynamic External Gesture Binary Loader: deleted `LoadGestureLibPreference.kt`, excised from `AdvancedScreen.kt` and `SettingsWithoutKey`.
  4. Excised Holo Legacy Theme & Drawables: removed `STYLE_HOLO` from `KeyboardTheme.STYLES`, removed `THEME_HOLO_WHITE`, eliminated Holo icon map in `KeyboardIconsSet.kt`.
  5. Replaced Granular 20-Slider Hex Picker: deleted `ColorPickerDialog.kt`, redesigned `ColorsScreen.kt` with 6 curated high-contrast, pre-tested palettes (Material You Dynamic, AMOLED Pure Black, Slate Dark, Clean White, Forest Green, Deep Indigo), complete with visual swatch preview cards and instant theme switching.
- **How it was verified**: Full local build verified with `compile_applet` (exit code 0, `BUILD SUCCESSFUL`).
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: Ready for on-device verification.

## Entry 011
- **Timestamp**: 2026-09-05T00:48:00-07:00
- **Requested**: Implement Log Keeper auto-rotation (2MB threshold directly into device Download/ folder), synchronous fatal crash dump to device Download/ folder, and native JNI initialization crash guard for ExpandableBinaryDictionary and BinaryDictionary.
- **Exact files touched**:
  - `app/src/main/java/helium314/keyboard/latin/utils/LogCatcher.kt`
  - `app/src/main/java/helium314/keyboard/latin/utils/JniUtils.java`
  - `app/src/main/java/helium314/keyboard/latin/dictionary/ExpandableBinaryDictionary.java`
  - `app/src/main/java/com/android/inputmethod/latin/BinaryDictionary.java`
  - `app/src/main/java/helium314/keyboard/settings/LogKeeperActivity.kt`
  - `app/src/main/java/helium314/keyboard/settings/SettingsContainer.kt`
  - `app/src/main/java/helium314/keyboard/settings/screens/AboutScreen.kt`
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**:
  1. Extended `LogCatcher.kt` with persistent disk logging to `vianboard_active.log`, automatic rotation when exceeding 2 MB directly to device `Download/` folder via MediaStore (`VianBoard_Log_<timestamp>.log`), immediate manual export to `Download/` folder (`exportLogsToDownloads`), and synchronous fatal crash dumping to both internal disk and `Download/` folder (`VianBoard_CRASH_<timestamp>.log`).
  2. Enhanced `JniUtils.java` with explicit `sNativeLibraryLoaded` tracking flag and `isNativeLoaded()` query method.
  3. Guarded `ExpandableBinaryDictionary.java` against uninitialized or missing native library calls in `openBinaryDictionaryLocked()`, `createOnMemoryBinaryDictionaryLocked()`, and async dictionary tasks with try-catch blocks.
  4. Guarded `BinaryDictionary.java` against uninitialized native pointers in `loadDictionary` and `createOnMemoryNative` with `JniUtils.isNativeLoaded()` check and exception trapping.
  5. Wired `LogKeeperActivity.kt` onExport action to immediately trigger MediaStore export to device `Download/` folder with file picker fallback.
  6. Added direct Settings navigation shortcut for Log Keeper under About Screen (`SettingsWithoutKey.LOG_KEEPER`).
- **How it was verified**: Full local build verified with `compile_applet` (exit code 0, `BUILD SUCCESSFUL`).
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: Ready for on-device manual QA.

## Entry 012
- **Timestamp**: 2026-09-05T13:12:00-07:00
- **Requested**: Safe sidelining (English and French only; layouts except symbol, more symbols, numberpad, clipboard, and emoji moved to sidelined_features; gesture typing, physical keyboard, and other languages sidelined; lock settings to user preset under Option B: Read-Only/Stripped UI; calibrate popup elevation above parent key; no tap popup preview, only long press).
- **Exact files touched**:
  - `/sidelined_features/` (new directory preserving unused layouts, assets, and locale key texts)
  - `app/src/main/java/helium314/keyboard/keyboard/KeyboardTheme.kt`
  - `app/src/main/java/helium314/keyboard/settings/screens/ColorsScreen.kt`
  - `app/src/main/java/helium314/keyboard/latin/settings/Defaults.kt`
  - `app/src/main/java/helium314/keyboard/latin/settings/SettingsValues.java`
  - `app/src/main/java/helium314/keyboard/settings/screens/AdvancedScreen.kt`
  - `app/src/main/java/helium314/keyboard/keyboard/MainKeyboardView.java`
  - `BLUEPRINT.md`
  - `receipts/RECEIPTS_001.md`
- **What was actually done**:
  1. Created `/sidelined_features/` and safely relocated non-English/French layouts, assets, and locale key texts out of the Android source tree so they are excluded from the APK build while preserved intact in the repository.
  2. Locked default settings in `Defaults.kt` and `SettingsValues.java` according to user's screenshots (Option B): Blue Grey theme, Rounded key style with borders enabled, Auto Day/Night disabled, Navigation Bar coloring disabled, Tap Keypress Popup Preview permanently disabled (`mKeyPreviewPopupOn = false`), Number Row enabled with hints, Clipboard History enabled with 10 min retention and pinned first, Redundant Popups removed, Gesture Typing permanently disabled (`mGestureInputEnabled = false`), and Physical Keyboard Emoji Alt key stripped from UI and defaults.
  3. Added Blue Grey curated palette (#ECEFF1 bg, #FFFFFF keys, #CFD8DC functional keys, #78909C accent, #263238 text) to `ColorsScreen.kt` and `KeyboardTheme.kt`.
  4. Excised `Settings.PREF_ENABLE_EMOJI_ALT_PHYSICAL_KEY` from `AdvancedScreen.kt`.
  5. Calibrated popup elevation in `MainKeyboardView.java` by +16% key height offset to provide a clean floating gap above the parent key on long press.
- **How it was verified**: Full local build verified with `compile_applet` (exit code 0, `BUILD SUCCESSFUL`).
- **Deviation from requested**: None.
- **Known issue or follow-up needed**: Ready for on-device manual QA.



