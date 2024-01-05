package me.jsedwards.mod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.jsedwards.modloader.ModLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Modrinth {

    private static final String USER_AGENT = "Contrabass26/minecraft-wrapper";

    public static List<ModrinthProject> search(String query, ModLoader loader, String mcVersion) {
        try {
            String path = "search?query=%s&facets=[[\"versions:%s\"],[\"categories:%s\"],[\"server_side!=unsupported\"]]".formatted(query, mcVersion, loader.toString().toLowerCase());
            ArrayNode hits = (ArrayNode) doApiCall(path).get("hits");
            List<ModrinthProject> projects = new ArrayList<>();
            for (JsonNode hit : hits) {
                projects.add(new ModrinthProject(hit));
            }
            return projects;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonNode doApiCall(String path) throws IOException {
        String url = formatUrl("https://api.modrinth.com/v2/" + path);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String content = reader.readLine();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    public static Project.ModFile getVersionFile(String id, ModLoader loader, String mcVersion) {
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
                            return new Project.ModFile(url, filename);
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
}
