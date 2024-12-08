package com.svx.chess.model;

import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class ChessTile extends StackPane {
    private final ObjectProperty<ChessPiece> pieceProperty;
    private final SimpleBooleanProperty isValidMove;
    private final SimpleBooleanProperty isEatable;
    private final SimpleBooleanProperty isRecentMove;
    private final SimpleBooleanProperty isCastleMove;
    private boolean isJumpMove;
    private boolean justJumped;
    private final SimpleBooleanProperty isEnPassantMove;

    public ChessTile() {
        pieceProperty = new SimpleObjectProperty<>();
        isValidMove = new SimpleBooleanProperty(false);
        isEatable = new SimpleBooleanProperty(false);
        isRecentMove = new SimpleBooleanProperty(false);
        isCastleMove = new SimpleBooleanProperty(false);
        isJumpMove = false;
        justJumped = false;
        isEnPassantMove = new SimpleBooleanProperty(false);

        getStyleClass().add("tile");

        addValidMoveIndicator();
        addRecentMoveIndicator();
        addCastleMoveIndicator();
        addEnPassantMoveIndicator();
        addEnemyIndicator();
        addChessPieceImage();
        addSizeAdjustor();
    }

    private void addValidMoveIndicator() {
        Circle validMoveCircle = new Circle(10);
        validMoveCircle.getStyleClass().add("valid-move-circle");
        validMoveCircle.setVisible(false);
        getChildren().add(validMoveCircle);
        validMoveCircle.visibleProperty().bind(isValidMove);
    }

    private void addRecentMoveIndicator() {
        isRecentMove.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                getStyleClass().add("recent");
            } else {
                getStyleClass().remove("recent");
            }
        });
    }

    private void addCastleMoveIndicator() {
        Circle castleMoveCircle = new Circle(10);
        castleMoveCircle.getStyleClass().add("valid-move-circle");
        castleMoveCircle.setVisible(false);
        getChildren().add(castleMoveCircle);
        castleMoveCircle.visibleProperty().bind(isCastleMove);
    }

    private void addEnPassantMoveIndicator() {
        Circle enPassantCircle = new Circle(10);
        enPassantCircle.getStyleClass().add("valid-move-circle");
        enPassantCircle.setVisible(false);
        getChildren().add(enPassantCircle);
        enPassantCircle.visibleProperty().bind(isEnPassantMove);
    }

    private void addEnemyIndicator() {
        Circle enemyCircle = new Circle();
        enemyCircle.getStyleClass().add("enemy-circle");
        enemyCircle.setVisible(false);
        getChildren().add(enemyCircle);
        enemyCircle.visibleProperty().bind(isEatable);
    }

    private void addChessPieceImage() {
        pieceProperty.addListener((observable) -> {
            ChessPiece newPiece = pieceProperty.get();

            if (getChildren().size() == 5) {
                getChildren().remove(4);
            }

            if (newPiece != null) {
                getChildren().add(newPiece.getImageView());
            }
        });
    }

    private void addSizeAdjustor() {
        final PauseTransition pause = new PauseTransition(Duration.millis(5));
        pause.setOnFinished(event -> adjustPieceSize());

        widthProperty().addListener((observable, oldValue, newValue) -> pause.playFromStart());
        heightProperty().addListener((observable, oldValue, newValue) -> pause.playFromStart());
    }

    private void adjustPieceSize() {
        double size = Math.min(getWidth(), getHeight());
        if (getPiece() != null && getPiece().getImageView() != null) {
            getPiece().getImageView().setFitWidth(size / 1.5);
            getPiece().getImageView().setFitHeight(size / 1.5);
        }

        for (int i = 0; i < 3; i++) {
            if (getChildren().get(i) instanceof Circle) {
                Circle circle = (Circle) getChildren().get(i);
                circle.setRadius(size / 7.5);
            }
        }

        Circle enemyCircle = (Circle) getChildren().get(3);
        enemyCircle.setStrokeWidth(size / 12.5);
        enemyCircle.setRadius(size / 2.5);
    }

    public ChessPiece getPiece() { return pieceProperty.get(); }
    public void setPiece(ChessPiece piece) { pieceProperty.set(piece); }
    public boolean isValidMove() { return isValidMove.get(); }
    public void setIsValidMove(boolean isValidMove) { this.isValidMove.set(isValidMove); }
    public boolean isJumpMove() { return isJumpMove; }
    public void setIsJumpMove(boolean isJumpMove) { this.isJumpMove = isJumpMove; }
    public boolean justJumped() { return justJumped; }
    public void setJustJumped(boolean justJumped) { this.justJumped = justJumped; }
    public boolean isEatable() { return isEatable.get(); }
    public void setIsEatable(boolean isEatable) { this.isEatable.set(isEatable); }
    public void setIsRecentMove(boolean isRecentMove) { this.isRecentMove.set(isRecentMove); }
    public boolean isCastleMove() { return isCastleMove.get(); }
    public void setIsCastleMove(boolean isCastleMove) { this.isCastleMove.set(isCastleMove); }
    public boolean isEnPassantMove() { return isEnPassantMove.get(); }
    public void setIsEnPassantMove(boolean isEnPassantMove) { this.isEnPassantMove.set(isEnPassantMove); }
}