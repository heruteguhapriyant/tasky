# PRD — Tasky (Revisi 1)

| Informasi | Detail |
|---|---|
| Nama Produk | Tasky |
| Platform | Android |
| Jenis Aplikasi | Productivity / Task Management |
| Model | Offline-first |
| Bahasa | Kotlin |
| UI | Jetpack Compose |
| Arsitektur | MVVM + Repository |
| Database | Room (SQLite) |
| Versi Saat Ini | V1 — MVP |
| Status Dokumen | Revisi 1 — keputusan teknis dikunci, inkonsistensi diperbaiki |

> **Ringkasan perubahan dari versi sebelumnya**
> - Ditambah bagian **Keputusan Teknis Terkunci** (bagian 4).
> - Status `In Progress` dan tab **Stats** ditunda dari V1.
> - Kolom `priority` tetap dibuat di V1 (default `MEDIUM`), UI-nya baru V2.
> - Ditambah **acceptance criteria** per fitur, **search**, **dark mode**, **empty/error state**, dan **metrik keberhasilan**.
> - Timeline dipecah jadi milestone; V3 dijadikan target stretch, bukan komitmen.

---

## 1. Product Overview

Tasky adalah aplikasi Android untuk mengelola aktivitas dan tugas sehari-hari secara offline. Tasky dimulai sebagai To-Do List yang benar-benar usable, lalu bertahap berkembang menjadi personal productivity management system: task, kategori, deadline, prioritas, goal/project, recurring task, reminder, statistik, dan insight.

## 2. Problem Statement

Aplikasi To-Do sederhana hanya mencatat tugas, tanpa gambaran jelas tentang:

- Berapa tugas yang selesai dan yang terlambat.
- Aktivitas pengguna dari waktu ke waktu.
- Kategori yang paling sering dikerjakan.
- Produktivitas harian, mingguan, dan bulanan.

Selain itu, pengguna tidak selalu punya koneksi internet saat ingin mencatat tugas. Tasky memakai pendekatan offline-first sehingga fungsi utama selalu tersedia.

## 3. Goals dan Metrik Keberhasilan

**Primary goals**

1. Task management yang bisa dipakai sepenuhnya offline.
2. Pengelolaan task yang sederhana dan cepat (buat task dalam langkah minimal).
3. Data tersimpan lokal dan tidak hilang.
4. Ringkasan aktivitas di dashboard.

**Secondary goals**

- Goal & Project, recurring task, reminder, laporan produktivitas, backup/sync (versi lanjutan).

**Metrik keberhasilan (proyek pribadi)**

- V1 dianggap berhasil jika developer memakainya sendiri **setiap hari selama 2 minggu** tanpa kehilangan data dan tanpa crash yang mengganggu.

## 4. Keputusan Teknis Terkunci

Bagian ini adalah sumber kebenaran. Jangan diubah di tengah jalan tanpa mencatat alasannya.

### 4.1 Tanggal dan waktu
- Semua timestamp (`dueDate`, `createdAt`, `updatedAt`, `completedAt`) disimpan sebagai **epoch millis UTC (`Long`)**.
- Konversi ke tampilan memakai timezone perangkat saat runtime.
- `dueDate` bersifat **opsional** (nullable). Field tambahan `hasDueTime: Boolean` menandai apakah deadline menyertakan jam.
- Deadline tanpa jam dianggap jatuh tempo pada **23:59:59 waktu lokal** di tanggal tersebut.

### 4.2 Status task
- V1 hanya memakai dua status: **`TODO`** dan **`COMPLETED`**.
- `IN_PROGRESS` ditunda ke V2 (jika terbukti dibutuhkan).
- Enum disimpan sebagai string lewat `TypeConverter` agar penambahan nilai baru tidak butuh migrasi.

### 4.3 Definisi Dashboard (tidak boleh tumpang tindih)
- **Completed**: `status == COMPLETED`.
- **Overdue**: `status != COMPLETED` **dan** `dueDate != null` **dan** `dueDate < sekarang` (setelah aturan 4.1).
- **Pending**: `status != COMPLETED` dan **bukan** overdue.
- **Total** = Completed + Pending + Overdue.
- Task tanpa deadline yang belum selesai dihitung sebagai Pending.

### 4.4 Priority
- Kolom `priority` (`LOW`/`MEDIUM`/`HIGH`) **sudah dibuat di V1** dengan default `MEDIUM` untuk menghindari migrasi.
- UI untuk mengubah dan memfilter priority baru tersedia di V2.

