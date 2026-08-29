# Receipts Log (Part 2)

## Entry 041
- **Timestamp**: 2026-08-28T13:46:30-07:00
- **Summary**: Updated `BLUEPRINT.md` with toolbar tools (Go Right Up / Down), pinned right quick actions, popups over toolbar, and universal 4-button modal bottom bar.
- **Exact Files Touched**:
  - `/BLUEPRINT.md`
  - `/receipts/RECEIPTS_002.md`
- **What was actually done**:
  1. Updated `BLUEPRINT.md` to incorporate:
     - Toolbar Expanded Tools tray items including `[ ⤹ Go Right Up ]`, `[ ⤸ Go Right Down ]`, and placeholders for Security Vault, Prompt List, Voice Input, and One-Handed mode.
     - Pinned right-side toolbar quick actions (`[ ⬚ Select ] [ ⎘ Copy ] [ 📋 Clipboard ]`) with their tap vs. long-press bindings.
     - 5-minute temporary Incognito mode and HeliBoard-style chevron-bordered incognito indicator.
     - Popups layer rule: Single-key preview and 2-row MoreKeys popups for top rows (0 & 1) float over the 44dp toolbar strip without clipping.
     - Universal 4-button bottom bar for all modals (Emoji, Clipboard, Prompt List): `[ ABC ] [ ?123/Menu ] [ Spacebar ] [ ⌫ Backspace ]`.
  2. Began `/receipts/RECEIPTS_002.md` following the 500-line cap rule on `RECEIPTS_001.md`.
- **How it was verified**: Verified file structure and specification alignment.
- **Deviation**: None.
- **Follow-up**: Ready for implementation phase approval.

## Entry 043
- **Timestamp**: 2026-08-28T15:08:30-07:00
- **Summary**: Implemented authentic HeliBoard neutral theme, soft rectangular keycaps, floating KeyPopupOverlayView over toolbar, and expanded multi-row comma & key popup grids.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/keyboard/internal/KeyPopupOverlayView.kt`
  - `/app/src/main/java/com/example/keyboard/internal/MainKeyboardView.kt`
  - `/app/src/main/java/com/example/ime/data/MoreKeysSpecs.kt`
  - `/app/src/main/java/com/example/ime/SuggestionStripView.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/receipts/RECEIPTS_002.md`
- **What was actually done**:
  1. Created `KeyPopupOverlayView.kt` anchored directly to the root `FrameLayout`. Key previews and 2-row MoreKeys popups for Row 0 and Row 1 now float freely above the keyboard frame and seamlessly overlap the top suggestion strip/toolbar without clipping or finger occlusion.
  2. Redesigned `MainKeyboardView.kt` to match HeliBoard reference screenshots:
     - Soft rectangular keycaps with 6dp corner radius and tight 2.5dp margins.
     - Clean neutral off-white backdrop (`#E8ECEF`), pure white alpha keys (`#FFFFFF`), neutral slate-grey functional keys (`#DDE2E6`), and HeliBoard action enter key (`#78909C`).
     - Neutral dark charcoal primary labels (`#202124`) and neutral grey corner sub-labels (`#757575`).
  3. Updated `MoreKeysSpecs.kt` with the full 16-symbol 2-row grid for `A` and added the `😀` Emoji shortcut to the Comma key popup menu.
  4. Updated `SuggestionStripView.kt` with matching `#E8ECEF` background and `#202124` text colors.
  5. Stripped all Android navigation bar overrides from `VianBoardService.kt` to leave system window insets untouched.
  6. Verified zero-error compilation with `compile_applet`.
- **How it was verified**: Full zero-error compilation with `compile_applet`.
- **Deviation**: None.
- **Follow-up**: Ready for on-device manual QA.

