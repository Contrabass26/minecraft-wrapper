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
            System.out.println(url_str);
            System.out.println(apiKey);
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

    public static void getCategories() {
        InputStream stream = apiQuery("v1/categories?gameId=432");
        assert stream != null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            reader.lines().forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<CurseForgeProject> search(String query, ModLoader loader, String mcVersion) { // onSuccess will be executed on a different thread
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
                    List<String> authors = new ArrayList<>();
                    for (JsonNode author : datum.get("authors")) {
                        authors.add(author.get("name").textValue());
                    }
                    String authorsStr = StringUtils.join(authors, ", ");
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

    public static Project.ModFile getModFile(int modId, String mcVersion, ModLoader loader) {
        InputStream stream = apiQuery("v1/mods/%s/files?gameVersion=%s&modLoaderType=%s".formatted(modId, mcVersion, loader.getCfModLoaderType()));
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode file = mapper.readTree(stream).get("data").get(0);
            String filename = file.get("fileName").textValue();
            String url = file.get("downloadUrl").textValue();
            return new Project.ModFile(url, filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean contains(ArrayNode array, Predicate<JsonNode> predicate) {
        for (JsonNode node : array) {
            if (predicate.test(node)) return true;
        }
        return false;
    }

    public static String getNumericProjectId(String projectUrl) {
        try {
            Document document = Main.WINDOW.statusPanel.curlToJsoup(new URL(projectUrl));
            Element detailsBox = document.select(".project-details-box").get(0);
            return detailsBox.child(1).child(1).child(5).text();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
