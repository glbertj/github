package com.svx.github.view.dialog;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CloneRepositoryDialogView extends DialogView<VBox> {
    private VBox repositoryList;
    private TextField pathField;
    private Button chooseDirectoryButton;

    @Override
    public void initializeView() {
        root = new VBox(10);
        root.getStyleClass().add("dialog-root");

        HBox titleBar = createTitleBar("Add Local Repository");

        repositoryList = new VBox(10);
        repositoryList.getStyleClass().add("repository-list");

        pathField = new TextField();
        pathField.setPromptText("Enter repository path...");
        chooseDirectoryButton = new Button("Choose");
        chooseDirectoryButton.getStyleClass().add("secondary-button");

        confirmButton = new Button("Clone");
        confirmButton.getStyleClass().add("primary-button");
        confirmButton.getStyleClass().add("bottom-button");
        confirmButton.setDisable(true);
        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.getStyleClass().add("bottom-button");
        HBox buttons = new HBox(10, confirmButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        root.getChildren().addAll(
                titleBar,
                new Label("Select Repository"),
                repositoryList,
                new Label("Destination Path"),
                new HBox(10, pathField, chooseDirectoryButton),
                errorLabel,
                buttons
        );
    }

    public VBox getRepositoryList() {
        return repositoryList;
    }

    public TextField getPathField() {
        return pathField;
    }

    public Button getChooseDirectoryButton() {
        return chooseDirectoryButton;
    }
}

