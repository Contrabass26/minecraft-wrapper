package me.jsedwards.configserver;

import me.jsedwards.Card;
import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class ServerConfigPanel extends JPanel implements Card {

    private static final Logger LOGGER = LogManager.getLogger();

    private String server = null;
    private final JLabel serverNameLbl;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final AdvancedPanel[] advancedPanels = {
            new AdvancedPanel(ServerPropertiesManager::new, "Vanilla", s -> true),
            new AdvancedPanel(SpigotConfigManager::new, "Spigot", s -> true),
            new AdvancedPanel(BukkitConfigManager::new, "Bukkit", s -> true)
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
        // Tabbed pane
        BasicPanel basicPanel = new BasicPanel();
        tabbedPane.add("General", basicPanel);
        for (AdvancedPanel panel : advancedPanels) {
            tabbedPane.add(panel.name, panel);
        }
        this.add(tabbedPane, new GridBagConstraints(1, 2, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
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
    }

    @Override
    public void exit() {
        for (AdvancedPanel panel : advancedPanels) {
            panel.saveProperties();
        }
    }

    private class BasicPanel extends JPanel {

        public BasicPanel() {
            super();
            this.setLayout(new GridBagLayout());
            // General optimisation slider
            JSlider slider = createSlider();
            this.add(slider, new GridBagConstraints(1, 1, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            // Slider labels
            JLabel lowerLbl = new JLabel("More performance");
            lowerLbl.setHorizontalAlignment(SwingConstants.LEFT);
            this.add(lowerLbl, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            JLabel upperLbl = new JLabel("Better experience");
            upperLbl.setHorizontalAlignment(SwingConstants.LEFT);
            this.add(upperLbl, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            // Padding
            this.add(new JPanel(), new GridBagConstraints(1, 3, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        }

        private JSlider createSlider() {
            JSlider slider = new JSlider(0, 100);
            slider.addChangeListener(e -> {
                int sliderValue = slider.getValue();
                for (AdvancedPanel panel : ServerConfigPanel.this.advancedPanels) {
                    panel.optimise(sliderValue);
                }
            });
            return slider;
        }
    }

}
