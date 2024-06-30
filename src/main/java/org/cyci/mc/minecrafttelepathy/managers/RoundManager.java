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

    public RoundManager(JavaPlugin plugin, int totalRounds, int maxTeams) {
        this.plugin = plugin;
        this.totalRounds = totalRounds;
        this.maxTeams = maxTeams;
        this.themes = ThemeManager.getThemeNames();
        this.currentRound = 0;
        this.currentMode = GameMode.LOBBY;
        this.teams = new ArrayList<>();
        initializeTeams();
    }

    private void initializeTeams() {
        for (int i = 1; i <= maxTeams; i++) {
            String color = "Team Color " + i;
            String name = "Team Name " + i;
            teams.add(new TeamManager(TeamColor.random(), name));
        }
    }

    public void assignPlayerToTeam(Player player) {
        for (TeamManager team : teams) {
            if (team.getPlayers().size() < 4) {
                team.addPlayer(player);
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{team}", team.getName());
                player.sendMessage(Lang.TEAM_ASSIGN.getConfigValue(player, replacements));
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "All teams are full!");
    }

    public void startGame() {
        currentRound = 0;
        currentMode = GameMode.IN_GAME;
        startNextRound();
    }

    private void startNextRound() {
        if (currentRound >= totalRounds) {
            endGame();
            return;
        }

        currentThemeBlock = getCurrentThemeBlock();
        currentRound++;

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{round}", String.valueOf(currentRound));
        replacements.put("{theme}", getCurrentTheme());
        broadcastMessageToAll(Lang.ROUND_START, replacements);

        roundTimer = new CountdownTimer(plugin, 90,
                () -> {
                    // Before Timer starts (optional)
                },
                this::endCurrentRound,
                (timer) -> {
                    // Every second action (optional)
                }
        );
        roundTimer.scheduleTimer();
    }

    private void endCurrentRound() {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("{round}", String.valueOf(currentRound));
        broadcastMessageToAll(Lang.ROUND_END, replacements);
        // Handle end of round logic, such as checking if players have placed the correct blocks
        checkRoundResults();
        startNextRound();
    }

    private void checkRoundResults() {
        // Implement logic to check if players have placed the correct blocks and award points to teams
        for (TeamManager team : teams) {
            boolean allCorrect = true;
            for (Player player : team.getPlayers()) {
                // Check if the player placed the correct block (simplified example)
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

    private void endGame() {
        currentMode = GameMode.END;
        broadcastMessageToAll(Lang.GAME_END, null);
        announceWinners();
    }

    private void announceWinners() {
        TeamManager winningTeam = teams.stream().max(Comparator.comparingInt(TeamManager::getPoints)).orElse(null);
        if (winningTeam != null) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("{team}", winningTeam.getName());
            replacements.put("{points}", String.valueOf(winningTeam.getPoints()));
            broadcastMessageToAll(Lang.WINNING_TEAM, replacements);
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

    private Material getCurrentThemeBlock() {
        String currentTheme = getCurrentTheme();
        return ThemeManager.getRandomBlockForTheme(currentTheme);
    }

    private void broadcastMessageToAll(Lang lang, Map<String, String> replacements) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(lang.getConfigValue(player, replacements));
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