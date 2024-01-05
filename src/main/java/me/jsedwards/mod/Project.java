package me.jsedwards.mod;

import me.jsedwards.modloader.ModLoader;

import java.io.File;
import java.io.IOException;

public abstract class Project {

    public final String title;
    public final String description;
    public final String author;
    public final String icon;

    public Project(String title, String description, String author, String icon) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.icon = icon;
    }

    public abstract ModFile getFile(String mcVersion, ModLoader loader);

    public abstract void downloadFile(ModFile file, File out) throws IOException;

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
