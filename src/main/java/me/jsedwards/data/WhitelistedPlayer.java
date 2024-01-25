package me.jsedwards.data;

import me.jsedwards.util.MinecraftUtils;

@SuppressWarnings("unused")
public class WhitelistedPlayer {

    public String uuid;
    public String name;

    @Override
    public String toString() {
        return name;
    }

    public static WhitelistedPlayer create(String name) {
        WhitelistedPlayer player = new WhitelistedPlayer();
        player.name = name;
        player.uuid = MinecraftUtils.getPlayerUuid(name);
        if (player.uuid == null) return null;
        return player;
    }
}
