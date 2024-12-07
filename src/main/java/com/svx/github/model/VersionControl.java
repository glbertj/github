package com.svx.github.model;

import com.svx.github.manager.RepositoryManager;
import com.svx.github.repository.BlobRepository;
import com.svx.github.repository.CommitRepository;
import com.svx.github.repository.RepositoryRepository;
import com.svx.github.repository.TreeRepository;
import com.svx.github.utility.FileUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

public class VersionControl {
    private final Repository repository;
    private final Index index;
    private Commit currentCommit;

    public VersionControl(Repository repository) throws IOException, SQLException {
        this.repository = repository;
        this.index = new Index();
        this.index.loadFromIndexFile(repository);
        loadCurrentCommit();
    }

    public Index getIndex() {
        return index;
    }

    public Commit getCurrentCommit() {
        return currentCommit;
    }

    public void setCurrentCommit(Commit commit) {
        this.currentCommit = commit;
    }

    private void loadCurrentCommit() throws SQLException {
        if (repository == null) {
            return;
        }

        String headCommitId = repository.getLatestCommitId();
        if (headCommitId != null) {
            this.currentCommit = CommitRepository.load(headCommitId, repository);
        }
    }

    public boolean commitChanges(String message) throws IOException {
        if (index.getStagedFiles().isEmpty()) {
            return false;
        }

        Tree newTree = new Tree(index.getStagedFiles());
        newTree.saveToDisk(repository.getObjectsPath());

        Commit newCommit = new Commit(newTree.getId(), message, currentCommit != null ? currentCommit.getId() : null);
        newCommit.saveToDisk(repository.getObjectsPath());

        updateHeadFile(newCommit.getId());
        currentCommit = newCommit;
        repository.setLatestCommitId(newCommit.getId());

        index.clear();
        return true;
    }

    private void updateHeadFile(String commitId) throws IOException {
        Path headFilePath = repository.getGitPath().resolve("refs").resolve("heads").resolve("master");
        Files.createDirectories(headFilePath.getParent());
        Files.writeString(headFilePath, commitId);
    }

    public void push() throws Exception {
        if (currentCommit == null) {
            return;
        }

        List<Commit> commitsToPush = getCommitsToPush();
        if (commitsToPush.isEmpty()) {
            return;
        }

        for (Commit commit : commitsToPush) {
            Tree tree = Tree.loadFromDisk(commit.getTreeId(), repository.getObjectsPath());
            TreeRepository.save(tree);

            for (String blobId : tree.getEntries().values()) {
                if (BlobRepository.load(blobId, repository) == null) {
                    String blobContent = FileUtility.loadFromDisk(blobId, repository.getObjectsPath());
                    Blob blob = new Blob(blobContent, repository);
                    BlobRepository.save(blob);
                }
            }
            CommitRepository.save(commit);
        }

        RepositoryRepository.save(repository);
    }

    private List<Commit> getCommitsToPush() throws SQLException {
        List<Commit> commitsToPush = new ArrayList<>();
        Commit commitToPush = currentCommit;

        while (commitToPush != null) {
            if (CommitRepository.load(commitToPush.getId(), repository) != null) {
                break;
            }
            commitsToPush.add(commitToPush);

            commitToPush = commitToPush.getParentId() != null
                    ? Commit.loadFromDisk(commitToPush.getParentId(), repository.getObjectsPath())
                    : null;
        }

        Collections.reverse(commitsToPush);
        return commitsToPush;
    }

    public void pull() throws Exception {
        String latestDatabaseCommitId = RepositoryRepository.getLatestCommitId(repository);

        if (latestDatabaseCommitId == null) {
            return;
        }

        Path objectsPath = repository.getObjectsPath();
        Set<String> pulledFiles = new HashSet<>();

        Commit commitToPull = CommitRepository.load(latestDatabaseCommitId, repository);
        while (commitToPull != null) {
            commitToPull.saveToDisk(objectsPath);

            Tree treeToPull = TreeRepository.load(commitToPull.getTreeId());
            if (treeToPull != null) {
                treeToPull.saveToDisk(objectsPath);

                for (Map.Entry<String, String> entry : treeToPull.getEntries().entrySet()) {
                    String blobId = entry.getValue();
                    String filePath = entry.getKey();

                    if (pulledFiles.contains(filePath)) {
                        continue;
                    }

                    if (!Files.exists(objectsPath.resolve(blobId.substring(0, 2)).resolve(blobId.substring(2)))) {
                        Blob blobToPull = BlobRepository.load(blobId, repository);
                        if (blobToPull != null) {
                            FileUtility.saveToDisk(blobToPull.getId(), blobToPull.getContent(), objectsPath);
                        }
                    }

                    Path workingFilePath = repository.getPath().resolve(filePath);
                    Files.createDirectories(workingFilePath.getParent());
                    Files.writeString(workingFilePath, Objects.requireNonNull(BlobRepository.load(blobId, repository)).getContent());

                    pulledFiles.add(filePath);
                }
            }

            String parentId = commitToPull.getParentId();
            commitToPull = (parentId != null) ? CommitRepository.load(parentId, repository) : null;
        }

        Path headFilePath = repository.getGitPath().resolve("refs").resolve("heads").resolve("master");
        Files.createDirectories(headFilePath.getParent());
        Files.writeString(headFilePath, latestDatabaseCommitId);

        repository.setLatestCommitId(latestDatabaseCommitId);
        RepositoryManager.setCurrentRepository(repository);

        Commit latestCommit = CommitRepository.load(latestDatabaseCommitId, repository);
        if (latestCommit != null) {
            VersionControl versionControl = RepositoryManager.getVersionControl();
            if (versionControl != null) {
                versionControl.setCurrentCommit(latestCommit);
            }
        }
    }
}