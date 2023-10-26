package me.jsedwards;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {

    public CardPanel() {
        super();
        CardLayout layoutManager = new CardLayout();
        this.setLayout(layoutManager);
        ServerSelectPanel serverSelectPanel = new ServerSelectPanel();
        this.add("server_select", serverSelectPanel);
    }
}
