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

