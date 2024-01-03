package me.jsedwards.configserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import me.jsedwards.data.OperatorPlayer;
import me.jsedwards.data.WhitelistedPlayer;
import me.jsedwards.util.Identifier;
import me.jsedwards.util.MathUtils;
import me.jsedwards.util.OSUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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
    private final JsonConfigPanel jsonConfigPanel;
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
        // Whitelist and ops
        jsonConfigPanel = new JsonConfigPanel();
        this.add(jsonConfigPanel, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        // Optimisation options
        JLabel optimisationOptionsLbl = new JLabel("General optimisation options:");
        optimisationOptionsLbl.setFont(Main.MAIN_FONT.deriveFont(18f));
        this.add(optimisationOptionsLbl, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
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

    public void save() {
        ObjectMapper mapper = new ObjectMapper();
        // Whitelist
        List<WhitelistedPlayer> whitelistedPlayers = new ArrayList<>();
        jsonConfigPanel.whitelistModel.elements().asIterator().forEachRemaining(whitelistedPlayers::add);
        File whitelistFile = new File(server.serverLocation + "/whitelist.json");
        try {
            mapper.writeValue(whitelistFile, whitelistedPlayers);
            LOGGER.info("Saved whitelist to " + whitelistFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to save whitelist to " + whitelistFile.getAbsolutePath(), e);
        }
        // Operators
        List<OperatorPlayer> operatorPlayers = new ArrayList<>();
        jsonConfigPanel.opsModel.elements().asIterator().forEachRemaining(operatorPlayers::add);
        File opsFile = new File(server.serverLocation + "/ops.json");
        try {
            mapper.writeValue(opsFile, operatorPlayers);
            LOGGER.info("Saved operators to " + opsFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to save operators to " + opsFile.getAbsolutePath(), e);
        }
    }

    private static class JsonConfigPanel extends JPanel {

        private static final TypeReference<List<WhitelistedPlayer>> WHITELIST_TYPE_REFERENCE = new TypeReference<>() {};
        private static final TypeReference<List<OperatorPlayer>> OPS_TYPE_REFERENCE = new TypeReference<>() {};

        private final DefaultListModel<WhitelistedPlayer> whitelistModel;
        private final JList<WhitelistedPlayer> whitelist;
        private final DefaultListModel<OperatorPlayer> opsModel;
        private final JList<OperatorPlayer> opsList;

        public JsonConfigPanel() {
            super();
            setLayout(new GridBagLayout());
            // Whitelist
            JLabel whitelistLbl = new JLabel("Whitelist:");
            whitelistLbl.setFont(Main.MAIN_FONT.deriveFont(18f));
            whitelistModel = new DefaultListModel<>();
            whitelist = new JList<>(whitelistModel);
            JButton whitelistAddBtn = new JButton("Add player");
            whitelistAddBtn.addActionListener(e -> {
                String username = JOptionPane.showInputDialog(Main.WINDOW, "Enter player name:", "Add to whitelist", JOptionPane.QUESTION_MESSAGE);
                WhitelistedPlayer player = WhitelistedPlayer.create(username);
                whitelistModel.addElement(player);
            });
            JButton whitelistRemoveBtn = new JButton("Remove selected");
            whitelistRemoveBtn.addActionListener(e -> {
                for (int index : whitelist.getSelectedIndices()) {
                    whitelistModel.remove(index);
                }
                whitelist.invalidate();
                whitelist.repaint();
            });
            this.add(whitelistLbl, new GridBagConstraints(1, 1, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            this.add(whitelist, new GridBagConstraints(1, 2, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 0, 0, 5), 0, 0));
            this.add(whitelistAddBtn, new GridBagConstraints(1, 3, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 3), 0, 0));
            this.add(whitelistRemoveBtn, new GridBagConstraints(2, 3, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 3, 0, 5), 0, 0));
            // Ops
            JLabel opsLbl = new JLabel("Operators:");
            opsLbl.setFont(Main.MAIN_FONT.deriveFont(18f));
            opsModel = new DefaultListModel<>();
            opsList = new JList<>(opsModel);
            JButton opsAddBtn = new JButton("Add player");
            opsAddBtn.addActionListener(e -> {
                String username = JOptionPane.showInputDialog(Main.WINDOW, "Enter player name:", "Add operator", JOptionPane.QUESTION_MESSAGE);
                OperatorPlayer player = OperatorPlayer.create(username);
                opsModel.addElement(player);
            });
            JButton opsRemoveBtn = new JButton("Remove selected");
            opsRemoveBtn.addActionListener(e -> {
                for (int index : opsList.getSelectedIndices()) {
                    opsModel.remove(index);
                }
                opsList.invalidate();
                opsList.repaint();
            });
            this.add(opsLbl, new GridBagConstraints(3, 1, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            this.add(opsList, new GridBagConstraints(3, 2, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 5, 0, 0), 0, 0));
            this.add(opsAddBtn, new GridBagConstraints(3, 3, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 3), 0, 0));
            this.add(opsRemoveBtn, new GridBagConstraints(4, 3, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 3, 0, 0), 0, 0));
        }

        public void setServer(Server server) {
            ObjectMapper mapper = new ObjectMapper();
            // Whitelist
            File whitelistFile = new File(server.serverLocation + "/whitelist.json");
            try {
                whitelistModel.clear();
                mapper.readValue(whitelistFile, WHITELIST_TYPE_REFERENCE).forEach(whitelistModel::addElement);
                LOGGER.info("Loaded whitelist from " + whitelistFile.getAbsolutePath());
                whitelist.invalidate();
                whitelist.repaint();
            } catch (IOException e) {
                LOGGER.error("Failed to load whitelist from " + whitelistFile.getAbsolutePath(), e);
            }
            // Operators
            File opsFile = new File(server.serverLocation + "/ops.json");
            try {
                opsModel.clear();
                mapper.readValue(opsFile, OPS_TYPE_REFERENCE).forEach(opsModel::addElement);
                LOGGER.info("Loaded operators from " + opsFile.getAbsolutePath());
                opsList.invalidate();
                opsList.repaint();
            } catch (IOException e) {
                LOGGER.error("Failed to load operators from " + opsFile.getAbsolutePath(), e);
            }
        }

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
            Identifier key = new Identifier(checkBox.getText());
            checkBox.setSelected(serverConfigPanel.isKeyOptimised(key));
            checkBox.setEnabled(serverConfigPanel.isNamespaceEnabled(key.namespace));
        }
        jsonConfigPanel.setServer(server);
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
