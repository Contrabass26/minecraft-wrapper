package me.jsedwards.createserver;

import me.jsedwards.Main;
import me.jsedwards.util.OSUtils;
import me.jsedwards.util.UnifiedListenerTextField;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class LocationStagePanel extends ValidatedStage {

    private final JTextField textField;
    private final JLabel validationLabel;

    public LocationStagePanel() {
        super();
        // Layout
        this.setLayout(new GridBagLayout());
        // Label
        JLabel label = new JLabel("Server location:");
        label.setFont(Main.MAIN_FONT.deriveFont(18f));
        this.add(label, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 20), 0, 0));
        // Text field
        this.textField = new UnifiedListenerTextField() {
            @Override
            protected void update() {
                String text = LocationStagePanel.this.textField.getText();
                if (text.isEmpty()) {
                    LocationStagePanel.this.validationLabel.setText("No location entered");
                    LocationStagePanel.this.validationLabel.setForeground(Color.RED);
                    return;
                }
                final Path path = new File(text).toPath();
                if (!Files.exists(path)) {
                    LocationStagePanel.this.validationLabel.setText("Path does not exist");
                    LocationStagePanel.this.validationLabel.setForeground(Color.RED);
                    return;
                }
                if (!Files.isDirectory(path)) {
                    LocationStagePanel.this.validationLabel.setText("Not a directory");
                    LocationStagePanel.this.validationLabel.setForeground(Color.RED);
                    return;
                }
                try (Stream<Path> files = Files.list(path))  {
                    List<Path> children = files.toList();
                    if (!children.isEmpty()) {
                        for (Path child : children) {
                            if (!child.endsWith(".DS_Store")) {
                                LocationStagePanel.this.validationLabel.setText("Directory is not empty - files may be overwritten");
                                LocationStagePanel.this.validationLabel.setForeground(Color.ORANGE);
                                return;
                            }
                        }
                    }
                } catch (IOException e) {
                    LocationStagePanel.this.validationLabel.setText("Unable to parse path");
                    LocationStagePanel.this.validationLabel.setForeground(Color.RED);
                }
                LocationStagePanel.this.validationLabel.setText("Valid location");
                LocationStagePanel.this.validationLabel.setForeground(Color.GREEN);
            }
        };
        this.add(this.textField, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Choose folder button
        JButton chooseFolderButton = new JButton("Choose");
        chooseFolderButton.addActionListener(e -> {
            String location = TinyFileDialogs.tinyfd_selectFolderDialog("Choose a folder", OSUtils.userHome);
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
    public boolean validateStage() {
        String text = LocationStagePanel.this.textField.getText();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No server location entered", "Invalid options", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        final Path path = new File(text).toPath();
        if (!Files.exists(path)) {
            JOptionPane.showMessageDialog(null, "Server location does not exist", "Invalid options", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!Files.isDirectory(path)) {
            JOptionPane.showMessageDialog(null, "Server location is not a directory", "Invalid options", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try (Stream<Path> files = Files.list(path))  {
            List<Path> children = files.toList();
            if (!children.isEmpty()) {
                for (Path child : children) {
                    if (!child.endsWith(".DS_Store")) {
                        return JOptionPane.showConfirmDialog(null, "Server location is not empty - files may be overwritten", "Questionable options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Server location is not valid", "Invalid options", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}