package com.svx.github.view.dialog;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CloneRepositoryDialogView extends DialogView<VBox> {
    private ScrollPane repositoryListContainer;
    private VBox repositoryList;
    private TextField pathField;
    private Button chooseDirectoryButton;

    @Override
    public void initializeView() {
        root = new VBox(10);
        root.getStyleClass().add("dialog-root");

        HBox titleBar = createTitleBar("Add Local Repository");

        Label selectRepositoryLabel = new Label("Your Repositories");
        selectRepositoryLabel.getStyleClass().add("dialog-section-title");
        selectRepositoryLabel.getStyleClass().add("bold");

        repositoryList = new VBox(10);
        repositoryList.getStyleClass().add("repository-list");

        repositoryListContainer = new ScrollPane(repositoryList);
        repositoryListContainer.setFitToWidth(true);
        repositoryListContainer.getStyleClass().add("scroll-pane");

        pathField = new TextField();
        pathField.setPromptText("Enter repository path...");
        chooseDirectoryButton = new Button("Choose");
        chooseDirectoryButton.getStyleClass().add("secondary-button");

        Label destinationPathLabel = new Label("Destination Path");
        destinationPathLabel.getStyleClass().addAll("bold");

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

        VBox bottomSection = new VBox(10, destinationPathLabel, new HBox(10, pathField, chooseDirectoryButton), errorLabel, buttons);
        bottomSection.getStyleClass().add("dialog-bottom-section");

        root.getChildren().addAll(
                titleBar,
                selectRepositoryLabel,
                repositoryListContainer,
                bottomSection
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

