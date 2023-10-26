package me.jsedwards;

import java.io.IOException;
import java.io.InputStream;

public class ResourceLoader {

    public static InputStream getResource(String name) {
        return ResourceLoader.class.getClassLoader().getResourceAsStream(name);
    }

    public static MenuBarConfig getMenuBarConfig(String menuBarName) throws IOException {
        InputStream stream = ResourceLoader.getResource("menubar/" + menuBarName + ".json");
        return MenuBarConfig.create(stream);
    }
}
