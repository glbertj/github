package com.svx.github.controller;

import com.svx.github.view.View;
import javafx.scene.Parent;

public abstract class Controller<T extends View<? extends Parent>> {
    protected final T view;
    protected final AppController appController;

    protected Controller(T view, AppController appController) {
        this.view = view;
        this.appController = appController;
        view.initializeView();
    }

    protected abstract void setActions();

    public T getView() {
        return view;
    }
}