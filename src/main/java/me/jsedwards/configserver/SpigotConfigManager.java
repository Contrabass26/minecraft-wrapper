package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpigotConfigManager extends YamlConfigManager {

    private static final HashMap<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DATA_TYPES = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DEFAULTS = new HashMap<>();
    private static final Map<String, Function<Integer, Integer>> OPTIMISATION_FUNCTIONS = new HashMap<>();
    private static final Map<String, Boolean> KEYS_ENABLED = new HashMap<>();

    static {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://docs.papermc.io/assets/js/281a9c5e.2220deb7.js").openStream()))) {
            String content = reader.lines().collect(Collectors.joining());
            Pattern pattern = Pattern.compile("const i='(advancements.+[^\\\\])'");
            pattern.matcher(content).results().findFirst().ifPresentOrElse(matchResult -> {
                String[] lines = matchResult.group(1).split("\n");
                Stack<String> currentPath = new Stack<>();
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.startsWith("default:")) {
                        // This line and the next form a description of the current path
                        String defaultValue = line.
                    }
                }
            }, () -> {throw new IllegalStateException("No descriptions found in https://docs.papermc.io/assets/js/281a9c5e.2220deb7.js");});
        } catch (IOException | IllegalStateException e) {
            LOGGER.warn("Failed to get property descriptions for spigot.yml", e);
        }
        OPTIMISATION_FUNCTIONS.put("world-settings/default/view-distance", ConfigManager.VIEW_DISTANCE_OPTIMISATION);
        OPTIMISATION_FUNCTIONS.put("world-settings/default/simulation-distance", ConfigManager.SIMULATION_DISTANCE_OPTIMISATION);
        OPTIMISATION_FUNCTIONS.put("world-settings/default/merge-radius/exp", slider -> (int) Math.round(5 - 0.04 * slider));
        OPTIMISATION_FUNCTIONS.put("world-settings/default/merge-radius/item", slider -> (int) Math.round(5 - 0.04 * slider));
        OPTIMISATION_FUNCTIONS.put("world-settings/default/item-despawn-rate", slider -> (int) Math.min(Math.round(6100 - 5000 * Math.pow(Math.E, -0.05 * slider)), 6000));
    }

    public SpigotConfigManager(Server server) {
        super(server, s -> s.serverLocation + File.separator + "spigot.yml");
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
    public Set<String> getKeysToOptimise() {
        return OPTIMISATION_FUNCTIONS.keySet();
    }

    @Override
    public boolean isKeyOptimised(String key) {
        return KEYS_ENABLED.getOrDefault(key, true);
    }

    @Override
    public void setKeyOptimised(String key, boolean enabled) {
        KEYS_ENABLED.put(key, enabled);
    }

    @Override
    public void optimise(int sliderValue) {
        OPTIMISATION_FUNCTIONS.forEach((key, function) -> {
            if (isKeyOptimised(key)) {
                this.set(key, String.valueOf(function.apply(sliderValue)));
            }
        });
    }
}
