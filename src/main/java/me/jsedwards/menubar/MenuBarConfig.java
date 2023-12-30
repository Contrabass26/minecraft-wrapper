package me.jsedwards.menubar;

import me.jsedwards.util.JsonUtils;

import java.io.IOException;
import java.io.InputStream;

public class MenuBarConfig {

    public Menu[] menus;

    private MenuBarConfig() {}

    public static MenuBarConfig create(InputStream stream) throws IOException {
        return JsonUtils.readJson(stream, MenuBarConfig.class);
    }

    public static class Menu {

        private Menu() {}

        public String name;
        public MenuItem[] children;
    }

    public static class MenuItem {

        private MenuItem() {}

        public String name;
    }
}
