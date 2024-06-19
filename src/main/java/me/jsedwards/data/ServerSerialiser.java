package me.jsedwards.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import me.jsedwards.dashboard.Server;
import me.jsedwards.mod.CurseForgeProject;
import me.jsedwards.mod.Project;
import me.jsedwards.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class ServerSerialiser extends StdSerializer<Server> {

    @SuppressWarnings("unused")
    public ServerSerialiser() {
        this(null);
    }

    public ServerSerialiser(Class<Server> t) {
        super(t);
    }

    @Override
    public void serialize(Server server, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("serverName", server.serverName);
        generator.writeStringField("serverLocation", server.serverLocation);
        generator.writeStringField("modLoader", server.modLoader.toString().toUpperCase());
        generator.writeStringField("mcVersion", server.mcVersion);
        generator.writeNumberField("mbMemory", server.mbMemory);
        generator.writeStringField("javaVersion", server.javaVersion);
        generator.writeNumberField("optimisationLevel", server.optimisationLevel);
        generator.writeObjectFieldStart("keysToOptimise");
        for (Map.Entry<Identifier, Boolean> entry : server.keysToOptimise.entrySet()) {
            generator.writeBooleanField(entry.getKey().toString(), entry.getValue());
        }
        generator.writeEndObject();
        // Mods
        generator.writeStartArray("mods");
        for (Project mod : server.mods) {
            generator.writeStartObject();
            if (mod instanceof CurseForgeProject) {

            }
            generator.writeEndObject();
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }
}
