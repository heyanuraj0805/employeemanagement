package main.dao;

import main.config.DatabaseConfig;
import main.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for Department entities.
 */
public class DepartmentDAO {

    public List<Department> findAll() throws SQLException {
        String sql = "SELECT * FROM departments ORDER BY name";
        List<Department> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Department findById(int id) throws SQLException {
        String sql = "SELECT * FROM departments WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean insert(Department d) throws SQLException {
        String sql = "INSERT INTO departments (name, description) VALUES (?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getDescription());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) d.setId(gk.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Department d) throws SQLException {
        String sql = "UPDATE departments SET name=?, description=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getDescription());
            ps.setInt   (3, d.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM departments WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Count of employees in a department – used before delete. */
    public int countEmployees(int deptId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE department_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Department mapRow(ResultSet rs) throws SQLException {
        return new Department(rs.getInt("id"), rs.getString("name"), rs.getString("description"));
    }
}
