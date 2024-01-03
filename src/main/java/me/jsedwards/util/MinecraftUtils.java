package me.jsedwards.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

public class MinecraftUtils {
    public static String getPlayerUuid(String username) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new URL("https://api.mojang.com/users/profiles/minecraft/" + username));
            return formatUuid(root.get("id").textValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatUuid(String uuid) {
        StringBuilder builder = new StringBuilder(uuid);
        builder.insert(20, '-');
        builder.insert(16, '-');
        builder.insert(12, '-');
        builder.insert(8, '-');
        return builder.toString();
    }
}
