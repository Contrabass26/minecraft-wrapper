package me.jsedwards;

import me.jsedwards.util.OSUtils;
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
            INSTANCE = MinecraftWrapperUtils.readJson(OSUtils.getSettingsFile(), Settings.class);
            LOGGER.info("Loaded settings from " + OSUtils.settingsLocation);
        } catch (IOException e) {
            INSTANCE =  new Settings();
            LOGGER.warn("Failed to load settings from %s, using default".formatted(OSUtils.settingsLocation), e);
        }
    }

    public static void save() {
        try {
            OSUtils.createDataDir();
            File location = OSUtils.getSettingsFile();
            location.createNewFile();
            MinecraftWrapperUtils.writeJson(location, INSTANCE);
            LOGGER.info("Saved settings to " + OSUtils.settingsLocation);
        } catch (IOException e) {
            LOGGER.error("Failed to save settings to " + OSUtils.settingsLocation, e);
        }
    }
}
