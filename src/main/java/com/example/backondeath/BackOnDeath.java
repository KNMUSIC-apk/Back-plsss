package com.example.backondeath;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BackOnDeath extends JavaPlugin implements Listener {

    private final Map<UUID, DeathData> deathDataMap = new HashMap<>();
    private final Map<UUID, Integer> taskIds = new HashMap<>();
    private int radius;
    private int countdownSeconds;
    private boolean useActionBar;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfigValues();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("back").setExecutor(new BackCommand(this));
        getLogger().info("BackOnDeath enabled!");
    }

    @Override
    public void onDisable() {
        taskIds.values().forEach(Bukkit.getScheduler()::cancelTask);
        taskIds.clear();
        deathDataMap.clear();
        getLogger().info("BackOnDeath disabled.");
    }

    public void reloadConfigValues() {
        FileConfiguration config = getConfig();
        radius = config.getInt("radius", 10);
        countdownSeconds = config.getInt("countdown", 10);
        useActionBar = config.getBoolean("enable-action-bar", true);
        if (radius < 1) radius = 1;
        if (countdownSeconds < 1) countdownSeconds = 1;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLoc = player.getLocation();
        boolean hasNearby = player.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .anyMatch(p -> !p.equals(player) && p.isOnline() && p.getHealth() > 0);
        deathDataMap.put(player.getUniqueId(), new DeathData(deathLoc, !hasNearby));
        cancelTask(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cancelTask(uuid);
        deathDataMap.remove(uuid);
    }

    public void startBackTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        DeathData data = deathDataMap.get(uuid);
        if (data == null || !data.isAllowed()) {
            player.sendMessage("§cBạn không thể sử dụng /back lúc này.");
            return;
        }
        cancelTask(uuid);
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            int seconds = countdownSeconds;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelTask(uuid);
                    return;
                }
                if (seconds <= 0) {
                    player.teleport(data.getDeathLocation());
                    player.sendMessage("§aBạn đã được hồi sinh về nơi đã chết!");
                    deathDataMap.remove(uuid);
                    cancelTask(uuid);
                } else {
                    String msg = "§eHồi sinh sau " + seconds + " giây...";
                    if (useActionBar) {
                        player.sendActionBar(msg);
                    } else {
                        player.sendMessage(msg);
                    }
                    seconds--;
                }
            }
        }, 0L, 20L);
        taskIds.put(uuid, taskId);
    }

    public void cancelTask(UUID uuid) {
        Integer taskId = taskIds.remove(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public boolean hasDeathData(UUID uuid) {
        return deathDataMap.containsKey(uuid);
    }

    public boolean isAllowed(UUID uuid) {
        DeathData data = deathDataMap.get(uuid);
        return data != null && data.isAllowed();
    }
}
