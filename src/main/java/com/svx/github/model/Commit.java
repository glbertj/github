package com.svx.github.model;

import com.svx.github.utility.FileUtility;
import com.svx.github.utility.HashUtility;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Commit {
    private final String id;
    private final String treeId;
    private final String parentId;
    private final String message;
    private final LocalDateTime timestamp;

    public Commit(String treeId, String message, String parentId) {
        this.treeId = treeId;
        this.message = message;
        this.parentId = parentId;
        this.timestamp = LocalDateTime.now();
        this.id = HashUtility.computeSHA1(getContentForHashing());
    }

    public Commit(String id, String treeId, String parentId, String message, LocalDateTime timestamp) {
        this.id = id;
        this.treeId = treeId;
        this.parentId = parentId;
        this.message = message;
        this.timestamp = timestamp;
    }

    private String getContentForHashing() {
        return "tree " + treeId + "\n" +
                (parentId != null ? "parent " + parentId + "\n" : "") +
                "message " + message + "\n" +
                "timestamp " + timestamp.toString();
    }

    public void saveToDisk(Path objectsPath) {
        String content = getContentForHashing();
        FileUtility.saveToDisk(id, content, objectsPath);
    }

    public String getId() {
        return id;
    }

    public String getTreeId() {
        return treeId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public static Commit loadFromDisk(String id, Path objectsPath) {
        String content = FileUtility.loadFromDisk(id, objectsPath);

        String treeId = null, parentId = null, message = null;
        LocalDateTime timestamp = null;

        for (String line : content.split("\n")) {
            if (line.startsWith("tree ")) {
                treeId = line.substring(5).trim();
            } else if (line.startsWith("parent ")) {
                parentId = line.substring(7).trim();
            } else if (line.startsWith("message ")) {
                message = line.substring(8).trim();
            } else if (line.startsWith("timestamp ")) {
                timestamp = LocalDateTime.parse(line.substring(10).trim());
            }
        }

        return new Commit(id, treeId, parentId, message, timestamp);
    }
}