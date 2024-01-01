package me.jsedwards.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import me.jsedwards.dashboard.Server;

import java.io.IOException;

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
        generator.writeStringField("name", server.serverName);
        generator.writeStringField("location", server.serverLocation);
        generator.writeStringField("modLoader", server.modLoader.toString().toUpperCase());
        generator.writeStringField("mcVersion", server.mcVersion);
        generator.writeEndObject();
    }
}