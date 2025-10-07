package com.github.ibanetchep.msquests.bukkit.service;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.config.GlobalConfig;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import dev.dejvokep.boostedyaml.YamlDocument;
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
import java.util.stream.Collectors;

public class GlobalConfigLoaderService {

    private MSQuestsPlugin plugin;
    private YamlDocument config;

    public GlobalConfigLoaderService(MSQuestsPlugin plugin) {
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

        GlobalConfig.DatabaseConfig database = new GlobalConfig.DatabaseConfig(
                config.getString("database.type", "mysql"),
                config.getString("database.host"),
                config.getInt("database.port"),
                config.getString("database.name"),
                config.getString("database.user"),
                config.getString("database.password")
        );

        return new GlobalConfig(language, database);
    }
}
