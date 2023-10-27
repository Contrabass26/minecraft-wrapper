package me.jsedwards.gui;

import com.formdev.flatlaf.util.SystemInfo;
import me.jsedwards.Main;
import me.jsedwards.gui.about.AboutWindow;
import me.jsedwards.gui.menubar.ConfiguredMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.desktop.AboutEvent;
import java.io.IOException;

public class Window extends JFrame {

    public final CardPanel cardPanel;
    private AboutWindow aboutWindow = null;

    public Window() {
        super(Main.NAME);
        // Maximise window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width, screenSize.height);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        // Set content pane - main panel with lots of cards
        this.cardPanel = new CardPanel();
        this.setContentPane(this.cardPanel);
        // Menu bar - native if macOS, otherwise at the top
        try {
            ConfiguredMenuBar menuBar = new ConfiguredMenuBar("main");
            this.setJMenuBar(menuBar);
        } catch (IOException e) {
            System.err.println("Failed to load menu bar config: " + e.getMessage());
            e.printStackTrace();
        }
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
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
