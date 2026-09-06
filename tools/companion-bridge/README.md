# FlexKeypad Desktop Companion Bridge

Desktop companion untuk mode koneksi kabel USB dengan latensi ultra-rendah ($\le 5\text{ ms}$) dan pengalaman **Zero-Touch Plug & Play**.

---

## 1. Windows Native Companion (Direkomendasikan - Plug & Play)

Tidak memerlukan instalasi Python, Node.js, atau dependensi eksternal apapun. Berjalan di System Tray dengan konsumsi memori hanya ~12 MB RAM.

### Cara Menggunakan (1-Click Setup)
1. Sambungkan HP ke Laptop/PC via kabel USB (USB Debugging aktif).
2. Klik ganda:
   ```cmd
   install-companion.bat
   ```
   Script ini akan:
   - Mengompilasi `FlexKeypadCompanion.exe` (atau menggunakan binary siap pakai).
   - Membuat shortcut di **Desktop**.
   - Mendaftarkan auto-start di **Windows Startup** (`shell:startup`).
   - Menjalankan companion di background (ikon di System Tray dekat jam).
3. Buka aplikasi **FlexKeypad** di smartphone. Status koneksi di HP dan icon di tray Windows otomatis berubah menjadi **Hijau (USB Connected)**. Siap digunakan!

### Fitur Native Companion
- **System Tray:** Ikon visual dinamis (Hijau = Connected, Abu-abu = Standby), context menu untuk Reconnect / Auto-start toggle / Exit.
- **Auto Port Forwarding:** Mendeteksi `adb.exe` dan menyetel port forwarding secara otomatis tanpa jendela CMD yang berkedip.
- **Interactive Desktop Keystrokes:** Menyuntikkan keystroke langsung ke aplikasi aktif apapun di monitor desktop Windows via Win32 API.
- **Auto Reconnect & Keepalive:** Heartbeat ping periodik dan auto-reconnect saat kabel USB dicabut dan dicolokkan kembali.

---

## 2. Alternatif Script (Cross-Platform / Developer)

- **PowerShell (Windows):** `.\bridge.ps1`
- **Python (Windows / macOS / Linux):** `python bridge.py` (memerlukan `pip install pynput`)
- **Node.js (Cross-Platform):** `node bridge.js` (memerlukan `npm install robotjs`)
- **Quick Batch Launcher:** `start-bridge.bat`

