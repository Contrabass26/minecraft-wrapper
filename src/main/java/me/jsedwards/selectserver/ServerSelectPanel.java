package me.jsedwards.selectserver;

import me.jsedwards.Card;
import me.jsedwards.Main;
import me.jsedwards.dashboard.Server;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;

public class ServerSelectPanel extends JPanel implements Card {

    private static final int BOXES_PER_ROW = 5;

    private final ButtonsPanel buttonsPanel;

    public ServerSelectPanel() {
        super();
        // Layout
        this.setLayout(new GridBagLayout());
        // Top label and padding panels
        this.add(new JPanel(), new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 0), 0, 0));
        JLabel label = new JLabel("Select or create a server:");
        label.setFont(Main.MAIN_FONT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(label, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 0), 0, 0));
        this.add(new JPanel(), new GridBagConstraints(3, 1, 1, 1, 1, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 10), 0, 0));
        // Buttons panel
        buttonsPanel = new ButtonsPanel();
        this.add(buttonsPanel, new GridBagConstraints(1, 2, 3, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
        // Button to create server
        JButton createButton = new JButton("Create server");
        createButton.addActionListener(e -> {
            // Create new server
            Main.WINDOW.cardPanel.switchToServerCreation();
        });
        createButton.setFont(Main.MAIN_FONT);
        this.add(createButton, new GridBagConstraints(1, 3, 3, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 10, 10), 20, 20));
    }

    public void addServer(Server server) {
        this.buttonsPanel.addServer(server);
    }

    @Override
    public void exit() {}

    @Override
    public void onShowCard() {

    }

    private static class ButtonsPanel extends JPanel {

        private int count = -1;

        private ButtonsPanel() {
            super();
            this.setLayout(new GridBagLayout());
        }

        private void addServer(Server server) {
            SwingUtilities.invokeLater(() -> {
                // Increase count
                this.count++;
                // Create new button
                JButton button = new JButton("%s - %s".formatted(server.serverName, server.serverLocation));
                button.addActionListener(e -> {
                    // Switch to correct card
                    Main.WINDOW.cardPanel.switchToServer(StringUtils.substringBefore(button.getText(), " - "));
                });
                button.setFont(Main.MAIN_FONT);
                // Add to layout
                ButtonsPanel.this.add(button,
                        new GridBagConstraints(
                                ButtonsPanel.this.count % BOXES_PER_ROW,
                                Math.floorDiv(ButtonsPanel.this.count, BOXES_PER_ROW),
                                1, 1, 1, 1,
                                GridBagConstraints.NORTH,
                                GridBagConstraints.BOTH,
                                new Insets(ButtonsPanel.this.count >= BOXES_PER_ROW ? 10 : 0, ButtonsPanel.this.count % BOXES_PER_ROW == 0 ? 0 : 10, 0, 0),
                                0, 0
                        )
                );
                ButtonsPanel.this.validate();
                ButtonsPanel.this.repaint();
            });
        }
    }
}
