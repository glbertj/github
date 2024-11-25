package com.svx.github.controller.dialog;

import com.svx.github.controller.AppController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import com.svx.github.repository.RepositoryRepository;
import com.svx.github.utility.ComponentUtility;
import com.svx.github.view.dialog.CloneRepositoryDialogView;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class CloneRepositoryDialogController extends DialogController<CloneRepositoryDialogView> {
    private Repository selectedRepo = null;

    public CloneRepositoryDialogController(AppController appController) {
        super(new CloneRepositoryDialogView(), appController);
        setActions();
        populateRepositoryList();
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

        view.getConfirmButton().setOnAction(e -> cloneRepository());

        view.getConfirmButton().disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> selectedRepo == null || view.getPathField().getText() == null || view.getPathField().getText().isBlank(),
                        view.getPathField().textProperty()
                )
        );
    }

    private void populateRepositoryList() {
        List<Repository> repositories = null;
        try {
            repositories = RepositoryRepository.loadAllUserRepositories();
        } catch (SQLException e) {
            appController.showNotification("Error loading repositories.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
        }

        repositories.forEach(repo -> {
            if (repositoryExistsInList(repo)) {
                view.getRepositoryList().getChildren().add(createRepositoryButton(repo));
            }
        });
    }

    private boolean repositoryExistsInList(Repository repo) {
        return view.getRepositoryList().getChildren().stream()
                .noneMatch(node -> {
                    if (node instanceof HBox buttonContent) {
                        Repository existingRepo = (Repository) buttonContent.getUserData();
                        return existingRepo != null && existingRepo.equals(repo);
                    }
                    return false;
                });
    }

    public HBox createRepositoryButton(Repository repository) {
        HBox button = ComponentUtility.createListButton(repository, view, ComponentUtility.listButtonType.CLONE_REPOSITORY_DIALOG);
        if (button == null) return null;

        button.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            selectedRepo = repository;
        });

        return button;
    }

    private void cloneRepository() {
        Path destinationPath = Paths.get(view.getPathField().getText().trim());

        if (selectedRepo == null) {
            appController.showNotification("No repository selected to clone.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
            return;
        }

        if (Files.exists(destinationPath.resolve(".git"))) {
            appController.showNotification("The destination already contains a Git repository.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
            return;
        }

        try {
            Files.createDirectories(destinationPath);

            Repository clonedRepo = new Repository(
                    selectedRepo.getName(),
                    selectedRepo.getLatestCommitId(),
                    UserSingleton.getCurrentUser().getId(),
                    destinationPath
            );

            try {
                Path gitDir = destinationPath.resolve(".git");
                Path objectsDir = gitDir.resolve("objects");
                Path configFile = gitDir.resolve("config");
                Path refsDir = gitDir.resolve("refs").resolve("heads");

                Files.createDirectories(objectsDir);
                Files.createDirectories(refsDir);

                try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
                    writer.write("[repository]\n");
                    writer.write("name = " + clonedRepo.getName() + "\n");
                }

                Files.setAttribute(gitDir, "dos:hidden", true);
            } catch (IOException ex) {
                appController.showNotification("Error setting up .git directory structure.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
                return;
            }

            VersionControl versionControl = new VersionControl(clonedRepo);
            try {
                versionControl.pull();
            } catch (Exception e) {
                appController.showNotification("Error cloning repository.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
                return;
            }

            Repository.addRepository(clonedRepo);
            RepositoryManager.setCurrentRepository(clonedRepo);

            appController.showNotification("Repository cloned successfully", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            hideDialog();
        } catch (IOException | SQLException ex) {
            appController.showNotification("Error cloning repository.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
        }
    }
}

