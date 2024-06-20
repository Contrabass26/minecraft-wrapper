package me.jsedwards.mod;

import com.fasterxml.jackson.core.JsonGenerator;
import me.jsedwards.modloader.ModLoader;

import java.io.File;
import java.io.IOException;

public abstract class Project implements Comparable<Project> {

    public final String title;
    public final String description;
    public final String author;
    public final String icon;
    public final int downloads;

    public Project(String title, String description, String author, String icon, int downloads) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.icon = icon;
        this.downloads = downloads;
    }

    public abstract ModFile getFile(String mcVersion, ModLoader loader);

    public abstract void downloadFile(ModFile file, File out) throws IOException;

    public abstract void serialise(JsonGenerator generator) throws IOException;

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int compareTo(Project o) {
        // Descending sort by download count
        return Integer.compare(o.downloads, this.downloads);
    }

    // TODO: Include file size
    public record ModFile(String url, String filename) {

        @Override
        public String toString() {
            return filename;
        }
    }
}
