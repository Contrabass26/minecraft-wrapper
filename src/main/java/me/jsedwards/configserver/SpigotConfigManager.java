package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import me.jsedwards.modloader.ModLoader;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpigotConfigManager extends YamlConfigManager {

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

    public SpigotConfigManager() {
        super("spigot.yml", s -> s.modLoader == ModLoader.PUFFERFISH);
    }

    @Override
    protected String getPath(Server server) {
        return server.serverLocation + "/" + name;
    }

    @Override
    public String getDescription(String key) {
        String description = PROPERTY_DESCRIPTIONS.get(key);
        if (description.startsWith("\"")) return description.substring(1, description.length() - 1);
        return description;
    }

    @Override
    public String getDataType(String key) {
        return null;
    }

    @Override
    public String getDefaultValue(String key) {
        String defaultValue = PROPERTY_DEFAULTS.get(key);
        return defaultValue.substring(1, defaultValue.length() - 1);
    }

    @Override
    protected String optimise(int sliderValue, ConfigProperty property) {
        return switch (property.key) {
            case "world-settings/default/view-distance" ->
                    String.valueOf(ServerPropertiesManager.VIEW_DISTANCE_OPTIMISATION.apply(sliderValue));
            case "world-settings/default/simulation-distance" ->
                    String.valueOf(ServerPropertiesManager.SIMULATION_DISTANCE_OPTIMISATION.apply(sliderValue));
            case "world-settings/default/merge-radius/exp", "world-settings/default/merge-radius/item" ->
                    String.valueOf((int) Math.round(5 - 0.04 * sliderValue));
            case "world-settings/default/item-despawn-rate" ->
                    String.valueOf((int) Math.min(Math.round(6100 - 5000 * Math.pow(Math.E, -0.05 * sliderValue)), 6000));
            default -> property.value;
        };
    }
}
