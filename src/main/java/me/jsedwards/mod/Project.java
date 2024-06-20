package me.jsedwards.mod;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import me.jsedwards.Main;
import me.jsedwards.modloader.ModLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

@JsonSerialize(using = Project.Serialiser.class)
@JsonDeserialize(using = Project.Deserialiser.class)
public class Project implements Comparable<Project> {

    public final String id;
    public final String title;
    public final String description;
    public final String author;
    public final String icon;
    public final int downloads;
    public final ModProvider source;
    private final List<String> supportedVersions;

    public Project(String id, String title, String description, String author, String icon, int downloads, List<String> supportedVersions, ModProvider source) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.icon = icon;
        this.downloads = downloads;
        this.source = source;
        this.supportedVersions = supportedVersions;
    }

    public static Project create(ModProvider source, String id) {
        return source.createProject(id);
    }

    public boolean supportsVersion(String mcVersion) {
        return supportedVersions.contains(mcVersion);
    }

    public ModFile getFile(ModLoader modLoader, String mcVersion) {
        return source.getFile(this, modLoader, mcVersion);
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(title, source);
    }

    @Override
    public int compareTo(Project o) {
        // Descending sort by download count
        return Integer.compare(o.downloads, this.downloads);
    }

    public static class Serialiser extends StdSerializer<Project> {

        @SuppressWarnings("unused")
        public Serialiser() {
            this(null);
        }

        protected Serialiser(Class<Project> t) {
            super(t);
        }

        @Override
        public void serialize(Project value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("source", value.source.toString());
            gen.writeStringField("id", value.id);
            gen.writeEndObject();
        }
    }

    public static class Deserialiser extends StdDeserializer<Project> {

        @SuppressWarnings("unused")
        public Deserialiser() {
            this(null);
        }

        protected Deserialiser(Class<?> vc) {
            super(vc);
        }

        @Override
        public Project deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonNode root = parser.getCodec().readTree(parser);
            ModProvider source = ModProvider.valueOf(root.get("source").textValue());
            String id = root.get("id").textValue();
            return source.createProject(id);
        }
    }

    public record ModFile(String url, String filename, Project parent) {

        @Override
        public String toString() {
            return filename;
        }

        public void download(String destinationFolder) {
            try {
                Main.WINDOW.statusPanel.saveFileFromUrl(new URL(url), new File(destinationFolder + filename));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
