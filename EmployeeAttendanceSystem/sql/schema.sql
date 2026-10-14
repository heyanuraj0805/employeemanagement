-- ============================================================
--  Employee Attendance Management System
--  Database Schema
--  Student  : Anuraj Bhawarlal Jangid | Roll: BT230016CS
--  College  : J D College of Engineering & Management, Nagpur
--  Internship: Pratinik Infotech  (19 May 2026 – 18 July 2026)
-- ============================================================

CREATE DATABASE IF NOT EXISTS attendance_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE attendance_db;

-- ──────────────────────────────────────────
--  TABLE: users  (application login)
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,          -- stored as SHA-256 hex
    role        ENUM('ADMIN','MANAGER','USER') NOT NULL DEFAULT 'USER',
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    is_active   TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ──────────────────────────────────────────
--  TABLE: departments
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS departments (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ──────────────────────────────────────────
--  TABLE: employees
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employees (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    emp_code        VARCHAR(20)  NOT NULL UNIQUE,
    first_name      VARCHAR(60)  NOT NULL,
    last_name       VARCHAR(60)  NOT NULL,
    gender          ENUM('Male','Female','Other') NOT NULL,
    dob             DATE,
    email           VARCHAR(100) UNIQUE,
    phone           VARCHAR(15),
    address         TEXT,
    department_id   INT,
    designation     VARCHAR(100),
    date_of_joining DATE         NOT NULL,
    employment_type ENUM('Full-Time','Part-Time','Contract','Intern') NOT NULL DEFAULT 'Full-Time',
    status          ENUM('Active','Inactive','Resigned','Terminated') NOT NULL DEFAULT 'Active',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_emp_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ──────────────────────────────────────────
--  TABLE: attendance
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS attendance (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    employee_id     INT          NOT NULL,
    attendance_date DATE         NOT NULL,
    check_in        DATETIME,
    check_out       DATETIME,
    status          ENUM('Present','Absent','Late','Half-Day','On-Leave','Holiday','Weekend') NOT NULL DEFAULT 'Present',
    working_hours   DECIMAL(5,2) GENERATED ALWAYS AS (
                        CASE
                            WHEN check_in IS NOT NULL AND check_out IS NOT NULL
                            THEN ROUND(TIMESTAMPDIFF(MINUTE, check_in, check_out) / 60.0, 2)
                            ELSE 0
                        END
                    ) STORED,
    overtime_hours  DECIMAL(5,2) GENERATED ALWAYS AS (
                        CASE
                            WHEN check_in IS NOT NULL AND check_out IS NOT NULL
                                 AND ROUND(TIMESTAMPDIFF(MINUTE, check_in, check_out) / 60.0, 2) > 8
                            THEN ROUND(ROUND(TIMESTAMPDIFF(MINUTE, check_in, check_out) / 60.0, 2) - 8, 2)
                            ELSE 0
                        END
                    ) STORED,
    remarks         VARCHAR(255),
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_att_emp   FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT uq_emp_date  UNIQUE (employee_id, attendance_date)
) ENGINE=InnoDB;

-- ──────────────────────────────────────────
--  TABLE: leave_types
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS leave_types (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80) NOT NULL UNIQUE,
    max_days    INT         NOT NULL DEFAULT 0,
    description VARCHAR(255)
) ENGINE=InnoDB;

-- ──────────────────────────────────────────
--  TABLE: leave_requests
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS leave_requests (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    employee_id     INT         NOT NULL,
    leave_type_id   INT         NOT NULL,
    from_date       DATE        NOT NULL,
    to_date         DATE        NOT NULL,
    reason          TEXT,
    status          ENUM('Pending','Approved','Rejected','Cancelled') NOT NULL DEFAULT 'Pending',
    applied_on      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by     INT,
    reviewed_on     DATETIME,
    review_remarks  VARCHAR(255),
    CONSTRAINT fk_lr_emp    FOREIGN KEY (employee_id)   REFERENCES employees(id)    ON DELETE CASCADE,
    CONSTRAINT fk_lr_type   FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)  ON DELETE RESTRICT,
    CONSTRAINT fk_lr_rev    FOREIGN KEY (reviewed_by)   REFERENCES users(id)        ON DELETE SET NULL
) ENGINE=InnoDB;

-- ──────────────────────────────────────────
--  TABLE: holidays
-- ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS holidays (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    holiday_date    DATE         NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(255)
) ENGINE=InnoDB;

