package me.jsedwards;

import me.jsedwards.gui.Server;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpigotConfigManager extends DefaultListModel<String> implements ConfigManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private final File yamlFile;
    private Map<String, Object> map = new HashMap<>();
    private final List<String> keys = new ArrayList<>();
    private List<String> filteredKeys;
    private final Map<String, String> descriptions = new HashMap<>();
    private final Map<String, String> dataTypes = new HashMap<>();
    private boolean saved = true;

    public SpigotConfigManager(Server server) {
        List<String> lines = new ArrayList<>();
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
                // Get lines
                try (BufferedReader reader = new BufferedReader(new FileReader(yamlFile))) {
                    lines = reader.lines().toList();
                } catch (IOException e) {
                    LOGGER.error("Failed to read lines of spigot config from " + yamlFile.getAbsolutePath(), e);
                }
            }
        }
        explore(map, "");
        filteredKeys = new ArrayList<>(keys);
        // Get surrounding comments
        StringBuilder comment = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("#")) {
                if (line.length() >= 3) {
                    comment.append(line.substring(2)).append(' ');
                } else {
                    comment.append("\n");
                }
            } else if (!line.isBlank()) {
                // Associate comment with current key
                String key = StringUtils.substringBefore(line, ':').strip();
                descriptions.put(key, comment.toString());
                comment = new StringBuilder();
            }
        }
        LOGGER.info("Loaded spigot config");
    }

    private void explore(Map<?, ?> in, String current) {
        for (Object key : in.keySet()) {
            String newCurrent = current + "/" + key;
            Object value = in.get(key);
            if (value instanceof Map) {
                explore((Map<?, ?>) value, newCurrent);
            } else {
                keys.add(newCurrent.substring(1));
                dataTypes.put(newCurrent.substring(1), value.getClass().getSimpleName());
            }
        }
    }

    private Object getFromPath(String path) {
        Object next = map;
        String[] split = path.split("/");
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
        if (!saved) realSave();
    }

    private void realSave() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(yamlFile))) {
            Yaml yaml = new Yaml();
            String dump = yaml.dumpAsMap(map);
            writer.write(dump);
            saved = true;
        } catch (IOException e) {
            LOGGER.error("Failed to save spigot config to " + yamlFile.getAbsolutePath(), e);
        }
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
        saved = false;
    }

    @Override
    public String getDescription(String key) {
        String[] split = key.split("/");
        for (int i = split.length - 1; i >= 0; i--) {
            String description = descriptions.getOrDefault(split[i], "Not found");
            if (!description.isEmpty()) return description;
        }
        return "Not found";
    }

    @Override
    public String getDataType(String key) {
        return dataTypes.getOrDefault(key, "Not found");
    }

    @Override
    public String getDefaultValue(String key) {
        return "Not found";
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
        return key + ": " + getFromPath(key);
    }
}
