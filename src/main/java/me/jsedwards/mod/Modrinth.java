package me.jsedwards.mod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.jsedwards.modloader.ModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Modrinth {

    private static final String USER_AGENT = "Contrabass26/minecraft-wrapper";
    private static final Logger LOGGER = LogManager.getLogger();

    public static List<ModrinthProject> search(String query, ModLoader loader, String mcVersion) {
        try {
            String path = "search?query=%s&facets=[[\"versions:%s\"],[\"categories:%s\"],[\"server_side!=unsupported\"]]".formatted(query, mcVersion, loader.toString().toLowerCase());
            ArrayNode hits = (ArrayNode) doApiCall(path).get("hits");
            List<ModrinthProject> projects = new ArrayList<>();
            for (JsonNode hit : hits) {
                projects.add(new ModrinthProject(hit));
                LOGGER.debug("Added project " + hit.get("title").textValue());
            }
            return projects;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonNode doApiCall(String path) throws IOException {
        String url = formatUrl("https://api.modrinth.com/v2/" + path);
//        LOGGER.debug(url);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String content = reader.readLine();
//        LOGGER.debug(content);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    public static ModrinthFile getVersionFile(String id, ModLoader loader, String mcVersion) {
        try {
            String path = "project/%s/version?loaders=[\"%s\"]&game_versions=[\"%s\"]".formatted(id, loader.toString().toLowerCase(), mcVersion);
            ArrayNode versions = (ArrayNode) doApiCall(path);
            for (JsonNode version : versions) {
                if (version.get("version_type").textValue().equals("release")) {
                    ArrayNode files = (ArrayNode) version.get("files");
                    for (JsonNode file : files) {
                        String filename = file.get("filename").textValue();
                        if (filename.endsWith(".jar")) {
                            String url = file.get("url").textValue();
                            return new ModrinthFile(url, filename);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static String formatUrl(String url) {
        return url
                .replace("[", "%5B")
                .replace("]", "%5D")
                .replace("\"", "%22");
    }

    public record ModrinthFile(String url, String filename) {

        @Override
        public String toString() {
            return filename;
        }
    }
}
