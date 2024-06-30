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
        lobbyManager = new LobbyManager(100, 2, 4, 2, 4);
        teams = new ArrayList<>();
        messagesFile = new ConfigWrapper(this, "messages.yml");

        // Register event listeners
        getServer().getPluginManager().registerEvents(new TelepathyListener(), this);

        // Register commands from within the api
        CommandHandler commandHandler = new CommandHandler(new MainCommand());
        commandHandler.registerCommand("example", this);

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

    public GameMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(GameMode mode) {
        this.currentMode = mode;
    }

    public List<TeamManager> getTeams() {
        return teams;
    }

    public void addTeam(TeamManager team) {
        teams.add(team);
    }

    public void removeTeam(TeamManager team) {
        teams.remove(team);
    }

    public TeamManager getTeamByName(String name) {
        for (TeamManager team : teams) {
            if (team.getName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }

    // Method to start lobby countdown using CountdownTimer
    public void startLobbyCountdown() {
        if (currentMode == GameMode.LOBBY) {
            currentMode = GameMode.STARTING;

            Runnable beforeTimer = () -> Bukkit.broadcastMessage(Lang.PREFIX.getConfigValue() + " Lobby countdown started! Game will start in " + lobbyCountdownTimer.getTotalSeconds() + " seconds.");

            Consumer<CountdownTimer> everySecond = timer -> Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(Lang.PREFIX.getConfigValue() + " Game starting in " + timer.getSecondsLeft() + " seconds..."));

            // Start the game when countdown ends
            Runnable afterTimer = this::startGame;

            lobbyCountdownTimer = new CountdownTimer(this, 60, beforeTimer, afterTimer, everySecond);
            lobbyCountdownTimer.scheduleTimer();
        }
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

    // Method to start the game
    private void startGame() {
        // Transition to IN_GAME mode, initialize game logic, etc.
        currentMode = GameMode.IN_GAME;
        Bukkit.broadcastMessage(Lang.PREFIX.getConfigValue() + " Game started! Good luck!");

        // Implement game start logic here
    }

    // Utility method to handle player joins during lobby phase
    public void handlePlayerJoin(Player player) {
        if (currentMode == GameMode.LOBBY || currentMode == GameMode.WAITING) {
            player.sendMessage(Lang.PREFIX.getConfigValue() + " Welcome to Minecraft Telepathy!");
        } else {
            player.sendMessage(Lang.PREFIX.getConfigValue() + " The game is already in progress.");
        }
    }

    // Utility method to handle player leaves during lobby phase
    public void handlePlayerLeave(Player player) {
        if (currentMode == GameMode.LOBBY || currentMode == GameMode.WAITING) {
            player.sendMessage(Lang.PREFIX.getConfigValue() + " You left Minecraft Telepathy lobby.");
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