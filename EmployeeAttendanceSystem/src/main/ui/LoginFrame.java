package main.ui;

import main.config.DatabaseConfig;
import main.model.User;
import main.service.SessionManager;
import main.util.PasswordUtil;
import main.util.UITheme;
import main.dao.UserDAO;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

/**
 * Login window – the first screen the user sees.
 */
public class LoginFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JButton        loginBtn;
    private JLabel         statusLabel;

    public LoginFrame() {
        setTitle("Employee Attendance System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        // ── Root panel with gradient background ────────────────────────────
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, UITheme.PRIMARY_DARK,
                        0, getHeight(), UITheme.PRIMARY_LIGHT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ── Header ─────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("🏢", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Employee Attendance", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Pratinik Infotech", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(255, 255, 255, 180));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        header.add(Box.createVerticalStrut(10));
        header.add(logo);
        header.add(Box.createVerticalStrut(8));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(24));

        // ── Card ───────────────────────────────────────────────────────────
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        // Sign-in label
        JLabel signIn = new JLabel("Sign In");
        signIn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        signIn.setForeground(UITheme.PRIMARY_DARK);
        card.add(signIn, gbc);

        // Username
        card.add(fieldLabel("Username"), gbc);
        usernameField = UITheme.textField(20);
        usernameField.setPreferredSize(new Dimension(300, 38));
        card.add(usernameField, gbc);

        // Password
        card.add(fieldLabel("Password"), gbc);
        passwordField = new JPasswordField(20);
        passwordField.setFont(UITheme.FONT_BODY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        passwordField.setPreferredSize(new Dimension(300, 38));
        card.add(passwordField, gbc);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(statusLabel, gbc);

        // Login button
        loginBtn = UITheme.primaryButton("  Login  ");
        loginBtn.setPreferredSize(new Dimension(300, 42));
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(loginBtn, gbc);

        // Default credentials hint
        JLabel hint = new JLabel("Default: admin / admin123");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_SECONDARY);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(hint, gbc);

        // ── Wire up ────────────────────────────────────────────────────────
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        root.add(header, BorderLayout.NORTH);
        root.add(card,   BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel(
            "Anuraj Jangid · BT230016CS · J D College, Nagpur", SwingConstants.CENTER);
        footer.setFont(UITheme.FONT_SMALL);
        footer.setForeground(new Color(255,255,255,150));
        footer.setBorder(new EmptyBorder(12, 0, 0, 0));
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        return lbl;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        loginBtn.setEnabled(false);
        statusLabel.setText("Authenticating…");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override protected User doInBackground() throws Exception {
                if (!DatabaseConfig.testConnection()) {
                    throw new RuntimeException("Cannot connect to database. Check db.properties.");
                }
                UserDAO dao = new UserDAO();
                return dao.authenticate(username, PasswordUtil.hash(password));
            }

            @Override protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        SessionManager.getInstance().setCurrentUser(user);
                        dispose();
                        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
                    } else {
                        statusLabel.setText("Invalid username or password.");
                        passwordField.setText("");
                    }
                } catch (Exception ex) {
                    statusLabel.setText(ex.getCause() != null
                            ? ex.getCause().getMessage() : ex.getMessage());
                } finally {
                    loginBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
