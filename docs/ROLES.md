# Pembagian Peran & Tanggung Jawab (ROLES) — Tasky

Dokumen ini mengatur pembagian tugas dan tanggung jawab antara **Backend (BE)** dan **Frontend (FE)** dalam pengembangan aplikasi Android **Tasky** berbasis arsitektur **MVVM + Repository**.

---

## 1. Titik Temu & Kontrak Sistem (System Contract)

Dalam arsitektur Android offline-first Tasky, titik potong (*boundary*) antara BE dan FE terletak pada **Repository Interface** dan **Domain Model**.

```
FE (UI & ViewModel) ──[ Mengamati UiState & memanggil Repository ]──> BE (Repository, DAO, Room, Domain Logic)
```

- **BE** bertanggung jawab menyediakan **Repository Interface & Implementation** beserta data `Flow`/`StateFlow` dan method-method pendukungnya.
- **FE** mengonsumsi data dari **Repository** melalui **ViewModel** untuk diubah menjadi `UiState` yang di-render oleh Jetpack Compose.

---

## 2. Peran & Tanggung Jawab Backend (BE)

Fokus utama BE adalah **Data Layer**, **Domain Layer (Business Logic)**, **Database Persistence**, dan **Dependency Injection**.

### 🛠️ Scope Pekerjaan Backend:
1. **Local Database & Storage (Room):**
   - Mendefinisikan **Room Entities** (`TaskEntity`, `CategoryEntity`, `AppSettingsEntity`).
   - Membuat **DAOs (Data Access Objects)** dengan query SQLite yang efisien (menggunakan index pada `categoryId`, `status`, `dueDate`).
   - Mengatur **TypeConverters** (misal: Enum ke String untuk `status` dan `priority`).
   - Pengaturan **Room Database** (`exportSchema = true`) dan menangani **Database Migration** eksplisit (tanpa `fallbackToDestructiveMigration()`).

2. **Domain & Business Logic Layer:**
   - Mendefinisikan **Domain Models** (`Task`, `Category`, `TaskStatus`, `Priority`).
   - Mengimplementasikan aturan bisnis terkunci sesuai PRD:
     - Logika kalkulasi **Overdue, Pending, Completed, dan Total** tanpa tumpang tindih.
     - Penanganan timestamp UTC (epoch millis `Long`) dan penentuan deadline tanpa jam (jatuh tempo 23:59:59 waktu lokal).
     - Transaksi penghapusan kategori: memindahkan task ke kategori sistem **Other** dalam satu transaksi database.
     - Logika pencarian (*search case-insensitive*) dan penyaringan (*filter*).

3. **Repository Pattern:**
   - Menyediakan `TaskRepository` dan `CategoryRepository` (Interface & Implementation).
   - Menyajikan data ke FE dalam bentuk `Flow<List<Task>>` atau `Flow<Task>` agar UI dapat menerima update secara *real-time*.

4. **Dependency Injection (Hilt - Data Level):**
   - Membuat modul Hilt (`DatabaseModule`, `RepositoryModule`) untuk menyuntikkan instance Database, DAO, dan Repository.

5. **Pengujian (Testing - BE):**
   - Menulis Unit Test untuk Repository dan Business Logic.
   - Menulis Instrumented Test untuk Room DAO dan Migration Test (`MigrationTestHelper`).

---

## 3. Peran & Tanggung Jawab Frontend (FE)

Fokus utama FE adalah **UI Layer (Jetpack Compose)**, **ViewModel / State Management**, **Navigasi**, dan **Desain UX/Theme**.

### 🎨 Scope Pekerjaan Frontend:
1. **User Interface (Jetpack Compose & Material 3):**
   - Membuat seluruh layar (*Screens*) sesuai PRD:
     - **Splash & Onboarding Screen**
     - **Dashboard / Home Screen** (Ringkasan Total, Completed, Pending, Overdue + Upcoming Tasks)
     - **Task List Screen** (Daftar task, Filter chip, Search bar)
     - **Create / Edit Task Form Screen** (Form judul, deskripsi, pilih kategori, deadline picker)
     - **Task Detail Screen** (Detail task, tombol aksi Edit/Delete/Complete)
     - **Category Management Screen** (Daftar kategori, tambah/edit/hapus kategori)
     - **Settings Screen** (Pengaturan tema System/Light/Dark, tentang aplikasi)
   - Menyediakan tampilan **Empty State** dan **Error State** yang jelas.

2. **State Management (ViewModel & UiState):**
   - Membuat **ViewModel** untuk tiap layar.
   - Memasangkan data dari Repository ke `StateFlow<UiState>` yang immutable.
   - Menangani **UiEvent / Action** dari interaksi pengguna (misal: klik Save, Delete dengan Snackbar Undo, Toggle Complete).

3. **Design System & Theme:**
   - Mengatur `Theme.kt`, `Color.kt`, `Type.kt` menggunakan Material 3.
   - Mendukung **Dark Mode** (otomatis mengikuti sistem atau toggle manual di Settings).
   - Membuat komponen UI yang reusabel (`TaskCard`, `FilterChip`, `CategoryBadge`, `TopAppBar`, `BottomNavBar`).

4. **Navigation Compose:**
   - Menyusun grafik navigasi (`NavHost`, routes, screen arguments).
   - Mengatur struktur Bottom Navigation 3 Tab (**Home**, **Tasks**, **More**).

5. **Pengujian (Testing - FE):**
   - Membuat Compose Previews untuk tiap komponen UI dan layar.
   - UI / Instrumentation Test untuk komponen Compose jika diperlukan.

---

## 4. Matriks Pembagian Tugas per Milestone (V1 MVP)

| Milestone | Tugas Backend (BE) | Tugas Frontend (FE) |
|---|---|---|
| **M1: Core & Setup** | Room Setup (`TaskEntity`, `CategoryEntity`), Seed default Categories, DAO, `TaskRepository`, Unit test data layer | Setup Compose Theme, Navigation Graph dasar, Hilt ViewModel binding |
| **M2: Task CRUD** | Fungsi Create, Read, Update, Delete, Complete task di Repository + Logika Undo di memori | UI Task List, Form Create/Edit Task, Task Detail Screen, Snackbar Undo |
| **M3: Category & Filter** | Logika hapus kategori (auto-move ke *Other*), query Filter & Search case-insensitive | UI Category Management, UI Filter Chip, Search Bar, Empty state search |
| **M4: Dashboard & Settings** | Query statistik (Total/Completed/Pending/Overdue), `app_settings` DataStore/Room | UI Dashboard ringkasan, Splash & Onboarding, UI Settings & Dark Mode |
| **M5: Polish & Release** | Optimasi query, migrasi database check, pengujian akhir data persistence | Polish UI/UX, animasi transisi, pengujian di perangkat fisik, build APK release |

---

## 5. Aturan Kolaborasi BE & FE

1. **Sepakati Kontrak Dulu:** Sebelum coding fitur baru, BE dan FE harus menyepakati method pada `Repository` interface dan bentuk data `Domain Model` / `UiState`.
2. **Mocking Data:** FE tidak perlu menunggu BE selesai 100%. FE dapat menggunakan data tiruan (*fake/mock repository*) berdasarkan interface yang sudah disepakati.
3. **Commit Convention:** Gunakan prefix commit yang jelas:
   - `feat(be): add room entity and dao for tasks`
   - `feat(fe): build compose task list screen`
   - `docs: add roles documentation`
