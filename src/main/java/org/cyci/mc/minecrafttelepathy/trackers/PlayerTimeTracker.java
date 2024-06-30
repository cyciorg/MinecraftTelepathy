package org.cyci.mc.minecrafttelepathy.trackers;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.cyci.mc.minecrafttelepathy.Registry;
import org.cyci.mc.minecrafttelepathy.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * org.cyci.mc.minecrafttelepathy.trackers
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sun - June/Sun/2024
 */
public class PlayerTimeTracker {
    private final HikariDataSource dataSource;
    private final Logger logger = Logger.getInstance(Registry.getInstance());
    private List<PlayerData> topPlayers = new ArrayList<>();

    public PlayerTimeTracker(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void addPlayerIfNotExists(Player player) {
        String uuid = player.getUniqueId().toString();

        if (!playerExists(uuid)) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement statement = conn.prepareStatement(
                         "INSERT INTO player_data (uuid, logins, playtime_minutes, points, wins, times_played) VALUES (?, 0, 0, 0, 0, 0)")) {
                statement.setString(1, uuid);
                statement.executeUpdate();
                logger.info("Added new player data for " + player.getName());
            } catch (SQLException e) {
                logger.error("Error adding player to database: " + e.getMessage());
            }
        }
    }

    public boolean playerExists(String uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT COUNT(*) AS count FROM player_data WHERE uuid = ?")) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                return count > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking if player exists: " + e.getMessage());
        }
        return false;
    }

    public void recordLogin(String uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "UPDATE player_data SET logins = logins + 1 WHERE uuid = ?")) {
            statement.setString(1, uuid);
            statement.executeUpdate();
            logger.info("Recorded login for player with UUID " + uuid);
        } catch (SQLException e) {
            logger.error("Error recording login: " + e.getMessage());
        }
    }

    public void recordPlaytime(String uuid, int minutes) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "UPDATE player_data SET playtime_minutes = playtime_minutes + ? WHERE uuid = ?")) {
            statement.setInt(1, minutes);
            statement.setString(2, uuid);
            statement.executeUpdate();
            logger.info("Recorded " + minutes + " minutes of playtime for player with UUID " + uuid);
        } catch (SQLException e) {
            logger.error("Error recording playtime: " + e.getMessage());
        }
    }

    public int getPlaytime(String uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT playtime_minutes FROM player_data WHERE uuid = ?")) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("playtime_minutes");
            }
        } catch (SQLException e) {
            logger.error("Error getting playtime: " + e.getMessage());
        }
        return 0;
    }

    public int getLogins(String uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT logins FROM player_data WHERE uuid = ?")) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("logins");
            }
        } catch (SQLException e) {
            logger.error("Error getting logins: " + e.getMessage());
        }
        return 0;
    }

    public void addPoints(String uuid, int points) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "UPDATE player_data SET points = points + ? WHERE uuid = ?")) {
            statement.setInt(1, points);
            statement.setString(2, uuid);
            statement.executeUpdate();
            logger.info("Added " + points + " points to player with UUID " + uuid);
        } catch (SQLException e) {
            logger.error("Error adding points: " + e.getMessage());
        }
    }

    public void addWin(String uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "UPDATE player_data SET wins = wins + 1 WHERE uuid = ?")) {
            statement.setString(1, uuid);
            statement.executeUpdate();
            logger.info("Recorded a win for player with UUID " + uuid);
        } catch (SQLException e) {
            logger.error("Error recording win: " + e.getMessage());
        }
    }

    public void incrementTimesPlayed(String uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "UPDATE player_data SET times_played = times_played + 1 WHERE uuid = ?")) {
            statement.setString(1, uuid);
            statement.executeUpdate();
            logger.info("Incremented times played for player with UUID " + uuid);
        } catch (SQLException e) {
            logger.error("Error incrementing times played: " + e.getMessage());
        }
    }

    public int getPoints(String uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT points FROM player_data WHERE uuid = ?")) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("points");
            }
        } catch (SQLException e) {
            logger.error("Error getting points: " + e.getMessage());
        }
        return 0;
    }

    public int getWins(String uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT wins FROM player_data WHERE uuid = ?")) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("wins");
            }
        } catch (SQLException e) {
            logger.error("Error getting wins: " + e.getMessage());
        }
        return 0;
    }

    public int getTimesPlayed(String uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT times_played FROM player_data WHERE uuid = ?")) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("times_played");
            }
        } catch (SQLException e) {
            logger.error("Error getting times played: " + e.getMessage());
        }
        return 0;
    }

    public void displayPlaytimeLeaderboard(int numPlayers) {
        List<PlayerData> topPlayers = getTopPlayers(numPlayers);
        Location scoreboardLocation = new Location(Bukkit.getWorld("world"), 0, 100, 0); // Adjust as needed
        // Hologram code here, if using HolographicDisplays or similar

        if (topPlayers.isEmpty()) {
            // Display empty leaderboard message
        } else {
            // Display leaderboard
        }
    }

    private List<PlayerData> getTopPlayers(int numPlayers) {
        List<PlayerData> topPlayers = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT uuid, playtime_minutes FROM player_data ORDER BY playtime_minutes DESC LIMIT ?")) {
            statement.setInt(1, numPlayers);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                int playtimeMinutes = resultSet.getInt("playtime_minutes");
                topPlayers.add(new PlayerData(uuid, playtimeMinutes));
            }
            return topPlayers;
        } catch (SQLException e) {
            logger.error("Error getting top players: " + e.getMessage());
        }
        return null;
    }

    private static class PlayerData {
        private final String uuid;
        private final int playtimeMinutes;

        public PlayerData(String uuid, int playtimeMinutes) {
            this.uuid = uuid;
            this.playtimeMinutes = playtimeMinutes;
        }

        public String getUuid() {
            return uuid;
        }

        public int getPlaytimeMinutes() {
            return playtimeMinutes;
        }
    }
}
