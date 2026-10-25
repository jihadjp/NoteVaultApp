# NoteVault Academic Platform — Database Migrations (`NOTEvault_DATABASE_MIGRATIONS.md`)

## 1. Local Android Room Database Migration (`version 2` → `version 3`)

### 1.1 Migration Policy
`AppDatabase.java` defines `MIGRATION_2_3`, a **non-destructive SQLite migration** that preserves all existing cached notes, local PDF file paths (`localFilePath`), and download flags (`isDownloaded`).

### 1.2 SQL Operations Executed in `MIGRATION_2_3`
1. **Extend `notes` table**:
   - `ALTER TABLE notes ADD COLUMN universityName TEXT`
   - `ALTER TABLE notes ADD COLUMN resourceCategory TEXT`
   - `ALTER TABLE notes ADD COLUMN semesterName TEXT`
   - `ALTER TABLE notes ADD COLUMN batchName TEXT`
   - `ALTER TABLE notes ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0`
   - `ALTER TABLE notes ADD COLUMN isVerified INTEGER NOT NULL DEFAULT 0`
   - `ALTER TABLE notes ADD COLUMN moderationStatus TEXT`
   - `ALTER TABLE notes ADD COLUMN ratingAvg REAL NOT NULL DEFAULT 0`
   - `ALTER TABLE notes ADD COLUMN ratingCount INTEGER NOT NULL DEFAULT 0`
   - `ALTER TABLE notes ADD COLUMN lastOpenedAt INTEGER NOT NULL DEFAULT 0`
   - `ALTER TABLE notes ADD COLUMN lastReadPage INTEGER NOT NULL DEFAULT 0`
   - `ALTER TABLE notes ADD COLUMN totalPages INTEGER NOT NULL DEFAULT 0`
2. **Create `my_courses` table (`MyCourseEntity`)**:
   - Stores the student's personal course list (`courseCode` PK, `courseTitle`, `departmentName`, `universityName`, `semesterName`, `creditHours`, `addedAt`).
3. **Create `study_tasks` table (`StudyTaskEntity`)**:
   - Stores student-configured exam countdown dates and study tasks (`id` PK autoincrement, `title`, `courseCode`, `taskType`, `targetDateMillis`, `notes`, `isCompleted`, `createdAt`).

---

## 2. Server MySQL Database Migration (`notevault-backend/`)

### 2.1 Fresh Installation
Run `notevault-backend/database.sql` on MySQL 5.7+ / MariaDB 10.3+:
```sql
SOURCE notevault-backend/database.sql;
```
This creates:
- `universities` (seeded with Daffodil International University `DIU`)
- `departments` (seeded with `SWE`, `CSE`, `EEE`, `BBA`)
- `courses` (seeded with verified DIU SWE and CSE courses)
- `users` (role-based accounts: `GUEST`, `STUDENT`, `MODERATOR`, `ADMIN`)
- `notes` (academic resources with hierarchy, verification, and rating columns)
- `all_questions` (past examination question papers)
- `resource_reviews` (1–5 star ratings and reviews with `UNIQUE(resource_id, reviewer_name)`)
- `resource_reports` (user reports for broken PDFs, duplicates, or copyright issues)
- `moderation_logs` (immutable audit trail of moderator status changes)

### 2.2 Upgrading an Existing Production Database
For an existing database that already has the legacy `notes` table:
1. Run `notevault-backend/database.sql` (all `CREATE TABLE IF NOT EXISTS` statements safely create the new tables without touching `notes`).
2. Run `notevault-backend/migrations/001_academic_platform_upgrade.sql` to add `file_size`, `university_name`, `resource_category`, `semester_name`, `batch_name`, `rating_avg`, `rating_count`, and `is_verified` to `notes` without dropping any rows or changing existing `id` values.

### 2.3 Rollback Guidance
If rolling back the MySQL `notes` table column additions is ever required:
```sql
ALTER TABLE notes
    DROP COLUMN file_size,
    DROP COLUMN university_name,
    DROP COLUMN resource_category,
    DROP COLUMN semester_name,
    DROP COLUMN batch_name,
    DROP COLUMN rating_avg,
    DROP COLUMN rating_count,
    DROP COLUMN is_verified;
```
Note: `notevault-backend/api/notes/store.php` includes a `try/catch` fallback so uploads continue to succeed even before `001_academic_platform_upgrade.sql` is applied.
