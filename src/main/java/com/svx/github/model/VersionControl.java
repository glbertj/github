package com.svx.github.model;

import com.svx.github.repository.BlobRepository;
import com.svx.github.repository.CommitRepository;
import com.svx.github.repository.TreeRepository;
import com.svx.github.utility.FileUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public void setCurrentCommit(Commit commit) {
        this.currentCommit = commit;
    }

    public Commit getCurrentCommit() {
        return currentCommit;
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

        Path headFilePath = repository.getGitPath().resolve("refs").resolve("heads").resolve("master");
        try {
            Files.createDirectories(headFilePath.getParent());
            Files.writeString(headFilePath, newCommit.getId());
        } catch (IOException e) {
            System.out.println("Error updating HEAD file: " + e.getMessage());
        }

        currentCommit = newCommit;
        repository.setLatestCommitId(newCommit.getId());

        System.out.println("Commit created locally with ID: " + newCommit.getId());
    }

    public void push() {
        if (currentCommit == null) {
            System.out.println("No commits to push.");
            return;
        }

        Commit commitToPush = currentCommit;
        while (commitToPush != null) {
            if (CommitRepository.load(commitToPush.getId(), repository) != null) break;

            CommitRepository.save(commitToPush);

            Tree tree = Tree.loadFromDisk(commitToPush.getTreeId(), repository.getObjectsPath());
            TreeRepository.save(tree);

            for (String blobId : tree.getEntries().values()) {
                if (BlobRepository.load(blobId, repository) == null) { // Avoid duplicate saves
                    String blobContent = FileUtility.loadFromDisk(blobId, repository.getObjectsPath());
                    Blob blob = new Blob(blobContent, repository);
                    BlobRepository.save(blob);
                }
            }

            commitToPush = commitToPush.getParentId() != null
                    ? CommitRepository.load(commitToPush.getParentId(), repository)
                    : null;
        }

        System.out.println("Push completed.");
    }
}