package me.jsedwards.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class JsonUtils {

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

    private static <T> T readJson(BufferedReader reader, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(reader, clazz);
    }

    public static <T> T readJson(File file, TypeReference<T> type) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(reader, type);
        }
    }

    public static <T> T readJson(InputStream stream, TypeReference<T> type) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(reader, type);
        }
    }

    public static <T> void writeJson(File file, T value) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, value);
    }
}
