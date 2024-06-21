package me.jsedwards.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import me.jsedwards.configserver.ConfigManager;
import me.jsedwards.configserver.ConfigProperty;
import me.jsedwards.dashboard.Server;
import me.jsedwards.mod.Project;
import me.jsedwards.modloader.ModLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerDeserialiser extends StdDeserializer<Server> {

    @SuppressWarnings("unused")
    public ServerDeserialiser() {
        this(null);
    }

    public ServerDeserialiser(Class<?> vc) {
        super(vc);
    }

    @Override
    public Server deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode root = parser.getCodec().readTree(parser);
        String name = root.get("serverName").textValue();
        String location = root.get("serverLocation").textValue();
        ModLoader modLoader = ModLoader.valueOf(root.get("modLoader").textValue());
        String mcVersion = root.get("mcVersion").textValue();
        int mbMemory = root.get("mbMemory").intValue();
        String javaVersion = root.get("javaVersion").textValue();
        int optimisationLevel = root.get("optimisationLevel").intValue();
        Map<ConfigProperty, Boolean> keysToOptimise = new HashMap<>();
        root.get("keysToOptimise").fields().forEachRemaining(entry -> {
            String fullKey = entry.getKey();
            String[] splits = fullKey.split(":");
            ConfigProperty property = new ConfigProperty(splits[0], ConfigManager.valueOf(splits[1]));
            boolean value = entry.getValue().booleanValue();
            keysToOptimise.put(property, value);
        });
        ObjectMapper mapper = new ObjectMapper();
        List<Project.ModFile> mods = new ArrayList<>();
        for (JsonNode mod : root.get("mods")) {
            mods.add(mapper.treeToValue(mod, Project.ModFile.class));
        }
        return Server.create(name, location, modLoader, mcVersion, mbMemory, javaVersion, optimisationLevel, keysToOptimise, mods, false);
    }
}
