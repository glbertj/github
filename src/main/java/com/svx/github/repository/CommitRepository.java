package com.svx.github.repository;

import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.Commit;
import com.svx.github.model.Repository;
import java.sql.*;
import java.time.LocalDateTime;

public class CommitRepository {

    public static void save(Commit commit) {
        String query = "INSERT INTO commits (id, tree_id, parent_commit_id, message, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, commit.getId());
            stmt.setString(2, commit.getTreeId());
            stmt.setString(3, commit.getParentId());
            stmt.setString(4, commit.getMessage());
            stmt.setTimestamp(5, Timestamp.valueOf(commit.getTimestamp()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving commit to database: " + e.getMessage());
        }
    }

    public static Commit load(String commitId, Repository repository) {
        String query = "SELECT tree_id, parent_commit_id, message, timestamp FROM commits WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, commitId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String treeId = rs.getString("tree_id");
                String parentId = rs.getString("parent_commit_id");
                String message = rs.getString("message");
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();

                return new Commit(commitId, treeId, parentId, message, timestamp);
            }
        } catch (SQLException e) {
            System.out.println("Error loading commit from database: " + e.getMessage());
        }
        return null;
    }
}
