package main.ui;

import main.service.SessionManager;
import main.ui.panels.*;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main application window.
 *
 * Layout:
 *  ┌──────────────────────────────────────────┐
 *  │              TOP BAR                    │
 *  ├──────────┬───────────────────────────────┤
 *  │ SIDEBAR  │       CONTENT  (CardLayout)   │
 *  └──────────┴───────────────────────────────┘
 */
public class MainFrame extends JFrame {

    private JPanel  contentPanel;
    private CardLayout cardLayout;

    // Panel keys
    public static final String PANEL_DASHBOARD  = "Dashboard";
    public static final String PANEL_EMPLOYEES  = "Employees";
    public static final String PANEL_ATTENDANCE = "Attendance";
    public static final String PANEL_CHECKIN    = "CheckIn";
    public static final String PANEL_REPORTS    = "Reports";
    public static final String PANEL_LEAVES     = "Leaves";
    public static final String PANEL_DEPARTMENTS= "Departments";
    public static final String PANEL_USERS      = "Users";
    public static final String PANEL_SETTINGS   = "Settings";

    private JLabel clockLabel;
    private ButtonGroup navGroup = new ButtonGroup();

    public MainFrame() {
        setTitle("Employee Attendance System — Pratinik Infotech");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 650));
        buildUI();
        startClock();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        root.add(buildTopBar(),   BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Top Bar ──────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_CARD);
        bar.setPreferredSize(new Dimension(0, 52));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));

        JLabel appName = new JLabel("  🏢  Employee Attendance System");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appName.setForeground(UITheme.PRIMARY_DARK);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        right.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(UITheme.FONT_BODY);
        clockLabel.setForeground(UITheme.TEXT_SECONDARY);

        String userName = SessionManager.getInstance().getCurrentUser().getFullName();
        String userRole = SessionManager.getInstance().getCurrentUser().getRole().name();
        JLabel userLbl  = new JLabel("👤 " + userName + "  [" + userRole + "]");
        userLbl.setFont(UITheme.FONT_BOLD);
        userLbl.setForeground(UITheme.PRIMARY);

        JButton logoutBtn = UITheme.dangerButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(90, 32));
        logoutBtn.addActionListener(e -> doLogout());

        right.add(clockLabel);
        right.add(userLbl);
        right.add(logoutBtn);

        bar.add(appName, BorderLayout.WEST);
        bar.add(right,   BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(12, 0, 12, 0));

        boolean isAdmin   = SessionManager.getInstance().isAdmin();
        boolean isManager = SessionManager.getInstance().isManager();

        addNavItem(sidebar, "📊  Dashboard",   PANEL_DASHBOARD,  true);
        addNavItem(sidebar, "👥  Employees",   PANEL_EMPLOYEES,  true);
        addNavItem(sidebar, "✅  Attendance",  PANEL_ATTENDANCE, true);
        addNavItem(sidebar, "🕐  Check In/Out",PANEL_CHECKIN,    true);
        addNavItem(sidebar, "📋  Leave Mgmt",  PANEL_LEAVES,     isManager);
        addNavItem(sidebar, "📁  Departments", PANEL_DEPARTMENTS,isAdmin);
        addNavItem(sidebar, "📈  Reports",     PANEL_REPORTS,    true);
        addNavItem(sidebar, "👤  Users",       PANEL_USERS,      isAdmin);
        addNavItem(sidebar, "⚙️  Settings",    PANEL_SETTINGS,   true);

        return sidebar;
    }

    private void addNavItem(JPanel sidebar, String label, String panelKey, boolean visible) {
        if (!visible) return;

        JToggleButton btn = new JToggleButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (isSelected()) {
                    g2.setColor(UITheme.SIDEBAR_ACTIVE);
                    g2.fillRect(0, 0, 4, getHeight());
                    g2.setColor(UITheme.SIDEBAR_ACTIVE);
                    g2.fillRect(4, 0, getWidth() - 4, getHeight());
                } else if (getModel().isRollover()) {
                    g2.setColor(UITheme.SIDEBAR_HOVER);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(UITheme.SIDEBAR_TEXT);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));

        btn.addActionListener(e -> {
            btn.setForeground(Color.WHITE);
            cardLayout.show(contentPanel, panelKey);
        });
        btn.addChangeListener(e -> {
            if (!btn.isSelected()) btn.setForeground(UITheme.SIDEBAR_TEXT);
        });

        navGroup.add(btn);
        sidebar.add(btn);

        // Select Dashboard by default
        if (panelKey.equals(PANEL_DASHBOARD)) {
            btn.setSelected(true);
            btn.setForeground(Color.WHITE);
        }
    }

    // ── Content ──────────────────────────────────────────────────────────────
    private JPanel buildContent() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_MAIN);

        contentPanel.add(new DashboardPanel(this),   PANEL_DASHBOARD);
        contentPanel.add(new EmployeePanel(),         PANEL_EMPLOYEES);
        contentPanel.add(new AttendancePanel(),       PANEL_ATTENDANCE);
        contentPanel.add(new CheckInOutPanel(),       PANEL_CHECKIN);
        contentPanel.add(new LeavePanel(),            PANEL_LEAVES);
        contentPanel.add(new DepartmentPanel(),       PANEL_DEPARTMENTS);
        contentPanel.add(new ReportPanel(),           PANEL_REPORTS);
        contentPanel.add(new UserPanel(),             PANEL_USERS);
        contentPanel.add(new SettingsPanel(),         PANEL_SETTINGS);

        cardLayout.show(contentPanel, PANEL_DASHBOARD);
        return contentPanel;
    }

    /** Switches to a panel by key (called from other panels). */
    public void showPanel(String key) {
        cardLayout.show(contentPanel, key);
    }

    // ── Clock ────────────────────────────────────────────────────────────────
    private void startClock() {
        Timer timer = new Timer(1000, e -> clockLabel.setText(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm:ss"))));
        timer.start();
        timer.getActionListeners()[0].actionPerformed(null);
    }

    // ── Logout ───────────────────────────────────────────────────────────────
    private void doLogout() {
        int opt = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
