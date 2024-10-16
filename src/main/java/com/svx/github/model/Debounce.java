package com.svx.github.model;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class Debounce {
    private final PauseTransition pauseTransition;

    public Debounce(Duration duration, Runnable action) {
        pauseTransition = new PauseTransition(duration);
        pauseTransition.setOnFinished(event -> action.run());
    }

    public void trigger() {
        pauseTransition.playFromStart();
    }
}
