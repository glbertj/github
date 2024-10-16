package com.svx.github.controller.dialog;

import com.svx.github.model.Config;
import com.svx.github.utility.FileUtility;
import com.svx.github.view.dialog.AddRepositoryDialogView;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class AddRepositoryDialogController extends DialogController<AddRepositoryDialogView> {
    public AddRepositoryDialogController() {
        super(new AddRepositoryDialogView());
        setActions();
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

        view.getPathField().textProperty().addListener(((observable, oldValue, newValue) -> {
            FileUtility.checkRepositoryValidity(oldValue, view);
        }));

        view.getConfirmButton().setOnAction(e -> {
            String path = view.getPathField().getText().trim();
            File configFile = new File(path, "config");
            Config config = new Config(configFile);

            System.out.println(config.getValue("repository", "name"));
        });
    }
}
