package com.svx.github.controller;

import com.svx.github.model.User;
import com.svx.github.repository.UserRepository;
import com.svx.github.utility.CryptoUtility;
import com.svx.github.view.RegisterView;

import java.util.UUID;

public class RegisterController extends Controller<RegisterView> {

    protected RegisterController(AppController appController) {
        super(new RegisterView(), appController);
    }

    @Override
    protected void setActions() {
        view.getToLoginButton().setOnAction(e -> appController.navigatePage(new LoginController(appController)));
        view.getRegisterButton().setOnAction(e -> handleRegister());
    }

    private void handleRegister() {
        String username = view.getUsernameField().getText();
        String email = view.getEmailField().getText();
        String password = view.getPasswordField().getText();

        boolean registerSuccessful = UserRepository.registerUser(new User(UUID.randomUUID(), username, email, CryptoUtility.hashPassword(password)));
        if (registerSuccessful) {
            System.out.println("Register successful!");
        } else {
            System.out.println("Register failed!");
        }
    }
}
