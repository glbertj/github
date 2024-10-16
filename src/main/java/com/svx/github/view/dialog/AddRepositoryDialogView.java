package com.svx.github.view.dialog;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AddRepositoryDialogView extends DialogView<VBox> {
    private TextField pathField;
    private Button chooseDirectoryButton;

    @Override
    public void initializeView() {
        root = new VBox(10);

        pathField = new TextField();
        chooseDirectoryButton = new Button("Choose");
        confirmButton = new Button("Add Local Repository");
        cancelButton = new Button("Cancel");
        errorLabel = new Label("");

        root.getChildren().addAll(
                new Label("Path"),
                new HBox(10, pathField, chooseDirectoryButton),
                new HBox(10, confirmButton, cancelButton),
                errorLabel
        );
    }

    public TextField getPathField() {
        return pathField;
    }

    public Button getChooseDirectoryButton() {
        return chooseDirectoryButton;
    }
}
