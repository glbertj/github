package com.svx.chess.model;

import com.svx.chess.controller.dialog.PromotePawnDialogController;
import com.svx.github.controller.AppController;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

public class Chess {
    private static Chess.PieceColor playerColor;
    private static Chess.PieceType promotionChoice;

    public enum PieceType {
        PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
    }

    public enum PieceColor {
        WHITE, BLACK
    }

    public static void handlePawnPromotion(ChessTile currentTile, Chess.PieceColor color, AppController appController) {
        int blackPromotionRow = 7;
        int whitePromotionRow = 0;

        if (!color.equals(Chess.PieceColor.WHITE)) {
            blackPromotionRow = 0;
            whitePromotionRow = 7;
        }

        int currentRow = GridPane.getRowIndex(currentTile);
        ChessPiece currentPiece = currentTile.getPiece();

        if (currentPiece.getColor() == Chess.PieceColor.WHITE && currentRow == whitePromotionRow) {
            promotePawn(currentTile, Chess.PieceColor.WHITE, appController);
        } else if (currentPiece.getColor() == Chess.PieceColor.BLACK && currentRow == blackPromotionRow) {
            promotePawn(currentTile, Chess.PieceColor.BLACK, appController);
        }
    }

    private static void promotePawn(ChessTile currentTile, Chess.PieceColor color, AppController appController) {
        appController.openDialog(new PromotePawnDialogController(appController, color));

        ChessPiece promotedPiece;
        switch (promotionChoice) {
            case ROOK:
                promotedPiece = new ChessPiece(Chess.PieceType.ROOK, color);
                break;
            case BISHOP:
                promotedPiece = new ChessPiece(Chess.PieceType.BISHOP, color);
                break;
            case KNIGHT:
                promotedPiece = new ChessPiece(Chess.PieceType.KNIGHT, color);
                break;
            default:
                promotedPiece = new ChessPiece(Chess.PieceType.QUEEN, color);
                break;
        }

        currentTile.setPiece(promotedPiece);
        promotionChoice = null;
    }

    public static int[] getValidMoves(ChessTile currentTile, ChessTile[][] tiles, ChessTile kingTile) {
        List<Integer> validMoves = new ArrayList<>();
        ChessPiece piece = currentTile.getPiece();

        switch (piece.getType()) {
            case PAWN:
                validMoves.addAll(getPawnMoves(tiles, currentTile, kingTile));
                break;
            case ROOK:
                validMoves.addAll(getRookMoves(tiles, currentTile, kingTile));
                break;
            case KNIGHT:
                validMoves.addAll(getKnightMoves(tiles, currentTile, kingTile));
                break;
            case BISHOP:
                validMoves.addAll(getBishopMoves(tiles, currentTile, kingTile));
                break;
            case QUEEN:
                validMoves.addAll(getQueenMoves(tiles, currentTile, kingTile));
                break;
            case KING:
                validMoves.addAll(getKingMoves(tiles, currentTile, kingTile));
                break;
            default:
                break;
        }

        return validMoves.stream().mapToInt(i -> i).toArray();
    }

    private static List<Integer> getPawnMoves(ChessTile[][] tiles, ChessTile currentTile, ChessTile kingTile) {
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
            if (wouldNotPutKingInCheck(tiles, currentTile, targetTile, kingTile)) {
                moves.add((currentRow + direction) * 8 + currentCol);
            }
        }

        if (currentRow == startingRow) {
            ChessTile targetTile1 = tiles[currentRow + direction][currentCol];
            ChessTile targetTile2 = tiles[currentRow + 2 * direction][currentCol];
            if (targetTile1 != null && targetTile2 != null
                    && targetTile1.getPiece() == null && targetTile2.getPiece() == null) {
                if (wouldNotPutKingInCheck(tiles, currentTile, targetTile2, kingTile)) {
                    tiles[currentRow + 2 * direction][currentCol].setIsJumpMove(true);
                    moves.add((currentRow + 2 * direction) * 8 + currentCol);
                }
            }
        }

