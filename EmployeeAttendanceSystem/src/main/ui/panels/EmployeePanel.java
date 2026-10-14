package main.ui.panels;

import main.dao.DepartmentDAO;
import main.dao.EmployeeDAO;
import main.model.Department;
import main.model.Employee;
import main.service.SessionManager;
import main.util.DateUtil;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Employee management panel – list, add, edit, delete employees.
 */
public class EmployeePanel extends JPanel {

    private final EmployeeDAO   empDAO  = new EmployeeDAO();
    private final DepartmentDAO deptDAO = new DepartmentDAO();

    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        searchField;
    private List<Employee>    currentList;

    private static final String[] COLS = {
        "#", "Code", "Name", "Gender", "Department", "Designation",
        "Joining Date", "Type", "Status"
    };

    public EmployeePanel() {
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ── Header ─────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Employee Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.PRIMARY_DARK);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setOpaque(false);

        searchField = UITheme.textField(20);
        searchField.setToolTipText("Search by name, code, email, department…");
        searchField.setPreferredSize(new Dimension(220, 34));
        JButton searchBtn = UITheme.primaryButton("🔍 Search");
        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());

        JButton addBtn = UITheme.successButton("➕ Add Employee");
        addBtn.addActionListener(e -> openForm(null));

        JButton refreshBtn = UITheme.infoButton("⟳ Refresh");
        refreshBtn.addActionListener(e -> loadData());

        rightBar.add(new JLabel("Search: "));
        rightBar.add(searchField);
        rightBar.add(searchBtn);
        rightBar.add(addBtn);
        rightBar.add(refreshBtn);

        header.add(title,    BorderLayout.WEST);
        header.add(rightBar, BorderLayout.EAST);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        // ── Table ──────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        // Double-click → edit
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int idx = table.getSelectedRow();
                    openForm(currentList.get(idx));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        // ── Action bar below table ──────────────────────────────────────────
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actionBar.setOpaque(false);

        JButton editBtn   = UITheme.primaryButton("✏️ Edit");
        JButton deleteBtn = UITheme.dangerButton("🗑 Delete");
        JButton viewBtn   = UITheme.infoButton("👁 View Details");

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Please select an employee."); return; }
            openForm(currentList.get(row));
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Please select an employee."); return; }
            doDelete(currentList.get(row));
        });

        viewBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Please select an employee."); return; }
            showDetails(currentList.get(row));
        });

        actionBar.add(editBtn);
        actionBar.add(deleteBtn);
        actionBar.add(viewBtn);

        // Only admins/managers can add/edit/delete
        boolean canModify = SessionManager.getInstance().isManager();
        addBtn   .setVisible(canModify);
        editBtn  .setVisible(canModify);
        deleteBtn.setVisible(SessionManager.getInstance().isAdmin());

        add(header,    BorderLayout.NORTH);
        add(scroll,    BorderLayout.CENTER);
        add(actionBar, BorderLayout.SOUTH);
    }

    private void loadData() {
        SwingWorker<List<Employee>, Void> w = new SwingWorker<>() {
            @Override protected List<Employee> doInBackground() throws Exception {
                return empDAO.findAll();
            }
            @Override protected void done() {
                try {
                    populateTable(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeePanel.this,
                        "Error loading employees: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    private void doSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        SwingWorker<List<Employee>, Void> w = new SwingWorker<>() {
            @Override protected List<Employee> doInBackground() throws Exception {
                return empDAO.search(kw);
            }
            @Override protected void done() {
                try { populateTable(get()); } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeePanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private void populateTable(List<Employee> list) {
        this.currentList = list;
        tableModel.setRowCount(0);
        int i = 1;
        for (Employee e : list) {
            tableModel.addRow(new Object[]{
                i++,
                e.getEmpCode(),
                e.getFullName(),
                e.getGender().name(),
                e.getDepartmentName() != null ? e.getDepartmentName() : "—",
                e.getDesignation()    != null ? e.getDesignation()    : "—",
                DateUtil.formatDate(e.getDateOfJoining()),
                formatEmpType(e.getEmploymentType()),
                e.getStatus().name()
            });
        }
    }

    private String formatEmpType(Employee.EmploymentType t) {
        return switch (t) {
            case FullTime -> "Full-Time";
            case PartTime -> "Part-Time";
            case Contract -> "Contract";
            case Intern   -> "Intern";
        };
    }

    // ── Forms ────────────────────────────────────────────────────────────────
    private void openForm(Employee existing) {
        EmployeeFormDialog dlg = new EmployeeFormDialog(
            SwingUtilities.getWindowAncestor(this), existing, deptDAO);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void doDelete(Employee emp) {
        int opt = JOptionPane.showConfirmDialog(this,
            "Delete employee: " + emp.getFullName() + " (" + emp.getEmpCode() + ")?\n" +
            "This will also delete all their attendance records.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;
        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                return empDAO.delete(emp.getId());
            }
            @Override protected void done() {
                try {
                    if (get()) { showInfo("Employee deleted."); loadData(); }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeePanel.this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    private void showDetails(Employee e) {
        String msg = String.format("""
                Employee Details
                ────────────────────────────────────
                Code       : %s
                Name       : %s
                Gender     : %s
                DOB        : %s
                Email      : %s
                Phone      : %s
                Department : %s
                Designation: %s
                Joining    : %s
                Type       : %s
                Status     : %s
                Address    : %s
                """,
            e.getEmpCode(), e.getFullName(), e.getGender(),
            DateUtil.formatDate(e.getDob()),
            e.getEmail() != null ? e.getEmail() : "—",
            e.getPhone() != null ? e.getPhone() : "—",
            e.getDepartmentName() != null ? e.getDepartmentName() : "—",
            e.getDesignation() != null ? e.getDesignation() : "—",
            DateUtil.formatDate(e.getDateOfJoining()),
            formatEmpType(e.getEmploymentType()),
            e.getStatus(),
            e.getAddress() != null ? e.getAddress() : "—");
        JOptionPane.showMessageDialog(this, msg, "Employee Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
