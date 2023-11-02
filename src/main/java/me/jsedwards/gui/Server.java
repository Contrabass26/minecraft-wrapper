package me.jsedwards.gui;

import me.jsedwards.ConsoleWrapper;
import me.jsedwards.Main;
import me.jsedwards.ModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Server extends JPanel {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<Server> servers = new ArrayList<>();

    public final String serverName;
    public final String serverLocation;
    public final ModLoader modLoader;
    public final String mcVersion;
    private final ConsolePanel consolePanel;
    private ConsoleWrapper consoleWrapper = null;

    private Server(String serverName, String serverLocation, ModLoader modLoader, String mcVersion) {
        super();
        this.serverName = serverName;
        this.serverLocation = serverLocation;
        this.modLoader = modLoader;
        this.mcVersion = mcVersion;
        // Layout
        this.setLayout(new GridBagLayout());
        // Top panel
        this.add(new TopPanel(), new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(30, 10, 10, 10), 0, 0));
        // Console panel
        consolePanel = new ConsolePanel();
        this.add(consolePanel, new GridBagConstraints(1, 2, 1, 1, 1, 1, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0));
    }

    public static boolean exists(String name) {
        return servers.stream().anyMatch(s -> s.serverName.equals(name));
    }

    public static Server create(String name, String location, ModLoader modLoader, String mcVersion) {
        Server server = new Server(name, location, modLoader, mcVersion);
        servers.add(server);
        Main.WINDOW.cardPanel.addServerCard(server);
        return server;
    }

    public void start() {
        try {
            consoleWrapper = new ConsoleWrapper("java -Xmx2G -jar fabric-server-launch.jar nogui", new File(this.serverLocation), this.consolePanel::log, this.consolePanel::log);
        } catch (IOException e) {
            consolePanel.log("Failed to start server: " + e.getMessage());
            LOGGER.error("Failed to start server", e);
        }
    }

    private class TopPanel extends JPanel {

        private TopPanel() {
            super();
            // Layout
            this.setLayout(new GridBagLayout());
            // Back button
            JButton backButton = new JButton("Back");
            backButton.addActionListener(e -> Main.WINDOW.cardPanel.switchToServerSelect());
            this.add(backButton, new GridBagConstraints(1, 1, 1, 1, 0, 1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 10), 0, 0));
            // Title label
            JLabel titleLabel = new JLabel(Server.this.serverName + " - " + Server.this.modLoader);
            titleLabel.setFont(Main.MAIN_FONT);
            this.add(titleLabel, new GridBagConstraints(2, 1, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            // Start button
            StartStopButton startStopButton = new StartStopButton();
            this.add(startStopButton, new GridBagConstraints(3, 1, 1, 1, 0, 1, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        }
    }

    private class StartStopButton extends JButton implements ActionListener {

        private boolean running = false;

        private StartStopButton() {
            super("Start");
            this.setBackground(Color.GREEN);
            this.setForeground(Color.BLACK);
            this.addActionListener(this);
        }

        private void updateColors() {
            this.setBackground(running ? Color.RED : Color.GREEN);
            this.setForeground(running ? Color.WHITE : Color.BLACK);
            this.setText(running ? "Stop" : "Start");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            running = !running;
            if (running) {
                Server.this.consolePanel.clearOutput();
                Server.this.start();
            } else {
                try {
                    Server.this.consoleWrapper.write("stop\n");
                    Server.this.consolePanel.log("stop");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            updateColors();
        }
    }

    private class ConsolePanel extends JPanel {

        private final JTextArea outputArea = new JTextArea();
        private final JScrollPane scrollPane;
        private boolean firstLog = true;

        private ConsolePanel() {
            super();
            // Layout
            this.setLayout(new GridBagLayout());
            // Output area
            outputArea.setEditable(false);
            outputArea.setAlignmentY(BOTTOM_ALIGNMENT);
            outputArea.setFont(Main.MONOSPACED_FONT);
            scrollPane = new JScrollPane(outputArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.add(scrollPane, new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            // Input field
            JTextField textField = new JTextField();
            textField.setFont(Main.MONOSPACED_FONT);
            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        try {
                            Server.this.consoleWrapper.write(textField.getText() + "\n");
                            ConsolePanel.this.log(textField.getText());
                            textField.setText("");
                        } catch (IOException ex) {
                            LOGGER.error("Failed to send command to server", ex);
                        }
                    }
                }
            });
            this.add(textField, new GridBagConstraints(1, 2, 2, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
        }

        public void clearOutput() {
            this.outputArea.setText("");
            this.firstLog = true;
        }

        public void log(String text) {
            Document document = outputArea.getDocument();
            String appendage = (this.firstLog ? "" : "\n") + text;
            this.firstLog = false;
            try {
                document.insertString(document.getLength(), appendage, null);
            } catch (BadLocationException e) {
                LOGGER.error("Failed to write server output", e);
            }
            scrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
        }
    }
}
