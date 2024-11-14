package com.svx.github.model;

import com.svx.github.utility.HashUtility;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Tree {
    private final Map<String, String> entries;
    private final String id;
    private final Repository repository;

    public Tree(Repository repository) {
        this.entries = new HashMap<>();
        this.repository = repository;
        this.id = HashUtility.computeSHA1(getContentForHashing());
    }

    public void addEntry(String name, String objectId) {
        entries.put(name, objectId);
    }

    public String getId() {
        return id;
    }

    public List<String> getFilenames() {
        return new ArrayList<>(entries.keySet());
    }

    public String getBlobId(String filename) {
        return entries.get(filename);
    }

    public String getBlobContent(String filename) {
        String blobId = entries.get(filename);
        if (blobId == null) return null;

        try {
            Blob blob = Blob.loadFromDisk(blobId, repository);
            return blob.getContent();
        } catch (IOException e) {
            System.out.println("Error loading blob content for " + filename + ": " + e.getMessage());
            return null;
        }
    }

    private String getContentForHashing() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            builder.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
        }
        return builder.toString();
    }

    public void saveToDisk() {
        Path objectDir = repository.getObjectsPath().resolve(id.substring(0, 2));
        Path objectPath = objectDir.resolve(id.substring(2));

        try {
            Files.createDirectories(objectDir);

            try (FileOutputStream fos = new FileOutputStream(objectPath.toFile());
                 DeflaterOutputStream dos = new DeflaterOutputStream(fos)) {
                dos.write(getContentForHashing().getBytes(StandardCharsets.UTF_8));
            }

            System.out.println("Tree saved to disk with ID: " + id);
        } catch (IOException e) {
            System.out.println("Error saving tree to disk: " + e.getMessage());
        }
    }

    public static Tree loadFromDisk(String id, Repository repository) throws IOException {
        Path objectPath = repository.getObjectsPath().resolve(id.substring(0, 2)).resolve(id.substring(2));

        try (FileInputStream fis = new FileInputStream(objectPath.toFile());
             InflaterInputStream iis = new InflaterInputStream(fis);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = iis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            Tree tree = new Tree(repository);
            String content = baos.toString(StandardCharsets.UTF_8);
            for (String line : content.split("\n")) {
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    tree.addEntry(parts[0], parts[1]);
                }
            }

            return tree;
        }
    }
}
