package me.jsedwards.gui;

import me.jsedwards.Main;
import me.jsedwards.ServerPropertiesManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class ServerConfigPanel extends JPanel {

    private static final Logger LOGGER = LogManager.getLogger();

    private String server = null;
    private final JLabel serverNameLbl;
    private final PropertiesPanel propertiesPanel = new PropertiesPanel();

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
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Properties", propertiesPanel);
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
        propertiesPanel.setServer(server);
    }

    private static class PropertiesPanel extends JPanel {

        private ServerPropertiesManager properties = new ServerPropertiesManager();
        private final JList<String> propertiesList;

        public PropertiesPanel() {
            this.setLayout(new GridBagLayout());
            // List box
            propertiesList = new JList<>(properties);
            JScrollPane scrollPane = new JScrollPane(propertiesList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.add(scrollPane, new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            // Side panel
            SidePanel sidePanel = new SidePanel();
            this.add(sidePanel, new GridBagConstraints(2, 1, 1, 1, 0.3, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 5, 0, 0), 0, 0));
            // Selection listener
            propertiesList.addListSelectionListener(e -> sidePanel.update());
        }

        private void setServer(Server server) {
            this.properties = new ServerPropertiesManager(server.getPropertiesLocation());
            this.propertiesList.setModel(this.properties);
        }

        private class SidePanel extends JPanel {

            private final JLabel nameLbl;

            public SidePanel() {
                super();
                setLayout(new GridBagLayout());
                // Property name
                nameLbl = new JLabel("Select a property...");
                nameLbl.setFont(Main.MAIN_FONT.deriveFont(18f));
                this.add(nameLbl, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                // Edit button
                JButton editBtn = createEditButton();
                this.add(editBtn, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
                // Padding
                this.add(new JPanel(), new GridBagConstraints(1, 3, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                // Save button
                JButton saveBtn = new JButton("Save");
                saveBtn.addActionListener(e -> PropertiesPanel.this.properties.save());
                this.add(saveBtn, new GridBagConstraints(1, 4, 1, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            }

            private JButton createEditButton() {
                JButton editBtn = new JButton("Edit");
                editBtn.addActionListener(e -> {
                    String selected = PropertiesPanel.this.propertiesList.getSelectedValue();
                    int splitIndex = selected.indexOf(':');
                    String key = selected.substring(0, splitIndex);
                    String value = JOptionPane.showInputDialog(editBtn, "Enter new value for %s:".formatted(key), "Edit value", JOptionPane.QUESTION_MESSAGE);
                    PropertiesPanel.this.properties.set(key, value);
                    PropertiesPanel.this.propertiesList.invalidate();
                    PropertiesPanel.this.propertiesList.repaint();
                });
                return editBtn;
            }

            private void update() {
                String selectedItem = PropertiesPanel.this.propertiesList.getSelectedValue();
                if (selectedItem != null) {
                    this.nameLbl.setText(selectedItem.substring(0, selectedItem.indexOf(':')));
                }
            }
        }
    }
}
