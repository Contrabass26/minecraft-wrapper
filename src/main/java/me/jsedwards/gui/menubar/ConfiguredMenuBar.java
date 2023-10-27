package me.jsedwards.gui.menubar;

import me.jsedwards.ResourceLoader;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfiguredMenuBar extends JMenuBar {

    private final Map<String, JMenuItem> elements = new HashMap<>();

    public ConfiguredMenuBar(String configName) throws IOException {
        super();
        MenuBarConfig config = ResourceLoader.getMenuBarConfig(configName);
        for (MenuBarConfig.Menu menu : config.menus) {
            JMenu jMenu = new JMenu(menu.name);
            for (MenuBarConfig.MenuItem menuItem : menu.children) {
                JMenuItem jMenuItem = new JMenuItem(menuItem.name);
                elements.put(menu.name + "$" + menuItem.name, jMenuItem);
                jMenu.add(jMenuItem);
            }
            this.add(jMenu);
        }
    }

    public void addListener(String path, ActionListener listener) {
        elements.get(path).addActionListener(listener);
    }
}
