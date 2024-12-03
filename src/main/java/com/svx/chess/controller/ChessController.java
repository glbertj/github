package com.svx.chess.controller;

import com.svx.chess.model.Chess;
import com.svx.chess.model.ChessBoard;
import com.svx.chess.model.ChessPiece;
import com.svx.chess.model.ChessTile;
import com.svx.chess.utility.SoundUtility;
import com.svx.chess.view.ChessView;
import com.svx.github.controller.AppController;
import com.svx.github.controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;

public class ChessController extends Controller<ChessView> {
    private final ChessBoard chessBoard;
    private final Chess.PieceColor playerColor;

    private boolean whiteTurn = true;
    private ChessTile selectedTile;

    private final ObservableList<ChessPiece> capturedBlackPiece = FXCollections.observableArrayList();
    private final ObservableList<ChessPiece> capturedWhitePiece = FXCollections.observableArrayList();

    public ChessController(AppController appController) {
        super(new ChessView(), appController);

        chessBoard = view.getChessBoard();
        playerColor = view.getPlayerColor();

        Chess.setPlayerColor(playerColor);

        setActions();

        SoundUtility.SoundType.START.play();
    }

    @Override
    protected void setActions() {
        ChessTile[][] tiles = chessBoard.getTiles();
        for (ChessTile[] row : tiles) {
            for (ChessTile tile : row) {
                tile.setOnMouseClicked(e -> onTileClick(tile));
            }
        }

        capturedBlackPiece.addListener((ListChangeListener<ChessPiece>) c -> {
            view.getCapturedBlackBox().getChildren().clear();
            for (ChessPiece piece : capturedBlackPiece) {
                view.getCapturedBlackBox().getChildren().add(piece.getImageView());
            }
        });

        capturedWhitePiece.addListener((ListChangeListener<ChessPiece>) c -> {
            view.getCapturedWhiteBox().getChildren().clear();
            for (ChessPiece piece : capturedWhitePiece) {
                view.getCapturedWhiteBox().getChildren().add(piece.getImageView());
            }
        });
    }

    private void onTileClick(ChessTile targetTile) {
        if (selectedTile == null) {
            if (targetTile.getPiece() == null) return;
            if (targetTile.getPiece().getColor().equals(Chess.PieceColor.WHITE) && !whiteTurn) return;
            if (targetTile.getPiece().getColor().equals(Chess.PieceColor.BLACK) && whiteTurn) return;
            selectPiece(targetTile);
            return;
        }

        if (selectedTile == targetTile) {
            deselectPiece();
            return;
        }

        ChessPiece selectedPiece = selectedTile.getPiece();
        if (targetTile.getPiece() != null && targetTile.getPiece().getColor() == selectedPiece.getColor()) {
            deselectPiece();
            selectPiece(targetTile);
            return;
        }

        handleBoardUpdate(selectedPiece, targetTile);

        if (targetTile.getPiece() != null && targetTile.getPiece().getType() == Chess.PieceType.PAWN) {
            Chess.handlePawnPromotion(targetTile, playerColor);
        }

        if (Chess.isKingInCheck(chessBoard.getTiles(), chessBoard.getWhiteKingTile()) || Chess.isKingInCheck(chessBoard.getTiles(), chessBoard.getBlackKingTile())) {
            if (Chess.isCheckMate(chessBoard.getTiles(), chessBoard.getWhiteKingTile())) {
                SoundUtility.SoundType.CHECKMATE.play();
                return;
            } else if (Chess.isCheckMate(chessBoard.getTiles(), chessBoard.getBlackKingTile())) {
                SoundUtility.SoundType.CHECKMATE.play();
                return;
            }

            if (Chess.isKingInCheck(chessBoard.getTiles(), chessBoard.getWhiteKingTile())) {
                chessBoard.getWhiteKingTile().getPiece().setCanCastle(false);
            } else {
                chessBoard.getBlackKingTile().getPiece().setCanCastle(false);
            }

            SoundUtility.SoundType.CHECK.play();
        }
    }

    private void selectPiece(ChessTile targetTile) {
        selectedTile = targetTile;
        selectedTile.setIsRecentMove(true);
        int[] validMoves = Chess.getValidMoves(selectedTile, chessBoard.getTiles(), getKingTileForCurrentTurn());
        view.showValidMoves(selectedTile, validMoves);
    }

