package me.jsedwards.util;

import me.jsedwards.dashboard.ConsoleWrapper;
import me.jsedwards.dashboard.Server;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OSUtils {

    private static final Logger LOGGER = LogManager.getLogger("OSUtils");
    public static final Pattern JAVA_VERSION_REGEX = Pattern.compile("[^.0-9]([0-9]+)");

    public static final String settingsLocation;
    public static final String dataDir;
    public static final String serversLocation;
    public static final String userHome;
    public static final long totalMemoryBytes;
    public static final List<String> javaVersions = Collections.synchronizedList(new ArrayList<>());

    static {
        String username = System.getProperty("user.name");
        dataDir = getAppDataLocation().formatted(username) + "minecraft-wrapper";
        settingsLocation = dataDir + File.separator + "settings.json";
        serversLocation = dataDir + File.separator + "servers.json";
        userHome = getUserHome().formatted(username);
        getJavaVersions();
        // Memory
        totalMemoryBytes = getSystemMemory();
        LOGGER.info("System memory detected as %s bytes".formatted(totalMemoryBytes));
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

    public static File getServersFile() {
        return new File(serversLocation);
    }

    public static void deleteDirectory(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        file.delete();
    }

    public static void deleteDirectory(Server server, String relativePath) {
        deleteDirectory(new File(server.serverLocation + "/" + relativePath));
    }

    private static long getSystemMemory() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            return (long) mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), "TotalPhysicalMemorySize");
        } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException | MalformedObjectNameException e) {
            LOGGER.error("Failed to read system memory", e);
        }
        return 8589934592L; // Default to 8GB
    }

    private static void getJavaVersions() {
        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                new ConsoleWrapper("where java", new File("C:/"), javaVersions::add, s -> {}, () -> {
                    sortJavaVersions();
                    LOGGER.info("Found %s Java versions on Windows".formatted(javaVersions.size()));
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (SystemUtils.IS_OS_MAC) {
            File root = new File("/Library/Java/JavaVirtualMachines");
            File[] children = root.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.isDirectory()) {
                        javaVersions.add(child.getAbsolutePath() + "/Contents/Home/bin/java");
                    }
                }
            }
            sortJavaVersions();
        }
    }

    private static void sortJavaVersions() {
        synchronized (javaVersions) {
            javaVersions.sort(new Comparator<>() {
                @Override
                public int compare(String o1, String o2) {
                    int v1 = getValue(o1);
                    int v2 = getValue(o2);
                    return Integer.compare(v2, v1); // Reverse order
                }

                private int getValue(String version) {
                    AtomicInteger value = new AtomicInteger(0);
                    Matcher matcher = JAVA_VERSION_REGEX.matcher(version);
                    matcher.results().findFirst().ifPresent(matchResult -> value.set(Integer.parseInt(matchResult.group(1))));
                    return value.get();
                }
            });
        }
    }
}
