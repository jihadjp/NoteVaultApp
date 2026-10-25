# NoteVault DIU — Phase 0 Baseline Audit Report (`NOTEvault_AUDIT_REPORT.md`)

## 1. Executive Summary

This report documents the baseline technical, architectural, UX, and security audit of the **NoteVault DIU** repository (`com.jptechgenius.notevault`) and its companion PHP/MySQL backend (`notevault-backend/`) prior to implementing the Academic Platform Upgrade.

- **Baseline Android Build Status**: Passing (`assembleDebug` succeeds with JDK 21, Android SDK 36, AGP 9.1.1, Gradle 9.3.1).
- **UI Stack**: Java 11 + XML layouts with ViewBinding, Material Components (`1.14.0`), AndroidX AppCompat, ConstraintLayout, CoordinatorLayout, SwipeRefreshLayout, Shimmer, Glide (`5.0.7`), and `io.github.oothp:android-pdf-viewer:3.2.0-beta06` (actively maintained Maven Central fork of `barteksc` PDFViewer).
- **Data Stack**: Room (`2.8.4`), Lifecycle ViewModel + LiveData (`2.11.0`), Retrofit (`3.0.0`) + Gson + OkHttp (`5.4.0`).
- **Backend Stack**: PHP 7.4+/8.x front-controller (`notevault-backend/index.php`) with PDO MySQL (`notevault-backend/config/database.php`).

---

## 2. Verified Android Architecture & Source Inventory

### 2.1 Activities & ViewModels
| Component | Path | Status & Audit Findings |
|---|---|---|
| `NoteVaultApp` | `NoteVaultApp.java` | Creates notification channel `download_channel`. |
| `SplashActivity` | `ui/splash/SplashActivity.java` | Animated splash screen (2000ms delay) launching `HomeActivity`. |
| `HomeActivity` | `ui/home/HomeActivity.java` | Displays 3 tabs (`Notes`, `Questions`, `Saved`), search bar, department/course filter card, and RecyclerView. **Issues found**: `fabUpload` click listener is not wired; `onDeleteNote` and `onMoreOptions` are empty no-op methods; `offlineBanner` visibility is never updated from `viewModel.isOffline`; `viewModel.downloadingNoteId` is not observed to update `NoteAdapter`. |
| `HomeViewModel` | `ui/home/HomeViewModel.java` | Manages `isLoading`, `currentTab`, `questions`, `toastMessage`, `isOffline`, `downloadingNoteId`. **Issues found**: Uses `observeForever` on `repository.fetchNotesFromServer()` without removing the observer; `searchNotes()` in `HomeActivity` attaches new observers on every keystroke without debounce or observer cleanup. |
| `NoteDetailActivity` | `ui/detail/NoteDetailActivity.java` | Displays PDF via `PDFView`, top toolbar, and bottom floating action bar (`Download`, `Share`, `Info`, `Delete File`). **Issues found**: Streams remote PDFs to `getCacheDir()/temp_view.pdf` using a raw `new OkHttpClient()` without timeout config or cancellation on `onDestroy()`; does not save or restore last-read page (`defaultPage`); `btnCopyPath` in `bottom_sheet_note_info.xml` is never wired; no bookmark, rating/review, or content reporting action. |
| `NoteDetailViewModel` | `ui/detail/NoteDetailViewModel.java` | Manages single-note state. **Issues found**: `loadNote(int noteId)` uses `observeForever` without cleanup in `onCleared()`. |

### 2.2 Local Persistence (Room)
- `AppDatabase.java`: Room database (`notevault_db`, version 2) with `fallbackToDestructiveMigration()` and a single entity: `NoteEntity`.
- `NoteEntity.java`: Stores `id`, `title`, `description`, `subject`, `courseCode`, `departmentName`, `uploadedBy`, `uploadedAt`, `serverUrl`, `localFilePath`, `isDownloaded`, `fileSize`, `downloadCount`, `likeCount`, `dislikeCount`, `thumbnailUrl`, `savedAt`.
- **Missing Local Capabilities**:
  - No bookmark state (`isBookmarked`) or bookmark queries.
  - No reading progress fields (`lastReadPage`, `totalPages`, `lastOpenedAt`) for "Continue Studying".
  - No academic hierarchy metadata (`universityName`, `semesterName`, `batchName`, `resourceCategory`, `isVerified`, `moderationStatus`, `ratingAvg`, `ratingCount`).
  - No personal course tracking (`My Courses` table) or personal `Study Planner / Exam Countdown` table.

