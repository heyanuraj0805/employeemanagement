package main.dao;

import main.config.DatabaseConfig;
import main.model.LeaveRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for LeaveRequest entities.
 */
public class LeaveRequestDAO {

    private static final String BASE_SQL = """
            SELECT lr.*,
                   CONCAT(e.first_name,' ',e.last_name) AS emp_name,
                   e.emp_code,
                   lt.name AS leave_type_name
              FROM leave_requests lr
              JOIN employees   e  ON e.id  = lr.employee_id
              JOIN leave_types lt ON lt.id = lr.leave_type_id
            """;

    public List<LeaveRequest> findAll() throws SQLException {
        String sql = BASE_SQL + " ORDER BY lr.applied_on DESC";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<LeaveRequest> findByEmployee(int empId) throws SQLException {
        String sql = BASE_SQL + " WHERE lr.employee_id=? ORDER BY lr.applied_on DESC";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<LeaveRequest> findPending() throws SQLException {
        String sql = BASE_SQL + " WHERE lr.status='Pending' ORDER BY lr.applied_on";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public LeaveRequest findById(int id) throws SQLException {
        String sql = BASE_SQL + " WHERE lr.id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean insert(LeaveRequest lr) throws SQLException {
        String sql = """
                INSERT INTO leave_requests (employee_id, leave_type_id, from_date, to_date, reason, status)
                VALUES (?,?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, lr.getEmployeeId());
            ps.setInt   (2, lr.getLeaveTypeId());
            ps.setObject(3, lr.getFromDate());
            ps.setObject(4, lr.getToDate());
            ps.setString(5, lr.getReason());
            ps.setString(6, lr.getStatus().name());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) lr.setId(gk.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateStatus(int id, LeaveRequest.Status status, int reviewedBy, String remarks) throws SQLException {
        String sql = """
                UPDATE leave_requests
                   SET status=?, reviewed_by=?, reviewed_on=NOW(), review_remarks=?
                 WHERE id=?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, reviewedBy);
            ps.setString(3, remarks);
            ps.setInt   (4, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM leave_requests WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int countPending() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM leave_requests WHERE status='Pending'")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private LeaveRequest mapRow(ResultSet rs) throws SQLException {
        LeaveRequest lr = new LeaveRequest();
        lr.setId           (rs.getInt   ("id"));
        lr.setEmployeeId   (rs.getInt   ("employee_id"));
        lr.setEmployeeName (rs.getString("emp_name"));
        lr.setEmpCode      (rs.getString("emp_code"));
        lr.setLeaveTypeId  (rs.getInt   ("leave_type_id"));
        lr.setLeaveTypeName(rs.getString("leave_type_name"));
        Date fd = rs.getDate("from_date");
        if (fd != null) lr.setFromDate(fd.toLocalDate());
        Date td = rs.getDate("to_date");
        if (td != null) lr.setToDate(td.toLocalDate());
        lr.setReason       (rs.getString("reason"));
        lr.setStatus       (LeaveRequest.Status.valueOf(rs.getString("status")));
        Timestamp ao = rs.getTimestamp("applied_on");
        if (ao != null) lr.setAppliedOn(ao.toLocalDateTime());
        int rb = rs.getInt("reviewed_by");
        if (!rs.wasNull()) lr.setReviewedBy(rb);
        Timestamp ro = rs.getTimestamp("reviewed_on");
        if (ro != null) lr.setReviewedOn(ro.toLocalDateTime());
        lr.setReviewRemarks(rs.getString("review_remarks"));
        return lr;
    }
}
