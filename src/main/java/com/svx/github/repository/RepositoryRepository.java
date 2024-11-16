package com.svx.github.repository;

import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.Repository;
import com.svx.github.model.User;
import com.svx.github.model.UserSingleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RepositoryRepository {

    public static void save(Repository repository) {
        String query = "INSERT INTO repositories (repository_id, owner_id, name, latest_commit_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, repository.id().toString());
            stmt.setString(2, repository.ownerId().toString());
            stmt.setString(3, repository.name());
            stmt.setString(4, repository.latestCommitId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving repository: " + e.getMessage());
        }
    }

    public static ObservableList<Repository> loadAllUserRepositories(UUID ownerId) {
        ObservableList<Repository> repositories = FXCollections.observableArrayList();
        String query = "SELECT * FROM repositories WHERE owner_id = ?";
        try (Connection conn = ConnectionManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, ownerId.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("repository_id"));
                String name = rs.getString("name");
                String latestCommitId = rs.getString("latest_commit_id");
                Repository repository = new Repository(id, name, latestCommitId, ownerId);
                repositories.add(repository);
            }
        } catch (SQLException e) {
            System.out.println("Error loading repositories: " + e.getMessage());
        }
        return repositories;
    }

    public static void updateLatestCommitId(UUID repositoryId, String latestCommitId) {
        String query = "UPDATE repositories SET latest_commit_id = ? WHERE repository_id = ?";
        try (Connection conn = ConnectionManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, latestCommitId);
            stmt.setString(2, repositoryId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating latest commit ID: " + e.getMessage());
        }
    }
}

