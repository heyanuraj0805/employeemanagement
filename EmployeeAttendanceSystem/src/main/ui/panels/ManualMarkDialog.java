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
import java.time.LocalDate;
import java.util.List;

/**
 * Dialog for manually marking or editing an attendance record.
 */
public class ManualMarkDialog extends JDialog {

    private final EmployeeDAO       empDAO;
    private final AttendanceService attSvc;
    private boolean saved = false;

    private JComboBox<Employee> empCombo;
    private JTextField          dateField, checkInField, checkOutField, remarksField;
    private JComboBox<String>   statusBox;

    public ManualMarkDialog(Window owner, EmployeeDAO empDAO, AttendanceService attSvc) {
        super(owner, "Manual Attendance Mark", ModalityType.APPLICATION_MODAL);
        this.empDAO = empDAO;
        this.attSvc = attSvc;
        setSize(460, 420);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(20, 24, 16, 24));
        root.setBackground(UITheme.BG_CARD);

        JLabel title = new JLabel("Manual Attendance Mark");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(UITheme.PRIMARY_DARK);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(UITheme.BG_CARD);

        empCombo = new JComboBox<>();
        empCombo.setPreferredSize(new Dimension(200, 30));
        new SwingWorker<List<Employee>, Void>() {
            @Override protected List<Employee> doInBackground() throws Exception { return empDAO.findAll(); }
            @Override protected void done() {
                try { for (Employee e : get()) empCombo.addItem(e); } catch (Exception ignored) {}
            }
        }.execute();

        dateField     = UITheme.textField(10);
        dateField.setText(DateUtil.formatDate(LocalDate.now()));
        dateField.setToolTipText("dd-MM-yyyy");

        checkInField  = UITheme.textField(8);
        checkInField.setToolTipText("HH:mm  (e.g. 09:00)");

        checkOutField = UITheme.textField(8);
        checkOutField.setToolTipText("HH:mm  (e.g. 18:00)");

        remarksField  = UITheme.textField(15);

        statusBox = new JComboBox<>(new String[]{
            "Present","Absent","Late","Half-Day","On-Leave","Holiday","Weekend"});

        addRow(form, "Employee *",    empCombo);
        addRow(form, "Date *",        dateField);
        addRow(form, "Status *",      statusBox);
        addRow(form, "Check-In (HH:mm)", checkInField);
        addRow(form, "Check-Out (HH:mm)",checkOutField);
        addRow(form, "Remarks",       remarksField);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UITheme.BG_CARD);

        JButton saveBtn   = UITheme.successButton("Save");
        JButton cancelBtn = UITheme.secondaryButton("Cancel");
        saveBtn  .addActionListener(e -> doSave());
        cancelBtn.addActionListener(e -> dispose());

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        root.add(title,  BorderLayout.NORTH);
        root.add(form,   BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void addRow(JPanel form, String label, JComponent c) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UITheme.FONT_BODY);
        form.add(lbl);
        form.add(c);
    }

    private void doSave() {
        Employee emp = (Employee) empCombo.getSelectedItem();
        if (emp == null || emp.getId() == 0) {
            JOptionPane.showMessageDialog(this, "Please select an employee.");
            return;
        }

        LocalDate date = DateUtil.parseDate(dateField.getText().trim());
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Invalid date. Use dd-MM-yyyy.");
            return;
        }

        String statusStr = (String) statusBox.getSelectedItem();
        Attendance.Status status;
        try { status = Attendance.Status.fromDbString(statusStr); }
        catch (Exception e) { status = Attendance.Status.Present; }

        Attendance a = new Attendance();
        a.setEmployeeId    (emp.getId());
        a.setAttendanceDate(date);
        a.setStatus        (status);
        a.setRemarks       (remarksField.getText().trim());

        // Parse check-in / check-out
        try {
            if (!checkInField.getText().isBlank()) {
                a.setCheckIn(date.atTime(
                    java.time.LocalTime.parse(checkInField.getText().trim(),
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm"))));
            }
            if (!checkOutField.getText().isBlank()) {
                a.setCheckOut(date.atTime(
                    java.time.LocalTime.parse(checkOutField.getText().trim(),
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm"))));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:mm (e.g. 09:00).");
            return;
        }

        final Attendance att = a;
        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                return attSvc.manualMark(att);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ManualMarkDialog.this,
                            "Attendance marked successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        saved = true;
                        dispose();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManualMarkDialog.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    public boolean isSaved() { return saved; }
}
