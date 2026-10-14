package main.ui.panels;

import main.dao.EmployeeDAO;
import main.model.Attendance;
import main.model.Employee;
import main.service.AttendanceService;
import main.util.DateUtil;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

/**
 * Reports panel – monthly summary, date-range export to CSV/text.
 */
public class ReportPanel extends JPanel {

    private final AttendanceService attSvc = new AttendanceService();
    private final EmployeeDAO       empDAO = new EmployeeDAO();

    private JComboBox<Employee> empCombo;
    private JComboBox<String>   monthBox, yearBox;
    private JTable              table;
    private DefaultTableModel   tableModel;
    private JLabel              summaryLabel;
    private List<Attendance>    currentReport;

    private static final String[] COLS = {
        "Date", "Day", "Check-In", "Check-Out", "Working Hrs", "Overtime", "Status", "Remarks"
    };

    public ReportPanel() {
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Attendance Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.PRIMARY_DARK);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));

        // ── Controls ────────────────────────────────────────────────────────
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controls.setBackground(UITheme.BG_CARD);
        controls.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER),
            new EmptyBorder(8, 12, 8, 12)));

        empCombo = new JComboBox<>();
        empCombo.setPreferredSize(new Dimension(220, 30));
        new SwingWorker<List<Employee>, Void>() {
            @Override protected List<Employee> doInBackground() throws Exception {
                return empDAO.findAll();
            }
            @Override protected void done() {
                try { for (Employee e : get()) empCombo.addItem(e); }
                catch (Exception ignored) {}
            }
        }.execute();

        // Month combo
        monthBox = new JComboBox<>();
        for (int m = 1; m <= 12; m++)
            monthBox.addItem(Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        monthBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        // Year combo
        yearBox = new JComboBox<>();
        int thisYear = LocalDate.now().getYear();
        for (int y = thisYear - 3; y <= thisYear + 1; y++) yearBox.addItem(String.valueOf(y));
        yearBox.setSelectedItem(String.valueOf(thisYear));

        JButton generateBtn = UITheme.primaryButton("📊 Generate Report");
        JButton exportCsvBtn = UITheme.successButton("💾 Export CSV");
        JButton printBtn     = UITheme.infoButton("🖨 Print / Preview");

        generateBtn .addActionListener(e -> generateReport());
        exportCsvBtn.addActionListener(e -> exportCsv());
        printBtn    .addActionListener(e -> printReport());

        controls.add(new JLabel("Employee:"));
        controls.add(empCombo);
        controls.add(new JLabel("Month:"));
        controls.add(monthBox);
        controls.add(new JLabel("Year:"));
        controls.add(yearBox);
        controls.add(generateBtn);
        controls.add(exportCsvBtn);
        controls.add(printBtn);

        // ── Summary bar ─────────────────────────────────────────────────────
        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(UITheme.FONT_BOLD);
        summaryLabel.setForeground(UITheme.PRIMARY_DARK);
        summaryLabel.setBorder(new EmptyBorder(6, 4, 6, 4));

        // ── Table ──────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // Status colour
        table.getColumnModel().getColumn(6).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                    if (!sel) {
                        String s = v != null ? v.toString() : "";
                        setForeground(switch (s) {
                            case "Present"   -> UITheme.SUCCESS;
                            case "Absent"    -> UITheme.DANGER;
                            case "Late"      -> UITheme.WARNING;
                            case "Half-Day"  -> new Color(0xE65100);
                            case "On-Leave"  -> UITheme.INFO;
                            default          -> UITheme.TEXT_SECONDARY;
                        });
                        setFont(UITheme.FONT_BOLD);
                    }
                    return this;
                }
            });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(title,        BorderLayout.NORTH);
        north.add(controls,     BorderLayout.CENTER);
        north.add(summaryLabel, BorderLayout.SOUTH);

        add(north,  BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void generateReport() {
        Employee emp = (Employee) empCombo.getSelectedItem();
        if (emp == null) { info("Please select an employee."); return; }

        int month  = monthBox.getSelectedIndex() + 1;
        int year   = Integer.parseInt((String) yearBox.getSelectedItem());

        new SwingWorker<List<Attendance>, Void>() {
            @Override protected List<Attendance> doInBackground() throws Exception {
                return attSvc.getByEmployeeAndMonth(emp.getId(), year, month);
            }
            @Override protected void done() {
                try {
                    currentReport = get();
                    populateTable(emp, year, month, currentReport);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ReportPanel.this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void populateTable(Employee emp, int year, int month, List<Attendance> list) {
        tableModel.setRowCount(0);

        Map<LocalDate, Attendance> byDate = new LinkedHashMap<>();
        for (Attendance a : list) byDate.put(a.getAttendanceDate(), a);

        int present = 0, absent = 0, late = 0, halfDay = 0, onLeave = 0;
        double totalHours = 0;

        LocalDate date = LocalDate.of(year, month, 1);
        LocalDate end  = date.withDayOfMonth(date.lengthOfMonth());

        while (!date.isAfter(end)) {
            Attendance a = byDate.get(date);
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            if (a != null) {
                String s = a.getStatus().toDbString();
                switch (a.getStatus()) {
                    case Present -> present++;
                    case Absent  -> absent++;
                    case Late    -> { late++; present++; }
                    case HalfDay -> halfDay++;
                    case OnLeave -> onLeave++;
                    default -> {}
                }
                totalHours += a.getWorkingHours();
                tableModel.addRow(new Object[]{
                    DateUtil.formatDate(date), dayOfWeek,
                    DateUtil.formatTime(a.getCheckIn()),
                    DateUtil.formatTime(a.getCheckOut()),
                    DateUtil.formatHours(a.getWorkingHours()),
                    DateUtil.formatHours(a.getOvertimeHours()),
                    s,
                    a.getRemarks() != null ? a.getRemarks() : ""
                });
            } else {
                // Weekend or not marked
                boolean isWeekend = date.getDayOfWeek().getValue() >= 6;
                tableModel.addRow(new Object[]{
                    DateUtil.formatDate(date), dayOfWeek,
                    "—", "—", "—", "—",
                    isWeekend ? "Weekend" : "—", ""
                });
            }
            date = date.plusDays(1);
        }

        summaryLabel.setText(String.format(
            "Employee: %s (%s)  |  Month: %s %d  |  " +
            "Present: %d  |  Absent: %d  |  Late: %d  |  Half-Day: %d  |  On-Leave: %d  |  Total Hours: %.1f",
            emp.getFullName(), emp.getEmpCode(),
            Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH), year,
            present, absent, late, halfDay, onLeave, totalHours));
    }

    private void exportCsv() {
        if (currentReport == null || currentReport.isEmpty()) {
            info("Please generate a report first.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("attendance_report.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Header
            pw.println(String.join(",", COLS));
            // Rows
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                StringBuilder sb = new StringBuilder();
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    if (col > 0) sb.append(",");
                    Object val = tableModel.getValueAt(row, col);
                    sb.append(val != null ? "\"" + val.toString().replace("\"","\"\"") + "\"" : "");
                }
                pw.println(sb);
            }
            // Summary
            pw.println();
            pw.println("Summary:," + summaryLabel.getText().replace(",", ";"));
            info("Report exported to: " + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReport() {
        try {
            table.print();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
