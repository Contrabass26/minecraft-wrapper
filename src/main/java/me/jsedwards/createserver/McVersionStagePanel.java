package me.jsedwards.createserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.jsedwards.Main;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class McVersionStagePanel extends ValidatedStage {

    private static final List<String> VERSIONS;
    static {
        VERSIONS = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            ArrayNode versions = (ArrayNode) mapper.readTree(new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")).get("versions");
            for (JsonNode version : versions) {
                if (version.get("type").textValue().equals("release")) {
                    VERSIONS.add(version.get("id").textValue());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final JComboBox<String> comboBox;

    public McVersionStagePanel() {
        super();
        // Layout
        this.setLayout(new GridBagLayout());
        // Label
        JLabel label = new JLabel("Minecraft version:");
        label.setFont(Main.MAIN_FONT.deriveFont(18f));
        this.add(label, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        // Combo box
        comboBox = new JComboBox<>(VERSIONS.toArray(new String[0]));
        this.add(comboBox, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 0), 0, 0));
    }

    public String getSelectedVersion() {
        return (String) this.comboBox.getSelectedItem();
    }

    @Override
    public boolean validateStage() {
        return true;
    }
}