### 2.3 Remote API Contracts (Retrofit vs PHP Backend)
| Android `ApiService` Call | Backend Route (`index.php`) | Audit Finding |
|---|---|---|
| `GET api/notes` | `GET /api/notes` (`api/notes/index.php`) | Matches. Returns JSON array of approved notes in camelCase (`NoteResponse`). |
| `GET api/notes/{id}` | `GET /api/notes/{id}` (`api/notes/show.php`) | Matches. Returns single `NoteResponse` object. |
| `GET api/notes/search?q=` | `GET /api/notes/search?q=` (`api/notes/search.php`) | Matches. Searches `title`, `course_code`, `course_name`, `department_name`, `description`, `user_name`. |
| `GET api/questions` | `GET /api/questions` (`api/questions/index.php`) | Matches. Queries `all_questions` table (`department`, `course`, `semester`, `type`). Note: `database.sql` was missing the `all_questions` table DDL. |
| *(Not in `ApiService`)* | `POST /api/notes` (`api/notes/store.php`) | Exists in PHP backend (`multipart/form-data`), inserts with `status = 'PENDING'`, but not wired in Android `ApiService` or UI. |
| *(Not in `ApiService`)* | `POST /api/notes/{id}/download` (`api/notes/download.php`) | Exists in PHP backend to increment `view_count`, not called by Android client after download. |
| *(Not in `ApiService`)* | `DELETE /api/notes/{id}` (`api/notes/delete.php`) | Soft-deletes note (`status = 'REJECTED'`) without any authentication or ownership check. |

---

## 3. Security & Privacy Audit Findings

1. **CRITICAL — Hardcoded Database Credentials in Backend (`notevault-backend/config/database.php`)**:
   - Contains plaintext MySQL username and password (`jihadjp1_notevault_diu` / `notevault_diu#jihadjp`) and outputs `$e->getMessage()` in JSON responses on connection failure.
   - **Remediation**: Load credentials from environment variables (`getenv('DB_HOST')`, `getenv('DB_NAME')`, `getenv('DB_USER')`, `getenv('DB_PASS')`) and suppress internal PDO exception details in responses.
2. **HIGH — Production Error Display Enabled (`notevault-backend/index.php`)**:
   - `ini_set('display_errors', 1)` and `error_reporting(E_ALL)` are enabled unconditionally at the top of `index.php`, leaking file paths and stack traces.
   - **Remediation**: Disable `display_errors` unless `APP_DEBUG=true` is explicitly set in the environment.
3. **HIGH — Unauthenticated Resource Deletion (`notevault-backend/api/notes/delete.php`)**:
   - Anyone can send `DELETE /api/notes/{id}` and mark any approved resource as `REJECTED`.
   - **Remediation**: Require valid authorization token and verify either resource ownership or Moderator/Admin role before permitting deletion/withdrawal.
4. **MEDIUM — Upload File Signature Validation (`notevault-backend/api/notes/store.php`)**:
   - Checks `finfo_file` MIME type, but does not explicitly verify the `%PDF-` magic header bytes or prevent double extensions.
   - **Remediation**: Verify `%PDF-` header signature for PDF uploads, enforce strict extension whitelist, and generate cryptographically random filenames (`bin2hex(random_bytes(16))`).
5. **LOW — Verbose Network Logging in Release (`RetrofitClient.java`)**:
   - `HttpLoggingInterceptor.Level.BODY` is enabled unconditionally, which can log binary PDF responses or tokens in production builds.
   - **Remediation**: Use `Level.BASIC` and avoid logging sensitive headers or binary streams.

---

## 4. UI/UX & Localization Audit Findings

1. **Theme & Color Tokens**:
   - Current app uses Indigo (`#4F46E5`) and Amber (`#F59E0B`).
   - Needs migration to the **Premium Dark Emerald / Deep Teal** academic palette (`#086B61` Primary, `#06463F` Primary Dark, `#14B8A6` Accent, `#F6F8FA` Light Background, `#101817` Dark Background, `#FFFFFF` Light Surface, `#192321` Dark Surface) with full DayNight theme support.
2. **Localization**:
   - Many strings in `activity_home.xml`, `item_note_card.xml`, `bottom_sheet_note_info.xml`, `layout_filter_notes.xml`, and `HomeActivity.java` are hardcoded in English.
   - Missing Bengali localization (`app/src/main/res/values-bn/strings.xml`) and in-app language switcher.
3. **Navigation & Workspace**:
   - Needs structured navigation across **Home**, **Explore**, **Downloads**, **Bookmarks/Workspace**, and **Profile**, plus academic context persistence (University, Department, Batch/Semester) defaulting to Daffodil International University (DIU).
