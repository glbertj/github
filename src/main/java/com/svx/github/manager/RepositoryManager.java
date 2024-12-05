package com.svx.github.manager;

import com.svx.github.model.Commit;
import com.svx.github.model.Repository;
import com.svx.github.model.UserSingleton;
import com.svx.github.model.VersionControl;
import com.svx.github.utility.GitUtility;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RepositoryManager {
    private static final ObjectProperty<Repository> currentRepository = new SimpleObjectProperty<>();
    private static final Map<Repository, VersionControl> versionControlMap = new HashMap<>();
    private static final Path RECENT_REPOSITORY_FILE = Paths.get("C:/gittub/sessions/recent_repository.dat");

    public static void setCurrentRepository(Repository repository) throws IOException, SQLException {
        currentRepository.set(repository);

        if (!versionControlMap.containsKey(repository)) {
            VersionControl versionControl = new VersionControl(repository);

            if (repository == null) return;
            Path headFilePath = repository.getGitPath().resolve("refs").resolve("heads").resolve("master");
            if (Files.exists(headFilePath)) {
                try {
                    String headCommitId = Files.readString(headFilePath).trim();
                    repository.setLatestCommitId(headCommitId);
                    Commit headCommit = Commit.loadFromDisk(headCommitId, repository.getObjectsPath());
                    versionControl.setCurrentCommit(headCommit);
                } catch (IOException e) {
                    throw new IOException();
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

    public static void loadRecentRepository() {
        if (!Files.exists(RECENT_REPOSITORY_FILE)) {
            return;
        }

        BufferedReader reader;
        try {
            reader = Files.newBufferedReader(RECENT_REPOSITORY_FILE);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", 3);
                if (parts.length != 3) continue;

                String name = parts[0];
                String ownerId = parts[1];
                Path path = Paths.get(parts[2]);
                String latestCommitId = GitUtility.getLatestCommitIdFromHead(path);

                if (GitUtility.hasRepository(path)) {
                    Repository repo = new Repository(name, latestCommitId, UUID.fromString(ownerId), path);
                    if (String.valueOf(UserSingleton.getCurrentUser().getId()).equals(ownerId)) {
                        repo.setPath(path);
                        Repository.addRepository(repo);
                    }
                } else {
                    System.out.println("Repository folder no longer exists: " + path);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateRecentRepository() {
        try (BufferedWriter writer = Files.newBufferedWriter(RECENT_REPOSITORY_FILE)) {
            for (Repository repo : Repository.getRepositories()) {
                writer.write(String.format("%s;%s;%s",
                        repo.getName(),
                        repo.getOwnerId(),
                        repo.getPath().toString()
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error updating recent repositories: " + e.getMessage());
        }
    }

    public static void deleteRecentRepository() {
        try {
            Files.deleteIfExists(RECENT_REPOSITORY_FILE);
        } catch (IOException e) {
            System.out.println("Error deleting recent repositories: " + e.getMessage());
        }
    }
}

