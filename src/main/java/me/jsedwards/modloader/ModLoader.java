package me.jsedwards.modloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.jsedwards.Main;
import me.jsedwards.dashboard.ConsoleWrapper;
import me.jsedwards.dashboard.Server;
import me.jsedwards.util.JsonUtils;
import me.jsedwards.util.MinecraftUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        public String getStartCommand(int mbMemory, Server server) {
            return "java -Xmx%sM -jar server.jar nogui".formatted(mbMemory);
        }
    },
    FORGE {
        private static final Set<String> BLACKLIST = Set.of("1.14", "1.14.1", "1.16", "1.17");

        @Override
        public void downloadFiles(File destination, String mcVersion) throws IOException {
            Main.WINDOW.statusPanel.getJsoupFromUrl("https://files.minecraftforge.net/net/minecraftforge/forge/index_%s.html".formatted(mcVersion), document -> {
                String messyUrl = document.select("div.link.link-boosted").get(0).child(0).attr("href");
                Pattern pattern = Pattern.compile("url=(https://maven\\.minecraftforge\\.net/.*)");
                Matcher matcher = pattern.matcher(messyUrl);
                MatchResult matchResult = matcher.results().findFirst().orElseThrow(IllegalStateException::new);
                String url = matchResult.group(1);
                try {
                    Main.WINDOW.statusPanel.saveFileFromUrl(new URL(url), new File(destination.getAbsolutePath() + "/installer.jar"), () -> {
                        try {
                            Main.WINDOW.statusPanel.setMax(22507); // Heuristic value based on 1.20.2 install
                            final AtomicInteger lineCount = new AtomicInteger();
                            new ConsoleWrapper("java -jar installer.jar -installServer", destination, s -> {
                                int currentCount = lineCount.incrementAndGet();
                                Main.WINDOW.statusPanel.setProgress(currentCount);
                                Main.WINDOW.statusPanel.setStatus(s);
                            }, LOGGER::error, () -> {
                                LOGGER.info("Forge installer for %s finished, outputting %s lines".formatted(mcVersion, lineCount.get()));
                                Main.WINDOW.statusPanel.setStatus("Ready");
                                Main.WINDOW.statusPanel.setProgress(0);
                            });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            });
            ModLoader.writeEula(destination);
        }

        @Override
        public String getStartCommand(int mbMemory, Server server) {
            File[] children = new File(server.serverLocation).listFiles();
            if (children != null) {
                for (File file : children) {
                    String name = file.getName();
                    if (name.matches("minecraft_server.*\\.jar")) {
                        return "java -Xmx%sM -jar %s nogui".formatted(mbMemory, name);
                    }
                }
            }
            String libsPath = server.serverLocation + "/libraries/net/minecraftforge/forge";
            String version = Objects.requireNonNull(new File(libsPath).listFiles())[0].getName();
            return "java -Xmx" + mbMemory + "M @libraries/net/minecraftforge/forge/" + version + "/win_args.txt nogui %*";
        }

        @Override
        public boolean supportsVersion(String version) {
            return !BLACKLIST.contains(version);
        }

        @Override
        public boolean supportsMods() {
            return true;
        }

        @Override
        public int getGameFlavour() {
            return 1;
        }
    },
    FABRIC {
        @Override
        public void downloadFiles(File destination, String mcVersion) throws IOException {
            // Get fabric-server-launch.jar
            Main.WINDOW.statusPanel.saveFileFromUrl(new URL("https://meta.fabricmc.net/v2/versions/loader/%s/%s/%s/server/jar".formatted(mcVersion, FABRIC_LOADER_VERSION, FABRIC_INSTALLER_VERSION)), new File(destination.getAbsolutePath() + "/fabric-server-launch.jar"));
            // eula.txt
            ModLoader.writeEula(destination);
        }

        @Override
        public String getStartCommand(int mbMemory, Server server) {
            return "java -Xmx%sM -jar fabric-server-launch.jar nogui".formatted(mbMemory);
        }

        @Override
        public boolean supportsVersion(String version) {
            return MinecraftUtils.compareVersions(version, "1.14") >= 0;
        }

        @Override
        public boolean supportsMods() {
            return true;
        }

        @Override
        public int getGameFlavour() {
            return 4;
        }
    },
    PUFFERFISH {
        @Override
        public void downloadFiles(File destination, String mcVersion) throws IOException {
            // Pufferfish files
            String shortMcVersion = StringUtils.countMatches(mcVersion, '.') == 1 ? mcVersion : StringUtils.substringBeforeLast(mcVersion, ".");
            Main.WINDOW.statusPanel.getJsoupFromUrl("https://ci.pufferfish.host/job/Pufferfish-%s/changes".formatted(shortMcVersion), document -> {
                Elements children = document.select("#main-panel").get(0).children();
                String chosenBuild = null;
                for (int i = 0; i < children.size(); i++) {
                    Element child = children.get(i);
                    if (child.is("h2")) {
                        Element description = children.get(i + 1);
                        if (description.is("ol") && description.text().contains(mcVersion)) {
                            // Get that one
                            chosenBuild = StringUtils.substringBetween(child.text(), "#", " ");
                            break;
                        }
                    }
                }
                // Confirm manually
                String message = chosenBuild == null ? "No relevant build was detected in https://ci.pufferfish.host/job/Pufferfish-%s/changes".formatted(shortMcVersion) : "Detected build %s in https://ci.pufferfish.host/job/Pufferfish-%s/changes".formatted(chosenBuild, shortMcVersion);
                chosenBuild = (String) JOptionPane.showInputDialog(Main.WINDOW, message + " - enter the build to use:", "Enter build to use", JOptionPane.QUESTION_MESSAGE, null, null, chosenBuild == null ? "" : chosenBuild);
                // Download jar
                String finalBuild = chosenBuild;
                Main.WINDOW.statusPanel.getJsoupFromUrl("https://ci.pufferfish.host/job/Pufferfish-%s/%s".formatted(shortMcVersion, finalBuild), document1 -> {
                    String relativeJarPath = document1.select(".fileList").get(0).child(0).child(0).child(1).child(0).attr("href");
                    try {
                        Main.WINDOW.statusPanel.saveFileFromUrl(new URL("https://ci.pufferfish.host/job/Pufferfish-%s/%s/%s".formatted(shortMcVersion, finalBuild, relativeJarPath)), new File(destination.getAbsolutePath() + "/pufferfish.jar"));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
            ModLoader.writeEula(destination);
        }

        @Override
        public String getStartCommand(int mbMemory, Server server) {
            return "java -Xmx%sM -jar pufferfish.jar nogui".formatted(mbMemory);
        }

        @Override
        public boolean supportsVersion(String version) {
            return MinecraftUtils.compareVersions(version, "1.17") >= 0;
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

    public String getStartCommand(int mbMemory, Server server) {
        throw new RuntimeException("Mod loader not supported!");
    }

    public boolean supportsMods() {
        return false;
    }

    public int getGameFlavour() {
        return -1; // Signifies no game flavour
    }

    // Will only be tested back to 1.8.9
    public boolean supportsVersion(String version) {
        return true;
    }

    @Override
    public String toString() {
        return StringUtils.capitalize(super.toString().toLowerCase());
    }
}
