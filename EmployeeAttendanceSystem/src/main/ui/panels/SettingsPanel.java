package main.ui.panels;

import main.config.DatabaseConfig;
import main.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Settings panel – shows database connection info and test connection button.
 */
public class SettingsPanel extends JPanel {

    public SettingsPanel() {
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.PRIMARY_DARK);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // ── DB Connection Card ─────────────────────────────────────────────
        JPanel dbCard = UITheme.card();
        dbCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel dbTitle = UITheme.sectionHeader("🗄️  Database Configuration");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        dbCard.add(dbTitle, gbc);
        gbc.gridwidth = 1;

        addInfoRow(dbCard, gbc, 1, "Host",     DatabaseConfig.getHost());
        addInfoRow(dbCard, gbc, 2, "Port",     DatabaseConfig.getPort());
        addInfoRow(dbCard, gbc, 3, "Database", DatabaseConfig.getDatabase());
        addInfoRow(dbCard, gbc, 4, "Username", DatabaseConfig.getUsername());
        addInfoRow(dbCard, gbc, 5, "Password", "••••••••");

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JLabel hint = new JLabel(
            "<html><i>To change connection settings, edit <b>db.properties</b> " +
            "in the root directory and restart the application.</i></html>");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_SECONDARY);
        dbCard.add(hint, gbc);

        gbc.gridy = 7;
        JButton testBtn = UITheme.primaryButton("🔗 Test Connection");
        JLabel resultLbl = new JLabel();
        resultLbl.setFont(UITheme.FONT_BOLD);
        testBtn.addActionListener(e -> {
            testBtn.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return DatabaseConfig.testConnection();
                }
                @Override protected void done() {
                    try {
                        boolean ok = get();
                        resultLbl.setText(ok ? "✔ Connection successful!" : "✘ Connection failed!");
                        resultLbl.setForeground(ok ? UITheme.SUCCESS : UITheme.DANGER);
                    } catch (Exception ex) {
                        resultLbl.setText("✘ Error: " + ex.getMessage());
                        resultLbl.setForeground(UITheme.DANGER);
                    }
                    testBtn.setEnabled(true);
                }
            }.execute();
        });
        JPanel testRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        testRow.setOpaque(false);
        testRow.add(testBtn);
        testRow.add(resultLbl);
        dbCard.add(testRow, gbc);

        // ── About Card ─────────────────────────────────────────────────────
        JPanel aboutCard = UITheme.card();
        aboutCard.setLayout(new BorderLayout(0, 8));
        JLabel aboutTitle = UITheme.sectionHeader("ℹ️  About This Application");

        JTextArea aboutText = new JTextArea("""
                Project  : Employee Attendance Management System
                Student  : Anuraj Bhawarlal Jangid
                Roll No  : BT230016CS
                College  : J D College of Engineering & Management, Nagpur
                Internship: Pratinik Infotech  (19 May 2026 – 18 July 2026)
                
                Technology Stack:
                  • Language  : Java 17
                  • GUI       : Java Swing
                  • Database  : MySQL 8.x
                  • Connector : MySQL Connector/J (JDBC)
                  • Platform  : Windows
                """);
        aboutText.setEditable(false);
        aboutText.setFont(UITheme.FONT_BODY);
        aboutText.setForeground(UITheme.TEXT_SECONDARY);
        aboutText.setBackground(UITheme.BG_CARD);
        aboutCard.add(aboutTitle, BorderLayout.NORTH);
        aboutCard.add(aboutText,  BorderLayout.CENTER);

        // ── Layout ─────────────────────────────────────────────────────────
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(dbCard);
        center.add(Box.createVerticalStrut(20));
        center.add(aboutCard);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(center, BorderLayout.NORTH);

        add(title, BorderLayout.NORTH);
        add(wrap,  BorderLayout.CENTER);
    }

    private void addInfoRow(JPanel card, GridBagConstraints gbc, int row,
                            String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UITheme.FONT_BOLD);
        card.add(lbl, gbc);
        gbc.gridx = 1;
        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_BODY);
        val.setForeground(UITheme.TEXT_SECONDARY);
        card.add(val, gbc);
    }
}
