package org.cyci.mc.minecrafttelepathy.lang;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

/**
 * Helped and created by an old friend
 */
public enum Lang {
    PREFIX("messages.prefix", "&a&lMinecraftTelepathy &7>"),
    NO_PERM("messages.noperm", "{prefix} &7You do not have the &c{permission} &7permission"),
    RELOAD("messages.reload", "{prefix} &aReloaded the config."),
    NOT_ENOUGH_ARGS("messages.no_arg", "{prefix} &cNot enough args."),
    TEAM_ASSIGN("messages.team_assign", "{prefix} &7You have been assigned to &a{team}&7!"),
    ROUND_START("messages.round_start", "{prefix} &aRound {round} has started! Theme: &b{theme}"),
    ROUND_END("messages.round_end", "{prefix} &eRound {round} has ended!"),
    ROUND_CANCEL("messages.round_cancel", "{prefix} &cRound {round} has been canceled!"),
    GAME_END("messages.game_end", "{prefix} &6The game has ended!"),
    GAME_RESET("messages.game_reset", "{prefix} &9The game has been reset!"),
    TEAM_POINT("messages.team_point", "{prefix} &aYour team has earned a point!"),
    WINNING_TEAM("messages.winning_team", "{prefix} &aThe winning team is {team} with {points} points!"),
    PLAYER_JOIN("messages.player_join", "{prefix} &e{player} has joined the game!"),
    PLAYER_LEAVE("messages.player_leave", "{prefix} &e{player} has left the game."),
    GAME_INSTANCE_START("messages.game_instance_start", "{prefix} &aA new game instance has started with {players} players!"),
    GAME_INSTANCE_END("messages.game_instance_end", "{prefix} &eGame instance has ended."),
    PLAYER_STATS("messages.player_stats", "{prefix} &7{player}'s stats - Points: &a{points}, Wins: &a{wins}, Times Played: &a{times_played}"),
    GAME_NOT_IN_PLAY("messages.game_not_in_play", "{prefix} &cThe game is not currently in play!"),
    BLOCK_OUT_OF_BOUNDS("messages.block_out_of_bounds", "{prefix} &cYou can only place blocks within the game map!");


    private final String path;
    private final String def;

    private static FileConfiguration LANG;

    Lang(String path, String def) {
        this.path = path;
        this.def = def;
    }

    public String getPath() {
        return path;
    }

    public String getDefault() {
        return def;
    }

    public String getConfigValue() {
        return LANG.getString(path, def);
    }
    public String getConfigValue(Player player, Map<String, String> replacements) {
        String value = LANG.getString(path, def);
        if (replacements != null && !replacements.isEmpty()) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                value = value.replace(entry.getKey(), entry.getValue());
            }
        }
        return processPlaceholders(player, value);
    }

    public String getConfigValue(Player player, String... args) {
        String value = LANG.getString(path, def);
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                value = value.replace("{" + i + "}", args[i]);
            }
        }
        return processPlaceholders(player, value);
    }

    public String getConfigValue(Player player, String placeholder, String replacement) {
        String value = LANG.getString(path, def);
        if (placeholder != null && replacement != null) {
            value = value.replace(placeholder, replacement);
        }
        return processPlaceholders(player, value);
    }

    private String processPlaceholders(Player player, String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Check if PlaceholderAPI is available
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            input = PlaceholderAPI.setPlaceholders(player, input);
        }

        return input;
    }

    public static void setFile(FileConfiguration config) {
        LANG = config;
    }
}