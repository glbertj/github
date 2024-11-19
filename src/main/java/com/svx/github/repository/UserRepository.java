package com.svx.github.repository;

import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.User;
import com.svx.github.model.UserSingleton;
import com.svx.github.utility.CryptoUtility;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserRepository {

    public static User getByID(String id) {
        String query = "SELECT * FROM users WHERE id = ?";
        return getUser(query, id);
    }

    public static User getByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        return getUser(query, username);
    }

    private static User getUser(String query, String parameter) {
        try (Connection conn = ConnectionManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, parameter);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(UUID.fromString(rs.getString("id")), rs.getString("username"), rs.getString("email"), rs.getString("password"));
            }
        } catch (SQLException e) {
            System.out.println("Error getting user: " + e.getMessage());
        }
        return null;
    }

    public static boolean registerUser(User user) {
        try (Connection conn = ConnectionManager.getConnection()) {
            String query = "INSERT INTO users (id, username, email, password) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getId().toString());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
        }
        return false;
    }
}
