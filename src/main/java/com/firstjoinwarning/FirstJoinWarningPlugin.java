package com.firstjoinwarning.firstjoinwarning;

import org.bukkit.plugin.java.JavaPlugin;

public class FirstJoinWarningPlugin extends JavaPlugin {

    private PlayerDataManager playerDataManager;
    private JoinListener joinListener;

    @Override
    public void onEnable() {
        // save the default if it isnt there
        saveDefaultConfig();

        // init the data manager
        playerDataManager = new PlayerDataManager(this);
        playerDataManager.load();

        // register the join listener
        joinListener = new JoinListener(this, playerDataManager);
        getServer().getPluginManager().registerEvents(joinListener, this);

        // register the reload command
        getCommand("firstjoin").setExecutor(new ReloadCommand(this, playerDataManager));
        getCommand("firstjoin").setTabCompleter(new ReloadCommand(this, playerDataManager));

        getLogger().info("FirstJoinWarning plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save player data
        if (playerDataManager != null) {
            playerDataManager.save();
        }

        getLogger().info("FirstJoinWarning plugin has been disabled!");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        if (joinListener != null) {
            joinListener.reloadMessages();
        }
        getLogger().info("Configuration reloaded!");
    }

    public JoinListener getJoinListener() {
        return joinListener;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}