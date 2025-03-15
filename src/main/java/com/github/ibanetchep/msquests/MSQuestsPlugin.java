package com.github.ibanetchep.msquests;

import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.DbCredentials;
import com.github.ibanetchep.msquests.database.repository.ActorRepository;
import com.github.ibanetchep.msquests.database.repository.QuestEntryRepository;
import com.github.ibanetchep.msquests.event.PlayerJoinListener;
import com.github.ibanetchep.msquests.manager.QuestActorManager;
import com.github.ibanetchep.msquests.manager.QuestEntryManager;
import com.github.ibanetchep.msquests.objective.BlockBreakObjectiveEntry;
import com.github.ibanetchep.msquests.registry.ActorRegistry;
import com.github.ibanetchep.msquests.registry.ObjectiveEntryTypeRegistry;
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

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class MSQuestsPlugin extends JavaPlugin {

    private ActorRegistry actorRegistry;
    private ObjectiveEntryTypeRegistry objectiveEntryTypeRegistry;

    private QuestActorManager questActorManager;
    private QuestEntryManager questManager;

    private ActorRepository actorRepository;
    private QuestEntryRepository questEntryRepository;

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
        objectiveEntryTypeRegistry = new ObjectiveEntryTypeRegistry();
        objectiveEntryTypeRegistry.registerObjectiveType(BlockBreakObjectiveEntry.class);

        actorRepository = new ActorRepository(dbAccess);
        questEntryRepository = new QuestEntryRepository(dbAccess);

        questActorManager = new QuestActorManager(this, actorRepository);
        questManager = new QuestEntryManager(this, questEntryRepository);


        registerListeners();
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
                config.getString("database.dbname"),
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


    public void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
    }

    public QuestEntryManager getQuestManager() {
        return questManager;
    }

    public QuestActorManager getQuestActorManager() {
        return questActorManager;
    }

    public ActorRegistry getActorRegistry() {
        return actorRegistry;
    }

    public ObjectiveEntryTypeRegistry getObjectiveTypeRegistry() {
        return objectiveEntryTypeRegistry;
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
}