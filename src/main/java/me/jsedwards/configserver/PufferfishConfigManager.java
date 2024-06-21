package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import me.jsedwards.modloader.ModLoader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class PufferfishConfigManager extends YamlConfigManager {

    private static final Map<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();

    static {
        Pattern pattern = Pattern.compile("(.*?): (.*)");
        try {
            Document document = Jsoup.connect("https://docs.pufferfish.host/setup/pufferfish-fork-configuration/").userAgent("Mozilla").get();
            for (Element child : document.select(".post-content").get(0).select("p")) {
                String text = child.text();
                Optional<MatchResult> optional = pattern.matcher(text).results().findFirst();
                if (optional.isPresent()) {
                    MatchResult matchResult = optional.get();
                    String key = matchResult.group(1).replace('.', '/');
                    String description = matchResult.group(2);
                    PROPERTY_DESCRIPTIONS.put(key, description);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PufferfishConfigManager() {
        super("pufferfish.yml", s -> s.modLoader == ModLoader.PUFFERFISH);
    }

    @Override
    protected String getPath(Server server) {
        return server.serverLocation + "/" + name;
    }

    @Override
    public String getDescription(String key) {
        return PROPERTY_DESCRIPTIONS.get(key);
    }

    @Override
    public String getDataType(String key) {
        return null;
    }

    @Override
    public String getDefaultValue(String key) {
        return null;
    }

    @Override
    protected String optimise(int sliderValue, ConfigProperty property) {
        return property.value;
    }
}
