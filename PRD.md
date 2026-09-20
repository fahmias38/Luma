# Product Requirements Document (PRD) - Luma

## 1. Ikhtisar Proyek
**Luma** adalah aplikasi manajemen keuangan pribadi berbasis *mobile native* Android. Aplikasi ini dirancang untuk mencatat arus kas (pemasukan dan pengeluaran), mengelola utang-piutang, serta menghitung pembagian tagihan kelompok (*Split Bill*) yang terintegrasi langsung dengan pencatatan utang.

## 2. Spesifikasi Teknologi & Arsitektur
Proyek ini wajib memenuhi minimal 5 dari 7 kriteria materi Native Android Jetpack Compose:
*   **Bahasa Utama:** Kotlin.
*   **UI Framework:** Jetpack Compose (Material Design 3). Dilarang menggunakan XML Layout. Menggunakan `Column`, `Row`, `Box`, `Modifier`, serta `LazyColumn`/`LazyGrid`.
*   **Arsitektur:** MVVM (Model-View-ViewModel) dengan Unidirectional Data Flow (UDF) dan State Hoisting.
*   **State Management:** `UiState` (Loading, Success, Error) menggunakan `StateFlow`.
*   **Navigasi:** Type-Safe Navigation Compose, `Scaffold`, dan `BottomNavigation` (minimal 3 layar terhubung).
*   **Database & API:** Room (Local DB) dan Supabase (Authentication/Backend), dikelola dengan *Dependency Injection* (Hilt).

## 3. Modul Utama & Feature Ownership
Pengerjaan proyek tidak dibagi berdasarkan *layer* (misal: satu orang khusus UI), melainkan berdasarkan *Feature Ownership* dari UI hingga DAO/API.

*   **Fahri (25%) - Authentication & Profile (PIC Navigasi & Session):**
    *   Register, Login, Logout, Edit Profil, Session Management (Auto-login), dan Navigation Architecture.
*   **Nindy (25%) - Transaction Management (PIC Local Data):**
    *   CRUD Pemasukan & Pengeluaran, Kategori *fixed*, Filter (Riwayat), dan implementasi Room (Transaction Entity & DAO).
*   **Fahmi (25%) - Debt & Receivable + Split Bill (PIC Business Logic):**
    *   Manajemen Utang/Piutang manual, Pembayaran, pembuatan Split Bill, dan logika *auto-generate* utang dari peserta yang belum membayar.
*   **Melysa (25%) - Dashboard & Statistics (PIC UI Components):**
    *   Ringkasan saldo total, total pemasukan/pengeluaran, statistik kategori pengeluaran terbesar, dan pembuatan komponen UI *reusable* (Card, Empty State, Button).

## 4. Alur Pengembangan (Fase Prioritas)
*   **Fase 0:** Project Foundation (Git, Struktur Folder, Navigation Skeleton, Hilt Setup).
*   **Fase 1 (Fahri):** Authentication, Profile, Session Management.
*   **Fase 2 (Nindy):** Transaction Management (CRUD & History).
*   **Fase 3 (Fahmi):** Debt & Receivable (Manual input & Payment).
*   **Fase 4 (Fahmi):** Split Bill (Integrasi ke Debt).
*   **Fase 5 & 6 (Melysa):** Dashboard Aggregation & Financial Statistics.
*   **Fase 7 & 8 (Semua):** Integrasi aliran data, Testing, Bug Fixing, dan Polishing UI.