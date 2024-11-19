package com.svx.github.controller;

import com.svx.github.manager.SessionManager;
import com.svx.github.model.User;
import com.svx.github.model.UserSingleton;
import com.svx.github.repository.UserRepository;
import com.svx.github.utility.CryptoUtility;
import com.svx.github.view.LoginView;

public class LoginController extends Controller<LoginView> {

    public LoginController(AppController appController) {
        super(new LoginView(), appController);
    }

    @Override
    protected void setActions() {
        view.getLoginButton().setOnAction(e -> handleLogin());
        view.getRegisterButton().setOnAction(e -> navigateToRegister());
    }

    private void handleLogin() {
        String username = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText();

        clearError();

        if (username.isEmpty() || password.isEmpty()) {
            return;
        }

        User authenticatedUser = UserRepository.getByUsername(username);
        if (authenticatedUser != null && CryptoUtility.verifyPassword(password, authenticatedUser.getPassword())) {
            SessionManager.createSession(authenticatedUser);
            UserSingleton.setCurrentUser(authenticatedUser);

            appController.navigatePage(new MainLayoutController(appController));
        } else {

        }
    }

    private void navigateToRegister() {
        appController.navigatePage(new RegisterController(appController));
    }

    private void clearError() {
        view.getErrorLabel().setVisible(false);
        view.getErrorLabel().setText("");
    }
}
