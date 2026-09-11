# Project Blueprint: VianBoard

## 1. Overview
VianBoard is a fully customizable, privacy-conscious offline Android keyboard application based on AOSP / OpenBoard / HeliBoard.
- **Application ID**: `shura.vianboard`
- **Namespace**: `helium314.keyboard.latin`
- **Architecture**: MVVM / Clean Android Architecture with LatinIME Service and Jetpack Compose for modern Settings UI.
- **Offline / Privacy Guarantee**: 100% offline, zero internet permissions.

## 2. Key Modules & Components
- **Core Input Method Service (`LatinIME`)**: Handles touch input, key layout generation, popup keys, gesture data handling, and input connection.
- **Settings & Preferences (`helium314.keyboard.settings`)**: Built with Jetpack Compose (Material 3), enabling theme customization, custom color pickers, key borders, custom layouts, dictionaries, gestures, clipboard manager, and spell checker.
- **Spell Checker (`AndroidSpellCheckerService`)**: AOSP-based offline spell checker service.
- **Clipboard Content Provider (`ClipboardContentProvider`)**: Internal clipboard history (`shura.vianboard.clipprovider`) and secure pasting.
- **Log Keeper / Diagnostics (`Log.kt`, `DebugFlags.kt`)**: Built-in crash and diagnostics logging with debug toggles and export capabilities.

## 3. Development & Migration Phases
- **Phase 1: Project Setup & Cloning**: Import source files, resources, assets, dictionaries, and layouts from repository.
- **Phase 2: Build & Dependency Alignment**: Configure Gradle DSL (`app/build.gradle.kts`, `settings.gradle.kts`, `libs.versions.toml`) with Kotlin 2.2.10, AGP 9.1.1, Compose BOM, Serialization, Desugaring, Reorderable, and ColorPicker.
- **Phase 3: Verification & Compilation**: Proactively compile via `compile_applet` and perform sanity tests.
- **Phase 4: Security & Integrity Audit**: Audit workspace for zero exposed credentials, gitignore checks, and clean artifact state.
- **Phase 5: Package & Identity Alignment**: Updated `applicationId` to `shura.vianboard`, app name to `VianBoard`, sync provider authorities, and added GitHub Actions APK CI workflow.
- **Phase 6: Modern Clipboard Engine**: Standardized 2-column responsive layout, quick-action micro-menu (Pin/Unpin, Delete, Paste), and unified 4-button footer (`[ABC] [SPACE] [⌫] [↵]`).
- **Phase 7: LogKeeper UI & Diagnostics**: Integrated privacy-focused LogKeeper activity (`LogKeeperActivity.kt`) with master toggle, category-based log filtering (Errors/Warnings/Fatal), on-device file export, and resolved all project compilation dependencies.
- **Phase 8: Dictionary Asset Optimization**: Pruned bundled dictionary assets to English (`main_en-US.dict`, `main_en-GB.dict`) and French (`main_fr.dict`) only, optimizing APK size while preserving post-install import and download capabilities for all other languages.
- **Phase 9: CI Pipeline & JVM Metaspace Optimization**: Configured automated debug keystore generation in GitHub Actions workflow to prevent CI packaging failures, and configured `-XX:MaxMetaspaceSize=1024m` in `gradle.properties` to prevent daemon heap exhaustion.
- **Phase 10: Repository & Resource Pruning**: Removed 98 redundant non-English/French localized string resource folders (`values-*`), updated `locales_config.xml`, added `resourceConfigurations += listOf("en", "fr")` and `ndk.abiFilters` (`arm64-v8a`, `x86_64`) in `app/build.gradle.kts`, added `.build-outputs/` to `.gitignore`, reducing res folder by 75% and source repo to <10MB.
- **Phase 11: Modern Layout, Icons & Theme Realignment**: Restored official `KeyLabel.kt` icon mapping for functional keys, resolved extension function imports in `TextKeyData.kt` and `PopupKeysUtils.kt`, aligned defaults to modern Rounded M3 style with key borders and number row, and verified popup view offsets.
- **Phase 12: Dual-Engine Log Keeper & Comma Popup Grid Realignment**:
  - Split logging into background persistent catcher (`LogCatcher.kt`) and Material 3 UI (`LogKeeperActivity.kt`).
  - Implemented disk-persisted crash interceptor (`last_crash.log`), thread-safe ring buffer, component lifecycle tracking ("All Running" tab), and 4-action bar (`Copy`, `Export`, `Refresh`, `Clear`).
  - Switched Log Keeper icon from generic settings cog to logbook vector icon (`sym_keyboard_log_keeper_rounded` / `ic_settings_about_log`).
  - Realigned comma popup keys layout with auto-column grid order (`!autoColumnOrder!`) to match the neat multi-row grid of the period key popup.
