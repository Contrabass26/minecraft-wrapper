package me.jsedwards;

import javax.swing.*;
import java.awt.*;

public class ServerSelectPanel extends JPanel {

    public ServerSelectPanel() {
        super();
        this.setLayout(new GridBagLayout());
        JLabel label = new JLabel("Select a server:");
        label.setFont(Main.FONT);
        this.add(label, new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(30, 10, 0, 0), 0, 0));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.RED);
        g.drawRect(0, 0, this.getWidth(), this.getHeight());
    }
}
