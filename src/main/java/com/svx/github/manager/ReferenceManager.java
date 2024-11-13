package com.svx.github.manager;

import com.svx.github.model.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ReferenceManager {
    private final Repository repository;

    public ReferenceManager(Repository repository) {
        this.repository = repository;
    }

    public String loadHeadCommitId() throws IOException {
        Path headPath = repository.getGitPath().resolve("HEAD");
        if (!Files.exists(headPath)) {
            System.out.println("No HEAD reference found.");
            return null;
        }
        return Files.readString(headPath).trim();
    }

    public void saveHeadCommitId(String commitId) throws IOException {
        Path headPath = repository.getGitPath().resolve("HEAD");
        Files.writeString(headPath, commitId, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // Load a commit ID from a branch reference (e.g., refs/heads/main)
    public String loadBranchCommitId(String branchName) throws IOException {
        Path branchPath = repository.getGitPath().resolve("refs").resolve("heads").resolve(branchName);
        if (!Files.exists(branchPath)) {
            System.out.println("Branch " + branchName + " does not exist.");
            return null;
        }
        return Files.readString(branchPath).trim();
    }

    // Save a commit ID to a branch reference (e.g., refs/heads/main)
    public void saveBranchCommitId(String branchName, String commitId) throws IOException {
        Path branchDir = repository.getGitPath().resolve("refs").resolve("heads");
        Files.createDirectories(branchDir);  // Ensure the refs/heads directory exists
        Path branchPath = branchDir.resolve(branchName);
        Files.writeString(branchPath, commitId, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // Helper method: Set the HEAD to point to a branch (symbolic ref)
    public void setHeadToBranch(String branchName) throws IOException {
        Path headPath = repository.getGitPath().resolve("HEAD");
        String refContent = "ref: refs/heads/" + branchName;
        Files.writeString(headPath, refContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // Check if HEAD points to a branch or a direct commit ID
    public boolean isHeadDetached() throws IOException {
        Path headPath = repository.getGitPath().resolve("HEAD");
        String headContent = Files.readString(headPath).trim();
        return !headContent.startsWith("ref: ");
    }

    // Load the branch name if HEAD is pointing to a branch; otherwise, return null
    public String getHeadBranchName() throws IOException {
        Path headPath = repository.getGitPath().resolve("HEAD");
        String headContent = Files.readString(headPath).trim();
        if (headContent.startsWith("ref: refs/heads/")) {
            return headContent.substring("ref: refs/heads/".length());
        }
        return null;
    }
}

