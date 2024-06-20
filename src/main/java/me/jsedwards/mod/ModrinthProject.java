package me.jsedwards.mod;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import me.jsedwards.Main;
import me.jsedwards.modloader.ModLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ModrinthProject extends Project {

    public final String id;

    public ModrinthProject(JsonNode root) {
        super(
                root.get("title").textValue(),
                root.get("description").textValue(),
                root.get("author").textValue(),
                root.get("icon_url").textValue(),
                root.get("downloads").intValue());
        this.id = root.get("project_id").textValue();
    }

    public ModrinthProject(String id) {
        this(Modrinth.getProjectNode(id));
    }

    public ModFile getFile(String mcVersion, ModLoader loader) {
        return Modrinth.getVersionFile(id, loader, mcVersion);
    }

    @Override
    public void downloadFile(ModFile file, File out) throws IOException {
        Main.WINDOW.statusPanel.saveFileFromUrl(new URL(file.url()), out);
    }

    @Override
    public void serialise(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("source", "modrinth");
        generator.writeStringField("id", id);
        generator.writeEndObject();
    }

    @Override
    public String toString() {
        return super.toString() + " (Modrinth)";
    }
}
