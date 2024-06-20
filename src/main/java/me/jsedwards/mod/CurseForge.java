package me.jsedwards.mod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.jsedwards.Main;
import me.jsedwards.modloader.ModLoader;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CurseForge {

    private static InputStream apiQuery(String query) {
        try {
            String apiKey = System.getenv("CF_API_KEY");
            String url_str = "https://api.curseforge.com/" + query;
            URL url = URI.create(url_str).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-api-key", apiKey);
            return connection.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public static List<CurseForgeProject> search(String query, ModLoader loader, String mcVersion) {
        List<CurseForgeProject> mods = new ArrayList<>();
        // Minecraft: gameId=432
        // Sort by popularity: sortField=2
        String url = "v1/mods/search?gameId=432&gameVersion=%s&searchFilter=%s&modLoaderType=%s&sortField=2&sortOrder=desc".formatted(mcVersion, query.replace(" ", "%20"), loader.toString());
        InputStream stream = apiQuery(url);
        assert stream != null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            ArrayNode data = (ArrayNode) mapper.readTree(stream).get("data");
            for (JsonNode datum : data) {
                String website = datum.get("links").get("websiteUrl").textValue();
                if (website.contains("mc-mods")) {
                    // Authors
                    String authorsStr = getAuthorString(datum.get("authors"));
                    mods.add(new CurseForgeProject(
                            datum.get("name").textValue(),
                            datum.get("summary").textValue(),
                            authorsStr,
                            datum.get("logo").get("thumbnailUrl").textValue(),
                            datum.get("id").intValue(),
                            datum.get("downloadCount").intValue()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return mods;
    }

    public static String getAuthorString(JsonNode authorsNode) {
        List<String> authors = new ArrayList<>();
        for (JsonNode author : authorsNode) {
            authors.add(author.get("name").textValue());
        }
        return StringUtils.join(authors, ", ");
    }

    public static JsonNode getProjectNode(int id) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            InputStream stream = apiQuery("v1/mods/%s".formatted(id));
            return mapper.readTree(stream).get("data");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Project.ModFile getModFile(int modId, String mcVersion, ModLoader loader) {
        InputStream stream = apiQuery("v1/mods/%s/files?gameVersion=%s&modLoaderType=%s".formatted(modId, mcVersion, loader.getCfModLoaderType()));
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode intermediate = mapper.readTree(stream);
            JsonNode file = intermediate.get("data").get(0);
            String filename = file.get("fileName").textValue();
            String url = file.get("downloadUrl").textValue();
            return new Project.ModFile(url, filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
