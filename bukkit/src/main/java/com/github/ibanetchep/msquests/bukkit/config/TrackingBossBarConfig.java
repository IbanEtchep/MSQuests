package com.github.ibanetchep.msquests.bukkit.config;

import com.github.ibanetchep.msquests.bukkit.platform.BarOverlay;
import org.bukkit.boss.BarColor;

public record TrackingBossBarConfig(
        boolean enabled,
        String message,
        BarColor color,
        BarOverlay style,
        boolean showProgress
) {}
