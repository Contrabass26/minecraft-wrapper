package me.jsedwards;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public enum ModLoader {

    VANILLA,
    FORGE,
    FABRIC {
        @Override
        public void downloadFiles(File destination, String mcVersion) throws IOException {
            // Get fabric-server-launch.jar
            HttpURLConnection connection = (HttpURLConnection) new URL("https://meta.fabricmc.net/v2/versions/loader/%s/%s/%s/server/jar".formatted(mcVersion, FABRIC_LOADER_VERSION, FABRIC_INSTALLER_VERSION)).openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            try (FileOutputStream stream = new FileOutputStream(destination.getAbsolutePath() + "/fabric-server-launch.jar")) {
                inputStream.transferTo(stream);
            }
            inputStream.close();
            // eula.txt
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination.getAbsolutePath() + "/eula.txt"))) {
                writer.write("eula=true");
            }
        }
    };

    private static final String FABRIC_LOADER_VERSION;
    private static final String FABRIC_INSTALLER_VERSION;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final TypeReference<ArrayList<FabricLoaderData>> FABRIC_LOADER_DATA_LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<ArrayList<FabricInstallerData>> FABRIC_INSTALLER_DATA_LIST_TYPE = new TypeReference<>() {};

    static {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://meta.fabricmc.net/v2/versions/loader").openConnection();
            connection.setRequestMethod("GET");
            List<FabricLoaderData> loaders = MinecraftWrapperUtils.readJson(connection.getInputStream(), FABRIC_LOADER_DATA_LIST_TYPE);
            FABRIC_LOADER_VERSION = loaders.get(0).version;
            LOGGER.info("Detected latest Fabric loader version: " + FABRIC_LOADER_VERSION);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://meta.fabricmc.net/v2/versions/installer").openConnection();
            connection.setRequestMethod("GET");
            List<FabricInstallerData> installers = MinecraftWrapperUtils.readJson(connection.getInputStream(), FABRIC_INSTALLER_DATA_LIST_TYPE);
            FABRIC_INSTALLER_VERSION = installers.get(0).version;
            LOGGER.info("Detected latest Fabric installer version: " + FABRIC_INSTALLER_VERSION);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    @Override
    public String toString() {
        return StringUtils.capitalize(super.toString().toLowerCase());
    }
}
