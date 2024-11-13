package com.svx.github.model;

import com.svx.github.manager.ReferenceManager;

import java.util.Map;

public class VersionControl {
    private final ReferenceManager referenceManager;
    private final Index index;

    public VersionControl() {
        this.referenceManager = new ReferenceManager();
        this.index = new Index();
    }

    public Index getIndex() {
        return index;
    }

    public void commitChanges(String message) {
        if (index.getStagedFiles().isEmpty()) {
            System.out.println("No files staged for commit.");
            return;
        }

        Tree newTree = new Tree();
        for (Map.Entry<String, Blob> entry : index.getStagedFiles().entrySet()) {
            newTree.addEntry(entry.getKey(), entry.getValue());
        }

        Commit newCommit;
        if (referenceManager.hasCommits()) {
            newCommit = new Commit(newTree, message, referenceManager.getHead());
        } else {
            newCommit = new Commit(newTree, message, null);
        }

        referenceManager.setHead(newCommit);
        index.clear();
        System.out.println("Commit created with ID: " + newCommit.getId());
    }


    public Commit getCurrentCommit() {
        return referenceManager.getHead();
    }
}