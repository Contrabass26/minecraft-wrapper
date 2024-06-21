package me.jsedwards.configserver;

import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import me.jsedwards.util.UnifiedListenerTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AdvancedPanel extends JPanel {

    protected static final Logger LOGGER = LogManager.getLogger();

    private final SidePanel sidePanel;
    private final JList<ConfigProperty> propertiesList;
    private final List<ConfigProperty> allProperties = new ArrayList<>();
    private final DefaultListModel<ConfigProperty> filteredProperties = new DefaultListModel<>();
    public final ConfigManager configManager;
    private Server server = null;

    protected AdvancedPanel(ConfigManager configManager) {
        this.configManager = configManager;
        this.setLayout(new GridBagLayout());
        // Search label
        this.add(new JLabel("Search properties:"), new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        // Search box
        JTextField searchBox = new UnifiedListenerTextField() {
            @Override
            protected void update() {
                filteredProperties.clear();
                String text = this.getText();
                for (ConfigProperty property : allProperties) {
                    if (property.key.contains(text)) {
                        filteredProperties.add(filteredProperties.size(), property);
                    }
                }
                AdvancedPanel.this.updateList();
            }
        };
        this.add(searchBox, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // List box
        propertiesList = new JList<>(filteredProperties);
        JScrollPane scrollPane = new JScrollPane(propertiesList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, new GridBagConstraints(1, 2, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        // Side panel
        sidePanel = new SidePanel();
        this.add(sidePanel, new GridBagConstraints(3, 1, 1, 2, 0.3, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        // Selection listener
        propertiesList.addListSelectionListener(e -> sidePanel.update());
    }

    public List<ConfigProperty> getOptimisable() {
        return allProperties.stream().filter(ConfigProperty::canOptimise).toList();
    }

    public boolean isEnabled(Server server) {
        return configManager.isEnabled(server);
    }

    public void setServer(Server server) {
        this.server = server;
        allProperties.clear();
        filteredProperties.clear();
        configManager.addKeys(allProperties, server);
        Collections.sort(allProperties);
    }

    private void updateList() {
        propertiesList.invalidate();
        propertiesList.repaint();
        sidePanel.update();
    }

    public final void optimise(int sliderValue) {
        for (ConfigProperty property : allProperties) {
            if (server.keysToOptimise.getOrDefault(property, true)) {
                property.value = configManager.optimise(sliderValue, property);
            }
        }
    }

    public void save() {
        configManager.save(allProperties, server);
    }

    private class SidePanel extends JPanel {

        private final JLabel nameLbl;
        private final JLabel dataTypeLbl;
        private final JLabel defaultValueLbl;
        private final JTextPane descriptionLbl;

        public SidePanel() {
            super();
            setLayout(new GridBagLayout());
            // Property name
            nameLbl = new JLabel("Select a property");
            nameLbl.setFont(Main.MAIN_FONT.deriveFont(18f));
            this.add(nameLbl, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
            // Data type
            dataTypeLbl = new JLabel("Data type:");
            this.add(dataTypeLbl, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 0, 0), 0, 0));
            // Default value
            defaultValueLbl = new JLabel("Default value:");
            this.add(defaultValueLbl, new GridBagConstraints(1, 3, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 0, 0), 0, 0));
            // Property description
            descriptionLbl = new JTextPane(new DefaultStyledDocument());
            descriptionLbl.setContentType("text/html");
            descriptionLbl.setEditable(false);
            this.add(descriptionLbl, new GridBagConstraints(1, 4, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(20, 0, 0, 0), 0, 0));
            // Edit button
            JButton editBtn = createEditButton();
            this.add(editBtn, new GridBagConstraints(1, 5, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 0, 0), 0, 0));
            // Padding
            this.add(new JPanel(), new GridBagConstraints(1, 6, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int) (AdvancedPanel.this.getWidth() * 0.05), AdvancedPanel.this.getHeight());
        }

        private JButton createEditButton() {
            JButton editBtn = new JButton("Edit");
            editBtn.addActionListener(e -> {
                ConfigProperty selected = AdvancedPanel.this.propertiesList.getSelectedValue();
                selected.edit();
                AdvancedPanel.this.updateList();
            });
            return editBtn;
        }

        private void update() {
            ConfigProperty selectedItem = AdvancedPanel.this.propertiesList.getSelectedValue();
            if (selectedItem != null) {
                this.nameLbl.setText(selectedItem.key);
                this.descriptionLbl.setText("<html>" + selectedItem.getDescription() + "</html>");
                this.dataTypeLbl.setText("Data type: " + selectedItem.type);
                this.defaultValueLbl.setText("Default value: " + selectedItem.getDefaultValue());
            }
        }
    }
}
