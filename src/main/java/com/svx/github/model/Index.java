package com.svx.github.model;

import com.svx.github.manager.RepositoryManager;
import com.svx.github.utility.FileUtility;
import com.svx.github.utility.JsonUtility;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Index {
    private final Map<String, String> stagedFiles;

    public Index() {
        this.stagedFiles = new HashMap<>();
    }

    public Map<String, String> getStagedFiles() {
        return new HashMap<>(stagedFiles);
    }

    public void clear() {
        stagedFiles.clear();
    }

    private void saveToIndexFile(Repository repository) throws IOException {
        Path indexPath = repository.getIndexPath();
        String serializedIndex = JsonUtility.serialize(stagedFiles);
        Files.writeString(indexPath, serializedIndex, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void loadFromIndexFile(Repository repository) throws IOException {
        if (repository == null) {
            return;
        }
        Path indexPath = repository.getIndexPath();

        if (Files.exists(indexPath)) {
                String serializedIndex = Files.readString(indexPath).trim();
                if (!serializedIndex.isEmpty()) {
                    Map<String, String> loadedStagedFiles = JsonUtility.deserialize(serializedIndex);
                    stagedFiles.clear();
                    stagedFiles.putAll(loadedStagedFiles);
                }
        }
    }

    public void detectAndStageChanges(Repository repository) throws IOException {
        Path repoPath = repository.getPath();
        VersionControl versionControl = RepositoryManager.getVersionControl();
        Commit latestCommit = versionControl.getCurrentCommit();
        Tree latestTree;

        latestTree = latestCommit != null ? Tree.loadFromDisk(latestCommit.getTreeId(), repository.getObjectsPath()) : null;

        try (Stream<Path> paths = Files.walk(repoPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> !path.startsWith(repository.getGitPath()))
                    .forEach(file -> {
                        try {
                            String blobId = Blob.computeBlobId(file);
                            String relativePath = repoPath.relativize(file).toString();

                            if (latestTree != null) {
                                String committedBlobId = latestTree.getEntries().get(relativePath);
                                if (committedBlobId != null && committedBlobId.equals(blobId)) {
                                    return;
                                }
                            }

                            try (BufferedReader reader = Files.newBufferedReader(file)) {
                                String content = reader.lines().collect(Collectors.joining("\n"));
                                Blob blob = new Blob(content, repository);
                                stagedFiles.put(relativePath, blob.getId());
                                FileUtility.saveToDisk(blob.getId(), blob.getContent(), repository.getObjectsPath());
                            }
                        } catch (Exception e) {
                            System.err.println("Error detecting and staging: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error walking through files: " + e.getMessage());
        }

        saveToIndexFile(repository);
    }
}

