package com.svx.chess.controller.dialog;

import com.svx.chess.controller.ChessController;
import com.svx.chess.model.Chess;
import com.svx.chess.view.dialog.CheckmateDialogView;
import com.svx.github.controller.AppController;
import com.svx.github.controller.dialog.DialogController;
import javafx.scene.control.Button;

public class CheckmateDialogController extends DialogController<CheckmateDialogView> {
    private final ChessController chessController;

    public CheckmateDialogController(AppController appController, Chess.PieceColor color, Button toLoginButton, ChessController chessController) {
        super(new CheckmateDialogView(color, toLoginButton), appController);
        this.chessController = chessController;
    }

    @Override
    public void setActions() {
        super.setActions();

        view.getResetButton().setOnAction(e -> {
            chessController.resetGame();
            hideDialog();
        });
    }
}
