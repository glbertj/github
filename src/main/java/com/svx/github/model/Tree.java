package com.svx.github.model;

import com.svx.github.utility.CompressionUtility;
import com.svx.github.utility.HashUtility;
import com.svx.github.utility.JsonUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Tree {
    private final String id;
    private final String databaseUuid;
    private final Map<String, String> entries;
    private final Repository repository;

    public Tree(Map<String, String> entries, Repository repository) {
        this.entries = entries;
        this.id = HashUtility.computeSHA1(getContentForHashing());
        this.databaseUuid = UUID.randomUUID().toString();
        this.repository = repository;
    }

    public Tree(String id, Map<String, String> entries, String databaseUuid, Repository repository) {
        this.id = id;
        this.entries = entries;
        this.databaseUuid = databaseUuid;
        this.repository = repository;
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

    public void saveToDisk() throws IOException {
        Path treePath = repository.getObjectsPath().resolve(id);
        String content = JsonUtility.serialize(entries);
        byte[] compressedContent = CompressionUtility.compress(content);
        Files.write(treePath, compressedContent);
    }

    public static Tree loadFromDisk(String treeId, Repository repository) throws IOException {
        Path treePath = repository.getObjectsPath().resolve(treeId);
        byte[] compressedContent = Files.readAllBytes(treePath);
        String content = CompressionUtility.decompress(compressedContent);
        Map<String, String> entries = JsonUtility.deserialize(content);
        return new Tree(entries, repository);
    }

    public String getId() {
        return id;
    }

    public String getDatabaseUuid() {
        return databaseUuid;
    }

    public void addEntry(String name, String objectId) {
        entries.put(name, objectId);
    }

    public Map<String, String> getEntries() {
        return entries;
    }

    public List<String> getFilenames() {
        return new ArrayList<>(entries.keySet());
    }

    public String getBlobId(String filename) {
        return entries.get(filename);
    }
}
