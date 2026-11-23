-- ============================================================
-- Smart Attendance Management System - Database Schema
-- ============================================================

-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    full_name    VARCHAR(150) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role         ENUM('STUDENT','LECTURER','ADMIN') NOT NULL DEFAULT 'STUDENT',
    status       ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_users_email (email),
    INDEX idx_users_role  (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Students
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS students (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL UNIQUE,
    student_number VARCHAR(50)  NOT NULL UNIQUE,
    program        VARCHAR(150),
    class_group    VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_students_student_number (student_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Lecturers
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lecturers (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL UNIQUE,
    staff_number VARCHAR(50)  NOT NULL UNIQUE,
    department   VARCHAR(150),
    PRIMARY KEY (id),
    CONSTRAINT fk_lecturers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_lecturers_staff_number (staff_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Courses
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courses (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    course_code     VARCHAR(20)  NOT NULL UNIQUE,
    course_name     VARCHAR(200) NOT NULL,
    description     TEXT,
    lecturer_id     BIGINT,
    academic_period VARCHAR(50),
    status          ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_courses_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers(id) ON DELETE SET NULL,
    INDEX idx_courses_code        (course_code),
    INDEX idx_courses_lecturer_id (lecturer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Enrollments
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS enrollments (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    student_id      BIGINT   NOT NULL,
    course_id       BIGINT   NOT NULL,
    academic_period VARCHAR(50),
    enrolled_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course  FOREIGN KEY (course_id)  REFERENCES courses(id)  ON DELETE CASCADE,
    UNIQUE INDEX uq_enrollment (student_id, course_id, academic_period),
    INDEX idx_enrollments_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Attendance Sessions
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS attendance_sessions (
    id                     BIGINT   NOT NULL AUTO_INCREMENT,
    course_id              BIGINT   NOT NULL,
    lecturer_id            BIGINT   NOT NULL,
    session_date           DATE     NOT NULL,
    start_time             DATETIME NOT NULL,
    end_time               DATETIME,
    late_threshold_minutes INT      NOT NULL DEFAULT 15,
    status                 ENUM('OPEN','CLOSED','CANCELLED') NOT NULL DEFAULT 'OPEN',
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sessions_course   FOREIGN KEY (course_id)   REFERENCES courses(id)   ON DELETE CASCADE,
    CONSTRAINT fk_sessions_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers(id) ON DELETE CASCADE,
    INDEX idx_sessions_course_id  (course_id),
    INDEX idx_sessions_session_date (session_date),
    INDEX idx_sessions_status     (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- QR Tokens
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS qr_tokens (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    session_id BIGINT       NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME     NOT NULL,
    status     ENUM('ACTIVE','USED','EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_qr_tokens_session FOREIGN KEY (session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    INDEX idx_qr_tokens_session_id (session_id),
    INDEX idx_qr_tokens_status     (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Face References
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS face_references (
    id             BIGINT     NOT NULL AUTO_INCREMENT,
    student_id     BIGINT     NOT NULL UNIQUE,
    reference_data LONGBLOB   NOT NULL,
    created_at     DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_face_references_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Attendance Records
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS attendance_records (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    student_id          BIGINT       NOT NULL,
    session_id          BIGINT       NOT NULL,
    check_in_time       DATETIME,
    status              ENUM('PRESENT','LATE','ABSENT') NOT NULL DEFAULT 'ABSENT',
    verification_status ENUM('VERIFIED','FAILED','PENDING') NOT NULL DEFAULT 'PENDING',
    check_in_method     VARCHAR(50),
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_att_records_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_att_records_session FOREIGN KEY (session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    UNIQUE INDEX uq_attendance (student_id, session_id),
    INDEX idx_att_records_session_id (session_id),
    INDEX idx_att_records_status     (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Attendance Reviews
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS attendance_reviews (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    attendance_id BIGINT NOT NULL,
    reviewer_id   BIGINT NOT NULL,
    reason        TEXT,
    resolution    TEXT,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_reviews_attendance FOREIGN KEY (attendance_id) REFERENCES attendance_records(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_reviewer   FOREIGN KEY (reviewer_id)   REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reviews_attendance_id (attendance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Audit Logs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT,
    action      VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   BIGINT,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_logs_user_id     (user_id),
    INDEX idx_audit_logs_entity_type (entity_type),
    INDEX idx_audit_logs_created_at  (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
