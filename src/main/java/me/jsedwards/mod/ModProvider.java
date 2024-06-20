package me.jsedwards.mod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.jsedwards.modloader.ModLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public enum ModProvider {

    CURSEFORGE {
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

        public static String getAuthorString(JsonNode authorsNode) {
            List<String> authors = new ArrayList<>();
            for (JsonNode author : authorsNode) {
                authors.add(author.get("name").textValue());
            }
            return StringUtils.join(authors, ", ");
        }

        @Override
        public List<Project> search(String query, ModLoader loader, String mcVersion) {
            List<Project> mods = new ArrayList<>();
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
                        mods.add(createProject(datum));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return mods;
        }

        @Override
        public Project createProject(String id) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                InputStream stream = apiQuery("v1/mods/%s".formatted(id));
                JsonNode data = mapper.readTree(stream).get("data");
                return createProject(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Project createProject(JsonNode data) {
            String id = String.valueOf(data.get("id").intValue());
            ObjectMapper mapper = new ObjectMapper();
            Set<String> supportedVersions = new HashSet<>();
            InputStream stream = apiQuery("v1/mods/%s/files".formatted(id));
            try {
                JsonNode files = mapper.readTree(stream).get("data");
                for (JsonNode file : files) {
                    for (JsonNode version : file.get("gameVersions")) {
                        supportedVersions.add(version.textValue());
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to get supported versions", e);
            }
            return new Project(
                    id,
                    data.get("name").textValue(),
                    data.get("summary").textValue(),
                    getAuthorString(data.get("authors")),
                    data.get("logo").get("thumbnailUrl").textValue(),
                    data.get("downloadCount").intValue(),
                    new ArrayList<>(supportedVersions),
                    CURSEFORGE
            );
        }

        @Override
        public Project.ModFile getFile(Project project, ModLoader loader, String mcVersion) {
            InputStream stream = apiQuery("v1/mods/%s/files?gameVersion=%s&modLoaderType=%s".formatted(project.id, mcVersion, loader.getCfModLoaderType()));
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode intermediate = mapper.readTree(stream);
                JsonNode file = intermediate.get("data").get(0);
                String filename = file.get("fileName").textValue();
                String url = file.get("downloadUrl").textValue();
                return new Project.ModFile(url, filename, project);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    },
    MODRINTH {
        private static final String USER_AGENT = "Contrabass26/minecraft-wrapper";


        private static String formatUrl(String url) {
            return url
                    .replace("[", "%5B")
                    .replace("]", "%5D")
                    .replace("\"", "%22");
        }

        private static JsonNode doApiCall(String path) throws IOException {
            String url = formatUrl("https://api.modrinth.com/v2/" + path);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String content = reader.readLine();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(content);
        }

        @Override
        public List<Project> search(String query, ModLoader loader, String mcVersion) {
            try {
                String path = "search?query=%s&facets=[[\"versions:%s\"],[\"categories:%s\"],[\"server_side!=unsupported\"]]".formatted(query, mcVersion, loader.toString().toLowerCase());
                ArrayNode hits = (ArrayNode) doApiCall(path).get("hits");
                List<Project> projects = new ArrayList<>();
                for (JsonNode hit : hits) {
                    projects.add(createProject(hit));
                }
                return projects;
            } catch (IOException e) {
                return new ArrayList<>();
            }
        }

        @Override
        public Project createProject(String id) {
            try {
                return createProject(doApiCall("project/%s".formatted(id)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Project createProject(JsonNode data) {
            JsonNode versions = getGameVersions(data);
            Set<String> supportedVersions = new HashSet<>();
            for (JsonNode version : versions) {
                supportedVersions.add(version.textValue());
            }
            return new Project(
                    getId(data),
                    data.get("title").textValue(),
                    data.get("description").textValue(),
                    getAuthorString(data),
                    data.get("icon_url").textValue(),
                    data.get("downloads").intValue(),
                    new ArrayList<>(supportedVersions),
                    MODRINTH
            );
        }

        private static JsonNode getGameVersions(JsonNode root) {
            if (root.has("game_versions")) {
                return root.get("game_versions");
            }
            return root.get("versions");
        }

        private static String getId(JsonNode root) {
            if (root.has("id")) return root.get("id").textValue();
            if (root.has("project_id")) return root.get("project_id").textValue();
            throw new IllegalStateException("No project ID found");
        }

        private static String getAuthorString(JsonNode root) {
            if (root.has("team")) {
                String teamId = root.get("team").textValue();
                try {
                    ArrayNode team = (ArrayNode) doApiCall("team/%s/members".formatted(teamId));
                    List<String> members = new ArrayList<>();
                    for (JsonNode member : team) {
                        members.add(member.get("user").get("username").textValue());
                    }
                    return StringUtils.join(members, ", ");
                } catch (IOException e) {
                    LOGGER.warn("Failed to get members of team %s".formatted(teamId), e);
                }
            }
            if (root.has("author")) {
                return root.get("author").textValue();
            }
            return "Not found";
        }

        @Override
        public Project.ModFile getFile(Project project, ModLoader loader, String mcVersion) {
            try {
                String path = "project/%s/version?loaders=[\"%s\"]&game_versions=[\"%s\"]".formatted(project.id, loader.toString().toLowerCase(), mcVersion);
                ArrayNode versions = (ArrayNode) doApiCall(path);
                for (JsonNode version : versions) {
                    if (version.get("version_type").textValue().equals("release")) {
                        ArrayNode files = (ArrayNode) version.get("files");
                        for (JsonNode file : files) {
                            String filename = file.get("filename").textValue();
                            if (filename.endsWith(".jar")) {
                                String url = file.get("url").textValue();
                                return new Project.ModFile(url, filename, project);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    };

    private static final Logger LOGGER = LogManager.getLogger();

    public abstract List<Project> search(String query, ModLoader loader, String mcVersion);

    public abstract Project createProject(String id);

    public abstract Project createProject(JsonNode data);

    public abstract Project.ModFile getFile(Project project, ModLoader loader, String mcVersion);
}
