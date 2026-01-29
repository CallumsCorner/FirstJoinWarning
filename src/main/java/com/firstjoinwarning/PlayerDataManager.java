package com.firstjoinwarning.firstjoinwarning;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerDataManager {

    private final FirstJoinWarningPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, Integer> playerWarningStage;

    public PlayerDataManager(FirstJoinWarningPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.playerWarningStage = new HashMap<>();
    }

    public void load() {
        // Create folder if needed
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // create file if needed
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create playerdata.yml", e);
            }
        }

        // load config
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        //  load player data
        if (dataConfig.contains("players")) {
            for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                int stage = dataConfig.getInt("players." + uuidString);
                playerWarningStage.put(uuid, stage);
            }
        }

        plugin.getLogger().info("Loaded data for " + playerWarningStage.size() + " players");
    }

    public void save() {
        // save all playser
        for (Map.Entry<UUID, Integer> entry : playerWarningStage.entrySet()) {
            dataConfig.set("players." + entry.getKey().toString(), entry.getValue());
        }

        // Write to file
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save playerdata.yml", e);
        }
    }

    public int getWarningStage(UUID uuid) {
        return playerWarningStage.getOrDefault(uuid, 0);
    }

    public void setWarningStage(UUID uuid, int stage) {
        playerWarningStage.put(uuid, stage);
    }

    public void incrementWarningStage(UUID uuid) {
        int currentStage = getWarningStage(uuid);
        setWarningStage(uuid, currentStage + 1);
    }

    public boolean hasCompletedWarnings(UUID uuid) {
        return getWarningStage(uuid) >= 3;
    }
}
