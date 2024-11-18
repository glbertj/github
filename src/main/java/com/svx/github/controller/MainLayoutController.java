package com.svx.github.controller;

import com.svx.github.controller.dialog.AddRepositoryDialogController;
import com.svx.github.controller.dialog.CommitMessageDialogController;
import com.svx.github.controller.dialog.CreateRepositoryDialogController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import com.svx.github.utility.DiffUtility;
import com.svx.github.utility.FileUtility;
import com.svx.github.view.MainLayoutView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.io.IOException;
import java.util.Map;

public class MainLayoutController extends Controller<MainLayoutView> {

    public MainLayoutController(AppController appController) {
        super(new MainLayoutView(), appController);
        setActions();
    }

    @Override
    protected void setActions() {
        setMenuActions();
        setSidebarActions();
        setListeners();
    }

    private void setMenuActions() {
        view.getCreateRepositoryMenu().setOnAction(e -> appController.openDialog(new CreateRepositoryDialogController()));

        view.getAddRepositoryMenu().setOnAction(e -> appController.openDialog(new AddRepositoryDialogController()));

        view.getPushMenu().setOnAction(e -> {
            Repository currentRepo = RepositoryManager.getCurrentRepository();
            if (currentRepo == null) {
                System.out.println("No repository selected.");
                return;
            }

            VersionControl versionControl = RepositoryManager.getVersionControl();
            versionControl.push();
        });

        view.getExitMenu().setOnAction(e -> appController.exitApp());
    }

    private void setSidebarActions() {
        view.getChangesButton().setOnAction(e -> view.showChangesTab());
        view.getHistoryButton().setOnAction(e -> view.showHistoryTab());

        view.getCommitButton().setOnAction(e -> handleCommitAction());

        view.getRepositoryDropdown().valueProperty().addListener((observable, oldRepo, newRepo) -> {
            if (newRepo != null) {
                RepositoryManager.setCurrentRepository(newRepo);
                refreshChangesTab();
                updateHistoryTab();
            }
        });
    }

