package com.svx.github.view.dialog;

import javafx.geometry.Pos;
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
        // Main container
        root = new VBox(10);
        root.getStyleClass().add("dialog-root");

        // Title bar
        HBox titleBar = createTitleBar("Add Local Repository");

        // Path input section
        pathField = new TextField();
        pathField.setPromptText("Enter repository path...");
        chooseDirectoryButton = new Button("Choose");
        chooseDirectoryButton.getStyleClass().add("secondary-button");

        // Buttons at the bottom
        confirmButton = new Button("Add");
        confirmButton.getStyleClass().add("primary-button");
        confirmButton.getStyleClass().add("bottom-button");
        confirmButton.setDisable(true);
        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.getStyleClass().add("bottom-button");
        HBox buttons = new HBox(10, confirmButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(
                titleBar,
                new Label("Local path"),
                new HBox(10, pathField, chooseDirectoryButton),
                errorLabel,
                buttons
        );
    }

    public TextField getPathField() { return pathField; }
    public Button getChooseDirectoryButton() { return chooseDirectoryButton; }
}
