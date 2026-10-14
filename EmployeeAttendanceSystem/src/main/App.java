package main;

import main.util.UITheme;
import main.ui.LoginFrame;

import javax.swing.*;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║      Employee Attendance Management System           ║
 * ║                                                      ║
 * ║  Student   : Anuraj Bhawarlal Jangid                ║
 * ║  Roll No   : BT230016CS                             ║
 * ║  College   : J D College of Engineering & Mgmt,     ║
 * ║              Nagpur                                  ║
 * ║  Internship: Pratinik Infotech                       ║
 * ║              19 May 2026 – 18 July 2026              ║
 * ╚══════════════════════════════════════════════════════╝
 *
 *  Entry-point class.
 */
public class App {

    public static void main(String[] args) {
        // Apply theme before any Swing component is created
        UITheme.apply();

        // Launch login window on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
