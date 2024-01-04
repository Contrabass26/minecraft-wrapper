package me.jsedwards.mod;

import com.fasterxml.jackson.databind.JsonNode;

public class ModrinthProject {

    public final String slug;
    public final String title;
    public final String description;
    public final boolean serverSide;

    public ModrinthProject(JsonNode root) {
        this.slug = root.get("slug").textValue();
        this.title = root.get("title").textValue();
        this.description = root.get("description").textValue();
        this.serverSide = !root.get("server_side").textValue().equals("unsupported");
    }

    public boolean isServerSide() {
        return serverSide;
    }

    @Override
    public String toString() {
        return title;
    }
}
