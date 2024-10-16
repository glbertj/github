package com.svx.github.controller;

import com.svx.github.repository.UserRepository;
import com.svx.github.view.LoginView;

public class LoginController extends Controller<LoginView> {

    public LoginController(AppController appController) {
        super(new LoginView(), appController);
    }

    @Override
    protected void setActions() {
        view.getLoginButton().setOnAction(e -> handleLogin());
        view.getRegisterButton().setOnAction(e -> handleRegisterNavigation());
    }

    private void handleLogin() {
        String username = view.getUsernameField().getText();
        String password = view.getPasswordField().getText();

//        boolean loginSuccessful = UserRepository.authenticate(username, password);
//        if (loginSuccessful) {
            appController.navigatePage(new MainLayoutController(appController));
//        } else {
//            view.getErrorLabel().setText("Wrong credentials");
//        }
    }

    private void handleRegisterNavigation() {
        appController.navigatePage(new RegisterController(appController));
    }
}