### 4.5 Aturan Category
- Saat pertama kali dijalankan, aplikasi membuat kategori default: Study, Work, Programming, Personal, Finance, Health, **Other**.
- Kategori **Other** bersifat sistem: tidak bisa dihapus atau diubah namanya.
- Menghapus kategori lain: dialog konfirmasi menampilkan jumlah task terdampak; semua task di dalamnya **dipindahkan ke Other** (dalam satu transaksi database di repository).
- Nama kategori: wajib, unik (case-insensitive), maksimal 30 karakter.

### 4.6 Hapus task
- Selalu ada **konfirmasi** sebelum hapus dari halaman detail.
- Setelah dihapus muncul Snackbar dengan aksi **Undo** (beberapa detik).
- V1 memakai hard delete; undo diimplementasikan dengan menahan data di memori sampai Snackbar hilang.

### 4.7 Database dan migrasi
- `exportSchema = true`; folder schema di-commit ke Git.
- **Dilarang** memakai `fallbackToDestructiveMigration()`.
- Setiap perubahan skema memakai `Migration` eksplisit, minimal diuji dengan `MigrationTestHelper` sebelum rilis V2.
- Versi database V1 = 1.

### 4.8 Tema
- Light/dark mengikuti sistem secara default; opsi manual (System/Light/Dark) ada di Settings dan disimpan di `app_settings`.

## 5. Target User

| User | Kebutuhan |
|---|---|
| Pelajar | Mengatur tugas belajar dan deadline |
| Mahasiswa | Mengatur tugas kuliah, project, dan skripsi |
| Developer | Mengatur task/project pribadi |
| Pekerja | Mengelola pekerjaan harian |
| Pengguna umum | Mengatur aktivitas sehari-hari |

## 6. Scope per Versi

### V1 — Core / MVP
Fokus: aplikasi To-Do offline yang benar-benar usable.

| Fitur | Deskripsi | Prioritas |
|---|---|---|
| Splash | Tampilan awal + inisialisasi (seed kategori) | Must |
| Onboarding | Pengenalan singkat, hanya tampil sekali | Must |
| Dashboard | Ringkasan aktivitas | Must |
| Task List | Daftar semua task | Must |
| Create / Edit Task | Form task | Must |
| Delete Task | Dengan konfirmasi dan undo | Must |
| Task Detail | Detail dan aksi task | Must |
| Complete Task | Tandai selesai / batal selesai | Must |
| Category | Kelola kategori | Must |
| Deadline | Tanggal (dan jam opsional) | Must |
| Filter | Berdasarkan status dan kategori | Must |
| Search | Cari berdasarkan judul | Should |
| Basic Summary | Total / Completed / Pending / Overdue | Must |
| Settings | Tema, tentang aplikasi | Should |
| Empty/Error State | Tampilan kosong dan gagal yang jelas | Must |
| Dark Mode | Mengikuti sistem | Should |

### V2 — Productivity
Priority (UI), Recurring Task, Goals, Project, Calendar, Reminder (WorkManager + notifikasi lokal, tanpa internet), evaluasi status `IN_PROGRESS`.

### V3 — Analytics *(target stretch, bukan komitmen)*
Statistik harian/mingguan/bulanan, completion rate, overdue statistics, category statistics, grafik (Vico), laporan completion dan category. Tab **Stats** baru muncul di bottom navigation pada versi ini.

### V4 — Advanced *(di luar target bulan pertama)*
Streak, activity history, export CSV/PDF, productivity insights, cloud backup, cloud sync.

## 7. Navigation

**V1 — 3 tab** (tidak ada tab kosong):

```
┌──────────────────────────────┐
│         PAGE CONTENT         │
├──────────┬──────────┬────────┤
│   Home   │  Tasks   │  More  │
│    🏠    │    ✓     │   ⋯    │
└──────────┴──────────┴────────┘
```

- **Home**: Dashboard.
- **Tasks**: Task List, filter, search, dan akses ke detail.
- **More**: Category, Settings, dan fitur tambahan.

**V3+**: tab **Stats** ditambahkan di antara Tasks dan More. Goals, Project, Calendar, dan Reminder ditempatkan di More (atau dipromosikan ke tab sesuai penggunaan).

## 8. User Flow V1

```
Buka Aplikasi
     ↓
   Splash ──(pertama kali)──→ Onboarding
     ↓                            ↓
     └───────────→ Dashboard ←────┘
                      ↓
                  Task List
             ┌────────┴────────┐
             ↓                 ↓
        Create Task       Task Detail
             ↓                 ↓
           Save        Edit / Delete / Complete
             ↓                 ↓
             └───→ Dashboard ter-update
```

## 9. Acceptance Criteria V1

