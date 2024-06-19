package me.jsedwards.configserver;

import me.jsedwards.Card;
import me.jsedwards.Main;
import me.jsedwards.createserver.McVersionStagePanel;
import me.jsedwards.dashboard.Server;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.Identifier;
import me.jsedwards.util.ColouredCellRenderer;
import me.jsedwards.util.MinecraftUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerConfigPanel extends JPanel implements Card {

    private static final Logger LOGGER = LogManager.getLogger("ServerConfigPanel");

    private String server = null;
    private final JLabel serverNameLbl;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final BasicPanel basicPanel;
    private final ModsPanel modsPanel;
    private final AdvancedPanel[] advancedPanels = {
            new AdvancedPanel(ServerPropertiesManager::new, "Vanilla", s -> true),
            new AdvancedPanel(SpigotConfigManager::new, "Spigot", s -> s.modLoader == ModLoader.PUFFERFISH),
            new AdvancedPanel(BukkitConfigManager::new, "Bukkit", s -> s.modLoader == ModLoader.PUFFERFISH),
            new AdvancedPanel(PufferfishConfigManager::new, "Pufferfish", s -> s.modLoader == ModLoader.PUFFERFISH)
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
        // Update button
        JButton updateBtn = new JButton("Update");
        updateBtn.addActionListener(e -> {
            Server server = Server.get(this.server);
            assert server != null;
            List<String> versions = new ArrayList<>(McVersionStagePanel.VERSIONS.stream().filter(version -> MinecraftUtils.compareVersions(version, server.mcVersion) > 0).toList());
            if (versions.isEmpty()) {
                JOptionPane.showMessageDialog(Main.WINDOW, "There are no later versions to upgrade to.", "No available versions", JOptionPane.ERROR_MESSAGE);
            } else {
                // Create table showing which components are upgradable
                String[] headings = {"Version", server.modLoader.toString()};
                DefaultTableModel model = new DefaultTableModel(0, 2) {
                    @Override
                    public String getColumnName(int column) {
                        return headings[column];
                    }
                };
                for (String version : versions) {
                    model.addRow(new Object[]{version, server.modLoader.supportsVersion(version) ? Color.GREEN : Color.RED});
                }
                ColouredCellRenderer renderer = new ColouredCellRenderer();
                JTable table = new JTable(model) {
                    @Override
                    public TableCellRenderer getCellRenderer(int row, int column) {
                        if (column == 0) {
                            return super.getCellRenderer(row, column);
                        }
                        return renderer;
                    }

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                table.setPreferredScrollableViewportSize(new Dimension(500, 300));
                table.setFillsViewportHeight(true);
                JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                int result = JOptionPane.showConfirmDialog(Main.WINDOW, scrollPane, "Select a version to update to", JOptionPane.OK_CANCEL_OPTION);
                if (result == 0) {
                    String newVersion = versions.get(table.getSelectedRow());
                    LOGGER.info("Updating server %s to version %s".formatted(server.serverName, newVersion));
                    // Update mod loader
                    server.modLoader.updateFiles(newVersion, server);
                }
            }
        });
        this.add(updateBtn, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(30, 0, 0, 5), 0, 0));
        // Delete button
        JButton deleteBtn = createDeleteBtn();
        this.add(deleteBtn, new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(30, 0, 0, 10), 0, 0));
        // Tabbed pane
        basicPanel = new BasicPanel(this);
        tabbedPane.add("General", basicPanel);
        for (AdvancedPanel panel : advancedPanels) {
            tabbedPane.add(panel.name, panel);
        }
        modsPanel = new ModsPanel();
        tabbedPane.add("Mods", modsPanel);
        this.add(tabbedPane, new GridBagConstraints(1, 2, 4, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
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
        // Mods panel
        tabbedPane.setEnabledAt(advancedPanels.length + 1, server.modLoader.supportsMods());
        modsPanel.setServer(server);
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
