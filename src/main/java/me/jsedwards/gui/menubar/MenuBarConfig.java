package me.jsedwards.gui.menubar;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MenuBarConfig {

    public Menu[] menus;

    private MenuBarConfig() {}

    public static MenuBarConfig create(InputStream stream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new InputStreamReader(stream), MenuBarConfig.class);
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
