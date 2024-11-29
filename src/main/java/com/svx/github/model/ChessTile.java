package com.svx.github.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class ChessTile extends StackPane {
    private final ObjectProperty<ChessPiece> pieceProperty;
    private final int row;
    private final int col;
    private final SimpleBooleanProperty isValidMove;
    private final SimpleBooleanProperty isEnemy;
    private final SimpleBooleanProperty isRecentMove;

    public ChessTile(int row, int col) {
        pieceProperty = new SimpleObjectProperty<>();
        this.row = row;
        this.col = col;
        isValidMove = new SimpleBooleanProperty(false);
        isEnemy = new SimpleBooleanProperty(false);
        isRecentMove = new SimpleBooleanProperty(false);

        getStyleClass().add("tile");
        String tileClass = (row + col) % 2 == 0 ? "green" : "white";
        getStyleClass().add(tileClass);

        // Valid move indicator
        Circle validMoveCircle = new Circle(10);
        validMoveCircle.getStyleClass().add("valid-move-circle");
        validMoveCircle.setVisible(false);
        getChildren().add(validMoveCircle);
        validMoveCircle.visibleProperty().bind(isValidMove);

        // Enemy indicator
        Circle enemyCircle = new Circle(10);
        enemyCircle.getStyleClass().add("enemy-circle");
        enemyCircle.setVisible(true);
        getChildren().add(enemyCircle);
        enemyCircle.visibleProperty().bind(isEnemy);

        // Recent move indicator
        isRecentMove.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                getStyleClass().add("recent");
            } else {
                getStyleClass().remove("recent");
            }
        });

        // ImageView of the piece
        pieceProperty.addListener((observable) -> {
            ChessPiece newPiece = pieceProperty.get();
            if (newPiece != null) {
                getChildren().add(newPiece.getImageView());
            } else {
                if (getChildren().get(2) != null) {
                    getChildren().remove(2);
                }
            }
        });
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public ChessPiece getPiece() { return pieceProperty.get(); }
    public void setPiece(ChessPiece piece) { pieceProperty.set(piece); }
    public void setIsValidMove(boolean isValidMove) { this.isValidMove.set(isValidMove); }
    public void setIsEnemy(boolean isEnemy) { this.isEnemy.set(isEnemy); }
    public void setIsRecentMove(boolean isRecentMove) { this.isRecentMove.set(isRecentMove); }
}
