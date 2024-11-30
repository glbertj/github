package com.svx.github.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class LoginView extends AuthView {
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label registerButton;

    public LoginView() {
        super("Sign in to GoaThub");
    }

    @Override
    public void initializeView() {
        setupRoot();

        usernameField = createTextField("yora");
        passwordField = createPasswordField();

        formContainer.add(createFieldBox("Username or email address", usernameField), 0, 0);
        formContainer.add(createFieldBox("Password", passwordField), 0, 1);

        loginButton = createButton("Sign in");
        formContainer.add(loginButton, 0, 2);

        registerButton = createLink("Create an account.");

        lowerContainer.add(new Label("New to GoaThub?"), 0, 0);
        lowerContainer.add(registerButton, 1, 0);
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

    public Label getRegisterButton() {
        return registerButton;
    }
}
