package me.jsedwards.gui;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {


    public CardPanel() {
        super();
        this.setLayout(new CardLayout());
        this.add("select_server", new ServerSelectPanel());
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
}
