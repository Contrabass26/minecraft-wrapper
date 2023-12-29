package me.jsedwards.util;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;

public class OSUtils {

    public static final String settingsLocation;
    public static final String dataDir;
    public static final String serversLocation;
    public static final String userHome;

    static {
        String username = System.getProperty("user.name");
        dataDir = getAppDataLocation().formatted(username) + "minecraft-wrapper";
        settingsLocation = dataDir + File.separator + "settings.json";
        serversLocation = dataDir + File.separator + "servers.json";
        userHome = getUserHome().formatted(username);
    }

    private OSUtils() {}

    private static String getAppDataLocation() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "C:\\Users\\%s\\AppData\\Roaming\\";
        }
        if (SystemUtils.IS_OS_MAC) {
            return "/Users/%s/Library/Application Support/";
        }
        if (SystemUtils.IS_OS_LINUX) {
            return "/home/%s/.";
        }
        throw new RuntimeException("Operating system not supported: " + System.getProperty("os.name"));
    }

    private static String getUserHome() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "C:\\Users\\%s";
        }
        if (SystemUtils.IS_OS_MAC) {
            return "/Users/%s";
        }
        if (SystemUtils.IS_OS_LINUX) {
            return "/home/%s";
        }
        throw new RuntimeException("Operating system not supported: " + System.getProperty("os.name"));
    }

    public static void createDataDir() {
        new File(dataDir).mkdirs();
    }

    public static File getSettingsFile() {
        return new File(settingsLocation);
    }

    public static File getServersFile() {
        return new File(serversLocation);
    }
}
