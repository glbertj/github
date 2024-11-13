package com.svx.github.manager;

import com.svx.github.model.Repository;
import com.svx.github.model.VersionControl;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.util.HashMap;
import java.util.Map;

public class RepositoryManager {
    private static final ObjectProperty<Repository> currentRepository = new SimpleObjectProperty<>();
    private static final Map<Repository, VersionControl> versionControlMap = new HashMap<>();

    private RepositoryManager() {}

    public static Repository getCurrentRepository() {
        return currentRepository.get();
    }

    public static void setCurrentRepository(Repository repository) {
        currentRepository.set(repository);

        if (!versionControlMap.containsKey(repository)) {
            versionControlMap.put(repository, new VersionControl());
        }
    }

    public static VersionControl getVersionControl() {
        Repository activeRepo = getCurrentRepository();
        return activeRepo != null ? versionControlMap.get(activeRepo) : null;
    }
}
