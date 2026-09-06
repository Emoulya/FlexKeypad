# FlexKeypad Desktop Companion Bridge

Companion bridge ringan berbasis Python untuk mode koneksi kabel USB dengan latensi ultra-rendah ($\le 5\text{ ms}$).

## Cara Menggunakan

1. **Sambungkan HP / Tablet ke PC via kabel USB** dan pastikan **USB Debugging** aktif.
2. **Lakukan port forwarding dengan ADB:**
   ```bash
   adb reverse tcp:8899 tcp:8899
   ```
3. **Install dependensi (opsional, untuk injeksi keystroke otomatis ke Windows/macOS/Linux):**
   ```bash
   pip install pynput
   ```
4. **Jalankan Companion Bridge:**
   ```bash
   python bridge.py
   ```
5. Buka aplikasi **FlexKeypad** di perangkat Android Anda. Status koneksi akan langsung berubah menjadi **USB (1 Host)** dan siap digunakan!
