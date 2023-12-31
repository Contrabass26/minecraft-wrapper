package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class BukkitConfigManager extends YamlConfigManager {

    private static final Map<String, Function<Integer, Integer>> OPTIMISATION_FUNCTIONS = new HashMap<>();
    private static final Map<String, Boolean> KEYS_ENABLED = new HashMap<>();

    public BukkitConfigManager(Server server) {
        super(server, s -> s.serverLocation + File.separator + "bukkit.yml");
    }

    @Override
    public String getDescription(String key) {
        return "Not found";
    }

    @Override
    public String getDataType(String key) {
        return "Not found";
    }

    @Override
    public String getDefaultValue(String key) {
        return "Not found";
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
