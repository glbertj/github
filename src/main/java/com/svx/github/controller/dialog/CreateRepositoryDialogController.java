package com.svx.github.controller.dialog;

import com.svx.github.model.Debounce;
import com.svx.github.model.Repository;
import com.svx.github.utility.FileUtility;
import com.svx.github.view.dialog.CreateRepositoryDialogView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

        view.getPathField().textProperty().addListener((observable, oldValue, newValue)-> {
            debounce.trigger();
        });

        view.getConfirmButton().setOnAction(e -> {
            String path = view.getPathField().getText() + "/.git";
            File configFile = new File(path, "config");

            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write("[repository]\n");
                writer.write("name = " + view.getNameField() + "\n");
            } catch (IOException error) {
                System.out.println("Error creating config file: " + error.getMessage());
            }

            Repository.addRepository(new Repository(view.getNameField().getText(), view.getPathField().getText()));

            try {
                Files.setAttribute(Path.of(path), "dos:hidden", true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            hideDialog();
        });
    }

    private void initializeDebounce() {
        debounce = new Debounce(Duration.seconds(0.5), () -> {
            FileUtility.checkRepositoryValidity(view.getPathField().getText(), view);
        });
    }
}
