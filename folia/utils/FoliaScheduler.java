package com.bx.ultimateDonutSmp.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class FoliaScheduler {

    private static final long MILLIS_PER_TICK = 50L;

    private final Plugin plugin;

    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    public ScheduledTask runGlobal(Runnable runnable) {
        if (!canSchedule(runnable)) {
            return null;
        }
        try {
            return Bukkit.getGlobalRegionScheduler().run(plugin, task -> runnable.run());
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runGlobalLater(Runnable runnable, long delayTicks) {
        if (delayTicks <= 0L) {
            return runGlobal(runnable);
        }
        if (!canSchedule(runnable)) {
            return null;
        }
        try {
            return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delayTicks);
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runGlobalTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (!canSchedule(runnable)) {
            return null;
        }
        try {
            return Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                    plugin,
                    task -> runnable.run(),
                    Math.max(1L, initialDelayTicks),
                    Math.max(1L, periodTicks)
            );
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runAsync(Runnable runnable) {
        if (!canSchedule(runnable)) {
            return null;
        }
        try {
            return Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runAsyncLater(Runnable runnable, long delayTicks) {
        if (!canSchedule(runnable)) {
            return null;
        }
        try {
            return Bukkit.getAsyncScheduler().runDelayed(
                    plugin,
                    task -> runnable.run(),
                    ticksToMillis(delayTicks),
                    TimeUnit.MILLISECONDS
            );
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runAsyncTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (!canSchedule(runnable)) {
            return null;
        }
        try {
            return Bukkit.getAsyncScheduler().runAtFixedRate(
                    plugin,
                    task -> runnable.run(),
                    ticksToMillis(initialDelayTicks),
                    Math.max(1L, ticksToMillis(periodTicks)),
                    TimeUnit.MILLISECONDS
            );
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runEntity(Entity entity, Runnable runnable) {
        if (entity == null || !canSchedule(runnable)) {
            return null;
        }
        try {
            return entity.getScheduler().run(plugin, task -> runnable.run(), null);
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runEntityLater(Entity entity, Runnable runnable, long delayTicks) {
        if (entity == null) {
            return null;
        }
        if (delayTicks <= 0L) {
            return runEntity(entity, runnable);
        }
        if (!canSchedule(runnable)) {
            return null;
        }
        try {
            return entity.getScheduler().runDelayed(plugin, task -> runnable.run(), null, delayTicks);
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runEntityTimer(Entity entity, Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (entity == null || !canSchedule(runnable)) {
            return null;
        }
        try {
            return entity.getScheduler().runAtFixedRate(
                    plugin,
                    task -> runnable.run(),
                    null,
                    Math.max(1L, initialDelayTicks),
                    Math.max(1L, periodTicks)
            );
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runRegion(Location location, Runnable runnable) {
        if (location == null || location.getWorld() == null || !canSchedule(runnable)) {
            return null;
        }
        try {
            return Bukkit.getRegionScheduler().run(plugin, location, task -> runnable.run());
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runRegionLater(Location location, Runnable runnable, long delayTicks) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        if (delayTicks <= 0L) {
            return runRegion(location, runnable);
        }
        if (!canSchedule(runnable)) {
            return null;
        }
        try {
            return Bukkit.getRegionScheduler().runDelayed(plugin, location, task -> runnable.run(), delayTicks);
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask runRegion(World world, int chunkX, int chunkZ, Runnable runnable) {
        if (world == null || !canSchedule(runnable)) {
            return null;
        }
        try {
            return Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, task -> runnable.run());
        } catch (IllegalPluginAccessException ignored) {
            return null;
        }
    }

    public ScheduledTask forEachOnlinePlayer(Consumer<Player> consumer) {
        if (consumer == null || !plugin.isEnabled()) {
            return null;
        }
        return runGlobal(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                runEntity(player, () -> {
                    if (player.isOnline()) {
                        consumer.accept(player);
                    }
                });
            }
        });
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
        if (!plugin.isEnabled()) {
            return CompletableFuture.completedFuture(false);
        }
        return entity.teleportAsync(location, cause);
    }

    private long ticksToMillis(long ticks) {
        return Math.max(0L, ticks) * MILLIS_PER_TICK;
    }

    private boolean canSchedule(Runnable runnable) {
        return runnable != null && plugin.isEnabled();
    }
}
