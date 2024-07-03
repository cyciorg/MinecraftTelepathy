package org.cyci.mc.minecrafttelepathy.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.cyci.mc.minecrafttelepathy.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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
public class PartyManager {

    private final Map<UUID, Party> partyMap;
    private final Map<UUID, UUID> playerPartyMap; // Maps player UUIDs to party UUIDs
    private final MySQLManager db;
    private final Logger logger;

    public PartyManager(MySQLManager db, Logger logger) {
        this.db = db;
        this.partyMap = new HashMap<>();
        this.playerPartyMap = new HashMap<>();
        this.logger = logger;
        loadPartiesFromDatabase();
    }

    private void loadPartiesFromDatabase() {
        try (Connection conn = db.getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM parties");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UUID partyId = UUID.fromString(resultSet.getString("id"));
                UUID leader = UUID.fromString(resultSet.getString("leader"));
                String[] membersArray = resultSet.getString("members").split(",");
                Set<UUID> members = new HashSet<>();
                for (String member : membersArray) {
                    members.add(UUID.fromString(member));
                }

                Party party = new Party(leader);
                party.getMembers().addAll(members);
                partyMap.put(partyId, party);

                for (UUID member : members) {
                    playerPartyMap.put(member, partyId);
                }
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public void createParty(UUID leader) {
        if (playerPartyMap.containsKey(leader)) {
            // Player is already in a party
            return;
        }
        Party party = new Party(leader);
        partyMap.put(party.getId(), party);
        playerPartyMap.put(leader, party.getId());

        // Save party to database
        savePartyToDatabase(party);
    }

    private void savePartyToDatabase(Party party) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = db.getConnection();
                 PreparedStatement statement = conn.prepareStatement(
                         "INSERT INTO parties (id, leader, members) VALUES (?, ?, ?)")) {
                statement.setString(1, party.getId().toString());
                statement.setString(2, party.getLeader().toString());
                statement.setString(3, String.join(",", party.getMembers().stream().map(UUID::toString).toArray(String[]::new)));
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        });
    }

    private void updatePartyInDatabase(Party party) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = db.getConnection();
                 PreparedStatement statement = conn.prepareStatement(
                         "UPDATE parties SET members = ? WHERE id = ?")) {
                statement.setString(1, String.join(",", party.getMembers().stream().map(UUID::toString).toArray(String[]::new)));
                statement.setString(2, party.getId().toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        });
    }

    private void removePartyFromDatabase(UUID partyId) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = db.getConnection();
                 PreparedStatement statement = conn.prepareStatement("DELETE FROM parties WHERE id = ?")) {
                statement.setString(1, partyId.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        });
    }

    public Party getParty(UUID player) {
        UUID partyId = playerPartyMap.get(player);
        return partyMap.get(partyId);
    }

    public void inviteToParty(UUID partyId, UUID inviter, UUID invitee) {
        Party party = partyMap.get(partyId);
        if (party == null || !party.getLeader().equals(inviter)) {
            // Party does not exist or inviter is not the leader
            return;
        }
        if (party.isMember(invitee)) {
            // Invitee is already in the party
            return;
        }

        // Add invitee to party
        party.addMember(invitee);
        playerPartyMap.put(invitee, partyId);

        // Update party in database
        updatePartyInDatabase(party);
    }

    public void removeFromParty(UUID partyId, UUID remover, UUID member) {
        Party party = partyMap.get(partyId);
        if (party == null || !party.getLeader().equals(remover) && !remover.equals(member)) {
            // Party does not exist, remover is not the leader, and member is not removing themselves
            return;
        }
        if (!party.isMember(member)) {
            // Member is not in the party
            return;
        }

        // Remove member from party
        party.removeMember(member);
        playerPartyMap.remove(member);

        // Update party in database
        updatePartyInDatabase(party);
    }

    public void disbandParty(UUID partyId) {
        Party party = partyMap.remove(partyId);
        if (party == null) {
            // Party does not exist
            return;
        }
        for (UUID member : party.getMembers()) {
            playerPartyMap.remove(member);
        }

        // Remove party from database
        removePartyFromDatabase(partyId);
    }

    // work on this
    public void addToParty(UUID partyId, Player partyLeader, Player invitee) {
        Party party = partyMap.get(partyId);
        if (party == null || !party.getLeader().equals(invitee.getUniqueId())) {
            // Party does not exist, invitee is not the leader
            return;
        }
        if (!party.isMember(invitee.getUniqueId())) {
            // Member is not in the party
            return;
        }
        // Create interactive message for the invitee
        Component acceptButton = Component.text("[Accept]")
                .color(TextColor.color(0x00FF00))
                .clickEvent(ClickEvent.runCommand("/party accept " + partyLeader.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Click to accept the party invite")));

        Component declineButton = Component.text("[Decline]")
                .color(TextColor.color(0xFF0000))
                .clickEvent(ClickEvent.runCommand("/party decline " + partyLeader.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Click to decline the party invite")));

        Component message = Component.text(partyLeader.getName() + " has invited you to join their party. ")
                .append(acceptButton)
                .append(Component.text(" "))
                .append(declineButton);

        invitee.sendMessage(message);
    }
}
