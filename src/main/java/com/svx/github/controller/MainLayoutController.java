package com.svx.github.controller;

import com.svx.github.controller.dialog.AddRepositoryDialogController;
import com.svx.github.controller.dialog.CommitMessageDialogController;
import com.svx.github.controller.dialog.CreateRepositoryDialogController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import com.svx.github.repository.BlobRepository;
import com.svx.github.repository.CommitRepository;
import com.svx.github.repository.TreeRepository;
import com.svx.github.utility.DiffUtility;
import com.svx.github.view.MainLayoutView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
                detectAndStageChanges();
                updateChangedFilesList();
                view.getRepositoryDropdown().getSelectionModel().select(newRepo);
            }
        });

        appController.getFocusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                detectAndStageChanges();
                updateChangedFilesList();
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

        stagedFiles.forEach((filename, blobId) -> {
            Button fileButton = new Button(filename);
            fileButton.setOnAction(e -> showFileDifference(filename, blobId));
            view.getChangedFilesList().getChildren().add(fileButton);
        });
    }

    private void showFileDifference(String filename, String blobId) {
        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) return;

        Blob currentBlob = BlobRepository.load(blobId, currentRepo);
        if (currentBlob == null) return;

        String newContent = currentBlob.getContent();
        String oldContent = "";

        VersionControl versionControl = RepositoryManager.getVersionControl();
        Commit latestCommit = versionControl.getCurrentCommit();
        if (latestCommit != null) {
            Tree latestTree = TreeRepository.load(latestCommit.getTreeId(), currentRepo);
            if (latestTree != null) {
                String oldBlobId = latestTree.getEntries().get(filename);
                if (oldBlobId != null) {
                    Blob oldBlob = BlobRepository.load(oldBlobId, currentRepo);
                    if (oldBlob != null) oldContent = oldBlob.getContent();
                }
            }
        }

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

        refreshChangesTab();
        updateHistoryTab();
    }

    private void updateHistoryTab() {
        view.getHistoryList().getChildren().clear();
        Repository currentRepo = RepositoryManager.getCurrentRepository();
        if (currentRepo == null) return;

        String commitId = currentRepo.getLatestCommitId();
        while (commitId != null) {
            Commit commit = CommitRepository.load(commitId, currentRepo);
            if (commit == null) break;

            Button commitButton = new Button(commit.getMessage() + " - " + commit.getTimestamp());
            view.getHistoryList().getChildren().add(commitButton);
            commitId = commit.getParentId();
        }
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
