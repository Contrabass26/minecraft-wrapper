package me.jsedwards;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class Main {

    public static final String NAME = "Minecraft Wrapper";
    public static final String CREATOR = "Joseph Edwards";

    public static final Font FONT;
    static {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            environment.registerFont(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("font/my_font.ttf"))));
        } catch (IOException | FontFormatException e) {
            System.err.println("Failed to load font");
            e.printStackTrace();
        }
        FONT = new Font("my_font", Font.PLAIN, 26);
    }

    public static void main(String[] args) {
        // Mac-specific properties - must happen before any AWT classes are loaded
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true"); // Use native menu bar
            System.setProperty("apple.awt.application.name", Main.NAME); // Application name in menu bar
            System.setProperty( "apple.awt.application.appearance", "system"); // Colour of title bar
        }
        // Detect OS theme and set application theme accordingly
        OsThemeDetector themeDetector = OsThemeDetector.getDetector();
        if (themeDetector.isDark()) {
            FlatDarculaLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        // Create main window
        SwingUtilities.invokeLater(Window::new);
    }
}
