package com.github.sirblobman.fall.avert.command;

import java.util.Arrays;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.sirblobman.fall.avert.FallAvertPlugin;
import com.github.sirblobman.fall.avert.listener.ListenerFallAvert;

public class CommandFallAvert implements CommandExecutor {
    private final FallAvertPlugin plugin;
    public CommandFallAvert(FallAvertPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) return false;

        String sub = args[0].toLowerCase();
        String[] newArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        switch(sub) {
            case "reload":
            case "reloadconfig":
            case "config":
                return reloadConfig(sender);

            case "check":
            case "info":
                return check(sender, newArgs);

            default: return false;
        }
    }

    private boolean reloadConfig(CommandSender sender) {
        this.plugin.reloadConfig();

        String message = this.plugin.getConfigMessage("reload");
        sender.sendMessage(message);
        return true;
    }

    private boolean check(CommandSender sender, String[] args) {
        if(args.length < 1 && !(sender instanceof Player)) {
            String message = this.plugin.getConfigMessage("not-player");
            sender.sendMessage(message);
            return true;
        }

        Player target = args.length < 1 ? (Player) sender : Bukkit.getPlayer(args[0]);
        if(target == null) {
            String message = this.plugin.getConfigMessage("invalid-target");
            sender.sendMessage(message);
            return false;
        }
        String targetName = target.getName();

        boolean isFalling = ListenerFallAvert.isFalling(target);
        Block block = target.getLocation().getBlock();
        Block below = block.getRelative(BlockFace.DOWN);

        sender.sendMessage("Checking " + targetName + "...");
        sender.sendMessage("Falling: " + isFalling);
        sender.sendMessage("Block In: " + block.getType());
        sender.sendMessage("Block Down: " + below.getType());
        return true;
    }
}