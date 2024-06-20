package me.jsedwards.configserver;

import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import me.jsedwards.util.Identifier;
import me.jsedwards.util.UnifiedListenerTextField;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AdvancedPanel extends JPanel {

    private final SidePanel sidePanel;
    private final Function<Server, ConfigManager> configManagerCreator;
    private final Predicate<Server> enabled;
    private ConfigManager properties;
    private final JList<ConfigProperty> propertiesList;
    public final String name;

    public AdvancedPanel(Function<Server, ConfigManager> configManagerCreator, String name, Predicate<Server> enabled) {
        this.name = name;
        this.configManagerCreator = configManagerCreator;
        this.enabled = enabled;
        this.setLayout(new GridBagLayout());
        properties = configManagerCreator.apply(null);
        // Search label
        this.add(new JLabel("Search properties:"), new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        // Search box
        JTextField searchBox = new UnifiedListenerTextField() {
            @Override
            protected void update() {
                properties.updateSearch(this.getText());
                AdvancedPanel.this.updateList();
            }
        };
        this.add(searchBox, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // List box
        propertiesList = new JList<>(properties);
        JScrollPane scrollPane = new JScrollPane(propertiesList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, new GridBagConstraints(1, 2, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        // Side panel
        sidePanel = new SidePanel();
        this.add(sidePanel, new GridBagConstraints(3, 1, 1, 2, 0.3, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        // Selection listener
        propertiesList.addListSelectionListener(e -> sidePanel.update());
    }

    public boolean isEnabled(Server server) {
        return enabled.test(server);
    }

    public void setServer(Server server) {
        this.properties = configManagerCreator.apply(server);
        this.propertiesList.setModel(this.properties);
        server.keysToOptimise.forEach((key, optimise) -> properties.setKeyOptimised(key.path, optimise));
    }

    private void updateList() {
        propertiesList.invalidate();
        propertiesList.repaint();
        sidePanel.update();
    }

    public Set<Identifier> getPropertiesToOptimise() {
        return properties.getKeysToOptimise().stream().map(s -> new Identifier(name, s)).collect(Collectors.toSet());
    }

    public void setKeyOptimised(Identifier key, boolean selected) {
        properties.setKeyOptimised(key.path, selected);
    }

    public boolean isKeyOptimised(Identifier key) {
        return properties.isKeyOptimised(key.path);
    }

    public void saveProperties() {
        properties.save();
    }

    public void optimise(int sliderValue) {
        properties.optimise(sliderValue);
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
                int splitIndex = selected.indexOf(':');
                String key = selected.substring(0, splitIndex);
                String value = JOptionPane.showInputDialog(editBtn, "Enter new value for %s:".formatted(key), "Edit value", JOptionPane.QUESTION_MESSAGE);
                AdvancedPanel.this.properties.set(key, value);
                AdvancedPanel.this.updateList();
            });
            return editBtn;
        }

        private void update() {
            ConfigProperty selectedItem = AdvancedPanel.this.propertiesList.getSelectedValue();
            if (selectedItem != null) {
                String key = selectedItem.substring(0, selectedItem.indexOf(':'));
                this.nameLbl.setText(key);
                this.descriptionLbl.setText("<html>" + AdvancedPanel.this.properties.getDescription(key) + "</html>");
                this.dataTypeLbl.setText("Data type: " + AdvancedPanel.this.properties.getDataType(key));
                this.defaultValueLbl.setText("Default value: " + AdvancedPanel.this.properties.getDefaultValue(key));
            }
        }
    }
}
