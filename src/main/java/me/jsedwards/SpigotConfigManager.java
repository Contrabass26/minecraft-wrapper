package me.jsedwards;

import me.jsedwards.gui.Server;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class SpigotConfigManager extends YamlConfigManager {

    private static final HashMap<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DATA_TYPES = new HashMap<>();
    private static final HashMap<String, String> PROPERTY_DEFAULTS = new HashMap<>();

    static {
        Pattern pattern = Pattern.compile("Default: ((?:.(?!Type:))*) Type: ((?:.(?!Description:))*) Description: ((?:.(?!Default:))*)");
        try {
            Document document = Jsoup.connect("https://www.spigotmc.org/wiki/spigot-configuration/").userAgent("Mozilla").get();
            List<Node> children = document.select(".page-content").get(0).childNodes();
            for (int i = 0; i < children.size(); i++) {
                Node node = children.get(i);
                if (node instanceof Element child && child.is("span")) {
                    String key = child.text();
                    if (PROPERTY_DESCRIPTIONS.containsKey(key)) {
                        PROPERTY_DESCRIPTIONS.put(key, "Not found");
                    } else {
                        StringBuilder text = new StringBuilder();
                        for (int j = i + 1; j < children.size(); j++) {
                            Node candidate = children.get(j);
                            if (candidate instanceof Element element) {
                                if (element.is("style")) continue;
                                if (element.is("span")) {
                                    Optional<MatchResult> matcher = pattern.matcher(text).results().findFirst();
                                    if (matcher.isPresent()) {
                                        MatchResult matchResult = matcher.get();
                                        PROPERTY_DEFAULTS.put(key, matchResult.group(1));
                                        PROPERTY_DATA_TYPES.put(key, matchResult.group(2));
                                        PROPERTY_DESCRIPTIONS.put(key, matchResult.group(3));
                                    }
                                    break;
                                }
                                text.append(element.text());
                            } else if (candidate instanceof TextNode textNode) {
                                text.append(textNode.text());
                            }
                        }
                    }
                }
            }
            LOGGER.info(PROPERTY_DESCRIPTIONS.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SpigotConfigManager(Server server) {
        super(server, s -> s.serverLocation + File.separator + "spigot.yml");
    }

    @Override
    public String getDescription(String key) {
        return PROPERTY_DESCRIPTIONS.getOrDefault(StringUtils.substringAfterLast(key, '/'), "Not found");
    }

    @Override
    public String getDataType(String key) {
        return PROPERTY_DATA_TYPES.getOrDefault(StringUtils.substringAfterLast(key, '/'), "Not found");
    }

    @Override
    public String getDefaultValue(String key) {
        return PROPERTY_DEFAULTS.getOrDefault(StringUtils.substringAfterLast(key, '/'), "Not found");
    }

    @Override
    public void optimise(int sliderValue) {

    }
}
