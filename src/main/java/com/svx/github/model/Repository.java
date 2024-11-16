package com.svx.github.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.nio.file.Path;
import java.util.UUID;

public class Repository {
    private final UUID id;
    private final String name;
    private final String latestCommitId;
    private final UUID ownerId;
    private final Path path;

    private static final ObservableList<Repository> repositories = FXCollections.observableArrayList();

    public Repository(UUID id, String name, String latestCommitId, UUID ownerId, Path path) {
        this.id = id;
        this.name = name;
        this.latestCommitId = latestCommitId;
        this.ownerId = ownerId;
        this.path = path;
    }

    public Repository(UUID id, String name, String latestCommitId, UUID ownerId) {
        this(id, name, latestCommitId, ownerId, null);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLatestCommitId() {
        return latestCommitId;
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
}
