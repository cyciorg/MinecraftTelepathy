package org.cyci.mc.minecrafttelepathy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyci.mc.minecrafttelepathy.commandhandler.CommandHandler;
import org.cyci.mc.minecrafttelepathy.commands.MainCommand;
import org.cyci.mc.minecrafttelepathy.enums.GameMode;
import org.cyci.mc.minecrafttelepathy.lang.Lang;
import org.cyci.mc.minecrafttelepathy.listeners.TelepathyListener;
import org.cyci.mc.minecrafttelepathy.managers.LobbyManager;
import org.cyci.mc.minecrafttelepathy.managers.MySQLManager;
import org.cyci.mc.minecrafttelepathy.managers.TeamManager;
import org.cyci.mc.minecrafttelepathy.themes.ThemeManager;
import org.cyci.mc.minecrafttelepathy.utils.ConfigWrapper;
import org.cyci.mc.minecrafttelepathy.utils.CountdownTimer;
import org.cyci.mc.minecrafttelepathy.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Registry extends JavaPlugin {

    private static Registry instance;
    private GameMode currentMode;
    private CountdownTimer lobbyCountdownTimer;
    private ConfigWrapper messagesFile;
    private List<TeamManager> teams;
    private LobbyManager lobbyManager;
    private Logger logger;
    private MySQLManager mysqlManager;

    @Override
    public void onEnable() {
        instance = this;
        currentMode = GameMode.LOBBY; // Initialize with LOBBY mode

        ThemeManager.initialize(this);
        // Will check the max players allowed to play from a config
        lobbyManager = new LobbyManager(100, 2, 4, 2, 4);
        teams = new ArrayList<>();
        messagesFile = new ConfigWrapper(this, "messages.yml");

        // Register event listeners
        getServer().getPluginManager().registerEvents(new TelepathyListener(), this);

        // Register commands from within the api
        CommandHandler commandHandler = new CommandHandler(new MainCommand());
        commandHandler.registerCommand("telepathy", this);

        // Load messages from config
        loadMessages();

        //if (isMySQLConfigValid(mysqlHost, mysqlPort, mysqlDatabase, mysqlUsername, mysqlPassword)) {
            //mysqlManager = new MySQLManager(mysqlHost, mysqlPort, mysqlDatabase, mysqlUsername, mysqlPassword);

            mysqlManager.connectAsync().thenRun(() -> {
                getLogger().info("Connected to MySQL!");
                //playerTimeTracker = new PlayerTimeTracker(this.mysqlManager.getDataSource());
                try {
//                    Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
//                    Bukkit.getServer().getPluginManager().registerEvents(new PlayerLeave(), this);
//                    Bukkit.getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
//                    Bukkit.getServer().getPluginManager().registerEvents(new InventoryMoveItemEvent(), this);
                } catch (Exception e) {
                    getLogger().info(e.getMessage());
                } finally {
                    getLogger().info("Registered the events");
                }

                int updateInterval = 1200;
                //new PlaytimeUpdaterTask().runTaskTimer(this, 0, updateInterval);
            }).exceptionally(e -> {
                getLogger().severe("Error: " + e.getMessage());
                e.printStackTrace();
                getLogger().severe("Disabling the plugin due to MySQL connection failure...");
                getServer().getPluginManager().disablePlugin(this);
                return null;
            });
        //} else {
        //    getLogger().severe("MySQL configuration is not valid. Please provide correct MySQL information in your config.yml.");
        //    getLogger().severe("Disabling the plugin...");
        //    getServer().getPluginManager().disablePlugin(this);
        //}


        getLogger().info("Minecraft Telepathy has been enabled!");
    }

    @Override
    public void onDisable() {
        stopLobbyCountdown(); // Ensure lobby countdown is stopped on plugin disable
        getLogger().info("Minecraft Telepathy has been disabled.");
    }

    public static Registry getInstance() {
        return instance;
    }

    // Method to stop lobby countdown
    public void stopLobbyCountdown() {
        if (lobbyCountdownTimer != null && lobbyCountdownTimer.isRunning()) {
            lobbyCountdownTimer.cancel();
            lobbyCountdownTimer = null;
            currentMode = GameMode.LOBBY;
            Bukkit.broadcastMessage(Lang.PREFIX.getConfigValue() + " Lobby countdown stopped.");
        }
    }

    public void loadMessages() {
        Lang.setFile(this.messagesFile.getConfig());
        for (Lang value : Lang.values())
            this.messagesFile.getConfig().addDefault(value.getPath(), value.getDefault());
        this.messagesFile.getConfig().options().copyDefaults(true);
        this.messagesFile.saveConfig();
    }

    public TeamManager[] getTeamManagers() {
        return teams.toArray(new TeamManager[0]);
    }

    public LobbyManager getLobbyManager() {
        return this.lobbyManager;
    }

    public MySQLManager getMySQLManager() {
        return this.mysqlManager;
    }
}