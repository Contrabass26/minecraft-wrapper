package me.jsedwards.mod;

import me.jsedwards.modloader.ModLoader;

public final class CurseForgeProject extends Project {

    public CurseForgeProject(String title, String description) {
        super(title, description);
    }

    @Override
    public ModFile getFile(String mcVersion, ModLoader loader) {
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        public String title;
        public String description;

        private Builder() {}

        public CurseForgeProject build() {
            return new CurseForgeProject(title, description);
        }
    }
}
