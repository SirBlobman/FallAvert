package com.github.sirblobman.fall.avert.listener;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public ListenerFallAvert(FallAvertPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
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

    private FallAvertPlugin getPlugin() {
        return this.plugin;
    }

    private FileConfiguration getConfiguration() {
        FallAvertPlugin plugin = getPlugin();
        return plugin.getConfig();
    }

    private boolean isNotFalling(Player player) {
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

    private List<String> getBlockedCommands() {
        FileConfiguration configuration = getConfiguration();
        return configuration.getStringList("commands.blocked-command-list");
    }

    private List<String> getAllowedCommands() {
        FileConfiguration configuration = getConfiguration();
        return configuration.getStringList("commands.allowed-command-list");
    }

    private List<String> getPunishmentCommands() {
        FileConfiguration configuration = getConfiguration();
        return configuration.getStringList("punishments.commands");
    }

    private boolean startsWithAny(String command, List<String> values) {
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

    private boolean isBlocked(String command) {
        List<String> allowList = getAllowedCommands();
        if (startsWithAny(command, allowList)) {
            return false;
        }

        List<String> blockList = getBlockedCommands();
        return startsWithAny(command, blockList);
    }

    private void sendBlockedCommandMessage(Player player, String command) {
        FallAvertPlugin plugin = getPlugin();
        String messageFormat = plugin.getConfigMessage("blocked-command");
        if (messageFormat.isEmpty()) {
            return;
        }

        String message = messageFormat.replace("{command}", command);
        player.sendMessage(message);
    }

    private void punish(Player player) {
        if (player == null) {
            return;
        }

        String playerName = player.getName();
        List<String> punishmentCommandList = getPunishmentCommands();
        for (String punishmentCommand : punishmentCommandList) {
            String replacedCommand = punishmentCommand.replace("{player}", playerName);
            runCommand(replacedCommand);
        }
    }

    private void runCommand(String command) {
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
