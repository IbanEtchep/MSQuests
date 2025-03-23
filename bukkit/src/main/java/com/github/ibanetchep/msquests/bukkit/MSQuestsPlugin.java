package com.github.ibanetchep.msquests.bukkit;

import com.github.ibanetchep.msquests.bukkit.command.QuestAdminCommand;
import com.github.ibanetchep.msquests.bukkit.database.DbAccess;
import com.github.ibanetchep.msquests.bukkit.database.DbCredentials;
import com.github.ibanetchep.msquests.bukkit.database.repository.ActorSqlRepository;
import com.github.ibanetchep.msquests.bukkit.database.repository.QuestDefinitionSqlRepository;
import com.github.ibanetchep.msquests.bukkit.database.repository.QuestSqlRepository;
import com.github.ibanetchep.msquests.bukkit.listener.PlayerJoinListener;
import com.github.ibanetchep.msquests.bukkit.questobjective.blockbreak.BlockBreakObjective;
import com.github.ibanetchep.msquests.bukkit.questobjective.blockbreak.BlockBreakObjectiveDefinition;
import com.github.ibanetchep.msquests.bukkit.questobjective.blockbreak.BlockBreakObjectiveHandler;
import com.github.ibanetchep.msquests.core.manager.QuestManager;
import com.github.ibanetchep.msquests.core.mapper.QuestDefinitionMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestEntryMapper;
import com.github.ibanetchep.msquests.core.registry.ActorTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;
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

public final class MSQuestsPlugin extends JavaPlugin {

    private ActorTypeRegistry actorRegistry;
    private ObjectiveTypeRegistry objectiveTypeRegistry;

    private QuestManager questManager;

    private YamlDocument config;
    private DbAccess dbAccess;
    private FoliaLib foliaLib;

    @Override
    public void onEnable() {
        loadConfig();
        loadDatabase();
        foliaLib = new FoliaLib(this);

        actorRegistry = new ActorTypeRegistry();
        objectiveTypeRegistry = new ObjectiveTypeRegistry();


        questManager = new QuestManager(
                new QuestDefinitionSqlRepository(dbAccess),
                new ActorSqlRepository(dbAccess),
                new QuestSqlRepository(dbAccess),
                new QuestDefinitionMapper(objectiveTypeRegistry),
                new QuestEntryMapper(objectiveTypeRegistry),
                actorRegistry,
                objectiveTypeRegistry
        );

        registerListeners();
        registerCommands();
        registerObjectiveTypes();
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

    private void registerObjectiveTypes() {
        objectiveTypeRegistry.registerType(
                BlockBreakObjectiveDefinition.class,
                BlockBreakObjective.class,
                new BlockBreakObjectiveHandler(this)
        );
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public ActorTypeRegistry getActorRegistry() {
        return actorRegistry;
    }

    public ObjectiveTypeRegistry getObjectiveTypeRegistry() {
        return objectiveTypeRegistry;
    }

    @NotNull
    public YamlDocument getConfiguration() {
        return config;
    }

    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }
}