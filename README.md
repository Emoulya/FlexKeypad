# FlexKeypad

<p align="center">
  <img src="app/src/main/res/drawable/ic_splash_logo.xml" width="120" height="120" alt="FlexKeypad Logo" />
</p>

<p align="center">
  <strong>Ubah Perangkat Android Anda Menjadi Macro Pad & Gaming Surface Controller Virtual Berlatensi Rendah</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%2011%2B%20(API%2030%2B)-3DDC84?logo=android&logoColor=white" alt="Platform Android 11+" />
  <img src="https://img.shields.io/badge/Language-Kotlin%202.2-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin 2.2" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/HID-Bluetooth%20%2B%20USB%20Bridge-00F2FE" alt="HID Connectivity" />
</p>

---

## 📌 Tentang FlexKeypad

**FlexKeypad** adalah aplikasi utilitas Android yang mengubah tablet atau smartphone Anda menjadi perangkat keras *virtual macro pad* dan *surface controller* modular untuk laptop maupun PC. 

Berbeda dengan keyboard konvensional yang kaku dan terbatas pada tata letak QWERTY, FlexKeypad berjalan dalam orientasi **Landscape** dan menyajikan **kanvas bebas** tempat Anda dapat merancang, mengatur letak, serta menentukan ukuran tombol sesuka hati sesuai kebutuhan ergonomi dan gaya bermain Anda.

---

## ✨ Fitur Utama

### 1. Kanvas Modular & Bebas (*Freeform Canvas Editor*)
- **Drag-and-Drop & Resizing:** Bebas geser dan sesuaikan dimensi tombol langsung di layar sentuh.
- **Snap to Grid:** Opsi grid magnetik (*snap*) untuk merapikan baris dan kolom tombol secara simetris.
- **Mode Edit vs Mode Play:**
  - **Mode Edit:** Desain tata letak, ubah label, petakan tombol keyboard, gandakan (*duplicate*), dan atur ukuran.
  - **Mode Play:** Kanvas terkunci total (*anti-move*), fitur *Screen Always-On* aktif otomatis agar layar tidak meredup saat sesi bermain game intens.

### 2. Konektivitas Ganda (*Dual-Mode Transmission*)
- **Driverless Bluetooth HID:** Menggunakan protokol `BluetoothHidDevice` bawaan Android. HP Anda terdeteksi langsung oleh Windows, macOS, dan Linux sebagai **keyboard fisik hardware**, bebas driver tambahan dan mampu menembus proteksi anti-cheat game PC (seperti game dengan proteksi kernel/elevasi Administrator).
- **USB Cable Bridge (Ultra-Low Latency):** Transmisi melalui soket TCP via kabel data USB dan *ADB Port Forwarding*, menghasilkan latensi input yang nyaris $0\text{ ms}$.

### 3. Responsivitas Multi-Touch & Chording
- Mendukung penekanan banyak tombol secara bersamaan (misal: `W` + `Shift` + `Space` atau kombinasi tuts pintasan editor `Ctrl` + `Alt` + `Delete`).
- Setiap sentuhan jari diproses secara mandiri (*independent pointer tracking*) tanpa gangguan *key-ghosting*.

### 4. Manajemen Profil (*Profile Management*)
- Buat dan simpan berbagai tata letak tombol untuk berbagai kebutuhan (contoh: Profil Game FPS, Profil Simulator, Profil Video Editing Premiere Pro/Blender).
- Dukungan **Export & Import Profil** dalam format JSON ke penyimpanan perangkat.

### 5. Getaran Haptik Taktil Independen
- Switch *haptic feedback* bawaan aplikasi yang dirancang khusus dengan override mandiri. Tetap menghasilkan sensasi klik mekanikal taktil yang responsif di aplikasi meskipun pengaturan getar global pada sistem HP sedang dimatikan.

### 6. Desain Visual Modern & Adaptif
- **Splash Screen Resmi Android 12+ (`androidx.core:core-splashscreen`):** Animasi pembuka mulus dengan transisi *fade-out* tanpa layar putih berkedip (*zero flicker*).
- **App Icon Modern (Adaptive Icon):** Desain cyber neon berlatar *Deep Obsidian Slate* dengan dukungan *Themed Icons* Material You (Android 13+).
- **Expand/Collapse Side Tab Menu:** Tombol menu minimalis yang menempel rata (*flush*) di tepi kanan atas layar dengan panel geser samping yang tidak menghalangi tombol gameplay.
- **Mode Layar Penuh (Immersive Mode):** Menyembunyikan status bar dan navigation bar untuk ruang kontrol maksimal.