## Entry 045
- **Timestamp**: 2026-08-29T01:57:15-07:00
- **Summary**: Stripped unused runtime dependencies, bypassed unneeded dictionary preloading, and deferred voice input engine to eliminate background resource consumption.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/app/build.gradle.kts`
  - `/receipts/RECEIPTS_002.md`
- **What was actually done**:
  1. Updated `VianBoardService.kt` to eliminate background dictionary preloading and voice speech recognizer initialization on service boot. Voice input is deferred to Phase 4.
  2. Streamlined text composition to direct zero-delay canvas input.
  3. Trimmed unused Room, Retrofit, OkHttp, and Firebase dependencies from `app/build.gradle.kts` to reduce APK footprint and ART DEX verification overhead.
  4. Verified full compilation with `compile_applet`.
- **How it was verified**: Full clean compilation with `compile_applet`.
- **Deviation**: None.
- **Follow-up**: Ready for on-device manual QA.

## Entry 046
- **Timestamp**: 2026-08-29T02:41:20-07:00
- **Summary**: Resolved stuck grey keys on rapid typing, removed tap popup bubbles, aligned Shift icon with HeliBoard chevron, and routed Comma popup monochrome icons directly to functional actions.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/keyboard/internal/PointerTracker.kt`
  - `/app/src/main/java/com/example/keyboard/internal/MainKeyboardView.kt`
  - `/app/src/main/java/com/example/keyboard/internal/KeyPopupOverlayView.kt`
  - `/app/src/main/java/com/example/ime/data/MoreKeysSpecs.kt`
  - `/app/src/main/java/com/example/ime/VianBoardService.kt`
  - `/app/src/main/res/drawable/ic_ime_shift.xml`
  - `/receipts/RECEIPTS_002.md`
- **What was actually done**:
  1. Updated `PointerTracker.kt` to reset all `isPressed` states on `ACTION_UP`, `ACTION_POINTER_UP`, and `ACTION_CANCEL`, preventing any keys from remaining grey during fast multi-touch typing.
  2. Removed instant key preview bubbles on regular key taps in `MainKeyboardView.kt` for a clean, distraction-free typing experience.
  3. Replaced `ic_ime_shift.xml` path with an authentic HeliBoard upward chevron arrow.
  4. Updated `MoreKeysSpecs.kt` to use clean monochrome glyphs for the comma long-press card (`☺`, `⚙`, `📋`, `🌐`, `⤢`, `🪵`).
  5. Updated `KeyPopupOverlayView.kt` with pure black glyph paints on neutral grey floating cards.
  6. Updated `VianBoardService.kt` `onMoreKeySelected` to properly route drag-and-drop actions to their functional destinations (Settings, Clipboard modal, Emoji modal, Language switch, etc.) rather than committing emoji text.
  7. Verified full compilation with `compile_applet`.
- **How it was verified**: Full clean compilation with `compile_applet`.
- **Deviation**: None.
- **Follow-up**: Ready for on-device manual QA.

## Entry 047
- **Timestamp**: 2026-08-29T02:57:30-07:00
- **Summary**: Implemented live in-browser Appearance & Layout Tester, updated Welcome Screen with clean list navigation, and organized hierarchical Settings pages.
- **Exact Files Touched**:
  - `/app/src/main/java/com/example/ui/AppearanceTesterScreen.kt`
  - `/app/src/main/java/com/example/onboarding/WelcomeOnboardingScreen.kt`
  - `/app/src/main/java/com/example/MainActivity.kt`
  - `/app/src/main/java/com/example/keyboard/internal/MainKeyboardView.kt`
  - `/app/src/main/java/com/example/keyboard/internal/KeyPopupOverlayView.kt`
  - `/receipts/RECEIPTS_002.md`
- **What was actually done**:
  1. Built `/app/src/main/java/com/example/ui/AppearanceTesterScreen.kt` providing interactive layout switching (QWERTY, Shifted, Symbols ?123, More Symbols =\\<, Numpad), live modal testing (Clipboard, Emoji, Vault placeholder, Prompts), and popup card previews without enabling system IME.
  2. Refactored `WelcomeOnboardingScreen.kt` to a clean list structure (Keyboard Setup, Appearance Tester, Settings, Log Keeper) without a FAB.
  3. Integrated `APPEARANCE_TESTER` into `MainActivity.kt` hierarchical sub-pages and root settings list.
  4. Added `currentKeys` getter and `isShowingMoreKeys()` helper for seamless component bridging in the interactive tester.
  5. Verified compilation using `compile_applet`.
- **How it was verified**: Full clean compilation with `compile_applet`.
- **Deviation**: None.
- **Follow-up**: Ready for testing and on-device manual QA.

## Entry 048
- **Timestamp**: 2026-08-29T07:48:30-07:00
- **Summary**: Synchronized platform metadata `name` with `res/values/strings.xml` `app_name` to ensure exact naming alignment across AI Studio and trigger emulator rebuild.
- **Exact Files Touched**:
  - `/metadata.json`
  - `/receipts/RECEIPTS_002.md`
- **What was actually done**:
  1. Updated `metadata.json` name from `Remix Remix Vian board` to `Vian Board` to match `res/values/strings.xml` (`<string name="app_name">Vian Board</string>`).
  2. Updated description to reflect the active keyboard capabilities.
- **How it was verified**: Direct verification and compilation check.
- **Deviation**: None.
- **Follow-up**: Triggered platform preview rebuild.
