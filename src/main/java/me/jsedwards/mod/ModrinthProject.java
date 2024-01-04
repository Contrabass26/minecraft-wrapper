package me.jsedwards.mod;

import com.fasterxml.jackson.databind.JsonNode;
import me.jsedwards.modloader.ModLoader;

public class ModrinthProject {

    public final String id;
    public final String title;
    public final String description;

    public ModrinthProject(JsonNode root) {
        this.id = root.get("project_id").textValue();
        this.title = root.get("title").textValue();
        this.description = root.get("description").textValue();
    }

    @Override
    public String toString() {
        return title;
    }

    public Modrinth.ModrinthFile getFile(String mcVersion, ModLoader loader) {
        return Modrinth.getVersionFile(id, loader, mcVersion);
    }
}
