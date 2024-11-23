package com.svx.github.controller;

import com.svx.github.controller.dialog.AddRepositoryDialogController;
import com.svx.github.controller.dialog.CloneRepositoryDialogController;
import com.svx.github.controller.dialog.CreateRepositoryDialogController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import com.svx.github.repository.CommitRepository;
import com.svx.github.repository.RepositoryRepository;
import com.svx.github.utility.DesktopUtility;
import com.svx.github.utility.DiffUtility;
import com.svx.github.utility.FileUtility;
import com.svx.github.view.MainLayoutView;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainLayoutController extends Controller<MainLayoutView> {

    public MainLayoutController(AppController appController) {
        super(new MainLayoutView(), appController);
        setActions();
    }

    @Override
    protected void setActions() {
        setMenuActions();
        setTopBarActions();
        setSidebarActions();
        setListeners();
        updateChangedFilesList();
        updateHistoryTab();
    }

    private void setMenuActions() {
        view.getCreateRepositoryMenu().setOnAction(e -> appController.openDialog(new CreateRepositoryDialogController(appController)));
        view.getAddRepositoryMenu().setOnAction(e -> appController.openDialog(new AddRepositoryDialogController(appController)));
        view.getCloneRepositoryMenu().setOnAction(e -> appController.openDialog(new CloneRepositoryDialogController(appController)));
        view.getLogoutMenu().setOnAction(e -> {
            if (RepositoryManager.getCurrentRepository() != null) {
                RepositoryManager.setCurrentRepository(null);

            }
            appController.logout();
        });
        view.getExitMenu().setOnAction(e -> appController.exitApp());

        view.getToggleFullScreenMenuItem().setOnAction(e -> appController.toggleFullScreen());
        view.getRemoveRepositoryMenuItem().setOnAction(e -> {
            if (RepositoryManager.getCurrentRepository() != null) {
                RepositoryManager.removeRepository();
                appController.showNotification("Repository removed successfully.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            } else {
                appController.showNotification("No repository selected.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            }
        });

        view.getShowInExplorerMenuItem().setOnAction(e -> {
            Repository currentRepo = RepositoryManager.getCurrentRepository();
            if (currentRepo != null) {
                try {
                    if (!DesktopUtility.openSystemExplorer(String.valueOf(currentRepo.getPath()))) {
                        appController.showNotification("Path does not exist..", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
                    }
                } catch (IOException ex) {
                    appController.showNotification("Failed to open system explorer.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
                }
            } else {
                appController.showNotification("No repository selected.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            }
        });
    }

    private void setTopBarActions() {
        view.getRepositoryToggleButton().setOnMouseClicked(e -> view.switchSideBar());
        view.getOriginButton().setOnMouseClicked(e -> updateMultiFunctionButton());
    }

    private void setSidebarActions() {
        view.getCommitButton().setOnAction(e -> handleCommitAction());
    }

    private void setListeners() {
        RepositoryManager.currentRepositoryProperty().addListener((observable, oldRepo, newRepo) -> {
            if (newRepo != null) {
                view.getTextArea().clear();
                detectAndStageChanges();
                updateChangedFilesList();
                updateHistoryTab();
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

        Repository.getRepositories().addListener((ListChangeListener<? super Repository>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Repository repo : change.getAddedSubList()) {
                        boolean alreadyExists = view.getRepositoryList().getChildren().stream().anyMatch(node -> {
                            if (node instanceof HBox buttonContent) {
                                Label label = (Label) buttonContent.getChildren().get(1);
                                return label.getText().equals(repo.getName())
                                        && buttonContent.getUserData() instanceof Repository
                                        && ((Repository) buttonContent.getUserData()).getOwnerId().equals(repo.getOwnerId());
                            }
                            return false;
                        });
                        if (!alreadyExists) {
                            view.getRepositoryList().getChildren().add(view.createRepositoryButton(repo));
                        }
                    }
                }
                if (change.wasRemoved()) {
                    for (Repository repo : change.getRemoved()) {
                        view.getRepositoryList().getChildren().removeIf(node -> {
                            if (node instanceof HBox buttonContent) {
                                Label label = (Label) buttonContent.getChildren().get(1);
                                return label.getText().equals(repo.getName());
                            }
                            return false;
                        });
                    }
                }
            }
        });
    }

    private void detectAndStageChanges() {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        Repository currentRepo = RepositoryManager.getCurrentRepository();

        if (versionControl != null && currentRepo != null) {
            versionControl.getIndex().detectAndStageChanges(currentRepo);
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

        Commit currentCommit = versionControl.getCurrentCommit();
        Map<String, String> cumulativeTree = loadCumulativeTree(currentCommit);

        stagedFiles.forEach((filename, stagedBlobId) -> {
            String committedBlobId = cumulativeTree.get(filename);

            boolean isChanged = !stagedBlobId.equals(committedBlobId);

            if (isChanged) {
                Button fileButton = new Button(filename);
                fileButton.setOnAction(e -> showFileDifference(filename, stagedBlobId));
                view.getChangedFilesList().getChildren().add(fileButton);
            }
        });

        if (view.getChangedFilesList().getChildren().isEmpty()) {
            Label noChangesLabel = new Label("No changed files.");
            view.getChangedFilesList().getChildren().add(noChangesLabel);
        }
    }

    private Map<String, String> loadCumulativeTree(Commit commit) {
        if (commit == null) return new HashMap<>();

        Map<String, String> cumulativeTree = new HashMap<>();

        Commit currentCommit = commit;
        while (currentCommit != null) {
            Tree currentTree = Tree.loadFromDisk(currentCommit.getTreeId(), RepositoryManager.getCurrentRepository().getObjectsPath());
            cumulativeTree.putAll(currentTree.getEntries());

            String parentId = currentCommit.getParentId();
            currentCommit = (parentId != null) ? Commit.loadFromDisk(parentId, RepositoryManager.getCurrentRepository().getObjectsPath()) : null;
        }

        return cumulativeTree;
    }

    private void resetMultiFunctionButton() {
        view.switchOriginButton(MainLayoutView.OriginType.FETCH);
        view.getOriginButton().setOnMouseClicked(e -> updateMultiFunctionButton());
    }

    private void updateMultiFunctionButton() {
        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) {
            System.out.println("hai");
            appController.showNotification("No repository selected.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            resetMultiFunctionButton();
            return;
        }

        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) {
            appController.showNotification("No version control system found.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            resetMultiFunctionButton();
            return;
        }

        Commit latestLocalCommit = versionControl.getCurrentCommit();
        if (latestLocalCommit == null) {
            appController.showNotification("You are up to date with the upstream branch.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            resetMultiFunctionButton();
            return;
        }

        String latestDatabaseCommitId = RepositoryRepository.getLatestCommitId(currentRepo);
        Commit latestDatabaseCommit = latestDatabaseCommitId != null
                ? CommitRepository.load(latestDatabaseCommitId, currentRepo)
                : null;

        boolean hasPendingCommits = latestDatabaseCommit == null
                || latestLocalCommit.getTimestamp().withNano(0).isAfter(latestDatabaseCommit.getTimestamp().withNano(0));

        if (hasPendingCommits) {
            appController.showNotification("Successfully fetched from origin. You have pending commits to push.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            view.switchOriginButton(MainLayoutView.OriginType.PUSH);
            view.getOriginButton().setOnMouseClicked(e -> {
                RepositoryManager.getVersionControl().push();
                updateMultiFunctionButton();
                appController.showNotification("Changes pushed successfully.", NotificationBox.NotificationType.SUCCESS, "fas-arrow-up");
            });
        } else if (!latestDatabaseCommitId.equals(latestLocalCommit.getId())) {
            appController.showNotification("Successfully fetched from origin. You are behind the upstream branch.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            view.switchOriginButton(MainLayoutView.OriginType.PULL);
            view.getOriginButton().setOnMouseClicked(e -> {
                System.out.println("Pulling changes...");
                try {
                    RepositoryManager.getVersionControl().pull();
                } catch (IOException ex) {
                    appController.showNotification("Failed to pull changes from the database.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
                }
                updateMultiFunctionButton();
                appController.showNotification("Changes pushed successfully.", NotificationBox.NotificationType.SUCCESS, "fas-arrow-down");
            });
        } else {
            appController.showNotification("You are up to date with the upstream branch.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            resetMultiFunctionButton();
        }
    }

    private void handleCommitAction() {
        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) {
            appController.showNotification("No repository selected.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
            return;
        }

        String commitMessage = view.getCommitSummaryTextField() + "\n\n" + view.getCommitDescriptionTextArea();

        VersionControl versionControl = RepositoryManager.getVersionControl();
        versionControl.commitChanges(commitMessage);

        updateChangedFilesList();
        updateHistoryTab();
        updateMultiFunctionButton();
    }

    private void updateHistoryTab() {
        view.getHistoryList().getChildren().clear();
        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) {
            Label noRepoLabel = new Label("No repository selected.");
            view.getHistoryList().getChildren().add(noRepoLabel);
            return;
        }

        String commitId = currentRepo.getLatestCommitId();
        if (commitId == null) {
            Label noCommitsLabel = new Label("No commit history.");
            view.getHistoryList().getChildren().add(noCommitsLabel);
            return;
        }

        while (commitId != null && !commitId.isBlank()) {
            Commit commit = Commit.loadFromDisk(commitId, currentRepo.getObjectsPath());

            Button commitButton = new Button(commit.getMessage() + " - " + commit.getTimestamp());
            commitButton.setOnAction(e -> showCommitDetails(commit));
            view.getHistoryList().getChildren().add(commitButton);

            commitId = commit.getParentId();
        }
    }

    private void showFileDifference(String filename, String blobId) {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) return;

        String newContent = FileUtility.loadFromDisk(blobId, currentRepo.getObjectsPath());
        String oldContent = "";

        Commit latestCommit = versionControl.getCurrentCommit();
        if (latestCommit != null) {
            Tree latestTree = Tree.loadFromDisk(latestCommit.getTreeId(), currentRepo.getObjectsPath());
            String oldBlobId = latestTree.getEntries().get(filename);
            if (oldBlobId != null) {
                oldContent = FileUtility.loadFromDisk(oldBlobId, currentRepo.getObjectsPath());
            }
        }

        String difference = DiffUtility.getDifference(oldContent, newContent);
        view.getTextArea().setText(difference);
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
}