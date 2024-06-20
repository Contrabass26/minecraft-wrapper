package me.jsedwards.configserver;

public class ConfigProperty {

    public final String key;
    public final String value;
    public final PropertyType type;
    public final String configFile;

    public ConfigProperty(String key, String defaultValue, String type, String configFile) {
        this.key = key;
        this.value = defaultValue;
        this.type = PropertyType.get(type);
        this.configFile = configFile;
    }
}
