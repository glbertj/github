package com.svx.github.model;

import com.svx.github.manager.RepositoryManager;
import com.svx.github.repository.BlobRepository;
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

    public Index() {
        this.stagedFiles = new HashMap<>();
    }

    public Map<String, String> getStagedFiles() {
        return new HashMap<>(stagedFiles);
    }

    public void addFile(String filePath, String blobId) {
        stagedFiles.put(filePath, blobId);
    }

    public void clear() {
        stagedFiles.clear();
    }

    public void detectAndStageChanges(Repository repository) {
        Path repoPath = repository.getPath();

        try {
            Files.walk(repoPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.startsWith(repository.getGitPath()))
                    .forEach(file -> {
                        try {
                            String blobId = Blob.computeBlobId(file);
                            if (!stagedFiles.containsKey(file.toString()) || !stagedFiles.get(file.toString()).equals(blobId)) {
                                Blob blob = new Blob(Files.readString(file), repository);
                                stagedFiles.put(repoPath.relativize(file).toString(), blob.getId());
                                BlobRepository.save(blob);
                            }
                        } catch (Exception e) {
                            System.out.println("Error processing file for staging: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.out.println("Error detecting changes: " + e.getMessage());
        }
    }
}

