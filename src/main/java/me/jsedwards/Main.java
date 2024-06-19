package me.jsedwards;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;
import me.jsedwards.dashboard.Server;
import me.jsedwards.mod.CurseForge;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.OSUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public final class Main {

    public static Window WINDOW;
    public static boolean DARK_THEME = false;

    public static final String NAME = "Minecraft Wrapper";
    public static final String CREATOR = "Joseph Edwards";

    public static final Font MAIN_FONT;
    public static final Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, 13);

    private static final Logger LOGGER = LogManager.getLogger("Main");

    static {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            environment.registerFont(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("font/main_font.ttf"))));
            LOGGER.info("Loaded font");
        } catch (IOException | FontFormatException e) {
            LOGGER.error("Failed to load font", e);
        }
        MAIN_FONT = new Font("main_font", Font.PLAIN, 20);
    }

    public static void main(String[] args) {
//        CurseForge.search("create", ModLoader.FORGE, "1.20.1", null);
//        CurseForge.getCategories();
//        System.exit(0);
        // Mac-specific properties - must happen before any AWT classes are loaded
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            LOGGER.info("MacOS detected - setting system properties");
            System.setProperty("apple.laf.useScreenMenuBar", "true"); // Use native menu bar
            System.setProperty("apple.awt.application.name", Main.NAME); // Application name in menu bar
            System.setProperty( "apple.awt.application.appearance", "system"); // Colour of title bar
        }
        // Detect OS theme and set application theme accordingly
        OsThemeDetector themeDetector = OsThemeDetector.getDetector();
        DARK_THEME = themeDetector.isDark();
        if (DARK_THEME) {
            FlatDarculaLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        LOGGER.info("Setting dark theme: " + DARK_THEME);
        // Memory check
        if (OSUtils.totalMemoryBytes < 4294967296L) { // Less than 4GB
            double gbMemory = OSUtils.totalMemoryBytes / 1073741824D;
            LOGGER.error("Not enough memory: >4GB required, only %.2f detected. Operating system: %s".formatted(gbMemory, System.getProperty("os.name")));
            JOptionPane.showMessageDialog(null, "You do not have enough system memory: >4GB required, only %.2fGB detected.".formatted(gbMemory), "Not enough memory", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        // Load servers
        Server.load();
        // Create main window
        SwingUtilities.invokeLater(() -> WINDOW = new Window());
    }
}
