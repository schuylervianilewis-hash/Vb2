# Vian Board — Architecture & Feature Outline

This document establishes the authoritative structural blueprint, multi-process boundaries, modal layouts, and security models for Vian Board as finalized in consultation.

---

## 1. Process & Memory Architecture

### A. Main IME Process (`com.example` / root package)
* **Components Included**:
  - Main Keyboard UI Canvas (`VianKeyboardView`, touch dispatching, layouts).
  - HeliBoard Dictionary Engine (binary memory-mapped dictionary index).
  - Prediction & Next-Word Engine (in-process lookup with zero IPC serialization latency).
* **Rationale**: Exactly matches HeliBoard's proven architecture. Sub-millisecond keystroke lookups with direct memory mapping (`mmap`), zero frame stutters, and immediate responsiveness.

### B. Voice Input Sub-Process (`:voice`)
* **Components Included**:
  - Heavy Speech-to-Text (STT) neural network / recognizer model (e.g. Whisper/Vosk/Sherpa-ONNX).
* **Execution & Lifecycle Rules**:
  - Strictly isolated in its own process (`:voice`).
  - **Post-Install Model Import**: The base APK remains lightweight on download; the user imports or downloads the voice model on-demand after installation.
  - **Visual Pulse Modal**: When voice is active, renders an animated audio pulse / waveform visualizer with an instant tap button to switch back to normal typing.
  - **Resource Governor**: When `:voice` is executing, non-essential background tasks in the app are paused to maximize system resources for audio inference.

### C. On-Demand Lightweight Components
* All other components (Security Vault, Personal Vault, Clipboard, Prompt List, Desktop Shortcuts, Emoji Picker) remain **strictly on-demand**.
* Zero idle memory footprint: resources and sensitive state are instantiated only when active and purged immediately upon completion or timeout.

---

## 2. Universal Anchor: Bottom-4 Buttons Row

To protect user muscle memory and ensure flawless tactile familiarity, **all secondary modes and modals** share the exact same bottom anchor row:

```
┌──────────────────────────────────────────────────────────────┐
│  [  ABC  ]  │  [       SPACE BAR       ]  │  [ ⌫ ]  │  [ ↵ ] │
└──────────────────────────────────────────────────────────────┘
```

* **Standard Bottom 4**:
  1. `[ ABC ]`: Returns immediately to the normal alpha-numeric typing canvas.
  2. `[ SPACE BAR ]`: Standard space insertion or contextual action.
  3. `[ ⌫ ]`: Backspace deletion.
  4. `[ ↵ ]`: Enter / Action key.
* **Input-Aware Adaptability**: Adapts to the target input field when applicable (e.g. Next, Done, Search, Go).
* **Uniformly applied across**:
  - Prompt List Modal
  - Desktop Shortcuts Modal
  - Clipboard Modal
  - Emoji Picker Modal
  - Security Vault "Chosen" Screen

---

## 3. In-Keyboard Security & Unlock Engine

### A. In-Board Height Pattern Unlock
* Renders a clean 3x3 security pattern grid directly within the keyboard's on-screen canvas height.
* Eliminates heavy window inflation and prevents screen-switching latency.
* Derives an AES-256 / Argon2 cryptographic key via Android Keystore.

### B. Session Timeout Rules
* **Security Vault (`.kdbx`)**:
  - Unlocked duration: **3 minutes**.
  - Auto-locks immediately upon expiration, manual lock tap, or keyboard dismissal (`onFinishInputView`).
* **Personal Vault (Quick Phrases / Auto-fill)**:
  - Unlocked duration: **5 minutes**.
  - Optimized for multi-field web forms (name, email, shipping address, tax ID).

---

## 4. Personal Vault (Personal Dictionary & Quick Phrases)

* **Scope**: Fork of HeliBoard's personal dictionary and quick phrases. Stores addresses, emails, phone numbers, full names, and sensitive personal snippets.
* **Suggestion Bar Integration**:
  - Automatically offered as a pill in the suggestion bar when matching fields/prompts are detected.
  - **Masking Privacy**: Displayed with only first and last characters visible, middle masked (e.g., `j•••••••e@example.com` -> `j•••e`).
  - **Unlock Trigger**: Tapping the masked pill switches the keyboard canvas to the **In-Board Pattern Unlock**.
  - Once verified, the decrypted content is committed directly to the active field.
* **Management & CRUD**:
  - Managed via the Settings app.
  - Guarded by a dedicated full-screen master unlock mechanism.

---

## 5. Security Vault (KeePassDX-Based `.kdbx` Integration)