    private void deselectPiece() {
        selectedTile.setIsRecentMove(false);
        view.hideValidMoves();
        selectedTile = null;
    }

    private void handleBoardUpdate(ChessPiece selectedPiece, ChessTile targetTile) {
        if (!targetTile.isValidMove() && !targetTile.isEatable() && !targetTile.isCastleMove() && !targetTile.isEnPassantMove()) {
            return;
        }

        view.clearHighlightedTiles();
        selectedTile.setIsRecentMove(true);
        targetTile.setIsRecentMove(true);
        selectedPiece.setCanCastle(false);

        if (selectedPiece.getColor().equals(playerColor)) {
            clearJumpMoves();
        }

        if (targetTile.isEnPassantMove()) {
            SoundUtility.SoundType.CAPTURE.play();
            enPassant(selectedPiece, targetTile);
        } else if (targetTile.isCastleMove()) {
            SoundUtility.SoundType.CASTLE.play();
            castle(selectedPiece, targetTile);
        } else if (targetTile.isValidMove()) {
            SoundUtility.SoundType.MOVE.play();

            if (targetTile.isJumpMove()) {
                targetTile.setJustJumped(true);
            }

            movePiece(selectedPiece, targetTile);
        } else if (targetTile.isEatable()) {
            SoundUtility.SoundType.CAPTURE.play();
            capturePiece(targetTile);
            movePiece(selectedPiece, targetTile);
        }

        whiteTurn = !whiteTurn;

        if (targetTile.getPiece().getType() == Chess.PieceType.KING) {
            updateKingTile(targetTile);
        }
    }

    private void movePiece(ChessPiece selectedPiece, ChessTile targetTile) {
        targetTile.setPiece(selectedPiece);
        selectedTile.setPiece(null);
        selectedTile = null;
        view.hideValidMoves();
    }

    private void capturePiece(ChessTile targetTile) {
        if (targetTile.getPiece().getColor() == Chess.PieceColor.WHITE) {
            capturedWhitePiece.add(targetTile.getPiece());
        } else {
            capturedBlackPiece.add(targetTile.getPiece());
        }
    }

    private void castle(ChessPiece selectedPiece, ChessTile targetTile) {
        ChessPiece rookPiece;
        ChessTile targetRookTile;
        if (GridPane.getColumnIndex(targetTile) > 4) {
            rookPiece = chessBoard.getTiles()[GridPane.getRowIndex(targetTile)][7].getPiece();
            targetRookTile = chessBoard.getTiles()[GridPane.getRowIndex(targetTile)][5];
        } else {
            rookPiece = chessBoard.getTiles()[GridPane.getRowIndex(targetTile)][0].getPiece();
            targetRookTile = chessBoard.getTiles()[GridPane.getRowIndex(targetTile)][3];
        }
        movePiece(selectedPiece, targetTile);

        selectedTile = chessBoard.getTiles()[GridPane.getRowIndex(targetTile)][GridPane.getColumnIndex(targetTile) > 4 ? 7 : 0];
        movePiece(rookPiece, targetRookTile);
    }

    private void enPassant(ChessPiece selectedPiece, ChessTile targetTile) {
        int direction = (selectedPiece.getColor() == Chess.PieceColor.BLACK) ? -1 : 1;
        if (playerColor.equals(Chess.PieceColor.BLACK)) {
            direction *= -1;
        }

        int targetTileRow = GridPane.getRowIndex(targetTile);
        int targetTileCol = GridPane.getColumnIndex(targetTile);

        ChessTile enemyPawnTile = chessBoard.getTiles()[targetTileRow + direction][targetTileCol];
        capturePiece(enemyPawnTile);
        movePiece(selectedPiece, targetTile);
    }

    private void clearJumpMoves() {
        for (ChessTile[] row : chessBoard.getTiles()) {
            for (ChessTile tile : row) {
                tile.setIsJumpMove(false);
                tile.setJustJumped(false);
            }
        }
    }

    private ChessTile getKingTileForCurrentTurn() {
        return whiteTurn ? chessBoard.getWhiteKingTile() : chessBoard.getBlackKingTile();
    }

    private void updateKingTile(ChessTile targetTile) {
        if (targetTile.getPiece().getColor() == Chess.PieceColor.WHITE) {
            chessBoard.setWhiteKingTile(targetTile);
        } else {
            chessBoard.setBlackKingTile(targetTile);
        }
    }
}