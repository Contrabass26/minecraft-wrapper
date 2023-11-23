package me.jsedwards;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.stream.Collector;

public class MinecraftWrapperUtils {

    public static final Collector<String, StringBuilder, String> STRING_COLLECTOR = Collector.of(StringBuilder::new, StringBuilder::append, StringBuilder::append, StringBuilder::toString);

    public static <T> T readJson(File file, Class<T> clazz) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return readJson(reader, clazz);
        }
    }

    public static <T> T readJson(InputStream stream, Class<T> clazz) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return readJson(reader, clazz);
        }
    }

    private static <T> T readJson(BufferedReader reader, Class<T> clazz) {
        String json = reader.lines().collect(STRING_COLLECTOR);
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    public static <T> T readJson(File stream, TypeToken<T> type) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(stream))) {
            String json = reader.lines().collect(STRING_COLLECTOR);
            Gson gson = new Gson();
            return gson.fromJson(json, type);
        }
    }

    public static <T> void writeJson(File file, T value) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(value);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(json);
        }
    }

    public static String getUserFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return "/Users/" + System.getProperty("user.name");
        }
        // TODO: Windows and linux user home folder
        throw new RuntimeException("Unsupported operating system");
    }
}
