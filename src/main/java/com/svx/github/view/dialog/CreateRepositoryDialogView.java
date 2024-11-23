package com.svx.github.view.dialog;

import javafx.geometry.Pos;
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
        root.getStyleClass().add("dialog-root");

        HBox titleBar = createTitleBar("Create New Repository");

        Label nameLabel = new Label("Name");
        nameLabel.getStyleClass().addAll("bold");
        nameField = new TextField();
        nameField.setPromptText("Enter repository name...");

        Label pathLabel = new Label("Local path");
        pathLabel.getStyleClass().addAll("bold");
        pathField = new TextField();
        pathField.setPromptText("Enter repository path...");

        chooseDirectoryButton = new Button("Choose");
        chooseDirectoryButton.getStyleClass().add("secondary-button");

        confirmButton = new Button("Create");
        confirmButton.getStyleClass().add("primary-button");
        confirmButton.getStyleClass().add("bottom-button");
        confirmButton.setDisable(true);
        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.getStyleClass().add("bottom-button");
        HBox buttons = new HBox(10, confirmButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-label");

        VBox bottomSection = new VBox(10, nameLabel, nameField, pathLabel,new HBox(10, pathField, chooseDirectoryButton), errorLabel, buttons);
        bottomSection.getStyleClass().add("dialog-bottom-section");

        root.getChildren().addAll(
                titleBar,
                bottomSection
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
