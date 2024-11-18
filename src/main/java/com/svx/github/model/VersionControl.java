package com.svx.github.model;

import com.svx.github.repository.CommitRepository;
import com.svx.github.repository.RepositoryRepository;
import com.svx.github.repository.TreeRepository;

public class VersionControl {
    private final Repository repository;
    private final Index index;
    private Commit currentCommit;

    public VersionControl(Repository repository) {
        this.repository = repository;
        this.index = new Index();
        loadCurrentCommit();
    }

    public Index getIndex() {
        return index;
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
        TreeRepository.save(newTree);

        Commit newCommit = new Commit(newTree.getId(), message, currentCommit != null ? currentCommit.getId() : null);
        CommitRepository.save(newCommit);

        currentCommit = newCommit;
        RepositoryRepository.updateHead(repository.getId(), newCommit.getId());

        index.clear();
        System.out.println("Commit created with ID: " + newCommit.getId());
    }
}