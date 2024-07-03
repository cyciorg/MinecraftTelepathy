package org.cyci.mc.minecrafttelepathy.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.cyci.mc.minecrafttelepathy.Registry;
import org.cyci.mc.minecrafttelepathy.lang.Lang;
import org.cyci.mc.minecrafttelepathy.managers.MySQLManager;
import org.cyci.mc.minecrafttelepathy.managers.RoundManager;
import org.cyci.mc.minecrafttelepathy.trackers.PlayerTimeTracker;
import org.cyci.mc.minecrafttelepathy.utils.Logger;

/**
 * org.cyci.mc.minecrafttelepathy.listeners
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sun - June/Sun/2024
 */
public class PlayerListener implements Listener {
    private final RoundManager roundManager;
    private final MySQLManager mySQLManager;
    private final Logger logger;
    private final PlayerTimeTracker playerTimeTracker;

    public PlayerListener() {
        this.roundManager = Registry.getInstance().getRoundManager();
        this.mySQLManager = Registry.getInstance().getMySQLManager();
        this.logger = Logger.getInstance(Registry.getInstance());
        this.playerTimeTracker = new PlayerTimeTracker(mySQLManager.getDataSource());
    }

//    @EventHandler
//    public void onPlayerJoin(PlayerJoinEvent event) {
//        playerTimeTracker.addPlayerIfNotExists(event.getPlayer());
//        playerTimeTracker.recordLogin(event.getPlayer().getUniqueId().toString());
//
//        event.getPlayer().sendMessage(Lang.PLAYER_JOIN.getConfigValue(event.getPlayer(), "{player}", event.getPlayer().getName()));
//        roundManager.
//    }
//
//    @EventHandler
//    public void onPlayerQuit(PlayerQuitEvent event) {
//        lobbyManager.removePlayerFromQueue(event.getPlayer());
//        event.getPlayer().sendMessage(Lang.PLAYER_LEAVE.getConfigValue(event.getPlayer(), "{player}", event.getPlayer().getName()));
//    }

    @EventHandler
    public void onPlayerStatisticIncrement(PlayerStatisticIncrementEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();

        switch (event.getStatistic()) {
            case PLAY_ONE_MINUTE:
                playerTimeTracker.recordPlaytime(playerUUID, 1);
                break;
            default:
                break;
        }
    }
}
