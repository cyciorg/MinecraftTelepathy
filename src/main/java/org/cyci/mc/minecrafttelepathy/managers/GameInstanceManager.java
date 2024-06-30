package org.cyci.mc.minecrafttelepathy.managers;

import org.bukkit.entity.Player;
import org.cyci.mc.minecrafttelepathy.Registry;
import org.cyci.mc.minecrafttelepathy.enums.GameMode;
import org.cyci.mc.minecrafttelepathy.enums.TeamColor;
import org.cyci.mc.minecrafttelepathy.utils.Logger;

import java.util.ArrayList;
import java.util.List;

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
public class GameInstanceManager {
    private final List<Player> players;
    private final List<TeamManager> teams;
    private final int minPlayersPerTeam;
    private final int maxPlayersPerTeam;
    private final int minTeams;
    private final int maxTeams;
    private final Logger logger;
    private GameMode currentMode;

    public GameInstanceManager(List<Player> players, int minPlayersPerTeam, int maxPlayersPerTeam, int minTeams, int maxTeams) {
        this.players = players;
        this.teams = new ArrayList<>();
        this.minPlayersPerTeam = minPlayersPerTeam;
        this.maxPlayersPerTeam = maxPlayersPerTeam;
        this.minTeams = minTeams;
        this.maxTeams = maxTeams;
        this.logger = Logger.getInstance(Registry.getInstance());
        this.currentMode = GameMode.LOBBY;
        initializeTeams();
    }

    private void initializeTeams() {
        int teamCount = Math.min(maxTeams, players.size() / minPlayersPerTeam);
        for (int i = 1; i <= teamCount; i++) {
            // add colors soon

            teams.add(new TeamManager(TeamColor.random(), "Team " + i));
        }
        assignPlayersToTeams();
    }

    private void assignPlayersToTeams() {
        int teamIndex = 0;
        for (Player player : players) {
            TeamManager team = teams.get(teamIndex);
            team.addPlayer(player);
            player.sendMessage("You have been assigned to " + team.getName());
            logger.info(player.getName() + " has been assigned to " + team.getName());
            teamIndex = (teamIndex + 1) % teams.size();
        }
    }

    public void startGame() {
        currentMode = GameMode.IN_GAME;
        // Additional logic to start the game
    }

    public void endGame() {
        currentMode = GameMode.END;
        // Additional logic to end the game
        Registry.getInstance().getLobbyManager().onGameEnd(this);
    }

    public List<TeamManager> getTeams() {
        return teams;
    }

    public GameMode getCurrentMode() {
        return currentMode;
    }
}