package com.svx.chess.view.dialog;

import com.svx.chess.model.Chess;
import com.svx.github.view.dialog.DialogView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CheckmateDialogView extends DialogView<VBox> {
    private final Chess.PieceColor color;
    private Button resetButton;
    private final Button toLoginButton;

    public CheckmateDialogView(Chess.PieceColor color, Button toLoginButton) {
        super();
        this.color = color;
        this.toLoginButton = toLoginButton;
    }

    @Override
    public void initializeView() {
        root = new VBox();
        root.getStyleClass().add("dialog-root");

        HBox titleBar = createTitleBar("Checkmate");

        Label message = new Label("Checkmate! " + color + " wins!");
        resetButton = new Button("Reset");
        resetButton.getStyleClass().add("primary-button");

        cancelButton = new Button("Cancel");

        VBox bottomSection = new VBox(10);
        bottomSection.getStyleClass().add("dialog-bottom-section");
        bottomSection.getChildren().addAll(
                message,
                new HBox(20, resetButton, toLoginButton)
        );

        root.getChildren().addAll(
                titleBar,
                bottomSection
        );
    }

    public Button getResetButton() { return resetButton; }
}
