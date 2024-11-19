package com.svx.github.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterView extends AuthView {
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private Button registerButton;
    private Button toLoginButton;

    public RegisterView() {
        super("Register");
    }

    @Override
    public void initializeView() {
        setupRoot();

        usernameField = createTextField("Enter your username");
        root.add(new Label("Username:"), 0, 1);
        root.add(usernameField, 1, 1);

        emailField = createTextField("Enter your email");
        root.add(new Label("Email:"), 0, 2);
        root.add(emailField, 1, 2);

        passwordField = createPasswordField();
        root.add(new Label("Password:"), 0, 3);
        root.add(passwordField, 1, 3);

        registerButton = createButton("Register");
        root.add(registerButton, 0, 4);

        toLoginButton = createButton("Back to Login");
        root.add(toLoginButton, 1, 4);
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getToLoginButton() {
        return toLoginButton;
    }
}