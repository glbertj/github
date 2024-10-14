package com.svx.github.controller;

import com.svx.github.view.View;
import javafx.scene.Parent;
import javafx.scene.Scene;

public abstract class Controller<T extends View<? extends Parent>> {
    protected final T view;
    protected final AppController appController;

    protected Controller(T view, AppController appController) {
        this.view = view;
        this.appController = appController;
        view.initializeView();
        setActions();
    }

    public Scene getView(double width, double height) {
        Scene scene = new Scene(view.getRoot(), width, height);

        if (view.getStyleReference() != null) {
            scene.getStylesheets().add(view.getStyleReference());
        } else {
            System.out.println("No style reference found for " + view.getClass().getSimpleName());
        }
        return scene;
    }

    protected abstract void setActions();
}