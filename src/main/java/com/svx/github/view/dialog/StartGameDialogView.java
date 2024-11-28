package com.svx.github.view.dialog;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StartGameDialogView extends DialogView<VBox> {

    public StartGameDialogView() {
        super();
    }

    @Override
    public void initializeView() {
        root = new VBox(10);
        root.getStyleClass().add("dialog-root");

        HBox titleBar = createTitleBar("Start Offline Game");

        Label nameLabel = new Label("You seem to be offline. Would you like to play Chess to kill time?");

        confirmButton = new Button("Yes");
        confirmButton.getStyleClass().add("primary-button");
        confirmButton.getStyleClass().add("bottom-button");
        cancelButton = new Button("No");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.getStyleClass().add("bottom-button");
        HBox buttons = new HBox(10, confirmButton, cancelButton);
        VBox bottomSection = new VBox(20, nameLabel, buttons);
        bottomSection.getStyleClass().add("dialog-bottom-section");
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                titleBar,
                bottomSection
        );
    }
}
