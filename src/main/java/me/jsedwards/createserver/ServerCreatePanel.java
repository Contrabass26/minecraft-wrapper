package me.jsedwards.createserver;

import me.jsedwards.Card;
import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import me.jsedwards.modloader.ModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerCreatePanel extends JPanel implements Card {

    private static final Logger LOGGER = LogManager.getLogger();

    private final List<ValidatedStage> stages = new ArrayList<>();

    public ServerCreatePanel() {
        super();
        // Layout
        this.setLayout(new GridBagLayout());
        // Title
        JLabel titleLabel = new JLabel("Create new server");
        titleLabel.setFont(Main.MAIN_FONT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(titleLabel, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
        // Server name
        NameStagePanel nameStagePanel = new NameStagePanel();
        stages.add(nameStagePanel);
        // Server location
        LocationStagePanel locationStagePanel = new LocationStagePanel();
        stages.add(locationStagePanel);
        // Mod loader
        ModLoaderStagePanel modLoaderStagePanel = new ModLoaderStagePanel();
        stages.add(modLoaderStagePanel);
        // Minecraft version
        McVersionStagePanel mcVersionStagePanel = new McVersionStagePanel();
        stages.add(mcVersionStagePanel);
        // Add stages
        for (int i = 0; i < stages.size(); i++) {
            this.add(stages.get(i), new GridBagConstraints(1, 2 + i, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
        }
        // Create button
        JButton createButton = new JButton("Create server");
        createButton.setFont(Main.MAIN_FONT);
        createButton.addActionListener(e -> {
            if (stages.stream().allMatch(ValidatedStage::isStageValid)) {
                // Register server
                String serverName = nameStagePanel.getServerName();
                String serverLocation = locationStagePanel.getServerLocation();
                ModLoader modLoader = modLoaderStagePanel.getSelectedModLoader();
                String mcVersion = mcVersionStagePanel.getSelectedVersion();
                Server server = Server.create(serverName, serverLocation, modLoader, mcVersion, true);
                // Add new button to server select panel
                Main.WINDOW.cardPanel.serverSelectPanel.addServer(server);
                // Download server
                File destination = new File(serverLocation);
                try {
                    modLoader.downloadFiles(destination, mcVersion);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                // Switch to new server dashboard
                Main.WINDOW.cardPanel.switchToServer(serverName);
            } else {
                LOGGER.info("Invalid options selected");
            }
        });
        this.add(createButton, new GridBagConstraints(1, stages.size() + 2, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
        // Padding
        JPanel bottomPadding = new JPanel();
        this.add(bottomPadding, new GridBagConstraints(1, stages.size() + 3, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    @Override
    public void exit() {}
}
