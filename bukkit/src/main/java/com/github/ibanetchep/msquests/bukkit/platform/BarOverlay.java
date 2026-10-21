package com.github.ibanetchep.msquests.bukkit.platform;

import org.bukkit.boss.BarStyle;

/**
 * A boss bar's divisions, under the names quest files and {@code config.yml} have always used
 * (Adventure's), mapped to Bukkit's {@link BarStyle}, which Paper and Spigot both have.
 */
public enum BarOverlay {
    PROGRESS(BarStyle.SOLID),
    NOTCHED_6(BarStyle.SEGMENTED_6),
    NOTCHED_10(BarStyle.SEGMENTED_10),
    NOTCHED_12(BarStyle.SEGMENTED_12),
    NOTCHED_20(BarStyle.SEGMENTED_20);

    private final BarStyle style;

    BarOverlay(BarStyle style) {
        this.style = style;
    }

    public BarStyle style() {
        return style;
    }
}
