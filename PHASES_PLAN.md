# Vian Board — Phased Implementation Plan

This document establishes the official 8-Phase implementation roadmap for transforming Vian Board into the multi-modal power keyboard defined in `/OUTLINE.md`.

---

## Strategic Overview & Non-Negotiable Rules

1. **Working Layouts Are Untouched**:
   - Alpha, Symbols, More Symbols, and Number Pad layouts are already built and fully functional. Their layouts and dedicated bottom rows are preserved as-is.
2. **Universal Bottom-4 Anchor Isolation**:
   - **Applied ONLY to**: Desktop Shortcuts Modal, Clipboard Modal, Prompt List Modal, Emoji Modal, and Security Vault Chosen View.
   - **Never applied to**: Alpha, Symbols, More Symbols, Number Pad, Pattern Unlock, or Vault List.
3. **Modular On-Demand Sub-Renderers**:
   - Canvas drawing for modals is strictly modularized into dedicated sub-renderers, loaded on demand only when the modal is active.
4. **Pattern Unlock Gateway**:
   - Acts as an air-gapped authentication gate for Personal Vault and Security Vault.
   - Full keyboard height viewport: **No toolbar, no suggestion bar**.
   - Zero pattern geometry logged; Log Keeper receives only `SUCCESS` or `FAILURE`.
5. **Heavier Modules Deferred to End**:
   - Full `.kdbx` Security Vault, Personal Vault, HeliBoard Dictionary/Prediction Engine, and isolated `:voice` process are built in the final phases.

---

## 🗺️ The 8 Implementation Phases

