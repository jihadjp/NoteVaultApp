# NoteVault Academic Platform — API Documentation (`NOTEvault_API_DOCUMENTATION.md`)

## 1. Overview & Compatibility Policy

The NoteVault PHP/MySQL backend (`notevault-backend/`) uses a front-controller (`index.php`) that supports:
- **Legacy Android routes** (`/api/notes`, `/api/questions`) for 100% backward compatibility.
- **Versioned Academic Platform v1 routes** (`/api/v1/*`) for multi-university hierarchy, reviews/ratings, content reporting, and moderator/admin queues.

All endpoints return `application/json; charset=UTF-8` with security headers (`X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`).

---

## 2. Public & Student Resource Endpoints

### 2.1 List Approved Academic Resources
- **Routes**: `GET /api/notes` or `GET /api/v1/resources`
- **Query Parameters**:
  - `courseCode` / `course_code` *(optional, string)* — Filter by exact course code (e.g. `SWE211`, `CSE226`).
  - `department` / `department_name` *(optional, string)* — Filter by department code (`SWE`, `CSE`, `EEE`, `BBA`).
  - `type` / `category` *(optional, string)* — Filter by resource category (`Lecture Notes`, `Lecture Slides`, `Official Syllabus`, `Question Papers`, `Suggestions`, `Lab Manuals`, `Assignments`).
  - `page` *(optional, int, default `1`)* — 1-based page number.
  - `limit` *(optional, int, default `50`, max `100`)* — Page size.
- **Response (`200 OK`)**: JSON array of `NoteResponse` objects (only `status = 'APPROVED'` records are returned).

### 2.2 Get Single Resource Detail
- **Routes**: `GET /api/notes/{id}` or `GET /api/v1/resources/{id}`
- **Response (`200 OK`)**: Single `NoteResponse` JSON object.
- **Error (`404 Not Found`)**: `{"success": false, "message": "Note not found."}`

### 2.3 Search Academic Resources
- **Routes**: `GET /api/notes/search?q={query}` or `GET /api/v1/search?q={query}`
- **Behavior**: Prepared statement `LIKE` search across `title`, `course_code`, `course_name`, `department_name`, `description`, and `user_name` for `APPROVED` resources.

### 2.4 Upload / Contribute Academic Resource
- **Routes**: `POST /api/notes` or `POST /api/v1/resources`
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `title` *(required, 1–255 chars)*
  - `description` *(optional)*
  - `course_name` *(optional)*
  - `course_code` *(optional)*
  - `department_name` *(optional)*
  - `university_name` *(optional, defaults to `Daffodil International University`)*
  - `resource_category` *(optional, defaults to `Lecture Notes`)*
  - `semester_name` *(optional)*
  - `user_name` *(optional; overridden by server session user when Bearer token is present)*
  - `pdf_file` *(required file, `application/pdf`, max 20 MB, verified for `%PDF-` magic header bytes)*
  - `thumbnail` *(optional image file, max 2 MB)*
- **Response (`201 Created`)**:
  ```json
  {
    "success": true,
    "message": "Resource uploaded and placed in Pending Review queue.",
    "id": 15,
    "status": "PENDING",
    "file_url": "https://notevault.jihadjp.com/uploads/notes/nv_9f8e7d6c5b4a32109f8e7d6c5b4a3210.pdf"
  }
  ```

### 2.5 Record Resource Download / View
- **Routes**: `POST /api/notes/{id}/download` or `POST /api/v1/resources/{id}/download`
- **Response (`200 OK`)**: `{"success": true, "message": "View count updated.", "viewCount": 1235}`

### 2.6 Withdraw / Remove Resource
- **Routes**: `DELETE /api/notes/{id}` or `DELETE /api/v1/resources/{id}`
- **Authorization**: Requires `Authorization: Bearer <token>`. Permitted only for the resource owner (`user_id` match) or users with `MODERATOR` / `ADMIN` role.
- **Response (`200 OK`)**: `{"success": true, "message": "Resource withdrawn/removed.", "id": 15}`

---

## 3. Academic Hierarchy & Past Questions Endpoints

### 3.1 List Universities
- **Route**: `GET /api/v1/universities`
- **Response (`200 OK`)**: Array of active universities (defaults to Daffodil International University `DIU`).

### 3.2 List Departments
- **Route**: `GET /api/v1/departments?university_id=1`
- **Response (`200 OK`)**: Array of verified departments (`SWE`, `CSE`, `EEE`, `BBA`, etc.).

### 3.3 List Courses
- **Route**: `GET /api/v1/courses?department=SWE&semester=L2-T1`
- **Response (`200 OK`)**: Array of verified courses with `course_code`, `course_title`, `credit_hours`, and `semester_name`.

### 3.4 List Past Examination Questions
- **Route**: `GET /api/questions?department={dept}&course={course}&semester={sem}&type={type}`
- **Response (`200 OK`)**: Array of `QuestionResponse` objects.

---

## 4. Reviews, Ratings, Reporting & Moderation Endpoints

### 4.1 List or Submit Resource Reviews
- **Routes**:
  - `GET /api/v1/resources/{id}/reviews?page=1&limit=20`
  - `POST /api/v1/resources/{id}/reviews` (`rating` 1–5, `review_text`, `reviewer_name`)
- **Behavior**: Enforces one active review per user/reviewer per resource (`UNIQUE KEY uq_resource_reviewer`) and recalculates `rating_avg` and `rating_count` on the server.

### 4.2 Submit Resource Report
- **Route**: `POST /api/v1/resources/{id}/reports`
- **Form Fields**: `reason` *(required)*, `details` *(optional)*, `reporter_name` *(optional)*.
- **Response (`201 Created`)**: Places report in `OPEN` status for moderator inspection.

### 4.3 Moderator Queue & Decision
- **Routes**:
  - `GET /api/v1/moderation/queue?status=PENDING` *(Requires `MODERATOR` or `ADMIN` Bearer token)*
  - `POST /api/v1/moderation/{id}/decision` *(Requires `MODERATOR` or `ADMIN` Bearer token; `decision` in `APPROVED`, `REJECTED`, `CHANGES_REQUESTED`, `REMOVED`)*
- **Behavior**: Updates resource status, sets `is_verified`, and writes an immutable entry to `moderation_logs`.