- **Phase 13: Repo Pruning & Curated Color Palettes**:
  - Excised Custom Background Image Engine (`BackgroundImagePreference.kt`, `Settings.readUserBackgroundImage`, bitmap cache).
  - Excised Custom User Font & Emoji Font Loader (`CustomFontPreference.kt`, `KeyboardTypeface` file loaders).
  - Excised Dynamic External Gesture Binary Loader (`LoadGestureLibPreference.kt`).
  - Excised Holo Legacy Theme & Drawables (`STYLE_HOLO`, `THEME_HOLO_WHITE`, `keyboardIconsHolo` map).
  - Replaced granular 20-slider hex picker with curated, pre-tested, high-contrast color palettes (Material You Dynamic, AMOLED Pure Black, Slate Dark, Clean White, Forest Green, Deep Indigo) with visual key swatches and one-tap selection.
- **Phase 14: Log Keeper Auto-Rotation & Native JNI Guard**:
  - Persistent active disk log (`vianboard_active.log`) with 2 MB threshold auto-rotation into device `Download/` folder.
  - Direct crash dump copy to device `Download/` folder (`VianBoard_CRASH_<timestamp>.log`) alongside internal storage cache.
  - Added direct Settings screen entry for Log Keeper under About screen (`SettingsWithoutKey.LOG_KEEPER`).
  - Hardened JNI native initialization guards (`JniUtils.isNativeLoaded()`) across `ExpandableBinaryDictionary` and `BinaryDictionary` to prevent hard SIGSEGV crashes if native binaries are unloaded or missing.
- **Phase 15: Safe Sidelining & Settings Preset Lockdown (Option B)**:
  - **Asset Sidelining**: Moved all non-English/French keyboard layouts and locale popup assets into root `/sidelined_features/` (excluded from APK build, preserved safely in repo).
  - **Locked Settings Presets**: Locked `Defaults.kt` and `SettingsValues.java` to the user's exact preset configuration: Blue Grey curated palette (#ECEFF1 bg, #FFFFFF keys, #CFD8DC funcs, #78909C accent), Rounded style with Key Borders enabled, Auto Day/Night disabled, Color Navigation Bar disabled, Popup on Keypress disabled (no tap popup preview, only long press), Number Row enabled with hints, Clipboard History enabled with 10 min retention and pinned items on top, Redundant Popups removed, Gesture input disabled, and Physical Keyboard Emoji Alt key stripped.
  - **Calibrated Popup Elevation**: Elevated long-press popup panels in `MainKeyboardView.java` by +16% key height offset to provide a clear, floating preview gap above the parent button.
  - **Physical Keyboard & Gesture Sidelining**: Excised physical keyboard emoji toggles from `AdvancedScreen.kt` and safely hardcoded gesture input to false to prevent runtime crashes.

- **Phase 16: Unisoc Native ABI Compatibility & Log Keeper 2-Tab Redesign**:
  - **Unisoc 32-bit ABI Support**: Restored `armeabi-v7a` to `app/build.gradle.kts` `abiFilters` alongside `arm64-v8a` and `x86_64`, enabling native binary execution on Unisoc devices running 32-bit userlands.
  - **CI Native Compilation**: Configured `.github/workflows/build-apk.yml` with automated `ndk-build` compilation step targeting `armeabi-v7a`, `arm64-v8a`, and `x86_64` prior to packaging the APK.
  - **Defensive Null Guards**: Hardened `ExpandableBinaryDictionary.java` against null `mBinaryDictionary` references across `isValidDictionaryLocked()`, `getFrequency()`, `runGCIfRequiredLocked()`, `addUnigramLocked()`, `addNgramEntryLocked()`, `loadBinaryDictionaryLocked()`, and `createNewDictionaryLocked()`, completely preventing NPE crashes during cursor updates, history tracking, and typing sessions.
  - **Non-Destructive Dictionary Loading**: Updated `DictionaryFactory.kt` to prevent `killDictionary(file)` from deleting valid dictionary cache files when the native library is not yet loaded.
  - **Log Keeper 2-Tab UI**: Redesigned `LogKeeperActivity.kt` to match the user's reference screenshot: top action bar with back navigation, bold title, Master Switch, Copy icon button, and Download/Export icon button; 2 tabs: **All Logs** and **Errors**; clean card layout with monospace timestamps, component tag badges, colored log level chips, and message text.

