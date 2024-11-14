package com.svx.github.view.dialog;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class CommitMessageDialogView extends DialogView<VBox> {
    private TextArea messageTextArea;

    public CommitMessageDialogView() {
        super();
    }

    @Override
    public void initializeView() {
        root = new VBox(10);

        Label messageLabel = new Label("Enter Commit Message:");
        messageTextArea = new TextArea();
        messageTextArea.setPromptText("Commit message...");

        confirmButton = new Button("Commit");
        confirmButton.setDisable(true);
        cancelButton = new Button("Cancel");
        errorLabel = new Label("");

        messageTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            confirmButton.setDisable(newValue.trim().isEmpty());
        });

        root.getChildren().addAll(
                messageLabel,
                messageTextArea,
                new VBox(confirmButton, cancelButton),
                errorLabel
        );
    }

    public TextArea getMessageTextArea() {
        return messageTextArea;
    }
}

