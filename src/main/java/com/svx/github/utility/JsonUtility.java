package com.svx.github.utility;

import java.util.HashMap;
import java.util.Map;

public class JsonUtility {

    public static String serialize(Map<String, String> entries) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public static Map<String, String> deserialize(String json) {
        Map<String, String> resultMap = new HashMap<>();
        String[] lines = json.split("\n");

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    resultMap.put(parts[0], parts[1]);
                }
            }
        }
        return resultMap;
    }
}