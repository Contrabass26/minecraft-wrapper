package me.jsedwards;

import com.formdev.flatlaf.util.SystemInfo;
import me.jsedwards.about.AboutWindow;
import me.jsedwards.dashboard.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.desktop.AboutEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Window extends JFrame {

    public final CardPanel cardPanel;
    public final StatusPanel statusPanel;
    private AboutWindow aboutWindow = null;

    public Window() {
        super(Main.NAME);
        // Maximise window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width, screenSize.height);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        this.setLayout(new BorderLayout());
        // Main panel with lots of cards
        this.cardPanel = new CardPanel();
        this.add(this.cardPanel, BorderLayout.CENTER);
        // Status bar
        statusPanel = new StatusPanel();
        this.add(statusPanel, BorderLayout.SOUTH);
        // Add servers to GUI elements
        Server.addToGUI(this.cardPanel);
        // Transparent title bar and full window content (macOS)
        if (SystemInfo.isMacFullWindowContentSupported) {
            this.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
            this.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
            this.getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
        }
        // "About" integration for macOS
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler(this::showAboutWindow);
        }
        // Other settings
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Save data
                cardPanel.exitCurrent();
                if (Server.save()) {
                    // Exit
                    statusPanel.exit();
                    Window.this.dispose();
                    System.exit(0);
                }
            }
        });
        this.setVisible(true);
    }

    private void showAboutWindow(AboutEvent e) {
        SwingUtilities.invokeLater(() -> {
            if (this.aboutWindow == null) {
                this.aboutWindow = new AboutWindow();
            }
            this.aboutWindow.setVisible(true);
        });
    }
}
