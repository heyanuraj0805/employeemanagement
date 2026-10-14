package main.ui.panels;

import main.dao.EmployeeDAO;
import main.model.Attendance;
import main.model.Employee;
import main.service.AttendanceService;
import main.util.DateUtil;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Check-In / Check-Out panel.
 *
 * Employees can be selected from a dropdown or searched by code.
 * Displays their current-day attendance status.
 */
public class CheckInOutPanel extends JPanel {

    private final AttendanceService attSvc = new AttendanceService();
    private final EmployeeDAO       empDAO = new EmployeeDAO();

    private JComboBox<Employee> empCombo;
    private JTextField          empCodeField;
    private JLabel              statusLabel, checkInLabel, checkOutLabel, hoursLabel;
    private JButton             checkInBtn, checkOutBtn;
    private JTextArea           remarksArea;
    private Employee            selectedEmployee;
    private Attendance          todayRecord;

    public CheckInOutPanel() {
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
        loadEmployees();
    }

    private void buildUI() {
        // ── Title ──────────────────────────────────────────────────────────
        JLabel title = new JLabel("Check In / Check Out");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.PRIMARY_DARK);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // ── Selection panel ────────────────────────────────────────────────
        JPanel selPanel = UITheme.card();
        selPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        selPanel.add(bold("Select Employee:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        empCombo = new JComboBox<>();
        empCombo.setPreferredSize(new Dimension(280, 34));
        empCombo.addActionListener(e -> onEmployeeSelected());
        selPanel.add(empCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        selPanel.add(bold("Or Enter Emp Code:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        empCodeField = UITheme.textField(12);
        empCodeField.setPreferredSize(new Dimension(150, 34));
        JButton searchByCode = UITheme.primaryButton("Search");
        searchByCode.addActionListener(e -> searchByCode());
        JPanel codeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        codeRow.setOpaque(false);
        codeRow.add(empCodeField);
        codeRow.add(searchByCode);
        selPanel.add(codeRow, gbc);

        // ── Status card ────────────────────────────────────────────────────
        JPanel statusCard = UITheme.card();
        statusCard.setLayout(new GridBagLayout());
        GridBagConstraints sg = new GridBagConstraints();
        sg.insets = new Insets(8, 12, 8, 12);
        sg.anchor = GridBagConstraints.WEST;

        sg.gridx = 0; sg.gridy = 0;
        statusCard.add(bold("Today's Status:"), sg);
        sg.gridx = 1;
        statusLabel = new JLabel("— Select an employee —");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);
        statusCard.add(statusLabel, sg);

        sg.gridx = 0; sg.gridy = 1;
        statusCard.add(bold("Check-In Time:"), sg);
        sg.gridx = 1;
        checkInLabel = new JLabel("—");
        checkInLabel.setFont(UITheme.FONT_BODY);
        statusCard.add(checkInLabel, sg);

        sg.gridx = 0; sg.gridy = 2;
        statusCard.add(bold("Check-Out Time:"), sg);
        sg.gridx = 1;
        checkOutLabel = new JLabel("—");
        checkOutLabel.setFont(UITheme.FONT_BODY);
        statusCard.add(checkOutLabel, sg);

        sg.gridx = 0; sg.gridy = 3;
        statusCard.add(bold("Working Hours:"), sg);
        sg.gridx = 1;
        hoursLabel = new JLabel("—");
        hoursLabel.setFont(UITheme.FONT_BODY);
        statusCard.add(hoursLabel, sg);

        // ── Action panel ────────────────────────────────────────────────────
        JPanel actionCard = UITheme.card();
        actionCard.setLayout(new BorderLayout(0, 12));

        JLabel remarksLbl = bold("Remarks (optional):");
        remarksArea = new JTextArea(3, 30);
        remarksArea.setFont(UITheme.FONT_BODY);
        remarksArea.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        remarksArea.setLineWrap(true);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnRow.setOpaque(false);

        checkInBtn  = UITheme.successButton("✅  CHECK IN");
        checkOutBtn = UITheme.dangerButton("🚪  CHECK OUT");
        checkInBtn .setFont(new Font("Segoe UI", Font.BOLD, 16));
        checkOutBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        checkInBtn .setPreferredSize(new Dimension(180, 50));
        checkOutBtn.setPreferredSize(new Dimension(180, 50));

        checkInBtn .addActionListener(e -> doCheckIn());
        checkOutBtn.addActionListener(e -> doCheckOut());
        checkInBtn .setEnabled(false);
        checkOutBtn.setEnabled(false);

        btnRow.add(checkInBtn);
        btnRow.add(checkOutBtn);

        actionCard.add(remarksLbl,             BorderLayout.NORTH);
        actionCard.add(new JScrollPane(remarksArea), BorderLayout.CENTER);
        actionCard.add(btnRow,                 BorderLayout.SOUTH);

        // ── Layout ─────────────────────────────────────────────────────────
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        selPanel  .setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        statusCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        actionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        center.add(selPanel);
        center.add(Box.createVerticalStrut(16));
        center.add(statusCard);
        center.add(Box.createVerticalStrut(16));
        center.add(actionCard);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(center, BorderLayout.NORTH);

        add(title, BorderLayout.NORTH);
        add(wrap,  BorderLayout.CENTER);
    }

    private JLabel bold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private void loadEmployees() {
        new SwingWorker<List<Employee>, Void>() {
            @Override protected List<Employee> doInBackground() throws Exception {
                return empDAO.findActive();
            }
            @Override protected void done() {
                try { for (Employee e : get()) empCombo.addItem(e); }
                catch (Exception ignored) {}
            }
        }.execute();
    }

    private void onEmployeeSelected() {
        Employee e = (Employee) empCombo.getSelectedItem();
        if (e == null) return;
        selectedEmployee = e;
        loadTodayRecord();
    }

    private void searchByCode() {
        String code = empCodeField.getText().trim();
        if (code.isBlank()) return;
        new SwingWorker<Employee, Void>() {
            @Override protected Employee doInBackground() throws Exception {
                return empDAO.findByEmpCode(code);
            }
            @Override protected void done() {
                try {
                    Employee emp = get();
                    if (emp != null) {
                        selectedEmployee = emp;
                        // Sync combo
                        for (int i = 0; i < empCombo.getItemCount(); i++) {
                            if (empCombo.getItemAt(i).getId() == emp.getId()) {
                                empCombo.setSelectedIndex(i);
                                break;
                            }
                        }
                        loadTodayRecord();
                    } else {
                        JOptionPane.showMessageDialog(CheckInOutPanel.this,
                            "No employee found with code: " + code, "Not Found",
                            JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CheckInOutPanel.this, ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadTodayRecord() {
        if (selectedEmployee == null) return;
        new SwingWorker<Attendance, Void>() {
            @Override protected Attendance doInBackground() throws Exception {
                return attSvc.getByEmployeeAndDate(selectedEmployee.getId(), LocalDate.now());
            }
            @Override protected void done() {
                try {
                    todayRecord = get();
                    updateStatusUI();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CheckInOutPanel.this, ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateStatusUI() {
        if (todayRecord == null) {
            statusLabel  .setText("Not Marked");
            statusLabel  .setForeground(UITheme.TEXT_SECONDARY);
            checkInLabel .setText("—");
            checkOutLabel.setText("—");
            hoursLabel   .setText("—");
            checkInBtn   .setEnabled(true);
            checkOutBtn  .setEnabled(false);
        } else {
            String statusStr = todayRecord.getStatus().toDbString();
            statusLabel.setText(statusStr);
            statusLabel.setForeground(switch (todayRecord.getStatus()) {
                case Present -> UITheme.SUCCESS;
                case Absent  -> UITheme.DANGER;
                case Late    -> UITheme.WARNING;
                default      -> UITheme.INFO;
            });
            checkInLabel .setText(DateUtil.formatTime(todayRecord.getCheckIn()));
            checkOutLabel.setText(DateUtil.formatTime(todayRecord.getCheckOut()));
            hoursLabel   .setText(todayRecord.getWorkingHours() > 0
                ? DateUtil.formatHours(todayRecord.getWorkingHours()) : "—");

            boolean canCheckIn  = todayRecord.getCheckIn()  == null;
            boolean canCheckOut = todayRecord.getCheckIn()  != null
                               && todayRecord.getCheckOut() == null;
            checkInBtn .setEnabled(canCheckIn);
            checkOutBtn.setEnabled(canCheckOut);
        }
    }

    private void doCheckIn() {
        if (selectedEmployee == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee."); return;
        }
        String remarks = remarksArea.getText().trim();
        new SwingWorker<Attendance, Void>() {
            @Override protected Attendance doInBackground() throws Exception {
                return attSvc.checkIn(selectedEmployee.getId(), remarks);
            }
            @Override protected void done() {
                try {
                    todayRecord = get();
                    updateStatusUI();
                    JOptionPane.showMessageDialog(CheckInOutPanel.this,
                        selectedEmployee.getFullName() + " checked in at " +
                        DateUtil.formatTime(todayRecord.getCheckIn()) +
                        "\nStatus: " + todayRecord.getStatus().toDbString(),
                        "Check-In Successful", JOptionPane.INFORMATION_MESSAGE);
                    remarksArea.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CheckInOutPanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doCheckOut() {
        if (selectedEmployee == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee."); return;
        }
        String remarks = remarksArea.getText().trim();
        new SwingWorker<Attendance, Void>() {
            @Override protected Attendance doInBackground() throws Exception {
                return attSvc.checkOut(selectedEmployee.getId(), remarks);
            }
            @Override protected void done() {
                try {
                    todayRecord = get();
                    updateStatusUI();
                    JOptionPane.showMessageDialog(CheckInOutPanel.this,
                        selectedEmployee.getFullName() + " checked out at " +
                        DateUtil.formatTime(todayRecord.getCheckOut()) +
                        "\nWorking Hours: " + DateUtil.formatHours(todayRecord.getWorkingHours()),
                        "Check-Out Successful", JOptionPane.INFORMATION_MESSAGE);
                    remarksArea.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CheckInOutPanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
