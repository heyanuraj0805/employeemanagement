package main.service;

import main.dao.AttendanceDAO;
import main.dao.EmployeeDAO;
import main.model.Attendance;
import main.model.Employee;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Business logic for attendance operations.
 *
 * Defines rules:
 *  – Check-in before 09:30 → Present
 *  – Check-in 09:31 – 10:30 → Late
 *  – No check-in → Absent
 *  – Working < 4 h → Half-Day
 */
public class AttendanceService {

    private static final LocalTime LATE_THRESHOLD        = LocalTime.of(9, 30);
    private static final LocalTime VERY_LATE_THRESHOLD   = LocalTime.of(10, 30);
    private static final LocalTime OFFICE_START          = LocalTime.of(9,  0);
    private static final LocalTime OFFICE_END            = LocalTime.of(18, 0);

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final EmployeeDAO   employeeDAO   = new EmployeeDAO();

    // ── Check-In ─────────────────────────────────────────────────────────────
    /**
     * Marks check-in for the given employee at the current time.
     * Throws if already checked in today.
     */
    public Attendance checkIn(int employeeId, String remarks) throws SQLException {
        LocalDate today = LocalDate.now();
        Attendance existing = attendanceDAO.findByEmployeeAndDate(employeeId, today);

        if (existing != null && existing.getCheckIn() != null) {
            throw new IllegalStateException("Employee has already checked in today.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime     time = now.toLocalTime();

        Attendance.Status status;
        if (time.isAfter(VERY_LATE_THRESHOLD)) {
            status = Attendance.Status.HalfDay;
        } else if (time.isAfter(LATE_THRESHOLD)) {
            status = Attendance.Status.Late;
        } else {
            status = Attendance.Status.Present;
        }

        Attendance a = (existing != null) ? existing : new Attendance();
        a.setEmployeeId    (employeeId);
        a.setAttendanceDate(today);
        a.setCheckIn       (now);
        a.setStatus        (status);
        a.setRemarks       (remarks);

        attendanceDAO.upsert(a);
        return a;
    }

    // ── Check-Out ────────────────────────────────────────────────────────────
    /**
     * Records check-out for the given employee.
     * Recalculates status if needed (working < 4 h → Half-Day).
     */
    public Attendance checkOut(int employeeId, String remarks) throws SQLException {
        LocalDate today = LocalDate.now();
        Attendance a = attendanceDAO.findByEmployeeAndDate(employeeId, today);

        if (a == null || a.getCheckIn() == null) {
            throw new IllegalStateException("No check-in record found for today.");
        }
        if (a.getCheckOut() != null) {
            throw new IllegalStateException("Employee has already checked out today.");
        }

        a.setCheckOut(LocalDateTime.now());
        if (remarks != null && !remarks.isBlank()) a.setRemarks(remarks);

        // Recompute status based on working hours
        double workHours = java.time.Duration.between(a.getCheckIn(), a.getCheckOut()).toMinutes() / 60.0;
        if (workHours < 4.0 && a.getStatus() != Attendance.Status.Late) {
            a.setStatus(Attendance.Status.HalfDay);
        }

        attendanceDAO.update(a);
        return a;
    }

    // ── Manual Mark ─────────────────────────────────────────────────────────
    /**
     * Admin/Manager manually marks attendance for any date.
     */
    public boolean manualMark(Attendance attendance) throws SQLException {
        return attendanceDAO.upsert(attendance);
    }

    // ── Queries ──────────────────────────────────────────────────────────────
    public List<Attendance> getTodayAttendance() throws SQLException {
        return attendanceDAO.findByDate(LocalDate.now());
    }

    public List<Attendance> getByDateRange(LocalDate from, LocalDate to) throws SQLException {
        return attendanceDAO.findByDateRange(from, to);
    }

    public List<Attendance> getByEmployee(int empId) throws SQLException {
        return attendanceDAO.findByEmployee(empId);
    }

    public List<Attendance> getByEmployeeAndMonth(int empId, int year, int month) throws SQLException {
        return attendanceDAO.findByEmployeeAndMonth(empId, year, month);
    }

    public Attendance getByEmployeeAndDate(int empId, LocalDate date) throws SQLException {
        return attendanceDAO.findByEmployeeAndDate(empId, date);
    }

    // ── Dashboard stats ──────────────────────────────────────────────────────
    public int getTodayPresentCount() throws SQLException { return attendanceDAO.countTodayPresent(); }
    public int getTodayAbsentCount()  throws SQLException { return attendanceDAO.countTodayAbsent();  }
    public int getTodayLateCount()    throws SQLException { return attendanceDAO.countTodayLate();    }

    /** Monthly summary: [present, absent, late, halfDay, onLeave] */
    public int[] getMonthlySummary(int empId, int year, int month) throws SQLException {
        return attendanceDAO.getMonthlySummary(empId, year, month);
    }

    public boolean delete(int id) throws SQLException {
        return attendanceDAO.delete(id);
    }
}
