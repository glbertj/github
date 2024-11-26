package com.svx.github.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.nio.file.Path;
import java.util.UUID;

public class Repository {
    private final String name;
    private String latestCommitId;
    private final UUID ownerId;
    private Path path;

    private static final ObservableList<Repository> repositories = FXCollections.observableArrayList();

    public Repository(String name, String latestCommitId, UUID ownerId, Path path) {
        this.name = name;
        this.latestCommitId = latestCommitId;
        this.ownerId = ownerId;
        this.path = path;
    }

    public Repository(String name, String latestCommitId, UUID ownerId) {
        this(name, latestCommitId, ownerId, null);
    }

    public String getName() {
        return name;
    }

    public String getLatestCommitId() {
        return latestCommitId;
    }

    public void setLatestCommitId(String latestCommitId) {
        this.latestCommitId = latestCommitId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Path getPath() {
        return path;
    }

    public Path getGitPath() {
        return path.resolve(".git");
    }

    public Path getObjectsPath() {
        return getGitPath().resolve("objects");
    }

    public Path getIndexPath() {
        return getGitPath().resolve("index");
    }

    public static ObservableList<Repository> getRepositories() {
        return repositories;
    }

    public static void addRepository(Repository repository) {
        repositories.add(repository);
    }

    public static void removeRepository(Repository repository) {
        repositories.remove(repository);
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