    private void setListeners() {
        RepositoryManager.currentRepositoryProperty().addListener((observable, oldRepo, newRepo) -> {
            if (newRepo != null) {
                view.getTextArea().clear();
                detectAndStageChanges();
                updateChangedFilesList();
                updateHistoryTab();
                view.getRepositoryDropdown().getSelectionModel().select(newRepo);
            }
        });

        appController.getFocusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                view.getTextArea().clear();
                detectAndStageChanges();
                updateChangedFilesList();
                updateHistoryTab();
            }
        });
    }

    private void detectAndStageChanges() {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        Repository currentRepo = RepositoryManager.getCurrentRepository();

        if (versionControl != null && currentRepo != null) {
            versionControl.getIndex().detectAndStageChanges(currentRepo);
            System.out.println("Changes detected and staged for repository: " + currentRepo.getName());
        }
    }

    public void updateChangedFilesList() {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        view.getChangedFilesList().getChildren().clear();

        Map<String, String> stagedFiles = versionControl.getIndex().getStagedFiles();
        if (stagedFiles == null || stagedFiles.isEmpty()) {
            Label noChangesLabel = new Label("No changed files.");
            view.getChangedFilesList().getChildren().add(noChangesLabel);
            return;
        }

        // Load the latest commit's tree
        Commit latestCommit = versionControl.getCurrentCommit();
        Tree latestTree;
        if (latestCommit != null) {
            latestTree = Tree.loadFromDisk(latestCommit.getTreeId(), RepositoryManager.getCurrentRepository().getObjectsPath());
        } else {
            latestTree = null;
        }

        stagedFiles.forEach((filename, stagedBlobId) -> {
            boolean isChanged = true;

            // Compare with the latest tree's blob ID (if available)
            if (latestTree != null) {
                String committedBlobId = latestTree.getEntries().get(filename);
                if (stagedBlobId.equals(committedBlobId)) {
                    isChanged = false; // File is unchanged
                }
            }

            // Only add files that are actually changed
            if (isChanged) {
                Button fileButton = new Button(filename);
                fileButton.setOnAction(e -> showFileDifference(filename, stagedBlobId));
                view.getChangedFilesList().getChildren().add(fileButton);
            }
        });

        // Add a label if no files are actually changed
        if (view.getChangedFilesList().getChildren().isEmpty()) {
            Label noChangesLabel = new Label("No changed files.");
            view.getChangedFilesList().getChildren().add(noChangesLabel);
        }
    }

    private void showFileDifference(String filename, String blobId) {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) return;

        String newContent = FileUtility.loadFromDisk(blobId, currentRepo.getObjectsPath());
        System.out.println("New content: " + newContent);

        String oldContent = "";
        Commit latestCommit = versionControl.getCurrentCommit();
        if (latestCommit != null) {
            Tree latestTree = Tree.loadFromDisk(latestCommit.getTreeId(), currentRepo.getObjectsPath());
            String oldBlobId = latestTree.getEntries().get(filename);
            if (oldBlobId != null) {
                oldContent = FileUtility.loadFromDisk(oldBlobId, currentRepo.getObjectsPath());
            }
        }
        System.out.println("Old content: " + oldContent);

        String difference = DiffUtility.getDifference(oldContent, newContent);
        view.getTextArea().setText(difference);
    }

    private void handleCommitAction() {
        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) {
            System.out.println("No repository selected.");
            return;
        }

        CommitMessageDialogController dialogController = new CommitMessageDialogController();
        appController.openDialog(dialogController);

        String commitMessage = dialogController.getView().getMessageTextArea().getText();
        if (commitMessage == null || commitMessage.isEmpty()) {
            System.out.println("Commit canceled or message was empty.");
            return;
        }

        VersionControl versionControl = RepositoryManager.getVersionControl();
        versionControl.commitChanges(commitMessage);

        updateChangedFilesList();
        updateHistoryTab();
    }

    private void updateHistoryTab() {
        view.getHistoryList().getChildren().clear();
        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) {
            Label noRepoLabel = new Label("No repository selected.");
            view.getHistoryList().getChildren().add(noRepoLabel);
            return;
        }

        System.out.println("commit id" + currentRepo.getLatestCommitId());
        String commitId = currentRepo.getLatestCommitId();
        if (commitId == null) {
            Label noCommitsLabel = new Label("No commit history.");
            view.getHistoryList().getChildren().add(noCommitsLabel);
            return;
        }

        while (commitId != null && !commitId.isBlank()) {
            try {
                Commit commit = Commit.loadFromDisk(commitId, currentRepo.getObjectsPath());

                Button commitButton = new Button(commit.getMessage() + " - " + commit.getTimestamp());
                commitButton.setOnAction(e -> showCommitDetails(commit));
                view.getHistoryList().getChildren().add(commitButton);

                commitId = commit.getParentId();
            } catch (IOException e) {
                System.out.println("Error loading commit from disk: " + e.getMessage());
                break;
            }
        }
    }

    private void showCommitDetails(Commit commit) {
        StringBuilder details = new StringBuilder();
        details.append("Commit Details:\n");
        details.append("ID: ").append(commit.getId()).append("\n");
        details.append("Message: ").append(commit.getMessage()).append("\n");
        details.append("Timestamp: ").append(commit.getTimestamp()).append("\n");
        details.append("Tree ID: ").append(commit.getTreeId()).append("\n");

        Tree tree = Tree.loadFromDisk(commit.getTreeId(), RepositoryManager.getCurrentRepository().getObjectsPath());
        details.append("\nTree Entries:\n");
        for (Map.Entry<String, String> entry : tree.getEntries().entrySet()) {
            details.append("  ").append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }

        view.getTextArea().setText(details.toString());
    }

    private void refreshChangesTab() {
        view.getChangedFilesList().getChildren().clear();
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        versionControl.getIndex().getStagedFiles().forEach((filename, blobId) -> {
            Button fileButton = new Button(filename);
            fileButton.setOnAction(e -> showFileDifference(filename, blobId));
            view.getChangedFilesList().getChildren().add(fileButton);
        });
    }
}
