package me.jsedwards.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import me.jsedwards.dashboard.Server;
import me.jsedwards.modloader.ModLoader;

import java.io.IOException;

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
        int optimisationLevel = root.get("optimisationLevel").intValue();
        return Server.create(name, location, modLoader, mcVersion, mbMemory, optimisationLevel, false);
    }
}
