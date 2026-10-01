# Tasky

Aplikasi Android **offline-first** untuk mengelola tugas, target, dan produktivitas sehari-hari.

Tasky dimulai sebagai To-Do List yang benar-benar usable, lalu bertahap berkembang menjadi personal productivity management system: kategori, deadline, prioritas, goal/project, reminder, hingga statistik dan insight produktivitas.

> **Status:** 🚧 V1 (MVP) dalam pengembangan

---

## Fitur

### V1 — Core (target saat ini)
- Splash dan onboarding
- Dashboard ringkasan: Total, Completed, Pending, Overdue
- Buat, ubah, hapus (dengan konfirmasi + undo), dan selesaikan task
- Kategori (bisa dikelola sendiri)
- Deadline tanggal dan jam (opsional)
- Filter (status, kategori) dan pencarian
- Dark mode mengikuti sistem
- Seluruh data tersimpan lokal, **tanpa internet**

### Roadmap

| Versi | Fokus | Fitur Utama |
|---|---|---|
| V1 | Core / MVP | To-Do offline, kategori, deadline, filter, dashboard |
| V2 | Productivity | Priority, recurring task, goals, project, calendar, reminder |
| V3 | Analytics | Statistik harian/mingguan/bulanan, grafik, laporan *(prototype)* |
| V4 | Advanced | Streak, riwayat aktivitas, export, insight, cloud backup/sync |

Urutan pengerjaan: **V1 selesai dulu, baru V2.** V3 adalah target stretch.

---

## Tech Stack

| Layer | Teknologi |
|---|---|
| Bahasa | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Arsitektur | MVVM + Repository |
| State | ViewModel + StateFlow |
| Database | Room (SQLite) |
| Async | Kotlin Coroutines + Flow |
| Navigasi | Navigation Compose |
| DI | Hilt |
| Build | Gradle (Kotlin DSL) + Version Catalog |

Ditambahkan di versi lanjutan: WorkManager dan Notification API (V2), Vico untuk grafik (V3).

---

## Arsitektur

```
Jetpack Compose (UI)
        ↓
ViewModel (StateFlow / UiState)
        ↓
Repository
        ↓
DAO (Room)
        ↓
SQLite
```

Detail lengkap ada di [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## Struktur Proyek

```
Tasky/
├── docs/                 ← dokumentasi (PRD, arsitektur, database, user flow)
├── app/
│   ├── schemas/          ← hasil export skema Room (wajib di-commit)
│   └── src/
│       ├── main/java/com/yourname/tasky/
│       │   ├── data/         ← local (entity, DAO, database) + repository
│       │   ├── domain/       ← model dan aturan bisnis (mis. definisi overdue)
│       │   ├── di/           ← modul Hilt
│       │   ├── ui/           ← navigation, theme, components, dan layar per fitur
│       │   └── util/
│       ├── test/             ← unit test
│       └── androidTest/      ← DAO test dan migration test
├── gradle/libs.versions.toml
└── build.gradle.kts
```

---

## Persyaratan

- Android Studio / Antigravity dengan Android SDK terpasang
- JDK 17
- Perangkat Android atau emulator (minSdk ditentukan di `app/build.gradle.kts`)
- ADB untuk instalasi ke perangkat fisik

## Menjalankan Proyek

```bash
# Clone
git clone https://github.com/<username>/tasky.git
cd tasky

# Build APK debug
./gradlew assembleDebug

# Install ke perangkat/emulator yang terhubung
./gradlew installDebug
# atau
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Jalankan unit test
./gradlew test

# Jalankan instrumented test (butuh perangkat/emulator)
./gradlew connectedAndroidTest
```

---

## Keputusan Teknis Penting

Ringkasan; versi lengkap ada di [`docs/PRD.md`](docs/PRD.md) bagian 4.

- **Tanggal:** disimpan sebagai epoch millis UTC (`Long`); ditampilkan sesuai timezone perangkat.
- **Status task V1:** hanya `TODO` dan `COMPLETED`.
- **Overdue:** belum selesai **dan** deadline sudah lewat. Deadline tanpa jam berakhir pukul 23:59:59 waktu lokal.
- **Pending:** belum selesai dan belum overdue. `Total = Completed + Pending + Overdue`.
- **Hapus kategori:** task di dalamnya dipindah ke kategori **Other** (kategori sistem, tidak bisa dihapus).
- **Migrasi database:** `exportSchema = true`, migrasi eksplisit, **tanpa** `fallbackToDestructiveMigration()`.

---

## Dokumentasi

| File | Isi |
|---|---|
| [`docs/PRD.md`](docs/PRD.md) | Product requirement, scope, acceptance criteria, keputusan teknis |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Arsitektur, layer, dan konvensi kode |
| [`docs/DATABASE.md`](docs/DATABASE.md) | Skema Room, relasi, dan strategi migrasi |
| [`docs/USER_FLOW.md`](docs/USER_FLOW.md) | Alur pengguna dan navigasi |

---

## Konvensi Pengembangan

- Satu milestone selesai dan diuji sebelum lanjut ke milestone berikutnya.
- UI hanya mengamati `UiState`; tidak ada akses DAO langsung dari ViewModel atau Composable.
- Logika bisnis berada di Repository atau `domain/`, bukan di Composable.
- Setiap perubahan skema database wajib disertai `Migration` dan file schema baru di `app/schemas/`.
- Commit kecil dan sering, misalnya dengan format `feat:`, `fix:`, `refactor:`, `docs:`.

## Milestone V1

| Milestone | Isi |
|---|---|
| M1 | Setup proyek, Hilt, Room, seed kategori, CRUD task |
| M2 | UI Task List, form Create/Edit, Task Detail |
| M3 | Kategori, filter, search |
| M4 | Dashboard, splash, onboarding, settings, dark mode |
| M5 | Polish, empty/error state, uji di HP, build APK |

---

## Lisensi

Belum ditentukan. Tambahkan file `LICENSE` jika proyek akan dipublikasikan.
