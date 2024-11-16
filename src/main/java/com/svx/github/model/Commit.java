package com.svx.github.model;

import com.svx.github.utility.CompressionUtility;
import com.svx.github.utility.HashUtility;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Commit {
    private final UUID id; // UUID for commit
    private final String treeId;
    private final String parentId;
    private final String message;
    private final LocalDateTime timestamp;
    private final Repository repository;
    private final String databaseUuid;

    public Commit(String treeId, String message, String parentId, Repository repository) {
        this.treeId = treeId;
        this.message = message;
        this.parentId = parentId;
        this.timestamp = LocalDateTime.now();
        this.repository = repository;
        this.id = UUID.randomUUID();  // Generate a unique commit ID
        this.databaseUuid = UUID.randomUUID().toString();  // Generate a UUID for the database
        saveToDisk();  // Save the commit to disk immediately
    }

    public Commit(UUID id, String treeId, String parentId, String message, LocalDateTime timestamp, String databaseUuid, Repository repository) {
        this.id = id;
        this.treeId = treeId;
        this.parentId = parentId;
        this.message = message;
        this.timestamp = timestamp;
        this.repository = repository;
        this.databaseUuid = databaseUuid;
    }

    public UUID getId() {
        return id;
    }

    public Tree getTree() {
        try {
            return Tree.loadFromDisk(treeId, repository);
        } catch (IOException e) {
            System.out.println("Error loading tree: " + e.getMessage());
            return null;
        }
    }

    public String getTreeId() {
        return treeId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDatabaseUuid() {
        return databaseUuid;
    }

    public Repository getRepository() {
        return repository;
    }

    private String getContentForHashing() {
        return "tree " + treeId + "\n" +
                (parentId != null ? "parent " + parentId + "\n" : "") +
                "message " + message + "\n" +
                "timestamp " + timestamp.toString() + "\n";
    }

    public void saveToDisk() {
        Path objectDir = repository.getObjectsPath().resolve(id.toString().substring(0, 2));
        Path objectPath = objectDir.resolve(id.toString().substring(2));

        try {
            Files.createDirectories(objectDir);
            byte[] compressedContent = CompressionUtility.compress(getContentForHashing());
            Files.write(objectPath, compressedContent);
            System.out.println("Commit saved to disk with ID: " + id);
        } catch (IOException e) {
            System.out.println("Error saving commit to disk: " + e.getMessage());
        }
    }

    public static Commit loadFromDisk(UUID id, Repository repository) throws IOException {
        Path objectPath = repository.getObjectsPath().resolve(id.toString().substring(0, 2)).resolve(id.toString().substring(2));

        byte[] compressedContent = Files.readAllBytes(objectPath);
        String content = CompressionUtility.decompress(compressedContent);

        String treeId = null, parentId = null, message = null;
        LocalDateTime timestamp = null;

        for (String line : content.split("\n")) {
            if (line.startsWith("tree ")) treeId = line.substring(5).trim();
            else if (line.startsWith("parent ")) parentId = line.substring(7).trim();
            else if (line.startsWith("message ")) message = line.substring(8).trim();
            else if (line.startsWith("timestamp ")) timestamp = LocalDateTime.parse(line.substring(10).trim());
        }

        return new Commit(id, treeId, parentId, message, timestamp, null, repository);
    }
}