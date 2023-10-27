package me.jsedwards;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Server extends JPanel {

    private static final List<Server> servers = new ArrayList<>();

    public final String serverName;
    public final String serverLocation;
    private final ConsolePanel consolePanel;
    private ConsoleWrapper consoleWrapper = null;

    private Server(String serverName, String serverLocation) {
        super();
        this.serverName = serverName;
        this.serverLocation = serverLocation;
        // Layout
        this.setLayout(new GridBagLayout());
        // Top panel
        this.add(new TopPanel(), new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(30, 10, 10, 10), 0, 0));
        // Console panel
        consolePanel = new ConsolePanel();
        this.add(consolePanel, new GridBagConstraints(1, 2, 1, 1, 1, 1, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0));
    }

    public static Server create(String name, String location) {
        Server server = new Server(name, location);
        servers.add(server);
        Main.WINDOW.cardPanel.addServerCard(server);
        return server;
    }

    public void start() {
        try {
            consoleWrapper = new ConsoleWrapper("java -Xmx2G -jar fabric-server-launch.jar nogui", new File(this.serverLocation), this.consolePanel::log, this.consolePanel::log);
        } catch (IOException e) {
            consolePanel.log("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class TopPanel extends JPanel {

        private TopPanel() {
            super();
            this.setLayout(new GridBagLayout());
            JLabel titleLabel = new JLabel(Server.this.serverName);
            titleLabel.setFont(Main.MAIN_FONT);
            this.add(titleLabel, new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            JButton startButton = new JButton("Start");
            startButton.addActionListener(e -> Server.this.start());
            this.add(startButton, new GridBagConstraints(2, 1, 1, 1, 0, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
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
                            consoleWrapper.write(textField.getText() + "\n");
                            textField.setText("");
                        } catch (IOException ex) {
                            log("Failed to send command: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                }
            });
            this.add(textField, new GridBagConstraints(1, 2, 2, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
        }

        public void log(String text) {
            Document document = outputArea.getDocument();
            String appendage = (this.firstLog ? "" : "\n") + text;
            this.firstLog = false;
            try {
                document.insertString(document.getLength(), appendage, null);
            } catch (BadLocationException e) {
                System.err.println("Failed to write console output");
                e.printStackTrace();
            }
            scrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
        }
    }
}
