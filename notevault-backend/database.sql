-- ─────────────────────────────────────────────────────────────
-- NoteVault DIU — Database Schema (Updated for diunotes format)
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
    file_url        VARCHAR(500)    NOT NULL,

    course_id       INT,
    course_code     VARCHAR(50),
    course_name     VARCHAR(255),

    department_id   INT,
    department_name VARCHAR(100),

    user_id         INT,
    user_name       VARCHAR(100),

    like_count      INT             DEFAULT 0,
    dislike_count   INT             DEFAULT 0,
    view_count      INT             DEFAULT 0,

    status          ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',

    thumbnail_url   VARCHAR(500),

    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_at     TIMESTAMP       NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for search performance
CREATE INDEX idx_title         ON notes(title);
CREATE INDEX idx_course_code   ON notes(course_code);
CREATE INDEX idx_course_name   ON notes(course_name);
CREATE INDEX idx_status        ON notes(status);

-- ─── Sample Data ──────────────────────────────────────────────
INSERT INTO notes (title, description, course_code, course_name, department_name, user_name, file_url, view_count, status, approved_at)
VALUES
(
    'Numerical Method Part 4',
    'Chapter 1-10 complete notes.',
    'CSE226',
    'Numerical Methods',
    'CSE',
    'Dr. Rahim Uddin',
    'https://api.diunotes.com/uploads/notes/Numerical_Method__Part_4_CSE226_Numerical_Methods_CSE_4d6a64ac9446.pdf',
    1234,
    'APPROVED',
    '2024-01-15 10:00:00'
),
(
    'OOP Lecture Slides Full Semester',
    'Complete Object Oriented Programming slides.',
    'CSE1102',
    'OOP',
    'CSE',
    'Prof. Karim Hassan',
    'https://api.diunotes.com/uploads/notes/Error_Analysis_CSE226_Numerical_Methods_CSE_31bfab5977dd.pdf',
    567,
    'APPROVED',
    '2024-02-20 14:30:00'
);
