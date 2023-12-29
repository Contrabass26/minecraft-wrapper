package me.jsedwards;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ServerPropertiesManager extends DefaultListModel<String> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Properties properties;
    private final List<String> keys;
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
    }

    public ServerPropertiesManager() {
        properties = new Properties();
        keys = new ArrayList<>();
        propertiesFile = null;
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

    @Override
    public int getSize() {
        return keys.size();
    }

    @Override
    public String getElementAt(int index) {
        Object key = keys.get(index);
        Object value = properties.get(key);
        return key.toString() + ": " + value.toString();
    }
}
