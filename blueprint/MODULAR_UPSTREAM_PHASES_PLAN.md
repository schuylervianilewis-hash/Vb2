# 🗺️ Modular Upstream Source-Import & Settings Architecture Plan

## Core Architecture Principle: Vertical Slices
Every component is imported from upstream or built new as a **complete vertical slice**:
$$\text{Vertical Slice} = \text{Core Engine Logic} + \text{Native Layouts / Drawables} + \text{Dedicated Settings Sub-Page}$$

There are no disconnected or deferred settings phases. When any subsystem is imported, its settings page is hooked directly into `MainSettingsScreen` and accessible via deep-link intent routing from keyboard shortcuts.

---

## 🏛️ Upstream Source Repositories
1. **HeliBoard Engine (`https://github.com/HeliBorg/HeliBoard`)**: Core LatinIME, Canvas key layout trees, XML themes/drawables, gesture engine, suggestion/toolbar strip, and binary `.dict` dictionary loader.
2. **FUTO Voice Input (`https://github.com/futo-org/voice-input`)**: On-device neural speech-to-text (Whisper / Sherpa-ONNX / Silero VAD).
3. **KeePassDX (`https://github.com/Kunzisoft/KeePassDX`)**: Secure `.kdbx` database reader, zero-clipboard credential injector, and RFC 6238 TOTP engine.
4. **Vian Board Custom Builds**: Log Keeper diagnostic engine, Personal Vault / Prompt List, Desktop Fat-Key Nav Pad, and Universal ZIP Backup/Restore.

---

## 📋 Detailed Phase Breakdown (Phase 0 through Phase 6)

### 🟢 Phase 0: Host App Foundation, Theme Styling & Empty Settings Hub
* **Components**:
  1. **Welcome Screen Visual Styling**:
     - Modern **Material 3 Neutral Grey / Slate** surface canvas.
     - Subtle **Light Sky Blue** outline accents, card borders, and divider lines.
     - 3-step setup (Enable IME $\to$ Select IME $\to$ Open Settings / Log Keeper).
  2. **Master Settings Hub (`MainSettingsScreen.kt`)**:
     - Initialized as a structured list container hosting sub-page routes.
     - Settings button on Welcome screen navigates directly here.
  3. **Log Keeper Diagnostic Engine**:
     - Circular memory buffer, zero-PII scrubber, export `.txt`.
     - Direct deep-link routing from Comma `🪵` shortcut verified.

---

### 🟢 Phase 1: HeliBoard Core Service & Preferences Infrastructure
* **1.1: LatinIME Service Lifecycle & Core State**:
  - Upstream `LatinIME.kt`, `RichInputConnection`, and input session manager.
  - EditorInfo inspection (URI, Email, Number, Text, Password).
* **1.2: General Keyboard Preferences Page**:
  - `GeneralSettingsScreen`: Haptic feedback strength slider, keypress sound toggle, vibrate on keypress, auto-capitalization.

---

### 🟢 Phase 2A: HeliBoard Layouts, Keycaps & Theme Styling
* **2A.1: XML Layout Trees & Key Geometry**:
  - Upstream `res/xml/` layouts (QWERTY, symbols, numpad), `KeyboardLayoutSet`, and `Key` models.
* **2A.2: Material 3 Rounded Pill Keycaps & Symbol Hints**:
  - Rounded rectangular pill drawables with Material 3 elevation.
  - Top-right superscript symbol hints on all letter keys (`Q` $\to$ `%`, `W` $\to$ `'`, `A` $\to$ `@`, `S` $\to$ `#`, `1`–`0`).
* **2A.3: Appearance & Theme Settings Sub-Page**:
  - `AppearanceSettingsScreen`: Number row toggle, symbol hints on keys toggle, key height scaling slider, theme selector (Material 3 Dynamic / AMOLED / Catppuccin).

---

### 🟢 Phase 2B: HeliBoard Multi-Touch & Tactile Gesture Engine
* **2B.1: PointerTracker & Touch Dispatcher**:
  - Upstream multi-touch event handling, long-press timer, and popup bubble anchors.
