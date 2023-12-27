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
//        // Padding panel
//        this.add(new JPanel(){
//            @Override
//            public void paint(Graphics g) {
//                super.paint(g);
//                g.setColor(Color.RED);
//                g.drawRect(1, 1, this.getWidth(), this.getHeight());
//            }
//        }, new GridBagConstraints(1, 3, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
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
            // Property name
            JLabel nameLbl = new JLabel("Select a property");
            
        }

        private void setServer(Server server) {
            this.properties = new ServerPropertiesManager(server.getPropertiesLocation());
            this.propertiesList.setModel(this.properties);
        }

//        @Override
//        public void paint(Graphics g) {
//            super.paint(g);
//            g.setColor(Color.RED);
//            g.drawRect(0, 0, getWidth(), getHeight());
//        }
    }
}
