package com.svx.github.repository;

import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.Repository;
import com.svx.github.model.UserSingleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RepositoryRepository {

    public static void save(Repository repository) throws SQLException {
        String query = "INSERT INTO repositories (owner_id, name, head_commit_id) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE head_commit_id = VALUES(head_commit_id)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, repository.getOwnerId().toString());
            stmt.setString(2, repository.getName());
            stmt.setString(3, repository.getLatestCommitId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        }
    }

    public static String getLatestCommitId(Repository repository) throws SQLException {
        String query = "SELECT head_commit_id FROM repositories WHERE owner_id = ? AND name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, repository.getOwnerId().toString());
            stmt.setString(2, repository.getName());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("head_commit_id");
            }
        } catch (SQLException e) {
            throw new SQLException();
        }

        return null;
    }

    public static Repository loadRepository(String name) throws SQLException {
        if (UserSingleton.getCurrentUser() == null) {
            return null;
        }

        String query = "SELECT head_commit_id FROM repositories WHERE owner_id = ? AND name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, UserSingleton.getCurrentUser().getId().toString());
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String headCommitId = rs.getString("head_commit_id");
                return new Repository(name, headCommitId, UserSingleton.getCurrentUser().getId());
            }
        } catch (SQLException e) {
            throw new SQLException();
        }

        return null;
    }

    public static List<Repository> loadAllUserRepositories() throws SQLException {
        if (UserSingleton.getCurrentUser() == null) {
            return new ArrayList<>();
        }

        String query = "SELECT name, head_commit_id FROM repositories WHERE owner_id = ?";
        List<Repository> repositories = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, UserSingleton.getCurrentUser().getId().toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String headCommitId = rs.getString("head_commit_id");

                Repository repository = new Repository(name, headCommitId, UserSingleton.getCurrentUser().getId());
                repositories.add(repository);
            }
        } catch (SQLException e) {
            throw new SQLException();
        }

        return repositories;
    }
}