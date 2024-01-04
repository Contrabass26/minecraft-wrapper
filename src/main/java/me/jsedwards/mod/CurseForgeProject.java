package me.jsedwards.mod;

import me.jsedwards.modloader.ModLoader;

public final class CurseForgeProject extends Project {

    public CurseForgeProject(String title, String description, String author, String icon) {
        super(title, description, author, icon);
    }

    @Override
    public ModFile getFile(String mcVersion, ModLoader loader) {
        return null;
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

        private Builder() {}

        public CurseForgeProject build() {
            return new CurseForgeProject(title, description, author, icon);
        }
    }
}
