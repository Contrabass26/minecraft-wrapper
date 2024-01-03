package me.jsedwards.configserver;

import me.jsedwards.Card;
import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class ServerConfigPanel extends JPanel implements Card {

    private static final Logger LOGGER = LogManager.getLogger();

    private String server = null;
    private final JLabel serverNameLbl;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final BasicPanel basicPanel;
    private final AdvancedPanel[] advancedPanels = {
            new AdvancedPanel(ServerPropertiesManager::new, "Vanilla", s -> true),
            new AdvancedPanel(SpigotConfigManager::new, "Spigot", s -> s.modLoader == ModLoader.PUFFERFISH),
            new AdvancedPanel(BukkitConfigManager::new, "Bukkit", s -> s.modLoader == ModLoader.PUFFERFISH)
    };

    public ServerConfigPanel() {
        this.setLayout(new GridBagLayout());
        // Back button
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> Main.WINDOW.cardPanel.switchToServer(server));
        this.add(backBtn, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(30, 10, 0, 10), 0, 0));
        // Server name label
        serverNameLbl = new JLabel();
        serverNameLbl.setFont(Main.MAIN_FONT);
        this.add(serverNameLbl, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(30, 0, 0, 0), 0, 0));
        // Delete button
        JButton deleteBtn = createDeleteBtn();
        this.add(deleteBtn, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(30, 0, 0, 10), 0, 0));
        // Tabbed pane
        basicPanel = new BasicPanel(this);
        tabbedPane.add("General", basicPanel);
        for (AdvancedPanel panel : advancedPanels) {
            tabbedPane.add(panel.name, panel);
        }
        this.add(tabbedPane, new GridBagConstraints(1, 2, 3, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
    }

    private JButton createDeleteBtn() {
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> {
            Server server = Server.get(this.server);
            assert server != null;
            boolean delete = JOptionPane.showConfirmDialog(Main.WINDOW, "Do you really want to delete server \"%s\"?".formatted(server.serverName), "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
            if (delete) {
                Server.delete(server);
            }
        });
        return deleteBtn;
    }

    public void optimiseConfigs(int sliderValue) {
        for (AdvancedPanel panel : advancedPanels) {
            panel.optimise(sliderValue);
        }
    }

    public Set<Identifier> getKeysToOptimise() {
        Set<Identifier> keys = new HashSet<>();
        for (AdvancedPanel panel : advancedPanels) {
            keys.addAll(panel.getPropertiesToOptimise());
        }
        return keys;
    }

    public boolean isNamespaceEnabled(String namespace) {
        for (AdvancedPanel panel : advancedPanels) {
            if (panel.name.equals(namespace)) {
                return panel.isEnabled(Server.get(server));
            }
        }
        return false;
    }

    public boolean isKeyOptimised(Identifier key) {
        for (AdvancedPanel panel : advancedPanels) {
            if (key.namespace.equals(panel.name)) {
                return panel.isKeyOptimised(key);
            }
        }
        throw new IllegalArgumentException("No panel with name " + key.namespace);
    }

    public void setServer(String serverName) {
        Server server = Server.get(serverName);
        if (server == null) {
            LOGGER.error("Trying to configure non-existent server \"" + serverName + "\"");
            return;
        }
        this.server = serverName;
        this.serverNameLbl.setText(serverName + " - " + server.modLoader);
        // Load server properties
        for (int i = 0; i < advancedPanels.length; i++) {
            AdvancedPanel panel = advancedPanels[i];
            boolean enabled = panel.isEnabled(server);
            if (enabled) {
                panel.setServer(server);
            }
            tabbedPane.setEnabledAt(i + 1, enabled);
        }
        // Set sliders on basic panel
        basicPanel.setServer(server);
    }

    @Override
    public void exit() {
        for (AdvancedPanel panel : advancedPanels) {
            panel.saveProperties();
        }
        basicPanel.save();
    }

    @Override
    public void onShowCard() {

    }

    public void setKeyOptimised(Identifier key, boolean selected) {
        for (AdvancedPanel panel : advancedPanels) {
            if (panel.name.equals(key.namespace)) {
                panel.setKeyOptimised(key, selected);
                break;
            }
        }
        Server server = Server.get(this.server);
        if (server != null) {
            server.keysToOptimise.put(key, selected);
        }
    }
}
