package com.SirBlobman.fallavert;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class FallAvert extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new ListenFallAvert(this), this);

        getCommand("fallavert").setExecutor(new CommandFallAvert(this));
    }

    public String getConfigMessage(String path) {
        FileConfiguration config = getConfig();
        String message = config.getString("messages." + path);

        return (message == null ? "" : ChatColor.translateAlternateColorCodes('&', message));
    }

    public void debug(String message) {
        FileConfiguration config = getConfig();
        if(!config.getBoolean("debug")) return;

        Logger logger = getLogger();
        logger.info("[Debug] " + message);
    }
}