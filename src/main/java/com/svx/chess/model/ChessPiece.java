package com.svx.chess.model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Objects;

public class ChessPiece implements Observable {
    private final ObjectProperty<ChessPiece> pieceProperty;

    private final Chess.PieceType type;
    private final Chess.PieceColor color;
    private final ImageView imageView;
    private boolean hasMoved;

    public ChessPiece(Chess.PieceType type, Chess.PieceColor color) {
        this.pieceProperty = new SimpleObjectProperty<>(this);
        this.type = type;
        this.color = color;
        this.hasMoved = false;

        String path = "/com/svx/chess/chess_image/" + color.toString().toLowerCase() + "_" + type.toString().toLowerCase() + ".png";
        Image image = new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
        this.imageView = new ImageView(image);
        this.imageView.getStyleClass().add("chess-piece");
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        pieceProperty.addListener(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        pieceProperty.removeListener(invalidationListener);
    }

    public Chess.PieceType getType() { return type; }
    public Chess.PieceColor getColor() { return color; }
    public ImageView getImageView() { return imageView; }
    public boolean hasMoved() { return hasMoved; }
    public void setHasMoved(boolean hasMoved) { this.hasMoved = hasMoved; }
}