- **Phase 17: Emoji Bottom Row Enter Key, Prompt List (Quick Notes) Modal & 2-Minute Temporary Incognito**:
  - **Emoji Bottom Row Enter Key**: Switched bottom row in `EmojiPalettesView.java` to `KeyboardElement.CLIPBOARD_BOTTOM_ROW` (`[ABC] [Space] [⌫] [↵ / Action]`), giving the emoji modal the same enter/action key as the clipboard modal for instant message sending or line breaks.
  - **Prompt List (Quick Notes) Modal**: Fully self-contained local notes repository (`PromptDao.kt`, `PromptHistoryView.kt`) with SQLite persistence (`PROMPTS` table) disconnected from system clipboard listeners.
    - **Move to Prompt List**: Long-pressing any clipboard item offers `📌 Pin/Unpin`, `📥 Move to Prompt List`, and `🗑️ Delete`. Moving to prompt list safely stores the text in `PromptDao` and removes it from clipboard history.
    - **Prompt Card Interaction**: Tapping any prompt card directly pastes the text into the active field.
    - **Edit Dialog**: Long-pressing a prompt card offers `📌 Pin/Unpin`, `✏️ Edit`, and `🗑️ Delete`. Selecting `✏️ Edit` opens a full dialog with multiline `EditText`, `Cancel`, and `Save` buttons, with window tokens properly bound to the IME view for normal typing.
    - **Toolbar Long-Press**: Long-pressing the Copy toolbar button opens the Prompt List modal.
  - **2-Minute Temporary Incognito Mode**:
    - **Trigger**: Long-pressing the Incognito toolbar key triggers 2-minute temporary incognito mode (`KeyCode.INCOGNITO_TEMP_2MIN`, `TempIncognitoManager.kt`).
    - **Timer & Expiration**: Automatically turns off incognito mode after 120,000 ms with feedback Toast.
    - **Keyboard Close Override**: If the keyboard is closed or hidden at any point while temporary incognito is active, the timer is immediately cancelled and incognito is guaranteed turned off, so when reopened, incognito is OFF.

- **Phase 18: Layout Overhaul, Image 1 Symbol Mapping, French Popup Accents & 3-Page Settings Architecture**:
  - **Gradle Debug Fallback & Credential Immunity**: Refactored `app/build.gradle.kts` debug signing configuration to check `DEBUG_KEYSTORE_PATH` and dynamically register `customDebug` only if valid, falling back cleanly to the built-in Android debug keystore.
  - **Image 1 Symbol Top-Right Layout**: Rewrote `qwerty.txt` with exact symbol mapping:
    - Row 1: `Q(%)`, `W(/)`, `E(|)`, `R(=)`, `T([)`, `Y(])`, `U(*)`, `I(!)`, `O(-)`, `P(;)`
    - Row 2: `A(@)`, `S(#)`, `D($$$)`, `F(_)`, `G(&)`, `H(-)`, `J(+)`, `K(()`, `L())`
    - Row 3: `Z(*)`, `X(")`, `C(')`, `V(:)`, `B(;)`, `N(!)`, `M(?)`
  - **French Accents in Long-Press Popups**: Updated `more_popups_main.txt` to prioritize Latin French accents (`e: é è ê ë ē`, `a: à â æ á ä ã å ā`, `i: î ï í ì ī`, `o: ô œ ö ò ó õ ø ō`, `u: ù û ü ...`) immediately following key symbols.
  - **Predefined Layout Sidelining**: Restricted `predefined_layouts` in `donottranslate.xml` to `Default` (`qwerty`), moved `azerty.json` and `bepo.txt` into `/sidelined_features/layouts/main/`, and locked `LayoutPickerDialog.kt` to Default + 1 single customizable layout slot.
  - **Default Currency ₹**: Configured `Defaults.PREF_CUSTOM_CURRENCY_KEY = "₹"` mapping `$$$` on key `d` to Rupee by default.
  - **3-Parent-Page Settings Architecture**:
    - **Appearance** (`AppearanceScreen.kt`): Default layout editor, 1 customizable layout slot manager, Currencies quick switch (₹, $, €, ¥), Toolbar key editor, and Desktop Shortcuts modal. Uses `ic_settings_preferences` icon.
    - **Word Engine** (`WordEngineScreen.kt`): Houses Text Correction (`TextCorrectionScreen`) and Dictionaries (`DictionaryScreen`). Uses `ic_settings_correction` icon.
    - **Advanced** (`AdvancedSettingsScreen.kt`): Houses Backup & Restore (`BackupRestoreScreen`) and About (`AboutScreen`). Uses `ic_settings_advanced` icon.
    - **Backup & Restore Sub-Page** (`BackupRestoreScreen.kt`): Unified "Backup All", "Restore All", and backward-compatible "Import HeliBoard Backup" flows with detailed migration confirmation.
    - **About Updates**: Credited HeliBoard and AOSP in `donottranslate.xml` and About description.

