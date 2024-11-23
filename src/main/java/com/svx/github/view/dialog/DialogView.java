package com.svx.github.view.dialog;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public abstract class DialogView<T extends Parent> {
    protected Stage dialogStage;
    protected T root;
    protected final String styleReference;

    protected Button cancelButton;
    protected Button confirmButton;
    protected Label errorLabel;
    protected FontIcon closeIcon;

    public DialogView() {
        root = null;
        styleReference = null;
    }

    public abstract void initializeView();

    protected HBox createTitleBar(String title) {
        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("title-bar");
        titleBar.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("primary-text", "dialog-title", "bold");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        closeIcon = new FontIcon("fas-times");
        closeIcon.getStyleClass().add("close-icon");
        closeIcon.setCursor(Cursor.HAND);

        titleBar.getChildren().addAll(titleLabel, spacer, closeIcon);

        return titleBar;
    }

    public Stage getDialogStage() { return dialogStage; }
    public void setDialogStage(Stage dialogStage) { this.dialogStage = dialogStage; }
    public T getRoot() { return root; }
    public String getStyleReference() { return styleReference; }
    public FontIcon getCloseIcon() { return closeIcon; }
    public Button getCancelButton() { return cancelButton; }
    public Button getConfirmButton() { return confirmButton; }
    public Label getErrorLabel() { return errorLabel; }
}