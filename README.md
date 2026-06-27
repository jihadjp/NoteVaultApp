# NoteVault DIU — PHP Backend

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
