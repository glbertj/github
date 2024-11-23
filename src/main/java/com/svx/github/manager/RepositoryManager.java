package com.svx.github.manager;

import com.svx.github.model.Commit;
import com.svx.github.model.Repository;
import com.svx.github.model.VersionControl;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RepositoryManager {
    private static final ObjectProperty<Repository> currentRepository = new SimpleObjectProperty<>();
    private static final Map<Repository, VersionControl> versionControlMap = new HashMap<>();

    public static void setCurrentRepository(Repository repository) {
        currentRepository.set(repository);

        if (!versionControlMap.containsKey(repository)) {
            VersionControl versionControl = new VersionControl(repository);

            Path headFilePath = repository.getGitPath().resolve("refs").resolve("heads").resolve("master");
            if (Files.exists(headFilePath)) {
                try {
                    String headCommitId = Files.readString(headFilePath).trim();
                    repository.setLatestCommitId(headCommitId); // Update repository's latest commit ID
                    Commit headCommit = Commit.loadFromDisk(headCommitId, repository.getObjectsPath());
                    versionControl.setCurrentCommit(headCommit);
                } catch (IOException e) {
                    System.out.println("Error reading HEAD file: " + e.getMessage());
                }
            }

            versionControlMap.put(repository, versionControl);
        }
    }

    public static void removeRepository() {
        Repository repository = currentRepository.get();
        versionControlMap.remove(repository);
        Repository.removeRepository(repository);
        currentRepository.set(null);
    }

    public static ObjectProperty<Repository> currentRepositoryProperty() {
        return currentRepository;
    }

    public static Repository getCurrentRepository() {
        return currentRepository.get();
    }

    public static VersionControl getVersionControl() {
        Repository repository = currentRepository.get();
        return versionControlMap.get(repository);
    }
}

