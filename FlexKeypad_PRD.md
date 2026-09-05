# Product Requirement Document (PRD)

## Project: FlexKeypad (Modular Macro Pad & Custom Virtual Controller)

### 1. Ringkasan Eksekutif (Executive Summary)

**FlexKeypad** adalah aplikasi utilitas Android yang mengubah tablet atau smartphone menjadi perangkat *hardware macro pad* / *surface controller* virtual yang sepenuhnya modular. Berbeda dengan aplikasi keyboard konvensional yang menampilkan tata letak QWERTY baku dan kaku, FlexKeypad beroperasi dalam orientasi **Landscape** secara *default* dan menyajikan kanvas kosong saat pertama kali dijalankan.

Pengguna memiliki kebebasan absolut untuk mendesain permukaan kendali mereka sendiri: menentukan tombol apa saja yang ingin ditampilkan, mengatur letak tombol dengan *drag-and-drop*, serta mengubah dimensi ukuran (*resizing*) tiap tombol sesuai kebutuhan ergonomi tangan. Aplikasi ini mentransmisikan sinyal *keystroke* ke laptop/PC melalui dua jalur koneksi: **Bluetooth HID** (nirkabel tanpa perlu driver tambahan) dan **USB** (kabel data dengan latensi ultra-rendah).

### 2. Latar Belakang & Masalah Pengguna (Problem Statement)

#### 2.1 Target Pengguna (User Personas)

1. **Kreator Konten, Video Editor, & 3D Artist (Premiere Pro, DaVinci, Blender, Photoshop):**
   - *Pain Point:* Sering menggunakan kombinasi tombol pintasan rumit (misal: $\text{Ctrl} + \text{Alt} + \text{Shift} + \text{E}$ atau alat *cut/razor*). Perangkat keras khusus seperti Elgato Stream Deck atau *mechanical macro pad* berharga mahal dan kurang fleksibel untuk diubah ukurannya.
2. **Gamer (Simulators, MMO, RPG, FPS):**
   - *Pain Point:* Membutuhkan panel tombol tambahan di sisi kiri/kanan keyboard utama untuk fungsi spesifik (misal: *gear shift*, *autopilot toggle*, *push-to-talk*, *inventory macro*) dengan tombol berukuran ekstra besar agar tidak meleset saat ditekan tanpa melihat layar secara intensif.
3. **Programmer & Pengguna Keyboard Kompak (60% / 65% Layout):**
   - *Pain Point:* Kehilangan tombol fisik esensial seperti `F1`–`F12`, tombol panah navigasi, `Home`, `End`, atau blok tombol angka (*Numpad*).
4. **Pengguna Aksesibilitas:**
   - *Pain Point:* Kesulitan menekan tuts keyboard laptop yang kecil dan berdekatan; membutuhkan 2–4 tombol berukuran raksasa di layar sentuh untuk menggantikan input tertentu.

#### 2.2 Solusi Produk

Menyediakan aplikasi Android berbasis kanvas bebas dengan orientasi horizontal (*landscape*) yang memungkinkan pengguna merakit tata letak tombol virtual mereka sendiri, memetakan tombol ke kode keyboard fisik standar, dan mengirimkan sinyal instan ke laptop/PC.

### 3. Value Proposition & Keunggulan Produk

- **Zero Hardware Cost:** Mengubah ponsel atau tablet Android yang sudah ada (termasuk perangkat lama) menjadi *macro pad* fungsional tanpa membeli hardware mahal.
- **Kanvas Bebas & Modularitas Penuh:** Tidak ada tata letak keyboard default yang membatasi. Pengguna bisa membuat 1 tombol raksasa seukuran layar penuh, 4 tombol navigasi, atau matriks $4 \times 4$.
- **Driverless Bluetooth HID:** Memanfaatkan Android `BluetoothHidDevice` sehingga terdeteksi secara *native* oleh Windows, macOS, Linux, Android TV, dan iOS sebagai keyboard fisik biasa tanpa perlu instalasi aplikasi tambahan di PC.
- **Koneksi Ganda (Dual-Mode Connectivity):** Mendukung koneksi Bluetooth untuk fleksibilitas kerja serta koneksi kabel USB untuk latensi nyaris $0\text{ ms}$.
- **Dua Status Operasional Jelas:** Mode Edit (*Canvas Editor*) untuk merancang tombol dan Mode Pakai (*Play Mode*) yang mengunci kanvas agar tata letak tidak bergeser saat ditekan cepat.

### 4. Alur Pengguna (User Journey)

```
[Buka Aplikasi FlexKeypad] ──► (Layar otomatis terkunci di posisi Landscape)
             │
             ▼
[Inisialisasi Pertama: Kanvas Kosong]
             │
             ├──► [Tambah Tombol Baru] 
             │          │
             │          ├── Pilih Fungsi Tombol (Karakter / Modifier / Tombol Fungsi)
             │          ├── Atur Posisi (Drag-and-Drop di Kanvas)
             │          └── Atur Ukuran (Resize via Pin Slider atau Handle Sudut)
             │
             ├──► [Pengaturan Koneksi]
             │          ├── Opsi 1: Pairing Bluetooth HID langsung ke Laptop/PC
             │          └── Opsi 2: Hubungkan Kabel USB (AOA / Companion Bridge)
             │
             ▼
[Aktifkan "Play Mode"] ──► Layar terkunci (anti-geser) & Screen Always-On
             │
             ▼
[Tekan Tombol Virtual] ──► Sinyal Keystroke Terkirim ke Laptop/PC (Real-Time)

```

