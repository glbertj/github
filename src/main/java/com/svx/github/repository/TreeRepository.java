package com.svx.github.repository;

import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.Repository;
import com.svx.github.model.Tree;
import com.svx.github.utility.CompressionUtility;
import com.svx.github.utility.JsonUtility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class TreeRepository {

    public static boolean save(Tree tree) {
        String query = "INSERT INTO trees (id, entries) VALUES (?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String serializedEntries = JsonUtility.serialize(tree.getEntries());

            stmt.setString(1, tree.getId());
            stmt.setString(2, serializedEntries);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error saving tree to database: " + e.getMessage());
            return false;
        }
    }

    public static Tree load(String treeId, Repository repository) {
        String query = "SELECT entries FROM trees WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, treeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String serializedEntries = rs.getString("entries");
                Map<String, String> entries = JsonUtility.deserialize(serializedEntries);
                return new Tree(entries);
            }
        } catch (SQLException e) {
            System.out.println("Error loading tree from database: " + e.getMessage());
        }
        return null;
    }
}
