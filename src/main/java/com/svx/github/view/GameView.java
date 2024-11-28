package com.svx.github.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GameView extends View<HBox> {
    // Left
    private final GridPane chessBoard;

    // Right
    private final Label onlineStatus;

    public GameView() {
        root = new HBox();

        // Left
        chessBoard = new GridPane();
        chessBoard.getStyleClass().add("chess-board");
        root.getChildren().add(chessBoard);

        // Right
        Label opponentName = new Label("Opponent");
        HBox opponentCapturedPieces = new HBox();

        Label playerName = new Label("Player (You)");
        HBox playerCapturedPieces = new HBox();

        VBox topSection = new VBox(opponentName, opponentCapturedPieces, playerName, playerCapturedPieces);

        Label onlineLabel = new Label("Online Status: ");
        onlineStatus = new Label("Offline");
        HBox onlineText = new HBox(onlineLabel, onlineStatus);

        VBox rightSection = new VBox();
        root.getChildren().add(rightSection);
    }

    @Override
    public void initializeView() {

    }
}
