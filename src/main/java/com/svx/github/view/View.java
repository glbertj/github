package com.svx.github.view;

import javafx.scene.layout.Pane;

public abstract class View {
    protected Pane root;

    public Pane getRoot() {
        return root;
    }

    public abstract void initializeView();
}
