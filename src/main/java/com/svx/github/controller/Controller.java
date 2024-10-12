package com.svx.github.controller;

import com.svx.github.view.View;
import javafx.scene.Parent;
import javafx.scene.Scene;

public abstract class Controller<T extends View<? extends Parent>> {
    protected final T view;

    protected Controller(T view) {
        this.view = view;
        view.initializeView();
        setActions();
    }

    public Scene getView() {
        Scene scene = new Scene(view.getRoot(), 960, 540);
        scene.getStylesheets().add(view.getStyleReference());
        return scene;
    }

    protected abstract void setActions();
}