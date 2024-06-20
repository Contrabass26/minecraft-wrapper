package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import me.jsedwards.util.MathUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;

public class ServerPropertiesManager extends AdvancedPanel {

    private static final Function<Integer, Integer> VIEW_DISTANCE_OPTIMISATION = slider -> (int) Math.round(MathUtils.quadraticFunction(slider, 0.0022, 0.07, 3));
    private static final Function<Integer, Integer> SIMULATION_DISTANCE_OPTIMISATION = slider -> (int) Math.round(MathUtils.quadraticFunction(slider, 0.0014, 0.13, 5));
    private static final Function<Integer, Integer> ENTITY_DISTANCE_OPTIMISATION = slider -> (int) Math.round(MathUtils.exponentialFunction(slider, 10, 0.0460517));

    private static final HashMap<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DATA_TYPES = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DEFAULTS = new HashMap<>();

    static {
        try {
            Document document = Jsoup.connect("https://minecraft.wiki/w/Server.properties").userAgent("Mozilla").get();
            Element table = document.select("table[data-description=Server properties]").getFirst();
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

    private Properties properties = new Properties();
    private File propertiesFile = null;

    public ServerPropertiesManager() {
        super("server.properties", s -> true);
    }

    @Override
    protected void save() {
        if (propertiesFile == null) {
            throw new IllegalStateException("Trying to save with no propertiesFile set");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile))) {
            properties.store(writer, "");
            LOGGER.info("Saved properties to %s".formatted(propertiesFile.getAbsolutePath()));
        } catch (IOException e) {
            LOGGER.error("Failed to save properties to %s".formatted(propertiesFile.getAbsolutePath()), e);
        }
    }

    @Override
    protected void addKeys(List<ConfigProperty> list, Server server) {
        propertiesFile = server.getPropertiesLocation();
        // Load properties
        properties = new Properties();
        if (Files.exists(propertiesFile.toPath())) {
            try (InputStream stream = new FileInputStream(propertiesFile)) {
                properties.load(stream);
                for (Object o : properties.keySet()) {
                    String key = (String) o;
                    list.add(new ConfigProperty(key,
                            PROPERTY_DEFAULTS.get(key),
                            PROPERTY_DESCRIPTIONS.get(key),
                            PROPERTY_DATA_TYPES.get(key),
                            name));
                }
                LOGGER.info("Loaded properties from %s".formatted(propertiesFile.getAbsolutePath()));
            } catch (IOException e) {
                LOGGER.error("Failed to load properties from %s".formatted(propertiesFile.getAbsolutePath()), e);
            }
        }
    }

    @Override
    protected String optimise(int sliderValue, ConfigProperty property) {
        if (property.key.equals("view-distance")) {
            return String.valueOf(VIEW_DISTANCE_OPTIMISATION.apply(sliderValue));
        }
        if (property.key.equals("simulation-distance")) {
            return String.valueOf(SIMULATION_DISTANCE_OPTIMISATION.apply(sliderValue));
        }
        return property.value;
    }
}
