package com.svx.github.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class ChessPiece {
    private final PieceType type;
    private final PieceColor color;
    private final ImageView imageView;

    public ChessPiece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;

        String path = "/com/svx/github/chess/" + color.toString().toLowerCase() + "_" + type.toString().toLowerCase() + ".png";
        Image image = new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
        this.imageView = new ImageView(image);
        this.imageView.getStyleClass().add("chess-piece");
    }

    public enum PieceType {
        PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
    }

    public enum PieceColor {
        WHITE, BLACK
    }

    public PieceType getType() { return type; }
    public PieceColor getColor() { return color; }
    public ImageView getImageView() { return imageView; }
}
