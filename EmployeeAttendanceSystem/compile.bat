@echo off
REM ================================================================
REM  Compile only (no run) – useful for checking for errors
REM  Employee Attendance Management System
REM ================================================================
setlocal
cd /d "%~dp0"

set JAVAC_CMD=javac
set SRC_DIR=src
set OUT_DIR=out\classes
set JAR_FILE=lib\mysql-connector-j.jar

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo [BUILD] Compiling all sources...

for /r "%SRC_DIR%" %%f in (*.java) do set SOURCES=!SOURCES! "%%f"

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

if %errorlevel% equ 0 (
    echo [BUILD] SUCCESS — all sources compiled.
    copy /y "db.properties" "%OUT_DIR%\db.properties" >nul
) else (
    echo [BUILD] FAILED — see errors above.
)

pause
endlocal
