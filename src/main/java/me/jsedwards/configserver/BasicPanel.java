package me.jsedwards.configserver;

import javax.swing.*;
import java.awt.*;

class BasicPanel extends JPanel {

    private final ServerConfigPanel serverConfigPanel;

    public BasicPanel(ServerConfigPanel serverConfigPanel) {
        super();
        this.serverConfigPanel = serverConfigPanel;
        this.setLayout(new GridBagLayout());
        // General optimisation slider
        JSlider slider = createSlider();
        this.add(slider, new GridBagConstraints(1, 1, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Slider labels
        JLabel lowerLbl = new JLabel("More performance");
        lowerLbl.setHorizontalAlignment(SwingConstants.LEFT);
        this.add(lowerLbl, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        JLabel upperLbl = new JLabel("Better experience");
        upperLbl.setHorizontalAlignment(SwingConstants.LEFT);
        this.add(upperLbl, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Padding
        this.add(new JPanel(), new GridBagConstraints(1, 3, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    private JSlider createSlider() {
        JSlider slider = new JSlider(0, 100);
        slider.addChangeListener(e -> {
            int sliderValue = slider.getValue();
            serverConfigPanel.optimiseConfigs(sliderValue);
        });
        return slider;
    }
}
