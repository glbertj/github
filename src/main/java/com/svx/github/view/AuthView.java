package com.svx.github.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import java.util.Objects;

public abstract class AuthView extends View<GridPane> {
    protected Label titleLabel;

    public AuthView(String title) {
        super();
        titleLabel = new Label(title);
        titleLabel.setFont(new Font(24));
        titleLabel.setId("auth-title");
    }

    protected void setupRoot() {
        root = new GridPane();
        root.setId("auth-pane");
        root.setVgap(15);
        root.setHgap(10);
        root.setAlignment(Pos.CENTER);

        styleReference = Objects.requireNonNull(
                getClass().getResource("/com/svx/github/style/auth.css")
        ).toExternalForm();

        root.add(titleLabel, 0, 0, 2, 1);
    }

    protected TextField createTextField(String placeholder) {
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        return textField;
    }

    protected PasswordField createPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        return passwordField;
    }

    protected Button createButton(String text) {
        Button button = createAnimatedButton(text);
        button.getStyleClass().add("auth-button");
        return button;
    }
}

