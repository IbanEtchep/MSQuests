package com.github.ibanetchep.msquests.bukkit.platform;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

/** Creates Bukkit boss bars; replaced in tests, where there is no server. */
@FunctionalInterface
public interface BossBars {

    BossBars BUKKIT = Bukkit::createBossBar;

    BossBar create(String legacyTitle, BarColor color, BarStyle style);
}
