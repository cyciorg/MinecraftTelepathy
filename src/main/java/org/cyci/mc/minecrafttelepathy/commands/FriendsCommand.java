package org.cyci.mc.minecrafttelepathy.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyci.mc.minecrafttelepathy.api.CommandInfo;
import org.cyci.mc.minecrafttelepathy.api.PaginatedSubcommand;
import org.cyci.mc.minecrafttelepathy.api.SubCommandInfo;
import org.cyci.mc.minecrafttelepathy.lang.Lang;
import org.cyci.mc.minecrafttelepathy.managers.FriendManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * org.cyci.mc.minecrafttelepathy.commands
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy.iml
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Wed - July/Wed/2024
 */
public class FriendsCommand {

    private final FriendManager friendManager;

    public FriendsCommand(FriendManager friendManager) {
        this.friendManager = friendManager;
    }

    @CommandInfo(name = "friend", description = "Manage your friends", usage = "/friend <add|remove|list>", permission = "minecrafttelepathy.friend")
    public void onFriendCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("Usage: /friend <add|remove|list>");
        }
    }

    @SubCommandInfo(name = "add", args = {"player"}, description = "Add a player as a friend", usage = "/friend add <player>", permission = "minecrafttelepathy.friend.add")
    public void addFriend(Player player, String friendName) {
        Player friend = player.getServer().getPlayer(friendName);
        if (friend == null) {
            player.sendMessage("Player not found.");
            return;
        }

        friendManager.addFriend(player.getUniqueId(), friend.getUniqueId());
        player.sendMessage(friendName + " has been added to your friends list.");
    }

    @SubCommandInfo(name = "remove", args = {"player"}, description = "Remove a player from your friends list", usage = "/friend remove <player>", permission = "minecrafttelepathy.friend.remove")
    public void removeFriend(Player player, String friendName) {
        Player friend = player.getServer().getPlayer(friendName);
        if (friend == null) {
            player.sendMessage("Player not found.");
            return;
        }

        friendManager.removeFriend(player.getUniqueId(), friend.getUniqueId());
        player.sendMessage(friendName + " has been removed from your friends list.");
    }

    @PaginatedSubcommand(
            name = "list",
            description = "List your friends",
            usage = "/friend list [page]",
            permission = "minecrafttelepathy.friend.list",
            itemsPerPage = 10
    )
    public void listFriends(Player player, Command command, String label, String[] args, int page, int itemsPerPage) {
        friendManager.getFriends(player.getUniqueId()).thenAccept(friends -> {
            if (friends.isEmpty()) {
                player.sendMessage(Lang.getFormattedMessage(Lang.NO_FRIENDS, player, null));
                return;
            }

            int totalPages = (int) Math.ceil((double) friends.size() / itemsPerPage);
            if (page < 1 || page > totalPages) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("totalPages", String.valueOf(totalPages));
                player.sendMessage(Lang.getFormattedMessage(Lang.INVALID_PAGE_NUMBER, player, placeholders));
                return;
            }

            int startIndex = (page - 1) * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, friends.size());

            Map<String, String> headerPlaceholders = new HashMap<>();
            headerPlaceholders.put("page", String.valueOf(page));
            headerPlaceholders.put("totalPages", String.valueOf(totalPages));
            player.sendMessage(Lang.getFormattedMessage(Lang.FRIENDS_LIST_HEADER, player, headerPlaceholders));

            for (int i = startIndex; i < endIndex; i++) {
                UUID friendId = friends.get(i);
                Player friend = player.getServer().getPlayer(friendId);
                Map<String, String> itemPlaceholders = new HashMap<>();
                itemPlaceholders.put("player", (friend != null ? friend.getName() : player.getServer().getOfflinePlayer(friendId).getName()));
                player.sendMessage(Lang.getFormattedMessage(Lang.FRIEND_LIST_ITEM, player, itemPlaceholders));
            }

            if (page < totalPages) {
                Map<String, String> nextPagePlaceholders = new HashMap<>();
                nextPagePlaceholders.put("nextPage", String.valueOf(page + 1));
                player.sendMessage(Lang.getFormattedMessage(Lang.FRIENDS_LIST_NEXT_PAGE, player, nextPagePlaceholders));
            }
        });
    }
}
