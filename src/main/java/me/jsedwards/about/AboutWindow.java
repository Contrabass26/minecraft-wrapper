package me.jsedwards.about;

import com.formdev.flatlaf.util.SystemInfo;
import me.jsedwards.Main;

import javax.swing.*;
import java.awt.*;

public class AboutWindow extends JFrame {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    public AboutWindow() {
        super(Main.NAME);
        // Size and position: centred on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBounds(screenSize.width / 2 - WIDTH / 2, screenSize.height / 2 - HEIGHT / 2, WIDTH, HEIGHT);
        // Contents
        this.setContentPane(new AboutPanel());
        this.setLocationRelativeTo(null);
        // Transparent title bar and full window content (macOS)
        if (SystemInfo.isMacFullWindowContentSupported) {
            this.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
            this.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
            this.getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
        }
        // Other settings
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
