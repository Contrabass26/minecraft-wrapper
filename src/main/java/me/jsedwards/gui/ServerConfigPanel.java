package me.jsedwards.gui;

import me.jsedwards.ConfigManager;
import me.jsedwards.Main;
import me.jsedwards.ServerPropertiesManager;
import me.jsedwards.SpigotConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ServerConfigPanel extends JPanel implements Card {

    private static final Logger LOGGER = LogManager.getLogger();

    private String server = null;
    private final JLabel serverNameLbl;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final AdvancedPanel[] advancedPanels = {
            new AdvancedPanel(ServerPropertiesManager::new, "Vanilla", s -> true),
            new AdvancedPanel(SpigotConfigManager::new, "Spigot", s -> true)
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
            panel.properties.save();
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
                    panel.properties.optimise(sliderValue);
                }
            });
            return slider;
        }
    }

    private static class AdvancedPanel extends JPanel {

        private final SidePanel sidePanel;
        private final Function<Server, ConfigManager> configManagerCreator;
        private final Predicate<Server> enabled;
        private ConfigManager properties;
        private final JList<String> propertiesList;
        public final String name;

        public AdvancedPanel(Function<Server, ConfigManager> configManagerCreator, String name, Predicate<Server> enabled) {
            this.name = name;
            this.configManagerCreator = configManagerCreator;
            this.enabled = enabled;
            this.setLayout(new GridBagLayout());
            properties = configManagerCreator.apply(null);
            // Search label
            this.add(new JLabel("Search properties:"), new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            // Search box
            JTextField searchBox = new UnifiedListenerTextField() {
                @Override
                protected void update() {
                    properties.updateSearch(this.getText());
                    AdvancedPanel.this.updateList();
                }
            };
            this.add(searchBox, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            // List box
            propertiesList = new JList<>(properties);
            JScrollPane scrollPane = new JScrollPane(propertiesList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.add(scrollPane, new GridBagConstraints(1, 2, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            // Side panel
            sidePanel = new SidePanel();
            this.add(sidePanel, new GridBagConstraints(3, 1, 1, 2, 0.3, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            // Selection listener
            propertiesList.addListSelectionListener(e -> sidePanel.update());
        }

        private boolean isEnabled(Server server) {
            return enabled.test(server);
        }

        private void setServer(Server server) {
            this.properties = configManagerCreator.apply(server);
            this.propertiesList.setModel(this.properties);
        }

        private void updateList() {
            propertiesList.invalidate();
            propertiesList.repaint();
            sidePanel.update();
        }

        private class SidePanel extends JPanel {

            private final JLabel nameLbl;
            private final JLabel dataTypeLbl;
            private final JLabel defaultValueLbl;
            private final JTextPane descriptionLbl;

            public SidePanel() {
                super();
                setLayout(new GridBagLayout());
                // Property name
                nameLbl = new JLabel("Select a property");
                nameLbl.setFont(Main.MAIN_FONT.deriveFont(18f));
                this.add(nameLbl, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
                // Data type
                dataTypeLbl = new JLabel("Data type:");
                this.add(dataTypeLbl, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 0, 0), 0, 0));
                // Default value
                defaultValueLbl = new JLabel("Default value:");
                this.add(defaultValueLbl, new GridBagConstraints(1, 3, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 0, 0), 0, 0));
                // Property description
                descriptionLbl = new JTextPane(new DefaultStyledDocument());
                descriptionLbl.setContentType("text/html");
                descriptionLbl.setEditable(false);
                this.add(descriptionLbl, new GridBagConstraints(1, 4, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(20, 0, 0, 0), 0, 0));
                // Edit button
                JButton editBtn = createEditButton();
                this.add(editBtn, new GridBagConstraints(1, 5, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 0, 0), 0, 0));
                // Padding
                this.add(new JPanel(), new GridBagConstraints(1, 6, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension((int) (AdvancedPanel.this.getWidth() * 0.05), AdvancedPanel.this.getHeight());
            }

            private JButton createEditButton() {
                JButton editBtn = new JButton("Edit");
                editBtn.addActionListener(e -> {
                    String selected = AdvancedPanel.this.propertiesList.getSelectedValue();
                    int splitIndex = selected.indexOf(':');
                    String key = selected.substring(0, splitIndex);
                    String value = JOptionPane.showInputDialog(editBtn, "Enter new value for %s:".formatted(key), "Edit value", JOptionPane.QUESTION_MESSAGE);
                    AdvancedPanel.this.properties.set(key, value);
                    AdvancedPanel.this.updateList();
                });
                return editBtn;
            }

            private void update() {
                String selectedItem = AdvancedPanel.this.propertiesList.getSelectedValue();
                if (selectedItem != null) {
                    String key = selectedItem.substring(0, selectedItem.indexOf(':'));
                    this.nameLbl.setText(key);
                    this.descriptionLbl.setText("<html>" + AdvancedPanel.this.properties.getDescription(key) + "</html>");
                    this.dataTypeLbl.setText("Data type: " + AdvancedPanel.this.properties.getDataType(key));
                    this.defaultValueLbl.setText("Default value: " + AdvancedPanel.this.properties.getDefaultValue(key));
                }
            }
        }
    }
}
