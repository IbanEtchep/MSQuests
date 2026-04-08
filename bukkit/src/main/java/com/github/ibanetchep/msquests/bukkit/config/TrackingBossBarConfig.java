package com.github.ibanetchep.msquests.bukkit.config;

import net.kyori.adventure.bossbar.BossBar;

public record TrackingBossBarConfig(
        boolean enabled,
        String message,
        BossBar.Color color,
        BossBar.Overlay style,
        boolean showProgress
) {}
