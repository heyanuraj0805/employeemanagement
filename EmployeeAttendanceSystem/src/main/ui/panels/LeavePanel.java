package main.ui.panels;

import main.dao.EmployeeDAO;
import main.dao.LeaveRequestDAO;
import main.model.Employee;
import main.model.LeaveRequest;
import main.service.SessionManager;
import main.util.DateUtil;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Leave management panel – view, approve/reject leave requests.
 */
public class LeavePanel extends JPanel {

    private final LeaveRequestDAO leaveDAO = new LeaveRequestDAO();
    private final EmployeeDAO     empDAO   = new EmployeeDAO();

    private JTable              table;
    private DefaultTableModel   tableModel;
    private List<LeaveRequest>  currentList;
    private JComboBox<String>   filterBox;

    private static final String[] COLS = {
        "#", "Employee", "Code", "Leave Type", "From", "To", "Days",
        "Status", "Applied On", "Reason"
    };

    public LeavePanel() {
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
        loadData("All");
    }

    private void buildUI() {
        JLabel title = new JLabel("Leave Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.PRIMARY_DARK);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterBar.setOpaque(false);

        filterBox = new JComboBox<>(new String[]{"All","Pending","Approved","Rejected","Cancelled"});
        filterBox.addActionListener(e -> loadData((String) filterBox.getSelectedItem()));

        JButton approveBtn = UITheme.successButton("✔ Approve");
        JButton rejectBtn  = UITheme.dangerButton("✘ Reject");
        JButton addBtn     = UITheme.primaryButton("➕ New Request");
        JButton refreshBtn = UITheme.infoButton("⟳ Refresh");

        approveBtn.addActionListener(e -> reviewSelected(LeaveRequest.Status.Approved));
        rejectBtn .addActionListener(e -> reviewSelected(LeaveRequest.Status.Rejected));
        addBtn    .addActionListener(e -> openNewRequest());
        refreshBtn.addActionListener(e -> loadData((String) filterBox.getSelectedItem()));

        filterBar.add(new JLabel("Filter:"));
        filterBar.add(filterBox);
        filterBar.add(refreshBtn);
        if (SessionManager.getInstance().isManager()) {
            filterBar.add(approveBtn);
            filterBar.add(rejectBtn);
        }
        filterBar.add(addBtn);

        // Table
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // Status column colour
        table.getColumnModel().getColumn(7).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                    if (!sel) {
                        String s = value != null ? value.toString() : "";
                        setForeground(switch (s) {
                            case "Approved"  -> UITheme.SUCCESS;
                            case "Rejected"  -> UITheme.DANGER;
                            case "Pending"   -> UITheme.WARNING;
                            default          -> UITheme.TEXT_SECONDARY;
                        });
                        setFont(UITheme.FONT_BOLD);
                    }
                    return this;
                }
            });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(title,     BorderLayout.NORTH);
        north.add(filterBar, BorderLayout.SOUTH);

        add(north,  BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData(String filter) {
        new SwingWorker<List<LeaveRequest>, Void>() {
            @Override protected List<LeaveRequest> doInBackground() throws Exception {
                return switch (filter) {
                    case "Pending" -> leaveDAO.findPending();
                    default        -> leaveDAO.findAll();
                };
            }
            @Override protected void done() {
                try {
                    currentList = get();
                    // If filter is not "All", filter locally
                    if (!filter.equals("All") && !filter.equals("Pending")) {
                        currentList = currentList.stream()
                            .filter(lr -> lr.getStatus().name().equals(filter))
                            .toList();
                    }
                    populateTable(currentList);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LeavePanel.this, ex.getMessage());
                }
            }
        }.execute();
    }

    private void populateTable(List<LeaveRequest> list) {
        tableModel.setRowCount(0);
        int i = 1;
        for (LeaveRequest lr : list) {
            long days = DateUtil.daysBetween(lr.getFromDate(), lr.getToDate());
            tableModel.addRow(new Object[]{
                i++,
                lr.getEmployeeName(),
                lr.getEmpCode(),
                lr.getLeaveTypeName(),
                DateUtil.formatDate(lr.getFromDate()),
                DateUtil.formatDate(lr.getToDate()),
                days,
                lr.getStatus().name(),
                DateUtil.formatDateTime(lr.getAppliedOn()),
                lr.getReason() != null ? lr.getReason() : ""
            });
        }
    }

    private void reviewSelected(LeaveRequest.Status newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) { info("Please select a request."); return; }
        LeaveRequest lr = currentList.get(row);
        if (lr.getStatus() != LeaveRequest.Status.Pending) {
            info("Only pending requests can be reviewed."); return;
        }

        String remarks = JOptionPane.showInputDialog(this,
            "Enter review remarks (optional):",
            newStatus == LeaveRequest.Status.Approved ? "Approve Leave" : "Reject Leave");
        if (remarks == null) return;

        int reviewedBy = SessionManager.getInstance().getCurrentUser().getId();
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return leaveDAO.updateStatus(lr.getId(), newStatus, reviewedBy, remarks);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        info("Leave " + newStatus.name().toLowerCase() + " successfully.");
                        loadData((String) filterBox.getSelectedItem());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LeavePanel.this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void openNewRequest() {
        // Simple dialog to apply leave
        JComboBox<Employee> empSel = new JComboBox<>();
        try { for (Employee e : empDAO.findAll()) empSel.addItem(e); }
        catch (Exception ignored) {}

        JTextField fromField = UITheme.textField(10);
        fromField.setText(DateUtil.formatDate(java.time.LocalDate.now()));
        JTextField toField   = UITheme.textField(10);
        toField  .setText(DateUtil.formatDate(java.time.LocalDate.now()));
        JTextArea reasonArea = new JTextArea(3, 20);
        reasonArea.setLineWrap(true);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Employee:")); form.add(empSel);
        form.add(new JLabel("From (dd-MM-yyyy):")); form.add(fromField);
        form.add(new JLabel("To (dd-MM-yyyy):")); form.add(toField);
        form.add(new JLabel("Reason:")); form.add(new JScrollPane(reasonArea));

        int result = JOptionPane.showConfirmDialog(this, form,
            "Apply Leave Request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        Employee emp = (Employee) empSel.getSelectedItem();
        if (emp == null) return;

        java.time.LocalDate from = DateUtil.parseDate(fromField.getText().trim());
        java.time.LocalDate to   = DateUtil.parseDate(toField  .getText().trim());
        if (from == null || to == null) { info("Invalid date format."); return; }

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId (emp.getId());
        lr.setLeaveTypeId(1); // Default: Casual Leave
        lr.setFromDate   (from);
        lr.setToDate     (to);
        lr.setReason     (reasonArea.getText().trim());
        lr.setStatus     (LeaveRequest.Status.Pending);

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return leaveDAO.insert(lr);
            }
            @Override protected void done() {
                try { if (get()) { info("Leave request submitted."); loadData("All"); } }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(LeavePanel.this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
