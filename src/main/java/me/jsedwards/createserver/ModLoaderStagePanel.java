package me.jsedwards.createserver;

import me.jsedwards.Main;
import me.jsedwards.util.ModLoader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModLoaderStagePanel extends ValidatedStage {

    private final List<JRadioButton> radioButtons = new ArrayList<>();

    public ModLoaderStagePanel() {
        super();
        // Layout
        this.setLayout(new GridBagLayout());
        // Label
        JLabel label = new JLabel("Mod loader:");
        label.setFont(Main.MAIN_FONT);
        this.add(label, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        // Radio buttons
        ButtonGroup buttonGroup = new ButtonGroup();
        for (int i = 0; i < ModLoader.count(); i++) {
            JRadioButton radioButton = new JRadioButton();
            radioButton.setSelected(i == 0);
            radioButton.setText(ModLoader.get(i).toString());
            radioButtons.add(radioButton);
            buttonGroup.add(radioButton);
            this.add(radioButton, new GridBagConstraints(2 + i, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
        }
        // Padding
        JPanel padding = new JPanel();
        this.add(padding, new GridBagConstraints(ModLoader.count() + 2, 1, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }

    public ModLoader getSelectedModLoader() {
        for (JRadioButton radioButton : radioButtons) {
            if (radioButton.isSelected()) {
                return ModLoader.valueOf(radioButton.getText().toUpperCase());
            }
        }
        return null;
    }

    @Override
    public boolean isStageValid() {
        return true;
    }
}
