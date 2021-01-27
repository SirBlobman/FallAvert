package com.github.sirblobman.fall.avert.listener;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.sirblobman.fall.avert.FallAvertPlugin;

public class ListenerFallAvert implements Listener {
    private final FallAvertPlugin plugin;
    public ListenerFallAvert(FallAvertPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
    public void beforeCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if(!isFalling(player)) return;

        boolean allowFlying = this.plugin.getConfig().getBoolean("allow-flying");
        if(allowFlying) {
            boolean allowFlight = player.getAllowFlight();
            boolean isFlying = player.isFlying();
            if(allowFlight && isFlying) return;
        }

        String command = e.getMessage();
        if(!command.startsWith("/")) command = "/" + command;

        int spaceIndex = command.indexOf(" ");
        String mainCommand = command.substring(0, spaceIndex < 0 ? command.length() : spaceIndex).toLowerCase();

        if(isBlocked(mainCommand)) {
            e.setCancelled(true);

            String message = this.plugin.getConfigMessage("blocked-command").replace("{command}", command);
            player.sendMessage(message);
        }
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if(!isFalling(player)) return;

        boolean allowFlying = this.plugin.getConfig().getBoolean("allow-flying");
        if(allowFlying) {
            boolean allowFlight = player.getAllowFlight();
            boolean isFlying = player.isFlying();
            if(allowFlight && isFlying) return;
        }

        boolean shouldPunish = this.plugin.getConfig().getBoolean("punishments.enabled");
        if(shouldPunish) punish(player);
    }

    public static boolean isFalling(Player player) {
        if(player == null) return false;

        Location location = player.getLocation();
        Block block = location.getBlock();
        Material type = block.getType();
        if(type.isSolid() && !type.isOccluding()) return false;

        Block below = block.getRelative(BlockFace.DOWN);
        Material belowType = below.getType();
        return (belowType == Material.AIR || belowType.name().endsWith("_AIR"));
    }

    private boolean isBlocked(String command) {
        FileConfiguration config = this.plugin.getConfig();
        List<String> commandList = config.getStringList("commands.list");
        boolean whitelistMode = config.getBoolean("commands.whitelist-mode");

        if(whitelistMode) return !commandList.contains(command);
        return commandList.contains(command);
    }

    private void punish(Player player) {
        if(player == null) return;
        CommandSender console = Bukkit.getConsoleSender();

        List<String> punishCommands = this.plugin.getConfig().getStringList("punishments.commands");
        for(String command : punishCommands) {
            command = command.replace("{player}", player.getName());
            try {
                Bukkit.dispatchCommand(console, command);
            } catch (Exception ex) {
                this.plugin.getLogger().log(Level.WARNING, "An error occurred while executing a punishment command.", ex);
            }
        }
    }
}