-- ──────────────────────────────────────────
--  INDEXES for performance
-- ──────────────────────────────────────────
CREATE INDEX idx_att_emp_date  ON attendance      (employee_id, attendance_date);
CREATE INDEX idx_att_date      ON attendance      (attendance_date);
CREATE INDEX idx_att_status    ON attendance      (status);
CREATE INDEX idx_emp_dept      ON employees       (department_id);
CREATE INDEX idx_emp_status    ON employees       (status);
CREATE INDEX idx_lr_emp        ON leave_requests  (employee_id);
CREATE INDEX idx_lr_status     ON leave_requests  (status);

-- ──────────────────────────────────────────
--  SEED DATA
-- ──────────────────────────────────────────

-- Default admin user  (password = "admin123" SHA-256)
INSERT INTO users (username, password, role, full_name, email) VALUES
('admin',   '240be518fabd2724ddb6f04eeb1da5967448d7e831186422d11efde5e5fd3a8c', 'ADMIN',   'System Administrator', 'admin@pratinik.com'),
('manager', '88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589', 'MANAGER', 'HR Manager',           'manager@pratinik.com'),
('user1',   '0a041b9462caa4a31bac3567e0b6e6fd9100787db2ab433d96f6d178cabfce90', 'USER',    'Demo User',            'user1@pratinik.com');
-- password for manager = "manager123", for user1 = "user123"

-- Departments
INSERT INTO departments (name, description) VALUES
('Human Resources',    'HR and administration'),
('Information Technology', 'Software & IT infrastructure'),
('Finance',            'Accounts and finance'),
('Operations',         'Day-to-day operations'),
('Marketing',          'Marketing and sales');

-- Leave types
INSERT INTO leave_types (name, max_days, description) VALUES
('Casual Leave',   12, 'For personal/casual purposes'),
('Sick Leave',     12, 'Medical or health-related absence'),
('Earned Leave',   15, 'Annual earned leave'),
('Maternity Leave',180,'Maternity benefit leave'),
('Paternity Leave', 15,'Paternity benefit leave'),
('Compensatory Off', 0,'Comp-off for extra working days');

-- Sample employees
INSERT INTO employees (emp_code, first_name, last_name, gender, dob, email, phone, department_id, designation, date_of_joining, employment_type, status) VALUES
('EMP001', 'Anuraj',   'Jangid',   'Male',   '2003-04-10', 'anuraj@pratinik.com',   '9876543210', 2, 'Software Intern',       '2026-05-19', 'Intern',    'Active'),
('EMP002', 'Priya',    'Sharma',   'Female', '1995-07-22', 'priya@pratinik.com',    '9123456780', 1, 'HR Executive',          '2022-01-15', 'Full-Time', 'Active'),
('EMP003', 'Rahul',    'Verma',    'Male',   '1990-03-11', 'rahul@pratinik.com',    '9012345678', 2, 'Senior Developer',      '2020-06-01', 'Full-Time', 'Active'),
('EMP004', 'Sneha',    'Patil',    'Female', '1998-09-05', 'sneha@pratinik.com',    '8901234567', 3, 'Finance Analyst',       '2023-03-20', 'Full-Time', 'Active'),
('EMP005', 'Karan',    'Mehta',    'Male',   '1993-12-28', 'karan@pratinik.com',    '8790123456', 4, 'Operations Executive',  '2021-09-10', 'Full-Time', 'Active'),
('EMP006', 'Divya',    'Nair',     'Female', '1997-06-14', 'divya@pratinik.com',    '8679012345', 5, 'Marketing Executive',   '2024-02-01', 'Full-Time', 'Active'),
('EMP007', 'Amit',     'Gupta',    'Male',   '1988-01-20', 'amit@pratinik.com',     '8568901234', 2, 'Tech Lead',             '2018-11-05', 'Full-Time', 'Active'),
('EMP008', 'Neha',     'Kulkarni', 'Female', '2000-08-30', 'neha@pratinik.com',     '8457890123', 2, 'Junior Developer',      '2024-07-01', 'Full-Time', 'Active');

-- Sample holidays (2026)
INSERT INTO holidays (holiday_date, name) VALUES
('2026-01-01', 'New Year''s Day'),
('2026-01-26', 'Republic Day'),
('2026-03-29', 'Holi'),
('2026-04-02', 'Good Friday'),
('2026-04-14', 'Ambedkar Jayanti'),
('2026-05-01', 'Maharashtra Day / Labour Day'),
('2026-08-15', 'Independence Day'),
('2026-08-27', 'Janmashtami'),
('2026-10-02', 'Gandhi Jayanti'),
('2026-10-20', 'Diwali'),
('2026-10-21', 'Diwali (Lakshmi Puja)'),
('2026-11-04', 'Diwali (Bhai Dooj)'),
('2026-12-25', 'Christmas');
