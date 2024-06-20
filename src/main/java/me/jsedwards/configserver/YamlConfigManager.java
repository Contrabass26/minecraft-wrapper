package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class YamlConfigManager extends AdvancedPanel {

    protected File yamlFile;

    public YamlConfigManager(String name, Predicate<Server> enabled) {
        super(name, enabled);
    }

    @Override
    protected void addKeys(List<ConfigProperty> list, Server server) {
        yamlFile = new File(getPath(server));
        Yaml yaml = new Yaml();
        if (Files.exists(yamlFile.toPath())) {
            try (InputStream stream = new FileInputStream(yamlFile)) {
                Map<?, ?> map = yaml.load(stream);
                LOGGER.info("Loaded config from %s".formatted(yamlFile.getAbsolutePath()));
            } catch (IOException e) {
                LOGGER.error("Failed to load config from %s".formatted(yamlFile.getAbsolutePath()), e);
            }
        }
    }

    protected abstract String getPath(Server server);

    @Override
    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(yamlFile))) {
            Yaml yaml = new Yaml();
            String dump = yaml.dumpAsMap(map);
            writer.write(dump);
            LOGGER.info("Saved config to %s".formatted(yamlFile.getAbsolutePath()));
        } catch (IOException e) {
            LOGGER.error("Failed to save config to %s".formatted(yamlFile.getAbsolutePath()), e);
        }
    }
}
