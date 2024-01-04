package me.jsedwards.mod;

import me.jsedwards.modloader.ModLoader;

public abstract class Project {

    public final String title;
    public final String description;

    public Project(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public abstract ModFile getFile(String mcVersion, ModLoader loader);

    @Override
    public String toString() {
        return title;
    }

    public record ModFile(String url, String filename) {

        @Override
        public String toString() {
            return filename;
        }
    }
}
