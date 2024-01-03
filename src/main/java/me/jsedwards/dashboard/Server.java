package me.jsedwards.dashboard;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.jsedwards.CardPanel;
import me.jsedwards.Main;
import me.jsedwards.data.ServerDeserialiser;
import me.jsedwards.data.ServerSerialiser;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.Identifier;
import me.jsedwards.util.OSUtils;
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
import java.util.Map;

@JsonSerialize(using = ServerSerialiser.class)
@JsonDeserialize(using = ServerDeserialiser.class)
public class Server extends JPanel {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<Server> servers = new ArrayList<>();
    private static final TypeReference<List<Server>> SERVER_LIST_TYPE_REFERENCE = new TypeReference<>() {};

    public final String serverName;
    public final String serverLocation;
    public final ModLoader modLoader;
    public final String mcVersion;
    private final TopPanel topPanel;
    public int mbMemory;
    public int optimisationLevel;
    public Map<Identifier, Boolean> keysToOptimise; // Only stores keys that have been changed - all others will have their default value
    private final ConsolePanel consolePanel;
    private ConsoleWrapper consoleWrapper = null;

    private Server(String serverName, String serverLocation, ModLoader modLoader, String mcVersion, int mbMemory, int optimisationLevel, Map<Identifier, Boolean> keysToOptimise) {
        super();
        this.serverName = serverName;
        this.serverLocation = serverLocation;
        this.modLoader = modLoader;
        this.mcVersion = mcVersion;
        this.mbMemory = mbMemory;
        this.optimisationLevel = optimisationLevel;
        this.keysToOptimise = keysToOptimise;
        // Layout
        this.setLayout(new GridBagLayout());
        // Top panel
        topPanel = new TopPanel();
        this.add(topPanel, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(30, 10, 10, 10), 0, 0));
        // Console panel
        consolePanel = new ConsolePanel();
        this.add(consolePanel, new GridBagConstraints(1, 2, 1, 1, 1, 1, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0));
    }

    public static boolean exists(String name) {
        return servers.stream().anyMatch(s -> s.serverName.equals(name));
    }

    public static Server get(String name) {
        for (Server server : servers) {
            if (server.serverName.equals(name)) {
                return server;
            }
        }
        return null;
    }

    public boolean isRunning() {
        if (consoleWrapper == null) return false;
        return consoleWrapper.isRunning();
    }

    /**
     * Creates a new server and adds it to the volatile list. Can be used before GUI elements have been initialised.
     * @param name The name of the server
     * @param location The location where the server files are stored
     * @param modLoader The mod loader used by the server
     * @param mcVersion The Minecraft version of the server, e.g. 1.20.1
     * @return The new server object with the specified properties
     */
    public static Server create(String name, String location, ModLoader modLoader, String mcVersion, int mbMemory, int optimisationLevel, Map<Identifier, Boolean> keysToOptimise, boolean addCard) {
        Server server = new Server(name, location, modLoader, mcVersion, mbMemory, optimisationLevel, keysToOptimise);
        servers.add(server);
        if (addCard) Main.WINDOW.cardPanel.addServerCard(server);
        return server;
    }

    public static void delete(Server server) {
        if (server.isRunning()) {
            server.consoleWrapper.forceStop();
            server.consoleWrapper.waitFor();
        }
        Main.WINDOW.cardPanel.removeServerCard(server);
        Main.WINDOW.cardPanel.serverSelectPanel.removeServer(server);
        Main.WINDOW.cardPanel.switchToServerSelect();
        servers.remove(server);
        OSUtils.deleteDirectory(new File(server.serverLocation));
    }

    /**
     * Clears the volatile list of servers, then loads all saved servers from file. Should be called at the very start of the program.
     */
    public static void load() {
        servers.clear();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readValue(OSUtils.getServersFile(), SERVER_LIST_TYPE_REFERENCE);
            LOGGER.info("Loaded %s servers from %s".formatted(servers.size(), OSUtils.serversLocation));
        } catch (IOException e) {
            LOGGER.warn("Failed to load server data from " + OSUtils.serversLocation, e);
        }
    }

    /**
     * Add all servers in the volatile list to the given CardPanel and adds buttons to its ServerSelectPanel. Should not be used before GUI elements have been initialised.
     * @param cardPanel The card panel to add the servers to
     */
    public static void addToGUI(CardPanel cardPanel) {
        servers.forEach(cardPanel.serverSelectPanel::addServer);
        servers.forEach(cardPanel::addServerCard);
    }

    public static boolean save() { // Returns: whether the servers are happy to stop
        // Check if any are running
        for (Server server : servers) {
            if (server.isRunning()) {
                boolean forceStop = JOptionPane.showConfirmDialog(Main.WINDOW, "Server \"%s\" is still running. Do you want to force it to stop?".formatted(server.serverName), "Server still running", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
                if (forceStop) {
                    server.consoleWrapper.forceStop();
                } else {
                    return false;
                }
            }
        }
        OSUtils.createDataDir();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(OSUtils.getServersFile(), servers);
            LOGGER.info("Saved %s servers to %s".formatted(servers.size(), OSUtils.serversLocation));
        } catch (IOException e) {
            LOGGER.error("Failed to save server data to " + OSUtils.serversLocation, e);
        }
        return true;
    }

    public File getPropertiesLocation() {
        return new File(this.serverLocation + "/server.properties");
    }

    public void start() {
        try {
            consoleWrapper = new ConsoleWrapper(modLoader.getStartCommand(mbMemory), new File(this.serverLocation), this.consolePanel::log, this.consolePanel::log, topPanel.startStopButton::stop);
        } catch (IOException e) {
            consolePanel.log("Failed to start server: " + e.getMessage());
            LOGGER.error("Failed to start server", e);
        }
    }

    private class TopPanel extends JPanel {

        private final StartStopButton startStopButton;

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
            // Configure button
            JButton configureButton = new JButton("Configure");
            configureButton.addActionListener(e -> Main.WINDOW.cardPanel.switchToServerConfig(Server.this.serverName));
            this.add(configureButton, new GridBagConstraints(3, 1, 1, 1, 0, 1, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 5), 0, 0));
            // Start button
            startStopButton = new StartStopButton();
            this.add(startStopButton, new GridBagConstraints(4, 1, 1, 1, 0, 1, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
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
            if (!running) {
                Server.this.consolePanel.clearOutput();
                Server.this.start();
                running = true;
                updateColors();
            } else {
                try {
                    Server.this.consoleWrapper.write("stop\n");
                    Server.this.consolePanel.log("stop");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        public void stop() {
            running = false;
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
            JTextField textField = makeTextField();
            this.add(textField, new GridBagConstraints(1, 2, 2, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
        }

        private JTextField makeTextField() {
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
            return textField;
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
