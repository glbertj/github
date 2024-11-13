package com.svx.github.model;

import com.svx.github.utility.HashUtility;

import java.time.LocalDateTime;

public class Commit {
    private final String id;
    private final Tree tree;
    private final String message;
    private final LocalDateTime timestamp;
    private final Commit parent;

    public Commit(Tree tree, String message, Commit parent) {
        this.tree = tree;
        this.message = message;
        this.parent = parent;
        this.timestamp = LocalDateTime.now();
        this.id = HashUtility.computeHash(tree.getId() + message + timestamp.toString() + (parent != null ? parent.getId() : ""));
    }

    public String getId() {
        return id;
    }

    public Tree getTree() {
        return tree;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Commit getParent() {
        return parent;
    }
}
