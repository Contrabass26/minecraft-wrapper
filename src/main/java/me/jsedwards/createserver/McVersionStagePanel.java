package me.jsedwards.createserver;

import me.jsedwards.Main;

import javax.swing.*;
import java.awt.*;

public class McVersionStagePanel extends ValidatedStage {

    private static final String[] VERSIONS = {"1.8.9", "1.9", "1.9.1", "1.9.2", "1.9.3", "1.9.4", "1.10", "1.10.1", "1.10.2", "1.11", "1.11.1", "1.11.2", "1.12", "1.12.1", "1.12.2", "1.13", "1.13.1", "1.13.2", "1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4", "1.15", "1.15.1", "1.15.2", "1.16", "1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5", "1.17", "1.17.1", "1.18", "1.18.1", "1.18.2", "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4", "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4"};

    private final JComboBox<String> comboBox;

    public McVersionStagePanel() {
        super();
        // Layout
        this.setLayout(new GridBagLayout());
        // Label
        JLabel label = new JLabel("Minecraft version:");
        label.setFont(Main.MAIN_FONT.deriveFont(18f));
        this.add(label, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        // Combo box
        comboBox = new JComboBox<>(VERSIONS);
        this.add(comboBox, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 0), 0, 0));
    }

    public String getSelectedVersion() {
        return (String) this.comboBox.getSelectedItem();
    }

    @Override
    public boolean isStageValid() {
        return true;
    }
}
