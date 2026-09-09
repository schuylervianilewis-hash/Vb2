# VIAN BOARD: MASTER BLUEPRINT & ARCHITECTURE SPECIFICATION

## Project Identity
- **Name**: Vian Board
- **Design Philosophy**: Ultra-lightweight, zero-bloat Android Input Method Editor (IME). High aesthetic fidelity matching the 12-screenshot reference suite with crisp white keycaps, slate labels, light sky-blue accents, and zero-resource idle stubbing.

---

## 1. Core Architecture: The Lightweight Stub & Direct Canvas Rendering

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          VIAN BOARD IME SERVICE                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  [ LIFECYCLE & STUB CONTROLLER ]                                            │
│  • onStartInputView()  -> Attach lightweight root layout (<10ms cold start) │
│  • onFinishInputView() -> Release popup windows, clear caches, drop to ~8MB │
├─────────────────────────────────────────────────────────────────────────────┤
│  [ 1. TOP TOOLBAR & HYBRID STRIP (Height: 44dp) ]                           │
│  • Left: [ > / < ] or [ 🕵️ Incognito (with original chevron border) ]       │
│  • Center: Suggestions / Active Chip / Tools Tray                           │
│  • Right (Pinned): [ ⬚ Select ]  [ ⎘ Copy ]  [ 📋 Clipboard ]              │
├─────────────────────────────────────────────────────────────────────────────┤
│  [ 2. FLOATING OVERLAY LAYER (Floats OVER Top Toolbar / Suggestion Strip) ] │
│  • Key Preview Bubble: Vertical pill with soft sky-blue ring                │
│  • MoreKeys Popup: 2-row multi-character symbol & accent grid (Row 0 & 1)   │
│  • Expanded Suggestions: Floating 2x3 candidate card                        │
├─────────────────────────────────────────────────────────────────────────────┤
│  [ 3. MAIN KEYBOARD CANVAS VIEW (Direct Hardware-Accelerated 2D Canvas) ]   │
│  • Mode 1: Main QWERTY + Numbers Row (1-0) + Superscript Sub-labels         │
│  • Mode 2: Symbols Page 1 (?123)                                            │
│  • Mode 3: Symbols Page 2 (=\<)                                             │
│  • Mode 4: Numpad / Calculator Mode (3x4 center digits + math operations)   │
│  • One-Handed Layout Mode (Dock Left / Dock Right / Expand)                 │
├─────────────────────────────────────────────────────────────────────────────┤
│  [ 4. ON-DEMAND MODAL OVERLAYS (Recycled when closed + 4 Bottom Buttons) ]  │
│  • Clipboard Manager: Top cursor/edit bar + 2-column Pinned 📌 cards grid    │
│  • Emoji / Kaomoji Picker: 10 category tabs + glyph grid                    │
│  • Prompt List (Placeholder)                                                │
│  • Universal Modal Bottom 4 Buttons: [ ABC ] [ Menu/Symbol ] [ Space ] [ ⌫ ]│
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Onscreen Layout & Component Specifications (Reference Image Suite)

### A. Main Alpha Keyboard (QWERTY)
* **Row 0 (Numbers):** `1¹ 2² 3³ 4⁴ 5⁵ 6⁶ 7⁷ 8⁸ 9⁹ 0⁰`
* **Row 1:** `Q% W/ E| R= T[ Y] U* I! O- P;` (Primary letter centered bold, secondary character in top-right corner).
* **Row 2:** `A@ S# D₹ F_ G& H- J+ K( L)`
* **Row 3:** `[ Shift ⇧ ] Z* X" C' V: B; N! M? [ Backspace ⌫ ]`
* **Row 4:** `[ ?123 ] [ , ... ] [ Spacebar ................ ] [ . ] [ ↵ / Go / Done ]`
* **Styling**: Pure White pill keycaps (`#FFFFFF`), Slate-900 primary labels (`#0F172A`), Slate-500 secondary labels (`#64748B`), soft Grey-Blue functional keys (`#E2E8F0`), Sky Blue accents (`#0EA5E9` / `#38BDF8`).

