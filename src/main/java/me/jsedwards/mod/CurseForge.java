package me.jsedwards.mod;

import me.jsedwards.dashboard.ConsoleWrapper;
import me.jsedwards.modloader.ModLoader;
import me.jsedwards.util.OSUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

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
                    }
                    onSuccess.accept(builder.build());
                }
            }, s -> {}, () -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
