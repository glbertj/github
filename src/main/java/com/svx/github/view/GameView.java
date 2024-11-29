package com.svx.github.view;

import com.svx.github.model.ChessPiece;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameView extends View<BorderPane> {
    // Left
    private GridPane chessBoard;
    private StackPane selectedTile;

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
        if (!targetTile.getChildren().isEmpty() && (
                targetTile.getChildren().getFirst() instanceof Circle validMoveCircle && validMoveCircle.isVisible()
                )) {
            clearHighlightedTiles();
            selectedTile.getStyleClass().add("recent");
            targetTile.getStyleClass().add("recent"); // TODO! FIX LATER - Try moving the same piece thrice or move twice + one other piece

            targetTile.getChildren().add(selectedPiece.getImageView());
            targetTile.setUserData(selectedPiece);
            selectedTile.setUserData(null);
            selectedTile = null;
            hideValidMoves();

//            if (true) {
//                // Friendly Piece
//                if (piece.getColor() == selectedPiece.getColor()) {
//                    selectedTile.getStyleClass().remove("recent");
//                    selectedTile = targetTile;
//                    selectedTile.getStyleClass().add("recent");
//                    hideValidMoves();
//                    showValidMoves(selectedTile);
//                }
//
//                // Opposing Piece
//                else {
//                    if (piece.getColor().equals(ChessPiece.PieceColor.BLACK)) {
//                        capturedBlackPiece.add(piece);
//                    } else {
//                        capturedWhitePiece.add(piece);
//                    }
//
//                    targetTile.setUserData(selectedPiece);
//
//                }
//            }
        }

        // Selected current piece
        else if (selectedTile == targetTile) {
            selectedTile.getStyleClass().remove("recent");
            hideValidMoves();
            selectedTile = null;
        }
    }

    private void showValidMoves(StackPane currentTile) {
        int currentRow = GridPane.getRowIndex(currentTile);
        int currentCol = GridPane.getColumnIndex(currentTile);

        ChessPiece selectedPiece = (ChessPiece) currentTile.getUserData();
        int[] validMoves = getValidMoves(selectedPiece, currentRow, currentCol);

        // Loop through the valid moves and show the valid move circles
        for (int move : validMoves) {
            int targetRow = move / 8; // Get the row from the valid move (assuming 8x8 board)
            int targetCol = move % 8; // Get the column from the valid move

            // Get the target tile based on row and column
            StackPane targetTile = getTileAt(targetRow, targetCol);

            // Check if there's a piece already on the target tile
            if (targetTile.getUserData() != null && targetTile.getUserData() instanceof ChessPiece piece) {
                if (piece.getColor() == selectedPiece.getColor()) {
                    continue; // Skip if the piece on the tile is the same color
                }
            }

            // Add the valid move circle (or make it visible if already added)
            Circle validMoveCircle = (Circle) targetTile.getChildren().getFirst(); // Assumes the circle is the first child
            validMoveCircle.setVisible(true); // Show the valid move circle
        }
    }

    private void hideValidMoves() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof StackPane tile) {
                if (!tile.getChildren().isEmpty()) {
                    Node firstChild = tile.getChildren().getFirst();
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

    // TODO! PLS MOVE THIS SOMEWHERE ELSE LATER BUT I JUST WANNA FINISH THIS STUPID SHIT
    public int[] getValidMoves(ChessPiece chessPiece, int currentRow, int currentCol) {
        List<Integer> validMoves = new ArrayList<>();

        switch (chessPiece.getType()) {
            case PAWN:
                validMoves.addAll(getPawnMoves(currentRow, currentCol));
                break;
            case ROOK:
                validMoves.addAll(getRookMoves(currentRow, currentCol));
                break;
            case KNIGHT:
                validMoves.addAll(getKnightMoves(currentRow, currentCol));
                break;
            case BISHOP:
                validMoves.addAll(getBishopMoves(currentRow, currentCol));
                break;
            case QUEEN:
                validMoves.addAll(getQueenMoves(currentRow, currentCol));
                break;
            case KING:
                validMoves.addAll(getKingMoves(currentRow, currentCol));
                break;
        }

        return validMoves.stream().mapToInt(i -> i).toArray();
    }

    private List<Integer> getPawnMoves(int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();
        ChessPiece currentPiece = (ChessPiece) getTileAt(currentRow, currentCol).getUserData();
        int direction = (currentPiece.getColor() == ChessPiece.PieceColor.BLACK) ? 1 : -1;  // White moves up, black moves down

        // Forward move (one square)
        if (isValidMove(currentRow + direction, currentCol)) {
            StackPane targetTile = getTileAt(currentRow + direction, currentCol);
            ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();
            if (pieceOnTarget == null) {
                moves.add((currentRow + direction) * 8 + currentCol);  // Empty space, can move
            }
        }

        // Forward move (two squares, only on first move)
        if ((currentPiece.getColor() == ChessPiece.PieceColor.BLACK && currentRow == 1) || (currentPiece.getColor() == ChessPiece.PieceColor.WHITE && currentRow == 6)) {
            if (isValidMove(currentRow + 2 * direction, currentCol)) {
                StackPane targetTile1 = getTileAt(currentRow + direction, currentCol);
                StackPane targetTile2 = getTileAt(currentRow + 2 * direction, currentCol);

                ChessPiece pieceOnTarget1 = (ChessPiece) targetTile1.getUserData();
                ChessPiece pieceOnTarget2 = (ChessPiece) targetTile2.getUserData();

                if (pieceOnTarget1 == null && pieceOnTarget2 == null) {
                    moves.add((currentRow + 2 * direction) * 8 + currentCol);  // Both squares empty, can move two squares
                }
            }
        }

        // Diagonal capture (left)
        if (isValidMove(currentRow + direction, currentCol - 1)) {
            StackPane targetTile = getTileAt(currentRow + direction, currentCol - 1);
            ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();
            if (pieceOnTarget != null && pieceOnTarget.getColor() != currentPiece.getColor()) {
                moves.add((currentRow + direction) * 8 + (currentCol - 1));  // Capture opponent's piece
            }
        }

        // Diagonal capture (right)
        if (isValidMove(currentRow + direction, currentCol + 1)) {
            StackPane targetTile = getTileAt(currentRow + direction, currentCol + 1);
            ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();
            if (pieceOnTarget != null && pieceOnTarget.getColor() != currentPiece.getColor()) {
                moves.add((currentRow + direction) * 8 + (currentCol + 1));  // Capture opponent's piece
            }
        }

        return moves;
    }

    private List<Integer> getRookMoves(int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();

        // Horizontal moves (left and right)
        for (int i = 1; i <= 7; i++) {
            if (isValidMove(currentRow, currentCol + i)) {
                StackPane targetTile = getTileAt(currentRow, currentCol + i);
                ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();

                if (pieceOnTarget == null) {
                    moves.add(currentRow * 8 + (currentCol + i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != ((ChessPiece) getTileAt(currentRow, currentCol).getUserData()).getColor()) {
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
            if (isValidMove(currentRow, currentCol - i)) {
                StackPane targetTile = getTileAt(currentRow, currentCol - i);
                ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();

                if (pieceOnTarget == null) {
                    moves.add(currentRow * 8 + (currentCol - i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != ((ChessPiece) getTileAt(currentRow, currentCol).getUserData()).getColor()) {
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
            if (isValidMove(currentRow + i, currentCol)) {
                StackPane targetTile = getTileAt(currentRow + i, currentCol);
                ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();

                if (pieceOnTarget == null) {
                    moves.add((currentRow + i) * 8 + currentCol);  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != ((ChessPiece) getTileAt(currentRow, currentCol).getUserData()).getColor()) {
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
            if (isValidMove(currentRow - i, currentCol)) {
                StackPane targetTile = getTileAt(currentRow - i, currentCol);
                ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();

                if (pieceOnTarget == null) {
                    moves.add((currentRow - i) * 8 + currentCol);  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != ((ChessPiece) getTileAt(currentRow, currentCol).getUserData()).getColor()) {
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

    private List<Integer> getKnightMoves(int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();
        int[] rowOffsets = {-2, -1, 1, 2, 2, 1, -1, -2};
        int[] colOffsets = {1, 2, 2, 1, -1, -2, -2, -1};

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isValidMove(newRow, newCol)) {
                moves.add(newRow * 8 + newCol);
            }
        }

        return moves;
    }

    private List<Integer> getBishopMoves(int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();

        // Diagonal moves (top-left to bottom-right)
        for (int i = 1; i <= 7; i++) {
            if (isValidMove(currentRow + i, currentCol + i)) {
                StackPane targetTile = getTileAt(currentRow + i, currentCol + i);
                ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();

                if (pieceOnTarget == null) {
                    moves.add((currentRow + i) * 8 + (currentCol + i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != ((ChessPiece) getTileAt(currentRow, currentCol).getUserData()).getColor()) {
                    moves.add((currentRow + i) * 8 + (currentCol + i));  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        // Diagonal moves (top-right to bottom-left)
        for (int i = 1; i <= 7; i++) {
            if (isValidMove(currentRow + i, currentCol - i)) {
                StackPane targetTile = getTileAt(currentRow + i, currentCol - i);
                ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();

                if (pieceOnTarget == null) {
                    moves.add((currentRow + i) * 8 + (currentCol - i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != ((ChessPiece) getTileAt(currentRow, currentCol).getUserData()).getColor()) {
                    moves.add((currentRow + i) * 8 + (currentCol - i));  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        // Diagonal moves (bottom-left to top-right)
        for (int i = 1; i <= 7; i++) {
            if (isValidMove(currentRow - i, currentCol + i)) {
                StackPane targetTile = getTileAt(currentRow - i, currentCol + i);
                ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();

                if (pieceOnTarget == null) {
                    moves.add((currentRow - i) * 8 + (currentCol + i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != ((ChessPiece) getTileAt(currentRow, currentCol).getUserData()).getColor()) {
                    moves.add((currentRow - i) * 8 + (currentCol + i));  // Opponent's piece, can capture
                    break;  // Stop after capturing (can't jump over it)
                } else {
                    break;  // Same color, stop (can't move past own piece)
                }
            } else {
                break;  // Edge of the board, stop
            }
        }

        // Diagonal moves (bottom-right to top-left)
        for (int i = 1; i <= 7; i++) {
            if (isValidMove(currentRow - i, currentCol - i)) {
                StackPane targetTile = getTileAt(currentRow - i, currentCol - i);
                ChessPiece pieceOnTarget = (ChessPiece) targetTile.getUserData();

                if (pieceOnTarget == null) {
                    moves.add((currentRow - i) * 8 + (currentCol - i));  // No piece blocking, add move
                } else if (pieceOnTarget.getColor() != ((ChessPiece) getTileAt(currentRow, currentCol).getUserData()).getColor()) {
                    moves.add((currentRow - i) * 8 + (currentCol - i));  // Opponent's piece, can capture
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

    private List<Integer> getQueenMoves(int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();
        moves.addAll(getRookMoves(currentRow, currentCol));  // Add rook-like moves
        moves.addAll(getBishopMoves(currentRow, currentCol));  // Add bishop-like moves
        return moves;
    }

    private List<Integer> getKingMoves(int currentRow, int currentCol) {
        List<Integer> moves = new ArrayList<>();
        int[] rowOffsets = {-1, 0, 1, 1, 1, 0, -1, -1};
        int[] colOffsets = {-1, -1, -1, 0, 1, 1, 1, 0};

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isValidMove(newRow, newCol)) {
                moves.add(newRow * 8 + newCol);
            }
        }

        return moves;
    }

    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    // Helper
    private StackPane getTileAt(int row, int col) {
        int index = row * 8 + col;
        return (StackPane) chessBoard.getChildren().get(index);
    }

    public GridPane getChessBoard() { return chessBoard; }
    public Label getOnlineStatus() { return onlineStatus; }
}