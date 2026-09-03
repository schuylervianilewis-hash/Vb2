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

### A. Clipboard Manager
* **Top Navigation & Cursor Bar:** `[ ⌃ ] [ ⌄ ] [ ⮜ ] [ ⮞ ]` (Directional arrows) + `[ ↶ Undo ] [ ✂ Cut ] [ ⎘ Copy ] [ 📋 Paste ] [ ⬚ Select All ] [ ✕ Close ]`.
* **Snippet Container:** 2-column scrollable grid of clean cards with `📌 Pin` and `🗑️ Delete` actions.
* **Universal Modal Bottom 4 Buttons:** `[ ABC ]` `[ ?123 / Menu ]` `[ Spacebar ]` `[ ⌫ Backspace ]`.

### B. Emoji / Kaomoji Picker
* **Top Category Tab Bar:** 10 icons (`Recent`, `Smileys`, `People`, `Animals`, `Food`, `Places`, `Activities`, `Objects`, `Symbols`, `Flags`, `Kaomoji`).
* **Content Area:** High-performance scrollable emoji grid.
* **Universal Modal Bottom 4 Buttons:** `[ ABC ]` `[ ?123 / Menu ]` `[ Spacebar ]` `[ ⌫ Backspace ]`.

### C. Prompt List *(Placeholder)*
* **Container:** 2-column / list layout for quick AI and template prompts.
* **Universal Modal Bottom 4 Buttons:** `[ ABC ]` `[ ?123 / Menu ]` `[ Spacebar ]` `[ ⌫ Backspace ]`.

---

## 6. Development Phases

| Phase | Description | Status |
| :--- | :--- | :--- |
| **Phase 1** | HeliBoard Preferences Infrastructure, Vectors & Datasets | COMPLETED |
| **Phase 2A** | Lightweight Canvas Main Keyboard Layouts (QWERTY with Row 0 & Sub-labels, Symbols 1 & 2, Numpad, Input Adaptation) | COMPLETED |
| **Phase 2B** | Top Toolbar System: Expanded Tools Tray, Pinned Right Customization, 3-min Incognito, Vector Icon Rendering & Settings Hierarchy | COMPLETED |
| **Phase 2C** | Key Preview Bubble & MoreKeys 2-Row Popup (Floating Over Toolbar) + Gestures | COMPLETED |
| **Phase 2D** | Clipboard Manager Modal (Top Navigation + 2-Column Pinned Grid + 4 Bottom Buttons) | PLANNED |
| **Phase 2E** | Emoji & Kaomoji Picker Modal (10 Category Tabs + Grid + 4 Bottom Buttons: ABC, Space, Backspace, Enter) | COMPLETED |
| **Phase 2F** | Interaction Matrix (Long-press Shortcuts, 3-Min Incognito, Comma Menu, Placeholders) | COMPLETED |
| **Phase 2G** | One-Handed Mode Docking & Zero-Resource Idle Stub Integration | PLANNED |
