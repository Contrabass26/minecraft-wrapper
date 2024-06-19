package me.jsedwards.util;

import me.jsedwards.dashboard.ConsoleWrapper;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class OSUtils {

    private static final Logger LOGGER = LogManager.getLogger("OSUtils");

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
                new ConsoleWrapper("where java", new File("C:/"), javaVersions::add, s -> {}, () -> LOGGER.info("Found %s Java versions on Windows".formatted(javaVersions.size())));
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
        }
    }
}
