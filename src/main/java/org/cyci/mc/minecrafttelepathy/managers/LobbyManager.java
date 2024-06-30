package org.cyci.mc.minecrafttelepathy.managers;

import org.bukkit.entity.Player;
import org.cyci.mc.minecrafttelepathy.Registry;
import org.cyci.mc.minecrafttelepathy.utils.Logger;

import java.util.*;

/**
 * org.cyci.mc.minecrafttelepathy.managers
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sun - June/Sun/2024
 */
public class LobbyManager {
    private final Map<UUID, Player> playerQueue;
    private final List<GameInstanceManager> activeGames;
    private final int maxPlayers;
    private final int minPlayersPerTeam;
    private final int maxPlayersPerTeam;
    private final int minTeams;
    private final int maxTeams;
    private final Logger logger;

    public LobbyManager(int maxPlayers, int minPlayersPerTeam, int maxPlayersPerTeam, int minTeams, int maxTeams) {
        this.playerQueue = (Map<UUID, Player>) new LinkedList<>();
        this.activeGames = new ArrayList<>();
        this.maxPlayers = maxPlayers;
        this.minPlayersPerTeam = minPlayersPerTeam;
        this.maxPlayersPerTeam = maxPlayersPerTeam;
        this.minTeams = minTeams;
        this.maxTeams = maxTeams;
        this.logger = Logger.getInstance(Registry.getInstance());
    }

    public void addPlayerToQueue(Player player) {
        playerQueue.put(player.getUniqueId(), player);
        player.sendMessage("You have been added to the queue.");
        logger.info(player.getName() + " has been added to the queue.");
        tryStartGame();
    }

    public void removePlayerFromQueue(Player player) {
        playerQueue.remove(player.getUniqueId());
        player.sendMessage("You have been removed from the queue.");
        logger.info(player.getName() + " has been removed from the queue.");
    }

    private void tryStartGame() {
        if (playerQueue.size() >= minTeams * minPlayersPerTeam) {
            List<Player> playersToStartGame = new ArrayList<>();
            for (UUID playerId : playerQueue.keySet()) {
                playersToStartGame.add(playerQueue.remove(playerId));
                if (playersToStartGame.size() >= maxPlayers) {
                    break;
                }
            }
            GameInstanceManager gameInstance = new GameInstanceManager(playersToStartGame, minPlayersPerTeam, maxPlayersPerTeam, minTeams, maxTeams);
            activeGames.add(gameInstance);
            gameInstance.startGame();
            logger.info("New game instance started with " + playersToStartGame.size() + " players.");
        }
    }

    public void onGameEnd(GameInstanceManager gameInstance) {
        activeGames.remove(gameInstance);
        logger.info("Game instance ended.");
    }

    public List<GameInstanceManager> getActiveGames() {
        return activeGames;
    }

    public int getQueueSize() {
        return playerQueue.size();
    }
}