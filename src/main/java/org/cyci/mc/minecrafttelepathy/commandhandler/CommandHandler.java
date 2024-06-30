package org.cyci.mc.minecrafttelepathy.commandhandler;

import jdk.jfr.internal.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyci.mc.minecrafttelepathy.Registry;
import org.cyci.mc.minecrafttelepathy.api.CommandInfo;
import org.cyci.mc.minecrafttelepathy.api.SubCommandInfo;
import org.cyci.mc.minecrafttelepathy.enums.LogLevel;

import java.lang.reflect.Method;
import java.util.*;

/**
 * org.cyci.mc.minecrafttelepathy.commandhandler
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sun - June/Sun/2024
 */
public class CommandHandler implements CommandExecutor, TabCompleter {

    private final Map<String, CommandData> commandMap = new HashMap<>();
    private final Map<String, Object> commandInstances = new HashMap<>();
    private Logger logger;

    public CommandHandler(Object... commandClasses) {
        for (Object commandClass : commandClasses) {
            for (Method method : commandClass.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(CommandInfo.class)) {
                    CommandInfo commandInfo = method.getAnnotation(CommandInfo.class);
                    CommandData commandData = new CommandData(commandInfo, method, commandClass);
                    commandMap.put(commandInfo.name().toLowerCase(), commandData);
                    commandInstances.put(commandInfo.name().toLowerCase(), commandClass);
                    for (String alias : commandInfo.aliases()) {
                        commandMap.put(alias.toLowerCase(), commandData);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandData commandData = commandMap.get(label.toLowerCase());
        if (commandData == null) {
            sender.sendMessage("Unknown command. Type \"/help\" for help.");
            return true;
        }

        if (!commandData.getCommandInfo().permission().isEmpty() && !sender.hasPermission(commandData.getCommandInfo().permission())) {
            sender.sendMessage("You don't have permission to use this command.");
            return true;
        }

        String subCommand = args.length > 0 ? args[0].toLowerCase() : "";
        SubCommandData subCommandData = commandData.getSubCommand(subCommand);
        if (subCommandData != null) {
            if (!subCommandData.getSubCommandInfo().permission().isEmpty() && !sender.hasPermission(subCommandData.getSubCommandInfo().permission())) {
                sender.sendMessage("You don't have permission to use this subcommand.");
                return true;
            }
            try {
                subCommandData.getMethod().invoke(commandInstances.get(label.toLowerCase()), sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            } catch (Exception e) {
                sender.sendMessage("An error occurred while executing the subcommand.");
                e.printStackTrace();
            }
        } else {
            try {
                commandData.getMethod().invoke(commandInstances.get(label.toLowerCase()), sender, command, label, args);
            } catch (Exception e) {
                sender.sendMessage("An error occurred while executing the command.");
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        CommandData commandData = commandMap.get(alias.toLowerCase());
        if (commandData == null) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return commandData.getSubCommands();
        }

        SubCommandData subCommandData = commandData.getSubCommand(args[0].toLowerCase());
        if (subCommandData == null) {
            return Collections.emptyList();
        }

        try {
            return (List<String>) subCommandData.getMethod().invoke(commandInstances.get(alias.toLowerCase()), sender, command, alias, args);
        } catch (Exception e) {
            //Logger.log(LogLevel.WARN);
            return Collections.emptyList();
        }
    }

    public void registerCommand(String commandName, JavaPlugin plugin) {
        plugin.getCommand(commandName).setExecutor(this);
        plugin.getCommand(commandName).setTabCompleter(this);
    }

    private static class CommandData {
        private final CommandInfo commandInfo;
        private final Method method;
        private final Object instance;
        private final Map<String, SubCommandData> subCommands = new HashMap<>();

        public CommandData(CommandInfo commandInfo, Method method, Object instance) {
            this.commandInfo = commandInfo;
            this.method = method;
            this.instance = instance;
            for (Method subMethod : instance.getClass().getDeclaredMethods()) {
                if (subMethod.isAnnotationPresent(SubCommandInfo.class)) {
                    SubCommandInfo subCommandInfo = subMethod.getAnnotation(SubCommandInfo.class);
                    subCommands.put(subCommandInfo.name().toLowerCase(), new SubCommandData(subCommandInfo, subMethod));
                }
            }
        }

        public CommandInfo getCommandInfo() {
            return commandInfo;
        }

        public Method getMethod() {
            return method;
        }

        public List<String> getSubCommands() {
            return new ArrayList<>(subCommands.keySet());
        }

        public SubCommandData getSubCommand(String name) {
            return subCommands.get(name);
        }
    }

    private static class SubCommandData {
        private final SubCommandInfo subCommandInfo;
        private final Method method;

        public SubCommandData(SubCommandInfo subCommandInfo, Method method) {
            this.subCommandInfo = subCommandInfo;
            this.method = method;
        }

        public SubCommandInfo getSubCommandInfo() {
            return subCommandInfo;
        }

        public Method getMethod() {
            return method;
        }
    }
}