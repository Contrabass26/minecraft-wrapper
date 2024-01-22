package me.jsedwards.util;

import org.apache.commons.lang3.StringUtils;

public class Identifier implements Comparable<Identifier> {

    public final String namespace;
    public final String path;

    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public Identifier(String s) {
        this.namespace = StringUtils.substringBefore(s, ':');
        this.path = StringUtils.substringAfter(s, ':');
    }

    public static String getShortPath(String path) {
        if (path.contains("/")) {
            return StringUtils.substringAfterLast(path, '/');
        }
        return path;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    @Override
    public int compareTo(Identifier other) {
        return toString().compareTo(other.toString());
    }
}
