package com.svx.github.controller;

import com.svx.github.view.View;
import javafx.scene.Scene;

public abstract class Controller<T extends View> {
    protected final T view;

    protected Controller(T view) {
        this.view = view;
        view.initializeView();
        setActions();
    }

    public Scene getView() {
        return new Scene(view.getRoot(), 960, 540);
    }

    protected abstract void setActions();
}
