package main.dao;

import main.config.DatabaseConfig;
import main.model.Employee;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for Employee entities.
 */
public class EmployeeDAO {

    // ── Read ─────────────────────────────────────────────────────────────────
    public List<Employee> findAll() throws SQLException {
        String sql = """
                SELECT e.*, d.name AS dept_name
                  FROM employees e
                  LEFT JOIN departments d ON d.id = e.department_id
                 ORDER BY e.first_name, e.last_name
                """;
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Employee> findActive() throws SQLException {
        String sql = """
                SELECT e.*, d.name AS dept_name
                  FROM employees e
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE e.status = 'Active'
                 ORDER BY e.first_name, e.last_name
                """;
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Employee findById(int id) throws SQLException {
        String sql = """
                SELECT e.*, d.name AS dept_name
                  FROM employees e
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE e.id = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Employee findByEmpCode(String empCode) throws SQLException {
        String sql = """
                SELECT e.*, d.name AS dept_name
                  FROM employees e
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE e.emp_code = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Full-text search across name, email, code, department. */
    public List<Employee> search(String keyword) throws SQLException {
        String sql = """
                SELECT e.*, d.name AS dept_name
                  FROM employees e
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE LOWER(e.first_name)  LIKE LOWER(?)
                    OR LOWER(e.last_name)   LIKE LOWER(?)
                    OR LOWER(e.emp_code)    LIKE LOWER(?)
                    OR LOWER(e.email)       LIKE LOWER(?)
                    OR LOWER(d.name)        LIKE LOWER(?)
                 ORDER BY e.first_name
                """;
        String kw = "%" + keyword + "%";
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) ps.setString(i, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Employee> findByDepartment(int deptId) throws SQLException {
        String sql = """
                SELECT e.*, d.name AS dept_name
                  FROM employees e
                  LEFT JOIN departments d ON d.id = e.department_id
                 WHERE e.department_id = ?
                 ORDER BY e.first_name
                """;
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Write ────────────────────────────────────────────────────────────────
    public boolean insert(Employee e) throws SQLException {
        String sql = """
                INSERT INTO employees
                  (emp_code, first_name, last_name, gender, dob, email, phone,
                   address, department_id, designation, date_of_joining,
                   employment_type, status)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getEmpCode());
            ps.setString(2, e.getFirstName());
            ps.setString(3, e.getLastName());
            ps.setString(4, e.getGender().name());
            ps.setObject(5, e.getDob());
            ps.setString(6, e.getEmail());
            ps.setString(7, e.getPhone());
            ps.setString(8, e.getAddress());
            if (e.getDepartmentId() > 0) ps.setInt(9, e.getDepartmentId());
            else                          ps.setNull(9, Types.INTEGER);
            ps.setString(10, e.getDesignation());
            ps.setObject(11, e.getDateOfJoining());
            ps.setString(12, toDbEmpType(e.getEmploymentType()));
            ps.setString(13, e.getStatus().name());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) e.setId(gk.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Employee e) throws SQLException {
        String sql = """
                UPDATE employees
                   SET emp_code=?, first_name=?, last_name=?, gender=?, dob=?,
                       email=?, phone=?, address=?, department_id=?, designation=?,
                       date_of_joining=?, employment_type=?, status=?
                 WHERE id=?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getEmpCode());
            ps.setString(2, e.getFirstName());
            ps.setString(3, e.getLastName());
            ps.setString(4, e.getGender().name());
            ps.setObject(5, e.getDob());
            ps.setString(6, e.getEmail());
            ps.setString(7, e.getPhone());
            ps.setString(8, e.getAddress());
            if (e.getDepartmentId() > 0) ps.setInt(9, e.getDepartmentId());
            else                          ps.setNull(9, Types.INTEGER);
            ps.setString(10, e.getDesignation());
            ps.setObject(11, e.getDateOfJoining());
            ps.setString(12, toDbEmpType(e.getEmploymentType()));
            ps.setString(13, e.getStatus().name());
            ps.setInt   (14, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int countAll() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM employees")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countActive() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM employees WHERE status='Active'")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Generate the next employee code like EMP009, EMP010 … */
    public String generateNextEmpCode() throws SQLException {
        String sql = "SELECT emp_code FROM employees ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("emp_code");
                try {
                    int num = Integer.parseInt(last.replaceAll("[^\\d]", ""));
                    return String.format("EMP%03d", num + 1);
                } catch (NumberFormatException ignored) {}
            }
        }
        return "EMP001";
    }

    // ── Mapping ──────────────────────────────────────────────────────────────
    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId            (rs.getInt   ("id"));
        e.setEmpCode       (rs.getString("emp_code"));
        e.setFirstName     (rs.getString("first_name"));
        e.setLastName      (rs.getString("last_name"));
        e.setGender        (Employee.Gender.valueOf(rs.getString("gender")));
        Date dob = rs.getDate("dob");
        if (dob != null) e.setDob(dob.toLocalDate());
        e.setEmail         (rs.getString("email"));
        e.setPhone         (rs.getString("phone"));
        e.setAddress       (rs.getString("address"));
        e.setDepartmentId  (rs.getInt   ("department_id"));
        e.setDepartmentName(rs.getString("dept_name"));
        e.setDesignation   (rs.getString("designation"));
        Date doj = rs.getDate("date_of_joining");
        if (doj != null) e.setDateOfJoining(doj.toLocalDate());
        e.setEmploymentType(fromDbEmpType(rs.getString("employment_type")));
        e.setStatus        (Employee.Status.valueOf(rs.getString("status")));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) e.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) e.setUpdatedAt(ua.toLocalDateTime());
        return e;
    }

    private String toDbEmpType(Employee.EmploymentType t) {
        return switch (t) {
            case FullTime  -> "Full-Time";
            case PartTime  -> "Part-Time";
            case Contract  -> "Contract";
            case Intern    -> "Intern";
        };
    }

    private Employee.EmploymentType fromDbEmpType(String s) {
        return switch (s) {
            case "Full-Time" -> Employee.EmploymentType.FullTime;
            case "Part-Time" -> Employee.EmploymentType.PartTime;
            case "Contract"  -> Employee.EmploymentType.Contract;
            default          -> Employee.EmploymentType.Intern;
        };
    }
}
