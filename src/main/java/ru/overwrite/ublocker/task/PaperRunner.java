package ru.overwrite.ublocker.task;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class PaperRunner implements Runner {

    private final Plugin plugin;
    private final AsyncScheduler asyncScheduler;
    private final GlobalRegionScheduler globalScheduler;

    public PaperRunner(Plugin plugin) {
        this.plugin = plugin;
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
        this.globalScheduler = plugin.getServer().getGlobalRegionScheduler();
    }

    @Override
    public void runPlayer(@NotNull Runnable task, @NotNull Player player) {
        player.getScheduler().run(plugin, toConsumer(task), null);
    }

    @Override
    public void run(@NotNull Runnable task) {
        globalScheduler.run(plugin, toConsumer(task));
    }

    @Override
    public void runAsync(@NotNull Runnable task) {
        asyncScheduler.runNow(plugin, toConsumer(task));
    }

    @Override
    public void runDelayed(@NotNull Runnable task, long delayTicks) {
        globalScheduler.runDelayed(plugin, toConsumer(task), delayTicks);
    }

    @Override
    public void runDelayedAsync(@NotNull Runnable task, long delayTicks) {
        asyncScheduler.runDelayed(plugin, toConsumer(task), toMilli(delayTicks), TimeUnit.MILLISECONDS);
    }

    @Override
    public void runPeriodical(@NotNull Runnable task, long delayTicks, long periodTicks) {
        globalScheduler.runAtFixedRate(plugin, toConsumer(task), delayTicks, periodTicks);
    }

    @Override
    public void runPeriodicalAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        asyncScheduler.runAtFixedRate(plugin, toConsumer(task), toMilli(delayTicks), toMilli(periodTicks), TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancelTasks() {
        globalScheduler.cancelTasks(plugin);
        asyncScheduler.cancelTasks(plugin);
    }

    private static Consumer<ScheduledTask> toConsumer(Runnable task) {
        return st -> task.run();
    }

    private static long toMilli(long ticks) {
        return ticks * 50L;
    }
}