package com.github.ibanetchep.msquests.bukkit.scheduler;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.scheduler.Scheduler;
import com.github.ibanetchep.msquests.core.scheduler.ThrowingRunnable;
import com.tcoded.folialib.FoliaLib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PluginScheduler implements Scheduler {

    private final FoliaLib foliaLib;
    private final Executor singleThreadExecutor;

    public PluginScheduler(MSQuestsPlugin plugin) {
        this.foliaLib = new FoliaLib(plugin);
        singleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public CompletableFuture<Void> runAsync(ThrowingRunnable runnable) {
        return foliaLib.getScheduler().runAsync(wrappedTask -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }

                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> runAsyncQueued(ThrowingRunnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, singleThreadExecutor);
    }
}