## 4. Change Ledger
- **2026-08-27**: Cloned and imported complete source tree from `schuylervianilewis-hash/Vianboardtryagain`.
- **2026-08-27**: Configured Gradle 9.3.1 / AGP 9.1.1 toolchain, updated `metadata.json`, `settings.gradle.kts`, `gradle/libs.versions.toml`, and `app/build.gradle.kts`.
- **2026-08-27**: Updated `applicationId` to `shura.vianboard`, app name resources to `VianBoard`, synced authorities to `shura.vianboard.clipprovider`/`provider`, configured `.github/workflows/build-apk.yml`, generated `gradlew` wrapper, and verified compilation.
- **2026-08-27**: Enhanced modern clipboard modal with 3-action long-press menu (`Pin/Unpin`, `Delete`, `Paste`) and standardized unified 4-button bottom bar.
- **2026-08-28**: Completed `LogKeeperActivity`, resolved `KeyLabel`, `PromptHistoryView`, and `KeyboardSwitcher` compilation dependencies, verified green build via `compile_applet`.
- **2026-09-01**: Pruned non-English/French dictionary assets from `app/src/main/assets/dicts/`, verified build via `compile_applet`.
- **2026-09-02**: Added Ensure Debug Keystore step to `.github/workflows/build-apk.yml` and tuned `gradle.properties` JVM args with `-XX:MaxMetaspaceSize=1024m`.
- **2026-09-03**: Pruned 98 redundant `values-*` directories, configured `resourceConfigurations` and `ndk.abiFilters` in `app/build.gradle.kts`, aligned `locales_config.xml`, and added `.build-outputs/` to `.gitignore`.
- **2026-09-03**: Restored functional key icon mappings in `KeyLabel.kt`, updated `TextKeyData.kt` and `PopupKeysUtils.kt`, and set default theme style to modern Rounded with key borders and dedicated number row in `Defaults.kt`.
- **2026-09-03**: Implemented `LogCatcher` persistent engine + crash interceptor, redesigned `LogKeeperActivity` with `Log Keeper` and `All Running` tabs, updated icon to clean logbook vector, and realigned comma popup with `!autoColumnOrder!` grid formatting.
- **2026-09-04**: Removed Custom Background Image Engine, Custom Font Loaders, External Gesture Binary Loader, and Holo Legacy Theme; replaced granular 20-slider hex picker with 6 curated high-contrast pre-tested color palettes; verified clean compilation.
- **2026-09-05**: Added Log Keeper 2MB disk auto-rotation and fatal crash dumping directly to device `Download/` folder via MediaStore, guarded `ExpandableBinaryDictionary` and `BinaryDictionary` against uninitialized native JNI calls, and added Log Keeper entry to About screen settings.
- **2026-09-05**: Implemented Safe Sidelining (non-EN/FR layouts & assets moved to `/sidelined_features/`), locked settings presets (Blue Grey theme, rounded key borders, no tap popup preview, number row with hints, 10m clipboard history, redundant popups removed), calibrated floating popup elevation in `MainKeyboardView.java`, and stripped physical keyboard settings from `AdvancedScreen.kt`.
- **2026-09-06**: Restored `armeabi-v7a` ABI support for Unisoc hardware, added CI native library compilation in GitHub Actions workflow, guarded `ExpandableBinaryDictionary` against null dictionary pointer crashes, protected dictionary cache from deletion in `DictionaryFactory`, and redesigned Log Keeper UI into a 2-tab view (**All Logs** & **Errors**) matching the reference screenshot.
- **2026-09-06**: Added Enter / Action key to Emoji modal bottom row (`CLIPBOARD_BOTTOM_ROW`), integrated Prompt List (Quick Notes) with 3-action long press (`Pin`, `Edit`, `Delete`), edit dialog with multiline text field, `Move to Prompt List` clipboard action, Copy toolbar long-press invocation, and implemented 2-minute temporary incognito mode on Incognito toolbar long-press with auto-revert and keyboard-close cancel guarantee.
- **2026-09-07**: Remediated security scan findings: deleted root `debug.keystore` and `debug.keystore.base64`, sanitized `app/build.gradle.kts` by removing hardcoded plaintext keystore passwords and credentials, loading on-demand from environment variables or gitignored `local.properties`.
- **2026-09-08**: Implemented layout and settings architecture overhaul: baked Image 1 symbol mapping into `qwerty.txt`, added French Latin accents into `more_popups_main.txt`, set default currency key to `₹`, restricted layouts to Default + 1 customizable slot, streamlined main settings to 3 parent pages (Appearance, Word Engine, Advanced) with dedicated sub-pages for Backup & Restore, and verified complete compilation.

