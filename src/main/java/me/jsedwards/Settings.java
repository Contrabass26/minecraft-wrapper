package me.jsedwards;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Settings {

    public static Settings INSTANCE = null;
    private static final Logger LOGGER = LogManager.getLogger();

    public String themeOverride = "os";

    public static void load() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            INSTANCE = objectMapper.readValue(getLocation(), Settings.class);
            LOGGER.info("Loaded settings from file");
        } catch (IOException e) {
            INSTANCE = new Settings();
            LOGGER.warn("Failed to load settings from file, using default", e);
        }
    }

    public static void save() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            getLocationDir().mkdirs();
            File location = getLocation();
            location.createNewFile();
            objectMapper.writeValue(location, INSTANCE);
            LOGGER.info("Saved settings to file");
        } catch (IOException e) {
            LOGGER.error("Failed to save settings", e);
        }
    }

    private static File getLocation() {
        // TODO: Generalise path
        return new File("/Users/josephedwards/Library/Application Support/minecraft-wrapper/settings.txt");
    }

    private static File getLocationDir() {
        // TODO: Generalise path
        return new File("/Users/josephedwards/Library/Application Support/minecraft-wrapper");
    }
}
