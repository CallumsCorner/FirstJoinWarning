package com.mattjgh.firstjoinwarning;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JoinListener implements Listener {

    private final FirstJoinWarningPlugin plugin;
    private final PlayerDataManager dataManager;

    // Cached warning messages - stored as a map of warning number to title/message
    private final Map<Integer, WarningMessage> warnings = new HashMap<>();
    private int totalWarnings = 0;

    private static class WarningMessage {
        String title;
        String message;

        WarningMessage(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }

    public JoinListener(FirstJoinWarningPlugin plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        loadMessages();
    }

    public void reloadMessages() {
        int oldTotalWarnings = totalWarnings;
        loadMessages();
        
        // If warnings were added, don't automatically update player data
        // Instead just log it
        if (totalWarnings > oldTotalWarnings) {
            plugin.getLogger().info("Added " + (totalWarnings - oldTotalWarnings) + " new warning(s).");
            plugin.getLogger().info("Players who previously completed all warnings will now see the new warnings on next join.");
        } else if (totalWarnings < oldTotalWarnings) {
            plugin.getLogger().warning("Warning count decreased from " + oldTotalWarnings + " to " + totalWarnings + "!");
            plugin.getLogger().warning("Some players may have invalid warning stages. Consider using /firstjoin reset if needed.");
        }
        
        plugin.getLogger().info("Warning messages reloaded from config! Total warnings: " + totalWarnings);
    }

    private void loadMessages() {
        warnings.clear();
        totalWarnings = 0;

        if (!plugin.getConfig().contains("warnings")) {
            plugin.getLogger().warning("No warnings found in config!");
            return;
        }

        var warningsSection = plugin.getConfig().getConfigurationSection("warnings");
        if (warningsSection == null) {
            plugin.getLogger().warning("Warnings section is null!");
            return;
        }

        for (String key : warningsSection.getKeys(false)) {
            // Extract warning number from key
            if (key.startsWith("warning-")) {
                try {
                    int warningNum = Integer.parseInt(key.substring(8));
                    String title = plugin.getConfig().getString("warnings." + key + ".title", "NOTICE #" + warningNum);
                    String message = plugin.getConfig().getString("warnings." + key + ".message", "");
                    
                    warnings.put(warningNum, new WarningMessage(title, message));
                    totalWarnings = Math.max(totalWarnings, warningNum);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid warning key format: " + key);
                }
            }
        }

        plugin.getLogger().info("Loaded " + totalWarnings + " warning(s) from config");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        // If another plugin (we use to authenticate players on discord BEFORE they join) has already disallowed the login, don't interfere
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        UUID playerUUID = event.getUniqueId();
        int warningStage = dataManager.getWarningStage(playerUUID);

        // If player has completed all warnings, let them join
        if (warningStage >= totalWarnings) {
            return;
        }

        String playerName = event.getName();

        // Get the current warning (stage is 0-indexed, warnings are 1-indexed)
        int currentWarningNum = warningStage + 1;
        WarningMessage warning = warnings.get(currentWarningNum);

        if (warning == null) {
            plugin.getLogger().warning("Warning #" + currentWarningNum + " not found in config!");
            return;
        }

        Component message = createWarningMessage(
            warning.title,
            warning.message.replace("{player}", playerName)
        );

        // increment
        dataManager.incrementWarningStage(playerUUID);

        // save
        plugin.getServer().getScheduler().runTask(plugin, () -> dataManager.save());

        // Kick tthe player with the warning message
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
    }

    private Component createWarningMessage(String title, String body) {
        return Component.text()
            .append(Component.text(title + "\n")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD))
            .append(Component.text("\n"))
            .append(Component.text(body + "\n")
                .color(NamedTextColor.YELLOW))
            .build();
    }

    public int getTotalWarnings() {
        return totalWarnings;
    }
}