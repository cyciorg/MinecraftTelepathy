package org.cyci.mc.minecrafttelepathy.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * org.cyci.mc.minecrafttelepathy.managers
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy.iml
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sun - June/Sun/2024
 */
public class FriendManager {

    private final MySQLManager db;

    public FriendManager(MySQLManager db) {
        this.db = db;
    }

    public void addFriend(UUID playerUuid, UUID friendUuid) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = db.getConnection();
                 PreparedStatement statement = conn.prepareStatement(
                         "INSERT INTO friends (player_uuid, friend_uuid) VALUES (?, ?)")) {
                statement.setString(1, playerUuid.toString());
                statement.setString(2, friendUuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeFriend(UUID playerUuid, UUID friendUuid) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = db.getConnection();
                 PreparedStatement statement = conn.prepareStatement(
                         "DELETE FROM friends WHERE player_uuid = ? AND friend_uuid = ?")) {
                statement.setString(1, playerUuid.toString());
                statement.setString(2, friendUuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public List<UUID> getFriends(UUID playerUuid) {
        List<UUID> friends = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT friend_uuid FROM friends WHERE player_uuid = ?")) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    friends.add(UUID.fromString(resultSet.getString("friend_uuid")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }
}