package com.svx.github.view.dialog;

import com.svx.github.model.Repository;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CloneRepositoryDialogView extends DialogView<VBox> {
    private ComboBox<Repository> repositoryDropdown;
    private TextField pathField;
    private Button chooseDirectoryButton;

    @Override
    public void initializeView() {
        root = new VBox(10);

        repositoryDropdown = new ComboBox<>();
        pathField = new TextField();
        chooseDirectoryButton = new Button("Choose");
        confirmButton = new Button("Clone");
        confirmButton.setDisable(true);
        cancelButton = new Button("Cancel");
        errorLabel = new Label("");

        root.getChildren().addAll(
                new Label("Select Repository"),
                repositoryDropdown,
                new Label("Destination Path"),
                new HBox(10, pathField, chooseDirectoryButton),
                new HBox(10, confirmButton, cancelButton),
                errorLabel
        );
    }

    public ComboBox<Repository> getRepositoryDropdown() {
        return repositoryDropdown;
    }

    public TextField getPathField() {
        return pathField;
    }

    public Button getChooseDirectoryButton() {
        return chooseDirectoryButton;
    }
}

