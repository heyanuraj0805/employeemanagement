package main.ui.panels;

import main.dao.EmployeeDAO;
import main.dao.LeaveRequestDAO;
import main.service.AttendanceService;
import main.service.SessionManager;
import main.ui.MainFrame;
import main.util.DateUtil;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard panel – shows a quick-glance summary of today's attendance,
 * employee stats, pending leaves, and the current date/time.
 */
public class DashboardPanel extends JPanel {

    private final MainFrame        mainFrame;
    private final AttendanceService attSvc  = new AttendanceService();
    private final EmployeeDAO       empDAO  = new EmployeeDAO();
    private final LeaveRequestDAO   leaveDAO= new LeaveRequestDAO();

    // Stat labels (updated on refresh)
    private JLabel presentLbl, absentLbl, lateLbl, totalEmpLbl, pendingLeaveLbl, activeLbl;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
        refresh();
    }

    private void buildUI() {
        // ── Page title ─────────────────────────────────────────────────────
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.PRIMARY_DARK);

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        JLabel dateLbl = new JLabel(dateStr);
        dateLbl.setFont(UITheme.FONT_BODY);
        dateLbl.setForeground(UITheme.TEXT_SECONDARY);

        JButton refreshBtn = UITheme.infoButton("⟳  Refresh");
        refreshBtn.addActionListener(e -> refresh());

        JPanel titleRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        titleRight.setOpaque(false);
        titleRight.add(dateLbl);
        titleRight.add(refreshBtn);

        titleRow.add(title,      BorderLayout.WEST);
        titleRow.add(titleRight, BorderLayout.EAST);

        // ── Today's attendance stat cards ──────────────────────────────────
        JPanel todaySection = new JPanel(new GridLayout(1, 5, 16, 0));
        todaySection.setOpaque(false);
        todaySection.setBorder(new EmptyBorder(20, 0, 0, 0));

        todaySection.add(createStatCard("Today Present",       "—", UITheme.SUCCESS,  c -> presentLbl     = c));
        todaySection.add(createStatCard("Today Absent",        "—", UITheme.DANGER,   c -> absentLbl      = c));
        todaySection.add(createStatCard("Late Arrivals",       "—", UITheme.WARNING,  c -> lateLbl        = c));
        todaySection.add(createStatCard("Total Employees",     "—", UITheme.PRIMARY,  c -> totalEmpLbl    = c));
        todaySection.add(createStatCard("Pending Leaves",      "—", UITheme.ACCENT,   c -> pendingLeaveLbl= c));

        // ── Quick-action buttons ───────────────────────────────────────────
        JPanel actionSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionSection.setOpaque(false);
        actionSection.setBorder(new EmptyBorder(24, 0, 8, 0));

        JLabel actLbl = new JLabel("Quick Actions:");
        actLbl.setFont(UITheme.FONT_BOLD);
        actLbl.setForeground(UITheme.TEXT_SECONDARY);
        actionSection.add(actLbl);

        JButton btnCheckIn = UITheme.successButton("✅  Check In / Out");
        btnCheckIn.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_CHECKIN));

        JButton btnEmployees = UITheme.primaryButton("👥  Manage Employees");
        btnEmployees.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_EMPLOYEES));

        JButton btnAttendance = UITheme.infoButton("📋  View Attendance");
        btnAttendance.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_ATTENDANCE));

        JButton btnReports = UITheme.warningButton("📈  Generate Report");
        btnReports.addActionListener(e -> mainFrame.showPanel(MainFrame.PANEL_REPORTS));

        actionSection.add(btnCheckIn);
        actionSection.add(btnEmployees);
        actionSection.add(btnAttendance);
        actionSection.add(btnReports);

        // ── Info cards row ─────────────────────────────────────────────────
        JPanel infoRow = new JPanel(new GridLayout(1, 2, 16, 0));
        infoRow.setOpaque(false);
        infoRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        infoRow.add(buildWelcomeCard());
        infoRow.add(buildSystemInfoCard());

        // ── Assembly ───────────────────────────────────────────────────────
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(todaySection);
        center.add(actionSection);
        center.add(infoRow);

        add(titleRow, BorderLayout.NORTH);
        add(center,   BorderLayout.CENTER);
    }

    @FunctionalInterface
    interface LabelConsumer { void accept(JLabel lbl); }

    private JPanel createStatCard(String title, String initVal, Color color, LabelConsumer cons) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, color),
            new EmptyBorder(16, 16, 16, 16)));

        JLabel valLbl = new JLabel(initVal);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valLbl.setForeground(color);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);

        card.add(valLbl,   BorderLayout.CENTER);
        card.add(titleLbl, BorderLayout.SOUTH);
        cons.accept(valLbl);
        return card;
    }

    private JPanel buildWelcomeCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel wLabel = UITheme.sectionHeader("👋  Welcome back, " +
            SessionManager.getInstance().getCurrentUser().getFullName());
        JTextArea info = new JTextArea(
            "Role: " + SessionManager.getInstance().getCurrentUser().getRole().name() + "\n" +
            "System: Employee Attendance Management System\n" +
            "Organisation: Pratinik Infotech\n" +
            "Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        info.setEditable(false);
        info.setFont(UITheme.FONT_BODY);
        info.setForeground(UITheme.TEXT_SECONDARY);
        info.setBackground(UITheme.BG_CARD);
        info.setLineWrap(true);

        card.add(wLabel, BorderLayout.NORTH);
        card.add(info,   BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSystemInfoCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = UITheme.sectionHeader("ℹ️  System Information");
        JTextArea info = new JTextArea(
            "Project: Employee Attendance System\n" +
            "Student: Anuraj Bhawarlal Jangid\n" +
            "Roll No: BT230016CS\n" +
            "College: J D College of Engineering & Management, Nagpur\n" +
            "Internship: Pratinik Infotech  (May – July 2026)\n" +
            "Technology: Java 17 | Swing | MySQL | JDBC");
        info.setEditable(false);
        info.setFont(UITheme.FONT_BODY);
        info.setForeground(UITheme.TEXT_SECONDARY);
        info.setBackground(UITheme.BG_CARD);
        info.setLineWrap(true);

        card.add(title, BorderLayout.NORTH);
        card.add(info,  BorderLayout.CENTER);
        return card;
    }

    // ── Data refresh ─────────────────────────────────────────────────────────
    public void refresh() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override protected int[] doInBackground() throws Exception {
                int present  = attSvc.getTodayPresentCount();
                int absent   = attSvc.getTodayAbsentCount();
                int late     = attSvc.getTodayLateCount();
                int total    = empDAO.countAll();
                int active   = empDAO.countActive();
                int pending  = leaveDAO.countPending();
                return new int[]{present, absent, late, total, active, pending};
            }

            @Override protected void done() {
                try {
                    int[] d = get();
                    presentLbl     .setText(String.valueOf(d[0]));
                    absentLbl      .setText(String.valueOf(d[1]));
                    lateLbl        .setText(String.valueOf(d[2]));
                    totalEmpLbl    .setText(String.valueOf(d[3]));
                    pendingLeaveLbl.setText(String.valueOf(d[5]));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                        "Error loading dashboard: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
