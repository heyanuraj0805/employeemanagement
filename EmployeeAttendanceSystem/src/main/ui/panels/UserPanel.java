package main.ui.panels;

import main.dao.UserDAO;
import main.model.User;
import main.service.SessionManager;
import main.util.PasswordUtil;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * User management panel (Admin only).
 * Lists users and allows add, edit, delete, and password change.
 */
public class UserPanel extends JPanel {

    private final UserDAO         userDAO = new UserDAO();
    private JTable                table;
    private DefaultTableModel     tableModel;
    private List<User>            currentList;

    public UserPanel() {
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel title = new JLabel("User Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.PRIMARY_DARK);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(title, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        JButton addBtn     = UITheme.successButton("➕ Add User");
        JButton editBtn    = UITheme.primaryButton("✏️ Edit");
        JButton deleteBtn  = UITheme.dangerButton("🗑 Delete");
        JButton pwdBtn     = UITheme.warningButton("🔑 Change Password");
        JButton refreshBtn = UITheme.infoButton("⟳ Refresh");

        addBtn    .addActionListener(e -> openUserForm(null));
        editBtn   .addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { info("Select a user first."); return; }
            openUserForm(currentList.get(row));
        });
        deleteBtn .addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { info("Select a user first."); return; }
            doDelete(currentList.get(row));
        });
        pwdBtn    .addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { info("Select a user first."); return; }
            changePassword(currentList.get(row));
        });
        refreshBtn.addActionListener(e -> loadData());

        btnBar.add(addBtn);
        btnBar.add(editBtn);
        btnBar.add(deleteBtn);
        btnBar.add(pwdBtn);
        btnBar.add(refreshBtn);
        topBar.add(btnBar, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
            new String[]{"#","ID","Username","Full Name","Role","Email","Active","Created"}, 0) {
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
        new SwingWorker<List<User>, Void>() {
            @Override protected List<User> doInBackground() throws Exception {
                return userDAO.findAll();
            }
            @Override protected void done() {
                try {
                    currentList = get();
                    tableModel.setRowCount(0);
                    int i = 1;
                    for (User u : currentList)
                        tableModel.addRow(new Object[]{
                            i++, u.getId(), u.getUsername(), u.getFullName(),
                            u.getRole().name(), u.getEmail() != null ? u.getEmail() : "—",
                            u.isActive() ? "Yes" : "No",
                            u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate() : ""
                        });
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserPanel.this, ex.getMessage());
                }
            }
        }.execute();
    }

    private void openUserForm(User existing) {
        JTextField usernameF = UITheme.textField(15);
        JTextField fullNameF = UITheme.textField(15);
        JTextField emailF    = UITheme.textField(15);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"ADMIN","MANAGER","USER"});
        JPasswordField pwdF  = new JPasswordField(15);
        JCheckBox activeChk  = new JCheckBox("Active", true);

        if (existing != null) {
            usernameF.setText(existing.getUsername());
            fullNameF.setText(existing.getFullName());
            emailF   .setText(existing.getEmail() != null ? existing.getEmail() : "");
            roleBox  .setSelectedItem(existing.getRole().name());
            activeChk.setSelected(existing.isActive());
            pwdF     .setToolTipText("Leave blank to keep existing password");
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Username *:")); form.add(usernameF);
        form.add(new JLabel("Full Name *:")); form.add(fullNameF);
        form.add(new JLabel("Email:"));      form.add(emailF);
        form.add(new JLabel("Role *:"));     form.add(roleBox);
        if (existing == null) {
            form.add(new JLabel("Password *:")); form.add(pwdF);
        }
        form.add(new JLabel(""));            form.add(activeChk);

        int res = JOptionPane.showConfirmDialog(this, form,
            existing == null ? "Add User" : "Edit User",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        if (usernameF.getText().isBlank() || fullNameF.getText().isBlank()) {
            info("Username and Full Name are required."); return;
        }
        if (existing == null && new String(pwdF.getPassword()).isBlank()) {
            info("Password is required for new users."); return;
        }

        User u = existing != null ? existing : new User();
        u.setUsername(usernameF.getText().trim());
        u.setFullName(fullNameF.getText().trim());
        u.setEmail   (emailF.getText().isBlank() ? null : emailF.getText().trim());
        u.setRole    (User.Role.valueOf((String) roleBox.getSelectedItem()));
        u.setActive  (activeChk.isSelected());
        if (existing == null)
            u.setPassword(PasswordUtil.hash(new String(pwdF.getPassword())));

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return existing == null ? userDAO.insert(u) : userDAO.update(u);
            }
            @Override protected void done() {
                try { if (get()) { info("User saved."); loadData(); } }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserPanel.this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doDelete(User u) {
        if (u.getId() == SessionManager.getInstance().getCurrentUser().getId()) {
            info("Cannot delete your own account."); return;
        }
        int opt = JOptionPane.showConfirmDialog(this,
            "Delete user: " + u.getUsername() + "?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception { return userDAO.delete(u.getId()); }
            @Override protected void done() {
                try { if (get()) loadData(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(UserPanel.this, ex.getMessage()); }
            }
        }.execute();
    }

    private void changePassword(User u) {
        JPasswordField newPwd  = new JPasswordField(15);
        JPasswordField confPwd = new JPasswordField(15);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("New Password *:"));    form.add(newPwd);
        form.add(new JLabel("Confirm Password *:")); form.add(confPwd);

        int res = JOptionPane.showConfirmDialog(this, form,
            "Change Password for " + u.getUsername(),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String p1 = new String(newPwd.getPassword());
        String p2 = new String(confPwd.getPassword());
        if (p1.isBlank()) { info("Password cannot be blank."); return; }
        if (!p1.equals(p2)) { info("Passwords do not match."); return; }

        String hash = PasswordUtil.hash(p1);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return userDAO.changePassword(u.getId(), hash);
            }
            @Override protected void done() {
                try { if (get()) info("Password changed successfully."); }
                catch (Exception ex) { JOptionPane.showMessageDialog(UserPanel.this, ex.getMessage()); }
            }
        }.execute();
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
