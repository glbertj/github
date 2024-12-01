package com.svx.github.model.game;

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
    private final SimpleBooleanProperty isEatable;
    private final SimpleBooleanProperty isRecentMove;

    public ChessTile(int row, int col) {
        pieceProperty = new SimpleObjectProperty<>();
        this.row = row;
        this.col = col;
        isValidMove = new SimpleBooleanProperty(false);
        isEatable = new SimpleBooleanProperty(false);
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

        // Enemy indicator (Ring)
        Circle enemyCircle = new Circle();
        enemyCircle.setRadius(30.0);
        enemyCircle.getStyleClass().add("enemy-circle");
        enemyCircle.setVisible(false);
        getChildren().add(enemyCircle);
        enemyCircle.visibleProperty().bind(isEatable);

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
                if (getChildren().size() == 3) {
                    getChildren().remove(2);
                }
                getChildren().add(newPiece.getImageView());
            }
        });

        // Resizes
//        widthProperty().addListener((observable, oldValue, newValue) -> {
//            double size = Math.min(getWidth(), getHeight());
//            enemyCircle.setRadius(size / 2);
//
//            if (pieceProperty.get() != null) {
//                pieceProperty.get().getImageView().setFitWidth(size);
//                pieceProperty.get().getImageView().setFitHeight(size);
//            }
//        });
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public ChessPiece getPiece() { return pieceProperty.get(); }
    public void setPiece(ChessPiece piece) { pieceProperty.set(piece); }
    public boolean isValidMove() { return isValidMove.get(); }
    public void setIsValidMove(boolean isValidMove) { this.isValidMove.set(isValidMove); }
    public boolean isEatable() { return isEatable.get(); }
    public void setIsEatable(boolean isEatable) { this.isEatable.set(isEatable); }
    public boolean isRecentMove() { return isRecentMove.get(); }
    public void setIsRecentMove(boolean isRecentMove) { this.isRecentMove.set(isRecentMove); }
}