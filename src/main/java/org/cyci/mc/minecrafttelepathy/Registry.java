package org.cyci.mc.minecrafttelepathy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyci.mc.minecrafttelepathy.commandhandler.CommandHandler;
import org.cyci.mc.minecrafttelepathy.commands.MainCommand;
import org.cyci.mc.minecrafttelepathy.commands.PartyCommand;
import org.cyci.mc.minecrafttelepathy.commands.FriendsCommand;
import org.cyci.mc.minecrafttelepathy.enums.GameMode;
import org.cyci.mc.minecrafttelepathy.lang.Lang;
import org.cyci.mc.minecrafttelepathy.listeners.TelepathyListener;
import org.cyci.mc.minecrafttelepathy.managers.*;
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
    private RoundManager roundManager;
    private Logger logger;
    private MySQLManager mysqlManager;
    private FriendManager friendManager;
    private PartyManager partyManager;
    private CommandHandler commandHandler;

    @Override
    public void onEnable() {
        instance = this;
        currentMode = GameMode.LOBBY; // Initialize with LOBBY mode

        // Initialize ThemeManager
        ThemeManager.initialize(this);

        // Initialize RoundManager with totalRounds and maxTeams
        roundManager = new RoundManager(this, 10, 4);
        teams = roundManager.getTeams(); // Get the list of teams from RoundManager
        messagesFile = new ConfigWrapper(this, "messages.yml");

        // Register event listeners
        getServer().getPluginManager().registerEvents(new TelepathyListener(), this);

        // Set up managers
        this.mysqlManager = new MySQLManager("host", 3306, "database", "username", "password");
        mysqlManager.connectAsync().join(); // Synchronously wait for the database connection
        this.friendManager = new FriendManager(mysqlManager, logger);
        this.partyManager = new PartyManager(mysqlManager, logger);

        // Register commands from within the api
        commandHandler = new CommandHandler(
                new FriendsCommand(friendManager),
                new PartyCommand(partyManager),
                new MainCommand()
        );
        commandHandler.registerCommand("telepathy", this);
        commandHandler.registerCommand("friend", this);
        commandHandler.registerCommand("party", this);

        // Load messages from config
        loadMessages();

        getLogger().info("Minecraft Telepathy has been enabled!");
    }

    @Override
    public void onDisable() {
        stopLobbyCountdown(); // Ensure lobby countdown is stopped on plugin disable
        roundManager.resetGame(); // Ensure game is reset on plugin disable
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

    public RoundManager getRoundManager() {
        return this.roundManager;
    }

    public MySQLManager getMySQLManager() {
        return this.mysqlManager;
    }
}