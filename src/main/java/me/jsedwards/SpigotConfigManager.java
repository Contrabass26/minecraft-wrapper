package me.jsedwards;

import me.jsedwards.gui.Server;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class SpigotConfigManager extends DefaultListModel<String> implements ConfigManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final HashMap<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DATA_TYPES = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DEFAULTS = new HashMap<>();
    static {
        Pattern pattern = Pattern.compile("Default: ((?:.(?!Type:))*) Type: ((?:.(?!Description:))*) Description: ((?:.(?!Default:))*)");
        try {
            Document document = Jsoup.connect("https://www.spigotmc.org/wiki/spigot-configuration/").userAgent("Mozilla").get();
            List<Node> children = document.select(".page-content").get(0).childNodes();
            for (int i = 0; i < children.size(); i++) {
                Node node = children.get(i);
                if (node instanceof Element child && child.is("span")) {
                    String key = child.text();
                    if (PROPERTY_DESCRIPTIONS.containsKey(key)) {
                        PROPERTY_DESCRIPTIONS.put(key, "Not found");
                    } else {
                        StringBuilder text = new StringBuilder();
                        for (int j = i + 1; j < children.size(); j++) {
                            Node candidate = children.get(j);
                            if (candidate instanceof Element element) {
                                if (element.is("style")) continue;
                                if (element.is("span")) {
                                    Optional<MatchResult> matcher = pattern.matcher(text).results().findFirst();
                                    if (matcher.isPresent()) {
                                        MatchResult matchResult = matcher.get();
                                        PROPERTY_DEFAULTS.put(key, matchResult.group(1));
                                        PROPERTY_DATA_TYPES.put(key, matchResult.group(2));
                                        PROPERTY_DESCRIPTIONS.put(key, matchResult.group(3));
                                    }
                                    LOGGER.debug(text.toString());
                                    break;
                                }
                                text.append(element.text());
                            } else if (candidate instanceof TextNode textNode) {
                                text.append(textNode.text());
                            }
                        }
                    }
                }
            }
            LOGGER.info(PROPERTY_DESCRIPTIONS.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final File yamlFile;
    private Map<String, Object> map = new HashMap<>();
    private final List<String> keys = new ArrayList<>();
    private List<String> filteredKeys;
    private boolean saved = true;

    public SpigotConfigManager(Server server) {
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
        explore(map, "");
        filteredKeys = new ArrayList<>(keys);
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
        return PROPERTY_DESCRIPTIONS.getOrDefault(StringUtils.substringAfterLast(key, '/'), "Not found");
    }

    @Override
    public String getDataType(String key) {
        return PROPERTY_DATA_TYPES.getOrDefault(StringUtils.substringAfterLast(key, '/'), "Not found");
    }

    @Override
    public String getDefaultValue(String key) {
        return PROPERTY_DEFAULTS.getOrDefault(StringUtils.substringAfterLast(key, '/'), "Not found");
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
