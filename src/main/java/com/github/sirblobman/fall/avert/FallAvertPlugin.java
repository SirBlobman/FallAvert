package com.github.sirblobman.fall.avert;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.ChatColor;

import com.github.sirblobman.fall.avert.command.CommandFallAvert;
import com.github.sirblobman.fall.avert.listener.ListenerFallAvert;

import org.jetbrains.annotations.NotNull;

public final class FallAvertPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        reloadConfig();

        registerListeners();
        registerCommands();
    }

    @NotNull
    public String getConfigMessage(String path) {
        FileConfiguration configuration = getConfig();
        String message = configuration.getString("messages." + path);
        if (message == null) {
            return path;
        }

        if (message.isEmpty()) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean isFalling(Player player) {
        printDebug("isFalling method called.");

        if (player == null) {
            printDebug("Null players are never falling.");
            return false;
        }

        Location location = player.getLocation();
        Block block = location.getBlock();
        Material blockType = block.getType();
        printDebug("Player Location: " + location);
        printDebug("Block Location: " + block.getLocation());
        printDebug("Block Type: " + blockType);

        if (blockType.isSolid() && !blockType.isOccluding()) {
            printDebug("Block type is solid and not occluding, not falling.");
            return false;
        }

        Block below = block.getRelative(BlockFace.DOWN);
        Material belowType = below.getType();
        printDebug("Below Block Type: " + below);

        boolean falling = (belowType == Material.AIR || belowType.name().endsWith("_AIR"));
        printDebug("Falling: " + falling);
        return falling;
    }

    private boolean isDebugModeDisabled() {
        FileConfiguration configuration = getConfig();
        return !configuration.getBoolean("debug-mode", false);
    }

    public void printDebug(String message) {
        if (isDebugModeDisabled()) {
            return;
        }

        Logger logger = getLogger();
        logger.info("[Debug] " + message);
    }

    private void registerListeners() {
        new ListenerFallAvert(this).register();
    }

    private void registerCommands() {
        new CommandFallAvert(this).register();
    }
}
