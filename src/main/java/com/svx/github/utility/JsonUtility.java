package com.svx.github.utility;

import java.io.*;
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

    public static void serializeToFile(Map<String, String> entries, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static Map<String, String> deserializeFromFile(String filePath) {
        Map<String, String> resultMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    resultMap.put(parts[0], parts[1]);
                } else {
                    System.err.println("Skipping invalid line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return resultMap;
    }
}