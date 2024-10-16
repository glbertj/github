package com.svx.github.view;

import javafx.scene.Parent;

public abstract class View<T extends Parent> {
    protected T root;
    protected String styleReference;

    public View() {
        root = null;
        styleReference = null;
    }

    public abstract void initializeView();

    public T getRoot() {
        return root;
    }

    public String getStyleReference() {
        return styleReference;
    }
}
