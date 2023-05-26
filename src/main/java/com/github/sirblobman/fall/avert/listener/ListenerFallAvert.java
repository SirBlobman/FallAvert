package com.github.sirblobman.fall.avert.listener;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.StringUtil;

import com.github.sirblobman.fall.avert.FallAvertPlugin;

public final class ListenerFallAvert implements Listener {
    private final FallAvertPlugin plugin;

    public ListenerFallAvert(@NotNull FallAvertPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void beforeCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (isNotFalling(player)) {
            return;
        }

        if (isAllowFlying()) {
            boolean allowFlight = player.getAllowFlight();
            boolean isFlying = player.isFlying();
            if (allowFlight && isFlying) {
                return;
            }
        }

        String command = e.getMessage();
        if (!command.startsWith("/")) {
            command = ("/" + command);
        }

        if (isBlocked(command)) {
            e.setCancelled(true);
            sendBlockedCommandMessage(player, command);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (isNotFalling(player)) {
            return;
        }

        if (isAllowFlying()) {
            boolean allowFlight = player.getAllowFlight();
            boolean isFlying = player.isFlying();
            if (allowFlight && isFlying) {
                return;
            }
        }

        if (shouldPunish()) {
            punish(player);
        }
    }

    public void register() {
        FallAvertPlugin plugin = getPlugin();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this, plugin);
    }

    private @NotNull FallAvertPlugin getPlugin() {
        return this.plugin;
    }

    private @NotNull FileConfiguration getConfiguration() {
        FallAvertPlugin plugin = getPlugin();
        return plugin.getConfig();
    }

    private boolean isNotFalling(@NotNull Player player) {
        FallAvertPlugin plugin = getPlugin();
        return !plugin.isFalling(player);
    }

    private boolean isAllowFlying() {
        FileConfiguration configuration = getConfiguration();
        return configuration.getBoolean("allow-flying", true);
    }

    private boolean shouldPunish() {
        FileConfiguration configuration = getConfiguration();
        return configuration.getBoolean("punishments.enabled", false);
    }

    private @NotNull List<String> getBlockedCommands() {
        FileConfiguration configuration = getConfiguration();
        return configuration.getStringList("commands.blocked-command-list");
    }

    private @NotNull List<String> getAllowedCommands() {
        FileConfiguration configuration = getConfiguration();
        return configuration.getStringList("commands.allowed-command-list");
    }

    private @NotNull List<String> getPunishmentCommands() {
        FileConfiguration configuration = getConfiguration();
        return configuration.getStringList("punishments.commands");
    }

    private boolean startsWithAny(@NotNull String command, @NotNull List<String> values) {
        if (values.contains("*")) {
            return true;
        }

        for (String value : values) {
            if (StringUtil.startsWithIgnoreCase(command, value)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlocked(@NotNull String command) {
        List<String> allowList = getAllowedCommands();
        if (startsWithAny(command, allowList)) {
            return false;
        }

        List<String> blockList = getBlockedCommands();
        return startsWithAny(command, blockList);
    }

    private void sendBlockedCommandMessage(@NotNull Player player, @NotNull String command) {
        FallAvertPlugin plugin = getPlugin();
        String messageFormat = plugin.getConfigMessage("blocked-command");
        if (messageFormat.isEmpty()) {
            return;
        }

        String message = messageFormat.replace("{command}", command);
        player.sendMessage(message);
    }

    private void punish(@NotNull Player player) {
        String playerName = player.getName();
        List<String> punishmentCommandList = getPunishmentCommands();
        for (String punishmentCommand : punishmentCommandList) {
            String replacedCommand = punishmentCommand.replace("{player}", playerName);
            runCommand(replacedCommand);
        }
    }

    private void runCommand(@NotNull String command) {
        FallAvertPlugin plugin = getPlugin();
        CommandSender console = Bukkit.getConsoleSender();

        try {
            plugin.printDebug("Executing command '" + command + "' in console.");
            boolean result = Bukkit.dispatchCommand(console, command);
            plugin.printDebug("Command Result: " + result);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while executing a console command:", ex);
        }
    }
}
