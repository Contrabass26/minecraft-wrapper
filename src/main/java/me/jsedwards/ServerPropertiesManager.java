package me.jsedwards;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ServerPropertiesManager implements ListModel<String> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Properties properties;
    private final List<Object> keys;

    public ServerPropertiesManager(File propertiesFile) {
        // Load properties
        properties = new Properties();
        keys = new ArrayList<>();
        if (Files.exists(propertiesFile.toPath())) {
            try (InputStream stream = new FileInputStream(propertiesFile)) {
                properties.load(stream);
                keys.addAll(properties.keySet());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ServerPropertiesManager() {
        properties = new Properties();
        keys = new ArrayList<>();
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

    @Override
    public void addListDataListener(ListDataListener l) {
        LOGGER.error("Tried to add ListDataListener to ServerPropertiesManager");
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        LOGGER.error("Tried to remove ListDataListener from ServerPropertiesManager");
    }
}
