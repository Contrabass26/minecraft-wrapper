package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import me.jsedwards.modloader.ModLoader;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitConfigManager extends YamlConfigManager {

    private static final HashMap<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DATA_TYPES = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DEFAULTS = new HashMap<>();
    private static final Map<String, Boolean> KEYS_ENABLED = new HashMap<>();

    static {
        Pattern h3Pattern = Pattern.compile("(.*) \\(([^()]*)\\)");
        try {
            Document document = Jsoup.connect("https://bukkit.fandom.com/wiki/Bukkit.yml").userAgent("Mozilla").get();
            Elements children = document.select(".mw-parser-output").get(0).children();
            for (int i = 0; i < children.size(); i++) {
                Element child = children.get(i);
                if (child.is("h2")) {
                    String key = child.child(0).text();
                    String description = children.get(i + 1).text();
                    PROPERTY_DESCRIPTIONS.put(key, description);
                } else if (child.is("h3")) {
                    String key = child.child(0).text();
                    String descriptionText = children.get(i + 1).text();
                    Matcher matcher = h3Pattern.matcher(descriptionText);
                    Optional<MatchResult> matchResult = matcher.results().findFirst();
                    if (matchResult.isPresent()) {
                        MatchResult result = matchResult.get();
                        PROPERTY_DESCRIPTIONS.put(key, result.group(1));
                        PROPERTY_DATA_TYPES.put(key, result.group(2));
                    } else {
                        PROPERTY_DESCRIPTIONS.put(key, descriptionText);
                    }
                    PROPERTY_DEFAULTS.put(key, StringUtils.substringAfter(children.get(i + 2).text(), "Default: "));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BukkitConfigManager() {
        super("bukkit.yml", s -> s.modLoader == ModLoader.PUFFERFISH);
    }

    @Override
    protected String optimise(int sliderValue, ConfigProperty property) {
        return property.value;
    }

    @Override
    protected String getPath(Server server) {
        return server.serverLocation + "/" + name;
    }
}
