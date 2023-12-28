package me.jsedwards;

import me.jsedwards.gui.Server;

public class ServerData {

    public String name;
    public String location;
    public String modLoader;
    public String mcVersion;

    public static ServerData create(Server server) {
        ServerData data = new ServerData();
        data.name = server.serverName;
        data.location = server.serverLocation;
        data.modLoader = server.modLoader.toString().toUpperCase();
        data.mcVersion = server.mcVersion;
        return data;
    }

    public void convert() {
        Server.create(this.name, this.location, ModLoader.valueOf(this.modLoader), this.mcVersion, false);
    }

    @Override
    public String toString() {
        return "ServerData{name='%s', location='%s', modLoader=%s, mcVersion='%s'}".formatted(name, location, this.modLoader.toString(), mcVersion);
    }
}
