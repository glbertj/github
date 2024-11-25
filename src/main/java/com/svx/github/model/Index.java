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
import java.util.concurrent.atomic.AtomicBoolean;

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

        if (latestCommit != null) {
            latestTree = Tree.loadFromDisk(latestCommit.getTreeId(), repository.getObjectsPath());
        } else {
            latestTree = null;
        }

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
                        System.err.println("Error detect and stage: " + e.getMessage());
                    }
                });

        saveToIndexFile(repository);
    }
}

