package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.MathUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum ConfigManager {

    VANILLA("server.properties", s -> true) {
        private static final Logger LOGGER = LogManager.getLogger("ConfigManager/Vanilla");
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

        @Override
        public String getDescription(String key) {
            return PROPERTY_DESCRIPTIONS.get(key);
        }

        @Override
        public String getDefaultValue(String key) {
            return PROPERTY_DEFAULTS.get(key);
        }

        @Override
        public String getDataType(String key) {
            return PROPERTY_DATA_TYPES.get(key);
        }

        @Override
        public void addKeys(List<ConfigProperty> list, Server server) {
            File propertiesFile = server.getPropertiesLocation();
            // Load properties
            Properties properties = new Properties();
            if (Files.exists(propertiesFile.toPath())) {
                try (InputStream stream = new FileInputStream(propertiesFile)) {
                    properties.load(stream);
                    for (Object o : properties.keySet()) {
                        String key = (String) o;
                        list.add(new ConfigProperty(key,
                                String.valueOf(properties.get(key)),
                                PROPERTY_DEFAULTS.get(key),
                                PROPERTY_DESCRIPTIONS.get(key),
                                PROPERTY_DATA_TYPES.get(key),
                                this));
                    }
                    LOGGER.info("Loaded properties from %s".formatted(propertiesFile.getAbsolutePath()));
                } catch (IOException e) {
                    LOGGER.error("Failed to load properties from %s".formatted(propertiesFile.getAbsolutePath()), e);
                }
            }
        }

        @Override
        public void save(List<ConfigProperty> properties, Server server) {
            File propertiesFile = new File(getPath(server));
            Properties toSave = new Properties();
            for (ConfigProperty property : properties) {
                toSave.put(property.key, property.value);
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile))) {
                toSave.store(writer, "");
                LOGGER.info("Saved properties to %s".formatted(propertiesFile.getAbsolutePath()));
            } catch (IOException e) {
                LOGGER.error("Failed to save properties to %s".formatted(propertiesFile.getAbsolutePath()), e);
            }
        }

        @Override
        public String optimise(int sliderValue, ConfigProperty property) {
            return switch (property.key) {
                case "view-distance" ->
                        String.valueOf((int) Math.round(MathUtils.quadraticFunction(sliderValue, 0.0022, 0.07, 3)));
                case "simulation-distance" ->
                        String.valueOf((int) Math.round(MathUtils.quadraticFunction(sliderValue, 0.0014, 0.13, 5)));
                case "entity-broadcast-range-percentage" ->
                        String.valueOf((int) Math.round(MathUtils.exponentialFunction(sliderValue, 10, 0.0460517)));
                default -> super.optimise(sliderValue, property);
            };
        }
    },
    BUKKIT("bukkit.yml", s -> s.modLoader == ModLoader.PUFFERFISH) {
        private static final HashMap<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();
        private static final HashMap<String, String> PROPERTY_DATA_TYPES = new HashMap<>();
        private static final HashMap<String, String> PROPERTY_DEFAULTS = new HashMap<>();

        static {
            Pattern h3Pattern = Pattern.compile("(.*) \\(([^()]*)\\)");
            try {
                Document document = Jsoup.connect("https://bukkit.fandom.com/wiki/Bukkit.yml").userAgent("Mozilla").get();
                Elements children = document.select(".mw-parser-output").getFirst().children();
                for (int i = 0; i < children.size(); i++) {
                    Element child = children.get(i);
                    if (child.is("h2")) {
                        String key = child.child(0).text();
                        String description = children.get(i + 1).text();
                        PROPERTY_DESCRIPTIONS.put(key, description);
                    } else if (child.is("h3")) {
                        String key = child.child(0).text();
                        String descriptionText = children.get(i + 1).text();
                        Matcher matcher = h3Pattern.matcher(descriptionText);
                        Optional<MatchResult> matchResult = matcher.results().findFirst();
                        if (matchResult.isPresent()) {
                            MatchResult result = matchResult.get();
                            PROPERTY_DESCRIPTIONS.put(key, result.group(1));
                            PROPERTY_DATA_TYPES.put(key, result.group(2));
                        } else {
                            PROPERTY_DESCRIPTIONS.put(key, descriptionText);
                        }
                        PROPERTY_DEFAULTS.put(key, StringUtils.substringAfter(children.get(i + 2).text(), "Default: "));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getDescription(String key) {
            return PROPERTY_DESCRIPTIONS.get(key);
        }

        @Override
        public String getDefaultValue(String key) {
            return PROPERTY_DEFAULTS.get(key);
        }

        @Override
        public String getDataType(String key) {
            return PROPERTY_DATA_TYPES.get(key);
        }

        @Override
        public void addKeys(List<ConfigProperty> list, Server server) {
            addYamlKeys(list, server);
        }

        @Override
        public void save(List<ConfigProperty> properties, Server server) {
            saveYaml(properties, server);
        }
    },
    SPIGOT("spigot.yml", s -> s.modLoader == ModLoader.PUFFERFISH) {
        private static final Logger LOGGER = LogManager.getLogger("ConfigManager/Spigot");
        private static final HashMap<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();
        private static final HashMap<String, String> PROPERTY_DEFAULTS = new HashMap<>();

        static {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://docs.papermc.io/assets/js/281a9c5e.2220deb7.js").openStream()))) {
                String content = reader.lines().collect(Collectors.joining());
                Pattern pattern = Pattern.compile("const i='(advancements.+[^\\\\])'");
                pattern.matcher(content).results().findFirst().ifPresentOrElse(matchResult -> {
                    String[] lines = matchResult.group(1).split("\\\\n(?!\")");
                    List<String> currentPath = new ArrayList<>();
                    int lastIndent = -2;
                    StringBuilder description = null;
                    for (String line : lines) {
                        if (line.isEmpty()) continue;
                        String path = StringUtils.join(currentPath, "/");
                        int indent = StringUtils.indexOfAnyBut(line, ' ');
                        String propertyName = line.substring(indent, line.length() - 1);
                        if (indent - lastIndent == 2) {
                            if (description != null) {
                                description.append(line.substring(indent));
                            } else if (line.stripLeading().startsWith("default: ")) {
                                PROPERTY_DEFAULTS.put(path, line.substring(indent + 9)); // After "default: "
                            } else {
                                currentPath.add(propertyName);
                            }
                        } else if (indent == lastIndent) {
                            if (description != null) {
                                description.append(" ").append(line.substring(indent));
                            } else if (line.stripLeading().equals("description: >-")) {
                                description = new StringBuilder();
                            } else if (line.stripLeading().startsWith("description: ")) {
                                PROPERTY_DESCRIPTIONS.put(path, line.substring(indent + 13)); // After "description: "
                            }
                        } else if (indent < lastIndent) {
                            for (int j = description == null ? 0 : 1; j < (lastIndent - indent) / 2; j++) { // The path won't actually be that long because one indent comes from "description: ", so start at 1
                                currentPath.removeLast();
                            }
                            if (description != null) {
                                PROPERTY_DESCRIPTIONS.put(path, description.toString());
                                description = null;
                            }
                            currentPath.add(propertyName);
                        }
                        lastIndent = indent;
                    }
                    LOGGER.info("Loaded spigot.yml property descriptions");
                }, () -> {throw new IllegalStateException("No descriptions found in https://docs.papermc.io/assets/js/281a9c5e.2220deb7.js");});
            } catch (IOException | IllegalStateException e) {
                LOGGER.warn("Failed to get property descriptions for spigot.yml", e);
            }
        }

        @Override
        public String getDescription(String key) {
            return PROPERTY_DESCRIPTIONS.get(key);
        }

        @Override
        public String getDefaultValue(String key) {
            return PROPERTY_DEFAULTS.get(key);
        }

        @Override
        public void addKeys(List<ConfigProperty> list, Server server) {
            addYamlKeys(list, server);
        }

        @Override
        public void save(List<ConfigProperty> properties, Server server) {
            saveYaml(properties, server);
        }
    },
    PUFFERFISH("pufferfish.yml", s -> s.modLoader == ModLoader.PUFFERFISH) {
        private static final Map<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();

        static {
            Pattern pattern = Pattern.compile("(.*?): (.*)");
            try {
                Document document = Jsoup.connect("https://docs.pufferfish.host/setup/pufferfish-fork-configuration/").userAgent("Mozilla").get();
                for (Element child : document.select(".post-content").getFirst().select("p")) {
                    String text = child.text();
                    Optional<MatchResult> optional = pattern.matcher(text).results().findFirst();
                    if (optional.isPresent()) {
                        MatchResult matchResult = optional.get();
                        String key = matchResult.group(1).replace('.', '/');
                        String description = matchResult.group(2);
                        PROPERTY_DESCRIPTIONS.put(key, description);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getDescription(String key) {
            return PROPERTY_DESCRIPTIONS.get(key);
        }

        @Override
        public void addKeys(List<ConfigProperty> list, Server server) {
            addYamlKeys(list, server);
        }

        @Override
        public void save(List<ConfigProperty> properties, Server server) {
            saveYaml(properties, server);
        }
    };

    private static final Logger LOGGER = LogManager.getLogger();

    public final String path;
    private final Predicate<Server> enabled;

    ConfigManager(String path, Predicate<Server> enabled) {
        this.path = path;
        this.enabled = enabled;
    }

    protected String getPath(Server server) {
        return server.serverLocation + "/" + path;
    }

    public String getDescription(String key) {
        return null;
    }

    public String getDefaultValue(String key) {
        return null;
    }

    public String getDataType(String key) {
        return null;
    }

    public boolean isEnabled(Server server) {
        return enabled.test(server);
    }

    public int getIndex() {
        for (int i = 0; i < ConfigManager.values().length; i++) {
            if (ConfigManager.values()[i] == this) {
                return i;
            }
        }
        throw new IllegalStateException("ConfigManager %s doesn't seem to exist".formatted(this));
    }

    public abstract void addKeys(List<ConfigProperty> list, Server server);

    public abstract void save(List<ConfigProperty> properties, Server server);

    public boolean canOptimise(ConfigProperty key) {
        return false;
    }

    public String optimise(int sliderValue, ConfigProperty property) {
        return property.value;
    }

    protected void addYamlKeys(List<ConfigProperty> list, Server server) {
        File yamlFile = new File(getPath(server));
        Yaml yaml = new Yaml();
        if (Files.exists(yamlFile.toPath())) {
            try (InputStream stream = new FileInputStream(yamlFile)) {
                Map<?, ?> map = yaml.load(stream);
                explore(list, map);
                LOGGER.info("Loaded config from %s".formatted(yamlFile.getAbsolutePath()));
            } catch (IOException e) {
                LOGGER.error("Failed to load config from %s".formatted(yamlFile.getAbsolutePath()), e);
            }
        }
    }

    private void explore(List<ConfigProperty> list, Map<?, ?> map) {
        map.forEach((key, value) -> {
            if (value instanceof Map<?, ?>) {
                explore(list, (Map<?, ?>) value);
            } else {
                list.add(new ConfigProperty((String) key, (String) value, this));
            }
        });
    }

    protected void saveYaml(List<ConfigProperty> properties, Server server) {
//        File yamlFile = new File(getPath(server));
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(yamlFile))) {
//            Yaml yaml = new Yaml();
//            String dump = yaml.dumpAsMap(map);
//            writer.write(dump);
//            LOGGER.info("Saved config to %s".formatted(yamlFile.getAbsolutePath()));
//        } catch (IOException e) {
//            LOGGER.error("Failed to save config to %s".formatted(yamlFile.getAbsolutePath()), e);
//        }
    }
}
