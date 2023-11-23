package me.jsedwards;

import me.jsedwards.gui.Server;

public class ServerData {

    public String name;
    public String location;
    public ModLoader modLoader;
    public String mcVersion;

    public ServerData(Server server) {
        this.name = server.serverName;
        this.location = server.serverLocation;
        this.modLoader = server.modLoader;
        this.mcVersion = server.mcVersion;
    }

    @SuppressWarnings("unused")
    public ServerData(String name, String location, String modLoader, String mcVersion) {
        this.name = name;
        this.location = location;
        this.modLoader = ModLoader.valueOf(modLoader);
        this.mcVersion = mcVersion;
    }

    public void convert() {
        Server.create(this.name, this.location, this.modLoader, this.mcVersion);
    }

    @Override
    public String toString() {
        return "ServerData{name='%s', location='%s', modLoader=%s, mcVersion='%s'}".formatted(name, location, this.modLoader.toString(), mcVersion);
    }
}