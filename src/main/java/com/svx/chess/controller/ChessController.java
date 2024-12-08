package com.svx.chess.controller;

import com.svx.chess.controller.dialog.CheckmateDialogController;
import com.svx.chess.model.Chess;
import com.svx.chess.model.ChessBoard;
import com.svx.chess.model.ChessPiece;
import com.svx.chess.model.ChessTile;
import com.svx.chess.utility.SoundUtility;
import com.svx.chess.view.ChessView;
import com.svx.github.controller.AppController;
import com.svx.github.controller.Controller;
import com.svx.github.controller.LoginController;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;

public class ChessController extends Controller<ChessView> {
    private final ChessBoard chessBoard;
    private Chess.PieceColor playerColor;

    private boolean whiteTurn = true;
    private ChessTile selectedTile;

    private final ObservableList<ChessPiece> capturedBlackPiece = FXCollections.observableArrayList();
    private final ObservableList<ChessPiece> capturedWhitePiece = FXCollections.observableArrayList();

    public ChessController(AppController appController) {
        super(new ChessView(), appController);
        chessBoard = view.getChessBoard();
        playerColor = view.getPlayerColor();
        Chess.setPlayerColor(playerColor);
        SoundUtility.SoundType.START.play();
        setActions();

        if (playerColor != Chess.PieceColor.WHITE) {
            randomBotMove();
        }
    }

    @Override
    protected void setActions() {
        ChessTile[][] tiles = chessBoard.getTiles();
        for (ChessTile[] row : tiles) {
            for (ChessTile tile : row) {
                tile.setOnMouseClicked(e -> {
                    onTileClick(tile, appController);
                });
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

        view.getBackToLoginButton().setOnAction(e -> appController.navigatePage(new LoginController(appController)));
    }

    private void onTileClick(ChessTile targetTile, AppController appController) {
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

        handleMove(selectedPiece, targetTile);

        if (targetTile.getPiece() != null && targetTile.getPiece().getType() == Chess.PieceType.PAWN) {
            Chess.handlePawnPromotion(targetTile, playerColor, appController);
        }

        if (Chess.isKingInCheck(chessBoard.getTiles(), chessBoard.getWhiteKingTile()) || Chess.isKingInCheck(chessBoard.getTiles(), chessBoard.getBlackKingTile())) {
            if (Chess.isCheckMate(chessBoard.getTiles(), chessBoard.getWhiteKingTile())) {
                SoundUtility.SoundType.CHECKMATE.play();
                appController.openDialog(new CheckmateDialogController(appController, Chess.PieceColor.BLACK, view.createBackToLoginButton(), this));
                return;
            } else if (Chess.isCheckMate(chessBoard.getTiles(), chessBoard.getBlackKingTile())) {
                SoundUtility.SoundType.CHECKMATE.play();
                appController.openDialog(new CheckmateDialogController(appController, Chess.PieceColor.WHITE, view.createBackToLoginButton(), this));
                return;
            }

            SoundUtility.SoundType.CHECK.play();
        }

        if ((playerColor != Chess.PieceColor.WHITE && whiteTurn) || (playerColor == Chess.PieceColor.WHITE && !whiteTurn)) {
            randomBotMove();
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

    private void handleMove(ChessPiece selectedPiece, ChessTile targetTile) {
        if (!targetTile.isValidMove() && !targetTile.isEatable() && !targetTile.isCastleMove() && !targetTile.isEnPassantMove()) {
            return;
        }

        view.clearHighlightedTiles();
        selectedTile.setIsRecentMove(true);
        targetTile.setIsRecentMove(true);
        selectedPiece.setCanCastle(false);

        clearJumpMoves();

        if (targetTile.isEnPassantMove()) {
            SoundUtility.SoundType.CAPTURE.play();
            enPassant(selectedPiece, targetTile);
        } else if (targetTile.isCastleMove() && !Chess.isKingInCheck(chessBoard.getTiles(), selectedTile)) {
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
        if (selectedTile != null) selectedTile.setPiece(null);
        selectedTile = null;
        view.hideValidMoves();
    }

    private void capturePiece(ChessTile targetTile) {
        if (targetTile.getPiece().getColor() == Chess.PieceColor.WHITE) {
            capturedWhitePiece.add(targetTile.getPiece());
        } else {
            capturedBlackPiece.add(targetTile.getPiece());
        }
        targetTile.setPiece(null);
    }

    private void castle(ChessPiece selectedPiece, ChessTile targetTile) {
        ChessTile rookTile;
        ChessPiece rookPiece;
        ChessTile targetRookTile;
        int row = GridPane.getRowIndex(targetTile);
        int col = GridPane.getColumnIndex(targetTile);

        boolean kingSide = (playerColor == Chess.PieceColor.WHITE) ? (col > 3) : (col < 4);

        if (kingSide) {
            if (playerColor == Chess.PieceColor.WHITE) {
                rookTile = chessBoard.getTiles()[row][7];
                rookPiece = rookTile.getPiece();
                targetRookTile = chessBoard.getTiles()[row][5];
            } else {
                rookTile = chessBoard.getTiles()[row][0];
                rookPiece = rookTile.getPiece();
                targetRookTile = chessBoard.getTiles()[row][2];
            }
        } else {
            if (playerColor == Chess.PieceColor.WHITE) {
                rookTile = chessBoard.getTiles()[row][0];
                rookPiece = rookTile.getPiece();
                targetRookTile = chessBoard.getTiles()[row][3];
            } else {
                rookTile = chessBoard.getTiles()[row][7];
                rookPiece = rookTile.getPiece();
                targetRookTile = chessBoard.getTiles()[row][4];
            }
        }

        movePiece(selectedPiece, targetTile);

        selectedTile = rookTile;
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
        int rowToClear = (playerColor.equals(Chess.PieceColor.WHITE)) ? (whiteTurn ? 3 : 4) : (whiteTurn ? 4 : 3);

        ChessTile[] targetRow = chessBoard.getTiles()[rowToClear];

        for (ChessTile tile : targetRow) {
            tile.setIsJumpMove(false);
            tile.setJustJumped(false);
        }
    }

    public void resetGame() {
        playerColor = playerColor.equals(Chess.PieceColor.WHITE) ? Chess.PieceColor.BLACK : Chess.PieceColor.WHITE;
        view.setPlayerColor(playerColor);
        Chess.setPlayerColor(playerColor);

        whiteTurn = true;
        selectedTile = null;
        capturedBlackPiece.clear();
        capturedWhitePiece.clear();
        view.clearHighlightedTiles();

        chessBoard.setPlayerColor(playerColor);
        chessBoard.reset();

        if (playerColor != Chess.PieceColor.WHITE) {
            randomBotMove();
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

    private void randomBotMove() {
        ChessTile[][] tiles = chessBoard.getTiles();
        ChessTile[] allTiles = new ChessTile[64];
        int index = 0;
        for (ChessTile[] row : tiles) {
            for (ChessTile tile : row) {
                allTiles[index++] = tile;
            }
        }

        ChessTile randomTile;
        int[] validMoves;
        do {
            do {
                randomTile = allTiles[(int) (Math.random() * 64)];
            } while (randomTile.getPiece() == null || randomTile.getPiece().getColor() == playerColor);

            validMoves = Chess.getValidMoves(randomTile, tiles, getKingTileForCurrentTurn());
        } while (validMoves.length == 0);

        selectPiece(randomTile);
        int randomMove = validMoves[(int) (Math.random() * validMoves.length)];

        ChessTile targetTile = tiles[randomMove / 8][randomMove % 8];
        handleMove(randomTile.getPiece(), targetTile);
    }
}