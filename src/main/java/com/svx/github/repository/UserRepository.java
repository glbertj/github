package com.svx.github.repository;

import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserRepository {

    public static User getByID(String id) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        return getUser(query, id);
    }

    public static User getByUsername(String input) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        return getUser(query, input);
    }

    public static User getByEmail(String input) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ?";
        return getUser(query, input);
    }

    private static User getUser(String query, String parameter) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, parameter);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(UUID.fromString(rs.getString("id")), rs.getString("username"), rs.getString("email"), rs.getString("password"));
            }
        } catch (SQLException e) {
            throw new SQLException();
        }
        return null;
    }

    public static boolean registerUser(User user) throws SQLException {
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
            throw new SQLException();
        }
    }
}