### B. Symbols Page 1 (`?123`)
* **Row 0 (Numbers):** `1 2 3 4 5 6 7 8 9 0`
* **Row 1:** `% / | = [ ] * ! - ; "`
* **Row 2:** `@ # ₹ _ & - + ( ) { }`
* **Row 3:** `[ =\< ] * " ' : ; ! ? : [ ⌫ ]`
* **Row 4:** `[ ABC ] [ , ... ] [ 12 34 ] [ Space ] [ . ] [ ↵ ]`

### C. Symbols Page 2 (`=\<`)
* **Row 0 (Numbers):** `1 2 3 4 5 6 7 8 9 0`
* **Row 1:** `~ ` \ • √ π ÷ × ¶ ∆`
* **Row 2:** `£ € $ ¢ ^ ° = { }`
* **Row 3:** `[ ?123 ] \ © ® ™ % [ ] [ ⌫ ]`
* **Row 4:** `[ ABC ] [ < ] [ Space ] [ > ] [ ↵ ]`

### D. Numpad / Calculator Mode
* **Left Column:** `+( ...`, `-) ...`, `*/ ...`, `[ ABC ]`
* **Center 3×4 Grid:**
  - `1 2 3`
  - `4 5 6`
  - `7 8 9`
  - `?123`, `0`, `="`
* **Right Column:** `% ₹ ...`, `_ ...`, `[ ⌫ ]`, `: ...`, `[ ↵ ]`

### E. One-Handed Layout Mode
* Docks keyboard to 75% width on Left or Right edge.
* Side bar controls: Top arrow (Flip Side), Bottom expand icon (Return to Full Width).

---

## 3. Toolbar Hybrid Strip & Interaction Matrix

### A. Top Toolbar 3 States
1. **State A (Default / Suggestions):**
   - Left: `[ > ]` expand toggle (or `[ 🕵️ ]` Incognito with original chevron border).
   - Center: Dynamic candidate suggestions with vertical dividers (`|`).
   - Right (Pinned): `[ ⬚ Select ] [ ⎘ Copy ] [ 📋 Clipboard ]`.
2. **State B (Expanded Tools Tray):**
   - Left: `[ < ]` collapse toggle.
   - Tools: `[ 🕵️ Incognito ]` `[ 🎤 Voice Input (Placeholder) ]` `[ ↶ Undo ]` `[ ↷ Redo ]` `[ ⚙️ Settings ]` `[ ≡ Text Edit ]` `[ ⤹ Go Right Up ]` `[ ⤸ Go Right Down ]` `[ 🛡️ Security Vault (Placeholder) ]` `[ 💬 Prompt List (Placeholder) ]` `[ 🗚 One-Handed (Placeholder) ]` `[ ⌄ Hide Keyboard ]`.
3. **State C (Active Chip):**
   - Left: `[ > ]`.
   - Center: `[ 📋 "Copied Text" ✕ ]` active action bubble chip.
   - Right (Pinned): `[ ⬚ Select ] [ ⎘ Copy ] [ 📋 Clipboard ]`.

### B. Comprehensive Tap vs. Long-Press Behavior Matrix

| Target Button / Key | Short Tap Action | Long-Press Action ($>250\text{ ms}$) |
| :--- | :--- | :--- |
| **`[ ⬚ ]` Select Button** | Select Current Word | **Select All Text** |
| **`[ ⎘ ]` Copy Button** | Copy selected text | **Open Prompt List (Placeholder)** |
| **`[ 📋 ]` Paste / Clipboard** | Paste from clipboard | **Open Clipboard Manager Modal** |
| **`[ 🕵️ ]` Incognito Mode** | Toggle Incognito On/Off | **5-Minute Temporary Incognito Mode** (Auto-reverts on timer) |
| **`[ 12 34 ]` Key (?123)** | Switch to Numpad Mode | **Open Security Vault (Placeholder)** |
| **`[ , ... ]` (Comma Key)** | Inserts comma `,` | **2-Row Menu Popup (10 items, 5 per row):**<br>Row 1: ⚙️ Settings, 😊 Emoji, 📋 Clipboard, 🪵 Log Keeper (OG Logger), 🖥️ Desktop Shortcuts<br>Row 2: 🎤 Voice Input *(Placeholder)*, 🖐️ One Hand *(Placeholder)*, 🪟 Floating Keyboard *(Placeholder)*, 🗄️ Personal Vault *(Placeholder)*, 🛡️ Security Vault *(Placeholder)* |
| **`[ Spacebar ]`** | Space / Commit suggestion | **1. Voice Input *(Placeholder)*** OR **2. Language Switcher** |

