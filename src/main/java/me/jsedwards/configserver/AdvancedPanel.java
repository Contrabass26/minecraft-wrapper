package me.jsedwards.configserver;

import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.UnifiedListenerTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public abstract class AdvancedPanel extends JPanel {

    protected static final Logger LOGGER = LogManager.getLogger();

    private final SidePanel sidePanel;
    private final Predicate<Server> enabled;
    private final JList<ConfigProperty> propertiesList;
    private final List<ConfigProperty> allProperties = new ArrayList<>();
    private final Vector<ConfigProperty> filteredProperties = new Vector<>();
    public final String name;

    protected AdvancedPanel(String name, Predicate<Server> enabled) {
        this.name = name;
        this.enabled = enabled;
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
                        filteredProperties.add(property);
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

    public boolean isEnabled(Server server) {
        return enabled.test(server);
    }

    public void setServer(Server server) {
        allProperties.clear();
        filteredProperties.clear();
        addKeys(allProperties, server);
        Collections.sort(allProperties);
    }

    protected abstract void addKeys(List<ConfigProperty> list, Server server);

    private void updateList() {
        propertiesList.invalidate();
        propertiesList.repaint();
        sidePanel.update();
    }

    protected abstract void save();

    public final void optimise(int sliderValue) {
        for (ConfigProperty property : allProperties) {
            property.value = optimise(sliderValue, property);
        }
    }

    protected abstract String optimise(int sliderValue, ConfigProperty property);

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
