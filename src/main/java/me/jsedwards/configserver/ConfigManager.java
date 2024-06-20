package me.jsedwards.configserver;

import me.jsedwards.util.MathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.util.Set;
import java.util.function.Function;

public abstract class ConfigManager extends DefaultListModel<ConfigProperty> {

    public static final Logger LOGGER = LogManager.getLogger("ConfigManager");
    public static final Function<Integer, Integer> VIEW_DISTANCE_OPTIMISATION = slider -> (int) Math.round(MathUtils.quadraticFunction(slider, 0.0022, 0.07, 3));
    public static final Function<Integer, Integer> SIMULATION_DISTANCE_OPTIMISATION = slider -> (int) Math.round(MathUtils.quadraticFunction(slider, 0.0014, 0.13, 5));

    public abstract void updateSearch(String query);

    public abstract void save();

    public abstract void set(String key, String value);

    public abstract String getDescription(String key);

    public abstract String getDataType(String key);

    public abstract String getDefaultValue(String key);

    public abstract Set<String> getKeysToOptimise();

    public abstract boolean isKeyOptimised(String key);

    public abstract void setKeyOptimised(String key, boolean enabled);

    public abstract void optimise(int sliderValue);
}
