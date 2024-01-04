package me.jsedwards.configserver;

import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import me.jsedwards.mod.Modrinth;
import me.jsedwards.mod.ModrinthProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ModsPanel extends JPanel {

    private final JTextField searchBox;
    private final DefaultListModel<ModrinthProject> searchResultsModel;
    private final JList<ModrinthProject> searchResults;
    private final DefaultListModel<String> currentModsModel;
    private final JList<String> currentMods;
    private Server server = null;

    public ModsPanel() {
        super();
        setLayout(new GridBagLayout());
        // Search label
        JLabel searchLbl = new JLabel("Search mods:");
        this.add(searchLbl, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(7, 0, 0, 15), 0, 0));
        // Search box
        searchBox = new JTextField();
        searchBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    search();
                }
            }
        });
        this.add(searchBox, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
        // Search button
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> search());
        this.add(searchBtn, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0));
        // Search results
        searchResultsModel = new DefaultListModel<>();
        searchResults = new JList<>(searchResultsModel);
        this.add(searchResults, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 3, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0));
        // Add button
        JButton addBtn = new JButton("Add");
        this.add(addBtn, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 3, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Currently added mods label
        JLabel currentModsLbl = new JLabel("Current mods installed:");
        this.add(currentModsLbl, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 3, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 0), 0, 0));
        // Currently added mods list box
        currentModsModel = new DefaultListModel<>();
        currentMods = new JList<>(currentModsModel);
        this.add(currentMods, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 4, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0));
        // Remove button
        JButton removeBtn = new JButton("Remove");
        removeBtn.addActionListener(e -> {
            new File(server.serverLocation + "/mods/" + currentMods.getSelectedValue()).delete();
            currentModsModel.remove(currentMods.getSelectedIndex());
        });
        this.add(removeBtn, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 4, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        // Info panel
        InfoPanel infoPanel = new InfoPanel();
        this.add(infoPanel, new GridBagConstraints(4, 1, 1, 5, 0.4, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 5, 0, 0), 0, 0));
        // Info panel listener
        searchResults.addListSelectionListener(e -> infoPanel.setSelected(searchResults.getSelectedValue()));
        // Add button listener
        addBtn.addActionListener(e -> {
            Modrinth.ModrinthFile file = searchResults.getSelectedValue().getFile(server.mcVersion, server.modLoader);
            try {
                Main.WINDOW.statusPanel.saveFileFromUrl(new URL(file.url()), new File(server.serverLocation + "/mods/" + file.filename()));
                currentModsModel.addElement(file.filename());
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void search() {
        String query = searchBox.getText();
        List<ModrinthProject> results = Modrinth.search(query, server.modLoader, server.mcVersion);
        searchResultsModel.clear();
        results.forEach(searchResultsModel::addElement);
        searchResults.invalidate();
        searchResults.repaint();
    }

    public void setServer(Server server) {
        this.server = server;
        currentModsModel.clear();
        File[] children = new File(server.serverLocation + "/mods").listFiles();
        if (children != null) {
            for (File file : children) {
                if (file.getAbsolutePath().endsWith(".jar")) {
                    currentModsModel.addElement(file.getName());
                }
            }
        }
    }

    private static class InfoPanel extends JPanel {

        private final JLabel titleLbl;
        private final JTextArea descriptionLbl;

        public InfoPanel() {
            super();
            setLayout(new GridBagLayout());
            // Title label
            titleLbl = new JLabel();
            this.add(titleLbl, new GridBagConstraints(1, 1, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            // Description label
            descriptionLbl = new JTextArea();
            descriptionLbl.setEditable(false);
            this.add(descriptionLbl, new GridBagConstraints(1, 2, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(0, 0);
        }

        public void setSelected(ModrinthProject project) {
            titleLbl.setText(project == null ? "" : project.title);
            descriptionLbl.setText(project == null ? "" : project.description);
        }
    }
}
