-- ─────────────────────────────────────────────────────────────
-- NoteVault DIU — Database Schema
-- MySQL 5.7+ / MariaDB 10.3+ compatible
-- ─────────────────────────────────────────────────────────────

CREATE DATABASE IF NOT EXISTS notevault_diu
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE notevault_diu;

-- Notes table
CREATE TABLE IF NOT EXISTS notes (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255)    NOT NULL,
    description     TEXT,
    subject         VARCHAR(100),
    uploaded_by     VARCHAR(100),
    uploaded_at     DATE,
    pdf_url         VARCHAR(500)    NOT NULL,
    file_size       BIGINT          DEFAULT 0,
    download_count  INT             DEFAULT 0,
    thumbnail_url   VARCHAR(500),
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active       TINYINT(1)      DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for search performance
CREATE INDEX idx_title   ON notes(title);
CREATE INDEX idx_subject ON notes(subject);
CREATE INDEX idx_active  ON notes(is_active);

-- ─── Sample Data ──────────────────────────────────────────────
INSERT INTO notes (title, description, subject, uploaded_by, uploaded_at, pdf_url, file_size, download_count, thumbnail_url)
VALUES
(
    'Numerical Method Part 4',
    'Chapter 1-10 complete notes including arrays, linked lists, trees, graphs and sorting algorithms.',
    'CSE 2101',
    'Dr. Rahim Uddin',
    '2024-01-15',
    'https://api.diunotes.com/uploads/notes/Numerical_Method__Part_4_CSE226_Numerical_Methods_CSE_4d6a64ac9446.pdf',
    2457600,
    1234,
    'https://notevault.jihadjp.com/uploads/thumbnails/ds_notes.jpg'
),
(
    'OOP Lecture Slides Full Semester',
    'Complete Object Oriented Programming slides covering all topics from semester.',
    'CSE 1102',
    'Prof. Karim Hassan',
    '2024-02-20',
    'https://api.diunotes.com/uploads/notes/Error_Analysis_CSE226_Numerical_Methods_CSE_31bfab5977dd.pdf',
    1048576,
    567,
    'https://notevault.jihadjp.com/uploads/thumbnails/oop_slides.jpg'
),
(
    'Database Management System Notes',
    'Full DBMS notes - ER diagram, normalization, SQL queries, transactions.',
    'CSE 3201',
    'Fatema Begum',
    '2024-03-10',
    'https://api.diunotes.com/uploads/notes/Numerical_Method__Part_3_CSE226_Numerical_Methods_CSE_95aea51115df.pdf',
    3145728,
    890,
    'https://notevault.jihadjp.com/uploads/thumbnails/dbms_notes.jpg'
),
(
    'Computer Networks Short Notes',
    'OSI model, TCP/IP, routing protocols - concise exam preparation notes.',
    'CSE 3301',
    'Arif Hossain',
    '2024-03-25',
    'https://api.diunotes.com/uploads/notes/Numerical_Method__Part_2_CSE226_Numerical_Methods_CSE_0d5282d0d8fc.pdf',
    1572864,
    432,
    'https://notevault.jihadjp.com/uploads/thumbnails/cn_notes.jpg'
);