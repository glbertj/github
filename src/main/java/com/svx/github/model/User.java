package com.svx.github.model;

import com.svx.github.utility.CryptoUtility;

import java.util.UUID;

public class User {
    private final UUID id;
    private String username;
    private String email;
    private String password;

    public User(UUID id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = CryptoUtility.hashPassword(password);
    }
}
