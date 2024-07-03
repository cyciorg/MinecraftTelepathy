package org.cyci.mc.minecrafttelepathy.managers;

import org.cyci.mc.minecrafttelepathy.utils.Logger;

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
    private final Logger logger;

    public FriendManager(MySQLManager db, Logger logger) {
        this.db = db;
        this.logger = logger;
    }

    public CompletableFuture<Void> addFriend(UUID playerUuid, UUID friendUuid) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = db.getConnection()) {
                // Start a transaction
                conn.setAutoCommit(false);

                try (PreparedStatement statement1 = conn.prepareStatement(
                        "INSERT INTO friends (player_uuid, friend_uuid) VALUES (?, ?)");
                     PreparedStatement statement2 = conn.prepareStatement(
                             "INSERT INTO friends (player_uuid, friend_uuid) VALUES (?, ?)")) {

                    // Insert player to friend relationship
                    statement1.setString(1, playerUuid.toString());
                    statement1.setString(2, friendUuid.toString());
                    statement1.executeUpdate();

                    // Insert friend to player relationship
                    statement2.setString(1, friendUuid.toString());
                    statement2.setString(2, playerUuid.toString());
                    statement2.executeUpdate();

                    // Commit the transaction
                    conn.commit();
                } catch (SQLException e) {
                    // Rollback the transaction if there is an error
                    conn.rollback();
                    logger.error(e.getMessage());
                } finally {
                    // Restore auto-commit mode
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> removeFriend(UUID playerUuid, UUID friendUuid) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = db.getConnection()) {
                // Start a transaction
                conn.setAutoCommit(false);

                try (PreparedStatement statement1 = conn.prepareStatement(
                        "DELETE FROM friends WHERE player_uuid = ? AND friend_uuid = ?");
                     PreparedStatement statement2 = conn.prepareStatement(
                             "DELETE FROM friends WHERE player_uuid = ? AND friend_uuid = ?")) {

                    statement1.setString(1, playerUuid.toString());
                    statement1.setString(2, friendUuid.toString());
                    statement1.executeUpdate();

                    statement2.setString(1, friendUuid.toString());
                    statement2.setString(2, playerUuid.toString());
                    statement2.executeUpdate();

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    logger.error(e.getMessage());
                } finally {
                    // Restore auto-commit mode
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
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
                logger.error(e.getMessage());
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
                logger.error(e.getMessage());
                return false;
            }
        });
    }
}