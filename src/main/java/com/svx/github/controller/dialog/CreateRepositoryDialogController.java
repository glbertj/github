package com.svx.github.controller.dialog;

import com.svx.github.controller.AppController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.Debounce;
import com.svx.github.model.NotificationBox;
import com.svx.github.model.Repository;
import com.svx.github.model.UserSingleton;
import com.svx.github.repository.RepositoryRepository;
import com.svx.github.utility.GitUtility;
import com.svx.github.view.dialog.CreateRepositoryDialogView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateRepositoryDialogController extends DialogController<CreateRepositoryDialogView> {
    private Debounce pathDebounce;
    private Debounce nameDebounce;

    private boolean isPathValid = false;
    private boolean isNameValid = false;

    public CreateRepositoryDialogController(AppController appController) {
        super(new CreateRepositoryDialogView(), appController);
        setActions();
        initializeNameDebounce();
        initializePathDebounce();
    }

    @Override
    public void setActions() {
        super.setActions();

        view.getChooseDirectoryButton().setOnAction(e -> chooseDirectory());

        view.getConfirmButton().setOnAction(e -> createRepository());

        view.getNameField().textProperty().addListener((observable, oldValue, newValue) -> {
            view.getConfirmButton().setDisable(true);
            nameDebounce.trigger();
        });

        view.getPathField().textProperty().addListener((observable, oldValue, newValue) -> {
            view.getConfirmButton().setDisable(true);
            pathDebounce.trigger();
        });
    }

    private void chooseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(view.getDialogStage());
        if (selectedDirectory != null) {
            view.getPathField().setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void createRepository() {
        Path path = Paths.get(view.getPathField().getText().trim());
        Path gitDir = path.resolve(".goat");
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
            appController.showNotification("Error setting up .goat directory structure.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
        }

        Repository newRepo = new Repository(view.getNameField().getText(), "", UserSingleton.getCurrentUser().getId(), path);
        Repository.addRepository(newRepo);
        try {
            RepositoryManager.setCurrentRepository(newRepo);
            RepositoryManager.updateRecentRepository();
        } catch (IOException | SQLException ex) {
            appController.showNotification("Error loading repository.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
        }

        hideDialog();
        appController.showNotification("Repository created successfully.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
    }

    private void initializeNameDebounce() {
        nameDebounce = new Debounce(Duration.seconds(0.5), () -> {
            List<Repository> repositories;
            try {
                repositories = RepositoryRepository.loadAllUserRepositories();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            List<String> repositoryNames = new ArrayList<>();
            for (Repository repository : repositories) {
                repositoryNames.add(repository.getName());
            }

            if (view.getNameField().getText().trim().isEmpty()) {
                view.getErrorLabel().setText("Repository name cannot be empty.");
                view.getConfirmButton().setDisable(true);
                isNameValid = false;
                return;
            }

            if (repositoryNames.contains(view.getNameField().getText().trim())) {
                view.getErrorLabel().setText("Repository name already exists.");
                view.getConfirmButton().setDisable(true);
                isNameValid = false;
            } else {
                isNameValid = true;

                if (isPathValid) {
                    view.getErrorLabel().setText("");
                } else {
                    pathDebounce.trigger();
                }

                view.getConfirmButton().setDisable(!isPathValid);
            }
        });
    }

    private void initializePathDebounce() {
        pathDebounce = new Debounce(Duration.seconds(0.5), () -> {
            if (view.getPathField().getText().trim().isEmpty()) {
                view.getErrorLabel().setText("Repository path cannot be empty.");
                view.getConfirmButton().setDisable(true);
                isPathValid = false;
                return;
            }

            Path path = Paths.get(view.getPathField().getText().trim());
            if (GitUtility.hasRepository(path)) {
                view.getErrorLabel().setText("Folder is already a repository.");
                view.getConfirmButton().setDisable(true);
                isPathValid = false;
            } else {
                isPathValid = true;

                if (isNameValid) {
                    view.getErrorLabel().setText("");
                } else {
                    nameDebounce.trigger();
                }

                view.getConfirmButton().setDisable(!isNameValid);
            }
        });
    }
}