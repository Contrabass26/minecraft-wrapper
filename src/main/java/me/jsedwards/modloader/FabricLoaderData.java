package me.jsedwards.modloader;

@SuppressWarnings("unused")
public class FabricLoaderData {

    public String separator;
    public int build;
    public String maven;
    public String version;
    public boolean stable;

    public boolean isStable() {
        return stable;
    }
}
