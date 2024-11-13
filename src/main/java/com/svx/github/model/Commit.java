package com.svx.github.model;

import com.svx.github.utility.HashUtility;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Commit {
    private final String id;
    private final String treeId;
    private final String parentId;
    private final String message;
    private LocalDateTime timestamp;
    private final Repository repository;

    public Commit(String treeId, String message, String parentId, Repository repository) {
        this.treeId = treeId;
        this.message = message;
        this.parentId = parentId;
        this.timestamp = LocalDateTime.now();
        this.repository = repository;
        this.id = computeSHA1(getContentForHashing());
        saveToDisk();
    }

    public String getId() {
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

    public String getParentId() {
        return parentId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    private String computeSHA1(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encodedHash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HashUtility.bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found!");
        }
    }

    private String getContentForHashing() {
        return "tree " + treeId + "\n" +
                (parentId != null ? "parent " + parentId + "\n" : "") +
                "message " + message + "\n" +
                "timestamp " + timestamp.toString() + "\n";
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

            System.out.println("Commit saved to disk with ID: " + id);
        } catch (IOException e) {
            System.out.println("Error saving commit to disk: " + e.getMessage());
        }
    }

    public static Commit loadFromDisk(String id, Repository repository) throws IOException {
        Path objectPath = repository.getObjectsPath().resolve(id.substring(0, 2)).resolve(id.substring(2));

        try (FileInputStream fis = new FileInputStream(objectPath.toFile());
             InflaterInputStream iis = new InflaterInputStream(fis);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = iis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            String content = baos.toString(StandardCharsets.UTF_8);
            String treeId = null, parentId = null, message = null;
            LocalDateTime timestamp = null;

            for (String line : content.split("\n")) {
                if (line.startsWith("tree ")) treeId = line.substring(5).trim();
                else if (line.startsWith("parent ")) parentId = line.substring(7).trim();
                else if (line.startsWith("message ")) message = line.substring(8).trim();
                else if (line.startsWith("timestamp ")) timestamp = LocalDateTime.parse(line.substring(10).trim());
            }

            Commit commit = new Commit(treeId, message, parentId, repository);
            commit.timestamp = timestamp;
            return commit;
        }
    }
}
