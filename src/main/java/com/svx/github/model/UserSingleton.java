package com.svx.github.model;

public class UserSingleton {
    private static User currentUser;

    private UserSingleton() {}

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clearCurrentUser() {
        currentUser = null;
    }
}
