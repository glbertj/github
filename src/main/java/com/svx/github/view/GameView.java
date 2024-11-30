package com.svx.github.view;

import com.svx.github.model.ChessPiece;
import com.svx.github.model.ChessTile;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameView extends View<BorderPane> {
    // Left
    private GridPane chessBoard;
    private ChessTile selectedTile;

    // Right
    private Label onlineStatus;
    private final ArrayList<ChessPiece> capturedWhitePiece = new ArrayList<>();
    private final ArrayList<ChessPiece> capturedBlackPiece = new ArrayList<>();

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
        ChessTile tile = new ChessTile(row, col);
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
            ChessTile pawnTile = getTileAt(row, col);
            pawnTile.setPiece(pawn);
        }
    }

    private void addPiecesToRow(int row, ChessPiece.PieceColor color, ChessPiece.PieceType[] pieceTypes) {
        for (int col = 0; col < 8; col++) {
            ChessPiece piece = new ChessPiece(pieceTypes[col], color);
            ChessTile tile = getTileAt(row, col);
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

        ChessPiece selectedPiece = selectedTile.getPiece();
        // Selected a valid move
        if (targetTile.isValidMove()) {
            clearHighlightedTiles();
            selectedTile.setIsRecentMove(true);
            targetTile.setIsRecentMove(true);

            targetTile.setPiece(selectedPiece);
            selectedTile.setPiece(null);
            selectedTile = null;
            hideValidMoves();
        }

        // Selected current piece
        else if (selectedTile == targetTile) {
            selectedTile.setIsRecentMove(false);
            hideValidMoves();
            selectedTile = null;
        }

        // Selected opposing piece
        else if (targetTile.isEatable()) {
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

        // Selected friendly piece
        else if (targetTile.getPiece() != null && targetTile.getPiece().getColor() == selectedPiece.getColor()) {
            hideValidMoves();
            selectedTile.setIsRecentMove(false);
            selectedTile = targetTile;
            selectedTile.setIsRecentMove(true);
            showValidMoves(selectedTile);
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
        VBox topSection = new VBox(opponentName, playerName);
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

        ChessTile currentTile = getTileAt(currentRow, currentCol);  // Get ChessTile
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

        // Forward move (one square)
        ChessTile targetTile = getTileAt(currentRow + direction, currentCol);
        if (targetTile != null && targetTile.getPiece() == null) {
            moves.add((currentRow + direction) * 8 + currentCol);  // Empty space, can move
        }

        // Forward move (two squares, only on first move)
        if ((currentPiece.getColor() == ChessPiece.PieceColor.BLACK && currentRow == 1) || (currentPiece.getColor() == ChessPiece.PieceColor.WHITE && currentRow == 6)) {
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

    public GridPane getChessBoard() { return chessBoard; }
    public Label getOnlineStatus() { return onlineStatus; }
}