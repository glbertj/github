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

        whiteTurn = !whiteTurn;
        processMoveOrCapture(selectedPiece, targetTile);

        if (targetTile.getPiece() != null && targetTile.getPiece().getType() == Chess.PieceType.PAWN) {
            Chess.handlePawnPromotion(targetTile, playerColor);
        }

        if (Chess.isKingInCheck(chessBoard.getTiles(), chessBoard.getWhiteKingTile())) {
            SoundUtility.SoundType.CHECK.play();
        }
        if (Chess.isKingInCheck(chessBoard.getTiles(), chessBoard.getBlackKingTile())) {
            SoundUtility.SoundType.CHECK.play();
        }
    }

    private void selectPiece(ChessTile targetTile) {
        selectedTile = targetTile;
        selectedTile.setIsRecentMove(true);
        int[] validMoves = Chess.getValidMoves(selectedTile, chessBoard.getTiles(), getKingTileForCurrentTurn());
        view.showValidMoves(validMoves);
    }

    private void deselectPiece() {
        selectedTile.setIsRecentMove(false);
        view.hideValidMoves();
        selectedTile = null;
    }

    private void processMoveOrCapture(ChessPiece selectedPiece, ChessTile targetTile) {
        if (!targetTile.isValidMove() && !targetTile.isEatable()) {
            return;
        }

        view.clearHighlightedTiles();
        selectedTile.setIsRecentMove(true);
        targetTile.setIsRecentMove(true);

        if (targetTile.isValidMove()) {
            movePiece(selectedPiece, targetTile);
        } else if (targetTile.isEatable()) {
            capturePiece(selectedPiece, targetTile);
        }

        if (targetTile.getPiece().getType() == Chess.PieceType.KING) {
            updateKingTile(targetTile);
        }
    }

    private void movePiece(ChessPiece selectedPiece, ChessTile targetTile) {
        SoundUtility.SoundType.MOVE.play();

        targetTile.setPiece(selectedPiece);
        selectedTile.setPiece(null);
        selectedTile = null;
        view.hideValidMoves();
    }

    public void capturePiece(ChessPiece selectedPiece, ChessTile targetTile) {
        SoundUtility.SoundType.CAPTURE.play();

        if (targetTile.getPiece().getColor() == Chess.PieceColor.WHITE) {
            capturedWhitePiece.add(targetTile.getPiece());
        } else {
            capturedBlackPiece.add(targetTile.getPiece());
        }

        targetTile.setPiece(selectedPiece);

        selectedTile.getPiece();
        selectedTile.setPiece(null);
        selectedTile = null;
        view.hideValidMoves();
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