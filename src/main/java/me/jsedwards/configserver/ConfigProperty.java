package me.jsedwards.configserver;

public class ConfigProperty implements Comparable<ConfigProperty> {

    public final String key;
    public String value;
    public final PropertyType type;
    public final String configFile;
    private final String description;
    private final String defaultValue;

    public ConfigProperty(String key, String defaultValue, String description, String type, String configFile) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.description = description;
        this.value = defaultValue;
        this.type = PropertyType.get(type, defaultValue);
        this.configFile = configFile;
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
        return "%s:%s".formatted(configFile, key);
    }

    @Override
    public int compareTo(ConfigProperty o) {
        return this.key.compareTo(o.key);
    }
}
