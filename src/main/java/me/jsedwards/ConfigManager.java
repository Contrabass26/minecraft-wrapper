package me.jsedwards;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public interface ConfigManager extends ListModel<String> {

    Logger LOGGER = LogManager.getLogger();

    void updateSearch(String query);

    void save();

    void set(String key, String value);

    String getDescription(String key);

    String getDataType(String key);

    String getDefaultValue(String key);

    void optimise(int sliderValue);
}
