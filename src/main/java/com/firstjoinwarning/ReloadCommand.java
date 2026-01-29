package com.firstjoinwarning.firstjoinwarning;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final FirstJoinWarningPlugin plugin;
    private final PlayerDataManager dataManager;

    public ReloadCommand(FirstJoinWarningPlugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage:")
                .color(NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("  /firstjoin reload - Reload configuration")
                .color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  /firstjoin reset <player> - Reset a player's warning progress")
                .color(NamedTextColor.GRAY));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("firstjoinwarning.reload")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.")
                        .color(NamedTextColor.RED));
                    return true;
                }

                plugin.reloadPluginConfig();
                sender.sendMessage(Component.text("FirstJoinWarning configuration reloaded successfully!")
                    .color(NamedTextColor.GREEN));
                return true;

            case "reset":
                if (!sender.hasPermission("firstjoinwarning.reset")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.")
                        .color(NamedTextColor.RED));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /firstjoin reset <player>")
                        .color(NamedTextColor.YELLOW));
                    return true;
                }

                String playerName = args[1];
                OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    sender.sendMessage(Component.text("Player '" + playerName + "' has never joined the server.")
                        .color(NamedTextColor.RED));
                    return true;
                }

                dataManager.setWarningStage(target.getUniqueId(), 0);
                dataManager.save();

                sender.sendMessage(Component.text("Reset warning progress for player '" + playerName + "'. They will see all warnings on their next join.")
                    .color(NamedTextColor.GREEN));
                return true;

            default:
                sender.sendMessage(Component.text("Unknown subcommand. Use /firstjoin for help.")
                    .color(NamedTextColor.RED));
                return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (sender.hasPermission("firstjoinwarning.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("firstjoinwarning.reset")) {
                completions.add("reset");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            if (sender.hasPermission("firstjoinwarning.reset")) {
                // list online player names
                return Bukkit.getOnlinePlayers().stream()
                    .map(player -> player.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}