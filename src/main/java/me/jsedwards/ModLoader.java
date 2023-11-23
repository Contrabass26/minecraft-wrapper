package me.jsedwards;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String FABRIC_LOADER_VERSION;
    private static final String FABRIC_INSTALLER_VERSION;

    static {
        Document contents;
        try {
            contents = Jsoup
                    .connect("https://fabricmc.net/use/installer/")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                    .header("Accept-Language", "*")
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String elementText = contents.getElementsContainingText("Installer Version").get(0).text();
        Matcher matcher = Pattern.compile("Installer Version: ([0-9.]+) \\(Latest\\)").matcher(elementText);
        FABRIC_INSTALLER_VERSION = matcher.group(1);
        FABRIC_LOADER_VERSION = "0.14.24";
        System.out.println(FABRIC_INSTALLER_VERSION);
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
