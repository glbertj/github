package com.svx.github.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginView extends AuthView {
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;

    public LoginView() {
        super("Login");
    }

    @Override
    public void initializeView() {
        setupRoot();

        usernameField = createTextField("Enter your username");
        root.add(new Label("Username:"), 0, 1);
        root.add(usernameField, 1, 1);

        passwordField = createPasswordField();
        root.add(new Label("Password:"), 0, 2);
        root.add(passwordField, 1, 2);

        loginButton = createButton("Login");
        root.add(loginButton, 0, 3);

        registerButton = createButton("Register");
        root.add(registerButton, 1, 3);
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public Button getRegisterButton() {
        return registerButton;
    }
}
