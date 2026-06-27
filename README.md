# NoteVault DIU 📚

**A PDF note sharing and offline storage app for Daffodil International University.**  
Built with MVVM architecture, Room database, Retrofit, and barteksc PDF Viewer.

---

## 📦 Project Structure

```
com.jptechgenius.notevault/
├── data/
│   ├── local/
│   │   ├── dao/NoteDao.java              — Room DAO (CRUD + search queries)
│   │   ├── entity/NoteEntity.java        — Room entity (notes table)
│   │   └── database/AppDatabase.java     — Room singleton database
│   ├── remote/
│   │   ├── api/ApiService.java           — Retrofit interface
│   │   ├── model/NoteResponse.java       — API response POJO (Gson)
│   │   └── RetrofitClient.java           — OkHttp + Retrofit singleton
│   └── repository/NoteRepository.java    — Single source of truth
├── ui/
│   ├── splash/SplashActivity.java        — Animated splash → HomeActivity
│   ├── home/
│   │   ├── HomeActivity.java             — Tabs, search, RecyclerView, dialogs
│   │   ├── HomeViewModel.java            — LiveData state for home screen
│   │   └── NoteAdapter.java              — ListAdapter + DiffUtil for cards
│   └── detail/
│       ├── NoteDetailActivity.java       — PDF viewer (local + streaming)
│       └── NoteDetailViewModel.java      — State for download, pages, error
├── receiver/DownloadReceiver.java        — Manifest BroadcastReceiver
└── utils/
    ├── AppExecutors.java                 — diskIO / networkIO / mainThread pools
    ├── Constants.java                    — BASE_URL, DB_NAME, extras, etc.
    ├── FileUtils.java                    — formatSize, fileExists, shareableUri
    ├── NetworkUtils.java                 — isNetworkAvailable, isWifiConnected
    └── DownloadCompleteListener.java     — Download callback interface
```

---

## 🎨 Design System

| Token | Value |
|-------|-------|
| `colorPrimary` | `#6C63FF` (purple) |
| `colorPrimaryDark` | `#4B44CC` |
| `colorAccent` | `#FF6584` (pink) |
| `colorBackground` | `#F5F6FA` |
| `colorDownloaded` | `#E8F5E9` (light green) |
| Card corner radius | `16dp` |
| Button corner radius | `12dp` |
| Bottom sheet corners | `24dp` (top only) |

---

## ⚙️ Setup Instructions

### 1. Clone / Import
Open in Android Studio (Hedgehog or newer recommended).

### 2. Configure your API base URL
In `utils/Constants.java`:
```java
public static final String BASE_URL = "https://your-api-server.com/";
```

### 3. Expected API shape
```json
// GET /api/notes
[
  {
    "id": 1,
    "title": "Data Structures Notes",
    "description": "Chapter 1-5 summary",
    "subject": "CSE 2101",
    "uploaded_by": "Rahim",
    "uploaded_at": "2024-01-15",
    "pdf_url": "https://example.com/files/ds_notes.pdf",
    "file_size": 2457600,
    "download_count": 1234,
    "thumbnail_url": "https://example.com/thumbs/ds_notes.jpg"
  }
]
```

### 4. JitPack (for PDF viewer)
Already included in `settings.gradle`:
```groovy
maven { url 'https://jitpack.io' }
```

### 5. Build & run
```
./gradlew assembleDebug
```

---



# PHP Backend

## 📁 ফোল্ডার Structure

```
notevault-backend/
├── index.php                  ← Front controller (সব request এখানে আসে)
├── .htaccess                  ← Apache URL rewriting
├── database.sql               ← Database + sample data
├── config/
│   ├── database.php           ← MySQL connection
│   └── helpers.php            ← CORS, JSON response, utilities
├── api/
│   └── notes/
│       ├── index.php          ← GET  /api/notes
│       ├── show.php           ← GET  /api/notes/{id}
│       ├── search.php         ← GET  /api/notes/search?q=
│       ├── store.php          ← POST /api/notes (upload)
│       ├── download.php       ← POST /api/notes/{id}/download
│       └── delete.php         ← DELETE /api/notes/{id}
└── uploads/
    ├── pdfs/                  ← uploaded PDF files এখানে যাবে
    └── thumbnails/            ← thumbnail images এখানে যাবে
```

---

## ⚙️ Setup — Step by Step

### Step 1 — Database তৈরি করো
```sql
-- phpMyAdmin বা MySQL CLI তে run করো:
source database.sql
```
অথবা phpMyAdmin → Import → `database.sql` file select করো।

### Step 2 — Database config বদলাও
`config/database.php` খোলো:
```php
define('DB_HOST', 'localhost');
define('DB_NAME', 'notevault_diu');
define('DB_USER', 'root');        // তোমার username
define('DB_PASS', '');            // তোমার password
```

### Step 3 — Server এ upload করো
পুরো `notevault-backend/` folder তোমার server এর `public_html/` বা `www/` এ দাও।

### Step 4 — Android এ URL বদলাও
`Constants.java` তে:
```java
public static final String BASE_URL = "https://yourdomain.com/";
//                                              ↑ তোমার real domain
```

### Step 5 — uploads folder permission দাও
```bash
chmod 755 uploads/
chmod 755 uploads/pdfs/
chmod 755 uploads/thumbnails/
```

---

## 🌐 API Endpoints

