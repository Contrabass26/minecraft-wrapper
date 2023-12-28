package me.jsedwards.gui;

import me.jsedwards.gui.createserver.ServerCreatePanel;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {

    public final ServerSelectPanel serverSelectPanel;
    public final ServerConfigPanel serverConfigPanel;

    public CardPanel() {
        super();
        this.setLayout(new CardLayout());
        // Server select panel
        serverSelectPanel = new ServerSelectPanel();
        this.add("select_server", serverSelectPanel);
        // Server config panel
        this.serverConfigPanel = new ServerConfigPanel();
        this.add("config_server", serverConfigPanel);
        // Create server with scroll pane
        ServerCreatePanel serverCreatePanel = new ServerCreatePanel();
        JScrollPane scrollPane = new JScrollPane(serverCreatePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add("create_server", scrollPane);
        // Server dashboards
        Server.addCards(this);
    }

    public void addServerCard(Server server) {
        this.add("server_" + server.serverName, server);
    }

    public void switchToServerSelect() {
        this.getCardLayout().show(this, "select_server");
    }

    private CardLayout getCardLayout() {
        return (CardLayout) this.getLayout();
    }

    public void switchToServer(String serverName) {
        this.getCardLayout().show(this, "server_" + serverName);
    }

    public void switchToServerCreation() {
        this.getCardLayout().show(this, "create_server");
    }

    public void switchToServerConfig(String server) {
        this.serverConfigPanel.setServer(server);
        this.getCardLayout().show(this, "config_server");
    }
}