**Create Task**
- Title wajib, 1–100 karakter (spasi di ujung di-trim). Tombol Save nonaktif jika title kosong.
- Description opsional, maksimal 1000 karakter.
- Category wajib; default kategori terakhir dipakai atau **Other**.
- Deadline opsional; jika jam tidak dipilih, berlaku aturan 4.1.
- Setelah Save, kembali ke halaman sebelumnya dan task langsung muncul di list.

**Edit Task**
- Semua field bisa diubah; `updatedAt` diperbarui.
- Meninggalkan form dengan perubahan belum tersimpan menampilkan konfirmasi.

**Complete Task**
- Toggle satu ketukan dari list maupun detail.
- Saat selesai, `completedAt` diisi; saat dibatalkan, `completedAt` dikosongkan dan status kembali `TODO`.
- Angka Dashboard berubah tanpa refresh manual.

**Delete Task**
- Sesuai aturan 4.6.

**Category**
- Sesuai aturan 4.5; daftar kategori menampilkan jumlah task per kategori.

**Filter dan Search**
- Filter status: Semua / Pending / Completed / Overdue; filter kategori: satu atau Semua.
- Filter dan search bisa dikombinasikan; search tidak sensitif huruf besar/kecil.
- Hasil kosong menampilkan empty state khusus ("Tidak ada task yang cocok").

**Dashboard**
- Menampilkan Total, Completed, Pending, Overdue sesuai definisi 4.3.
- Menampilkan hingga 5 **Upcoming Tasks** (belum selesai, deadline terdekat; task tanpa deadline tidak ditampilkan).
- Data dibaca langsung dari Room lewat `Flow`.

**Empty / Error State**
- Empty state untuk: task list kosong, hasil filter/search kosong, dashboard tanpa data.
- Error operasi database menampilkan Snackbar dengan pesan yang jelas; aplikasi tidak crash.

## 10. Screen Requirements

**V1**: Splash, Onboarding, Dashboard, Task List, Create/Edit Task, Task Detail, Category, Settings.
**V2**: Priority, Recurring Task, Goals, Goal Detail, Project, Calendar, Reminder.
**V3**: Statistics (Daily/Weekly/Monthly), Reports (Overview/Completion/Category).
**V4**: Streak, Activity History, Export, Productivity Insights.

## 11. Data Model

### V1

```
categories 1 ──── N tasks
app_settings (key-value, berdiri sendiri)
```

**`categories`**

| Kolom | Tipe | Catatan |
|---|---|---|
| id | Int (PK, auto) | |
| name | String | Unik, maks 30 karakter |
| icon | String | Emoji |
| isSystem | Boolean | `true` untuk **Other** |
| createdAt | Long | Epoch millis |

**`tasks`**

| Kolom | Tipe | Catatan |
|---|---|---|
| id | Int (PK, auto) | |
| title | String | 1–100 karakter |
| description | String? | Maks 1000 karakter |
| categoryId | Int (FK) | Index; ke `categories.id` |
| status | String (enum) | `TODO` / `COMPLETED` |
| priority | String (enum) | Default `MEDIUM` |
| dueDate | Long? | Epoch millis UTC |
| hasDueTime | Boolean | Default `false` |
| createdAt | Long | |
| updatedAt | Long | |
| completedAt | Long? | |

Index yang disarankan: `categoryId`, `status`, `dueDate`.

**`app_settings`**: `key: String (PK)`, `value: String` (contoh: `theme_mode`, `onboarding_done`).

Tabel `users` tidak diperlukan (single-user, offline).

### V2 — tambahan
`goals`, `projects`, `recurring_tasks`, `reminders` (via Migration eksplisit, lihat 4.7).

### V4 — jika cloud sync dibuat
`users`, `sync_metadata`, `remote_tasks`, dan arsitektur berubah menjadi: SQLite ↔ Sync Layer ↔ REST API ↔ PostgreSQL.

## 12. Offline Requirement

| Aktivitas | Butuh Internet |
|---|---|
| Create / Edit / Delete / Complete task | ❌ |
| Category, Dashboard, Search, Filter | ❌ |
| Statistics (V3) | ❌ |
| Reminder (V2) | ❌ |
| Cloud Backup / Sync (V4) | ✅ |

## 13. Technical Architecture

```
Jetpack Compose (UI)
        ↓  event
ViewModel (StateFlow / UiState)
        ↓
Repository
        ↓
DAO (Room)
        ↓
SQLite
```

Aturan: UI hanya mengamati `UiState`; ViewModel tidak mengakses DAO langsung; logika bisnis (definisi overdue, pemindahan kategori saat hapus) berada di Repository/domain, bukan di Composable.

## 14. Tech Stack

