package com.svx.github.view.dialog;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public abstract class DialogView<T extends Parent> {
    protected Stage dialogStage;
    protected T root;
    protected String styleReference;

    protected Button cancelButton;
    protected Button confirmButton;
    protected Label errorLabel;

    public DialogView() {
        root = null;
        styleReference = null;
    }

    public abstract void initializeView();

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public T getRoot() {
        return root;
    }

    public String getStyleReference() {
        return styleReference;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    public Label getErrorLabel() {
        return errorLabel;
    }
}
