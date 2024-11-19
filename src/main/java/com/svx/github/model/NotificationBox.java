package com.svx.github.model;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class NotificationBox extends HBox {
    private final Label messageLabel;
    private final ImageView iconView;

    public NotificationBox() {
        setPadding(new Insets(10));
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(300);
        setMaxHeight(100);
        setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        iconView = new ImageView();
        iconView.setFitWidth(24);
        iconView.setFitHeight(24);

        messageLabel = new Label();
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setFont(Font.font(14));
        messageLabel.setWrapText(true);

        getChildren().addAll(iconView, messageLabel);
        setVisible(false);
    }

    public void show(String message, NotificationType type, Image icon) {
        clear();

        messageLabel.setText(message);

        switch (type) {
            case SUCCESS -> setStyle("-fx-background-color: #28a745; -fx-background-radius: 8;");
            case ERROR -> setStyle("-fx-background-color: #d73a49; -fx-background-radius: 8;");
            case INFO -> setStyle("-fx-background-color: #0366d6; -fx-background-radius: 8;");
        }

        if (icon != null) {
            iconView.setImage(icon);
            iconView.setVisible(true);
        } else {
            iconView.setVisible(false);
        }

        animateShow();
    }

    private void animateShow() {
        setVisible(true);
        setOpacity(1.0);
        setTranslateY(50);

        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.2), this);
        slideIn.setToY(0);
        slideIn.play();

        slideIn.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), this);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                fadeOut.setOnFinished(resetEvent -> {
                    setVisible(false);
                    setTranslateY(50);
                });

                fadeOut.play();
            });

            pause.play();
        });
    }

    public enum NotificationType {
        SUCCESS, ERROR, INFO
    }

    public void clear() {
        getTransforms().clear();
        setOpacity(1.0);
        setVisible(false);
        setTranslateY(50);
    }
}