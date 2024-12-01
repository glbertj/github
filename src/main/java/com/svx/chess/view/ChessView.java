package com.svx.chess.view;

import com.svx.chess.model.Chess;
import com.svx.chess.model.ChessBoard;
import com.svx.chess.model.ChessPiece;
import com.svx.chess.model.ChessTile;
import com.svx.chess.utility.SoundUtility;
import com.svx.github.view.View;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.Objects;

public class ChessView extends View<BorderPane> {
    private Chess.PieceColor playerColor;
    private boolean whiteTurn = true;
    private boolean whiteInCheck = false;
    private boolean blackInCheck = false;

    // Left
    private ChessBoard chessBoard;
    private ChessTile selectedTile;

    // Right
    private Label onlineStatus;
    private final ObservableList<ChessPiece> capturedWhitePiece = FXCollections.observableArrayList();
    private HBox capturedWhiteBox;
    private final ObservableList<ChessPiece> capturedBlackPiece = FXCollections.observableArrayList();
    private HBox capturedBlackBox;

    @Override
    public void initializeView() {
        SoundUtility.SoundType.START.play();
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

    private void onTileClick(ChessTile targetTile) {
        // No piece selected
        if (selectedTile == null) {
            if (targetTile.getPiece() == null) {
                return;
            }

            selectedTile = targetTile;
            selectedTile.setIsRecentMove(true);

            showValidMoves(selectedTile);
            return;
        }

        // Selected current piece
        if (selectedTile == targetTile) {
            selectedTile.setIsRecentMove(false);
            hideValidMoves();
            selectedTile = null;
            return;
        }

        ChessPiece selectedPiece = selectedTile.getPiece();
        // Selected friendly piece
        if (targetTile.getPiece() != null && targetTile.getPiece().getColor() == selectedPiece.getColor()) {
            hideValidMoves();
            selectedTile.setIsRecentMove(false);
            selectedTile = targetTile;
            selectedTile.setIsRecentMove(true);
            showValidMoves(selectedTile);
            return;
        }

        whiteTurn = !whiteTurn;
        // Selected a valid move
        if (targetTile.isValidMove()) {
            SoundUtility.SoundType.MOVE.play();
            clearHighlightedTiles();
            selectedTile.setIsRecentMove(true);
            targetTile.setIsRecentMove(true);

            targetTile.setPiece(selectedPiece);
            selectedTile.setPiece(null);
            selectedTile = null;
            hideValidMoves();
        }

        // Ate an enemy piece
        else if (targetTile.isEatable()) {
            SoundUtility.SoundType.CAPTURE.play();
            if (targetTile.getPiece().getColor() == Chess.PieceColor.WHITE) {
                capturedWhitePiece.add(targetTile.getPiece());
            } else {
                capturedBlackPiece.add(targetTile.getPiece());
            }

            targetTile.setPiece(selectedPiece);
            selectedTile.setPiece(null);
            selectedTile = null;
            hideValidMoves();
        }

        if (targetTile.getPiece() != null && targetTile.getPiece().getType() == Chess.PieceType.PAWN) {
            handlePawnPromotion(targetTile);
        }
    }

    private void showValidMoves(ChessTile currentTile) {
        int currentRow = GridPane.getRowIndex(currentTile);
        int currentCol = GridPane.getColumnIndex(currentTile);

        ChessPiece selectedPiece = currentTile.getPiece();
        int[] validMoves = getValidMoves(selectedPiece, currentRow, currentCol);

        for (int move : validMoves) {
            int targetRow = move / 8;
            int targetCol = move % 8;

            ChessTile targetTile = getTileAt(targetRow, targetCol);
            if (targetTile == null) return;
            targetTile.setIsValidMove(true);

            if (targetTile.getPiece() != null) {
                if (!(targetTile.getPiece().getColor() == selectedPiece.getColor())) {
                    targetTile.setIsEatable(true);
                    targetTile.setIsValidMove(false);
                }
            }
        }
    }

    private void hideValidMoves() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof ChessTile tile) {
                tile.setIsValidMove(false);
                tile.setIsEatable(false);
            }
        }
    }

    private void clearHighlightedTiles() {
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

        capturedWhiteBox = new HBox();
        capturedWhiteBox.getStyleClass().add("captured-box");

        capturedBlackBox = new HBox();
        capturedBlackBox.getStyleClass().add("captured-box");

        capturedBlackPiece.addListener((ListChangeListener<ChessPiece>) c -> {
            capturedBlackBox.getChildren().clear();
            for (ChessPiece piece : capturedBlackPiece) {
                capturedBlackBox.getChildren().add(piece.getImageView());
            }
        });

        capturedWhitePiece.addListener((ListChangeListener<ChessPiece>) c -> {
            capturedWhiteBox.getChildren().clear();
            for (ChessPiece piece : capturedWhitePiece) {
                capturedWhiteBox.getChildren().add(piece.getImageView());
            }
        });

        VBox topSection;
        if (playerColor.equals(Chess.PieceColor.WHITE)) {
            topSection = new VBox(opponentName, capturedBlackBox, playerName, capturedWhiteBox);
        } else {
            topSection = new VBox(opponentName, capturedWhiteBox, playerName, capturedBlackBox);
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

    // TODO! PLS MOVE THIS SOMEWHERE ELSE LATER
    private void handlePawnPromotion(ChessTile currentTile) {
        int blackPromotionRow = 7;
        int whitePromotionRow = 0;

        if (!playerColor.equals(Chess.PieceColor.WHITE)) {
            blackPromotionRow = 0;
            whitePromotionRow = 7;
        }

        int currentRow = GridPane.getRowIndex(currentTile);
        ChessPiece currentPiece = currentTile.getPiece();

        if (currentPiece.getColor() == Chess.PieceColor.WHITE && currentRow == whitePromotionRow) {
            promotePawn(currentTile, Chess.PieceColor.WHITE);
        } else if (currentPiece.getColor() == Chess.PieceColor.BLACK && currentRow == blackPromotionRow) {
            promotePawn(currentTile, Chess.PieceColor.BLACK);
        }
    }

    private void promotePawn(ChessTile currentTile, Chess.PieceColor color) {
//        String promotionChoice = promptForPromotionChoice(color);
        String promotionChoice = "queen";

        ChessPiece promotedPiece = switch (promotionChoice.toLowerCase()) {
            case "rook" -> new ChessPiece(Chess.PieceType.ROOK, color);
            case "bishop" -> new ChessPiece(Chess.PieceType.BISHOP, color);
            case "knight" -> new ChessPiece(Chess.PieceType.KNIGHT, color);
            default -> new ChessPiece(Chess.PieceType.QUEEN, color);
        };

        currentTile.setPiece(promotedPiece);
    }
}