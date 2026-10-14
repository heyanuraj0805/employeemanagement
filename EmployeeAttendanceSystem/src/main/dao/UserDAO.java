package main.dao;

import main.config.DatabaseConfig;
import main.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for User entities.
 */
public class UserDAO {

    // ── Authenticate ────────────────────────────────────────────────────────
    /**
     * Returns the matching User or {@code null} if credentials are wrong
     * or the account is inactive.
     */
    public User authenticate(String username, String passwordHash) throws SQLException {
        String sql = """
                SELECT id, username, password, role, full_name, email, is_active, created_at
                  FROM users
                 WHERE username = ? AND password = ? AND is_active = 1
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY full_name";
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean insert(User u) throws SQLException {
        String sql = """
                INSERT INTO users (username, password, role, full_name, email, is_active)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole().name());
            ps.setString(4, u.getFullName());
            ps.setString(5, u.getEmail());
            ps.setBoolean(6, u.isActive());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) u.setId(gk.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(User u) throws SQLException {
        String sql = """
                UPDATE users
                   SET username=?, password=?, role=?, full_name=?, email=?, is_active=?
                 WHERE id=?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString (1, u.getUsername());
            ps.setString (2, u.getPassword());
            ps.setString (3, u.getRole().name());
            ps.setString (4, u.getFullName());
            ps.setString (5, u.getEmail());
            ps.setBoolean(6, u.isActive());
            ps.setInt    (7, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean changePassword(int userId, String newHash) throws SQLException {
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt   (2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Mapping ──────────────────────────────────────────────────────────────
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId       (rs.getInt    ("id"));
        u.setUsername (rs.getString ("username"));
        u.setPassword (rs.getString ("password"));
        u.setRole     (User.Role.valueOf(rs.getString("role")));
        u.setFullName (rs.getString ("full_name"));
        u.setEmail    (rs.getString ("email"));
        u.setActive   (rs.getBoolean("is_active"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) u.setCreatedAt(ca.toLocalDateTime());
        return u;
    }
}
