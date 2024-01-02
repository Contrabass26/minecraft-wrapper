package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import me.jsedwards.util.MathUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;

public class ServerPropertiesManager extends DefaultListModel<String> implements ConfigManager {

    private static final HashMap<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DATA_TYPES = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DEFAULTS = new HashMap<>();
    private static final Map<String, Function<Integer, Integer>> OPTIMISATION_FUNCTIONS = new HashMap<>();
    private static final Map<String, Boolean> KEYS_ENABLED = new HashMap<>();

    static {
        // Property descriptions
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
        // Optimisation functions
        OPTIMISATION_FUNCTIONS.put("view-distance", VIEW_DISTANCE_OPTIMISATION);
        OPTIMISATION_FUNCTIONS.put("entity-broadcast-range-percentage", slider -> (int) Math.round(MathUtils.exponentialFunction(slider, 10, 0.0460517)));
        OPTIMISATION_FUNCTIONS.put("simulation-distance", SIMULATION_DISTANCE_OPTIMISATION);
    }

    private final Properties properties;
    private final List<String> keys;
    private List<String> filteredKeys;
    private final File propertiesFile;
    private boolean saved = true;

    public ServerPropertiesManager(Server server) {
        if (server == null) {
            properties = new Properties();
            keys = new ArrayList<>();
            filteredKeys = new ArrayList<>();
            propertiesFile = null;
        } else {
            this.propertiesFile = server.getPropertiesLocation();
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
    }

    public void updateSearch(String query) {
        filteredKeys = keys.stream().filter(s -> s.contains(query)).toList();
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

    public String getDescription(String key) {
        return PROPERTY_DESCRIPTIONS.getOrDefault(key, "No description found");
    }

    public String getDataType(String key) {
        return PROPERTY_DATA_TYPES.getOrDefault(key, "Not found");
    }

    public String getDefaultValue(String key) {
        return PROPERTY_DEFAULTS.getOrDefault(key, "Not found");
    }

    @Override
    public Set<String> getKeysToOptimise() {
        return OPTIMISATION_FUNCTIONS.keySet();
    }

    @Override
    public void setKeyOptimised(String key, boolean enabled) {
        KEYS_ENABLED.put(key, enabled);
    }

    @Override
    public void optimise(int sliderValue) {
        OPTIMISATION_FUNCTIONS.forEach((key, function) -> {
            if (KEYS_ENABLED.getOrDefault(key, true)) {
                this.set(key, String.valueOf(function.apply(sliderValue)));
            }
        });
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
