package me.jsedwards;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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

    // TODO: Get latest automatically
    private static final String FABRIC_LOADER_VERSION = "0.14.24";
    private static final String FABRIC_INSTALLER_VERSION = "0.11.2";

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
