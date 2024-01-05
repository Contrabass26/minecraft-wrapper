package me.jsedwards.mod;

import me.jsedwards.Main;
import me.jsedwards.modloader.ModLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public final class CurseForgeProject extends Project {

    private final String numericId;
    private final String stringId;

    public CurseForgeProject(String title, String description, String author, String icon, String numericId, String stringId) {
        super(title, description, author, icon);
        this.numericId = numericId;
        this.stringId = stringId;
    }

    @Override
    public ModFile getFile(String mcVersion, ModLoader loader) {
        return CurseForge.getFile(numericId, mcVersion, loader);
    }

    @Override
    public void downloadFile(ModFile file, File out) throws IOException {
        Main.WINDOW.statusPanel.curlToFile(new URL(file.url()), out);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return super.toString() + " (CurseForge)";
    }

    public static class Builder {

        public String title;
        public String description;
        public String author;
        public String icon;
        public String numericId;
        public String stringId;

        private Builder() {}

        public CurseForgeProject build() {
            return new CurseForgeProject(title, description, author, icon, numericId, stringId);
        }
    }
}
