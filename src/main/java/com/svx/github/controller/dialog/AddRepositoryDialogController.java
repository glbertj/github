package com.svx.github.controller.dialog;

import com.svx.github.model.Config;
import com.svx.github.model.Debounce;
import com.svx.github.model.Repository;
import com.svx.github.utility.FileUtility;
import com.svx.github.view.dialog.AddRepositoryDialogView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

import java.io.File;

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
            File configFile = new File(path, ".git/config");
            Config config = new Config(configFile);

            Repository.addRepository(new Repository(config.getValue("repository", "name"), path));
        });
    }

    private void initializeDebounce() {
        debounce = new Debounce(Duration.seconds(0.5), () -> {
            if (!FileUtility.hasRepository(view.getPathField().getText(), view)) {
                view.getErrorLabel().setText("Not a repository");
                view.getConfirmButton().setDisable(true);
            } else {
                view.getErrorLabel().setText("");
                view.getConfirmButton().setDisable(false);
            }
        });
    }
}
