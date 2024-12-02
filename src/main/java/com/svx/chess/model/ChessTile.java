package com.svx.chess.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class ChessTile extends StackPane {
    private final ObjectProperty<ChessPiece> pieceProperty;
    private final SimpleBooleanProperty isValidMove;
    private final SimpleBooleanProperty isEatable;
    private final SimpleBooleanProperty isRecentMove;
    private final SimpleBooleanProperty isCastleMove;

    public ChessTile() {
        pieceProperty = new SimpleObjectProperty<>();
        isValidMove = new SimpleBooleanProperty(false);
        isEatable = new SimpleBooleanProperty(false);
        isRecentMove = new SimpleBooleanProperty(false);
        isCastleMove = new SimpleBooleanProperty(false);

        getStyleClass().add("tile");

        addValidMoveIndicator();
        addEnemyIndicator();
        addRecentMoveIndicator();
        addCastleMoveIndicator();
        addChessPieceImage();
    }

    private void addValidMoveIndicator() {
        Circle validMoveCircle = new Circle(10);
        validMoveCircle.getStyleClass().add("valid-move-circle");
        validMoveCircle.setVisible(false);
        getChildren().add(validMoveCircle);
        validMoveCircle.visibleProperty().bind(isValidMove);
    }

    private void addEnemyIndicator() {
        Circle enemyCircle = new Circle();
        enemyCircle.setRadius(30.0);
        enemyCircle.getStyleClass().add("enemy-circle");
        enemyCircle.setVisible(false);
        getChildren().add(enemyCircle);
        enemyCircle.visibleProperty().bind(isEatable);
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

    private void addChessPieceImage() {
        pieceProperty.addListener((observable) -> {
            ChessPiece newPiece = pieceProperty.get();
            if (newPiece != null) {
                if (getChildren().size() == 4) {
                    getChildren().remove(3);
                }
                getChildren().add(newPiece.getImageView());
            }
        });
    }

    private void addSizeAdjustor() {
        widthProperty().addListener((observable, oldValue, newValue) -> {
            double size = Math.min(getWidth(), getHeight());
            // TODO! Implement this
        });
    }

    public ChessPiece getPiece() { return pieceProperty.get(); }
    public void setPiece(ChessPiece piece) { pieceProperty.set(piece); }
    public boolean isValidMove() { return isValidMove.get(); }
    public void setIsValidMove(boolean isValidMove) { this.isValidMove.set(isValidMove); }
    public boolean isEatable() { return isEatable.get(); }
    public void setIsEatable(boolean isEatable) { this.isEatable.set(isEatable); }
    public void setIsRecentMove(boolean isRecentMove) { this.isRecentMove.set(isRecentMove); }
    public boolean isCastleMove() { return isCastleMove.get(); }
    public void setIsCastleMove(boolean isCastleMove) { this.isCastleMove.set(isCastleMove); }
}