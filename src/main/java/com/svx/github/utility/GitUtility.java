package com.svx.github.utility;

import java.nio.file.Files;
import java.nio.file.Path;

public class GitUtility {
    public static boolean hasRepository(Path path) {
        Path gitDir = path.resolve(".git");

        return Files.isDirectory(gitDir) &&
                Files.isDirectory(gitDir.resolve("objects")) &&
                Files.isDirectory(gitDir.resolve("refs")) &&
                Files.isRegularFile(gitDir.resolve("config"));
    }
}
