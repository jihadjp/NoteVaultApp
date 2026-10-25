-- ─────────────────────────────────────────────────────────────
-- 001_academic_platform_upgrade.sql
-- Non-destructive migration for existing production NoteVault MySQL databases.
-- Preserves all existing rows and IDs in `notes`.
-- ─────────────────────────────────────────────────────────────

ALTER TABLE notes
    ADD COLUMN file_size BIGINT DEFAULT 0 AFTER file_url,
    ADD COLUMN university_name VARCHAR(255) DEFAULT 'Daffodil International University' AFTER department_name,
    ADD COLUMN resource_category VARCHAR(80) DEFAULT 'Lecture Notes' AFTER university_name,
    ADD COLUMN semester_name VARCHAR(50) NULL AFTER resource_category,
    ADD COLUMN batch_name VARCHAR(50) NULL AFTER semester_name,
    ADD COLUMN rating_avg DECIMAL(3,2) DEFAULT 0.00 AFTER view_count,
    ADD COLUMN rating_count INT DEFAULT 0 AFTER rating_avg,
    ADD COLUMN is_verified TINYINT(1) DEFAULT 0 AFTER rating_count,
    MODIFY COLUMN status ENUM('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'CHANGES_REQUESTED', 'REMOVED') DEFAULT 'PENDING';

UPDATE notes SET is_verified = 1 WHERE status = 'APPROVED';
