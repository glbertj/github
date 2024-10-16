package com.svx.github.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public record Repository(String name, String path) {
    private static final ObservableList<Repository> repositories = FXCollections.observableArrayList();

    public static ObservableList<Repository> getRepositories() {
        return repositories;
    }

    public static void addRepository(Repository repository) {
        repositories.add(repository);
    }

    public static Repository getRepository(String name) {
        return repositories.stream().filter(repository -> repository.name().equals(name)).findFirst().orElse(null);
    }

    public static void removeRepository(Repository repository) {
        repositories.remove(repository);
    }
}
