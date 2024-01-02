package me.jsedwards.configserver;

import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import me.jsedwards.util.Identifier;
import me.jsedwards.util.MathUtils;
import me.jsedwards.util.OSUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

class BasicPanel extends JPanel {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ServerConfigPanel serverConfigPanel;
    private final JSlider memorySlider;
    private final JSlider optimiseSlider;
    private final List<JCheckBox> checkBoxes;
    private Server server = null;

    public BasicPanel(ServerConfigPanel serverConfigPanel) {
        super();
        this.serverConfigPanel = serverConfigPanel;
        this.setLayout(new GridBagLayout());
        // Optimisation slider label
        JLabel optimiseLbl = new JLabel("General optimisation:");
        optimiseLbl.setFont(Main.MAIN_FONT.deriveFont(18f));
        this.add(optimiseLbl, new GridBagConstraints(1, 1, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // General optimisation slider
        optimiseSlider = createSlider();
        this.add(optimiseSlider, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Slider labels
        JLabel lowerLbl = new JLabel("More performance");
        lowerLbl.setHorizontalAlignment(SwingConstants.LEFT);
        this.add(lowerLbl, new GridBagConstraints(1, 3, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        JLabel upperLbl = new JLabel("Better experience");
        upperLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        this.add(upperLbl, new GridBagConstraints(2, 3, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Memory slider label
        JLabel memoryLbl = new JLabel("Memory allocated:");
        memoryLbl.setFont(Main.MAIN_FONT.deriveFont(18f));
        this.add(memoryLbl, new GridBagConstraints(1, 4, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Memory snap checkbox
        JCheckBox memorySnapCheckbox = new JCheckBox("Snap to GB", true);
        memorySnapCheckbox.setHorizontalAlignment(SwingConstants.RIGHT);
        // Memory slider
        int mbMemory = (int) MathUtils.roundToNearestMultiple(OSUtils.totalMemoryBytes / 1048576f, 1024);
        memorySlider = new JSlider(1024, mbMemory - 2048); // Minimum 1GB, maximum 2GB less than total
        memorySlider.setMajorTickSpacing(1024);
        memorySlider.setSnapToTicks(true);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = memorySlider.getMinimum(); i <= memorySlider.getMaximum(); i += 1024) {
            labelTable.put(i, new JLabel(i / 1024 + "GB"));
        }
        memorySlider.setLabelTable(labelTable);
        memorySlider.setPaintLabels(true);
        memorySlider.addChangeListener(e -> server.mbMemory = memorySlider.getValue());
        // Finalise checkbox, then add checkbox and slider
        memorySnapCheckbox.addChangeListener(e -> {
            memorySlider.setSnapToTicks(memorySnapCheckbox.isSelected());
            if (memorySnapCheckbox.isSelected()) {
                memorySlider.setValue((int) MathUtils.roundToNearestMultiple(memorySlider.getValue(), 1024));
            }
        });
        this.add(memorySnapCheckbox, new GridBagConstraints(2, 4, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(memorySlider, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Optimisation options
        List<Identifier> keysToOptimise = new ArrayList<>(serverConfigPanel.getKeysToOptimise());
        Collections.sort(keysToOptimise);
        checkBoxes = new ArrayList<>();
        CheckBoxMatrixPanel optimisationPanel = new CheckBoxMatrixPanel();
        for (Identifier key : keysToOptimise) {
            JCheckBox checkBox = new JCheckBox(key.toString());
            checkBoxes.add(checkBox);
            checkBox.addActionListener(e -> serverConfigPanel.setKeyOptimised(key, checkBox.isSelected()));
            optimisationPanel.addOption(checkBox);
        }
        this.add(optimisationPanel, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Padding
        this.add(new JPanel(), new GridBagConstraints(1, GridBagConstraints.RELATIVE, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    private static class CheckBoxMatrixPanel extends JPanel {

        private static final int COLUMNS;
        static {
            COLUMNS = Toolkit.getDefaultToolkit().getScreenSize().width / 320;
            LOGGER.info("Using %s columns for CheckBoxMatrixPanel".formatted(COLUMNS));
        }

        private int count = -1;

        public CheckBoxMatrixPanel() {
            super();
            setLayout(new GridBagLayout());
        }

        public void addOption(JCheckBox checkBox) {
            count++;
//            LOGGER.debug("Added check box %s at (%s, %s)".formatted(checkBox.getText(), count % COLUMNS, Math.floorDiv(count, COLUMNS)));
            this.add(checkBox, new GridBagConstraints(
                    count % COLUMNS,
                    Math.floorDiv(count, COLUMNS),
                    1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        }
    }

    public void setServer(Server server) {
        this.server = server;
        memorySlider.setValue(server.mbMemory);
        optimiseSlider.setValue(server.optimisationLevel);
        for (JCheckBox checkBox : checkBoxes) {
            checkBox.setSelected(serverConfigPanel.isKeyOptimised(new Identifier(checkBox.getText())));
        }
    }

    private JSlider createSlider() {
        JSlider slider = new JSlider(0, 100);
        slider.addChangeListener(e -> {
            int sliderValue = slider.getValue();
            serverConfigPanel.optimiseConfigs(sliderValue);
            server.optimisationLevel = sliderValue;
        });
        return slider;
    }
}
