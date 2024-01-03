package me.jsedwards.data;

import me.jsedwards.util.MinecraftUtils;

@SuppressWarnings("unused")
public class OperatorPlayer {

    public String uuid;
    public String name;
    public int level;
    public boolean bypassesPlayerLimit;

    @Override
    public String toString() {
        return name;
    }

    public static OperatorPlayer create(String name) {
        OperatorPlayer player = new OperatorPlayer();
        player.name = name;
        player.uuid = MinecraftUtils.getPlayerUuid(name);
        player.level = 4;
        player.bypassesPlayerLimit = false;
        return player;
    }
}
