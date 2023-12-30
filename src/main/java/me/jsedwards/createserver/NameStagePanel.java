package me.jsedwards.createserver;

import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import me.jsedwards.util.UnifiedListenerTextField;

import javax.swing.*;
import java.awt.*;

public class NameStagePanel extends ValidatedStage {

    private final JTextField textField;
    private final JLabel validationLabel;

    public NameStagePanel() {
        super();
        // Layout
        this.setLayout(new GridBagLayout());
        // Label
        JLabel label = new JLabel("Server name:");
        label.setFont(Main.MAIN_FONT);
        this.add(label, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 20), 0, 0));
        // Text field
        this.textField = new UnifiedListenerTextField() {
            @Override
            protected void update() {
                String text = NameStagePanel.this.textField.getText();
                if (text.isEmpty()) {
                    NameStagePanel.this.validationLabel.setText("No name entered");
                    NameStagePanel.this.validationLabel.setForeground(Color.RED);
                } else if (Server.exists(text)) {
                    NameStagePanel.this.validationLabel.setText("Name is already taken");
                    NameStagePanel.this.validationLabel.setForeground(Color.RED);
                } else if (text.contains("\\")) {
                    NameStagePanel.this.validationLabel.setText("Name must not contain escape codes");
                    NameStagePanel.this.validationLabel.setForeground(Color.RED);
                } else {
                    NameStagePanel.this.validationLabel.setText("Valid server name");
                    NameStagePanel.this.validationLabel.setForeground(Color.GREEN);
                }
            }
        };
        this.add(this.textField, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Validation label
        validationLabel = new JLabel("No name entered");
        validationLabel.setForeground(Color.RED);
        this.add(validationLabel, new GridBagConstraints(1, 2, 2, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
    }

    public String getServerName() {
        return textField.getText().strip();
    }

    public boolean isStageValid() {
        String text = textField.getText();
        return !text.isEmpty() && !Server.exists(text);
    }
}
