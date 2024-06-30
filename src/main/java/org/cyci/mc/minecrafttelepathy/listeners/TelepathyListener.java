package org.cyci.mc.minecrafttelepathy.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.cyci.mc.minecrafttelepathy.Registry;
import org.cyci.mc.minecrafttelepathy.enums.GameMode;
import org.cyci.mc.minecrafttelepathy.lang.Lang;
import org.cyci.mc.minecrafttelepathy.managers.TeamManager;
import org.cyci.mc.minecrafttelepathy.themes.ThemeManager;
import org.cyci.mc.minecrafttelepathy.utils.C;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TelepathyListener implements Listener {

    private static final Map<UUID, Material> playerPlacedBlocks = new HashMap<>();
    private final GameMode gameMode = GameMode.IN_GAME; // Adjust this based on your game's logic
    private Material currentThemeBlock;

    private final Map<String, ProtectedRegion> gameMaps;

    public TelepathyListener() {
        this.gameMaps = loadGameMaps();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (gameMode != GameMode.IN_GAME) {
            player.sendMessage(ChatColor.RED + "The game is not currently in play!");
            return;
        }

        if (!isBlockInAnyGameMap(event.getBlock())) {
            player.sendMessage(ChatColor.RED + "You can only place blocks within the game map!");
            return;
        }

        if (currentThemeBlock == null) {
            currentThemeBlock = getCurrentThemeBlock(); // Get a random block for the current theme
        }

        // Store the block placed by the player
        playerPlacedBlocks.put(player.getUniqueId(), event.getBlockPlaced().getType());

        // Check if all players in each team have placed the correct block
        checkAllTeams();

        // Reset the current theme block for the next round
        if (playerPlacedBlocks.isEmpty()) {
            currentThemeBlock = null;
        }
    }

    private boolean isBlockInAnyGameMap(Block block) {
        for (ProtectedRegion region : gameMaps.values()) {
            if (region.contains(block.getX(), block.getY(), block.getZ())) {
                return true;
            }
        }
        return false;
    }

    private Map<String, ProtectedRegion> loadGameMaps() {
        Map<String, ProtectedRegion> maps = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            if (regionManager != null) {
                maps.putAll(regionManager.getRegions());
            }
        }
        return maps;
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want to throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }

    private void checkAllTeams() {
        Map<String, Boolean> teamMatches = new HashMap<>();

        for (TeamManager team : Registry.getInstance().getTeamManagers()) {
            boolean allMatch = true;
            Material firstBlock = null;

            for (Player player : team.getPlayers()) {
                Material placedBlock = playerPlacedBlocks.get(player.getUniqueId());

                if (firstBlock == null) {
                    firstBlock = placedBlock;
                } else if (firstBlock != placedBlock) {
                    allMatch = false;
                    break;
                }
            }

            if (allMatch) {
                teamMatches.put(team.getName(), true);
                team.addPoints(1);
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{team}", team.getName());
                team.getPlayers().forEach(p -> p.sendMessage(Lang.TEAM_POINT.getConfigValue(null, replacements)));
            } else {
                teamMatches.put(team.getName(), false);
            }
        }

        if (teamMatches.values().stream().allMatch(match -> match)) {
            broadcastMessageToAll(Lang.ROUND_END, null);
            playerPlacedBlocks.clear(); // Reset for the next round
        }
    }

    private String getCurrentTheme() {
        // Implement logic to retrieve current theme name
        // Example: Return a theme name based on game state or round number
        return "Forest"; // Default example
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
}