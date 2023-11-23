package me.jsedwards.gui.createserver;

import me.jsedwards.Main;
import me.jsedwards.MinecraftWrapperUtils;
import me.jsedwards.gui.UnifiedListenerTextField;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.swing.*;
import java.awt.*;

public class LocationStagePanel extends ValidatedStage {

    private final JTextField textField;
    private final JLabel validationLabel;

    public LocationStagePanel() {
        super();
        // Layout
        this.setLayout(new GridBagLayout());
        // Label
        JLabel label = new JLabel("Server location:");
        label.setFont(Main.MAIN_FONT);
        this.add(label, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 20), 0, 0));
        // Text field
        this.textField = new UnifiedListenerTextField() {
            @Override
            protected void update() {
                String text = LocationStagePanel.this.textField.getText();
                if (text.length() == 0) {
                    LocationStagePanel.this.validationLabel.setText("No location entered");
                    LocationStagePanel.this.validationLabel.setForeground(Color.RED);
                } else {
                    LocationStagePanel.this.validationLabel.setText("Valid location");
                    LocationStagePanel.this.validationLabel.setForeground(Color.GREEN);
                }
            }
        };
        this.add(this.textField, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Choose folder button
        JButton chooseFolderButton = new JButton("Choose");
        chooseFolderButton.addActionListener(e -> {
            String location = TinyFileDialogs.tinyfd_selectFolderDialog("Choose a folder", MinecraftWrapperUtils.getUserFolder());
            LocationStagePanel.this.textField.setText(location);
        });
        this.add(chooseFolderButton, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        // Validation label
        validationLabel = new JLabel("No location entered");
        validationLabel.setForeground(Color.RED);
        this.add(validationLabel, new GridBagConstraints(1, 2, 3, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
    }

    public String getServerLocation() {
        return textField.getText().strip();
    }

    @Override
    public boolean isStageValid() {
        return this.textField.getText().length() != 0;
    }
}