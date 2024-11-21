package com.svx.github.controller.dialog;

import com.svx.github.controller.AppController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import com.svx.github.repository.RepositoryRepository;
import com.svx.github.view.dialog.CloneRepositoryDialogView;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CloneRepositoryDialogController extends DialogController<CloneRepositoryDialogView> {

    public CloneRepositoryDialogController(AppController appController) {
        super(new CloneRepositoryDialogView(), appController);
        populateRepositoryDropdown();
        setActions();
    }

    private void populateRepositoryDropdown() {
        List<Repository> repositories = RepositoryRepository.loadAllUserRepositories(UserSingleton.getCurrentUser().getId());
        view.getRepositoryDropdown().setConverter(new StringConverter<>() {
            @Override
            public String toString(Repository repository) {
                return repository != null ? repository.getName() : "";
            }

            @Override
            public Repository fromString(String string) {
                return view.getRepositoryDropdown().getItems().stream()
                        .filter(repo -> repo.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        view.getRepositoryDropdown().getItems().addAll(repositories);

        if (!repositories.isEmpty()) {
            view.getRepositoryDropdown().getSelectionModel().select(0);
        }
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

        view.getPathField().textProperty().addListener((observable, oldValue, newValue) -> validateInputs());
        view.getRepositoryDropdown().valueProperty().addListener((observable, oldValue, newValue) -> validateInputs());

        view.getConfirmButton().setOnAction(e -> cloneRepository());
    }

    private void validateInputs() {
        boolean valid = !view.getPathField().getText().trim().isEmpty() && view.getRepositoryDropdown().getValue() != null;
        view.getConfirmButton().setDisable(!valid);
    }

    private void cloneRepository() {
        Repository selectedRepo = view.getRepositoryDropdown().getValue();
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