* **2B.2: Tactile Spacebar Trackpad & Swipe-to-Delete**:
  - Spacebar horizontal glide cursor tracking with micro-haptic ticks.
  - Backspace left-swipe word selection highlight and release-to-delete.
* **2B.3: Gesture Settings Sub-Page**:
  - `GestureSettingsScreen`: Spacebar cursor glide toggle & sensitivity slider, swipe-to-delete toggle, gesture distance thresholds.

---

### 🟢 Phase 3A: HeliBoard Toolbar & Suggestion Strip UI
* **3A.1: SuggestionStripView & Candidate Layout**:
  - Upstream 3-slot word suggestion bar (Center bolded primary candidate, left/right alternates).
  - Expand chevron (`>`) to toggle quick tools strip.
* **3A.2: Toolbar Customization Settings Sub-Page**:
  - `ToolbarSettingsScreen`: Toggle suggestion strip visibility, customize toolbar quick-action icon order.

---

### 🟢 Phase 3B: HeliBoard Dual-Language Binary Dictionary & Autocorrect Engine
* **3B.1: Memory-Mapped (.dict) Dictionary Facilitator**:
  - Upstream C++/mmap binary `.dict` loader for fast, zero-GC memory performance on low-end phones.
  - Dual-language simultaneous prediction and autocorrect pipeline.
* **3B.2: Dictionary & Correction Settings Sub-Page**:
  - `CorrectionSettingsScreen`: Autocorrect aggressiveness (Off / Modest / Aggressive), next-word prediction toggle, dual-language dictionary manager, custom wordlist importer.

---

### 🟢 Phase 4A: HeliBoard Native Emoji Suite & Symbol Pagers
* **4A.1: Emoji Keyboard & Category Tabs**:
  - Searchable emoji grid with category navigation, skin-tone selector, and recent emoji cache.
* **4A.2: Emoji Settings Sub-Page**:
  - `EmojiSettingsScreen`: Emoji physical key toggle, recent emoji history size, dedicated emoji row toggle.

---

### 🟢 Phase 4B: HeliBoard Native Clipboard Manager
* **4B.1: Clipboard History & Quick-Paste Pills**:
  - In-keyboard clipboard history shelf with pin, delete, and copy-to-input features.
* **4B.2: Clipboard Settings Sub-Page**:
  - `ClipboardSettingsScreen`: Clipboard history retention duration, auto-clear sensitive passwords/PINs toggle.

---

### 🟢 Phase 4C: Desktop Fat-Key Nav Pad (Built New)
* **4C.1: 3-Row Desktop Navigation Modal**:
  - Large-target fat-key layout: Arrow keys (`← ↑ ↓ →`), `Home`, `End`, `PgUp`, `PgDn`, `Select All`, `Cut`, `Copy`, `Paste`.
* **4C.2: Desktop Pad Settings Sub-Page**:
  - `DesktopNavSettingsScreen`: Custom key mapping, cursor repeat acceleration speed.

---

### 🟢 Phase 4D: Personal Vault & Prompt List (Built New)
* **4D.1: Prompt List Snippets Sheet**:
  - Fast-injection drawer for pre-saved multi-line prompts, code snippets, and standard replies.
* **4D.2: Prompt List Settings Sub-Page**:
  - `PromptListSettingsScreen`: Add, edit, organize categories, and export prompt JSON.

---

### 🟢 Phase 5: KeePassDX Security Vault & 2FA/TOTP Integration
* **Upstream**: `Kunzisoft/KeePassDX`
* **Core**: `.kdbx` database reader, in-keyboard master unlock, zero-clipboard credential injection, 6-digit TOTP generator.
* **Settings**: `SecurityVaultSettingsScreen`.

---

### 🟢 Phase 6: FUTO Offline Neural Voice Input & Universal ZIP Backup Engine
* **Upstream**: `futo-org/voice-input` (Whisper / Sherpa-ONNX with Silero VAD) + Custom Backup Engine.
* **Core**: Low-latency on-device speech-to-text sheet + HeliBoard/VianBoard compatible ZIP backup/restore.
* **Settings**: `VoiceSettingsScreen` & `BackupSettingsScreen`.
