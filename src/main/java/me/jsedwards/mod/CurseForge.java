package me.jsedwards.mod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.jsedwards.Main;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.MinecraftUtils;
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
import java.util.function.Consumer;
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

    public static void search(String query, ModLoader loader, String mcVersion, Consumer<CurseForgeProject> onSuccess) { // onSuccess will be executed on a different thread
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(apiQuery("v1/mods/search?gameId=432&gameVersion=%s&searchFilter=%s".formatted(mcVersion, query.replace(" ", "%20")))))) {
            reader.lines().forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MinecraftData getMinecraftData(String version) {
        try (InputStream stream = apiQuery("v1/minecraft/version/" + version)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(stream).get("data");
            return new MinecraftData(data);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    private static ModLoaderData getModLoaderData(ModLoader loader) {
        try (InputStream stream = apiQuery("v1/minecraft/modloader/" + loader.toString().toLowerCase())) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(stream).get("data");
            return new ModLoaderData(data);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean contains(ArrayNode array, Predicate<JsonNode> predicate) {
        for (JsonNode node : array) {
            if (predicate.test(node)) return true;
        }
        return false;
    }

    public static Project.ModFile getFile(String numericId, String mcVersion, ModLoader loader) {
        int gameFlavour = loader.getGameFlavour();
        if (gameFlavour == -1) return null;
        String url = "https://www.curseforge.com/api/v1/mods/%s/files?pageIndex=0&pageSize=20&sort=dateCreated&sortDescending=true&gameFlavorId=%s&removeAlphas=true".formatted(numericId, gameFlavour);
        System.out.println(url);
        try {
            ArrayNode root = (ArrayNode) Main.WINDOW.statusPanel.curlToJson(new URL(url)).get("data");
            for (JsonNode file : root) {
                ArrayNode versions = (ArrayNode) file.get("gameVersions");
                if (contains(versions, n -> n.textValue().equals(mcVersion))) {
                    // Use this file
                    String filename = file.get("fileName").textValue();
                    String fileId = String.valueOf(file.get("id").intValue());
                    String id1 = fileId.substring(0, 4);
                    String id2 = fileId.substring(4);
                    String fileUrl = "https://mediafilez.forgecdn.net/files/%s/%s/%s".formatted(id1, id2, filename);
                    return new Project.ModFile(fileUrl, filename);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
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

    private record MinecraftData(int id) {

        public MinecraftData(JsonNode node) {
            this(node.get("id").intValue());
        }
    }

    private record ModLoaderData() {

        public ModLoaderData(JsonNode node) {
            this();
        }
    }
}
