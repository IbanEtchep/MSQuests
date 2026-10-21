package com.github.ibanetchep.msquests.bukkit.service;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.config.ContentSource;
import com.github.ibanetchep.msquests.bukkit.config.GlobalConfig;
import com.github.ibanetchep.msquests.bukkit.config.PlaceholdersConfig;
import com.github.ibanetchep.msquests.bukkit.config.TrackingBossBarConfig;
import dev.dejvokep.boostedyaml.YamlDocument;
import com.github.ibanetchep.msquests.bukkit.platform.BarOverlay;
import org.bukkit.boss.BarColor;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class GlobalConfigLoaderService {

    private BukkitQuestsPlugin plugin;
    private YamlDocument config;

    public GlobalConfigLoaderService(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    private void loadYamlDocument() {
        try {
            DumperSettings dumperSettings = DumperSettings.builder()
                    .setScalarFormatter((tag, value, role, def) -> {
                        if (tag != Tag.STR) return def;
                        if (value.contains("\n")) return ScalarStyle.LITERAL;
                        return def;
                    })
                    .build();

            config = YamlDocument.create(
                    new File(plugin.getDataFolder(), "config.yml"),
                    Objects.requireNonNull(plugin.getResource("config.yml")),
                    GeneralSettings.builder()
                            .setKeyFormat(GeneralSettings.KeyFormat.OBJECT)
                            .build(),
                    LoaderSettings.DEFAULT,
                    dumperSettings,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config_version")).build()
            );

            config.update();
            config.save();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load configuration file, disabling plugin", e);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public GlobalConfig load() {
        loadYamlDocument();
        String language = config.getString("language", "en_EN");
        ContentSource contentSource = ContentSource.parse(config.getString("content_source", "legacy"));

        GlobalConfig.DatabaseConfig database = new GlobalConfig.DatabaseConfig(
                config.getString("database.type", "mysql"),
                config.getString("database.host"),
                config.getInt("database.port"),
                config.getString("database.name"),
                config.getString("database.user"),
                config.getString("database.password")
        );

        boolean bossBarEnabled = config.getBoolean("tracking.bossbar.enabled", false);
        String bossBarMessage = config.getString("tracking.bossbar.message", "%objective_name% - %objective_progress%");
        BarColor bossBarColor = BarColor.valueOf(config.getString("tracking.bossbar.color", "WHITE").toUpperCase());
        BarOverlay bossBarStyle = BarOverlay.valueOf(config.getString("tracking.bossbar.style", "PROGRESS").toUpperCase());
        boolean bossBarShowProgress = config.getBoolean("tracking.bossbar.show_progress", true);

        TrackingBossBarConfig trackingBossBarConfig = new TrackingBossBarConfig(
                bossBarEnabled, bossBarMessage, bossBarColor, bossBarStyle, bossBarShowProgress
        );

        int cycleDurationSeconds = Math.max(1, config.getInt("placeholders.cycle_duration_seconds", 5));
        PlaceholdersConfig placeholdersConfig = new PlaceholdersConfig(cycleDurationSeconds);

        boolean deliverFromShulkerBoxes = config.getBoolean("deliver_item.include_shulker_boxes", true);

        return new GlobalConfig(language, contentSource, database, trackingBossBarConfig, placeholdersConfig, deliverFromShulkerBoxes);
    }
}
