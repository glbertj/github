package com.svx.chess.controller.dialog;

import com.svx.chess.model.Chess;
import com.svx.chess.view.dialog.PromotePawnDialogView;
import com.svx.github.controller.AppController;
import com.svx.github.controller.dialog.DialogController;

public class PromotePawnDialogController extends DialogController<PromotePawnDialogView> {

    public PromotePawnDialogController(AppController appController, Chess.PieceColor color) {
        super(new PromotePawnDialogView(color), appController);
        setActions();
    }

    @Override
    public void setActions() {
        super.setActions();

        view.getPromoteQueenButton().setOnAction(e -> {
            Chess.setPromotionChoice(Chess.PieceType.QUEEN);
            hideDialog();
        });
        view.getPromoteRookButton().setOnAction(e -> {
            Chess.setPromotionChoice(Chess.PieceType.ROOK);
            hideDialog();
        });
        view.getPromoteBishopButton().setOnAction(e -> {
            Chess.setPromotionChoice(Chess.PieceType.BISHOP);
            hideDialog();
        });
        view.getPromoteKnightButton().setOnAction(e -> {
            Chess.setPromotionChoice(Chess.PieceType.KNIGHT);
            hideDialog();
        });
    }
}

