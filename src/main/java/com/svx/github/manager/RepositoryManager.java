package com.svx.github.manager;

import com.svx.github.model.Repository;
import javafx.beans.property.ObjectProperty;

public class RepositoryManager {
    private static ObjectProperty<Repository> currentRepository;

    private RepositoryManager() {}

    public static ObjectProperty<Repository> getCurrentRepository() {
        return currentRepository;
    }

    public static void setCurrentRepository(Repository repository) {
        currentRepository.set(repository);
    }
}
