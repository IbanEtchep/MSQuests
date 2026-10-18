package com.github.ibanetchep.msquests.bukkit.suite;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/** Runs one chain for one player, in a project's scope: Artisan's {@code ModuleActionsApi.run} or the local engine. */
@FunctionalInterface
public interface ChainExecutor {
    void run(Player player, List<?> steps, Map<String, Object> args, @Nullable String projectId);
}
