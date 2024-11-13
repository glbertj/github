package com.svx.github.model;

import java.util.HashMap;
import java.util.Map;

public class Index {
    private final Map<String, Blob> stagedFiles = new HashMap<>();

    public void addFile(String filename, String content) {
        Blob blob = new Blob(content);
        stagedFiles.put(filename, blob);
    }

    public Map<String, Blob> getStagedFiles() {
        return stagedFiles;
    }

    public void clear() {
        stagedFiles.clear();
    }
}

