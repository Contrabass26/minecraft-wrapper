package me.jsedwards;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Settings {

    public static Settings INSTANCE = null;
    private static final Logger LOGGER = LogManager.getLogger();

    public String themeOverride = "os";

    public static void load() {
        try {
            INSTANCE = MinecraftWrapperUtils.readJson(getLocation(), Settings.class);
            LOGGER.info("Loaded settings from file");
        } catch (IOException e) {
            INSTANCE =  new Settings();
            LOGGER.warn("Failed to load settings from file, using default", e);
        }
    }

    public static void save() {
        try {
            getLocationDir().mkdirs();
            File location = getLocation();
            location.createNewFile();
            MinecraftWrapperUtils.writeJson(location, INSTANCE);
            LOGGER.info("Saved settings to file");
        } catch (IOException e) {
            LOGGER.error("Failed to save settings", e);
        }
    }

    private static File getLocation() {
        // TODO: Generalise path
        return new File("/Users/josephedwards/Library/Application Support/minecraft-wrapper/settings.json");
    }

    private static File getLocationDir() {
        // TODO: Generalise path
        return new File("/Users/josephedwards/Library/Application Support/minecraft-wrapper");
    }
}
