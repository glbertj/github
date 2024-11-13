package com.svx.github.controller;

import com.svx.github.controller.dialog.AddRepositoryDialogController;
import com.svx.github.controller.dialog.CreateRepositoryDialogController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import com.svx.github.utility.DiffUtility;
import com.svx.github.view.MainLayoutView;
import javafx.scene.control.Button;
import java.io.IOException;
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

        view.getTestButton().setOnAction(e -> performTest());

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

    private void performTest() {
        Repository repository = RepositoryManager.getCurrentRepository();
        if (repository == null) {
            System.out.println("No active repository selected.");
            return;
        }

        String filename = "file1.txt";
        String content = "Sample content for testing blob persistence.";

        Blob blob = new Blob(content, repository);
        System.out.println("Created Blob with ID: " + blob.getId());

        try {
            Blob loadedBlob = Blob.loadFromDisk(blob.getId(), repository);
            System.out.println("Loaded Blob Content: " + loadedBlob.getContent());

            if (blob.getContent().equals(loadedBlob.getContent())) {
                System.out.println("Test Passed");
            } else {
                System.out.println("Test Failed");
            }
        } catch (IOException ex) {
            System.out.println("Error loading blob from disk: " + ex.getMessage());
        }
    }
}
