package com.svx.github.view;

import javafx.animation.ScaleTransition;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.util.Duration;

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

    protected static Button createAnimatedButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("auth-button");

        button.setOnMousePressed(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
            scaleDown.setToX(0.95);
            scaleDown.setToY(0.95);
            scaleDown.play();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.play();
        });

        return button;
    }

}
