package com.svx.chess.view;

import com.svx.chess.model.Chess;
import com.svx.chess.model.ChessBoard;
import com.svx.chess.model.ChessTile;
import com.svx.github.view.View;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.util.Objects;

public class ChessView extends View<BorderPane> {
    private Chess.PieceColor playerColor;

    // Left
    private ChessBoard chessBoard;

    // Right
    private Label onlineStatus;
    private FlowPane capturedWhiteBox;
    private FlowPane capturedBlackBox;

    @Override
    public void initializeView() {
        root = new BorderPane();
        root.getStyleClass().add("game-root");
        styleReference = Objects.requireNonNull(
                getClass().getResource("/com/svx/chess/style/game.css")
        ).toExternalForm();

        playerColor = Math.random() > 0.5 ? Chess.PieceColor.WHITE : Chess.PieceColor.BLACK;

        HBox leftSection = new HBox();
        chessBoard = new ChessBoard(playerColor);
        leftSection.getChildren().add(chessBoard);
        leftSection.setAlignment(Pos.CENTER);

        VBox rightSection = createRightSection();

        HBox container = new HBox(40, leftSection, rightSection);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        root.setCenter(container);
    }

    public void showValidMoves(ChessTile selectedTile, int[] validMoves) {
        ChessTile[][] tiles = chessBoard.getTiles();

        for (int move : validMoves) {
            int targetRow = move / 8;
            int targetCol = move % 8;

            ChessTile targetTile = tiles[targetRow][targetCol];
            if (targetTile == null) return;
            targetTile.setIsValidMove(true);

            if (targetTile.getPiece() != null) {
                if (!selectedTile.getPiece().getColor().equals(targetTile.getPiece().getColor())) {
                    targetTile.setIsEatable(true);
                    targetTile.setIsValidMove(false);
                }
            }
        }
    }

    public void hideValidMoves() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof ChessTile tile) {
                tile.setIsValidMove(false);
                tile.setIsEatable(false);
                tile.setIsEnPassantMove(false);
                tile.setIsCastleMove(false);
            }
        }
    }

    public void clearHighlightedTiles() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof ChessTile tile) {
                tile.setIsRecentMove(false);
            }
        }
    }

    // Right Section
    private VBox createRightSection() {
        Label opponentName = new Label("Opponent");
        Label playerName = new Label("Player (You)");

        capturedWhiteBox = new FlowPane();
        capturedWhiteBox.getStyleClass().add("captured-box");

        capturedBlackBox = new FlowPane();
        capturedBlackBox.getStyleClass().add("captured-box");

        VBox topSection;
        if (playerColor.equals(Chess.PieceColor.WHITE)) {
            topSection = new VBox(opponentName, capturedWhiteBox, playerName, capturedBlackBox);
        } else {
            topSection = new VBox(opponentName, capturedBlackBox, playerName, capturedWhiteBox);
        }
        VBox.setVgrow(topSection, Priority.ALWAYS);

        Label onlineLabel = new Label("Online Status: ");
        onlineStatus = new Label("Offline");
        HBox onlineTextBox = new HBox(onlineLabel, onlineStatus);
        Button backToLoginButton = new Button("Login");
        backToLoginButton.setDisable(true);
        VBox bottomSection = new VBox(onlineTextBox, backToLoginButton);
        VBox.setVgrow(bottomSection, Priority.NEVER);

        return new VBox(topSection, bottomSection);
    }

    public Chess.PieceColor getPlayerColor() { return playerColor; }
    public ChessBoard getChessBoard() { return chessBoard; }
    public FlowPane getCapturedWhiteBox() { return capturedWhiteBox; }
    public FlowPane getCapturedBlackBox() { return capturedBlackBox; }
}