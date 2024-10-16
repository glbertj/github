package com.svx.github.view.dialog;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CreateRepositoryDialogView extends DialogView<VBox> {
    private Button chooseDirectoryButton;

    private TextField nameField;
    private TextField pathField;

    public CreateRepositoryDialogView() {
        super();
    }

    @Override
    public void initializeView() {
        root = new VBox(10);

        nameField = new TextField();
        pathField = new TextField();

        chooseDirectoryButton = new Button("Choose");
        confirmButton = new Button("Create New Repository");
        cancelButton = new Button("Cancel");
        errorLabel = new Label("");

        root.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Local Path:"),
                new HBox(10, pathField, chooseDirectoryButton),
                new HBox(confirmButton, cancelButton),
                errorLabel
        );
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextField getPathField() {
        return pathField;
    }

    public Button getChooseDirectoryButton() {
        return chooseDirectoryButton;
    }
}
