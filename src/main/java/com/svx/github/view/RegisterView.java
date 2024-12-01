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
    private Label toLoginButton;

    public RegisterView() {
        super("Create an Account");
    }

    @Override
    public void initializeView() {
        setupRoot();

        usernameField = createTextField("yora");
        emailField = createTextField("yora@gmail.com");
        passwordField = createPasswordField();

        formContainer.add(createFieldBox("Username", usernameField), 0, 0);
        formContainer.add(createFieldBox("Email address", emailField), 0, 1);
        formContainer.add(createFieldBox("Password", passwordField), 0, 2);

        registerButton = createButton("Register");
        formContainer.add(registerButton, 0, 3);

        toLoginButton = createLink("Sign in to GoaThub.");

        lowerContainer.add(new Label("Already have an account?"), 0, 0);
        lowerContainer.add(toLoginButton, 1, 0);
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

    public Label getToLoginButton() {
        return toLoginButton;
    }
}