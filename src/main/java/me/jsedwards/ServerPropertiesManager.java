package me.jsedwards;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class ServerPropertiesManager extends DefaultListModel<String> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final HashMap<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DATA_TYPES = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DEFAULTS = new HashMap<>();
    static {
        try {
            Document document = Jsoup.connect("https://minecraft.wiki/w/Server.properties").userAgent("Mozilla").get();
            Element table = document.select("table[data-description=Server properties]").get(0);
            Elements rows = table.select("tr");
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cells = row.select("td");
                String key = cells.get(0).text();
                String description = cells.get(3).html();
                String datatype = cells.get(1).text();
                String defaultValue = cells.get(2).text();
                PROPERTY_DESCRIPTIONS.put(key, description);
                PROPERTY_DATA_TYPES.put(key, datatype);
                PROPERTY_DEFAULTS.put(key, defaultValue);
            }
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
        return PROPERTY_DESCRIPTIONS.getOrDefault(key, "No description found");
    }

    public static String getDataType(String key) {
        return PROPERTY_DATA_TYPES.getOrDefault(key, "Not found");
    }

    public static String getDefaultValue(String key) {
        return PROPERTY_DEFAULTS.getOrDefault(key, "Not found");
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
