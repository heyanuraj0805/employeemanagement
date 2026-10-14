@echo off
REM ================================================================
REM  Employee Attendance Management System
REM  Build & Run Script for Windows
REM  Student: Anuraj Bhawarlal Jangid | BT230016CS
REM ================================================================
setlocal

cd /d "%~dp0"

REM ── Configuration ──────────────────────────────────────────────
set JAVA_CMD=java
set JAVAC_CMD=javac
set SRC_DIR=src
set OUT_DIR=out\classes
set LIB_DIR=lib
set JAR_FILE=lib\mysql-connector-j.jar
set MAIN_CLASS=main.App
set PROPS_FILE=db.properties

REM ── Check Java ─────────────────────────────────────────────────
%JAVAC_CMD% -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java compiler not found. Please install JDK 17+ and add it to PATH.
    pause
    exit /b 1
)

REM ── Check MySQL Connector JAR ───────────────────────────────────
if not exist "%JAR_FILE%" (
    echo.
    echo [ERROR] MySQL Connector/J not found at: %JAR_FILE%
    echo.
    echo  Please download mysql-connector-j-*.jar from:
    echo    https://dev.mysql.com/downloads/connector/j/
    echo  and place it in the lib\ folder as:
    echo    lib\mysql-connector-j.jar
    echo.
    pause
    exit /b 1
)

REM ── Create output directory ─────────────────────────────────────
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

REM ── Compile all Java sources ────────────────────────────────────
echo.
echo [BUILD] Compiling sources...
%JAVAC_CMD% -encoding UTF-8 -source 17 -target 17 ^
    -cp "%JAR_FILE%;%SRC_DIR%" ^
    -d "%OUT_DIR%" ^
    "%SRC_DIR%\main\App.java" ^
    "%SRC_DIR%\main\config\DatabaseConfig.java" ^
    "%SRC_DIR%\main\model\User.java" ^
    "%SRC_DIR%\main\model\Employee.java" ^
    "%SRC_DIR%\main\model\Attendance.java" ^
    "%SRC_DIR%\main\model\Department.java" ^
    "%SRC_DIR%\main\model\LeaveRequest.java" ^
    "%SRC_DIR%\main\dao\UserDAO.java" ^
    "%SRC_DIR%\main\dao\EmployeeDAO.java" ^
    "%SRC_DIR%\main\dao\AttendanceDAO.java" ^
    "%SRC_DIR%\main\dao\DepartmentDAO.java" ^
    "%SRC_DIR%\main\dao\LeaveRequestDAO.java" ^
    "%SRC_DIR%\main\service\SessionManager.java" ^
    "%SRC_DIR%\main\service\AttendanceService.java" ^
    "%SRC_DIR%\main\util\PasswordUtil.java" ^
    "%SRC_DIR%\main\util\UITheme.java" ^
    "%SRC_DIR%\main\util\DateUtil.java" ^
    "%SRC_DIR%\main\ui\LoginFrame.java" ^
    "%SRC_DIR%\main\ui\MainFrame.java" ^
    "%SRC_DIR%\main\ui\panels\DashboardPanel.java" ^
    "%SRC_DIR%\main\ui\panels\EmployeePanel.java" ^
    "%SRC_DIR%\main\ui\panels\EmployeeFormDialog.java" ^
    "%SRC_DIR%\main\ui\panels\AttendancePanel.java" ^
    "%SRC_DIR%\main\ui\panels\ManualMarkDialog.java" ^
    "%SRC_DIR%\main\ui\panels\CheckInOutPanel.java" ^
    "%SRC_DIR%\main\ui\panels\LeavePanel.java" ^
    "%SRC_DIR%\main\ui\panels\DepartmentPanel.java" ^
    "%SRC_DIR%\main\ui\panels\ReportPanel.java" ^
    "%SRC_DIR%\main\ui\panels\UserPanel.java" ^
    "%SRC_DIR%\main\ui\panels\SettingsPanel.java"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed. See errors above.
    pause
    exit /b 1
)

REM ── Copy db.properties to output ───────────────────────────────
copy /y "%PROPS_FILE%" "%OUT_DIR%\db.properties" >nul

echo [BUILD] Compilation successful!
echo.

REM ── Run the application ─────────────────────────────────────────
echo [RUN] Starting Employee Attendance System...
%JAVA_CMD% -cp "%OUT_DIR%;%JAR_FILE%" %MAIN_CLASS%

endlocal
