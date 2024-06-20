package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class YamlConfigManager extends DefaultListModel<String>, ConfigManager {

    protected final File yamlFile;
    protected final List<String> keys = new ArrayList<>();
    protected Map<String, Object> map = new HashMap<>();
    protected List<String> filteredKeys;
    private boolean saved = true;

    public YamlConfigManager(Server server, Function<Server, String> pathGetter) {
        if (server == null) {
            yamlFile = null;
        } else {
            yamlFile = new File(pathGetter.apply(server));
            Yaml yaml = new Yaml();
            if (Files.exists(yamlFile.toPath())) {
                try (InputStream stream = new FileInputStream(yamlFile)) {
                    map = yaml.load(stream);
                    LOGGER.info("Loaded config from " + yamlFile.getAbsolutePath());
                } catch (IOException e) {
                    LOGGER.error("Failed to load config from " + yamlFile.getAbsolutePath(), e);
                }
            }
        }
        explore(map, "");
        filteredKeys = new ArrayList<>(keys);
    }

    protected void explore(Map<?, ?> in, String current) {
        for (Object key : in.keySet()) {
            String newCurrent = current + "/" + key;
            Object value = in.get(key);
            if (value instanceof Map) {
                explore((Map<?, ?>) value, newCurrent);
            } else {
                keys.add(newCurrent.substring(1));
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
            LOGGER.info("Saved config to " + yamlFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to save config to " + yamlFile.getAbsolutePath(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(String key, String value) {
        if (!keys.contains(key)) {
            LOGGER.warn("Tried to set property with non-existent key: " + key);
            return;
        }
        Object next = map;
        Object last = null;
        String[] split = key.split("/");
        for (String s : split) {
            last = next;
            next = ((Map<?, ?>) next).get(s);
        }
        Map<Object, Object> map = (Map<Object, Object>) last;
        String lastKey = split[split.length - 1];
        Object current = map.get(lastKey);
        Object castValue = cast(value, current.getClass());
        map.put(lastKey, castValue);
        saved = false;
    }

    private Object cast(String value, Class<?> clazz) {
        if (clazz == String.class) return value;
        if (clazz == Integer.class) return Integer.parseInt(value);
        if (clazz == Boolean.class) return Boolean.valueOf(value);
        if (clazz == Double.class) return Double.parseDouble(value);
        if (clazz == Float.class) return Float.parseFloat(value);
        LOGGER.warn("Unsupported class found in %s: %s".formatted(yamlFile.getAbsolutePath(), clazz.getName()));
        return value;
    }

    @Override
    public abstract String getDescription(String key);

    @Override
    public abstract String getDataType(String key);

    @Override
    public abstract String getDefaultValue(String key);

    @Override
    public abstract void optimise(int sliderValue);

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
