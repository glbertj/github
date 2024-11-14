package com.svx.github.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.nio.file.Path;

public record Repository(String name, Path path) {
    private static final ObservableList<Repository> repositories = FXCollections.observableArrayList();

    public static ObservableList<Repository> getRepositories() {
        return repositories;
    }

    public static void addRepository(Repository repository) {
        repositories.add(repository);
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
}