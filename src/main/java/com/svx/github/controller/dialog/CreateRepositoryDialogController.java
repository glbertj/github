package com.svx.github.controller.dialog;

import com.svx.github.controller.AppController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.Debounce;
import com.svx.github.model.NotificationBox;
import com.svx.github.model.Repository;
import com.svx.github.model.UserSingleton;
import com.svx.github.utility.GitUtility;
import com.svx.github.view.dialog.CreateRepositoryDialogView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateRepositoryDialogController extends DialogController<CreateRepositoryDialogView> {
    private Debounce debounce;

    public CreateRepositoryDialogController(AppController appController) {
        super(new CreateRepositoryDialogView(), appController);
        setActions();
        initializeDebounce();
    }

    @Override
    public void setActions() {
        super.setActions();

        view.getChooseDirectoryButton().setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(view.getDialogStage());
            if (selectedDirectory != null) {
                view.getPathField().setText(selectedDirectory.getAbsolutePath());
            }
        });

        view.getPathField().textProperty().addListener((observable, oldValue, newValue) -> debounce.trigger());

        view.getConfirmButton().setOnAction(e -> {
            Path path = Paths.get(view.getPathField().getText().trim());
            Path gitDir = path.resolve(".git");
            Path objectsDir = gitDir.resolve("objects");
            Path refsDir = gitDir.resolve("refs");
            Path headsDir = refsDir.resolve("heads");
            Path configFile = gitDir.resolve("config");
            Path indexFile = gitDir.resolve("index");

            try {
                Files.createDirectories(objectsDir);
                Files.createDirectories(refsDir);
                Files.createDirectories(headsDir);

                try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
                    writer.write("[repository]\n");
                    writer.write("name = " + view.getNameField().getText() + "\n");
                }

                Files.createFile(indexFile);
                Files.setAttribute(gitDir, "dos:hidden", true);
            } catch (IOException ex) {
                appController.showNotification("Error setting up .git directory structure.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
            }

            Repository newRepo = new Repository(view.getNameField().getText(), "", UserSingleton.getCurrentUser().getId(), path);
            Repository.addRepository(newRepo);
            RepositoryManager.setCurrentRepository(newRepo);

            hideDialog();
            appController.showNotification("Repository created successfully.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
        });
    }

    private void initializeDebounce() {
        debounce = new Debounce(Duration.seconds(0.5), () -> {
            Path path = Paths.get(view.getPathField().getText().trim());
            if (GitUtility.hasRepository(path)) {
                view.getErrorLabel().setText("Folder is already a repository.");
                view.getConfirmButton().setDisable(true);
            } else {
                view.getErrorLabel().setText("");
                view.getConfirmButton().setDisable(false);
            }
        });
    }
}