```
┌────────────────────────────────────────────────────────────────────────┐
│ PHASE 1: Settings Restructure & Layout Customization                   │
│          • Group "Appearance" & "Desktop Shortcuts" into               │
│            "Layout Customization"                                      │
│          • Build Desktop Shortcuts Configuration Page (5–7 buttons)    │
│          • Add "Toolbar Customization" placeholder                     │
├────────────────────────────────────────────────────────────────────────┤
│ PHASE 2: Interactive Toolbar & Ingress Engine                          │
│          • Modular Toolbar at top of keyboard                          │
│          • Icon slots: [ > Expand/Collapse ] [ 📋 Clipboard ]          │
│            [ 📝 Prompts ] [ 💻 Desktop Shortcuts ] [ 🎙️ Voice ]         │
│            [ ⚙️ Settings ]                                             │
│          • Seamless toggle: Toolbar Icons <-> Word Suggestions Bar     │
│          • Viewport modal switching infrastructure                     │
├────────────────────────────────────────────────────────────────────────┤
│ PHASE 3: Desktop Shortcuts Modal                                       │
│          • Triggered via Toolbar [ 💻 ] icon                           │
│          • Dedicated on-demand DesktopShortcutsRenderer                │
│          • Middle 5–7 fat buttons + Nav arrows + Universal Bottom 4    │
├────────────────────────────────────────────────────────────────────────┤
│ PHASE 4: Clipboard & Prompt List Modals + Edit Dialog                  │
│          • Triggered via Toolbar [ 📋 ] and [ 📝 ] icons               │
│          • Clipboard Modal matching screenshot (2-column cards, pin,   │
│            top edit row, Universal Bottom 4)                           │
│          • Prompt List Modal + Centered themed Android Edit Dialog     │
│          • Dedicated "Prompt List" page in Settings                    │
├────────────────────────────────────────────────────────────────────────┤
│ PHASE 5: Emoji Modal                                                   │
│          • Triggered via toolbar or dedicated key                      │
│          • HeliBoard-style categories + Universal Bottom 4             │
├────────────────────────────────────────────────────────────────────────┤
│ PHASE 6: In-Keyboard Pattern Unlock (The Gateway)                      │
│          • Air-gapped 3x3 grid, full viewport (NO toolbar/suggestions) │
│          • Protected Security item in Settings (phone unlock required) │
│          • Pure auth state: provides unlock session (3m / 5m)          │
│          • Log Keeper privacy: only logs SUCCESS or FAILURE            │
├────────────────────────────────────────────────────────────────────────┤
│ PHASE 7: Personal Vault & Security Vault (.kdbx Chosen View)           │
│          • Connected to Phase 6 Pattern Unlock gateway                 │
│          • Personal Vault: masked pills (j•••e) in suggestions         │
│          • Security Vault: long-press ?123 -> unlock -> Chosen View    │
│            (Title, Lock, Back, Username, Password, TOTP spinner,       │
│             Attachments popup, 1234 keypad, Universal Bottom 4)        │
├────────────────────────────────────────────────────────────────────────┤
│ PHASE 8: HeliBoard Dictionary, Prediction Engine & Voice (:voice)      │
│          • Main process binary mmap dictionary & n-gram prediction     │
│          • Isolated :voice sub-process with visual pulse modal         │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Detailed Phase Specifications

### 🟢 Phase 1: Settings Restructure & Layout Customization
* **Settings Architecture**:
  - Restructure Settings menu items.
  - Create **"Layout Customization"** category:
    - **Appearance Settings**: Existing key height, themes, grayness, corners.
    - **Desktop Shortcuts Settings**: Dedicated configuration page.
* **Desktop Shortcuts Configuration Page**:
  - Multi-select or orderable selector for the **5 to 7 fat buttons**:
    - `Select All` (`Ctrl+A`)
    - `Copy` (`Ctrl+C`)
    - `Cut` (`Ctrl+X`)
    - `Paste` (`Ctrl+V`)
    - `Undo` (`Ctrl+Z`)
    - `Redo` (`Ctrl+Y`)
    - `Find / Replace`
    - `Word Left / Word Right`
  - Persisted in SharedPreferences.

---

### 🟢 Phase 2: Interactive Toolbar & Ingress Engine
* **Modular Toolbar Component**:
  - Sits at top of the keyboard canvas.
  - Expand/collapse toggle arrow `>` on the far left.
  - **Collapsed State**: Displays live text candidate predictions and masked vault pills.
  - **Expanded State**: Displays quick tool action icons:
    - `[ 📋 Clipboard ]`
    - `[ 📝 Prompt List ]`
    - `[ 💻 Desktop Shortcuts ]`
    - `[ 🎙️ Voice Input ]`
    - `[ ⚙️ Settings ]`
* **Viewport Switcher Controller**:
  - Orchestrates swapping between standard typing layouts and modal viewports without UI inflation latency.

---

### 🟢 Phase 3: Desktop Shortcuts Modal
* **Renderer**: Dedicated `DesktopShortcutsRenderer` (loaded strictly on-demand).
* **Layout Structure**:
  - **Top**: Toolbar or quick-action header.
  - **Middle**: 5 to 7 customizable fat buttons chosen in Phase 1 settings.
  - **Bottom**: Directional navigation arrows flanking the **Universal Bottom 4**:
    - `[ ABC ]` (returns to typing)
    - `[ Space ]`
    - `[ Desktop Backspace ]`
    - `[ Desktop Enter ]`
* **Execution**: Dispatches directly through Android's `InputConnection` API.

---

### 🟢 Phase 4: Clipboard & Prompt List Modals + Edit Dialog
* **Clipboard Modal (Matching Screenshot)**:
  - Top editor sub-toolbar: Caret up/down/left/right, undo, cut, copy, clipboard, select all, close.
  - 2-column card grid on grayish canvas with rounded corners.
  - Pinned items marked with top-left thumbtack icon (`📌`).
  - Bottom: Universal Bottom-4 Anchor (`ABC`, `Space`, `Backspace`, `Enter`).
  - Long-press menu: `Pin`, `Delete`, `Take to Prompt List`.
* **Prompt List Modal**:
  - Dedicated storage for permanent prompt templates and reusable snippets.
  - Long-press menu: `Pin`, `Delete`, `Edit`.
  - **Prompt Edit Dialog**: Full centered Android `AlertDialog`:
    - Styled in the exact color theme of the keyboard (grayish-white canvas, white text card, teal-slate action buttons).
    - Minimalist: Editable text box + `[ Save ]` + `[ Cancel ]` buttons.
* **Settings Integration**:
  - Dedicated **"Prompt List"** item with its own management page in Settings (add, edit, delete, pin prompts).

---

### 🟢 Phase 5: Emoji Modal
* **Renderer**: Dedicated on-demand `EmojiRenderer`.
* **Layout Structure**:
  - Lightweight category tabs and grid modeled after HeliBoard.
  - Bottom: Universal Bottom-4 Anchor (`ABC`, `Space`, `Backspace`, `Enter`).

---

### 🟢 Phase 6: In-Keyboard Pattern Unlock (The Gateway)
* **Renderer**: Dedicated `PatternLockRenderer`.
* **Viewport & Isolation**:
  - Air-gapped 3x3 security grid taking full keyboard height.
  - **Toolbar and suggestion bar completely hidden/disabled**.
  - Touch loop isolated from keyboard text pipelines.
* **Security & Auth Session**:
  - Acts as an authentication gateway for both Personal Vault and Security Vault.
  - **3-minute session** for Security Vault.
  - **5-minute session** for Personal Vault.
  - Cryptographic key derivation via Android Keystore; auto-wipes on dismiss.
* **Log Keeper Privacy**:
  - Logs strictly `PatternAuth: SUCCESS` or `PatternAuth: FAILURE`.
  - Zero pattern coordinates, stroke lengths, or geometry ever logged.
* **Settings Guard**:
  - Added under **"Security"** in Settings (grouping Pattern Unlock, Personal Vault, Security Vault).
  - Protected behind device credentials (PIN/biometrics) or master password.

---

### 🟢 Phase 7: Personal Vault & Security Vault (.kdbx Chosen View)
* **Personal Vault (Quick Phrases / Autofill)**:
  - Masked suggestion pills in toolbar (`j•••e`).
  - Tap -> triggers Phase 6 Pattern Unlock -> commits unmasked snippet.
  - CRUD management in Settings behind master unlock.
* **Security Vault (KeePass `.kdbx`)**:
  - Ingress via context match or manual long-press on `?123` key.
  - Triggers Phase 6 Pattern Unlock -> opens foldable Vault list.
  - Tapping entry opens **Chosen View**:
    - **Dynamic unconstrained height**.
    - Top: Entry Title.
    - Action Row: `[ 🔒 Lock ]`, `[ ‹ Vault ]`, `[ Username ]` pill, `[ 🔑 Password ]` symbol, `[ ⏱️ TOTP ]` with 30s circular countdown spinner, `[ 📎 Attachment ]` popup.
    - Mid: `[ 1 ] [ 2 ] [ 3 ] [ 4 ]` quick digit / PIN pad.
    - Bottom: Universal Bottom-4 Anchor (`ABC`, `Space`, `Backspace`, `Enter`).

---

### 🟢 Phase 8: HeliBoard Dictionary, Prediction Engine & Voice (:voice)
* **Main Process Dictionary & Prediction**:
  - Binary memory-mapped (`mmap`) dictionary index and n-gram prediction engine running directly in the main IME process (`com.example`) for zero-latency per-keystroke lookups.
* **Voice Input Sub-Process (`:voice`)**:
  - Isolated heavy speech-to-text neural network model.
  - Post-install model import (keeps base APK featherweight).
  - Animated audio pulse visualizer modal with single-tap switch back to normal typing.
  - Non-essential background tasks throttled during active dictation.
