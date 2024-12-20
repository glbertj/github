package com.svx.github.model;

import com.svx.github.utility.FileUtility;
import com.svx.github.utility.HashUtility;
import com.svx.github.utility.JsonUtility;
import java.nio.file.Path;
import java.util.*;

public class Tree {
    private final String id;
    private final Map<String, String> entries;

    public Tree(Map<String, String> entries) {
        this.entries = new HashMap<>(entries);
        this.id = HashUtility.computeSHA1(JsonUtility.serialize(entries));
    }

    public void saveToDisk(Path objectsPath) {
        String serializedEntries = JsonUtility.serialize(entries);
        FileUtility.saveToDisk(id, serializedEntries, objectsPath);
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getEntries() {
        return new HashMap<>(entries);
    }

    public static Tree loadFromDisk(String id, Path objectsPath) {
        String serializedEntries = FileUtility.loadFromDisk(id, objectsPath);
        Map<String, String> entries = JsonUtility.deserialize(serializedEntries);
        return new Tree(entries);
    }
}
