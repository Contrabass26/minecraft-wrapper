package me.jsedwards;

import javax.swing.*;

public interface ConfigManager extends ListModel<String> {

    void updateSearch(String query);

    void save();

    void set(String key, String value);

    String getDescription(String key);

    String getDataType(String key);

    String getDefaultValue(String key);

    void optimise(int sliderValue);
}
