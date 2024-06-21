package me.jsedwards.configserver;

import org.apache.commons.lang3.StringUtils;

public class ConfigProperty implements Comparable<ConfigProperty> {

    public final String key;
    public String value;
    public final PropertyType type;
    public final ConfigManager configManager;
    private final String description;
    private final String defaultValue;

    public ConfigProperty(String key, String value, String defaultValue, String description, String type, ConfigManager configManager) {
        this.key = key;
        this.value = value;
        this.defaultValue = defaultValue;
        this.description = description;
        this.type = PropertyType.get(type, defaultValue);
        this.configManager = configManager;
    }

    public ConfigProperty(String key, String value, PropertyType type, ConfigManager configManager) {
        this.key = key;
        this.value = value;
        this.defaultValue = configManager.getDefaultValue(key);
        this.description = configManager.getDescription(key);
        this.type = type;
        this.configManager = configManager;
    }

    public ConfigProperty(String key, ConfigManager configManager) {
        this(key,
                configManager.getDefaultValue(key),
                configManager.getDefaultValue(key),
                configManager.getDescription(key),
                configManager.getDataType(key),
                configManager);
    }

    public ConfigProperty(String s) {
        this(
                StringUtils.substringAfter(s, ':'),
                ConfigManager.valueOf(StringUtils.substringBefore(s, ':'))
        );
    }

    public boolean canOptimise() {
        return configManager.canOptimise(this);
    }

    public void edit() {
        value = type.inputValue(this);
    }

    @Override
    public String toString() {
        return "%s = %s".formatted(key, value);
    }

    public String getDescription() {
        return description == null ? "Not found" : description;
    }

    public String getDefaultValue() {
        return defaultValue == null ? "Not found" : defaultValue;
    }

    public String serialise() {
        return "%s:%s".formatted(configManager, key);
    }

    @Override
    public int compareTo(ConfigProperty o) {
        return this.key.compareTo(o.key);
    }
}
