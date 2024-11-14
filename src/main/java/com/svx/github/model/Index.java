package com.svx.github.model;

import com.svx.github.manager.RepositoryManager;
import com.svx.github.utility.HashUtility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
        Commit latestCommit = RepositoryManager.getVersionControl().getCurrentCommit();
        Tree lastCommitTree = (latestCommit != null) ? latestCommit.getTree() : null;

        try (Stream<Path> paths = Files.walk(repoPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> !path.startsWith(repoPath.resolve(".git")))
                    .forEach(path -> {
                        String filename = repoPath.relativize(path).toString();

                        String currentBlobId = null;
                        try {
                            currentBlobId = Blob.computeBlobId(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        if (lastCommitTree == null || !currentBlobId.equals(lastCommitTree.getBlobId(filename))) {
                            processFileForStaging(path);
                        }
                    });
        } catch (IOException e) {
            System.out.println("Error scanning repository for changes: " + e.getMessage());
        }
    }

    private void processFileForStaging(Path filePath) {
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            String blobId = HashUtility.computeSHA1(content);
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

