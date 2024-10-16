package com.svx.github.controller.dialog;

import com.svx.github.model.Debounce;
import com.svx.github.model.Repository;
import com.svx.github.utility.GitUtility;
import com.svx.github.view.dialog.CreateRepositoryDialogView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class CreateRepositoryDialogController extends DialogController<CreateRepositoryDialogView> {
    private Debounce debounce;

    public CreateRepositoryDialogController() {
        super(new CreateRepositoryDialogView());
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
            String path = view.getPathField().getText() + "/.git";

            File gitDir = new File(path);
            File objectsDir = new File(gitDir, "objects");
            File refsDir = new File(gitDir, "refs");
            File headsDir = new File(refsDir, "heads");
            File configFile = new File(gitDir, "config");
            File indexFile = new File(gitDir, "index");

            if (gitDir.mkdirs() && gitDir.isDirectory() && objectsDir.mkdirs() && refsDir.mkdirs() && headsDir.mkdirs()) {
                System.out.println("Successfully created .git directory structure");
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write("[repository]\n");
                writer.write("name = " + view.getNameField().getText() + "\n");
            } catch (IOException error) {
                System.out.println("Error creating config file: " + error.getMessage());
            }

            try {
                if (indexFile.createNewFile()) {
                    System.out.println("Successfully created index file");
                }
            } catch (IOException error) {
                System.out.println("Error creating index file: " + error.getMessage());
            }

            Repository.addRepository(new Repository(view.getNameField().getText(), view.getPathField().getText()));

            try {
                Files.setAttribute(gitDir.toPath(), "dos:hidden", true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            hideDialog();
        });
    }

    private void initializeDebounce() {
        debounce = new Debounce(Duration.seconds(0.5), () -> {
            if (GitUtility.hasRepository(view.getPathField().getText(), view)) {
                view.getErrorLabel().setText("Repository already exists");
                view.getConfirmButton().setDisable(true);
            } else {
                view.getErrorLabel().setText("");
                view.getConfirmButton().setDisable(false);
            }
        });
    }
}
