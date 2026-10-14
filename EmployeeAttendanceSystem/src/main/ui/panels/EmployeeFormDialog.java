package main.ui.panels;

import main.dao.DepartmentDAO;
import main.dao.EmployeeDAO;
import main.model.Department;
import main.model.Employee;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modal dialog for adding or editing an Employee.
 */
public class EmployeeFormDialog extends JDialog {

    private final EmployeeDAO   empDAO;
    private final DepartmentDAO deptDAO;
    private final Employee      existing; // null → add mode

    private boolean saved = false;

    // Form fields
    private JTextField   empCodeField, firstNameField, lastNameField,
                         emailField, phoneField, addressField, designationField,
                         dobField, joiningDateField;
    private JComboBox<String>     genderBox, statusBox, empTypeBox;
    private JComboBox<Department> deptBox;

    public EmployeeFormDialog(Window owner, Employee existing, DepartmentDAO deptDAO) {
        super(owner, existing == null ? "Add Employee" : "Edit Employee", ModalityType.APPLICATION_MODAL);
        this.existing = existing;
        this.deptDAO  = deptDAO;
        this.empDAO   = new EmployeeDAO();
        setSize(560, 640);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUI();
        if (existing != null) prefill();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(20, 24, 16, 24));
        root.setBackground(UITheme.BG_CARD);

        // Title
        JLabel title = new JLabel(existing == null ? "Add New Employee" : "Edit Employee");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UITheme.PRIMARY_DARK);
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Form grid
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setBackground(UITheme.BG_CARD);

        // Employee Code
        empCodeField     = UITheme.textField(15);
        firstNameField   = UITheme.textField(15);
        lastNameField    = UITheme.textField(15);
        emailField       = UITheme.textField(15);
        phoneField       = UITheme.textField(15);
        dobField         = UITheme.textField(15);
        dobField.setToolTipText("dd-MM-yyyy");
        joiningDateField = UITheme.textField(15);
        joiningDateField.setToolTipText("dd-MM-yyyy");
        designationField = UITheme.textField(15);
        addressField     = UITheme.textField(15);

        genderBox  = new JComboBox<>(new String[]{"Male","Female","Other"});
        empTypeBox = new JComboBox<>(new String[]{"Full-Time","Part-Time","Contract","Intern"});
        statusBox  = new JComboBox<>(new String[]{"Active","Inactive","Resigned","Terminated"});

        // Load departments
        deptBox = new JComboBox<>();
        try {
            for (Department d : deptDAO.findAll()) deptBox.addItem(d);
        } catch (Exception e) { /* ignore – combo stays empty */ }

        // Auto-generate code for new employee
        if (existing == null) {
            try { empCodeField.setText(empDAO.generateNextEmpCode()); }
            catch (Exception ignored) { empCodeField.setText("EMP001"); }
        }

        addRow(form, "Employee Code *", empCodeField);
        addRow(form, "First Name *",    firstNameField);
        addRow(form, "Last Name *",     lastNameField);
        addRow(form, "Gender *",        genderBox);
        addRow(form, "Date of Birth",   dobField);
        addRow(form, "Email",           emailField);
        addRow(form, "Phone",           phoneField);
        addRow(form, "Department",      deptBox);
        addRow(form, "Designation",     designationField);
        addRow(form, "Date of Joining *", joiningDateField);
        addRow(form, "Employment Type *", empTypeBox);
        addRow(form, "Status *",          statusBox);
        addRow(form, "Address",           addressField);

        // Scroll form if needed
        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UITheme.BG_CARD);

        JButton saveBtn   = UITheme.successButton(existing == null ? "Save" : "Update");
        JButton cancelBtn = UITheme.secondaryButton("Cancel");

        saveBtn  .addActionListener(e -> doSave());
        cancelBtn.addActionListener(e -> dispose());

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        root.add(title,  BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void addRow(JPanel form, String label, JComponent field) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        form.add(lbl);
        form.add(field);
    }

    private void prefill() {
        empCodeField    .setText(existing.getEmpCode());
        firstNameField  .setText(existing.getFirstName());
        lastNameField   .setText(existing.getLastName());
        emailField      .setText(existing.getEmail()       != null ? existing.getEmail()       : "");
        phoneField      .setText(existing.getPhone()       != null ? existing.getPhone()       : "");
        addressField    .setText(existing.getAddress()     != null ? existing.getAddress()     : "");
        designationField.setText(existing.getDesignation() != null ? existing.getDesignation() : "");

        if (existing.getDob() != null)
            dobField.setText(existing.getDob().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        if (existing.getDateOfJoining() != null)
            joiningDateField.setText(existing.getDateOfJoining().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        genderBox.setSelectedItem(existing.getGender().name());

        String et = switch (existing.getEmploymentType()) {
            case FullTime -> "Full-Time";
            case PartTime -> "Part-Time";
            case Contract -> "Contract";
            case Intern   -> "Intern";
        };
        empTypeBox.setSelectedItem(et);
        statusBox.setSelectedItem(existing.getStatus().name());

        // Select correct department
        for (int i = 0; i < deptBox.getItemCount(); i++) {
            if (deptBox.getItemAt(i).getId() == existing.getDepartmentId()) {
                deptBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void doSave() {
        // Validate required fields
        if (empCodeField.getText().isBlank() || firstNameField.getText().isBlank()
                || lastNameField.getText().isBlank() || joiningDateField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this,
                "Please fill all required (*) fields.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Employee e = existing != null ? existing : new Employee();
        e.setEmpCode    (empCodeField.getText().trim());
        e.setFirstName  (firstNameField.getText().trim());
        e.setLastName   (lastNameField.getText().trim());
        e.setGender     (Employee.Gender.valueOf((String) genderBox.getSelectedItem()));
        e.setEmail      (emailField.getText().isBlank() ? null : emailField.getText().trim());
        e.setPhone      (phoneField.getText().isBlank() ? null : phoneField.getText().trim());
        e.setAddress    (addressField.getText().isBlank() ? null : addressField.getText().trim());
        e.setDesignation(designationField.getText().isBlank() ? null : designationField.getText().trim());

        // Parse dates
        try {
            if (!dobField.getText().isBlank())
                e.setDob(parseDate(dobField.getText().trim()));
            e.setDateOfJoining(parseDate(joiningDateField.getText().trim()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid date format. Use dd-MM-yyyy.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Department
        Department dept = (Department) deptBox.getSelectedItem();
        e.setDepartmentId(dept != null ? dept.getId() : 0);

        // Employment type
        String et = (String) empTypeBox.getSelectedItem();
        e.setEmploymentType(switch (et) {
            case "Full-Time" -> Employee.EmploymentType.FullTime;
            case "Part-Time" -> Employee.EmploymentType.PartTime;
            case "Contract"  -> Employee.EmploymentType.Contract;
            default          -> Employee.EmploymentType.Intern;
        });
        e.setStatus(Employee.Status.valueOf((String) statusBox.getSelectedItem()));

        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                return existing == null ? empDAO.insert(e) : empDAO.update(e);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(EmployeeFormDialog.this,
                            "Employee saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        saved = true;
                        dispose();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeFormDialog.this,
                        "Error saving employee: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    private LocalDate parseDate(String s) {
        return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public boolean isSaved() { return saved; }
}