        for (int offset : new int[]{-1, 1}) {
            int newRow = currentRow + direction;
            int newCol = currentCol + offset;

            if (isValidMove(newRow, newCol)) {
                ChessTile diagonalTile = tiles[newRow][newCol];
                ChessPiece pieceOnTarget = diagonalTile != null ? diagonalTile.getPiece() : null;
                if (pieceOnTarget != null && pieceOnTarget.getColor() != currentPiece.getColor()) {
                    if (wouldNotPutKingInCheck(tiles, currentTile, diagonalTile, kingTile)) {
                        moves.add(newRow * 8 + newCol);
                    }
                }
            }
        }

        ChessTile enPassantTile = getEnPassantAbleTile(tiles, currentTile);
        if (enPassantTile != null) {
            if (wouldNotPutKingInCheck(tiles, currentTile, enPassantTile, kingTile)) {
                enPassantTile.setIsEnPassantMove(true);
            }
        }

        return moves;
    }

    private static List<Integer> getRookMoves(ChessTile[][] tiles, ChessTile currentTile, ChessTile kingTile) {
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
                        if (wouldNotPutKingInCheck(tiles, currentTile, targetTile, kingTile)) {
                            moves.add(newRow * 8 + newCol);
                        }
                    } else if (pieceOnTarget.getColor() != currentTile.getPiece().getColor()) {
                        if (wouldNotPutKingInCheck(tiles, currentTile, targetTile, kingTile)) {
                            moves.add(newRow * 8 + newCol);
                        }
                        break;
                    } else {
                        break;
                    }
                }
            }
        }

        return moves;
    }

    private static List<Integer> getKnightMoves(ChessTile[][] tiles, ChessTile currentTile, ChessTile kingTile) {
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

                if ((pieceOnTarget == null || pieceOnTarget.getColor() != currentTile.getPiece().getColor())) {
                    assert targetTile != null;
                    if (wouldNotPutKingInCheck(tiles, currentTile, targetTile, kingTile)) {
                        moves.add(newRow * 8 + newCol);
                    }
                }
            }
        }

        return moves;
    }

    private static List<Integer> getBishopMoves(ChessTile[][] tiles, ChessTile currentTile, ChessTile kingTile) {
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

                    if ((pieceOnTarget == null || pieceOnTarget.getColor() != currentPiece.getColor())) {
                        assert targetTile != null;
                        if (wouldNotPutKingInCheck(tiles, currentTile, targetTile, kingTile)) {
                            moves.add(newRow * 8 + newCol);
                        }
                    }

                    if (pieceOnTarget != null) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return moves;
    }

    private static List<Integer> getQueenMoves(ChessTile[][] tiles, ChessTile currentTile, ChessTile kingTile) {
        List<Integer> moves = new ArrayList<>();
        moves.addAll(getRookMoves(tiles, currentTile, kingTile));
        moves.addAll(getBishopMoves(tiles, currentTile, kingTile));

        return moves;
    }

    private static List<Integer> getKingMoves(ChessTile[][] tiles, ChessTile currentTile, ChessTile kingTile) {
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

                if ((pieceOnTarget == null || pieceOnTarget.getColor() != currentPiece.getColor())) {
                    assert targetTile != null;
                    if (wouldNotPutKingInCheck(tiles, currentTile, targetTile, kingTile)) {
                        moves.add(newRow * 8 + newCol);
                    }
                }
            }
        }

        // Castling logic
        int kingRow = GridPane.getRowIndex(kingTile);
        int kingCol = GridPane.getColumnIndex(kingTile);

        int kingSideRookCol = 7;
        int queenSideRookCol = 0;

        if (playerColor.equals(Chess.PieceColor.BLACK)) {
            kingSideRookCol = 0;
            queenSideRookCol = 7;
        }

        // TODO! improve
        if (canCastle(tiles, kingTile, tiles[kingRow][kingSideRookCol], true)) {
            if (playerColor.equals(Chess.PieceColor.WHITE)) {
                if (wouldNotPutKingInCheck(tiles, currentTile, tiles[kingRow][kingCol + 2], kingTile)
                        && wouldNotPutKingInCheck(tiles, currentTile, tiles[kingRow][kingCol + 2], kingTile)) {
                    tiles[kingRow][kingCol + 2].setIsCastleMove(true);
                }
            } else {
                if (wouldNotPutKingInCheck(tiles, currentTile, tiles[kingRow][kingCol - 2], kingTile)
                        && wouldNotPutKingInCheck(tiles, currentTile, tiles[kingRow][kingCol - 2], kingTile)) {
                    tiles[kingRow][kingCol - 2].setIsCastleMove(true);
                }
            }
        }

        if (canCastle(tiles, kingTile, tiles[kingRow][queenSideRookCol], false)) {
            if (playerColor.equals(Chess.PieceColor.WHITE)) {
                if (wouldNotPutKingInCheck(tiles, currentTile, tiles[kingRow][kingCol - 2], kingTile)
                        && wouldNotPutKingInCheck(tiles, currentTile, tiles[kingRow][kingCol - 2], kingTile)) {
                    tiles[kingRow][kingCol - 2].setIsCastleMove(true);
                }
            } else {
                if (wouldNotPutKingInCheck(tiles, currentTile, tiles[kingRow][kingCol + 2], kingTile)
                        && wouldNotPutKingInCheck(tiles, currentTile, tiles[kingRow][kingCol + 2], kingTile)) {
                    tiles[kingRow][kingCol + 2].setIsCastleMove(true);
                }
            }
        }

        return moves;
    }

    private static boolean isValidMove(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private static boolean wouldNotPutKingInCheck(ChessTile[][] tiles, ChessTile currentTile, ChessTile targetTile, ChessTile kingTile) {
        ChessPiece previousMovingPiece = currentTile.getPiece();
        ChessPiece previousTargetPiece = targetTile.getPiece();

        targetTile.setPiece(previousMovingPiece);
        currentTile.setPiece(null);

        boolean isInCheck;
        if (kingTile.getPiece() == null) {
            isInCheck = isKingInCheck(tiles, targetTile);
        } else {
            isInCheck = isKingInCheck(tiles, kingTile);
        }

        currentTile.setPiece(previousMovingPiece);
        targetTile.setPiece(previousTargetPiece);

        return !isInCheck;
    }

    public static boolean isKingInCheck(ChessTile[][] tiles, ChessTile kingTile) {
        int kingRow = GridPane.getRowIndex(kingTile);
        int kingCol = GridPane.getColumnIndex(kingTile);

        // Check rook, queen, bishop moves
        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1},
                {1, 1},
                {-1, -1},
                {1, -1},
                {-1, 1}
        };

        for (int[] direction : directions) {
            int dRow = direction[0];
            int dCol = direction[1];

            int r = kingRow + dRow;
            int c = kingCol + dCol;

            while (isValidMove(r, c)) {
                ChessTile tile = tiles[r][c];
                if (tile != null && tile.getPiece() != null) {
                    ChessPiece piece = tile.getPiece();
                    if (piece == null) break;

                    if (piece.getColor() != kingTile.getPiece().getColor()) {
                        boolean diagonalMove = Math.abs(dRow) == Math.abs(dCol);
                        switch (piece.getType()) {
                            case ROOK:
                                if (dRow == 0 || dCol == 0) {
                                    return true;
                                }
                                break;
                            case QUEEN:
                                if (dRow == 0 || dCol == 0 || diagonalMove) {
                                    return true;
                                }
                                break;
                            case BISHOP:
                                if (diagonalMove) {
                                    return true;
                                }
                                break;
                        }
                    }
                    break;
                }
                r += dRow;
                c += dCol;
            }
        }

        // Check Pawn moves
        for(int[] direction : directions) {
            int dRow = direction[0];
            int dCol = direction[1];

            int r = kingRow + dRow;
            int c = kingCol + dCol;

            if (!isValidMove(r, c)) continue;

            ChessTile tile = tiles[r][c];
            if (tile != null && tile.getPiece() != null) {
                ChessPiece piece = tile.getPiece();
                if (piece.getColor().equals(kingTile.getPiece().getColor())) continue;

                if ((playerColor.equals(kingTile.getPiece().getColor()))) {
                    if (dRow == -1 && (dCol == -1 || dCol == 1) && piece.getType().equals(Chess.PieceType.PAWN)) {
                        return true;
                    }
                } else {
                    if (dRow == 1 && (dCol == -1 || dCol == 1) && piece.getType().equals(Chess.PieceType.PAWN)) {
                        return true;
                    }
                }
            }
        }

        // Check king moves
        for(int[] direction : directions) {
            int dRow = direction[0];
            int dCol = direction[1];

            int r = kingRow + dRow;
            int c = kingCol + dCol;

            if (!isValidMove(r, c)) continue;

            ChessTile tile = tiles[r][c];
            if (tile != null && tile.getPiece() != null) {
                ChessPiece piece = tile.getPiece();
                if (piece.getColor().equals(kingTile.getPiece().getColor())) continue;

                if (piece.getType().equals(Chess.PieceType.KING)) {
                    return true;
                }
            }
        }

        // Check Knight moves
        int[][] knightMoves = {
                {kingRow + 2, kingCol + 1}, {kingRow + 2, kingCol - 1},
                {kingRow - 2, kingCol + 1}, {kingRow - 2, kingCol - 1},
                {kingRow + 1, kingCol + 2}, {kingRow + 1, kingCol - 2},
                {kingRow - 1, kingCol + 2}, {kingRow - 1, kingCol - 2}
        };

        for (int[] move : knightMoves) {
            int targetRow = move[0];
            int targetCol = move[1];
            if (isValidMove(targetRow, targetCol)) {
                ChessTile targetTile = tiles[targetRow][targetCol];
                if (targetTile != null && targetTile.getPiece() != null) {
                    ChessPiece targetPiece = targetTile.getPiece();
                    if (targetPiece.getColor() != kingTile.getPiece().getColor() && targetPiece.getType().equals(Chess.PieceType.KNIGHT)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isCheckMate(ChessTile[][] tiles, ChessTile kingTile) {
        for (ChessTile[] row : tiles) {
            for (ChessTile tile : row) {
                if (tile.getPiece() != null && tile.getPiece().getColor() == kingTile.getPiece().getColor()) {
                    int[] validMoves = getValidMoves(tile, tiles, kingTile);
                    if (validMoves.length > 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean canCastle(ChessTile[][] tiles, ChessTile kingTile, ChessTile rookTile, boolean isRightSide) {
        ChessPiece king = kingTile.getPiece();
        ChessPiece rook = rookTile.getPiece();

        if (king == null || rook == null) return false;
        if (king.cannotCastle() || rook.cannotCastle()) return false;

        int startCol;
        int endCol;

        if (playerColor.equals(PieceColor.WHITE)) {
            startCol = isRightSide ? GridPane.getColumnIndex(kingTile) + 1 : GridPane.getColumnIndex(kingTile) - 3;
            endCol = isRightSide ? GridPane.getColumnIndex(rookTile) - 1 : GridPane.getColumnIndex(rookTile) + 3;
        } else {
            startCol = isRightSide ? GridPane.getColumnIndex(kingTile) - 2 : GridPane.getColumnIndex(kingTile) + 1;
            endCol = isRightSide ? GridPane.getColumnIndex(rookTile) + 2 : GridPane.getColumnIndex(rookTile) - 1;
        }

        for (int col = startCol; col <= endCol; col++) {
            ChessTile tile = tiles[GridPane.getRowIndex(kingTile)][col];
            if (tile.getPiece() != null) {
                return false;
            }
        }

        return true;
    }

    public static ChessTile getEnPassantAbleTile(ChessTile[][] tiles, ChessTile pawnTile) {
        int currentRow = GridPane.getRowIndex(pawnTile);
        int currentCol = GridPane.getColumnIndex(pawnTile);

        int leftCol = currentCol - 1;
        int rightCol = currentCol + 1;

        ChessPiece pawnPiece = pawnTile.getPiece();

        int direction = (pawnPiece.getColor() == Chess.PieceColor.BLACK) ? 1 : -1;
        if (playerColor.equals(Chess.PieceColor.BLACK)) {
            direction *= -1;
        }

        if (isValidMove(currentRow, leftCol) && tiles[currentRow][leftCol].justJumped()
                && pawnPiece.getColor() != tiles[currentRow][leftCol].getPiece().getColor()) {
            return tiles[currentRow + direction][leftCol];
        }

        if (isValidMove(currentRow, rightCol) && tiles[currentRow][rightCol].justJumped()
                && pawnPiece.getColor() != tiles[currentRow][rightCol].getPiece().getColor()) {
            return tiles[currentRow + direction][rightCol];
        }

        return null;
    }

    public static void setPlayerColor(Chess.PieceColor color) {
        playerColor = color;
    }
    public static void setPromotionChoice(Chess.PieceType choice) { promotionChoice = choice; }
}