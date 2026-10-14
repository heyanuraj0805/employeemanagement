package main.util;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Centralised UI theme and factory methods for the Attendance System.
 *
 * Provides: colours, fonts, panel/button factories, and table styling.
 */
public class UITheme {

    // ── Colour Palette ───────────────────────────────────────────────────────
    public static final Color PRIMARY         = new Color(0x1565C0);   // deep blue
    public static final Color PRIMARY_DARK    = new Color(0x003C8F);
    public static final Color PRIMARY_LIGHT   = new Color(0x5E92F3);
    public static final Color ACCENT          = new Color(0x00ACC1);   // teal
    public static final Color SUCCESS         = new Color(0x2E7D32);
    public static final Color WARNING         = new Color(0xF57F17);
    public static final Color DANGER          = new Color(0xC62828);
    public static final Color INFO            = new Color(0x0277BD);
    public static final Color BG_MAIN         = new Color(0xF5F7FA);
    public static final Color BG_CARD         = Color.WHITE;
    public static final Color BG_SIDEBAR      = new Color(0x1A237E);
    public static final Color SIDEBAR_TEXT    = new Color(0xB0BEC5);
    public static final Color SIDEBAR_HOVER   = new Color(0x283593);
    public static final Color SIDEBAR_ACTIVE  = new Color(0x3949AB);
    public static final Color TEXT_PRIMARY    = new Color(0x212121);
    public static final Color TEXT_SECONDARY  = new Color(0x757575);
    public static final Color BORDER          = new Color(0xE0E0E0);
    public static final Color TABLE_HEADER_BG = new Color(0x1565C0);
    public static final Color TABLE_ROW_ALT   = new Color(0xF3F6FB);
    public static final Color TABLE_SELECT    = new Color(0xBBDEFB);

    // ── Fonts ────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font FONT_MONO   = new Font("Consolas",  Font.PLAIN, 13);

    private UITheme() {}

    // ── Global LAF setup ────────────────────────────────────────────────────
    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background",       BG_MAIN);
        UIManager.put("Label.font",             FONT_BODY);
        UIManager.put("TextField.font",         FONT_BODY);
        UIManager.put("TextArea.font",          FONT_BODY);
        UIManager.put("ComboBox.font",          FONT_BODY);
        UIManager.put("Table.font",             FONT_BODY);
        UIManager.put("TableHeader.font",       FONT_BOLD);
        UIManager.put("Button.font",            FONT_BUTTON);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
    }

    // ── Button Factories ─────────────────────────────────────────────────────
    public static JButton primaryButton(String text) {
        return styledButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton successButton(String text) {
        return styledButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton dangerButton(String text) {
        return styledButton(text, DANGER, Color.WHITE);
    }

    public static JButton warningButton(String text) {
        return styledButton(text, WARNING, Color.WHITE);
    }

    public static JButton infoButton(String text) {
        return styledButton(text, INFO, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        return styledButton(text, new Color(0x9E9E9E), Color.WHITE);
    }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                        ? bg.darker()
                        : getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        btn.setBorder(new EmptyBorder(5, 16, 5, 16));
        return btn;
    }

    // ── Text-field factory ───────────────────────────────────────────────────
    public static JTextField textField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return tf;
    }

    // ── Card panel ───────────────────────────────────────────────────────────
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)));
        return p;
    }

    // ── Stat card (number + label) ───────────────────────────────────────────
    public static JPanel statCard(String title, String value, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            new EmptyBorder(14, 16, 14, 16)));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(accent);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(TEXT_SECONDARY);

        p.add(valLbl,   BorderLayout.CENTER);
        p.add(titleLbl, BorderLayout.SOUTH);
        return p;
    }

    // ── Table styling ────────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setGridColor(BORDER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(TABLE_SELECT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setPreferredSize(new Dimension(0, 36));

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus,
                    int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? BG_CARD : TABLE_ROW_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    // ── Section header ───────────────────────────────────────────────────────
    public static JLabel sectionHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(PRIMARY_DARK);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return lbl;
    }

    // ── Separator ────────────────────────────────────────────────────────────
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        return sep;
    }
}
