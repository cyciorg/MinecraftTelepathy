package org.cyci.mc.minecrafttelepathy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyci.mc.minecrafttelepathy.api.CommandInfo;
import org.cyci.mc.minecrafttelepathy.api.SubCommandInfo;
import org.cyci.mc.minecrafttelepathy.managers.FriendManager;

import java.util.List;
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

    @SubCommandInfo(name = "list", description = "List your friends", usage = "/friend list", permission = "minecrafttelepathy.friend.list")
    public void listFriends(Player player) {
        List<UUID> friends = friendManager.getFriends(player.getUniqueId());
        if (friends.isEmpty()) {
            player.sendMessage("You have no friends.");
            return;
        }

        player.sendMessage("Your friends:");
        for (UUID friendId : friends) {
            Player friend = player.getServer().getPlayer(friendId);
            if (friend != null) {
                player.sendMessage("- " + friend.getName());
            } else {
                player.sendMessage("- " + friendId.toString());
            }
        }
    }
}
