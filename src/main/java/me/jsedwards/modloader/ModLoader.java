package me.jsedwards.modloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.jsedwards.Main;
import me.jsedwards.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public enum ModLoader {

    VANILLA {
        @Override
        public void downloadFiles(File destination, String mcVersion) throws IOException {
            Main.WINDOW.statusPanel.getJsonNodeFromUrl(new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"), node -> {
                JsonNode versions = node.get("versions");
                if (versions instanceof ArrayNode list) {
                    for (JsonNode version : list) {
                        if (mcVersion.equals(version.get("id").textValue())) {
                            try {
                                Main.WINDOW.statusPanel.getJsonNodeFromUrl(new URL(version.get("url").textValue()), node1 -> {
                                    String url = node1.get("downloads").get("server").get("url").textValue();
                                    try {
                                        Main.WINDOW.statusPanel.saveFileFromUrl(new URL(url), new File(destination.getAbsolutePath() + "/server.jar"));
                                    } catch (MalformedURLException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            });
            ModLoader.writeEula(destination);
        }

        @Override
        public String getStartCommand(int mbMemory) {
            return "java -Xmx%sM -jar server.jar nogui".formatted(mbMemory);
        }
    },
    FORGE,
    FABRIC {
        @Override
        public void downloadFiles(File destination, String mcVersion) throws IOException {
            // Get fabric-server-launch.jar
            Main.WINDOW.statusPanel.saveFileFromUrl(new URL("https://meta.fabricmc.net/v2/versions/loader/%s/%s/%s/server/jar".formatted(mcVersion, FABRIC_LOADER_VERSION, FABRIC_INSTALLER_VERSION)), new File(destination.getAbsolutePath() + "/fabric-server-launch.jar"));
            // eula.txt
            ModLoader.writeEula(destination);
        }

        @Override
        public String getStartCommand(int mbMemory) {
            return "java -Xmx%sM -jar fabric-server-launch.jar nogui".formatted(mbMemory);
        }
    };

    private static String FABRIC_LOADER_VERSION;
    private static String FABRIC_INSTALLER_VERSION;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final TypeReference<ArrayList<FabricLoaderData>> FABRIC_LOADER_DATA_LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<ArrayList<FabricInstallerData>> FABRIC_INSTALLER_DATA_LIST_TYPE = new TypeReference<>() {};

    static {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://meta.fabricmc.net/v2/versions/loader").openConnection();
            connection.setRequestMethod("GET");
            List<FabricLoaderData> loaders = JsonUtils.readJson(connection.getInputStream(), FABRIC_LOADER_DATA_LIST_TYPE);
            FABRIC_LOADER_VERSION = loaders.stream().filter(FabricLoaderData::isStable).findFirst().orElseThrow(IOException::new).version;
            LOGGER.info("Detected latest stable Fabric loader version: " + FABRIC_LOADER_VERSION);
        } catch (IOException e) {
            LOGGER.error("Failed to load latest stable Fabric loader version");
            FABRIC_LOADER_VERSION = "0.15.3";
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://meta.fabricmc.net/v2/versions/installer").openConnection();
            connection.setRequestMethod("GET");
            List<FabricInstallerData> installers = JsonUtils.readJson(connection.getInputStream(), FABRIC_INSTALLER_DATA_LIST_TYPE);
            FABRIC_INSTALLER_VERSION = installers.stream().filter(FabricInstallerData::isStable).findFirst().orElseThrow(IOException::new).version;
            LOGGER.info("Detected latest stable Fabric installer version: " + FABRIC_INSTALLER_VERSION);
        } catch (IOException e) {
            LOGGER.error("Failed to load latest stable Fabric installer version");
            FABRIC_INSTALLER_VERSION = "0.11.2";
        }
    }

    private static void writeEula(File serverRoot) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(serverRoot.getAbsolutePath() + "/eula.txt"))) {
            writer.write("eula=true");
        }
    }

    public static ModLoader get(int i) {
        return ModLoader.values()[i];
    }

    public static int count() {
        return ModLoader.values().length;
    }

    public void downloadFiles(File destination, String mcVersion) throws IOException {
        throw new RuntimeException("Mod loader not supported!");
    }

    public String getStartCommand(int mbMemory) {
        throw new RuntimeException("Mod loader not supported!");
    }

    @Override
    public String toString() {
        return StringUtils.capitalize(super.toString().toLowerCase());
    }
}