---

## 4. Popups & Gestures Over Toolbar Layer

1. **Popups Overlaying Toolbar Rule**:
   - Window token overlay bounds allow Single-Key Preview Bubble and 2-Row MoreKeys Card for top rows (Row 0 & Row 1) to **float upwards over the top toolbar / suggestion strip** without boundary clipping.
2. **Key Preview Bubble**: Floating vertical pill with soft sky-blue ring.
3. **MoreKeys Grid**: 2-row multi-character grid with finger slide selection.
4. **Gesture Controls**:
   - Spacebar cursor glide (horizontal drag moves cursor).
   - Backspace swipe delete (leftward drag selects words to delete).

---

## 5. Full Modal Overlays & Universal 4-Button Bottom Bar

### A. Clipboard Manager Modal
* **Speed Island Editing Toolbar (replaces suggestion bar):** DPAD navigation arrows (`◀`, `▶`, `▲`, `▼`), separator, editing tools (`Select All`, `Cut`, `Copy`, `Paste`), and `Clear Unpinned`.
* **Snippet Container:** 2-column scrollable grid of clean cards with `📌 Pin` and `🗑️ Delete` actions, with empty state placeholder.
* **Unified 4-Button Bottom Bar Component (Exclusive to Modals):** `[ ABC ]` `[ Space ]` `[ ⌫ Backspace ]` `[ ↵ Enter ]`.

### B. Emoji Picker Modal
* **Top Category Pill Tab Strip (replaces suggestion bar entirely):** 10 pill-style tabs (`Recent`, `Smileys`, `People`, `Animals`, `Food`, `Travel`, `Activities`, `Objects`, `Symbols`, `Flags`).
* **Content Area:** High-performance scrollable emoji grid (8-column).
* **Unified 4-Button Bottom Bar Component (Exclusive to Modals):** `[ ABC ]` `[ Space ]` `[ ⌫ Backspace ]` `[ ↵ Enter ]`.

### C. Prompt List *(Placeholder)*
* **Container:** 2-column / list layout for quick AI and template prompts.
* **Unified 4-Button Bottom Bar Component (Exclusive to Modals):** `[ ABC ]` `[ Space ]` `[ ⌫ Backspace ]` `[ ↵ Enter ]`.

---

## 6. Development Phases

