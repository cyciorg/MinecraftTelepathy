package org.cyci.mc.minecrafttelepathy.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.cyci.mc.minecrafttelepathy.Registry;
import org.cyci.mc.minecrafttelepathy.enums.LogLevel;
import org.cyci.mc.minecrafttelepathy.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @project - JoinEvents
 * @author - Phil
 * @website - https://cyci.org
 * @email - staff@cyci.org
 * @created Mon - 02/Oct/2023 - 4:35 PM
 */
public class MySQLManager {
    private HikariDataSource dataSource;
    private ExecutorService executorService;
    private AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private final Logger logger;

    public MySQLManager(String host, int port, String database, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false");
        config.setUsername(username);
        config.setPassword(password);

        dataSource = new HikariDataSource(config);
        executorService = Executors.newCachedThreadPool();
        this.logger = Logger.getInstance(Registry.getInstance());
    }

    public CompletableFuture<Void> connectAsync() {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                if (!conn.isClosed()) {
                    logger.info("Connected to MySQL!");
                    createPlayerDataTable();
                } else {
                    logger.error("Connection is closed immediately after connecting.");
                }
            } catch (SQLException e) {
                String errorMessage = "Failed to connect to MySQL: " + e.getMessage();
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage, e);
            }
        }, executorService);
    }

    public CompletableFuture<Void> closeConnectionAsync() {
        if (isShuttingDown.get()) {
            return CompletableFuture.completedFuture(null);
        }

        isShuttingDown.set(true);

        return CompletableFuture.runAsync(() -> {
            try {
                dataSource.close();
                logger.warn("Closed MySQL connection.");
            } catch (Exception e) {
                String errorMessage = "Error while closing MySQL connection: " + e.getMessage();
                logger.error(errorMessage);
            }
        }, executorService).thenRun(this::shutdownExecutorService);
    }

    private void shutdownExecutorService() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.error("Executor service shutdown timed out.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Executor service shutdown interrupted: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (isShuttingDown.compareAndSet(false, true)) {
            closeConnectionAsync().join();
            shutdownExecutorService();
            logger.info("Executor service for MySQL manager shut down.");
            logger.info("Plugin disabled!");
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void createPlayerDataTable() {
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS player_data ("
                             + "uuid VARCHAR(36) PRIMARY KEY,"
                             + "logins INT NOT NULL DEFAULT 0,"
                             + "playtime_minutes INT NOT NULL DEFAULT 0,"
                             + "points INT NOT NULL DEFAULT 0,"
                             + "wins INT NOT NULL DEFAULT 0,"
                             + "times_played INT NOT NULL DEFAULT 0,"
                             + "blocks_placed INT NOT NULL DEFAULT 0,"
                             + "blocks_broken INT NOT NULL DEFAULT 0,"
                             + "time_spent_in_game INT NOT NULL DEFAULT 0,"
                             + "level INT NOT NULL DEFAULT 0,"
                             + "last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                             + ");")) {

            statement.execute();
            logger.info("Creating player_data table if it doesn't exist.");
        } catch (SQLException e) {
            logger.error("Error creating player_data table: " + e.getMessage());
        }
    }

    public HikariDataSource getDataSource() {
        return this.dataSource;
    }
}