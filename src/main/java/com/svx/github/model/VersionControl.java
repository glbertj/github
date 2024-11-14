package com.svx.github.model;

import com.svx.github.manager.ReferenceManager;
import java.io.IOException;
import java.util.Map;

public class VersionControl {
    private final Repository repository;
    private final ReferenceManager referenceManager;
    private final Index index;
    private Commit currentCommit;

    public VersionControl(Repository repository) {
        this.repository = repository;
        this.referenceManager = new ReferenceManager(repository);
        this.index = new Index(repository);
        loadCurrentCommit();
    }

    public void commitChanges(String message) {
        if (index.getStagedFiles().isEmpty()) {
            System.out.println("No files staged for commit.");
            return;
        }

        Tree newTree = createTreeFromIndex();

        Commit newCommit = new Commit(newTree.getId(), message,
                currentCommit != null ? currentCommit.getId() : null,
                repository);
        newCommit.saveToDisk();

        try {
            referenceManager.saveHeadCommitId(newCommit.getId());
        } catch (IOException e) {
            System.out.println("Error updating HEAD reference: " + e.getMessage());
        }

        currentCommit = newCommit;
        index.clear();
        System.out.println("Commit created with ID: " + newCommit.getId());
    }

    private Tree createTreeFromIndex() {
        Tree newTree = new Tree(repository);

        for (Map.Entry<String, String> entry : index.getStagedFiles().entrySet()) {
            newTree.addEntry(entry.getKey(), entry.getValue());
        }

        newTree.saveToDisk();
        return newTree;
    }

    private void loadCurrentCommit() {
        try {
            String commitId = referenceManager.loadHeadCommitId();
            if (commitId != null) {
                currentCommit = Commit.loadFromDisk(commitId, repository);
            }
        } catch (IOException e) {
            System.out.println("Error loading current commit: " + e.getMessage());
            currentCommit = null;
        }
    }

    public Index getIndex() {
        return index;
    }

    public Commit getCurrentCommit() {
        return currentCommit;
    }
}