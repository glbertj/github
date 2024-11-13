package com.svx.github.controller.dialog;

import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.Debounce;
import com.svx.github.model.Repository;
import com.svx.github.utility.GitUtility;
import com.svx.github.view.dialog.AddRepositoryDialogView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import java.io.*;

public class AddRepositoryDialogController extends DialogController<AddRepositoryDialogView> {
    private Debounce debounce;

    public AddRepositoryDialogController() {
        super(new AddRepositoryDialogView());
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
            String path = view.getPathField().getText().trim();

            if (GitUtility.hasRepository(path)) {
                File configFile = new File(path, ".git/config");
                String repositoryName = loadRepositoryName(configFile);

                Repository newRepo = new Repository(repositoryName, path);
                Repository.addRepository(newRepo);
                RepositoryManager.setCurrentRepository(newRepo);

                System.out.println("Repository added successfully: " + repositoryName);

                hideDialog();
            } else {
                view.getErrorLabel().setText("The selected directory is not a valid Git repository.");
            }
        });
    }

    private String loadRepositoryName(File configFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
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
            System.out.println("Error reading config file: " + e.getMessage());
        }
        return "Unnamed Repository";
    }


    private void initializeDebounce() {
        debounce = new Debounce(Duration.seconds(0.5), () -> {
            if (!GitUtility.hasRepository(view.getPathField().getText())) {
                view.getErrorLabel().setText("Not a repository");
                view.getConfirmButton().setDisable(true);
            } else {
                view.getErrorLabel().setText("");
                view.getConfirmButton().setDisable(false);
            }
        });
    }
}
