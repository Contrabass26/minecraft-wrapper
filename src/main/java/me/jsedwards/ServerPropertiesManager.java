package me.jsedwards;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class ServerPropertiesManager extends DefaultListModel<String> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final HashMap<String, String> PROPERTY_DESCRIPTIONS;
    static {
        try {
            PROPERTY_DESCRIPTIONS = MinecraftWrapperUtils.readJson(ServerPropertiesManager.class.getClassLoader().getResourceAsStream("descriptions/property_descriptions.json"), new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Properties properties;
    private final List<String> keys;
    private List<String> filteredKeys;
    private final File propertiesFile;
    private boolean saved = true;

    public ServerPropertiesManager(File propertiesFile) {
        this.propertiesFile = propertiesFile;
        // Load properties
        properties = new Properties();
        keys = new ArrayList<>();
        if (Files.exists(propertiesFile.toPath())) {
            try (InputStream stream = new FileInputStream(propertiesFile)) {
                properties.load(stream);
                properties.keySet().stream().map(o -> (String) o).forEach(keys::add);
                Collections.sort(keys);
                LOGGER.info("Loaded properties from " + propertiesFile.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.error("Failed to load properties from " + propertiesFile.getAbsolutePath(), e);
            }
        }
        filteredKeys = new ArrayList<>(keys);
    }

    public ServerPropertiesManager() {
        properties = new Properties();
        keys = new ArrayList<>();
        filteredKeys = new ArrayList<>();
        propertiesFile = null;
    }

    public void updateSearch(String text) {
        filteredKeys = keys.stream().filter(s -> s.contains(text)).toList();
    }

    public void save() {
        if (!saved) realSave();
    }

    public void set(String key, String value) {
        if (!value.equals(this.properties.getProperty(key))) {
            saved = false;
        }
        properties.setProperty(key, value);
    }

    private void realSave() {
        if (propertiesFile == null) {
            throw new IllegalStateException("Trying to save with no propertiesFile set");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile))) {
            properties.store(writer, "");
            LOGGER.info("Saved properties to " + propertiesFile.getAbsolutePath());
            saved = false;
        } catch (IOException e) {
            LOGGER.error("Failed to save properties to " + propertiesFile.getAbsolutePath(), e);
        }
    }

    public static String getDescription(String key) {
        return PROPERTY_DESCRIPTIONS.getOrDefault(key, "");
    }

    @Override
    public int getSize() {
        return filteredKeys.size();
    }

    @Override
    public String getElementAt(int index) {
        Object key = filteredKeys.get(index);
        Object value = properties.get(key);
        return key.toString() + ": " + value.toString();
    }
}
