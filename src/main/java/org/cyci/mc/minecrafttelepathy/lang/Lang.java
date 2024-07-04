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
    PLAYER_ONLY_COMMAND("messages.player_only_command", "{prefix} &cThis command can only be used by players."),
    PARTY_COMMAND_USAGE("messages.party.command_usage", "{prefix} &7Usage: /party <create|invite|kick|leave|disband|list>"),
    PARTY_CREATED("messages.party.created", "{prefix} &aParty created."),
    PLAYER_NOT_FOUND("messages.party.player_not_found", "{prefix} &cPlayer not found."),
    NOT_PARTY_LEADER("messages.party.not_party_leader", "{prefix} &cYou are not the leader of a party."),
    PLAYER_INVITED_TO_PARTY("messages.party.player_invited_to_party", "{prefix} &a{player} has been invited to your party."),
    PLAYER_KICKED_FROM_PARTY("messages.party.player_kicked_from_party", "{prefix} &a{player} has been kicked from the party."),
    NOT_IN_PARTY("messages.party.not_in_party", "{prefix} &cYou are not in a party."),
    PARTY_LEFT("messages.party.party_left", "{prefix} &aYou have left the party."),
    PARTY_DISBANDED("messages.party.party_disbanded", "{prefix} &aParty disbanded."),
    PARTY_MEMBERS_HEADER("messages.party.party_members_header", "{prefix} &7Your party members:"),
    PARTY_MEMBER("messages.party.party_member", "{prefix} &7- {player}"),
    FRIEND_COMMAND_USAGE("messages.friend.command_usage", "{prefix} &7Usage: /friend <add|remove|list>"),
    FRIEND_ADDED("messages.friend.friend_added", "{prefix} &a{player} has been added to your friends list."),
    FRIEND_REMOVED("messages.friend.friend_removed", "{prefix} &a{player} has been removed from your friends list."),
    NO_FRIENDS("messages.friend.no_friends", "{prefix} &cYou have no friends."),
    INVALID_PAGE_NUMBER("messages.friend.invalid_page_number", "{prefix} &cInvalid page number. Valid range: 1-{totalPages}."),
    FRIENDS_LIST_HEADER("messages.friend.friends_list_header", "{prefix} &7Your friends (Page {page}/{totalPages}):"),
    FRIEND_LIST_ITEM("messages.friend.friend_list_item", "{prefix} &7- {player}"),
    FRIENDS_LIST_NEXT_PAGE("messages.friend.friends_list_next_page", "{prefix} &7Type '/friend list {nextPage}' to see the next page."),
    TEAM_ASSIGN("messages.team.assign", "{prefix} &7You have been assigned to &a{team}&7!"),
    ROUND_START("messages.round.start", "{prefix} &aRound {round} has started! Theme: &b{theme}"),
    ROUND_END("messages.round.end", "{prefix} &eRound {round} has ended!"),
    ROUND_CANCEL("messages.round.cancel", "{prefix} &cRound {round} has been canceled!"),
    GAME_END("messages.game.end", "{prefix} &6The game has ended!"),
    GAME_RESET("messages.game.reset", "{prefix} &9The game has been reset!"),
    TEAM_POINT("messages.team.point", "{prefix} &aYour team has earned a point!"),
    WINNING_TEAM("messages.team.winning_team", "{prefix} &aThe winning team is {team} with {points} points!"),
    PLAYER_JOIN("messages.player.join", "{prefix} &e{player} has joined the game!"),
    PLAYER_LEAVE("messages.player.leave", "{prefix} &e{player} has left the game."),
    GAME_INSTANCE_START("messages.game.instance_start", "{prefix} &aA new game instance has started with {players} players!"),
    GAME_INSTANCE_END("messages.game.instance_end", "{prefix} &eGame instance has ended."),
    PLAYER_STATS("messages.player.stats", "{prefix} &7{player}'s stats - Points: &a{points}, Wins: &a{wins}, Times Played: &a{times_played}"),
    GAME_NOT_IN_PLAY("messages.game.not_in_play", "{prefix} &cThe game is not currently in play!"),
    BLOCK_OUT_OF_BOUNDS("messages.block.out_of_bounds", "{prefix} &cYou can only place blocks within the game map!");

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
    public static String getFormattedMessage(Lang message, Player player, Map<String, String> placeholders) {
        String msg = message.getDefault();

        // Replace prefix placeholder
        msg = msg.replace("{prefix}", PREFIX.getDefault());

        // Replace other placeholders
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message.processPlaceholders(player, msg);
    }
}