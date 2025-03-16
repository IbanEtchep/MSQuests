package com.github.ibanetchep.msquests;

import com.github.ibanetchep.msquests.command.QuestAdminCommand;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.DbCredentials;
import com.github.ibanetchep.msquests.event.PlayerJoinListener;
import com.github.ibanetchep.msquests.manager.QuestManager;
import com.github.ibanetchep.msquests.model.quest.type.BlockBreakObjective;
import com.github.ibanetchep.msquests.registry.ActorRegistry;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.BukkitLampConfig;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class MSQuestsPlugin extends JavaPlugin {

    private ActorRegistry actorRegistry;

    private QuestManager questManager;

    private Executor singleThreadExecutor;
    private YamlDocument config;
    private DbAccess dbAccess;
    private FoliaLib foliaLib;

    @Override
    public void onEnable() {
        loadConfig();
        loadDatabase();
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        this.foliaLib = new FoliaLib(this);

        actorRegistry = new ActorRegistry();

        questManager = new QuestManager(this);

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        dbAccess.closePool();
    }

    private void loadConfig() {
        try {
            config = YamlDocument.create(
                    new File(getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getResource("config.yml")),
                    GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build(),
                    LoaderSettings.DEFAULT,
                    DumperSettings.DEFAULT,
                    UpdaterSettings.DEFAULT
            );
            config.update();
            config.save();
        } catch (IOException e) {
            getLogger().severe("Failed to load configuration file, disabling plugin.");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public void loadDatabase() {
        var dbCredentials = new DbCredentials(
                config.getString("database.host"),
                config.getString("database.user"),
                config.getString("database.password"),
                config.getString("database.name"),
                config.getInt("database.port")
        );

        dbAccess = new DbAccess();

        try {
            dbAccess.initPool(dbCredentials);
        } catch (Exception e) {
            getLogger().severe("Failed to connect to database, disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommands() {
        Lamp<BukkitCommandActor> lamp =  BukkitLamp.builder(this)
                .build();

        BukkitLampConfig.builder(this).disableBrigadier().build();

        lamp.register(new QuestAdminCommand(this));
    }

    public void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
    }

    public DbAccess getDbAccess() {
        return dbAccess;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public ActorRegistry getActorRegistry() {
        return actorRegistry;
    }

    public Executor getSingleThreadExecutor() {
        return singleThreadExecutor;
    }

    @NotNull
    public YamlDocument getConfiguration() {
        return config;
    }

    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }

    public void runAsyncQueued(Runnable task) {
        singleThreadExecutor.execute(task);
    }
}