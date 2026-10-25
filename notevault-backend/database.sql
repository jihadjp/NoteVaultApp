-- ─────────────────────────────────────────────────────────────
-- NoteVault Academic Platform — Complete Database Schema
-- MySQL 5.7+ / MariaDB 10.3+ compatible (Non-destructive IF NOT EXISTS)
-- ─────────────────────────────────────────────────────────────

CREATE DATABASE IF NOT EXISTS notevault_diu
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE notevault_diu;

-- 1. Universities
CREATE TABLE IF NOT EXISTS universities (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    short_name  VARCHAR(50)  NOT NULL,
    country     VARCHAR(100) DEFAULT 'Bangladesh',
    is_active   TINYINT(1)   DEFAULT 1,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Departments
CREATE TABLE IF NOT EXISTS departments (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    university_id   INT          NOT NULL DEFAULT 1,
    code            VARCHAR(30)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    faculty_name    VARCHAR(255),
    is_active       TINYINT(1)   DEFAULT 1,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_uni_dept (university_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Courses
CREATE TABLE IF NOT EXISTS courses (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    department_id   INT          NOT NULL DEFAULT 1,
    department_code VARCHAR(30)  NOT NULL,
    course_code     VARCHAR(50)  NOT NULL,
    course_title    VARCHAR(255) NOT NULL,
    credit_hours    DECIMAL(3,1) DEFAULT 3.0,
    semester_name   VARCHAR(50),
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_dept_course (department_code, course_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Users & Roles
CREATE TABLE IF NOT EXISTS users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(190) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    api_token_hash  CHAR(64)      NULL UNIQUE,
    role            ENUM('GUEST', 'STUDENT', 'MODERATOR', 'ADMIN') DEFAULT 'STUDENT',
    university_id   INT          DEFAULT 1,
    department_id   INT          DEFAULT 1,
    is_active       TINYINT(1)   DEFAULT 1,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Notes / Academic Resources
CREATE TABLE IF NOT EXISTS notes (
    id                INT             AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255)    NOT NULL,
    description       TEXT,
    file_url          VARCHAR(500)    NOT NULL,
    file_size         BIGINT          DEFAULT 0,

    course_id         INT,
    course_code       VARCHAR(50),
    course_name       VARCHAR(255),

    department_id     INT,
    department_name   VARCHAR(100),
    university_name   VARCHAR(255)    DEFAULT 'Daffodil International University',
    resource_category VARCHAR(80)     DEFAULT 'Lecture Notes',
    semester_name     VARCHAR(50),
    batch_name        VARCHAR(50),

    user_id           INT,
    user_name         VARCHAR(100),

    like_count        INT             DEFAULT 0,
    dislike_count     INT             DEFAULT 0,
    view_count        INT             DEFAULT 0,
    rating_avg        DECIMAL(3,2)    DEFAULT 0.00,
    rating_count      INT             DEFAULT 0,

    is_verified       TINYINT(1)      DEFAULT 0,
    status            ENUM('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'CHANGES_REQUESTED', 'REMOVED') DEFAULT 'PENDING',

    thumbnail_url     VARCHAR(500),

    created_at        TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_at       TIMESTAMP       NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_title         ON notes(title);
CREATE INDEX idx_course_code   ON notes(course_code);
CREATE INDEX idx_course_name   ON notes(course_name);
CREATE INDEX idx_status        ON notes(status);

-- 6. Past Examination Questions (all_questions)
CREATE TABLE IF NOT EXISTS all_questions (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    course_name       VARCHAR(255) NOT NULL,
    department_name   VARCHAR(150) NOT NULL,
    department_short  VARCHAR(50)  NOT NULL,
    exam_type         VARCHAR(50)  NOT NULL,
    semester_name     VARCHAR(50)  NOT NULL,
    pdf_link          VARCHAR(500) NOT NULL,
    submissions_count INT          DEFAULT 1,
    created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Resource Reviews & Ratings
CREATE TABLE IF NOT EXISTS resource_reviews (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    resource_id   INT          NOT NULL,
    user_id       INT          DEFAULT 0,
    reviewer_name VARCHAR(120) NOT NULL,
    rating        TINYINT      NOT NULL,
    review_text   TEXT,
    status        ENUM('ACTIVE', 'HIDDEN') DEFAULT 'ACTIVE',
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_resource_reviewer (resource_id, reviewer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Resource Reports
CREATE TABLE IF NOT EXISTS resource_reports (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    resource_id   INT          NOT NULL,
    user_id       INT          DEFAULT 0,
    reporter_name VARCHAR(120) NOT NULL,
    reason        VARCHAR(255) NOT NULL,
    details       TEXT,
    status        ENUM('OPEN', 'RESOLVED', 'DISMISSED') DEFAULT 'OPEN',
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Moderation Audit Trail
CREATE TABLE IF NOT EXISTS moderation_logs (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    resource_id     INT         NOT NULL,
    moderator_id    INT         NOT NULL,
    previous_status VARCHAR(40),
    new_status      VARCHAR(40) NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─── Verified Initial Launch Data (DIU) ───────────────────────────────────────
INSERT IGNORE INTO universities (id, code, name, short_name, country) VALUES
(1, 'DIU', 'Daffodil International University', 'DIU', 'Bangladesh');

INSERT IGNORE INTO departments (id, university_id, code, name, faculty_name) VALUES
(1, 1, 'SWE', 'Software Engineering', 'Faculty of Science and Information Technology'),
(2, 1, 'CSE', 'Computer Science and Engineering', 'Faculty of Science and Information Technology'),
(3, 1, 'EEE', 'Electrical and Electronic Engineering', 'Faculty of Engineering'),
(4, 1, 'BBA', 'Business Administration', 'Faculty of Business and Entrepreneurship');

INSERT IGNORE INTO courses (department_id, department_code, course_code, course_title, credit_hours, semester_name) VALUES
(1, 'SWE', 'SWE112', 'Programming and Problem Solving', 3.0, 'L1-T1'),
(1, 'SWE', 'SWE122', 'Data Structures and Algorithms', 3.0, 'L1-T2'),
(1, 'SWE', 'SWE132', 'Object Oriented Programming', 3.0, 'L1-T3'),
(1, 'SWE', 'SWE211', 'Database Management Systems', 3.0, 'L2-T1'),
(1, 'SWE', 'SWE223', 'Software Engineering and System Analysis', 3.0, 'L2-T2'),
(2, 'CSE', 'CSE1102', 'Object Oriented Programming', 3.0, 'L1-T2'),
(2, 'CSE', 'CSE2101', 'Data Structures', 3.0, 'L2-T1'),
(2, 'CSE', 'CSE226', 'Numerical Methods', 3.0, 'L2-T2');

INSERT INTO notes (title, description, course_code, course_name, department_name, university_name, resource_category, semester_name, user_name, file_url, view_count, is_verified, status, approved_at)
VALUES
(
    'Numerical Method Part 4',
    'Chapter 1-10 complete notes.',
    'CSE226',
    'Numerical Methods',
    'CSE',
    'Daffodil International University',
    'Lecture Notes',
    'L2-T2',
    'Dr. Rahim Uddin',
    'https://api.diunotes.com/uploads/notes/Numerical_Method__Part_4_CSE226_Numerical_Methods_CSE_4d6a64ac9446.pdf',
    1234,
    1,
    'APPROVED',
    '2024-01-15 10:00:00'
),
(
    'OOP Lecture Slides Full Semester',
    'Complete Object Oriented Programming slides.',
    'CSE1102',
    'OOP',
    'CSE',
    'Daffodil International University',
    'Lecture Slides',
    'L1-T2',
    'Prof. Karim Hassan',
    'https://api.diunotes.com/uploads/notes/Error_Analysis_CSE226_Numerical_Methods_CSE_31bfab5977dd.pdf',
    567,
    1,
    'APPROVED',
    '2024-02-20 14:30:00'
);
