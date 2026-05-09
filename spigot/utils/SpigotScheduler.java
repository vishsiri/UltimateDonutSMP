package com.bx.ultimateDonutSmp.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class SpigotScheduler {

    private final Plugin plugin;

    public SpigotScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    public BukkitTask runGlobal(Runnable runnable) {
        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public BukkitTask runGlobalLater(Runnable runnable, long delayTicks) {
        if (delayTicks <= 0L) {
            return runGlobal(runnable);
        }
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }

    public BukkitTask runGlobalTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, Math.max(1L, initialDelayTicks), Math.max(1L, periodTicks));
    }

    public BukkitTask runAsync(Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public BukkitTask runAsyncLater(Runnable runnable, long delayTicks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, Math.max(0L, delayTicks));
    }

    public BukkitTask runAsyncTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, Math.max(1L, initialDelayTicks), Math.max(1L, periodTicks));
    }

    public BukkitTask runEntity(Entity entity, Runnable runnable) {
        if (entity == null) {
            return null;
        }
        return runGlobal(runnable);
    }

    public BukkitTask runEntityLater(Entity entity, Runnable runnable, long delayTicks) {
        if (entity == null) {
            return null;
        }
        if (delayTicks <= 0L) {
            return runEntity(entity, runnable);
        }
        return runGlobalLater(runnable, delayTicks);
    }

    public BukkitTask runEntityTimer(Entity entity, Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (entity == null) {
            return null;
        }
        return runGlobalTimer(runnable, initialDelayTicks, periodTicks);
    }

    public BukkitTask runRegion(Location location, Runnable runnable) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return runGlobal(runnable);
    }

    public BukkitTask runRegionLater(Location location, Runnable runnable, long delayTicks) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        if (delayTicks <= 0L) {
            return runRegion(location, runnable);
        }
        return runGlobalLater(runnable, delayTicks);
    }

    public BukkitTask runRegion(World world, int chunkX, int chunkZ, Runnable runnable) {
        if (world == null) {
            return null;
        }
        return runGlobal(runnable);
    }

    public void forEachOnlinePlayer(Consumer<Player> consumer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            runEntity(player, () -> {
                if (player.isOnline()) {
                    consumer.accept(player);
                }
            });
        }
    }

    public CompletableFuture<Boolean> teleport(Entity entity, Location location) {
        return teleport(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public CompletableFuture<Boolean> teleport(
            Entity entity,
            Location location,
            PlayerTeleportEvent.TeleportCause cause
    ) {
        if (entity == null || location == null || location.getWorld() == null) {
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.completedFuture(entity.teleport(location, cause));
    }
}