---

## 🛠️ Arsitektur & Teknologi

FlexKeypad dibangun dengan mengedepankan prinsip **Clean Architecture**, **SOLID**, dan **Clean Code**:

- **UI / Presentation:** Jetpack Compose, Material 3, Single-Activity Architecture, StateFlow.
- **Domain Layer:** Use Cases murni (`DispatchKeyStrokeUseCase`, `ManageProfileUseCase`), domain entities tanpa ketergantungan framework Android.
- **Data Layer:** 
  - `BluetoothHidController`: Implementasi Bluetooth HID Device profile.
  - `UsbBridgeController`: Socket server TCP untuk bridge USB ADB.
  - `JsonProfileRepositoryImpl`: Manajemen file JSON profil lokal.
  - `DataStoreAppSettingsRepositoryImpl`: Jetpack DataStore Preferences untuk persistensi preferensi aplikasi.
- **Versi & Kompatibilitas:**
  - **Min SDK:** 30 (Android 11)
  - **Target SDK / Compile SDK:** 37 (Android 16 preview / Android 15 ready)
  - **Java / JVM:** Java 17
  - **Kotlin:** 2.2.10

---

## 🚀 Panduan Memulai (*Getting Started*)

### Persyaratan
- Perangkat Android dengan sistem operasi Android 11 (API 30) ke atas.
- Bluetooth aktif pada Android dan PC/Laptop (untuk mode Bluetooth HID).
- Kabel data USB & ADB terpasang di PC (khusus jika menggunakan mode USB Bridge).

### Cara Menggunakan Mode Bluetooth HID (Rekomendasi)
1. Aktifkan Bluetooth pada perangkat Android dan PC Anda.
2. Buka aplikasi **FlexKeypad** di Android.
3. Buka menu samping di kanan atas, pilih **Status Koneksi**.
4. Pilih PC Anda dari daftar perangkat yang terpasang (*paired devices*) atau lakukan pairing baru via menu pengaturan Bluetooth HP.
5. Setelah status berubah menjadi warna hijau (**Connected**), tombol-tombol pada FlexKeypad langsung berfungsi sebagai input keyboard di PC Anda!

### Cara Menggunakan Mode USB Bridge
1. Hubungkan Android ke PC menggunakan kabel data USB dengan opsi **USB Debugging** aktif.
2. Jalankan perintah port forwarding di terminal PC:
   ```bash
   adb forward tcp:8899 tcp:8899
   ```
3. Jalankan aplikasi companion server di PC (tersedia di folder `tools/`):
   ```bash
   python tools/companion_server.py
   # atau jalankan executable FlexKeypadCompanion.exe (Run as Administrator jika bermain game ber-Anti-Cheat)
   ```
4. Indikator USB pada menu FlexKeypad akan menyala hijau dan siap digunakan.

---

## 💻 Panduan Pengembangan & Build (*Development*)

### Clone Repository
```bash
git clone https://github.com/Emoulya/FlexKeypad.git
cd FlexKeypad
```

### Build Debug APK
```bash
./gradlew assembleDebug
```
File APK hasil build akan berada di `app/build/outputs/apk/debug/app-debug.apk`.

### Install ke Perangkat via ADB
```bash
./gradlew installDebug
```

### Menjalankan Unit Tests
```bash
./gradlew test
```

---

## 📁 Struktur Direktori Proyek

```text
FlexKeypad/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/flexkeypad/
│   │   │   ├── data/             # Implementasi HID, USB, Repository & Storage
│   │   │   ├── domain/           # Use Cases, Models, & Interfaces
│   │   │   ├── ui/               # Jetpack Compose Screens, Canvas, & Components
│   │   │   └── MainActivity.kt   # Entry Point, Lifecycle & Splash Screen Hook
│   │   ├── res/                  # Vector Drawables, Mipmaps, Themes, Colors
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml        # Version Catalog Dependensi
├── tools/                        # PC Companion script & executable
└── README.md
```

---

## 📄 Lisensi

Proyek ini dikembangkan oleh **Emoulya** untuk kebutuhan kontroler modular berlatensi rendah. Bebas digunakan dan dikembangkan lebih lanjut.
