package com.svx.chess.model;

import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

public class Chess {
    private static Chess.PieceColor playerColor;

    public enum PieceType {
        PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
    }

    public enum PieceColor {
        WHITE, BLACK
    }

    public static void handlePawnPromotion(ChessTile currentTile, Chess.PieceColor color) {
        int blackPromotionRow = 7;
        int whitePromotionRow = 0;

        if (!color.equals(Chess.PieceColor.WHITE)) {
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

    private static void promotePawn(ChessTile currentTile, Chess.PieceColor color) {
//        PieceType promotionChoice = promptForPromotionChoice(color);
        PieceType promotionChoice = PieceType.QUEEN;

        ChessPiece promotedPiece = switch (promotionChoice) {
            case ROOK -> new ChessPiece(Chess.PieceType.ROOK, color);
            case BISHOP -> new ChessPiece(Chess.PieceType.BISHOP, color);
            case KNIGHT -> new ChessPiece(Chess.PieceType.KNIGHT, color);
            default -> new ChessPiece(Chess.PieceType.QUEEN, color);
        };

        currentTile.setPiece(promotedPiece);
    }

    public static int[] getValidMoves(ChessTile currentTile, ChessTile[][] tiles) {
        List<Integer> validMoves = new ArrayList<>();

        if (currentTile == null) return new int[0];
        ChessPiece piece = currentTile.getPiece();

        switch (piece.getType()) {
            case PAWN:
                validMoves.addAll(getPawnMoves(tiles, currentTile));
                break;
            case ROOK:
                validMoves.addAll(getRookMoves(tiles, currentTile));
                break;
            case KNIGHT:
                validMoves.addAll(getKnightMoves(tiles, currentTile));
                break;
            case BISHOP:
                validMoves.addAll(getBishopMoves(tiles, currentTile));
                break;
            case QUEEN:
                validMoves.addAll(getQueenMoves(tiles, currentTile));
                break;
            case KING:
                validMoves.addAll(getKingMoves(tiles, currentTile));
                break;
            default:
                break;
        }

        return validMoves.stream().mapToInt(i -> i).toArray();
    }

    private static List<Integer> getPawnMoves(ChessTile[][] tiles, ChessTile currentTile) {
        List<Integer> moves = new ArrayList<>();
        ChessPiece currentPiece = currentTile.getPiece();

        int direction = (currentPiece.getColor() == Chess.PieceColor.BLACK) ? 1 : -1;
        int startingRow = (currentPiece.getColor() == Chess.PieceColor.BLACK) ? 1 : 6;

        if (!playerColor.equals(Chess.PieceColor.WHITE)) {
            direction *= -1;
            startingRow = (currentPiece.getColor() == Chess.PieceColor.BLACK) ? 6 : 1;
        }

        int currentRow = GridPane.getRowIndex(currentTile);
        int currentCol = GridPane.getColumnIndex(currentTile);

        ChessTile targetTile = tiles[currentRow + direction][currentCol];
        if (targetTile != null && targetTile.getPiece() == null) {
            moves.add((currentRow + direction) * 8 + currentCol);
        }

        if (currentRow == startingRow) {
            ChessTile targetTile1 = tiles[currentRow + direction][currentCol];
            ChessTile targetTile2 = tiles[currentRow + 2 * direction][currentCol];
            if (targetTile1 != null && targetTile2 != null
                    && targetTile1.getPiece() == null && targetTile2.getPiece() == null) {
                moves.add((currentRow + 2 * direction) * 8 + currentCol);
            }
        }

        checkDiagonalCaptureMoves(tiles, moves, currentRow, currentCol, direction, currentPiece);

        return moves;
    }

    private static void checkDiagonalCaptureMoves(ChessTile[][] tiles, List<Integer> moves, int currentRow, int currentCol, int direction, ChessPiece currentPiece) {
        for (int offset : new int[]{-1, 1}) {
            int newRow = currentRow + direction;
            int newCol = currentCol + offset;

            if (isValidMove(newRow, newCol)) {
                ChessTile targetTile = tiles[newRow][newCol];
                ChessPiece pieceOnTarget = targetTile != null ? targetTile.getPiece() : null;
                if (pieceOnTarget != null && pieceOnTarget.getColor() != currentPiece.getColor()) {
                    moves.add(newRow * 8 + newCol);
                }
            }
        }
    }

    private static List<Integer> getRookMoves(ChessTile[][] tiles, ChessTile currentTile) {
        List<Integer> moves = new ArrayList<>();
        int currentRow = GridPane.getRowIndex(currentTile);
        int currentCol = GridPane.getColumnIndex(currentTile);

        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        for (int[] direction : directions) {
            int rowStep = direction[0];
            int colStep = direction[1];

            for (int i = 1; i <= 7; i++) {
                int newRow = currentRow + i * rowStep;
                int newCol = currentCol + i * colStep;

                if (newRow < 0 || newRow >= tiles.length || newCol < 0 || newCol >= tiles[0].length) {
                    break;
                }

                ChessTile targetTile = tiles[newRow][newCol];
                if (targetTile != null) {
                    ChessPiece pieceOnTarget = targetTile.getPiece();

                    if (pieceOnTarget == null) {
                        moves.add(newRow * 8 + newCol);
                    }
                    else if (pieceOnTarget.getColor() != currentTile.getPiece().getColor()) {
                        moves.add(newRow * 8 + newCol);
                        break;
                    }
                    else {
                        break;
                    }
                }
            }
        }

        return moves;
    }

    private static List<Integer> getKnightMoves(ChessTile[][] tiles, ChessTile currentTile) {
        List<Integer> moves = new ArrayList<>();
        int[] rowOffsets = {-2, -1, 1, 2, 2, 1, -1, -2};
        int[] colOffsets = {1, 2, 2, 1, -1, -2, -2, -1};

        int currentRow = GridPane.getRowIndex(currentTile);
        int currentCol = GridPane.getColumnIndex(currentTile);

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isValidMove(newRow, newCol)) {
                ChessTile targetTile = tiles[newRow][newCol];
                ChessPiece pieceOnTarget = targetTile != null ? targetTile.getPiece() : null;

                if (pieceOnTarget == null || pieceOnTarget.getColor() != currentTile.getPiece().getColor()) {
                    moves.add(newRow * 8 + newCol);
                }
            }
        }

        return moves;
    }

