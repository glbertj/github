package com.svx.chess.controller;

import com.svx.chess.model.ChessBoard;
import com.svx.chess.view.ChessView;
import com.svx.github.controller.AppController;
import com.svx.github.controller.Controller;

public class ChessController extends Controller<ChessView> {
    private ChessBoard chessBoard;

    public ChessController(AppController appController) {
        super(new ChessView(), appController);

    }

    @Override
    protected void setActions() {

    }
}
