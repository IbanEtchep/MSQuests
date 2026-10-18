package com.github.ibanetchep.msquests.bukkit.config;

public record GlobalConfig(String language, ContentSource contentSource, TrackingBossBarConfig trackingBossBar, PlaceholdersConfig placeholders, boolean deliverFromShulkerBoxes) {
}
