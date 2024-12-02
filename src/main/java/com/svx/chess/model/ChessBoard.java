package com.svx.chess.model;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;

public class ChessBoard extends GridPane {
    private final Chess.PieceColor playerColor;
    private final ChessTile[][] tiles;

    private ChessTile whiteKingTile;
    private ChessTile blackKingTile;

    public ChessBoard(Chess.PieceColor playerColor) {
        this.playerColor = playerColor;

        tiles = new ChessTile[8][8];

        initializeBoard();
    }

    public void initializeBoard() {
        getStyleClass().add("chess-board");
        setAlignment(Pos.CENTER);
        HBox.setHgrow(this, Priority.ALWAYS);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessTile tile = createTile(row, col);
                add(tile, col, row);
                tiles[row][col] = tile;
            }
        }

        widthProperty().addListener((observable, oldValue, newValue) -> updateTileSizes());
        heightProperty().addListener((observable, oldValue, newValue) -> updateTileSizes());

        addPieces();
    }

    private ChessTile createTile(int row, int col) {
        ChessTile tile = new ChessTile();
        String styleClass = (row + col) % 2 == 0 ? "white" : "green";
        tile.getStyleClass().add(styleClass);
        return tile;
    }

    private void addPieces() {
        int blackBackRow, blackFrontRow, whiteBackRow, whiteFrontRow;
        Chess.PieceType left;
        Chess.PieceType right;

        if (playerColor.equals(Chess.PieceColor.WHITE)) {
            blackBackRow = 0;
            blackFrontRow = 1;
            whiteFrontRow = 6;
            whiteBackRow = 7;
            left = Chess.PieceType.QUEEN;
            right = Chess.PieceType.KING;
        } else {
            whiteBackRow = 0;
            whiteFrontRow = 1;
            blackFrontRow = 6;
            blackBackRow = 7;
            left = Chess.PieceType.KING;
            right = Chess.PieceType.QUEEN;
        }

        addPawnsToRow(whiteFrontRow, Chess.PieceColor.WHITE);
        addPawnsToRow(blackFrontRow, Chess.PieceColor.BLACK);

        addPiecesToRow(whiteBackRow, Chess.PieceColor.WHITE, new Chess.PieceType[]{
                Chess.PieceType.ROOK, Chess.PieceType.KNIGHT, Chess.PieceType.BISHOP, left,
                right, Chess.PieceType.BISHOP, Chess.PieceType.KNIGHT, Chess.PieceType.ROOK
        });
        addPiecesToRow(blackBackRow, Chess.PieceColor.BLACK, new Chess.PieceType[]{
                Chess.PieceType.ROOK, Chess.PieceType.KNIGHT, Chess.PieceType.BISHOP, left,
                right, Chess.PieceType.BISHOP, Chess.PieceType.KNIGHT, Chess.PieceType.ROOK
        });
    }

    private void addPawnsToRow(int row, Chess.PieceColor color) {
        addPiecesToRow(row, color, new Chess.PieceType[]{
                Chess.PieceType.PAWN, Chess.PieceType.PAWN, Chess.PieceType.PAWN, Chess.PieceType.PAWN,
                Chess.PieceType.PAWN, Chess.PieceType.PAWN, Chess.PieceType.PAWN, Chess.PieceType.PAWN
        });
    }

    private void addPiecesToRow(int row, Chess.PieceColor color, Chess.PieceType[] pieceTypes) {
        for (int col = 0; col < 8; col++) {
            ChessPiece piece = new ChessPiece(pieceTypes[col], color);
            ChessTile tile = tiles[row][col];
            tile.setPiece(piece);

            if (pieceTypes[col].equals(Chess.PieceType.KING)) {
                if (color.equals(Chess.PieceColor.WHITE)) {
                    whiteKingTile = tiles[row][col];
                } else {
                    blackKingTile = tiles[row][col];
                }
            }
        }
    }

    private void updateTileSizes() {
        double boardWidth = getWidth();
        double boardHeight = getHeight();
        double size = Math.min(boardWidth, boardHeight) / 8;

        for (Node node : getChildren()) {
            if (node instanceof ChessTile tile) {
                tile.setPrefWidth(size);
                tile.setPrefHeight(size);
            }
        }
    }

    public ChessTile[][] getTiles() { return tiles; }
    public ChessTile getWhiteKingTile() { return whiteKingTile; }
    public void setWhiteKingTile(ChessTile whiteKingTile) { this.whiteKingTile = whiteKingTile; }
    public ChessTile getBlackKingTile() { return blackKingTile; }
    public void setBlackKingTile(ChessTile blackKingTile) { this.blackKingTile = blackKingTile; }
}
