package org.cyci.mc.minecrafttelepathy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyci.mc.minecrafttelepathy.api.CommandInfo;
import org.cyci.mc.minecrafttelepathy.api.SubCommandInfo;
import org.cyci.mc.minecrafttelepathy.managers.Party;
import org.cyci.mc.minecrafttelepathy.managers.PartyManager;

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
public class PartyCommand {

    private final PartyManager partyManager;

    public PartyCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @CommandInfo(name = "party", description = "Manage your party", usage = "/party <create|invite|kick|leave|disband|list>", permission = "minecrafttelepathy.party")
    public void onPartyCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("Usage: /party <create|invite|kick|leave|disband|list>");
        }
    }

    @SubCommandInfo(name = "create", description = "Create a party", usage = "/party create", permission = "minecrafttelepathy.party.create")
    public void createParty(Player player) {
        partyManager.createParty(player.getUniqueId());
        player.sendMessage("Party created.");
    }

    @SubCommandInfo(name = "invite", args = {"player"}, description = "Invite a player to your party", usage = "/party invite <player>", permission = "minecrafttelepathy.party.invite")
    public void inviteToParty(Player player, String inviteeName) {
        Player invitee = player.getServer().getPlayer(inviteeName);
        if (invitee == null) {
            player.sendMessage("Player not found.");
            return;
        }

        Party party = partyManager.getParty(player.getUniqueId());
        if (party == null || !party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("You are not the leader of a party.");
            return;
        }

        partyManager.inviteToParty(party.getId(), player.getUniqueId(), invitee.getUniqueId());
        player.sendMessage(inviteeName + " has been invited to your party.");
    }

    @SubCommandInfo(name = "kick", args = {"player"}, description = "Kick a player from your party", usage = "/party kick <player>", permission = "minecrafttelepathy.party.kick")
    public void kickFromParty(Player player, String memberName) {
        Player member = player.getServer().getPlayer(memberName);
        if (member == null) {
            player.sendMessage("Player not found.");
            return;
        }

        Party party = partyManager.getParty(player.getUniqueId());
        if (party == null || !party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("You are not the leader of a party.");
            return;
        }

        partyManager.removeFromParty(party.getId(), player.getUniqueId(), member.getUniqueId());
        player.sendMessage(memberName + " has been kicked from the party.");
    }

    @SubCommandInfo(name = "leave", description = "Leave your party", usage = "/party leave", permission = "minecrafttelepathy.party.leave")
    public void leaveParty(Player player) {
        Party party = partyManager.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage("You are not in a party.");
            return;
        }

        partyManager.removeFromParty(party.getId(), player.getUniqueId(), player.getUniqueId());
        player.sendMessage("You have left the party.");
    }

    @SubCommandInfo(name = "disband", description = "Disband your party", usage = "/party disband", permission = "minecrafttelepathy.party.disband")
    public void disbandParty(Player player) {
        Party party = partyManager.getParty(player.getUniqueId());
        if (party == null || !party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("You are not the leader of a party.");
            return;
        }

        partyManager.disbandParty(party.getId());
        player.sendMessage("Party disbanded.");
    }

    @SubCommandInfo(name = "list", description = "List your party members", usage = "/party list", permission = "minecrafttelepathy.party.list")
    public void listParty(Player player) {
        Party party = partyManager.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage("You are not in a party.");
            return;
        }

        player.sendMessage("Your party members:");
        for (UUID memberId : party.getMembers()) {
            Player member = player.getServer().getPlayer(memberId);
            if (member != null) {
                player.sendMessage("- " + member.getName());
            } else {
                player.sendMessage("- " + memberId.toString());
            }
        }
    }
}