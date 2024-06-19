package me.jsedwards.mod;

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

    @Override
    public ModFile getFile(String mcVersion, ModLoader loader) {
        return CurseForge.getModFile(numericId, mcVersion, loader);
    }

    @Override
    public void downloadFile(ModFile file, File out) throws IOException {
        Main.WINDOW.statusPanel.saveFileFromUrl(new URL(file.url()), out);
    }

    @Override
    public String toString() {
        return super.toString() + " (CurseForge)";
    }
}
