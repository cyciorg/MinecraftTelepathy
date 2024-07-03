package org.cyci.mc.minecrafttelepathy.managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyci.mc.minecrafttelepathy.enums.GameMode;
import org.cyci.mc.minecrafttelepathy.enums.TeamColor;
import org.cyci.mc.minecrafttelepathy.lang.Lang;
import org.cyci.mc.minecrafttelepathy.themes.ThemeManager;
import org.cyci.mc.minecrafttelepathy.utils.C;
import org.cyci.mc.minecrafttelepathy.utils.CountdownTimer;

import java.util.*;

/**
 * org.cyci.mc.minecrafttelepathy.managers
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sat - June/Sat/2024
 */
public class RoundManager {

    private final JavaPlugin plugin;
    private final List<String> themes;
    private final int totalRounds;
    private int currentRound;
    private GameMode currentMode;
    private Material currentThemeBlock;
    private CountdownTimer roundTimer;
    private final List<TeamManager> teams;
    private final int maxTeams;
    private final Queue<List<TeamManager>> readyTeamSets;

    public RoundManager(JavaPlugin plugin, int totalRounds, int maxTeams) {
        this.plugin = plugin;
        this.totalRounds = totalRounds;
        this.maxTeams = maxTeams;
        this.themes = ThemeManager.getThemeNames();
        this.currentRound = 0;
        this.currentMode = GameMode.LOBBY;
        this.teams = new ArrayList<>();
        this.readyTeamSets = new LinkedList<>();
        initializeTeams();
    }

    private void initializeTeams() {
        for (int i = 0; i < maxTeams; i++) {
            TeamColor team = TeamColor.random();
            String color = team.getColoredName();
            String name = "Team: " + team.getId();
            teams.add(new TeamManager(color, name));
        }
    }

    public void assignPlayerToTeam(Player player) {
        for (TeamManager team : teams) {
            if (team.getPlayers().size() < 4) {
                team.addPlayer(player);
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{team}", team.getName());
                player.sendMessage(Lang.TEAM_ASSIGN.getConfigValue(player, replacements));

                if (team.getPlayers().size() == 4) {
                    checkTeamsReady();
                }
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "All teams are full!");
    }

    private void checkTeamsReady() {
        List<TeamManager> readyTeams = new ArrayList<>();
        for (TeamManager team : teams) {
            if (team.getPlayers().size() == 4) {
                readyTeams.add(team);
                if (readyTeams.size() == 4) {
                    readyTeamSets.add(new ArrayList<>(readyTeams));
                    readyTeams.clear();
                }
            }
        }
        if (!readyTeamSets.isEmpty()) {
            startGameForReadyTeams();
        }
    }

    private void startGameForReadyTeams() {
        if (currentMode == GameMode.LOBBY && !readyTeamSets.isEmpty()) {
            List<TeamManager> startingTeams = readyTeamSets.poll();
            if (startingTeams != null) {
                currentRound = 0;
                currentMode = GameMode.IN_GAME;
                startNextRound(startingTeams);
            }
        }
    }

    public void startNextRound(List<TeamManager> startingTeams) {
        if (currentRound >= totalRounds) {
            endGame(startingTeams);
            return;
        }

        currentThemeBlock = getCurrentThemeBlock();
        currentRound++;

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{round}", String.valueOf(currentRound));
        replacements.put("{theme}", getCurrentTheme());
        broadcastMessageToTeams(startingTeams, Lang.ROUND_START, replacements);

        roundTimer = new CountdownTimer(plugin, 90,
                () -> {
                    // Before Timer starts (optional)
                },
                () -> endCurrentRound(startingTeams),
                (timer) -> {
                    // Every second action (optional)
                }
        );
        roundTimer.scheduleTimer();
    }

    private void endCurrentRound(List<TeamManager> startingTeams) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("{round}", String.valueOf(currentRound));
        broadcastMessageToTeams(startingTeams, Lang.ROUND_END, replacements);
        checkRoundResults(startingTeams);
        startNextRound(startingTeams);
    }

    private void checkRoundResults(List<TeamManager> startingTeams) {
        for (TeamManager team : startingTeams) {
            boolean allCorrect = true;
            for (Player player : team.getPlayers()) {
                if (player.getLocation().getBlock().getType() != currentThemeBlock) {
                    allCorrect = false;
                    break;
                }
            }
            if (allCorrect) {
                team.addPoints(1); // Example: 1 point per correct round
                broadcastMessageToTeam(team, Lang.TEAM_POINT, null);
            }
        }
    }

    private void endGame(List<TeamManager> startingTeams) {
        currentMode = GameMode.END;
        broadcastMessageToTeams(startingTeams, Lang.GAME_END, null);
        announceWinners(startingTeams);
        startGameForReadyTeams();
    }

    private void announceWinners(List<TeamManager> startingTeams) {
        TeamManager winningTeam = startingTeams.stream().max(Comparator.comparingInt(TeamManager::getPoints)).orElse(null);
        if (winningTeam != null) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("{team}", winningTeam.getName());
            replacements.put("{points}", String.valueOf(winningTeam.getPoints()));
            broadcastMessageToTeams(startingTeams, Lang.WINNING_TEAM, replacements);
        }
    }

    public void cancelCurrentRound() {
        if (roundTimer != null && roundTimer.isRunning()) {
            roundTimer.cancel();
            Map<String, String> replacements = new HashMap<>();
            replacements.put("{round}", String.valueOf(currentRound));
            broadcastMessageToAll(Lang.ROUND_CANCEL, replacements);
        }
    }

    public void resetGame() {
        currentRound = 0;
        currentMode = GameMode.LOBBY;
        for (TeamManager team : teams) {
            team.getPlayers().clear();
            team.addPoints(-team.getPoints()); // Reset points
        }
        broadcastMessageToAll(Lang.GAME_RESET, null);
    }

    public GameMode getCurrentMode() {
        return currentMode;
    }

    public String getCurrentTheme() {
        if (themes.isEmpty()) {
            return "Default"; // Fallback if no themes are available
        }
        return themes.get(currentRound % themes.size()); // Cycle through themes
    }

    public Material getCurrentThemeBlock() {
        if (currentThemeBlock == null) {
            String currentTheme = getCurrentTheme();
            currentThemeBlock = ThemeManager.getRandomBlockForTheme(currentTheme);
        }
        return currentThemeBlock;
    }

    public void resetCurrentThemeBlock() {
        currentThemeBlock = null;
    }

    private void broadcastMessageToAll(Lang lang, Map<String, String> replacements) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(lang.getConfigValue(player, replacements));
        }
    }

    private void broadcastMessageToTeams(List<TeamManager> teams, Lang lang, Map<String, String> replacements) {
        for (TeamManager team : teams) {
            broadcastMessageToTeam(team, lang, replacements);
        }
    }

    private void broadcastMessageToTeam(TeamManager team, Lang lang, Map<String, String> replacements) {
        for (Player player : team.getPlayers()) {
            player.sendMessage(lang.getConfigValue(player, replacements));
        }
    }

    public List<TeamManager> getTeams() {
        return teams;
    }
}