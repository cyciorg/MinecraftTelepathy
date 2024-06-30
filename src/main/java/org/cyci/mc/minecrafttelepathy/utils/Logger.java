package org.cyci.mc.minecrafttelepathy.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyci.mc.minecrafttelepathy.Registry;
import org.cyci.mc.minecrafttelepathy.enums.LogLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * org.cyci.mc.minecrafttelepathy.utils
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sun - June/Sun/2024
 */
public class Logger {

    private static Logger instance;
    private final Registry plugin;
    private final DateTimeFormatter dateTimeFormatter;

    private Logger(Registry plugin) {
        this.plugin = plugin;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public static Logger getInstance(Registry plugin) {
        if (instance == null) {
            instance = new Logger(plugin);
        }
        return instance;
    }

    public void log(LogLevel level, String message) {
        String formattedMessage = formatMessage(level, message);
        switch (level) {
            case INFO:
                plugin.getLogger().info(formattedMessage);
                break;
            case WARN:
                plugin.getLogger().warning(formattedMessage);
                break;
            case ERROR:
                plugin.getLogger().severe(formattedMessage);
                break;
            case DEBUG:
                plugin.getLogger().fine(formattedMessage);
                break;
        }
    }

    private String formatMessage(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        return String.format("[%s] [%s] %s", timestamp, level, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }
}