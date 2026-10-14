package main.dao;

import main.config.DatabaseConfig;
import main.model.Attendance;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for Attendance entities.
 */
public class AttendanceDAO {

    // ── Read ─────────────────────────────────────────────────────────────────
    public Attendance findByEmployeeAndDate(int employeeId, LocalDate date) throws SQLException {
        String sql = """
                SELECT a.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name,
                       e.emp_code, d.name AS dept_name
                  FROM attendance a
                  JOIN employees   e ON e.id = a.employee_id
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE a.employee_id = ? AND a.attendance_date = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, employeeId);
            ps.setObject(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Attendance> findByDate(LocalDate date) throws SQLException {
        String sql = """
                SELECT a.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name,
                       e.emp_code, d.name AS dept_name
                  FROM attendance a
                  JOIN employees   e ON e.id = a.employee_id
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE a.attendance_date = ?
                 ORDER BY emp_name
                """;
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Attendance> findByEmployee(int employeeId) throws SQLException {
        String sql = """
                SELECT a.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name,
                       e.emp_code, d.name AS dept_name
                  FROM attendance a
                  JOIN employees   e ON e.id = a.employee_id
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE a.employee_id = ?
                 ORDER BY a.attendance_date DESC
                """;
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Attendance> findByEmployeeAndMonth(int employeeId, int year, int month) throws SQLException {
        String sql = """
                SELECT a.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name,
                       e.emp_code, d.name AS dept_name
                  FROM attendance a
                  JOIN employees   e ON e.id = a.employee_id
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE a.employee_id = ?
                   AND YEAR(a.attendance_date)  = ?
                   AND MONTH(a.attendance_date) = ?
                 ORDER BY a.attendance_date
                """;
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Attendance> findByDateRange(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT a.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name,
                       e.emp_code, d.name AS dept_name
                  FROM attendance a
                  JOIN employees   e ON e.id = a.employee_id
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE a.attendance_date BETWEEN ? AND ?
                 ORDER BY a.attendance_date, emp_name
                """;
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, from);
            ps.setObject(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Attendance> findByEmployeeAndDateRange(int empId, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT a.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name,
                       e.emp_code, d.name AS dept_name
                  FROM attendance a
                  JOIN employees   e ON e.id = a.employee_id
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE a.employee_id = ?
                   AND a.attendance_date BETWEEN ? AND ?
                 ORDER BY a.attendance_date
                """;
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, empId);
            ps.setObject(2, from);
            ps.setObject(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Today's stats ────────────────────────────────────────────────────────
    public int countTodayPresent() throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE attendance_date=CURDATE() AND status='Present'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countTodayAbsent() throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE attendance_date=CURDATE() AND status='Absent'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countTodayLate() throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE attendance_date=CURDATE() AND status='Late'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── Write ────────────────────────────────────────────────────────────────
    public boolean insert(Attendance a) throws SQLException {
        String sql = """
                INSERT INTO attendance (employee_id, attendance_date, check_in, check_out, status, remarks)
                VALUES (?,?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, a.getEmployeeId());
            ps.setObject(2, a.getAttendanceDate());
            ps.setObject(3, a.getCheckIn()  != null ? Timestamp.valueOf(a.getCheckIn())  : null);
            ps.setObject(4, a.getCheckOut() != null ? Timestamp.valueOf(a.getCheckOut()) : null);
            ps.setString(5, a.getStatus().toDbString());
            ps.setString(6, a.getRemarks());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) a.setId(gk.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Attendance a) throws SQLException {
        String sql = """
                UPDATE attendance
                   SET check_in=?, check_out=?, status=?, remarks=?
                 WHERE id=?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, a.getCheckIn()  != null ? Timestamp.valueOf(a.getCheckIn())  : null);
            ps.setObject(2, a.getCheckOut() != null ? Timestamp.valueOf(a.getCheckOut()) : null);
            ps.setString(3, a.getStatus().toDbString());
            ps.setString(4, a.getRemarks());
            ps.setInt   (5, a.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Upsert: insert if no record for (employee, date), else update. */
    public boolean upsert(Attendance a) throws SQLException {
        Attendance existing = findByEmployeeAndDate(a.getEmployeeId(), a.getAttendanceDate());
        if (existing == null) {
            return insert(a);
        } else {
            a.setId(existing.getId());
            return update(a);
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM attendance WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Summary by employee × month ─────────────────────────────────────────
    public int[] getMonthlySummary(int empId, int year, int month) throws SQLException {
        // returns [present, absent, late, halfday, onLeave]
        String sql = """
                SELECT
                  SUM(status='Present') AS p,
                  SUM(status='Absent')  AS ab,
                  SUM(status='Late')    AS l,
                  SUM(status='Half-Day') AS hd,
                  SUM(status='On-Leave') AS ol
                FROM attendance
                WHERE employee_id=? AND YEAR(attendance_date)=? AND MONTH(attendance_date)=?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt("p"), rs.getInt("ab"),
                                     rs.getInt("l"), rs.getInt("hd"), rs.getInt("ol")};
                }
            }
        }
        return new int[]{0,0,0,0,0};
    }

    // ── Mapping ──────────────────────────────────────────────────────────────
    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId             (rs.getInt   ("id"));
        a.setEmployeeId     (rs.getInt   ("employee_id"));
        a.setEmployeeName   (rs.getString("emp_name"));
        a.setEmpCode        (rs.getString("emp_code"));
        a.setDepartmentName (rs.getString("dept_name"));
        Date d = rs.getDate("attendance_date");
        if (d != null) a.setAttendanceDate(d.toLocalDate());
        Timestamp ci = rs.getTimestamp("check_in");
        if (ci != null) a.setCheckIn(ci.toLocalDateTime());
        Timestamp co = rs.getTimestamp("check_out");
        if (co != null) a.setCheckOut(co.toLocalDateTime());
        a.setStatus        (Attendance.Status.fromDbString(rs.getString("status")));
        a.setWorkingHours  (rs.getDouble ("working_hours"));
        a.setOvertimeHours (rs.getDouble ("overtime_hours"));
        a.setRemarks       (rs.getString ("remarks"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) a.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) a.setUpdatedAt(ua.toLocalDateTime());
        return a;
    }
}