| Phase | Description | Status |
| :--- | :--- | :--- |
| **Phase 1** | HeliBoard Preferences Infrastructure, Vectors & Datasets | COMPLETED |
| **Phase 2A** | Lightweight Canvas Main Keyboard Layouts (QWERTY with Row 0 & Sub-labels, Symbols 1 & 2, Numpad, Input Adaptation) | COMPLETED |
| **Phase 2B** | Top Toolbar System: Expanded Tools Tray, Pinned Right Customization, 3-min Incognito, Vector Icon Rendering & Settings Hierarchy | COMPLETED |
| **Phase 2C** | Key Preview Bubble & MoreKeys 2-Row Popup (Floating Over Toolbar) + Gestures | COMPLETED |
| **Phase 2D** | Clipboard Manager Modal (Speed Island Toolbar + 2-Column Pinned Grid + Unified 4 Bottom Buttons) | COMPLETED |
| **Phase 2E** | Emoji Picker Modal (10 Category Tabs Pill Strip + 8-Col Grid + Unified 4 Bottom Buttons) | COMPLETED |
| **Phase 2F** | Interaction Matrix (Long-press Shortcuts, 3-Min Incognito, Comma Menu, Placeholders) | COMPLETED |
| **Phase 2G** | One-Handed Mode Docking & Zero-Resource Idle Stub Integration | PLANNED |
| **Phase 2H** | Hard Rule System Bar Insets, Period 16-Symbol Popup (2x8), Auto-Numpad Switch & Toolbar Drag UI | COMPLETED |
| **Phase 2I** | HeliBoard Fedora Incognito Icon, Full HeliBoard Tools Array & Interactive Drag-Handle Reordering (ItemTouchHelper) | COMPLETED |
| **Phase 2J** | HeliBoard Comma Popup Calibration, Vertical Gap Decoupling, Default 1dp Border Styling & Authentic HeliBoard Vector Icons | COMPLETED |
| **Phase 2K** | Period Popup Touch/Geometry Crash Hardening, Log Keeper 2-Part Architecture (Catcher/Storage with 2MB Auto-Cut + Crash Drop to Download/ & Scoped Storage UI Reader) | COMPLETED |
| **Phase 2L** | Isolated HeliBoard Reference Folder (`Vianboardtryagain-main/` gitignored), Upgraded Anchor Chevrons to Authentic Vector Icons | COMPLETED |
| **Phase 2M** | Credential Immunity & Security Remediation: Purged /debug.keystore.base64, Sanitized Build Script Signing Configs, Hardened .gitignore & Updated CI Workflow | COMPLETED |
| **Phase 2N** | Complete HeliBoard Vector Icons/Drawables Import (221 Vector Drawables: ic_* & sym_keyboard_*), Color Resource Alignment & Keystore Removal | COMPLETED |
| **Phase 2O** | Elimination of Popup on Tap: Removed WindowManager IPC Tap Previews, Kept Pure Canvas Key Inversion & Preserved Long-Press Strips | COMPLETED |
| **Phase 2P** | HeliBoard Look and Feel: 10dp Key Corner Radius, 1dp Bottom Bevel Base Layer, Stadium/Pill Shape for Functional Keys, Vector Suite Icons (Backspaces, Shifts, Return, Incognito, Toolbar Pinned Tools) & Period Hint | COMPLETED |
| **Phase 2Q** | Staggered Typewriter Geometry (Half-Key Spacer Stagger for Row 2, 1.5x Functional Keys, Typewriter Diagonal Offset), Borderless Toolbar Icon Glyphs & 400ms HeliBoard Long-Press Tuning | COMPLETED |
| **Phase 2R** | HeliBoard Key Bevel Realism (Bottom-Only Dark Bevel, Removed 4-Sided Wireframe Stroke), 52dp Default Key Height, Toolbar Expansion Pinned Persistence, Toolbar Icons 26dp Sizing, and Corner Symbol Injection in Long-Press Popups | COMPLETED |
| **Phase 2S** | CI Pipeline Task Target Hardening: Updated GitHub Actions workflow to explicitly invoke `:app:assembleDebug` via `./gradlew` with executable permissions, resolving Configuration Cache task lookup failure on root project | COMPLETED |
| **Phase 2T** | CI Wrapper-Less Execution: Replaced `./gradlew :app:assembleDebug` with system `gradle :app:assembleDebug` in `.github/workflows/build_apk.yml`, eliminating dependency on absent `gradle-wrapper.jar` | COMPLETED |
| **Phase 2U** | On-Demand Clipboard & Emoji Modals with Speed Island, Category Pill Tabs Strip, and Unified 4-Button Modal Bottom Bar Component (`ModalBottomBarView`) | COMPLETED |
| **Phase 2V** | Quick Notes On-Demand Modal (Separate Zero-Idle Storage, 2-Column Staggered Grid, Card Long-Press Compact Popups for Clipboard & Notes, Translucent Dialog Note Editing Activity, and Settings Transfer Mode) | COMPLETED |


