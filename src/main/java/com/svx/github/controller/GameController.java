package com.svx.github.controller;

import com.svx.github.view.GameView;

public class GameController extends Controller<GameView> {

    public GameController(AppController appController) {
        super(new GameView(), appController);
    }

    @Override
    protected void setActions() {

    }
}
