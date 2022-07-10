package com.github.sirblobman.fall.avert;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.fall.avert.command.CommandFallAvert;
import com.github.sirblobman.fall.avert.listener.ListenerFallAvert;

public final class FallAvertPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new ListenerFallAvert(this), this);

        getCommand("fallavert").setExecutor(new CommandFallAvert(this));
    }

    public String getConfigMessage(String path) {
        FileConfiguration config = getConfig();
        String message = config.getString("messages." + path);
        return (message == null ? "" : ChatColor.translateAlternateColorCodes('&', message));
    }

    public void debug(String message) {
        FileConfiguration config = getConfig();
        if(!config.getBoolean("debug")) {
            return;
        }

        Logger logger = getLogger();
        logger.info("[Debug] " + message);
    }
}
