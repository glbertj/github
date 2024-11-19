package com.svx.github.model;

import com.svx.github.manager.RepositoryManager;
import com.svx.github.repository.BlobRepository;
import com.svx.github.repository.CommitRepository;
import com.svx.github.repository.RepositoryRepository;
import com.svx.github.repository.TreeRepository;
import com.svx.github.utility.FileUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VersionControl {
    private final Repository repository;
    private final Index index;
    private Commit currentCommit;

    public VersionControl(Repository repository) {
        this.repository = repository;
        this.index = new Index();
        this.index.loadFromIndexFile(repository);
        loadCurrentCommit();
    }

    public Index getIndex() {
        return index;
    }

    public Commit getCurrentCommit() {
        return currentCommit;
    }

    public void setCurrentCommit(Commit commit) {
        this.currentCommit = commit;
    }

    private void loadCurrentCommit() {
        String headCommitId = repository.getLatestCommitId();
        if (headCommitId != null) {
            this.currentCommit = CommitRepository.load(headCommitId, repository);
        }
    }

    public void commitChanges(String message) {
        if (index.getStagedFiles().isEmpty()) {
            System.out.println("No files staged for commit.");
            return;
        }

        Tree newTree = new Tree(index.getStagedFiles());
        newTree.saveToDisk(repository.getObjectsPath());

        Commit newCommit = new Commit(newTree.getId(), message, currentCommit != null ? currentCommit.getId() : null);
        newCommit.saveToDisk(repository.getObjectsPath());

        updateHeadFile(newCommit.getId());
        currentCommit = newCommit;
        repository.setLatestCommitId(newCommit.getId());

        index.clear();
        System.out.println("Commit created locally with ID: " + newCommit.getId());
    }

    private void updateHeadFile(String commitId) {
        Path headFilePath = repository.getGitPath().resolve("refs").resolve("heads").resolve("master");
        try {
            Files.createDirectories(headFilePath.getParent());
            Files.writeString(headFilePath, commitId);
        } catch (IOException e) {
            System.out.println("Error updating HEAD file: " + e.getMessage());
        }
    }

    public void push() {
        if (currentCommit == null) {
            System.out.println("No commits to push.");
            return;
        }

        List<Commit> commitsToPush = getCommitsToPush();
        if (commitsToPush.isEmpty()) {
            System.out.println("No new commits to push.");
            return;
        }

        for (Commit commit : commitsToPush) {
            Tree tree = Tree.loadFromDisk(commit.getTreeId(), repository.getObjectsPath());
            TreeRepository.save(tree);

            for (String blobId : tree.getEntries().values()) {
                if (BlobRepository.load(blobId, repository) == null) {
                    String blobContent = FileUtility.loadFromDisk(blobId, repository.getObjectsPath());
                    Blob blob = new Blob(blobContent, repository);
                    BlobRepository.save(blob);
                    System.out.println("Blob saved to database: " + blob.getId());
                }
            }
            CommitRepository.save(commit);
            System.out.println("Commit saved to database: " + commit.getId());
        }

        RepositoryRepository.save(repository);
        System.out.println("Push completed. Repository head updated to commit: " + currentCommit.getId());
    }

    private List<Commit> getCommitsToPush() {
        List<Commit> commitsToPush = new ArrayList<>();
        Commit commitToPush = currentCommit;

        while (commitToPush != null) {
            if (CommitRepository.load(commitToPush.getId(), repository) != null) {
                break;
            }
            commitsToPush.add(commitToPush);

            try {
                commitToPush = commitToPush.getParentId() != null
                        ? Commit.loadFromDisk(commitToPush.getParentId(), repository.getObjectsPath())
                        : null;
            } catch (IOException e) {
                System.out.println("Error loading parent commit: " + e.getMessage());
                break;
            }
        }

        Collections.reverse(commitsToPush);
        return commitsToPush;
    }

    public void pull() {
        String latestDatabaseCommitId = RepositoryRepository.getLatestCommitId(repository);

        if (latestDatabaseCommitId == null) {
            System.out.println("No commits found in the database for this repository.");
            return;
        }

        Path objectsPath = repository.getObjectsPath();

        try {
            Commit commitToPull = CommitRepository.load(latestDatabaseCommitId, repository);
            while (commitToPull != null) {
                commitToPull.saveToDisk(objectsPath);

                Tree treeToPull = TreeRepository.load(commitToPull.getTreeId(), repository);
                if (treeToPull != null) {
                    treeToPull.saveToDisk(objectsPath);

                    for (String blobId : treeToPull.getEntries().values()) {
                        if (!Files.exists(objectsPath.resolve(blobId.substring(0, 2)).resolve(blobId.substring(2)))) {
                            Blob blobToPull = BlobRepository.load(blobId, repository);
                            if (blobToPull != null) {
                                FileUtility.saveToDisk(blobToPull.getId(), blobToPull.getContent(), objectsPath);
                            }
                        }
                    }
                }

                restoreFilesFromCommit(commitToPull, repository);

                String parentId = commitToPull.getParentId();
                commitToPull = (parentId != null) ? CommitRepository.load(parentId, repository) : null;
            }

            Path headFilePath = repository.getGitPath().resolve("refs").resolve("heads").resolve("master");
            Files.createDirectories(headFilePath.getParent());
            Files.writeString(headFilePath, latestDatabaseCommitId);

            repository.setLatestCommitId(latestDatabaseCommitId);
            RepositoryManager.setCurrentRepository(repository);

            System.out.println("Pull completed successfully.");
        } catch (IOException e) {
            System.out.println("Error during pull operation: " + e.getMessage());
        }
    }

    private void restoreFilesFromCommit(Commit commit, Repository repository) {
        try {
            Tree tree = Tree.loadFromDisk(commit.getTreeId(), repository.getObjectsPath());
            for (Map.Entry<String, String> entry : tree.getEntries().entrySet()) {
                String filename = entry.getKey();
                String blobId = entry.getValue();

                String blobContent = FileUtility.loadFromDisk(blobId, repository.getObjectsPath());

                Path filePath = repository.getPath().resolve(filename);
                Files.createDirectories(filePath.getParent());
                Files.writeString(filePath, blobContent);

                System.out.println("Restored file: " + filename);
            }
        } catch (IOException e) {
            System.out.println("Error restoring files from commit: " + e.getMessage());
        }
    }

    private boolean isCommitPresentLocally(String commitId) {
        Path commitPath = repository.getObjectsPath()
                .resolve(commitId.substring(0, 2))
                .resolve(commitId.substring(2));
        return Files.exists(commitPath);
    }
}