| Method | Endpoint | কী করে |
|--------|----------|--------|
| `GET` | `/api/notes` | সব notes পাও |
| `GET` | `/api/notes/search?q=keyword` | search করো |
| `GET` | `/api/notes/{id}` | একটা note পাও |
| `POST` | `/api/notes` | নতুন note upload করো |
| `POST` | `/api/notes/{id}/download` | download count বাড়াও |
| `DELETE` | `/api/notes/{id}` | note delete করো |

---

## 📤 GET /api/notes — Response Example

```json
[
  {
    "id": 1,
    "title": "Data Structures Complete Notes",
    "description": "Chapter 1-10 complete notes...",
    "subject": "CSE 2101",
    "uploaded_by": "Dr. Rahim Uddin",
    "uploaded_at": "2024-01-15",
    "pdf_url": "https://yourdomain.com/uploads/pdfs/nvd_abc123.pdf",
    "file_size": 2457600,
    "download_count": 1234,
    "thumbnail_url": "https://yourdomain.com/uploads/thumbnails/nvd_xyz.jpg"
  }
]
```

---

## 📤 POST /api/notes — Upload করার নিয়ম

`multipart/form-data` দিয়ে পাঠাতে হবে:

```
POST https://yourdomain.com/api/notes
Content-Type: multipart/form-data

Fields:
  title        = "My Notes"        (required)
  description  = "Chapter summary" (optional)
  subject      = "CSE 2101"        (optional)
  uploaded_by  = "Your Name"       (optional)

Files:
  pdf_file     = [PDF file]        (required, max 20MB)
  thumbnail    = [Image file]      (optional, max 2MB)
```

---

## 🧪 Browser এ Test করো

```
https://yourdomain.com/api/notes
https://yourdomain.com/api/notes/1
https://yourdomain.com/api/notes/search?q=data
```

---

## 🔗 Android এ কীভাবে connect হয়

```
Android App
    ↓ Retrofit GET /api/notes
PHP index.php (router)
    ↓ handleIndex()
api/notes/index.php
    ↓ MySQL query
notes table
    ↓ JSON response
Android NoteRepository.fetchNotesFromServer()
    ↓ cache to Room DB
HomeViewModel → RecyclerView
```

---

## ⚠️ Local Test (XAMPP/Laragon)

XAMPP use করলে `BASE_URL`:
```java
// Android Emulator এ:
public static final String BASE_URL = "http://10.0.2.2/notevault-backend/";

// Real device + same WiFi তে:
public static final String BASE_URL = "http://192.168.1.X/notevault-backend/";
//                                               ↑ তোমার PC এর local IP
```

## 🔑 Key Features

| Feature | Implementation |
|---------|---------------|
| Fetch from server | Retrofit → `NoteRepository.fetchNotesFromServer()` |
| Offline cache | Room (`notes` table) — auto-cached on each successful fetch |
| View PDF (online) | barteksc `PDFView.fromUri()` |
| Download PDF | `DownloadManager` + `BroadcastReceiver` |
| View PDF (offline) | `PDFView.fromFile()` from `localFilePath` |
| Downloaded tab | `NoteDao.getDownloadedNotes()` LiveData |
| Search | `NoteDao.searchNotes(query)` — title, subject, uploader |
| Pull-to-refresh | `SwipeRefreshLayout` → `loadFromServer()` |
| Shimmer loading | Facebook Shimmer + skeleton card layout |
| Share PDF | `Intent.ACTION_SEND` + `FileProvider` URI |
| Info bottom sheet | `BottomSheetDialog` with all metadata |
| Delete local file | `File.delete()` + `NoteDao.markAsDeleted()` |
| Delete note | Removes from Room + local file |

---

## 🗂️ Screens

1. **SplashActivity** — Purple gradient, logo animation, 2s delay
2. **HomeActivity** — AppBar + search + tabs (All / Downloaded) + shimmer + RecyclerView
3. **NoteDetailActivity** — Full-screen PDF viewer, bottom action bar, info sheet
4. **Dialogs** — Delete file confirmation, delete note confirmation (Material)

---

## 🔒 Permissions

```xml
INTERNET
ACCESS_NETWORK_STATE
WRITE_EXTERNAL_STORAGE   (maxSdkVersion=28)
READ_EXTERNAL_STORAGE    (maxSdkVersion=32)
POST_NOTIFICATIONS
```

---

## 🧵 Threading Model

| Thread | Used For |
|--------|----------|
| `diskIO` (single) | All Room DB reads/writes |
| `networkIO` (pool-3) | Retrofit calls |
| `mainThread` | LiveData observers, UI updates |

---

## 📝 Notes for Developers

- **Offline-first**: The app always reads from Room for display; server data is fetched asynchronously and cached.
- **DiffUtil**: `NoteAdapter` extends `ListAdapter` — submit new lists without `notifyDataSetChanged()`.
- **FileProvider**: Required on Android 7+ for sharing local PDFs via `Intent`. Configured in `AndroidManifest.xml` + `res/xml/file_paths.xml`.
- **DownloadManager receiver**: Both an inline `BroadcastReceiver` (in `NoteRepository`) and a manifest-registered `DownloadReceiver` are used — the inline one handles Room updates, the manifest one is a system fallback.
- **PDF streaming**: The barteksc viewer can load from `File`, `Uri`, `InputStream`, or `AssetManager`. Online loading uses `fromUri()`.
