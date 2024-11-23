package com.svx.github.controller.dialog;

import com.svx.github.controller.AppController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import com.svx.github.repository.RepositoryRepository;
import com.svx.github.view.dialog.CloneRepositoryDialogView;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CloneRepositoryDialogController extends DialogController<CloneRepositoryDialogView> {
    private Repository selectedRepo = null;

    public CloneRepositoryDialogController(AppController appController) {
        super(new CloneRepositoryDialogView(), appController);
        populateRepositoryList();
        setActions();
    }

    private void populateRepositoryList() {
        List<Repository> repositories = RepositoryRepository.loadAllUserRepositories();

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
        FontIcon iconView = new FontIcon("fab-git-alt");
        iconView.getStyleClass().add("icon");

        Label repositoryLabel = new Label(repository.getName());
        repositoryLabel.getStyleClass().add("primary-text");

        HBox buttonContent = new HBox(iconView, repositoryLabel);
        buttonContent.setUserData(repository);
        buttonContent.getStyleClass().add("repository-list-button");
        buttonContent.setOnMouseClicked(e -> {
            view.getRepositoryList().getChildren().forEach(node -> {
                if (node instanceof HBox hbox) {
                    hbox.getStyleClass().remove("active");
                }
            });

            buttonContent.getStyleClass().add("active");
            selectedRepo = repository;
        });

        return buttonContent;
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

            createGitStructure(destinationPath);

            Repository clonedRepo = new Repository(
                    selectedRepo.getName(),
                    selectedRepo.getLatestCommitId(),
                    UserSingleton.getCurrentUser().getId(),
                    destinationPath
            );

            VersionControl versionControl = new VersionControl(clonedRepo);
            versionControl.pull();

            Repository.addRepository(clonedRepo);
            RepositoryManager.setCurrentRepository(clonedRepo);

            appController.showNotification("Repository cloned successfully", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            hideDialog();
        } catch (IOException ex) {
            appController.showNotification("Error cloning repository.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
        }
    }

    private void createGitStructure(Path destinationPath) throws IOException {
        Path gitDir = destinationPath.resolve(".git");
        Path objectsDir = gitDir.resolve("objects");
        Path refsDir = gitDir.resolve("refs").resolve("heads");
        Path headFile = gitDir.resolve("HEAD");

        Files.createDirectories(objectsDir);
        Files.createDirectories(refsDir);

        Files.writeString(headFile, "ref: refs/heads/master\n");
    }
}

