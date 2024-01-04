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
            String url = "https://api.modrinth.com/v2/search?query=" + query + "&facets=%5B%5B%22versions:" + mcVersion + "%22%5D,%5B%22categories:" + loader.toString().toLowerCase() + "%22%5D%5D";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String content = reader.readLine();
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode hits = (ArrayNode) mapper.readTree(content).get("hits");
            List<ModrinthProject> projects = new ArrayList<>();
            for (JsonNode hit : hits) {
                projects.add(new ModrinthProject(hit));
            }
            return projects.stream().filter(ModrinthProject::isServerSide).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
