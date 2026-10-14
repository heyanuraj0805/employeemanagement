# Employee Attendance Management System

**Student:** Anuraj Bhawarlal Jangid | **Roll No:** BT230016CS  
**College:** J D College of Engineering & Management, Nagpur  
**Internship:** Pratinik Infotech · 19 May 2026 – 18 July 2026  
**Stack:** Java 17 · Java Swing · MySQL 8.x · JDBC

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Project Structure](#project-structure)
4. [Prerequisites](#prerequisites)
5. [Setup – Step by Step](#setup--step-by-step)
6. [Running the Application](#running-the-application)
7. [Default Login Credentials](#default-login-credentials)
8. [Application Screens](#application-screens)
9. [Database Schema](#database-schema)
10. [Architecture](#architecture)
11. [Troubleshooting](#troubleshooting)

---

## Project Overview

A fully-functional **desktop application** that replaces manual paper-based employee attendance tracking with a structured, database-backed digital system. All attendance data is persisted in MySQL and the UI is built with Java Swing.

---

## Features

| Module | What it does |
|--------|-------------|
| **Login** | Secure SHA-256 password authentication, role-based access |
| **Dashboard** | Real-time stats: today's present / absent / late, total employees, pending leaves |
| **Check-In / Check-Out** | Employee selects their name or enters employee code → records timestamped check-in/out |
| **Attendance Records** | Filter by employee + date range, colour-coded status, manual edit |
| **Employee Management** | Add, edit, delete employees with full profile; search by name/code/department |
| **Department Management** | CRUD for company departments |
| **Leave Management** | Apply leave, approve/reject workflow, pending requests badge |
| **Reports** | Monthly attendance summary per employee, CSV export, print |
| **User Management** | Admin: add/edit/delete login users, change passwords |
| **Settings** | DB connection info, test connection button |

### Attendance Business Rules
- Check-in ≤ 09:30 → **Present**
- Check-in 09:31–10:30 → **Late**
- Check-in > 10:30 → **Half-Day**
- Working hours < 4 h → **Half-Day**
- Overtime = working hours − 8 h (when positive)

---

## Project Structure

```
EmployeeAttendanceSystem/
├── src/
│   └── main/
│       ├── App.java                        ← Entry point
│       ├── config/
│       │   └── DatabaseConfig.java         ← JDBC connection
│       ├── model/                          ← POJOs
│       │   ├── User.java
│       │   ├── Employee.java
│       │   ├── Attendance.java
│       │   ├── Department.java
│       │   └── LeaveRequest.java
│       ├── dao/                            ← Database access
│       │   ├── UserDAO.java
│       │   ├── EmployeeDAO.java
│       │   ├── AttendanceDAO.java
│       │   ├── DepartmentDAO.java
│       │   └── LeaveRequestDAO.java
│       ├── service/                        ← Business logic
│       │   ├── SessionManager.java
│       │   └── AttendanceService.java
│       ├── util/                           ← Helpers
│       │   ├── UITheme.java
│       │   ├── DateUtil.java
│       │   └── PasswordUtil.java
│       └── ui/                             ← Swing GUI
│           ├── LoginFrame.java
│           ├── MainFrame.java
│           └── panels/
│               ├── DashboardPanel.java
│               ├── EmployeePanel.java
│               ├── EmployeeFormDialog.java
│               ├── AttendancePanel.java
│               ├── ManualMarkDialog.java
│               ├── CheckInOutPanel.java
│               ├── LeavePanel.java
│               ├── DepartmentPanel.java
│               ├── ReportPanel.java
│               ├── UserPanel.java
│               └── SettingsPanel.java
├── sql/
│   └── schema.sql                         ← Full DB setup + seed data
├── lib/
│   └── mysql-connector-j.jar              ← You must place this here
├── out/
│   └── classes/                           ← Compiled .class files
├── docs/
│   └── README.md
├── db.properties                          ← DB connection config
├── run.bat                                ← Build + Run (Windows)
└── compile.bat                            ← Compile only
```

---

## Prerequisites

| Requirement | Version | Download |
|-------------|---------|----------|
| JDK | 17 or later | https://adoptium.net/ |
| MySQL Server | 8.0+ | https://dev.mysql.com/downloads/mysql/ |
| MySQL Connector/J | 8.x or 9.x | https://dev.mysql.com/downloads/connector/j/ |

> ⚠️ **Important:** This project uses Java 17 text blocks and switch expressions.  
> JDK 11 will **not** work. Please use JDK 17+.

---

## Setup – Step by Step

### Step 1 – Install JDK 17+

1. Download from https://adoptium.net/
2. Install and set `JAVA_HOME` in Environment Variables
3. Add `%JAVA_HOME%\bin` to `PATH`
4. Verify: open Command Prompt → `java -version`

### Step 2 – Install & Start MySQL

1. Download and install MySQL Community Server 8.x
2. Start the MySQL service (it usually starts automatically)
3. Note your **root password** (or whichever user you'll use)

### Step 3 – Download MySQL Connector/J

1. Go to: https://dev.mysql.com/downloads/connector/j/
2. Select **Platform Independent → ZIP Archive** → Download
3. Extract the ZIP
4. Copy the file `mysql-connector-j-X.X.X.jar` into:
   ```
   EmployeeAttendanceSystem\lib\mysql-connector-j.jar
   ```
   (rename it exactly to `mysql-connector-j.jar`)

### Step 4 – Create the Database

Open MySQL Workbench **or** MySQL Command Line Client and run:

```sql
SOURCE C:\Users\HP\Desktop\pratnik\EmployeeAttendanceSystem\sql\schema.sql;
```

Or copy-paste the contents of `sql\schema.sql` into Workbench and execute.

This creates:
- Database `attendance_db`
- All tables (users, employees, attendance, departments, leave_requests, holidays)
- 8 sample employees, 5 departments, 13 holidays, 3 login users

### Step 5 – Configure Database Connection

Open `db.properties` (in the root folder) and set your credentials:

```properties
db.host=localhost
db.port=3306
db.name=attendance_db
db.username=root
db.password=YOUR_MYSQL_ROOT_PASSWORD
```

If your MySQL password is empty, leave `db.password=` blank.

### Step 6 – Build & Run

Double-click `run.bat`, or open Command Prompt in the project folder and run:

```cmd
run.bat
```

The script will:
1. Compile all Java source files
2. Copy `db.properties` to the output directory
3. Launch the application

---

## Running the Application

```
Double-click run.bat
```

Or from Command Prompt:
```cmd
cd C:\Users\HP\Desktop\pratnik\EmployeeAttendanceSystem
run.bat
```

---

## Default Login Credentials

| Username | Password   | Role    |
|----------|-----------|---------|
| `admin`  | `admin123` | Admin   |
| `manager`| `manager123`| Manager |
| `user1`  | `user123`  | User    |

> Passwords are stored as SHA-256 hashes in the database — never in plain text.

---

## Application Screens

### Login Screen
- Gradient blue background, card-style form
- Error message for wrong credentials
- Connects to DB on the background thread (non-blocking UI)

### Dashboard
- 5 real-time stat cards: Present / Absent / Late today, Total Employees, Pending Leaves
- Quick-action buttons to jump to other modules
- Welcome card + System Information card

### Check-In / Check-Out
- Select employee from dropdown or type employee code
- Displays today's current status, check-in time, check-out time, working hours
- Large CHECK IN / CHECK OUT buttons (state-aware — grayed when not applicable)
- Remarks field for notes

### Employee Management
- Full searchable table of all employees
- Add / Edit employee via form dialog (auto-generates next employee code)
- View detailed profile
- Delete (admin only)

### Attendance Records
- Filter by employee + date range
- Colour-coded status (green=Present, red=Absent, yellow=Late, orange=Half-Day)
- Manual mark dialog for admins/managers
- Delete record (admin only)

### Reports
- Select employee + month + year → generate monthly report
- Shows all calendar days: marked days with check-in/out, unmarked days as blank, weekends
- Summary bar: Present / Absent / Late / Half-Day / On-Leave / Total Hours
- **Export to CSV** and **Print / Print Preview**

### Leave Management
- View all leave requests with filter (All / Pending / Approved / Rejected)
- Approve or Reject selected pending requests (Manager+)
- Apply new leave request

### Department Management (Admin)
- Add, edit, delete departments
- Cannot delete a department that has employees assigned

### User Management (Admin)
- Add, edit, delete application login users
- Change password for any user
- Role management (Admin / Manager / User)

### Settings
- Displays current DB connection config
- Test Connection button
- About / project info

---

## Database Schema

```
users            – application login accounts
departments      – company departments
employees        – employee profiles
attendance       – daily check-in/out records (unique per employee×date)
leave_types      – types of leave (Casual, Sick, Earned, etc.)
leave_requests   – employee leave applications
holidays         – public holidays
```

Key design points:
- `attendance.working_hours` and `overtime_hours` are **GENERATED STORED** columns (computed by MySQL automatically)
- Unique constraint on `(employee_id, attendance_date)` prevents duplicate records
- Foreign keys with CASCADE DELETE for attendance → employee relationship

---

## Architecture

The application follows a layered architecture:

```
UI Layer (Swing)
    │
    ▼
Service Layer (Business Logic)
    │
    ▼
DAO Layer (Data Access Objects)
    │
    ▼
MySQL Database (via JDBC)
```

- **Model** – Plain Java objects (POJOs) representing data entities
- **DAO** – One class per entity; all SQL is here, uses `PreparedStatement`
- **Service** – Business rules (check-in timing logic, status computation)
- **UI** – Swing panels + dialogs; all DB calls via `SwingWorker` (non-blocking)

---

## Troubleshooting

### "MySQL JDBC driver not found"
- Ensure `lib\mysql-connector-j.jar` exists
- Re-run `run.bat`

### "Cannot connect to database"
- Check MySQL service is running: `services.msc` → MySQL80
- Verify credentials in `db.properties`
- Click **Settings → Test Connection** inside the app

### "Unknown column 'working_hours'" or schema errors
- You may have run the SQL on an old database
- Drop and recreate: `DROP DATABASE attendance_db;` then re-run `schema.sql`

### Compilation errors
- Ensure you are using **JDK 17 or later**: `javac -version`
- Text blocks and switch expressions require Java 14+/17+

### Application window appears blank
- This can happen if the screen resolution is very low
- Try resizing the window or maximizing it

---

*Generated as part of Java Internship at Pratinik Infotech, 2026.*
