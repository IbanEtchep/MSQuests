package com.github.ibanetchep.msquests.bukkit;

import com.github.ibanetchep.msquests.bukkit.command.QuestAdminCommand;
import com.github.ibanetchep.msquests.bukkit.command.annotations.QuestActorType;
import com.github.ibanetchep.msquests.bukkit.command.parametertypes.QuestActorParameterType;
import com.github.ibanetchep.msquests.bukkit.command.parametertypes.QuestGroupParameterType;
import com.github.ibanetchep.msquests.bukkit.lang.LangManager;
import com.github.ibanetchep.msquests.bukkit.listener.PlayerJoinListener;
import com.github.ibanetchep.msquests.bukkit.quest.actor.QuestPlayerActor;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak.BlockBreakObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak.BlockBreakObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak.BlockBreakObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem.DeliverItemObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem.DeliverItemObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem.DeliverItemObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.killentity.KillEntityObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.killentity.KillEntityObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.killentity.KillEntityObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.repository.QuestConfigYamlRepository;
import com.github.ibanetchep.msquests.core.manager.QuestManager;
import com.github.ibanetchep.msquests.core.mapper.QuestConfigMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestEntryMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.quest.QuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.registry.ActorTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.DbCredentials;
import com.github.ibanetchep.msquests.database.repository.ActorSqlRepository;
import com.github.ibanetchep.msquests.database.repository.QuestSqlRepository;
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
import java.nio.file.Path;
import java.util.Objects;

public final class MSQuestsPlugin extends JavaPlugin {

    private ActorTypeRegistry actorRegistry;
    private ObjectiveTypeRegistry objectiveTypeRegistry;

    private LangManager langManager;
    private QuestManager questManager;

    private YamlDocument config;
    private DbAccess dbAccess;
    private FoliaLib foliaLib;

    @Override
    public void onEnable() {
        loadConfig();
        loadDatabase();
        foliaLib = new FoliaLib(this);

        this.langManager = new LangManager(this);
        langManager.load();

        actorRegistry = new ActorTypeRegistry();
        objectiveTypeRegistry = new ObjectiveTypeRegistry();

        registerObjectiveTypes();

        actorRegistry.registerActorType("player", QuestPlayerActor.class);

        QuestConfigMapper questConfigMapper = new QuestConfigMapper(objectiveTypeRegistry);
        QuestGroupMapper questGroupMapper = new QuestGroupMapper(questConfigMapper);
        QuestEntryMapper questEntryMapper = new QuestEntryMapper(objectiveTypeRegistry);

        questManager = new QuestManager(
                getLogger(),
                new QuestConfigYamlRepository(Path.of(getDataFolder().toPath() + "/quests")),
                new ActorSqlRepository(dbAccess),
                new QuestSqlRepository(dbAccess),
                questConfigMapper,
                questGroupMapper,
                questEntryMapper,
                actorRegistry,
                objectiveTypeRegistry
        );

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
                config.getString("database.type", "mysql"),
                config.getString("database.host"),
                config.getString("database.user"),
                config.getString("database.password"),
                config.getString("database.name"),
                config.getInt("database.port"),
                getDataFolder()
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
                .parameterTypes(builder ->
                        builder
                                .addParameterType(QuestActor.class, new QuestActorParameterType(this))
                                .addParameterType(QuestGroup.class, new QuestGroupParameterType(this))
                )
                .suggestionProviders(providers -> {
                    providers.addProviderForAnnotation(QuestActorType.class, actorType -> {
                        return context -> actorRegistry.getAllActorTypes().keySet();
                    });
                })
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
                ObjectiveTypes.BLOCK_BREAK,
                BlockBreakObjectiveConfig.class,
                BlockBreakObjective.class,
                new BlockBreakObjectiveHandler(this)
        );
        objectiveTypeRegistry.registerType(
                ObjectiveTypes.DELIVER_ITEM,
                DeliverItemObjectiveConfig.class,
                DeliverItemObjective.class,
                new DeliverItemObjectiveHandler(this)
        );
        objectiveTypeRegistry.registerType(
                ObjectiveTypes.KILL_ENTITY,
                KillEntityObjectiveConfig.class,
                KillEntityObjective.class,
                new KillEntityObjectiveHandler(this)
        );
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public LangManager getLangManager() {
        return langManager;
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
