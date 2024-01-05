package me.jsedwards.mod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.jsedwards.Main;
import me.jsedwards.dashboard.ConsoleWrapper;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.OSUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CurseForge {

    public static void search(String query, ModLoader loader, String mcVersion, Consumer<CurseForgeProject> onSuccess) { // onSuccess will be executed on a different thread
        int gameFlavour = loader.getGameFlavour();
        if (gameFlavour == -1) return;
        String url = "https://www.curseforge.com/minecraft/search?page=1&pageSize=20&sortType=1&class=mc-mods&search=%s&gameVersion=%s&gameFlavorsIds=%s".formatted(query, mcVersion, gameFlavour);
        try {
            new ConsoleWrapper("curl -s -H \"User-Agent: Mozilla\" \"%s\"".formatted(url), new File(OSUtils.userHome), s -> {
                Document document = Jsoup.parse(s);
                Elements results = document.select(".results-container").get(0).children();
                for (Element result : results) {
                    if (!result.hasClass(" project-card")) continue;
                    CurseForgeProject.Builder builder = CurseForgeProject.builder();
                    for (Element child : result.children()) {
                        if (child.hasClass("name")) builder.title = child.text();
                        else if (child.hasClass("description")) builder.description = child.text();
                        else if (child.hasClass("art")) builder.icon = child.child(0).attr("src");
                        else if (child.hasClass("author")) builder.author = child.child(0).child(0).text();
                        else if (child.hasClass("overlay-link")) {
                            builder.numericId = getNumericProjectId("https://www.curseforge.com" + child.attr("href"));
                            builder.stringId = StringUtils.substringAfterLast(child.attr("href"), '/');
                        }
                    }
                    onSuccess.accept(builder.build());
                }
            }, s -> {}, () -> {});
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
            return detailsBox.child(0).child(1).child(5).text();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
