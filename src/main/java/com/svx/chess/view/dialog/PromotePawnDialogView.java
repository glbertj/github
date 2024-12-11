package com.svx.chess.view.dialog;

import com.svx.chess.model.Chess;
import com.svx.chess.model.ChessPiece;
import com.svx.github.view.dialog.DialogView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class PromotePawnDialogView extends DialogView<HBox> {
    private final Chess.PieceColor color;
    private Button promoteQueenButton;
    private Button promoteRookButton;
    private Button promoteBishopButton;
    private Button promoteKnightButton;

    public PromotePawnDialogView(Chess.PieceColor color) {
        super();
        this.color = color;
    }

    @Override
    public void initializeView() {
        root = new HBox();
        root.getStyleClass().add("dialog-root");

        double size = 100;

        ChessPiece queenPiece = new ChessPiece(Chess.PieceType.QUEEN, color);
        ChessPiece rookPiece = new ChessPiece(Chess.PieceType.ROOK, color);
        ChessPiece bishopPiece = new ChessPiece(Chess.PieceType.BISHOP, color);
        ChessPiece knightPiece = new ChessPiece(Chess.PieceType.KNIGHT, color);

        promoteQueenButton = new Button();
        promoteQueenButton.setGraphic(queenPiece.getImageView());
        promoteQueenButton.getStyleClass().add("promote-button");
        promoteRookButton = new Button();
        promoteRookButton.setGraphic(rookPiece.getImageView());
        promoteRookButton.getStyleClass().add("promote-button");
        promoteBishopButton = new Button();
        promoteBishopButton.setGraphic(bishopPiece.getImageView());
        promoteBishopButton.getStyleClass().add("promote-button");
        promoteKnightButton = new Button();
        promoteKnightButton.setGraphic(knightPiece.getImageView());
        promoteKnightButton.getStyleClass().add("promote-button");

        root.getChildren().addAll(promoteQueenButton, promoteRookButton, promoteBishopButton, promoteKnightButton);

        cancelButton = new Button();
        createTitleBar("Promote Pawn");

        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        root.getChildren().add(errorLabel);
    }

    public Button getPromoteQueenButton() {
        return promoteQueenButton;
    }

    public Button getPromoteRookButton() {
        return promoteRookButton;
    }

    public Button getPromoteBishopButton() {
        return promoteBishopButton;
    }

    public Button getPromoteKnightButton() {
        return promoteKnightButton;
    }
}
