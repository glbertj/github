package com.svx.github.model;

import com.svx.github.manager.RepositoryManager;
import com.svx.github.utility.FileUtility;
import com.svx.github.utility.JsonUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

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

    private void saveToIndexFile(Repository repository) {
        Path indexPath = repository.getIndexPath();

        try {
            String serializedIndex = JsonUtility.serialize(stagedFiles);
            Files.writeString(indexPath, serializedIndex, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error saving index to file: " + e.getMessage());
        }
    }

    public void loadFromIndexFile(Repository repository) {
        Path indexPath = repository.getIndexPath();

        if (Files.exists(indexPath)) {
            try {
                String serializedIndex = Files.readString(indexPath).trim();
                if (!serializedIndex.isEmpty()) {
                    Map<String, String> loadedStagedFiles = JsonUtility.deserialize(serializedIndex);
                    stagedFiles.clear();
                    stagedFiles.putAll(loadedStagedFiles);
                }
            } catch (IOException e) {
                System.out.println("Error loading index from file: " + e.getMessage());
            }
        } else {
            System.out.println("Index file does not exist for repository: " + repository.getName());
        }
    }

    public void detectAndStageChanges(Repository repository) {
        Path repoPath = repository.getPath();
        VersionControl versionControl = RepositoryManager.getVersionControl();
        Commit latestCommit = versionControl.getCurrentCommit();
        Tree latestTree;

        if (latestCommit != null) {
            latestTree = Tree.loadFromDisk(latestCommit.getTreeId(), repository.getObjectsPath());
        } else {
            latestTree = null;
        }

        try {
            Files.walk(repoPath)
                    .filter(Files::isRegularFile)
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

                            Blob blob = new Blob(Files.readString(file), repository);
                            stagedFiles.put(relativePath, blob.getId());
                            FileUtility.saveToDisk(blob.getId(), blob.getContent(), repository.getObjectsPath());
                        } catch (Exception e) {
                            System.out.println("Error processing file for staging: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.out.println("Error detecting changes: " + e.getMessage());
        }

        saveToIndexFile(repository);
    }
}

