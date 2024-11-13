package com.svx.github.controller;

import com.svx.github.controller.dialog.AddRepositoryDialogController;
import com.svx.github.controller.dialog.CreateRepositoryDialogController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import com.svx.github.utility.DiffUtility;
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

        view.getCommitButton().setOnAction(e -> {
            VersionControl versionControl = RepositoryManager.getVersionControl();
            if (versionControl == null) return;

            System.out.println("Committing changes...");
            versionControl.commitChanges("Commit from UI");

            updateChangedFilesList();
            view.getTextArea().clear();
        });

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
}
