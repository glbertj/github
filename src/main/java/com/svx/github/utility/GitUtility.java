package com.svx.github.utility;

import java.io.File;

public class GitUtility {
    public static boolean hasRepository(String path) {
        File gitDir = new File(path, ".git");

        // Check if .git directory exists and contains required subdirectories
        return gitDir.exists() && gitDir.isDirectory() &&
                new File(gitDir, "objects").isDirectory() &&
                new File(gitDir, "refs").isDirectory() &&
                new File(gitDir, "config").isFile();
    }
}
