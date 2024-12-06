package com.svx.github.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitUtility {

    public static boolean hasRepository(Path path) {
        Path gitDir = path.resolve(".goat");

        return Files.isDirectory(gitDir) &&
                Files.isDirectory(gitDir.resolve("objects")) &&
                Files.isDirectory(gitDir.resolve("refs")) &&
                Files.isRegularFile(gitDir.resolve("config"));
    }

    public static String getLatestCommitIdFromHead(Path repositoryPath) {
        Path headFilePath = repositoryPath.resolve(".goat").resolve("refs").resolve("heads").resolve("master");
        if (Files.exists(headFilePath)) {
            try {
                return Files.readString(headFilePath).trim();
            } catch (IOException e) {
                System.err.println("Error reading HEAD file: " + e.getMessage());
            }
        }
        return null;
    }

}
