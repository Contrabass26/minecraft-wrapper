package me.jsedwards.createserver;

import me.jsedwards.Main;
import me.jsedwards.util.MathUtils;
import me.jsedwards.util.OSUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

public class MemoryStagePanel extends ValidatedStage {

    private final JSlider slider;

    public MemoryStagePanel() {
        super();
        setLayout(new GridBagLayout());
        // Title label
        JLabel titleLbl = new JLabel("Memory allocation:");
        titleLbl.setFont(Main.MAIN_FONT.deriveFont(18f));
        this.add(titleLbl, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 20), 0, 0));
        // Snap checkbox
        JCheckBox checkbox = new JCheckBox("Snap to GB", true);
        checkbox.setHorizontalAlignment(SwingConstants.RIGHT);
        // Feedback label
        JLabel feedbackLbl = new JLabel("Valid memory allocation");
        feedbackLbl.setForeground(Color.GREEN);
        // Slider
        int mbMemory = (int) MathUtils.roundToNearestMultiple(OSUtils.totalMemoryBytes / 1048576f, 1024);
        slider = new JSlider(1024, mbMemory - 2048); // Minimum 1GB, maximum 2GB less than total
        slider.setMajorTickSpacing(1024);
        slider.setSnapToTicks(true);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = slider.getMinimum(); i <= slider.getMaximum(); i += 1024) {
            labelTable.put(i, new JLabel(i / 1024 + "GB"));
        }
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            if (slider.getValue() < 2048) {
                feedbackLbl.setText("You probably need more memory than that");
                feedbackLbl.setForeground(Color.ORANGE);
            } else if (slider.getMaximum() - slider.getValue() < 1024) {
                feedbackLbl.setText("Other programs probably need more memory than that");
                feedbackLbl.setForeground(Color.ORANGE);
            } else {
                feedbackLbl.setText("Valid memory allocation");
                feedbackLbl.setForeground(Color.GREEN);
            }
        });
        // Checkbox listener
        checkbox.addChangeListener(e -> {
            slider.setSnapToTicks(checkbox.isSelected());
            if (checkbox.isSelected()) {
                slider.setValue((int) MathUtils.roundToNearestMultiple(slider.getValue(), 1024));
            }
        });
        // Add checkbox, slider and feedback label
        this.add(slider, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(checkbox, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(feedbackLbl, new GridBagConstraints(1, 2, 3, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
    }

    @Override
    public boolean validateStage() {
        if (slider.getValue() < 2048) {
            return JOptionPane.showConfirmDialog(null, "You probably need more memory than that", "Questionable options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
        } else if (slider.getMaximum() - slider.getValue() < 1024) {
            return JOptionPane.showConfirmDialog(null, "Other programs will probably need more memory", "Questionable options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
        }
        return true;
    }

    public int getMbMemory() {
        return slider.getValue();
    }
}
