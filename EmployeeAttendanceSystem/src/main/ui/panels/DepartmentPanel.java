package main.ui.panels;

import main.dao.DepartmentDAO;
import main.model.Department;
import main.service.SessionManager;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Department management panel.
 */
public class DepartmentPanel extends JPanel {

    private final DepartmentDAO deptDAO = new DepartmentDAO();
    private JTable              table;
    private DefaultTableModel   tableModel;
    private List<Department>    currentList;

    public DepartmentPanel() {
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel title = new JLabel("Department Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.PRIMARY_DARK);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(title, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);
        JButton addBtn     = UITheme.successButton("➕ Add Department");
        JButton editBtn    = UITheme.primaryButton("✏️ Edit");
        JButton deleteBtn  = UITheme.dangerButton("🗑 Delete");
        JButton refreshBtn = UITheme.infoButton("⟳ Refresh");

        addBtn    .addActionListener(e -> openForm(null));
        editBtn   .addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { info("Please select a department."); return; }
            openForm(currentList.get(row));
        });
        deleteBtn .addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { info("Please select a department."); return; }
            doDelete(currentList.get(row));
        });
        refreshBtn.addActionListener(e -> loadData());

        btnBar.add(addBtn);
        btnBar.add(editBtn);
        btnBar.add(deleteBtn);
        btnBar.add(refreshBtn);
        topBar.add(btnBar, BorderLayout.EAST);

        tableModel = new DefaultTableModel(new String[]{"#","ID","Name","Description"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        new SwingWorker<List<Department>, Void>() {
            @Override protected List<Department> doInBackground() throws Exception {
                return deptDAO.findAll();
            }
            @Override protected void done() {
                try {
                    currentList = get();
                    tableModel.setRowCount(0);
                    int i = 1;
                    for (Department d : currentList)
                        tableModel.addRow(new Object[]{i++, d.getId(), d.getName(), d.getDescription()});
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DepartmentPanel.this, ex.getMessage());
                }
            }
        }.execute();
    }

    private void openForm(Department existing) {
        String name = (String) JOptionPane.showInputDialog(this,
            "Department Name:", existing == null ? "Add Department" : "Edit Department",
            JOptionPane.PLAIN_MESSAGE, null, null,
            existing != null ? existing.getName() : "");
        if (name == null || name.isBlank()) return;

        String desc = (String) JOptionPane.showInputDialog(this,
            "Description (optional):", existing == null ? "Add Department" : "Edit Department",
            JOptionPane.PLAIN_MESSAGE, null, null,
            existing != null ? existing.getDescription() : "");

        Department dept = existing != null ? existing : new Department();
        dept.setName(name.trim());
        dept.setDescription(desc != null ? desc.trim() : "");

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return existing == null ? deptDAO.insert(dept) : deptDAO.update(dept);
            }
            @Override protected void done() {
                try { if (get()) loadData(); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(DepartmentPanel.this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doDelete(Department d) {
        try {
            int empCount = deptDAO.countEmployees(d.getId());
            if (empCount > 0) {
                JOptionPane.showMessageDialog(this,
                    "Cannot delete: " + empCount + " employee(s) assigned to this department.",
                    "Cannot Delete", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ignored) {}

        int opt = JOptionPane.showConfirmDialog(this,
            "Delete department: " + d.getName() + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception { return deptDAO.delete(d.getId()); }
            @Override protected void done() {
                try { if (get()) loadData(); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(DepartmentPanel.this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
