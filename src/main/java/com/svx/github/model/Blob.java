package com.svx.github.model;

import com.svx.github.utility.FileUtility;
import com.svx.github.utility.HashUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Blob {
    private final String id;
    private final String content;

    public Blob(String content, Repository repository) {
        this.id = HashUtility.computeSHA1(content);
        this.content = content;

        FileUtility.saveToDisk(id, content, repository.getObjectsPath());
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public static String computeBlobId(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return HashUtility.computeSHA1(content);
    }
}
