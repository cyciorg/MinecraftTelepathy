package org.cyci.mc.minecrafttelepathy.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyci.mc.minecrafttelepathy.api.CommandInfo;
import org.cyci.mc.minecrafttelepathy.api.SubCommandInfo;
import org.cyci.mc.minecrafttelepathy.managers.Party;
import org.cyci.mc.minecrafttelepathy.managers.PartyManager;
import org.cyci.mc.minecrafttelepathy.lang.Lang;

import java.util.HashMap;
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
public class PartyCommand {

    private final PartyManager partyManager;

    public PartyCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @CommandInfo(name = "party", description = "Manage your party", usage = "/party <create|invite|kick|leave|disband|list>", permission = "minecrafttelepathy.party")
    public void onPartyCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.PLAYER_ONLY_COMMAND.getConfigValue(null));
            return;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(Lang.PARTY_COMMAND_USAGE.getConfigValue(player));
        }
    }

    @SubCommandInfo(name = "create", description = "Create a party", usage = "/party create", permission = "minecrafttelepathy.party.create")
    public void createParty(Player player) {
        partyManager.createParty(player.getUniqueId());
        player.sendMessage(Lang.PARTY_CREATED.getConfigValue(player));
    }

    @SubCommandInfo(name = "invite", args = {"player"}, description = "Invite a player to your party", usage = "/party invite <player>", permission = "minecrafttelepathy.party.invite")
    public void inviteToParty(Player player, String inviteeName) {
        Player invitee = player.getServer().getPlayer(inviteeName);
        if (invitee == null) {
            player.sendMessage(Lang.PLAYER_NOT_FOUND.getConfigValue(player));
            return;
        }

        Party party = partyManager.getParty(player.getUniqueId());
        if (party == null || !party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(Lang.NOT_PARTY_LEADER.getConfigValue(player));
            return;
        }

        partyManager.inviteToParty(party.getId(), player.getUniqueId(), invitee.getUniqueId());
        Map<String, String> replacements = new HashMap<>();
        replacements.put("{player}", inviteeName);
        player.sendMessage(Lang.PLAYER_INVITED_TO_PARTY.getConfigValue(player, replacements));
    }

    @SubCommandInfo(name = "accept", args = {"player"}, description = "Accept a party invite", usage = "/party accept <player>", permission = "minecrafttelepathy.party.accept")
    public void acceptPartyInvite(Player invitee, String inviterName) {
        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter == null) {
            invitee.sendMessage(Lang.PLAYER_NOT_FOUND.getConfigValue(invitee));
            return;
        }

        Party party = partyManager.getParty(inviter.getUniqueId());
        if (party == null) {
            // create this later. too tired
            invitee.sendMessage("No party found.");
            return;
        }

        partyManager.addToParty(party.getId(), invitee.getUniqueId());
        invitee.sendMessage(Lang.PLAYER_JOIN.getConfigValue(invitee));

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{player}", invitee.getName());
        inviter.sendMessage(Lang.PLAYER_JOIN.getConfigValue(inviter, replacements));
    }

    @SubCommandInfo(name = "decline", args = {"player"}, description = "Decline a party invite", usage = "/party decline <player>", permission = "minecrafttelepathy.party.decline")
    public void declinePartyInvite(Player invitee, String inviterName) {
        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter == null) {
            invitee.sendMessage(Lang.PLAYER_NOT_FOUND.getConfigValue(invitee));
            return;
        }
        // PARTY_INVITE_DECLINED (invitee)
        invitee.sendMessage("You declined the invite.");

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{player}", invitee.getName());
        // PARTY_INVITE_DECLINED (inviter) (replacements)
        inviter.sendMessage(invitee.getName() + " Declined your invite.");
    }

    @SubCommandInfo(name = "kick", args = {"player"}, description = "Kick a player from your party", usage = "/party kick <player>", permission = "minecrafttelepathy.party.kick")
    public void kickFromParty(Player player, String memberName) {
        Player member = player.getServer().getPlayer(memberName);
        if (member == null) {
            player.sendMessage(Lang.PLAYER_NOT_FOUND.getConfigValue(player));
            return;
        }

        Party party = partyManager.getParty(player.getUniqueId());
        if (party == null || !party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(Lang.NOT_PARTY_LEADER.getConfigValue(player));
            return;
        }

        partyManager.removeFromParty(party.getId(), player.getUniqueId(), member.getUniqueId());
        Map<String, String> replacements = new HashMap<>();
        replacements.put("{player}", memberName);
        player.sendMessage(Lang.PLAYER_KICKED_FROM_PARTY.getConfigValue(player, replacements));
    }

    @SubCommandInfo(name = "leave", description = "Leave your party", usage = "/party leave", permission = "minecrafttelepathy.party.leave")
    public void leaveParty(Player player) {
        Party party = partyManager.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(Lang.NOT_IN_PARTY.getConfigValue(player));
            return;
        }

        partyManager.removeFromParty(party.getId(), player.getUniqueId(), player.getUniqueId());
        player.sendMessage(Lang.PARTY_LEFT.getConfigValue(player));
    }

    @SubCommandInfo(name = "disband", description = "Disband your party", usage = "/party disband", permission = "minecrafttelepathy.party.disband")
    public void disbandParty(Player player) {
        Party party = partyManager.getParty(player.getUniqueId());
        if (party == null || !party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(Lang.NOT_PARTY_LEADER.getConfigValue(player));
            return;
        }

        partyManager.disbandParty(party.getId());
        player.sendMessage(Lang.PARTY_DISBANDED.getConfigValue(player));
    }

    @SubCommandInfo(name = "list", description = "List your party members", usage = "/party list", permission = "minecrafttelepathy.party.list")
    public void listParty(Player player) {
        Party party = partyManager.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(Lang.NOT_IN_PARTY.getConfigValue(player));
            return;
        }

        player.sendMessage(Lang.PARTY_MEMBERS_HEADER.getConfigValue(player));
        for (UUID memberId : party.getMembers()) {
            Player member = player.getServer().getPlayer(memberId);
            Map<String, String> replacements = new HashMap<>();
            replacements.put("{player}", member != null ? member.getName() : memberId.toString());
            player.sendMessage(Lang.PARTY_MEMBER.getConfigValue(player, replacements));
        }
    }
}