package com.svx.github.view;

import com.svx.github.model.ChessPiece;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Objects;

public class GameView extends View<BorderPane> {
    // Left
    private GridPane chessBoard;
    private StackPane selectedTile;

    // Right
    private Label onlineStatus;
    private HBox opponentCapturedPieces;
    private HBox playerCapturedPieces;

    @Override
    public void initializeView() {
        root = new BorderPane();
        root.getStyleClass().add("game-root");

        styleReference = Objects.requireNonNull(
                getClass().getResource("/com/svx/github/style/game.css")
        ).toExternalForm();


        HBox leftSection = new HBox();
        initializeBoard();
        leftSection.getChildren().add(chessBoard);
        leftSection.setAlignment(Pos.CENTER);

        VBox rightSection = createRightSection();

        HBox container = new HBox(40, leftSection, rightSection);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        root.setCenter(container);
    }

    public void initializeBoard() {
        chessBoard = new GridPane();
        chessBoard.getStyleClass().add("chess-board");
        chessBoard.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(chessBoard, Priority.ALWAYS);

        for (int row = 0; row < 8; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            chessBoard.getRowConstraints().add(rowConstraints);
        }

        for (int col = 0; col < 8; col++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            chessBoard.getColumnConstraints().add(columnConstraints);
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = createTile(row, col);
                chessBoard.add(tile, col, row);
            }
        }

        addPieces();

        chessBoard.widthProperty().addListener((observable, oldValue, newValue) -> updateTileSizes());
        chessBoard.heightProperty().addListener((observable, oldValue, newValue) -> updateTileSizes());
    }

    private void updateTileSizes() {
        double boardWidth = chessBoard.getWidth();
        double boardHeight = chessBoard.getHeight();

        double size = Math.min(boardWidth, boardHeight) / 8;

        for (Node node : chessBoard.getChildren()) {
            if (node instanceof StackPane tile) {
                tile.setPrefWidth(size);
                tile.setPrefHeight(size);
            }
        }
    }

    private StackPane createTile(int row, int col) {
        StackPane tile = new StackPane();
        tile.getStyleClass().add("tile");
        String tileClass = (row + col) % 2 == 0 ? "green" : "white";
        tile.getStyleClass().add(tileClass);

        Circle validMoveCircle = new Circle(10);
        validMoveCircle.setFill(Color.BLACK);
        validMoveCircle.setOpacity(0.3);
        validMoveCircle.setVisible(false);
        tile.getChildren().add(validMoveCircle);

        tile.setOnMouseClicked(e -> onTileClick(tile));

        return tile;
    }

    private void addPieces() {
        addPiecesToRow(0, ChessPiece.PieceColor.BLACK, new ChessPiece.PieceType[]{
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
        });
        addPawnsToRow(1, ChessPiece.PieceColor.BLACK);

        addPawnsToRow(6, ChessPiece.PieceColor.WHITE);
        addPiecesToRow(7, ChessPiece.PieceColor.WHITE, new ChessPiece.PieceType[]{
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
        });
    }

    private void addPawnsToRow(int row, ChessPiece.PieceColor color) {
        for (int col = 0; col < 8; col++) {
            ChessPiece pawn = new ChessPiece(ChessPiece.PieceType.PAWN, color);
            StackPane pawnTile = getTileAt(row, col);
            pawnTile.getChildren().add(pawn.getImageView());
            pawnTile.setUserData(pawn);
        }
    }

    private void addPiecesToRow(int row, ChessPiece.PieceColor color, ChessPiece.PieceType[] pieceTypes) {
        for (int col = 0; col < 8; col++) {
            ChessPiece piece = new ChessPiece(pieceTypes[col], color);
            StackPane tile = getTileAt(row, col);
            tile.setUserData(piece);
            tile.getChildren().add(piece.getImageView());
        }
    }

    private void onTileClick(StackPane targetTile) {
        // No piece selected
        if (selectedTile == null) {
            if (targetTile.getUserData() == null) {
                return;
            }

            selectedTile = targetTile;
            selectedTile.getStyleClass().add("recent");

            showValidMoves(selectedTile);
            return;
        }

        ChessPiece selectedPiece = (ChessPiece) selectedTile.getUserData();

        // Selected a valid move
        if (!targetTile.getChildren().isEmpty() && targetTile.getChildren().get(0) instanceof Circle validMoveCircle && validMoveCircle.isVisible()) {
            clearHighlightedTiles();
            selectedTile.getStyleClass().add("recent");
            targetTile.getStyleClass().add("recent"); // TODO! FIX LATER - Try moving the same piece thrice or move twice + one other piece

            targetTile.getChildren().add(selectedPiece.getImageView());
            targetTile.setUserData(selectedPiece);
            selectedTile.setUserData(null);
            selectedTile = null;
            hideValidMoves();
        }

        // Selected current piece
        else if (selectedTile == targetTile) {
            selectedTile.getStyleClass().remove("recent");
            hideValidMoves();
            selectedTile = null;
        }

        // Selected a friendly piece
        else if (targetTile.getUserData() != null && targetTile.getUserData() instanceof ChessPiece piece) {
            if (piece.getColor() == selectedPiece.getColor()) {
                selectedTile.getStyleClass().remove("recent");
                selectedTile = targetTile;
                selectedTile.getStyleClass().add("recent");
                hideValidMoves();
                showValidMoves(selectedTile);
            }
        }
    }

    private void showValidMoves(StackPane currentTile) {
        int currentRow = GridPane.getRowIndex(currentTile);
        int currentCol = GridPane.getColumnIndex(currentTile);

        ChessPiece selectedPiece = (ChessPiece) currentTile.getUserData();
        for (int row = Math.max(0, currentRow - 1); row <= Math.min(7, currentRow + 1); row++) {
            for (int col = Math.max(0, currentCol - 1); col <= Math.min(7, currentCol + 1); col++) {
                StackPane targetTile = getTileAt(row, col);

                if (targetTile.getUserData() != null && targetTile.getUserData() instanceof ChessPiece piece) {
                    if (piece.getColor() == selectedPiece.getColor()) {
                        continue;
                    }
                }

                Circle validMoveCircle = (Circle) targetTile.getChildren().getFirst();
                validMoveCircle.setVisible(true);
            }
        }
    }

    private void hideValidMoves() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof StackPane tile) {
                if (!tile.getChildren().isEmpty()) {
                    Node firstChild = tile.getChildren().get(0);
                    if (firstChild instanceof Circle validMoveCircle) {
                        validMoveCircle.setVisible(false);
                    }
                }
            }
        }
    }

    private void clearHighlightedTiles() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof StackPane tile) {
                tile.getStyleClass().remove("recent");
            }
        }
    }

    // Right Section
    private VBox createRightSection() {
        Label opponentName = new Label("Opponent");
        opponentCapturedPieces = new HBox();
        Label playerName = new Label("Player (You)");
        playerCapturedPieces = new HBox();
        VBox topSection = new VBox(opponentName, opponentCapturedPieces, playerName, playerCapturedPieces);
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

    // Helper
    private StackPane getTileAt(int row, int col) {
        int index = row * 8 + col;
        return (StackPane) chessBoard.getChildren().get(index);
    }

    public GridPane getChessBoard() { return chessBoard; }
    public Label getOnlineStatus() { return onlineStatus; }
    public HBox getOpponentCapturedPieces() { return opponentCapturedPieces; }
    public HBox getPlayerCapturedPieces() { return playerCapturedPieces; }
}