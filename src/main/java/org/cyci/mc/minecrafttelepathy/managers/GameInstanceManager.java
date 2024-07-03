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
    private final Logger logger;
    private GameMode currentMode;

    public GameInstanceManager(List<Player> players) {
        this.players = players;
        this.teams = new ArrayList<>();

        this.logger = Logger.getInstance(Registry.getInstance());
        this.currentMode = GameMode.LOBBY;
        initializeTeams();
    }

    private void initializeTeams() {
        TeamColor team = TeamColor.random();
        String color = team.getColoredName();
        String name = "Team: " + team.getId();
        teams.add(new TeamManager(color, name));
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
        Registry.getInstance().getRoundManager().resetGame();
    }

    public List<TeamManager> getTeams() {
        return teams;
    }

    public GameMode getCurrentMode() {
        return currentMode;
    }
}