package com.svx.github.view;

import com.svx.github.model.game.ChessPiece;
import com.svx.github.model.game.ChessTile;
import com.svx.github.utility.SoundUtility;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameView extends View<BorderPane> {
    private ChessPiece.PieceColor playerColor;
    private boolean whiteTurn = true;
    private boolean whiteInCheck = false;
    private boolean blackInCheck = false;

    // Left
    private GridPane chessBoard;
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
                getClass().getResource("/com/svx/github/style/game.css")
        ).toExternalForm();

        playerColor = Math.random() > 0.5 ? ChessPiece.PieceColor.WHITE : ChessPiece.PieceColor.BLACK;

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
        ChessTile tile = new ChessTile(row, col);
        tile.setOnMouseClicked(e -> {
            if (playerColor.equals(ChessPiece.PieceColor.WHITE) && whiteTurn) {
                onTileClick(tile);
            }
        });
        return tile;
    }

    private void addPieces() {
        int blackBackPieces;
        int blackFrontPieces;
        int whiteBackPieces;
        int whiteFrontPieces;

        if (playerColor.equals(ChessPiece.PieceColor.WHITE)) {
            blackBackPieces = 0;
            blackFrontPieces = 1;
            whiteFrontPieces = 6;
            whiteBackPieces = 7;
        } else {
            whiteBackPieces = 0;
            whiteFrontPieces = 1;
            blackFrontPieces = 6;
            blackBackPieces = 7;
        }

        addPiecesToRow(blackBackPieces, ChessPiece.PieceColor.BLACK, new ChessPiece.PieceType[]{
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
        });
        addPawnsToRow(blackFrontPieces, ChessPiece.PieceColor.BLACK);

        addPawnsToRow(whiteFrontPieces, ChessPiece.PieceColor.WHITE);
        addPiecesToRow(whiteBackPieces, ChessPiece.PieceColor.WHITE, new ChessPiece.PieceType[]{
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
        });
    }

    private void addPawnsToRow(int row, ChessPiece.PieceColor color) {
        for (int col = 0; col < 8; col++) {
            ChessPiece pawn = new ChessPiece(ChessPiece.PieceType.PAWN, color);
            ChessTile pawnTile = getTileAt(row, col);
            if (pawnTile == null) return;
            pawnTile.setPiece(pawn);
        }
    }

    private void addPiecesToRow(int row, ChessPiece.PieceColor color, ChessPiece.PieceType[] pieceTypes) {
        for (int col = 0; col < 8; col++) {
            ChessPiece piece = new ChessPiece(pieceTypes[col], color);
            ChessTile tile = getTileAt(row, col);
            if (tile == null) return;
            tile.setPiece(piece);
        }
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
            if (targetTile.getPiece().getColor() == ChessPiece.PieceColor.WHITE) {
                capturedWhitePiece.add(targetTile.getPiece());
            } else {
                capturedBlackPiece.add(targetTile.getPiece());
            }

            targetTile.setPiece(selectedPiece);
            selectedTile.setPiece(null);
            selectedTile = null;
            hideValidMoves();
        }

        if (targetTile.getPiece() != null && targetTile.getPiece().getType() == ChessPiece.PieceType.PAWN) {
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
        if (playerColor.equals(ChessPiece.PieceColor.WHITE)) {
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
    public int[] getValidMoves(ChessPiece chessPiece, int currentRow, int currentCol) {
        List<Integer> validMoves = new ArrayList<>();

        ChessTile currentTile = getTileAt(currentRow, currentCol);
        if (currentTile == null) return new int[0];
        switch (chessPiece.getType()) {
            case PAWN:
                validMoves.addAll(getPawnMoves(currentTile, currentRow, currentCol));
                break;
            case ROOK:
                validMoves.addAll(getRookMoves(currentTile, currentRow, currentCol));
                break;
            case KNIGHT:
                validMoves.addAll(getKnightMoves(currentTile, currentRow, currentCol));
                break;
            case BISHOP:
                validMoves.addAll(getBishopMoves(currentTile, currentRow, currentCol));
                break;
            case QUEEN:
                validMoves.addAll(getQueenMoves(currentTile, currentRow, currentCol));
                break;
            case KING:
                validMoves.addAll(getKingMoves(currentTile, currentRow, currentCol));
                break;
        }

        return validMoves.stream().mapToInt(i -> i).toArray();
    }

    private List<Integer> getPawnMoves(ChessTile currentTile, int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();
        ChessPiece currentPiece = currentTile.getPiece();
        int direction = (currentPiece.getColor() == ChessPiece.PieceColor.BLACK) ? 1 : -1;  // White moves up, black moves down
        int whiteStartingRow = 6;
        int blackStartingRow = 1;

        if (!playerColor.equals(ChessPiece.PieceColor.WHITE)) {
            direction *= -1;
            whiteStartingRow = 1;
            blackStartingRow = 6;
        }

        // Forward move (one square)
        ChessTile targetTile = getTileAt(currentRow + direction, currentCol);
        if (targetTile != null && targetTile.getPiece() == null) {
            moves.add((currentRow + direction) * 8 + currentCol);  // Empty space, can move
        }

        // Forward move (two squares, only on first move)
        if ((currentPiece.getColor() == ChessPiece.PieceColor.BLACK && currentRow == blackStartingRow) || (currentPiece.getColor() == ChessPiece.PieceColor.WHITE && currentRow == whiteStartingRow)) {
            ChessTile targetTile1 = getTileAt(currentRow + direction, currentCol);
            ChessTile targetTile2 = getTileAt(currentRow + 2 * direction, currentCol);
            if (targetTile1 != null && targetTile2 != null && targetTile1.getPiece() == null && targetTile2.getPiece() == null) {
                moves.add((currentRow + 2 * direction) * 8 + currentCol);  // Both squares empty, can move two squares
            }
        }

        // Diagonal capture (left)
        if (isValidMove(currentRow + direction, currentCol - 1)) {
            targetTile = getTileAt(currentRow + direction, currentCol - 1);
            ChessPiece pieceOnTarget = targetTile != null ? targetTile.getPiece() : null;
            if (pieceOnTarget != null && pieceOnTarget.getColor() != currentPiece.getColor()) {
                moves.add((currentRow + direction) * 8 + (currentCol - 1));
            }
        }

        // Diagonal capture (right)
        if (isValidMove(currentRow + direction, currentCol + 1)) {
            targetTile = getTileAt(currentRow + direction, currentCol + 1);
            ChessPiece pieceOnTarget = targetTile != null ? targetTile.getPiece() : null;
            if (pieceOnTarget != null && pieceOnTarget.getColor() != currentPiece.getColor()) {
                moves.add((currentRow + direction) * 8 + (currentCol + 1));  // Capture opponent's piece
            }
        }

        return moves;
    }

    private List<Integer> getRookMoves(ChessTile currentTile, int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();

        // Horizontal moves (left and right)
        for (int i = 1; i <= 7; i++) {
            ChessTile targetTile = getTileAt(currentRow, currentCol + i);
            if (targetTile != null) {
                ChessPiece pieceOnTarget = targetTile.getPiece();
                if (pieceOnTarget == null) {
                    moves.add(currentRow * 8 + (currentCol + i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != currentTile.getPiece().getColor()) {
                    moves.add(currentRow * 8 + (currentCol + i));  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        // Horizontal moves (left and right, the other direction)
        for (int i = 1; i <= 7; i++) {
            ChessTile targetTile = getTileAt(currentRow, currentCol - i);
            if (targetTile != null) {
                ChessPiece pieceOnTarget = targetTile.getPiece();
                if (pieceOnTarget == null) {
                    moves.add(currentRow * 8 + (currentCol - i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != currentTile.getPiece().getColor()) {
                    moves.add(currentRow * 8 + (currentCol - i));  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        // Vertical moves (up and down)
        for (int i = 1; i <= 7; i++) {
            ChessTile targetTile = getTileAt(currentRow + i, currentCol);
            if (targetTile != null) {
                ChessPiece pieceOnTarget = targetTile.getPiece();
                if (pieceOnTarget == null) {
                    moves.add((currentRow + i) * 8 + currentCol);  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != currentTile.getPiece().getColor()) {
                    moves.add((currentRow + i) * 8 + currentCol);  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        // Vertical moves (down and up, the other direction)
        for (int i = 1; i <= 7; i++) {
            ChessTile targetTile = getTileAt(currentRow - i, currentCol);
            if (targetTile != null) {
                ChessPiece pieceOnTarget = targetTile.getPiece();
                if (pieceOnTarget == null) {
                    moves.add((currentRow - i) * 8 + currentCol);  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != currentTile.getPiece().getColor()) {
                    moves.add((currentRow - i) * 8 + currentCol);  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        return moves;
    }

    private List<Integer> getKnightMoves(ChessTile currentTile, int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();
        int[] rowOffsets = {-2, -1, 1, 2, 2, 1, -1, -2};
        int[] colOffsets = {1, 2, 2, 1, -1, -2, -2, -1};

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isValidMove(newRow, newCol)) {
                ChessTile targetTile = getTileAt(newRow, newCol);
                if (targetTile != null) {
                    ChessPiece pieceOnTarget = targetTile.getPiece();
                    if (pieceOnTarget == null || pieceOnTarget.getColor() != currentTile.getPiece().getColor()) {
                        moves.add(newRow * 8 + newCol);
                    }
                }
            }
        }

        return moves;
    }

    private List<Integer> getBishopMoves(ChessTile currentTile, int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();
        ChessPiece currentPiece = currentTile.getPiece();

        // Diagonal moves (top-left)
        for (int i = 1; i <= 7; i++) {
            ChessTile targetTile = getTileAt(currentRow + i, currentCol - i);
            if (targetTile != null) {
                ChessPiece pieceOnTarget = targetTile.getPiece();
                if (pieceOnTarget == null) {
                    moves.add((currentRow + i) * 8 + (currentCol - i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != currentPiece.getColor()) {
                    moves.add((currentRow + i) * 8 + (currentCol - i));  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        // Diagonal moves (top-right)
        for (int i = 1; i <= 7; i++) {
            ChessTile targetTile = getTileAt(currentRow + i, currentCol + i);
            if (targetTile != null) {
                ChessPiece pieceOnTarget = targetTile.getPiece();
                if (pieceOnTarget == null) {
                    moves.add((currentRow + i) * 8 + (currentCol + i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != currentPiece.getColor()) {
                    moves.add((currentRow + i) * 8 + (currentCol + i));  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        // Diagonal moves (bottom-left)
        for (int i = 1; i <= 7; i++) {
            ChessTile targetTile = getTileAt(currentRow - i, currentCol - i);
            if (targetTile != null) {
                ChessPiece pieceOnTarget = targetTile.getPiece();
                if (pieceOnTarget == null) {
                    moves.add((currentRow - i) * 8 + (currentCol - i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != currentPiece.getColor()) {
                    moves.add((currentRow - i) * 8 + (currentCol - i));  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        // Diagonal moves (bottom-right)
        for (int i = 1; i <= 7; i++) {
            ChessTile targetTile = getTileAt(currentRow - i, currentCol + i);
            if (targetTile != null) {
                ChessPiece pieceOnTarget = targetTile.getPiece();
                if (pieceOnTarget == null) {
                    moves.add((currentRow - i) * 8 + (currentCol + i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != currentPiece.getColor()) {
                    moves.add((currentRow - i) * 8 + (currentCol + i));  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        return moves;
    }

    private List<Integer> getQueenMoves(ChessTile currentTile, int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();
        moves.addAll(getRookMoves(currentTile, currentRow, currentCol));
        moves.addAll(getBishopMoves(currentTile, currentRow, currentCol));

        return moves;
    }

    private List<Integer> getKingMoves(ChessTile currentTile, int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();
        ChessPiece currentPiece = currentTile.getPiece();

        int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isValidMove(newRow, newCol)) {
                ChessTile targetTile = getTileAt(newRow, newCol);
                if (targetTile != null) {
                    ChessPiece pieceOnTarget = targetTile.getPiece();
                    if (pieceOnTarget == null || pieceOnTarget.getColor() != currentPiece.getColor()) {
                        moves.add(newRow * 8 + newCol);
                    }
                }
            }
        }

        return moves;
    }

    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private void handlePawnPromotion(ChessTile currentTile) {
        int blackPromotionRow = 7;
        int whitePromotionRow = 0;

        if (!playerColor.equals(ChessPiece.PieceColor.WHITE)) {
            blackPromotionRow = 0;
            whitePromotionRow = 7;
        }

        int currentRow = GridPane.getRowIndex(currentTile);
        ChessPiece currentPiece = currentTile.getPiece();

        if (currentPiece.getColor() == ChessPiece.PieceColor.WHITE && currentRow == whitePromotionRow) {
            promotePawn(currentTile, ChessPiece.PieceColor.WHITE);
        } else if (currentPiece.getColor() == ChessPiece.PieceColor.BLACK && currentRow == blackPromotionRow) {
            promotePawn(currentTile, ChessPiece.PieceColor.BLACK);
        }
    }

    private void promotePawn(ChessTile currentTile, ChessPiece.PieceColor color) {
//        String promotionChoice = promptForPromotionChoice(color);
        String promotionChoice = "queen";

        ChessPiece promotedPiece = switch (promotionChoice.toLowerCase()) {
            case "rook" -> new ChessPiece(ChessPiece.PieceType.ROOK, color);
            case "bishop" -> new ChessPiece(ChessPiece.PieceType.BISHOP, color);
            case "knight" -> new ChessPiece(ChessPiece.PieceType.KNIGHT, color);
            default -> new ChessPiece(ChessPiece.PieceType.QUEEN, color);
        };

        currentTile.setPiece(promotedPiece);
    }

    public boolean isPlayerInCheck(ChessPiece.PieceColor playerColor) {
        ChessTile kingTile = findKing(playerColor);



        return false;
    }

    // Helper
    private ChessTile getTileAt(int row, int col) {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof ChessTile tile) {
                int nodeRow = GridPane.getRowIndex(tile);
                int nodeCol = GridPane.getColumnIndex(tile);
                if (nodeRow == row && nodeCol == col) {
                    return tile;
                }
            }
        }
        return null;
    }

    private ChessTile findKing(ChessPiece.PieceColor color) {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof ChessTile tile && tile.getPiece() != null && tile.getPiece() instanceof ChessPiece piece) {
                if (piece.getType().equals(ChessPiece.PieceType.KING) && piece.getColor().equals(color)) {
                    return tile;
                }
            }
        }
        return null;
    }
}