* **Data Source**: Encrypted KeePass `.kdbx` database file.
* **Access Triggers**:
  1. **Context-Aware Suggestion**: If the foreground app package / URL matches a vault entry, offers a masked vault pill in the suggestion bar -> Tap -> Pattern Unlock -> Auto-paste credentials.
  2. **Manual Ingress**: Long-press `?123` key -> In-Board Pattern Unlock -> Opens the **Vault List Modal**.

### A. Vault List Modal
* Compact, scrollable / foldable list displayed within keyboard viewport.
* Displays: Entry Title, Username, and TOTP refresh indicator.
* Sorting enabled; optimized for zero-lag traversal without heavy indexing.
* Tapping an entry navigates to the **Chosen View**.

### B. Chosen View (Entry Detail Screen)
* **Dynamic Viewport**: **Not restricted to normal keyboard height**; expands vertically as needed for clarity and ease of touch.
* **Layout Hierarchy (Top to Bottom)**:
  1. **Top Header**: Entry Title.
  2. **Action Bar Row**:
     - `[ 🔒 Lock ]`: Instant memory purge and vault lock.
     - `[ ‹ Vault ]`: Back to the vault entry list.
     - `[ Username ]`: Shows written username pill (tap commits to field).
     - `[ 🔑 Password ]`: Shows discreet symbol pill (tap commits password without exposing plain text).
     - `[ ⏱️ TOTP ]`: Displays live 6-digit one-time code paired with a circular countdown spinner indicating 30-second refresh.
     - `[ 📎 Attachment ]`: Tap triggers an in-viewport popup previewing any binary files/attachments stored in the `.kdbx` record.
  3. **Mid Keypad**: `[ 1 ] [ 2 ] [ 3 ] [ 4 ]` quick digit / PIN entry buttons.
  4. **Bottom Anchor Row**: `[ ABC ] [ Space ] [ ⌫ ] [ ↵ ]`.
* **Management & CRUD**:
  - Full record creation, modification, and file import handled in Settings under the master unlock shield.

---

## 6. Clipboard & Prompt List Modals

### A. Standard Clipboard Modal
* Captures and displays recent clipboard history.
* **Long-Press Context Menu (3 Options)**:
  1. **Pin**: Pins item to the top, preventing auto-expiry.
  2. **Delete**: Removes item permanently from history.
  3. **Take to Prompt List**: Promotes the clipboard snippet into the permanent Prompt repository.
* **Bottom Row**: Uniform Bottom-4 Anchor (`ABC`, `Space`, `Backspace`, `Enter`).

### B. Prompt List Modal
* Dedicated permanent repository for reusable prompt templates, system instructions, AI prompts, and code blocks (distinct from ephemeral clipboard items).
* **Long-Press Context Menu (3 Options)**:
  1. **Pin**: Pins prompt to top of list.
  2. **Delete**: Removes prompt from repository.
  3. **Edit**: Opens an in-keyboard editing dialog containing:
     - Editable text field.
     - `[ Save ]` and `[ Cancel ]` buttons.
* **Bottom Row**: Uniform Bottom-4 Anchor (`ABC`, `Space`, `Backspace`, `Enter`).

---

## 7. Desktop Shortcuts Modal

* Designed for rapid text editing, coding, and power-user terminal workflows.
* **Layout Architecture**:
  - **Fixed Navigation**: Directional arrow keys positioned beside the bottom anchor buttons.
  - **Fixed Bottom 4**:
    - `[ ABC ]`
    - `[ Space ]`
    - `[ Desktop Backspace ]` (Word / Forward Delete)
    - `[ Desktop Enter ]`
  - **Middle Canvas (5 to 7 Configurable "Fat Buttons")**:
    - Generously sized touch targets configured in Settings.
    - Available actions: `Copy`, `Copy All`, `Select All`, `Cut`, `Paste`, `Undo`, `Redo`, `Find`, `Replace`, `Word Left`, `Word Right`.
    - Dispatched directly via Android `InputConnection` commands and key events.

---

## 8. Emoji Modal

* Lightweight, zero-lag emoji picker modeled after HeliBoard.
* Categorized tabs with recent emoji caching.
* Anchored with the uniform Bottom-4 row (`ABC`, `Space`, `Backspace`, `Enter`) for immediate return to typing.

---

## 9. Security & Data Hygiene Mandates

1. **No Plaintext Credential Strings**: Decrypted passwords and cryptographic keys must be processed via `CharArray` or byte buffers, with explicit memory zeroing (`Arrays.fill(..., 0)`) immediately upon lock or window teardown.
2. **Zero Leakage**: Clipboard items marked sensitive or originating from the vault must be flagged with `ClipDescription.EXTRA_IS_SENSITIVE` on Android 13+.
3. **Storage Access**: Uses Android Storage Access Framework (SAF) for persistent `.kdbx` file reading without requiring broad external storage permissions.
