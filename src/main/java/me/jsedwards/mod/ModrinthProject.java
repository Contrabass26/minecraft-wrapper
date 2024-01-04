package me.jsedwards.mod;

import com.fasterxml.jackson.databind.JsonNode;
import me.jsedwards.modloader.ModLoader;

public class ModrinthProject extends Project {

    public final String id;

    public ModrinthProject(JsonNode root) {
        super(root.get("title").textValue(), root.get("description").textValue());
        this.id = root.get("project_id").textValue();
    }

    public ModFile getFile(String mcVersion, ModLoader loader) {
        return Modrinth.getVersionFile(id, loader, mcVersion);
    }
}
