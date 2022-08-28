package com.github.sirblobman.fall.avert.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.github.sirblobman.fall.avert.FallAvertPlugin;

public final class CommandFallAvert implements TabExecutor {
    private final FallAvertPlugin plugin;

    public CommandFallAvert(FallAvertPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) {
            List<String> valueList = Arrays.asList("reload", "reloadconfig", "config", "check", "info");
            return StringUtil.copyPartialMatches(args[0], valueList, new ArrayList<>());
        }

        if(args.length == 2) {
            String sub = args[0].toLowerCase(Locale.US);
            List<String> subValueList = Arrays.asList("check", "info");
            if(subValueList.contains(sub)) {
                Collection<? extends Player> onlinePlayerCollection = Bukkit.getOnlinePlayers();
                Set<String> onlinePlayerNameSet = onlinePlayerCollection.stream().map(Player::getName)
                        .collect(Collectors.toSet());
                return StringUtil.copyPartialMatches(args[1], onlinePlayerNameSet, new ArrayList<>());
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) {
            return false;
        }

        String sub = args[0].toLowerCase(Locale.US);
        String[] newArgs = (args.length < 2 ? new String[0] : Arrays.copyOfRange(args, 1, args.length));
        switch(sub) {
            case "reload":
            case "reloadconfig":
            case "config":
                return commandReload(sender);

            case "check":
            case "info":
                return commandCheck(sender, newArgs);

            default: break;
        }

        return false;
    }

    public void register() {
        FallAvertPlugin plugin = getPlugin();
        PluginCommand pluginCommand = plugin.getCommand("fallavert");
        if(pluginCommand == null) {
            Logger logger = plugin.getLogger();
            logger.warning("Command 'fallavert' is missing in the plugin.yml");
            return;
        }

        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }

    private FallAvertPlugin getPlugin() {
        return this.plugin;
    }

    private boolean commandReload(CommandSender sender) {
        FallAvertPlugin plugin = getPlugin();
        plugin.reloadConfig();

        String message = plugin.getConfigMessage("reload");
        sender.sendMessage(message);
        return true;
    }

    private boolean commandCheck(CommandSender sender, String[] args) {
        FallAvertPlugin plugin = getPlugin();
        if(args.length < 1 && !(sender instanceof Player)) {
            String message = plugin.getConfigMessage("not-player");
            sender.sendMessage(message);
            return true;
        }

        Player target = (args.length < 1 ? (Player) sender : Bukkit.getPlayerExact(args[0]));
        if(target == null) {
            String message = plugin.getConfigMessage("invalid-target");
            sender.sendMessage(message);
            return false;
        }

        String targetName = target.getName();
        sender.sendMessage("Checking " + targetName + "...");

        boolean isFalling = plugin.isFalling(target);
        Block block = target.getLocation().getBlock();
        Block below = block.getRelative(BlockFace.DOWN);

        sender.sendMessage("Falling: " + isFalling);
        sender.sendMessage("Block In: " + block.getType());
        sender.sendMessage("Block Down: " + below.getType());
        return true;
    }
}
