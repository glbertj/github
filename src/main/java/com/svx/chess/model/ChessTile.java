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

    public ChessTile() {
        pieceProperty = new SimpleObjectProperty<>();
        isValidMove = new SimpleBooleanProperty(false);
        isEatable = new SimpleBooleanProperty(false);
        isRecentMove = new SimpleBooleanProperty(false);

        getStyleClass().add("tile");

        addValidMoveIndicator();
        addEnemyIndicator();
        addRecentMoveIndicator();
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

    private void addChessPieceImage() {
        pieceProperty.addListener((observable) -> {
            ChessPiece newPiece = pieceProperty.get();
            if (newPiece != null) {
                if (getChildren().size() == 3) {
                    getChildren().remove(2);
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
}