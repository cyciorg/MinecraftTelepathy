package org.cyci.mc.minecrafttelepathy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyci.mc.minecrafttelepathy.api.CommandInfo;
import org.cyci.mc.minecrafttelepathy.api.SubCommandInfo;
import org.cyci.mc.minecrafttelepathy.utils.C;

/**
 * org.cyci.mc.minecrafttelepathy.commands
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sun - June/Sun/2024
 */
public class MainCommand {
    @CommandInfo(name = "telepathy", aliases = {"mtp"}, description = "The main command for MinecraftTelepathy", usage = "/telepathy", args = {"arg1", "arg2"}, permission = "telepathy.default")
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            StringBuilder page = new StringBuilder();
            page.append("&a&lMinecraftTelepathy\n\n");

            page.append("&6Developer: &aPrisk\n");
            page.append("&6Version: &a" + "\n");
            player.sendMessage(C.c(page.toString()));
        }
    }

    @SubCommandInfo(name = "sub", description = "An example subcommand", usage = "/telepathy sub", args = {"arg1"}, permission = "example.sub.use")
    public void onSubCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage("You used the example subcommand!");
        }
    }
}
