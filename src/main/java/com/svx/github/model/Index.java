package com.svx.github.model;

import com.svx.github.utility.HashUtility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Index {
    private final Map<String, String> stagedFiles;
    private final Repository repository;

    public Index(Repository repository) {
        this.repository = repository;
        this.stagedFiles = new HashMap<>();
        loadFromDisk();
    }

    public void addFile(String filename, String blobId) {
        stagedFiles.put(filename, blobId);
        saveToDisk();
    }

    public void detectAndStageChanges() {
        Path repoPath = repository.path();
        try {
            Files.walk(repoPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.startsWith(repoPath.resolve(".git")))
                    .forEach(this::processFileForStaging);
        } catch (IOException e) {
            System.out.println("Error scanning repository for changes: " + e.getMessage());
        }
    }

    private void processFileForStaging(Path filePath) {
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            String blobId = computeSHA1(content);
            String relativePath = repository.path().relativize(filePath).toString();

            if (!stagedFiles.containsKey(relativePath) || !stagedFiles.get(relativePath).equals(blobId)) {
                Blob blob = new Blob(content, repository);
                addFile(relativePath, blob.getId());
                System.out.println("Staged file: " + relativePath);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + filePath + " - " + e.getMessage());
        }
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

    public Map<String, String> getStagedFiles() {
        return stagedFiles;
    }

    private void saveToDisk() {
        Path indexPath = repository.getIndexPath();
        try (BufferedWriter writer = Files.newBufferedWriter(indexPath, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> entry : stagedFiles.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
            System.out.println("Index saved to disk at " + indexPath);
        } catch (IOException e) {
            System.out.println("Error saving index to disk: " + e.getMessage());
        }
    }

    private void loadFromDisk() {
        Path indexPath = repository.getIndexPath();
        if (Files.exists(indexPath)) {
            try (BufferedReader reader = Files.newBufferedReader(indexPath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" ", 2);
                    if (parts.length == 2) {
                        stagedFiles.put(parts[0], parts[1]);
                    }
                }
                System.out.println("Index loaded from disk.");
            } catch (IOException e) {
                System.out.println("Error loading index from disk: " + e.getMessage());
            }
        }
    }

    public void clear() {
        stagedFiles.clear();
        saveToDisk();
        System.out.println("Index cleared.");
    }
}

