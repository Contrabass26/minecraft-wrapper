package me.jsedwards.mod;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import me.jsedwards.Main;
import me.jsedwards.modloader.ModLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public final class CurseForgeProject extends Project {

    private final int numericId;

    public CurseForgeProject(String title, String description, String author, String icon, int numericId, int downloads) {
        super(title, description, author, icon, downloads);
        this.numericId = numericId;
    }

    public CurseForgeProject(JsonNode data) {
        this(
                data.get("name").textValue(),
                data.get("summary").textValue(),
                CurseForge.getAuthorString(data.get("authors")),
                data.get("logo").get("thumbnailUrl").textValue(),
                data.get("id").intValue(),
                data.get("downloadCount").intValue()
        );
    }

    public CurseForgeProject(int id) {
        this(CurseForge.getProjectNode(id));
    }

    @Override
    public ModFile getFile(String mcVersion, ModLoader loader) {
        return CurseForge.getModFile(numericId, mcVersion, loader);
    }

    @Override
    public void downloadFile(ModFile file, File out) throws IOException {
        Main.WINDOW.statusPanel.saveFileFromUrl(new URL(file.url()), out);
    }

    @Override
    public void serialise(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("source", "curseforge");
        generator.writeNumberField("id", numericId);
        generator.writeEndObject();
    }

    @Override
    public String toString() {
        return super.toString() + " (CurseForge)";
    }
}
