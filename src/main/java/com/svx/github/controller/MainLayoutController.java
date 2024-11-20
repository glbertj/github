package com.svx.github.controller;

import com.svx.github.controller.dialog.AddRepositoryDialogController;
import com.svx.github.controller.dialog.CloneRepositoryDialogController;
import com.svx.github.controller.dialog.CommitMessageDialogController;
import com.svx.github.controller.dialog.CreateRepositoryDialogController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import com.svx.github.repository.CommitRepository;
import com.svx.github.repository.RepositoryRepository;
import com.svx.github.utility.DiffUtility;
import com.svx.github.utility.FileUtility;
import com.svx.github.view.MainLayoutView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

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
        updateMultiFunctionButton();
    }

    private void setMenuActions() {
        view.getCreateRepositoryMenu().setOnAction(e -> appController.openDialog(new CreateRepositoryDialogController()));

        view.getAddRepositoryMenu().setOnAction(e -> appController.openDialog(new AddRepositoryDialogController()));

        view.getCloneRepositoryMenu().setOnAction(e -> appController.openDialog(new CloneRepositoryDialogController()));

        view.getLogoutMenu().setOnAction(e -> {
            if (RepositoryManager.getCurrentRepository() != null) {
                RepositoryManager.setCurrentRepository(null);

            }
            appController.logout();
        });

        view.getExitMenu().setOnAction(e -> appController.exitApp());
    }

    private void setTopBarActions() {
        view.getRepositoryToggleButton().setOnMouseClicked(e -> {
            view.switchSideBar();
        });

//        view.getRepositoryDropdown().valueProperty().addListener((observable, oldRepo, newRepo) -> {
//            if (newRepo != null) {
//                RepositoryManager.setCurrentRepository(newRepo);
//                refreshChangesTab();
//                updateHistoryTab();
//                updateMultiFunctionButton();
//            }
//        });
    }

    private void setSidebarActions() {
        view.getCommitButton().setOnAction(e -> handleCommitAction());
    }

    private void setListeners() {
        RepositoryManager.currentRepositoryProperty().addListener((observable, oldRepo, newRepo) -> {
            if (newRepo != null) {
//                view.getTextArea().clear();
                detectAndStageChanges();
                updateChangedFilesList();
                updateHistoryTab();
                resetMultiFunctionButton();
//                view.getRepositoryDropdown().getSelectionModel().select(newRepo);
            }
        });

        appController.getFocusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
//                view.getTextArea().clear();
                detectAndStageChanges();
                updateChangedFilesList();
                updateHistoryTab();
                resetMultiFunctionButton();
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

            // Move to the parent commit
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
            resetMultiFunctionButton();
            return;
        }

        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) {
            resetMultiFunctionButton();
            return;
        }

        Commit latestLocalCommit = versionControl.getCurrentCommit();
        if (latestLocalCommit == null) {
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
            view.switchOriginButton(MainLayoutView.OriginType.PUSH);
            view.getOriginButton().setOnMouseClicked(e -> {
                System.out.println("Pushing changes...");
                RepositoryManager.getVersionControl().push();
                updateMultiFunctionButton();
            });
        } else if (!latestDatabaseCommitId.equals(latestLocalCommit.getId())) {
            view.switchOriginButton(MainLayoutView.OriginType.PULL);
            view.getOriginButton().setOnMouseClicked(e -> {
                System.out.println("Pulling changes...");
                RepositoryManager.getVersionControl().pull();
                updateMultiFunctionButton();
            });
        } else {
            resetMultiFunctionButton();
        }
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

    private void refreshChangesTab() {
        detectAndStageChanges();
        updateChangedFilesList();
    }
}