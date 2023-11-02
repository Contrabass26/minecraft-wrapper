package me.jsedwards.gui.createserver;

import me.jsedwards.Main;

import javax.swing.*;
import java.awt.*;

public class McVersionStagePanel extends ValidatedStage {

    private static final String[] VERSIONS = {"1.8.9", "1.16.5", "1.20.2"};

    private final JComboBox<String> comboBox;

    public McVersionStagePanel() {
        super();
        // Layout
        this.setLayout(new GridBagLayout());
        // Label
        JLabel label = new JLabel("Minecraft version:");
        label.setFont(Main.MAIN_FONT);
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