### 5. Rincian Fitur & Prioritas (Feature Specifications)

Fitur diklasifikasikan menggunakan kerangka kerja **MoSCoW**:

| **Prioritas** | **Modul / Fitur** | **Deskripsi Fungsional** |
| ------------- | ----------------- | ------------------------ |
|               |                   |                          |

**P0 (Must Have)**

|   |
**Strict Landscape Orientation**

|   |
Aplikasi memaksa orientasi layar terkunci horizontal (*landscape*) sejak awal dibuka.

|   |
**P0 (Must Have)**

|   |
**Empty Canvas Initial State**

|   |
Menampilkan kanvas kosong tanpa tombol bawaan saat instalasi pertama, siap dikustomisasi.

|   |
**P0 (Must Have)**

|   |
**Dynamic Button Creation**

|   |
Tombol mengambang (*floating action*) untuk menambahkan kartu/tombol baru ke kanvas.

|   |
**P0 (Must Have)**

|   |
**Drag & Drop Repositioning**

|   |
Pengguna bebas memindahkan letak tombol di koordinat $(X, Y)$ kanvas dengan gestur seret.

|   |
**P0 (Must Have)**

|   |
**Individual Button Resizing**

|   |
Tombol memiliki *handle* sudut atau pengatur skala dimensi (lebar $\times$ tinggi) untuk memperbesar/memperkecil tombol secara independen.

|   |
**P0 (Must Have)**

|   |
**Key Mapping Engine**

|   |
Pemetaan fungsi tombol ke: alfabet (`A–Z`), angka (`0–9`), tombol fungsi (`F1–F12`), navigasi (`Arrows`, `Home`, `End`), dan kontrol (`Esc`, `Space`, `Enter`, `Tab`, `Backspace`).

|   |
**P0 (Must Have)**

|   |
**Play Mode Lock**

|   |
Tombol sakelar untuk mengunci kanvas dari mode edit ke mode pakai aktif, mematikan fungsi *drag* dan mengaktifkan deteksi ketukan instan.

|   |
**P0 (Must Have)**

|   |
**Bluetooth HID Engine**

|   |
Mengemulasikan profil keyboard nirkabel standar melalui API Android `BluetoothHidDevice` tanpa software tambahan di laptop.

|   |
**P1 (Should Have)**

|   |
**USB Wired Connection**

|   |
Pengiriman sinyal input melalui kabel data USB (Android Open Accessory / ADB Socket bridge) untuk latensi ultra-rendah $\le 5\text{ ms}$.

|   |
**P1 (Should Have)**

|   |
**Multi-Key Modifiers (Hotkeys)**

|   |
Mendukung kombinasi tombol dalam 1 sentuhan (contoh: `Ctrl + C`, `Alt + F4`, `Win + Shift + S`).

|   |
**P1 (Should Have)**

|   |
**Snap-to-Grid System**

|   |
Opsi magnetik (*magnetic grid*) di kanvas agar perataan tombol rapi dan presisi.

|   |
**P1 (Should Have)**

|   |
**Tactile Haptic Feedback**

|   |
Getaran haptik halus setiap kali tombol disentuh di Play Mode untuk mensimulasikan sensasi tuts mekanik.

|   |
**P1 (Should Have)**

|   |
**Multi-Profile Management**

|   |
Menyimpan beberapa kanvas berbeda (contoh: Profil "Photoshop", Profil "Sim-Racing", Profil "Numpad").

|   |
**P2 (Could Have)**

|   |
**Visual Styling & Icons**

|   |
Mengubah warna latar tombol, warna teks, atau memilih ikon representatif (ikon gunting, render, pause, dsb.).

|   |
**P2 (Could Have)**

|   |
**Export / Import Layout**

|   |
Ekspor dan impor rancangan layout kanvas dalam format file JSON antarperangkat.

