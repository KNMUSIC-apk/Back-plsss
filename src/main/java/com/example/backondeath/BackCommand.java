package com.example.backondeath;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor {
    private final BackOnDeath plugin;
    public BackCommand(BackOnDeath plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Chỉ người chơi mới dùng được.");
            return true;
        }
        Player player = (Player) sender;
        if (!plugin.hasDeathData(player.getUniqueId())) {
            player.sendMessage("§cBạn chưa chết gần đây.");
            return true;
        }
        if (!plugin.isAllowed(player.getUniqueId())) {
            player.sendMessage("§cCó người chơi khác ở gần khi bạn chết. Không thể /back.");
            return true;
        }
        plugin.startBackTeleport(player);
        return true;
    }
}
