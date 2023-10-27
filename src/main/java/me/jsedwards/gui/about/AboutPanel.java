package me.jsedwards.gui.about;

import me.jsedwards.Main;

import javax.swing.*;

public class AboutPanel extends JPanel {

    public AboutPanel() {
        super();
        // Layout
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        // Inner panel
        this.add(Box.createHorizontalGlue());
        this.add(new InnerPanel());
        this.add(Box.createHorizontalGlue());
    }

    private static class InnerPanel extends JPanel {

        private InnerPanel() {
            super();
            // Layout
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            // Title label
            JLabel titleLbl = new JLabel(Main.NAME);
            titleLbl.setFont(titleLbl.getFont().deriveFont(26f));
            this.add(Box.createVerticalGlue());
            this.add(titleLbl);
            // Creator label
            JLabel creatorLbl = new JLabel(Main.CREATOR);
            this.add(creatorLbl);
            this.add(Box.createVerticalGlue());
        }
    }
}
