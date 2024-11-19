package com.svx.github.model;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class NotificationBox extends HBox {
    private final Label messageLabel;
    private final FontIcon iconView;

    public NotificationBox() {
        setPadding(new Insets(10, 60, 10, 10));
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setMinWidth(300);
        setMaxWidth(Region.USE_PREF_SIZE);
        setPrefWidth(Region.USE_COMPUTED_SIZE);

        setMaxHeight(Region.USE_PREF_SIZE);
        setPrefHeight(Region.USE_COMPUTED_SIZE);
        setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        iconView = new FontIcon();
        iconView.setIconSize(20);
        iconView.setIconColor(Color.WHITE);

        messageLabel = new Label();
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setFont(Font.font(14));
        messageLabel.setWrapText(true);

        getChildren().addAll(iconView, messageLabel);
        setVisible(false);
    }

    public void show(String message, NotificationType type, String iconCode) {
        clear();

        messageLabel.setText(message);

        switch (type) {
            case SUCCESS -> setStyle("-fx-background-color: #28a745; -fx-background-radius: 8;");
            case ERROR -> setStyle("-fx-background-color: #d73a49; -fx-background-radius: 8;");
            case INFO -> setStyle("-fx-background-color: #0366d6; -fx-background-radius: 8;");
        }

        if (iconCode != null) {
            iconView.setIconLiteral(iconCode);
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