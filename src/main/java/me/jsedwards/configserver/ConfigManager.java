package me.jsedwards.configserver;

import me.jsedwards.dashboard.Server;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public enum ConfigManager {

    VANILLA("server.properties", s -> true, ServerPropertiesManager::new) {
        private static final HashMap<String, String> PROPERTY_DESCRIPTIONS = new HashMap<>();
        private static final HashMap<String, String> PROPERTY_DATA_TYPES = new HashMap<>();
        private static final HashMap<String, String> PROPERTY_DEFAULTS = new HashMap<>();

        static {
            try {
                Document document = Jsoup.connect("https://minecraft.wiki/w/Server.properties").userAgent("Mozilla").get();
                Element table = document.select("table[data-description=Server properties]").getFirst();
                Elements rows = table.select("tr");
                for (int i = 1; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    Elements cells = row.select("td");
                    String key = cells.get(0).text();
                    String description = cells.get(3).html();
                    String datatype = cells.get(1).text();
                    String defaultValue = cells.get(2).text();
                    PROPERTY_DESCRIPTIONS.put(key, description);
                    PROPERTY_DATA_TYPES.put(key, datatype);
                    PROPERTY_DEFAULTS.put(key, defaultValue);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getDescription(String key) {
            return PROPERTY_DESCRIPTIONS.get(key);
        }

        @Override
        public String getDefaultValue(String key) {
            return PROPERTY_DEFAULTS.get(key);
        }

        @Override
        public String getDataType(String key) {
            return PROPERTY_DATA_TYPES.get(key);
        }
    },
    BUKKIT(BukkitConfigManager::new),
    SPIGOT(SpigotConfigManager::new),
    PUFFERFISH(PufferfishConfigManager::new);

    public final String path;
    public final Predicate<Server> enabled;
    public final Supplier<AdvancedPanel> panelSupplier;

    ConfigManager(String path, Predicate<Server> enabled, Supplier<AdvancedPanel> panelSupplier) {
        this.path = path;
        this.enabled = enabled;
        this.panelSupplier = panelSupplier;
    }

    public abstract String getDescription(String key);

    public abstract String getDefaultValue(String key);

    public abstract String getDataType(String key);

    public abstract void addKeys(List<ConfigProperty> list, Server server);
}
