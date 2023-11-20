package me.jsedwards;

import me.jsedwards.gui.Server;

public class ServerData {

    public String name;
    public String location;
    public String mcVersion;

    public ServerData(Server server) {
        this.name = server.serverName;
        this.location = server.serverLocation;
        this.mcVersion = server.mcVersion;
    }

    @SuppressWarnings("unused")
    public ServerData(String name, String location, String modLoader, String mcVersion) {
        this.name = name;
        this.location = location;
        this.mcVersion = mcVersion;
    }

    public Server convert() {
        return Server.create(this.name, this.location, ModLoader.FABRIC, this.mcVersion);
    }

    @Override
    public String toString() {
        return "ServerData{name='%s', location='%s', modLoader=%s, mcVersion='%s'}".formatted(name, location, "null", mcVersion);
    }
}
