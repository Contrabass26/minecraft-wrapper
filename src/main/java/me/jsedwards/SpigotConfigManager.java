package me.jsedwards;

import me.jsedwards.gui.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpigotConfigManager extends DefaultListModel<String> implements ConfigManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private final File yamlFile;
    private Map<String, Object> map;
    private final List<String> keys;
    private List<String> filteredKeys;

    public SpigotConfigManager(Server server) {
        map = new HashMap<>();
        if (server == null) {
            yamlFile = null;
        } else {
            yamlFile = new File(server.serverLocation + File.separator + "spigot.yml");
            Yaml yaml = new Yaml();
            if (Files.exists(yamlFile.toPath())) {
                try (InputStream stream = new FileInputStream(yamlFile)) {
                    map = yaml.load(stream);
                    LOGGER.info("Loaded spigot config from " + yamlFile.getAbsolutePath());
                } catch (IOException e) {
                    LOGGER.error("Failed to load spigot config from " + yamlFile.getAbsolutePath(), e);
                }
            }
        }
        keys = new ArrayList<>();
        explore(map, keys, "");
        filteredKeys = new ArrayList<>(keys);
    }

    private static void explore(Map<?, ?> in, List<String> keysOut, String current) {
        for (Object key : in.keySet()) {
            String newCurrent = current + "/" + key;
            Object value = in.get(key);
            if (value instanceof Map) {
                explore((Map<?, ?>) value, keysOut, newCurrent);
            } else {
                keysOut.add(newCurrent);
            }
        }
    }

    private Object getFromPath(String path) {
        Object next = map;
        String[] split = path.substring(1).split("/");
        for (String s : split) {
            next = ((Map<?, ?>) next).get(s);
        }
        return next;
    }

    @Override
    public void updateSearch(String query) {
        filteredKeys = keys.stream().filter(s -> s.contains(query)).toList();
    }

    @Override
    public void save() {

    }

    @Override
    public void set(String key, String value) {
        Object next = map;
        Object last = null;
        String[] split = key.split("/");
        for (String s : split) {
            last = next;
            next = ((Map<?, ?>) next).get(s);
        }
        ((Map<Object, Object>) last).put(split[split.length - 1], value);
    }

    @Override
    public String getDescription(String key) {
        return "null";
    }

    @Override
    public String getDataType(String key) {
        return "null";
    }

    @Override
    public String getDefaultValue(String key) {
        return "null";
    }

    @Override
    public void optimise(int sliderValue) {

    }

    @Override
    public int getSize() {
        return filteredKeys.size();
    }

    @Override
    public String getElementAt(int index) {
        String key = filteredKeys.get(index);
        return key.substring(1) + ": " + getFromPath(key);
    }
}
