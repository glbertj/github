package com.svx.github.model;

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

        boolean isFirstCommit = RepositoryRepository.loadById(repository.getId()) == null;

        if (isFirstCommit) {
            RepositoryRepository.save(repository);
            System.out.println("Repository saved to database as this is the first commit.");
        }

        List<Commit> commitsToPush = new ArrayList<>();
        Commit commitToPush = currentCommit;

        try {
            while (commitToPush != null) {
                commitsToPush.add(commitToPush);
                commitToPush = commitToPush.getParentId() != null
                        ? Commit.loadFromDisk(commitToPush.getParentId(), repository.getObjectsPath())
                        : null;
            }
        } catch (IOException e) {
            System.out.println("Error loading commit during push operation: " + e.getMessage());
            return;
        }

        Collections.reverse(commitsToPush);

        for (Commit commit : commitsToPush) {
            Tree tree = Tree.loadFromDisk(commit.getTreeId(), repository.getObjectsPath());
            TreeRepository.save(tree);

            for (Map.Entry<String, String> entry : tree.getEntries().entrySet()) {
                String blobId = entry.getValue();
                if (BlobRepository.load(blobId, repository) == null) {
                    String blobContent = FileUtility.loadFromDisk(blobId, repository.getObjectsPath());
                    Blob blob = new Blob(blobContent, repository);
                    BlobRepository.save(blob);
                }
            }

            CommitRepository.save(commit);
        }

        RepositoryRepository.updateHead(repository, currentCommit.getId());

        System.out.println("Push completed. Repository head updated to commit: " + currentCommit.getId());
    }

}