|   |
**P3 (Won't Have Now)**

|   |
**Rotary Knob / Slider Input**

|   |
Input putaran dinamis virtual untuk mengatur volume atau timeline scrubbing (direncanakan untuk versi 2.0).

### 6. Spesifikasi Teknis & Arsitektur Sistem

#### 6.1 Platform & Library

- **Platform Target:** Android Native (Minimum SDK 28 / Android 9.0 — batas minimum API native `BluetoothHidDevice`).
- **Bahasa & UI Framework:** Kotlin dengan **Jetpack Compose**.
  - `Modifier.pointerInput` dan transformable/drag gesture detector untuk kanvas dinamis.
  - Canvas rendering hardware-accelerated untuk performa 60–120 FPS.
- **Penyimpanan Lokal:** Jetpack DataStore / Room Database untuk menyimpan koordinat $(X, Y)$, skala $(W, H)$, dan kode fungsi masing-masing tombol.

#### 6.2 Model Data Tata Letak Kanvas (Layout Schema)

Setiap tombol direpresentasikan dengan struktur data berikut:

```
{
  "profileId": "prof_photoshop_01",
  "profileName": "Photoshop Shortcuts",
  "buttons": [
    {
      "id": "btn_undo_01",
      "label": "UNDO",
      "positionX": 48.0,
      "positionY": 120.0,
      "width": 160.0,
      "height": 120.0,
      "backgroundColor": "#1E293B",
      "textColor": "#FFFFFF",
      "hidKeyCode": 29,
      "modifiers": ["MODIFIER_LEFT_CTRL", "MODIFIER_LEFT_ALT"],
      "hapticEnabled": true
    }
  ]
}

```

#### 6.3 Mekanisme Konektivitas

##### A. Mode Bluetooth HID (Driverless Direct)

1. Aplikasi memanfaatkan kelas `android.bluetooth.BluetoothHidDevice`.
2. Mendaftarkan SDP (*Service Discovery Protocol*) sebagai keyboard HID baku dengan *Descriptor Keyboard Standar* (Usage Page: `0x01` Generic Desktop, Usage: `0x06` Keyboard).
3. Saat tombol disentuh di Play Mode:
   - Mengirim **HID Input Report** (KeyDown: Modifier Byte + Array 6 Keycodes).
   - Mengirim **HID Release Report** saat jari diangkat (KeyUp: Byte `0x00`).
4. Kompatibel dengan semua sistem operasi laptop/desktop (Windows, macOS, Linux).

##### B. Mode USB Cable (Ultra-Low Latency Bridge)

Karena batasan kernel Android non-root untuk emulasi USB HID mentah:

1. **Jalur Komunikasi:** Transmisi data via protokol **Android Open Accessory (AOA)** atau Socket TCP via `adb reverse` port forwarding.
2. **Desktop Helper (Tray App):** Aplikasi *companion* ringan di laptop (berbasis Go / Rust / Electron) yang menerima paket data dari port USB lalu menginjeksikan keystroke ke sistem operasi laptop melalui library input native OS (seperti `SendInput` di Windows atau `CGEventPost` di macOS).

### 7. Kebutuhan Non-Fungsional (Non-Functional Requirements)

1. **Latensi Input (Responsiveness):**
   - Koneksi USB: Latensi total $\le 10\text{ ms}$ (target ideal $\le 5\text{ ms}$).
   - Koneksi Bluetooth: Latensi total $\le 20\text{ ms}$ (bebas *lag* persepsi tangan).
2. **Keandalan Sentuhan (Multi-touch & Anti-Ghosting):**
   - Mendukung input *multi-touch* simultan hingga 10 titik sentuh secara bersamaan tanpa ada sinyal *stroke* yang hilang (*zero dropped strokes*).
   - Menjamin penanganan status pelepasan tombol secara ketat agar tombol tidak "menggantung" (*stuck key*) di laptop pengguna.
3. **Manajemen Daya & Layar:**
   - Fitur **Keep Screen On** aktif secara default saat Play Mode berjalan agar layar tidak mati saat digunakan bekerja/bermain game.
   - Opsi *True Black AMOLED Theme* untuk menghemat konsumsi daya baterai saat perangkat berada di atas meja kerja.

### 8. Metrik Keberhasilan (Key Performance Indicators)

1. **Setup Success Rate:** $\ge 90\\%$ pengguna berhasil menghubungkan aplikasi ke PC pada sesi pertama (baik via Bluetooth maupun USB).
2. **Customization Depth:** Rata-rata pengguna aktif mengonfigurasi minimal 6 tombol kustom dengan ukuran variatif.
3. **Input Reliability:** $0\\%$ insiden tombol menyangkut (*stuck key*) dalam sesi penggunaan berdurasi $> 60\text{ menit}$.
4. **Active Usage Duration:** Rata-rata sesi pemakaian aktif aplikasi berada di kisaran $> 40\text{ menit}$ per sesi kerja/gaming.

### 9. Rencana Rilis (Product Roadmap)

- **Fase 1 (MVP Foundation):**
  - Canvas engine dengan orientasi landscape terkunci.
  - Fitur tambah tombol, atur posisi $(X,Y)$, dan atur dimensi ukuran tombol.
  - Integrasi Bluetooth HID Device untuk pemetaan tombol alfabet, angka, dan kontrol dasar.
  - Sakelar Play Mode / Edit Mode.
- **Fase 2 (Advanced Controls & USB):**
  - Dukungan Hotkey kombinasi ganda/tiga (`Ctrl`, `Alt`, `Shift`, `Win`).
  - Sistem multi-profil & penyimpanan layout lokal.
  - Integrasi konektivitas kabel USB menggunakan desktop companion helper.
  - Haptic tactile feedback.
- **Fase 3 (Polishing & Ecosystem):**
  - Sistem *Snap-to-Grid* magnetik dan *Smart Alignment*.
  - Kustomisasi visual (palet warna, library ikon vektor, ukuran teks).
  - Fitur ekspor/impor layout via JSON atau QR Code antarperangkat.
