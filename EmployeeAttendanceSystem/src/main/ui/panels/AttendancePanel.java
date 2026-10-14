package main.ui.panels;

import main.dao.EmployeeDAO;
import main.model.Attendance;
import main.model.Employee;
import main.service.AttendanceService;
import main.service.SessionManager;
import main.util.DateUtil;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Attendance panel – view, filter, and manually mark/edit attendance records.
 */
public class AttendancePanel extends JPanel {

    private final AttendanceService attSvc = new AttendanceService();
    private final EmployeeDAO       empDAO = new EmployeeDAO();

    private JTable            table;
    private DefaultTableModel tableModel;
    private JComboBox<Employee> empCombo;
    private JTextField        fromDateField, toDateField;
    private List<Attendance>  currentList;

    private static final String[] COLS = {
        "#", "Employee", "Code", "Department", "Date",
        "Check-In", "Check-Out", "Working Hrs", "Status", "Remarks"
    };

    public AttendancePanel() {
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
    }

    private void buildUI() {
        // ── Header ─────────────────────────────────────────────────────────
        JLabel title = new JLabel("Attendance Records");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.PRIMARY_DARK);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        // ── Filter bar ─────────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterBar.setBackground(UITheme.BG_CARD);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER),
            new EmptyBorder(8, 12, 8, 12)));

        // Employee combo
        empCombo = new JComboBox<>();
        empCombo.setPreferredSize(new Dimension(220, 30));
        empCombo.addItem(dummyEmployee("All Employees", 0));
        new SwingWorker<List<Employee>, Void>() {
            @Override protected List<Employee> doInBackground() throws Exception {
                return empDAO.findAll();
            }
            @Override protected void done() {
                try {
                    for (Employee e : get()) empCombo.addItem(e);
                } catch (Exception ignored) {}
            }
        }.execute();

        fromDateField = UITheme.textField(10);
        fromDateField.setToolTipText("From date (dd-MM-yyyy)");
        fromDateField.setPreferredSize(new Dimension(110, 30));
        // Default: start of current month
        fromDateField.setText(DateUtil.formatDate(LocalDate.now().withDayOfMonth(1)));

        toDateField = UITheme.textField(10);
        toDateField.setToolTipText("To date (dd-MM-yyyy)");
        toDateField.setPreferredSize(new Dimension(110, 30));
        toDateField.setText(DateUtil.formatDate(LocalDate.now()));

        JButton filterBtn  = UITheme.primaryButton("🔍 Filter");
        JButton todayBtn   = UITheme.infoButton("Today");
        JButton monthBtn   = UITheme.secondaryButton("This Month");
        JButton markBtn    = UITheme.warningButton("✏️ Manual Mark");
        JButton deleteBtn  = UITheme.dangerButton("🗑 Delete");

        filterBtn.addActionListener(e -> loadFiltered());
        todayBtn .addActionListener(e -> {
            fromDateField.setText(DateUtil.formatDate(LocalDate.now()));
            toDateField  .setText(DateUtil.formatDate(LocalDate.now()));
            loadFiltered();
        });
        monthBtn .addActionListener(e -> {
            fromDateField.setText(DateUtil.formatDate(LocalDate.now().withDayOfMonth(1)));
            toDateField  .setText(DateUtil.formatDate(LocalDate.now()));
            loadFiltered();
        });
        markBtn  .addActionListener(e -> openManualMarkDialog());
        deleteBtn.addActionListener(e -> doDelete());

        filterBar.add(new JLabel("Employee:"));
        filterBar.add(empCombo);
        filterBar.add(new JLabel("From:"));
        filterBar.add(fromDateField);
        filterBar.add(new JLabel("To:"));
        filterBar.add(toDateField);
        filterBar.add(filterBtn);
        filterBar.add(todayBtn);
        filterBar.add(monthBtn);
        if (SessionManager.getInstance().isManager()) {
            filterBar.add(markBtn);
        }
        if (SessionManager.getInstance().isAdmin()) {
            filterBar.add(deleteBtn);
        }

        // ── Table ──────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);

        // Colour-code status column
        table.getColumnModel().getColumn(8).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                    if (!sel) {
                        String s = value != null ? value.toString() : "";
                        setForeground(switch (s) {
                            case "Present"  -> UITheme.SUCCESS;
                            case "Absent"   -> UITheme.DANGER;
                            case "Late"     -> UITheme.WARNING;
                            case "HalfDay"  -> new Color(0xE65100);
                            case "OnLeave"  -> UITheme.INFO;
                            default         -> UITheme.TEXT_SECONDARY;
                        });
                        setFont(UITheme.FONT_BOLD);
                    }
                    return this;
                }
            });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        // ── Summary bar ────────────────────────────────────────────────────
        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        summaryBar.setOpaque(false);
        // Will be set after each load

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(title,     BorderLayout.NORTH);
        north.add(filterBar, BorderLayout.CENTER);

        add(north,      BorderLayout.NORTH);
        add(scroll,     BorderLayout.CENTER);
        add(summaryBar, BorderLayout.SOUTH);

        // Load today by default
        loadFiltered();
    }

    private void loadFiltered() {
        LocalDate from = DateUtil.parseDate(fromDateField.getText().trim());
        LocalDate to   = DateUtil.parseDate(toDateField.getText().trim());
        if (from == null) from = LocalDate.now();
        if (to   == null) to   = LocalDate.now();

        Employee selectedEmp = (Employee) empCombo.getSelectedItem();
        int empId = (selectedEmp != null && selectedEmp.getId() > 0) ? selectedEmp.getId() : 0;

        final LocalDate fFrom = from, fTo = to;

        SwingWorker<List<Attendance>, Void> w = new SwingWorker<>() {
            @Override protected List<Attendance> doInBackground() throws Exception {
                if (empId > 0) return attSvc.getByEmployeeAndDateRange(empId, fFrom, fTo);
                else           return attSvc.getByDateRange(fFrom, fTo);
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(AttendancePanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    private void populateTable(List<Attendance> list) {
        this.currentList = list;
        tableModel.setRowCount(0);
        int i = 1;
        for (Attendance a : list) {
            tableModel.addRow(new Object[]{
                i++,
                a.getEmployeeName(),
                a.getEmpCode(),
                a.getDepartmentName() != null ? a.getDepartmentName() : "—",
                DateUtil.formatDate(a.getAttendanceDate()),
                DateUtil.formatTime(a.getCheckIn()),
                DateUtil.formatTime(a.getCheckOut()),
                DateUtil.formatHours(a.getWorkingHours()),
                a.getStatus().toDbString(),
                a.getRemarks() != null ? a.getRemarks() : ""
            });
        }
    }

    private void openManualMarkDialog() {
        ManualMarkDialog dlg = new ManualMarkDialog(
            SwingUtilities.getWindowAncestor(this), empDAO, attSvc);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadFiltered();
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.");
            return;
        }
        Attendance a = currentList.get(row);
        int opt = JOptionPane.showConfirmDialog(this,
            "Delete attendance record for " + a.getEmployeeName() +
            " on " + DateUtil.formatDate(a.getAttendanceDate()) + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;
        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                return attSvc.delete(a.getId());
            }
            @Override protected void done() {
                try {
                    if (get()) loadFiltered();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AttendancePanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    private Employee dummyEmployee(String name, int id) {
        Employee e = new Employee();
        e.setId(id);
        e.setFirstName(name);
        e.setLastName("");
        return new Employee() {
            { setId(id); setFirstName(name); setLastName(""); }
            @Override public String toString() { return name; }
        };
    }
}
