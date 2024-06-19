package me.jsedwards.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;

public class MinecraftUtils {
    public static String getPlayerUuid(String username) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new URL("https://api.mojang.com/users/profiles/minecraft/" + username));
            return formatUuid(root.get("id").textValue());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Player username has no associated UUID", "Invalid player name", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static String formatUuid(String uuid) {
        StringBuilder builder = new StringBuilder(uuid);
        builder.insert(20, '-');
        builder.insert(16, '-');
        builder.insert(12, '-');
        builder.insert(8, '-');
        return builder.toString();
    }

    /**
     * @param v1 First version to compare
     * @param v2 Second version to compare
     * @return -1 if the first is earlier (less) than the second; 0 if the versions are equal; 1 if the first is later (greater) than the second
     */
    public static int compareVersions(String v1, String v2) {
        int[] nums1 = convertVersion(v1);
        int[] nums2 = convertVersion(v2);
        for (int i = 0; i < 3; i++) {
            if (nums1[i] != nums2[i]) {
                return Integer.compare(nums1[i], nums2[i]);
            }
        }
        return 0;
    }

    public static int[] convertVersion(String version) {
        int[] nums = new int[]{0, 0, 0};
        String[] split = version.split("\\.");
        for (int i = 0; i < split.length; i++) {
            nums[i] = Integer.parseInt(split[i]);
        }
        return nums;
    }

    public static boolean looksLikeVersion(String s) {
        return s.matches("[0-9]+\\.[0-9]+(?:\\.[0-9]+)?");
    }
}
