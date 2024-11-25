package com.svx.github.controller.dialog;

import com.svx.github.controller.AppController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.Debounce;
import com.svx.github.model.NotificationBox;
import com.svx.github.model.Repository;
import com.svx.github.model.UserSingleton;
import com.svx.github.utility.GitUtility;
import com.svx.github.view.dialog.AddRepositoryDialogView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class AddRepositoryDialogController extends DialogController<AddRepositoryDialogView> {
    private Debounce debounce;

    public AddRepositoryDialogController(AppController appController) {
        super(new AddRepositoryDialogView(), appController);
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

        view.getPathField().textProperty().addListener(((observable, oldValue, newValue) ->debounce.trigger()));

        view.getConfirmButton().setOnAction(e -> {
            Path path = Paths.get(view.getPathField().getText().trim());
            Path configFilePath = path.resolve(".git").resolve("config");
            String repositoryName = loadRepositoryName(configFilePath);

            Repository newRepo = new Repository(repositoryName, "", UserSingleton.getCurrentUser().getId(), path);
            Repository.addRepository(newRepo);
            try {
                RepositoryManager.setCurrentRepository(newRepo);
            } catch (IOException | SQLException ex) {
                appController.showNotification("Error loading repository", NotificationBox.NotificationType.ERROR, "fas-check-circle");
            }

            appController.showNotification("Repository added successfully", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            hideDialog();
        });
    }

    private String loadRepositoryName(Path configFilePath) {
        try (BufferedReader reader = Files.newBufferedReader(configFilePath)) {
            String line;
            boolean inRepositorySection = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.equals("[repository]")) {
                    inRepositorySection = true;
                } else if (inRepositorySection && line.startsWith("name =")) {
                    return line.split("=", 2)[1].trim();
                } else if (line.startsWith("[")) {
                    inRepositorySection = false;
                }
            }
        } catch (IOException e) {
            appController.showNotification("Error reading config file", NotificationBox.NotificationType.ERROR, "fas-check-circle");
        }
        return "Unnamed Repository";
    }

    private void initializeDebounce() {
        debounce = new Debounce(Duration.seconds(0.5), () -> {
            Path path = Paths.get(view.getPathField().getText().trim());

            if (!GitUtility.hasRepository(path)) {
                view.getErrorLabel().setText("Not a valid repository folder.");
                view.getConfirmButton().setDisable(true);
            } else {
                view.getErrorLabel().setText("");
                view.getConfirmButton().setDisable(false);
            }
        });
    }
}
