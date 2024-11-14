package com.svx.github.controller;

import com.svx.github.controller.dialog.AddRepositoryDialogController;
import com.svx.github.controller.dialog.CommitMessageDialogController;
import com.svx.github.controller.dialog.CreateRepositoryDialogController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import com.svx.github.utility.DiffUtility;
import com.svx.github.utility.TimeUtility;
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

        view.getExitMenu().setOnAction(e -> appController.exitApp());
    }

    private void setSidebarActions() {
        view.getChangesButton().setOnAction(e -> view.showChangesTab());
        view.getHistoryButton().setOnAction(e -> view.showHistoryTab());

        view.getCommitButton().setOnAction(e -> handleCommitAction());

        view.getRepositoryDropdown().valueProperty().addListener((observable, oldRepository, newRepository) -> {
            if (newRepository != null) {
                RepositoryManager.setCurrentRepository(newRepository);
            }
        });
    }

    private void setListeners() {
        RepositoryManager.currentRepositoryProperty().addListener((observable, oldRepo, newRepo) -> {
            if (newRepo != null) {
                detectAndStageChanges();
                updateChangedFilesList();
            }
        });
    }

    private void detectAndStageChanges() {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl != null) {
            versionControl.getIndex().detectAndStageChanges();
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

        stagedFiles.forEach((filename, blobId) -> {
            Button fileButton = new Button(filename);
            fileButton.setOnAction(e -> showFileDifference(filename, blobId));
            view.getChangedFilesList().getChildren().add(fileButton);
        });
    }

    private void showFileDifference(String filename, String blobId) {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        String newContent = "";
        String oldContent = "";

        try {
            Blob currentBlob = Blob.loadFromDisk(blobId, RepositoryManager.getCurrentRepository());
            newContent = currentBlob.getContent();
        } catch (IOException e) {
            System.out.println("Error loading current content: " + e.getMessage());
        }

        Commit latestCommit = versionControl.getCurrentCommit();
        if (latestCommit != null) {
            Tree tree = latestCommit.getTree();
            if (tree != null) {
                oldContent = tree.getBlobContent(filename);
            }
        }

        String difference = DiffUtility.getDifference(oldContent, newContent);
        view.getTextArea().setText(difference);
    }

    private void handleCommitAction() {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        CommitMessageDialogController dialogController = new CommitMessageDialogController();
        appController.openDialog(dialogController);

        String commitMessage = dialogController.showAndGetCommitMessage();
        if (commitMessage == null || commitMessage.isEmpty()) {
            System.out.println("Commit canceled or message was empty.");
            return;
        }

        versionControl.commitChanges(commitMessage);
        updateChangedFilesList();
        updateHistoryTab();
    }

    public void updateHistoryTab() {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        view.getHistoryList().getChildren().clear();

        Commit commit = versionControl.getCurrentCommit();
        while (commit != null) {
            String commitDisplayText = String.format("Commit: %s\nMessage: %s\nDate: %s",
                    commit.getId(), commit.getMessage(), commit.getTimestamp());

            Button commitButton = new Button(commitDisplayText);
            Commit finalCommit = commit;
            commitButton.setOnAction(e -> showCommitDetails(finalCommit));
            view.getHistoryList().getChildren().add(commitButton);

            String parentCommitId = commit.getParentId();
            if (parentCommitId != null) {
                try {
                    commit = Commit.loadFromDisk(parentCommitId, RepositoryManager.getCurrentRepository());
                } catch (IOException e) {
                    System.out.println("Error loading commit: " + e.getMessage());
                    break;
                }
            } else {
                commit = null;
            }
        }
    }

    private void showCommitDetails(Commit commit) {
        if (commit == null) return;

        StringBuilder commitDetails = new StringBuilder(String.format("Commit ID: %s\nMessage: %s\nDate: %s\n\nFiles:\n",
                commit.getId(), commit.getMessage(), TimeUtility.formatTimestamp(commit.getTimestamp())));

        Tree tree = commit.getTree();
        if (tree != null) {
            for (String filename : tree.getFilenames()) {
                commitDetails.append("- ").append(filename).append("\n");
            }
        }

        view.getTextArea().setText(commitDetails.toString());
    }
}