| Layer | Teknologi | Mulai |
|---|---|---|
| Editor | Antigravity | V1 |
| Language | Kotlin | V1 |
| UI | Jetpack Compose (Material 3) | V1 |
| Architecture | MVVM | V1 |
| State | ViewModel + StateFlow | V1 |
| Database | Room + SQLite | V1 |
| Async | Kotlin Coroutines + Flow | V1 |
| Navigation | Navigation Compose | V1 |
| DI | Hilt | V1 |
| Background Task | WorkManager | V2 |
| Notification | Android Notification API | V2 |
| Charts | Vico | V3 |
| Build | Gradle + Wrapper | V1 |
| Tools | Android SDK CLI, ADB | V1 |
| VCS | Git + GitHub | V1 |

## 15. Non-Functional Requirements

| Requirement | Target |
|---|---|
| Offline | Fitur inti berjalan tanpa internet |
| Performance | Scroll list 500+ task tetap mulus; query memakai index; `LazyColumn` dengan key |
| Data Persistence | Data tetap ada setelah aplikasi ditutup atau di-kill |
| Reliability | Tidak ada kehilangan task saat relaunch; operasi tulis dalam transaksi bila lebih dari satu tabel |
| Usability | Buat task ≤ 3 langkah dari Task List |
| Privacy | Data V1 hanya di perangkat, tanpa izin internet |
| Maintainability | MVVM + Repository, tanpa logika bisnis di UI |
| Scalability | Skema dan migrasi siap untuk V2–V4 |

## 16. Definition of Done — V1

- [ ] APK dapat di-install dan diuji di HP Android sungguhan.
- [ ] Splash dan onboarding berjalan (onboarding hanya sekali).
- [ ] Create, edit, delete (dengan konfirmasi + undo), detail, dan complete task berfungsi sesuai acceptance criteria.
- [ ] Category: buat, ubah, hapus (task pindah ke Other), jumlah task per kategori tampil.
- [ ] Deadline dan definisi overdue sesuai bagian 4.
- [ ] Filter dan search berfungsi dan bisa dikombinasikan.
- [ ] Dashboard menampilkan Total/Completed/Pending/Overdue tanpa hitung ganda.
- [ ] Empty state dan error state tersedia.
- [ ] Dark mode berfungsi.
- [ ] Data tersimpan di Room dan tetap ada setelah aplikasi ditutup.
- [ ] Skema Room di-export dan di-commit; tidak ada `fallbackToDestructiveMigration()`.
- [ ] Aplikasi dapat digunakan tanpa internet.

## 17. Roadmap dan Milestone

**V1 dikerjakan berurutan. V2 baru dimulai setelah Definition of Done V1 terpenuhi.**

| Milestone | Isi | Estimasi |
|---|---|---|
| M1 | Project setup, Hilt, Room (entity, DAO, repository), seed kategori, CRUD task diuji tanpa UI bagus | Minggu 1 (awal) |
| M2 | UI Task List + form Create/Edit + Task Detail + complete/delete/undo | Minggu 1 (akhir) |
| M3 | Category + filter + search | Minggu 2 (awal) |
| M4 | Dashboard, splash, onboarding, settings, dark mode | Minggu 2 (tengah) |
| M5 | Polish (empty/error state), pengujian di HP, build APK | Minggu 2 (akhir) |
| V2 | Priority UI, recurring, goals, project, calendar, reminder | Minggu 3–4 (lebih realistis bila diperpanjang) |
| V3 | Analytics — **prototype stretch, bukan komitmen** | Setelah V2 stabil |
| V4 | Advanced | Bulan berikutnya |

Bila M1–M5 memakan waktu lebih lama dari perkiraan, geser V2 dan V3; jangan memangkas kualitas V1.

## 18. Risiko

| Risiko | Mitigasi |
|---|---|
| Belajar Room + Hilt + Compose Navigation sekaligus | Kerjakan per milestone; selesaikan M1 sebelum UI |
| Scope creep ke V2 sebelum V1 rapi | Aturan "V1 selesai dulu" |
| Refactor besar karena keputusan tergantung | Bagian 4 dikunci sebelum coding |
| Migrasi database merusak data | Schema export, migrasi eksplisit, uji migrasi |

## 19. Product Direction

Target akhir Tasky:

> Aplikasi Android offline untuk mengelola aktivitas, target, dan produktivitas pengguna serta memberikan gambaran perkembangan produktivitas berdasarkan data aktivitas.

To-Do adalah fondasi; rekap, statistik, dan insight adalah nilai tambah utama.

## 20. Rencana Pemecahan Dokumen

Sebelum coding di Antigravity, pecah dokumen ini menjadi:

- `README.md` — overview, cara build, dan cara menjalankan.
- `ARCHITECTURE.md` — bagian 13–14.
- `DATABASE.md` — bagian 4.1–4.7 dan 11.
- `USER_FLOW.md` — bagian 7–10.
