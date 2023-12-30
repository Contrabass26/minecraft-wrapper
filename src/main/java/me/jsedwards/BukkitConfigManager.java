package me.jsedwards;

import me.jsedwards.gui.Server;

import java.io.File;

public class BukkitConfigManager extends YamlConfigManager {

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
    public void optimise(int sliderValue) {

    }
}
