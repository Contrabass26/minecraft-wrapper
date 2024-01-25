package me.jsedwards.configserver;

import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import me.jsedwards.mod.CurseForge;
import me.jsedwards.mod.Modrinth;
import me.jsedwards.mod.ModrinthProject;
import me.jsedwards.mod.Project;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ModsPanel extends JPanel {

    private final JTextField searchBox;
    private final DefaultListModel<Project> searchResultsModel;
    private final JList<Project> searchResults;
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
            String selected = currentMods.getSelectedValue();
            if (selected == null) return;
            new File(server.serverLocation + "/mods/" + selected).delete();
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
            Project selectedValue = searchResults.getSelectedValue();
            if (selectedValue == null) return;
            Project.ModFile file = selectedValue.getFile(server.mcVersion, server.modLoader);
            try {
                String modsFolder = server.serverLocation + "/mods/";
                new File(modsFolder).mkdir();
                selectedValue.downloadFile(file, new File(modsFolder + file.filename()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            currentModsModel.addElement(file.filename());
        });
    }

    private void search() {
        String query = searchBox.getText();
        List<ModrinthProject> results = new ArrayList<>(Modrinth.search(query, server.modLoader, server.mcVersion));
        CurseForge.search(query, server.modLoader, server.mcVersion, searchResultsModel::addElement);
        searchResultsModel.clear();
        results.forEach(searchResultsModel::addElement);
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

        private final JLabel iconLbl;
        private final JLabel titleLbl;
        private final JLabel authorLbl;
        private final JTextArea descriptionLbl;

        public InfoPanel() {
            super();
            setLayout(new GridBagLayout());
            // Icon
            iconLbl = new JLabel();
            this.add(iconLbl, new GridBagConstraints(1, 1, 1, 2, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            // Title label
            titleLbl = new JLabel();
            titleLbl.setFont(Main.MAIN_FONT.deriveFont(18f));
            this.add(titleLbl, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
            // Author label
            authorLbl = new JLabel();
            this.add(authorLbl, new GridBagConstraints(2, 2, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
            // Description label
            descriptionLbl = new JTextArea();
            descriptionLbl.setEditable(false);
            this.add(descriptionLbl, new GridBagConstraints(1, 3, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(0, 0);
        }

        public void setSelected(Project project) {
            if (project != null) {
                try {
                    BufferedImage image = ImageIO.read(new URL(project.icon));
                    Image scaled = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    iconLbl.setIcon(new ImageIcon(scaled));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                titleLbl.setText(project.title.strip());
                descriptionLbl.setText(project.description.strip());
                authorLbl.setText(project.author.strip());
            }
        }
    }
}
