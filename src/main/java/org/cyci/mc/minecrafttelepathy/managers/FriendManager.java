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

    public CompletableFuture<Void> addFriend(UUID playerUuid, UUID friendUuid) {
        return CompletableFuture.runAsync(() -> {
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

    public CompletableFuture<Void> removeFriend(UUID playerUuid, UUID friendUuid) {
        return CompletableFuture.runAsync(() -> {
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

    public CompletableFuture<List<UUID>> getFriends(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
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
        });
    }

    public CompletableFuture<Boolean> areFriends(UUID playerUuid, UUID friendUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = db.getConnection();
                 PreparedStatement statement = conn.prepareStatement(
                         "SELECT 1 FROM friends WHERE player_uuid = ? AND friend_uuid = ? LIMIT 1")) {
                statement.setString(1, playerUuid.toString());
                statement.setString(2, friendUuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}