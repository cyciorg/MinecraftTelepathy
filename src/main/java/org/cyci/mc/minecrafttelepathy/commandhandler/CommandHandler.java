package org.cyci.mc.minecrafttelepathy.commandhandler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyci.mc.minecrafttelepathy.api.CommandInfo;
import org.cyci.mc.minecrafttelepathy.api.PaginatedCommand;
import org.cyci.mc.minecrafttelepathy.api.PaginatedSubcommand;
import org.cyci.mc.minecrafttelepathy.api.SubCommandInfo;
import org.cyci.mc.minecrafttelepathy.lang.Lang;
import org.cyci.mc.minecrafttelepathy.utils.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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
                if (method.isAnnotationPresent(PaginatedCommand.class)) {
                    PaginatedCommand paginatedCommand = method.getAnnotation(PaginatedCommand.class);
                    CommandData commandData = new CommandData(paginatedCommand, method, commandClass);
                    commandMap.put(paginatedCommand.name().toLowerCase(), commandData);
                    commandInstances.put(paginatedCommand.name().toLowerCase(), commandClass);
                    for (String alias : paginatedCommand.aliases()) {
                        commandMap.put(alias.toLowerCase(), commandData);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        CommandData commandData = commandMap.get(label.toLowerCase());
        if (commandData == null) {
            sender.sendMessage("Unknown command. Type \"/telepathy help\" for help.");
            return true;
        }

        if (commandData.isPaginated()) {
            return handlePaginatedCommand(sender, command, label, args, commandData);
        }

        if (!commandData.getPermission().isEmpty() && !sender.hasPermission(commandData.getPermission())) {
            sender.sendMessage(Lang.NO_PERM.getConfigValue(null));
            return true;
        }

        String subCommand = args.length > 0 ? args[0].toLowerCase() : "";
        SubCommandData subCommandData = commandData.getSubCommand(subCommand);
        if (subCommandData != null) {
            return handleSubCommand(sender, command, label, args, subCommandData);
        } else {
            try {
                commandData.getMethod().invoke(commandInstances.get(label.toLowerCase()), sender, command, label, args);
            } catch (Exception e) {
                sender.sendMessage("An error occurred while executing the command.");
                logger.error(e.getMessage());
            }
        }
        return true;
    }

    private boolean handlePaginatedCommand(CommandSender sender, Command command, String label, String[] args, CommandData commandData) {
        PaginatedCommand paginatedCommand = (PaginatedCommand) commandData.getCommandInfo();
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid page number. Usage: /" + label + " [page]");
                return false;
            }
        }

        try {
            commandData.getMethod().invoke(commandInstances.get(label.toLowerCase()), sender, command, label, args, page, paginatedCommand.itemsPerPage());
        } catch (Exception e) {
            sender.sendMessage("An error occurred while executing the paginated command.");
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean handleSubCommand(CommandSender sender, Command command, String label, String[] args, SubCommandData subCommandData) {
        if (!subCommandData.getPermission().isEmpty() && !sender.hasPermission(subCommandData.getPermission())) {
            sender.sendMessage(Lang.NO_PERM.getConfigValue(null));
            return true;
        }

        if (subCommandData.isPaginated()) {
            return handlePaginatedSubCommand(sender, command, label, args, subCommandData);
        }

        try {
            subCommandData.getMethod().invoke(commandInstances.get(label.toLowerCase()), sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            sender.sendMessage("An error occurred while executing the subcommand.");
            logger.error(e.getMessage());
        }
        return true;
    }

    private boolean handlePaginatedSubCommand(CommandSender sender, Command command, String label, String[] args, SubCommandData subCommandData) {
        PaginatedSubcommand paginatedSubCommand = (PaginatedSubcommand) subCommandData.getSubCommandInfo();
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid page number. Usage: /" + label + " " + subCommandData.getName() + " [page]");
                return false;
            }
        }
        try {
            subCommandData.getMethod().invoke(commandInstances.get(label.toLowerCase()), sender, command, label, Arrays.copyOfRange(args, 2, args.length), page, paginatedSubCommand.itemsPerPage());
        } catch (Exception e) {
            sender.sendMessage("An error occurred while executing the paginated subcommand.");
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, String alias, String[] args) {
        CommandData commandData = commandMap.get(alias.toLowerCase());
        if (commandData == null) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return commandData.getSubCommands().stream()
                    .filter(subCmd -> sender.hasPermission(commandData.getSubCommand(subCmd).getPermission()))
                    .collect(Collectors.toList());
        }

        SubCommandData subCommandData = commandData.getSubCommand(args[0].toLowerCase());
        if (subCommandData == null) {
            return Collections.emptyList();
        }

        if (subCommandData.isPaginated() && args.length == 2) {
            return Collections.singletonList("<page>");
        }

        try {
            return (List<String>) subCommandData.getMethod().invoke(commandInstances.get(alias.toLowerCase()), sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public void registerCommand(String commandName, JavaPlugin plugin) {
        Objects.requireNonNull(plugin.getCommand(commandName)).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand(commandName)).setTabCompleter(this);
    }

    private static class CommandData {
        private final Object commandInfo;
        private final Method method;
        private final Map<String, SubCommandData> subCommands = new HashMap<>();

        public CommandData(Object commandInfo, Method method, Object instance) {
            this.commandInfo = commandInfo;
            this.method = method;
            for (Method subMethod : instance.getClass().getDeclaredMethods()) {
                if (subMethod.isAnnotationPresent(SubCommandInfo.class)) {
                    SubCommandInfo subCommandInfo = subMethod.getAnnotation(SubCommandInfo.class);
                    subCommands.put(subCommandInfo.name().toLowerCase(), new SubCommandData(subCommandInfo, subMethod));
                }
                if (subMethod.isAnnotationPresent(PaginatedSubcommand.class)) {
                    PaginatedSubcommand paginatedSubCommand = subMethod.getAnnotation(PaginatedSubcommand.class);
                    subCommands.put(paginatedSubCommand.name().toLowerCase(), new SubCommandData(paginatedSubCommand, subMethod));
                }
            }
        }

        public Object getCommandInfo() {
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

        public boolean isPaginated() {
            return commandInfo instanceof PaginatedCommand;
        }

        public String getPermission() {
            if (commandInfo instanceof CommandInfo) {
                return ((CommandInfo) commandInfo).permission();
            } else if (commandInfo instanceof PaginatedCommand) {
                return ((PaginatedCommand) commandInfo).permission();
            }
            return "";
        }
    }

    private static class SubCommandData {
        private final Object subCommandInfo;
        private final Method method;

        public SubCommandData(Object subCommandInfo, Method method) {
            this.subCommandInfo = subCommandInfo;
            this.method = method;
        }

        public Object getSubCommandInfo() {
            return subCommandInfo;
        }

        public Method getMethod() {
            return method;
        }

        public boolean isPaginated() {
            return subCommandInfo instanceof PaginatedSubcommand;
        }

        public String getPermission() {
            if (subCommandInfo instanceof SubCommandInfo) {
                return ((SubCommandInfo) subCommandInfo).permission();
            } else if (subCommandInfo instanceof PaginatedSubcommand) {
                return ((PaginatedSubcommand) subCommandInfo).permission();
            }
            return "";
        }

        public String getName() {
            if (subCommandInfo instanceof SubCommandInfo) {
                return ((SubCommandInfo) subCommandInfo).name();
            } else if (subCommandInfo instanceof PaginatedSubcommand) {
                return ((PaginatedSubcommand) subCommandInfo).name();
            }
            return "";
        }
    }
}