    private static List<Integer> getBishopMoves(ChessTile[][] tiles, ChessTile currentTile) {
        List<Integer> moves = new ArrayList<>();
        ChessPiece currentPiece = currentTile.getPiece();

        int currentRow = GridPane.getRowIndex(currentTile);
        int currentCol = GridPane.getColumnIndex(currentTile);

        int[] rowOffsets = {-1, 1, 1, -1};
        int[] colOffsets = {-1, 1, -1, 1};

        for (int direction = 0; direction < 4; direction++) {
            int rowOffset = rowOffsets[direction];
            int colOffset = colOffsets[direction];

            for (int i = 1; i <= 7; i++) {
                int newRow = currentRow + i * rowOffset;
                int newCol = currentCol + i * colOffset;

                if (isValidMove(newRow, newCol)) {
                    ChessTile targetTile = tiles[newRow][newCol];
                    ChessPiece pieceOnTarget = targetTile != null ? targetTile.getPiece() : null;

                    if (pieceOnTarget == null) {
                        moves.add(newRow * 8 + newCol);
                    } else if (pieceOnTarget.getColor() != currentPiece.getColor()) {
                        moves.add(newRow * 8 + newCol);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return moves;
    }

    private static List<Integer> getQueenMoves(ChessTile[][] tiles, ChessTile currentTile) {
        List<Integer> moves = new ArrayList<>();
        moves.addAll(getRookMoves(tiles, currentTile));
        moves.addAll(getBishopMoves(tiles, currentTile));

        return moves;
    }

    private static List<Integer> getKingMoves(ChessTile[][] tiles, ChessTile currentTile) {
        List<Integer> moves = new ArrayList<>();
        ChessPiece currentPiece = currentTile.getPiece();

        int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};

        int currentRow = GridPane.getRowIndex(currentTile);
        int currentCol = GridPane.getColumnIndex(currentTile);

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isValidMove(newRow, newCol)) {
                ChessTile targetTile = tiles[newRow][newCol];
                ChessPiece pieceOnTarget = targetTile != null ? targetTile.getPiece() : null;

                if (pieceOnTarget == null || pieceOnTarget.getColor() != currentPiece.getColor()) {
                    moves.add(newRow * 8 + newCol);
                }
            }
        }

        return moves;
    }

    private static boolean isValidMove(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public static void setPlayerColor(Chess.PieceColor color) { playerColor = color; }
}