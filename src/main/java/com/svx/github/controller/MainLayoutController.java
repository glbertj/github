package com.svx.github.controller;

import com.svx.github.controller.dialog.AddRepositoryDialogController;
import com.svx.github.controller.dialog.CreateRepositoryDialogController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.Blob;
import com.svx.github.model.Commit;
import com.svx.github.model.Tree;
import com.svx.github.model.VersionControl;
import com.svx.github.utility.DiffUtility;
import com.svx.github.view.MainLayoutView;
import javafx.scene.control.Button;

public class MainLayoutController extends Controller<MainLayoutView> {

    public MainLayoutController(AppController appController) {
        super(new MainLayoutView(), appController);
        setActions();
    }

    @Override
    protected void setActions() {
        setMenuActions();
        setSidebarActions();
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


        view.getStageAllButton().setOnAction(e -> stageFile("file1.txt", "Sample content for testing"));

        view.getRepositoryDropdown().valueProperty().addListener((observable, oldRepository, newRepository) -> {
            if (newRepository != null) {
                RepositoryManager.setCurrentRepository(newRepository);
            }
        });
    }

    public void updateChangedFilesList() {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        view.getChangedFilesList().getChildren().clear();
        versionControl.getIndex().getStagedFiles().forEach((filename, blob) -> {
            Button fileButton = new Button(filename);
            fileButton.setOnAction(e -> showFileDifference(filename, blob.getContent()));
            view.getChangedFilesList().getChildren().add(fileButton);
        });
    }

    private void showFileDifference(String filename, String newContent) {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        Commit currentCommit = versionControl.getCurrentCommit();
        Tree currentTree = currentCommit != null ? currentCommit.getTree() : null;

        String oldContent = "";
        if (currentTree != null) {
            Blob lastBlob = currentTree.getBlob(filename);
            oldContent = lastBlob != null ? lastBlob.getContent() : "";
        }

        String difference = DiffUtility.getDifference(oldContent, newContent);

        view.getTextArea().setText(difference);
    }

    public void stageFile(String filename, String content) {
        VersionControl versionControl = RepositoryManager.getVersionControl();
        if (versionControl == null) return;

        versionControl.getIndex().addFile(filename, content);
        updateChangedFilesList();
    }
}
