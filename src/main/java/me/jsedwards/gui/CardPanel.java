package me.jsedwards.gui;

import me.jsedwards.gui.createserver.ServerCreatePanel;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {

    public final ServerSelectPanel serverSelectPanel;
    public final ServerConfigPanel serverConfigPanel;
    private final ServerCreatePanel serverCreatePanel;
    private Card current;

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
        serverCreatePanel = new ServerCreatePanel();
        JScrollPane scrollPane = new JScrollPane(serverCreatePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add("create_server", scrollPane);
        // Set current
        current = this.serverSelectPanel;
    }

    public void addServerCard(Server server) {
        this.add("server_" + server.serverName, server);
    }

    public void switchToServerSelect() {
        exitCurrent();
        this.getCardLayout().show(this, "select_server");
        current = this.serverSelectPanel;
    }

    private CardLayout getCardLayout() {
        return (CardLayout) this.getLayout();
    }

    public void switchToServer(String serverName) {
        exitCurrent();
        this.getCardLayout().show(this, "server_" + serverName);
        current = null;
    }

    public void switchToServerCreation() {
        exitCurrent();
        this.getCardLayout().show(this, "create_server");
        current = this.serverCreatePanel;
    }

    public void switchToServerConfig(String server) {
        exitCurrent();
        this.serverConfigPanel.setServer(server);
        this.getCardLayout().show(this, "config_server");
        current = this.serverConfigPanel;
    }

    public void exitCurrent() {
        if (current != null) {
            current.exit();
        }
    }
}
