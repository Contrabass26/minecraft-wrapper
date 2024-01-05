package me.jsedwards.configserver;

import me.jsedwards.util.MathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.util.Set;
import java.util.function.Function;

public interface ConfigManager extends ListModel<String> {

    Logger LOGGER = LogManager.getLogger("ConfigManager");
    Function<Integer, Integer> VIEW_DISTANCE_OPTIMISATION = slider -> (int) Math.round(MathUtils.quadraticFunction(slider, 0.0022, 0.07, 3));
    Function<Integer, Integer> SIMULATION_DISTANCE_OPTIMISATION = slider -> (int) Math.round(MathUtils.quadraticFunction(slider, 0.0014, 0.13, 5));

    void updateSearch(String query);

    void save();

    void set(String key, String value);

    String getDescription(String key);

    String getDataType(String key);

    String getDefaultValue(String key);

    Set<String> getKeysToOptimise();

    boolean isKeyOptimised(String key);

    void setKeyOptimised(String key, boolean enabled);

    void optimise(int sliderValue);
}
