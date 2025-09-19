package com.github.ibanetchep.msquests.bukkit;

import com.github.ibanetchep.msquests.bukkit.command.QuestAdminCommand;
import com.github.ibanetchep.msquests.bukkit.command.annotations.QuestActorType;
import com.github.ibanetchep.msquests.bukkit.command.parametertypes.QuestActorParameterType;
import com.github.ibanetchep.msquests.bukkit.command.parametertypes.QuestConfigParameterType;
import com.github.ibanetchep.msquests.bukkit.command.parametertypes.QuestGroupParameterType;
import com.github.ibanetchep.msquests.bukkit.command.parametertypes.QuestParameterType;
import com.github.ibanetchep.msquests.bukkit.config.GlobalConfig;
import com.github.ibanetchep.msquests.bukkit.event.BukkitEventDispatcher;
import com.github.ibanetchep.msquests.bukkit.lang.Translator;
import com.github.ibanetchep.msquests.bukkit.listener.PlayerJoinListener;
import com.github.ibanetchep.msquests.bukkit.listener.QuestCompleteListener;
import com.github.ibanetchep.msquests.bukkit.listener.QuestStartListener;
import com.github.ibanetchep.msquests.bukkit.quest.action.*;
import com.github.ibanetchep.msquests.bukkit.quest.actor.QuestGlobalActor;
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
import com.github.ibanetchep.msquests.bukkit.service.GlobalConfigLoaderService;
import com.github.ibanetchep.msquests.bukkit.service.QuestPlayerActorService;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.mapper.QuestConfigMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestMapper;
import com.github.ibanetchep.msquests.core.platform.MSQuestsPlatform;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveHandler;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;
import com.github.ibanetchep.msquests.core.registry.ActionTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.ActorTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.service.QuestLifecycleService;
import com.github.ibanetchep.msquests.core.service.QuestPersistenceService;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.DbCredentials;
import com.github.ibanetchep.msquests.database.repository.ActorSqlRepository;
import com.github.ibanetchep.msquests.database.repository.QuestSqlRepository;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.BukkitLampConfig;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.nio.file.Path;

public final class MSQuestsPlugin extends JavaPlugin implements MSQuestsPlatform {

    private EventDispatcher eventDispatcher;

    private QuestFactory questFactory;

    private ActorTypeRegistry actorRegistry;
    private ObjectiveTypeRegistry objectiveTypeRegistry;
    private ActionTypeRegistry actionTypeRegistry;
    private QuestRegistry questRegistry;

    private QuestPersistenceService questPersistenceService;
    private QuestLifecycleService questLifecycleService;
    private QuestPlayerActorService questPlayerActorService;

    private GlobalConfig globalConfig;

    private Translator translator;

    private DbAccess dbAccess;
    private FoliaLib foliaLib;


    @Override
    public void onEnable() {
        actorRegistry = new ActorTypeRegistry();
        objectiveTypeRegistry = new ObjectiveTypeRegistry();
        actionTypeRegistry = new ActionTypeRegistry();
        questFactory = new QuestFactory(objectiveTypeRegistry);

        registerObjectiveTypes();
        registerActionTypes();
        registerActorTypes();

        loadConfig();
        loadDatabase();

        foliaLib = new FoliaLib(this);

        this.translator = new Translator(
                new File(getDataFolder(), "lang"),
                getGlobalConfig().language(),
                locale -> getResource("lang/" + locale + ".yml"),
                getLogger()
        );

        translator.load();

        eventDispatcher = new BukkitEventDispatcher(this);

        QuestConfigMapper questConfigMapper = new QuestConfigMapper(objectiveTypeRegistry, actionTypeRegistry);
        QuestGroupMapper questGroupMapper = new QuestGroupMapper(questConfigMapper);
        QuestMapper questEntryMapper = new QuestMapper(questFactory);

        questRegistry = new QuestRegistry();
        questPersistenceService = new QuestPersistenceService(
                getLogger(),
                questRegistry,
                new QuestConfigYamlRepository(Path.of(getDataFolder().toPath() + "/quests")),
                new ActorSqlRepository(dbAccess),
                new QuestSqlRepository(dbAccess),
                questGroupMapper,
                questEntryMapper
        );

        questLifecycleService = new QuestLifecycleService(eventDispatcher, questPersistenceService, questFactory);
        questPlayerActorService = new QuestPlayerActorService(this);

        registerListeners();
        registerCommands();

        questPersistenceService.loadQuestGroups();

        Bukkit.getOnlinePlayers().forEach(player -> questPlayerActorService.loadPlayerActor(player));
    }

    @Override
    public void onDisable() {
        dbAccess.closePool();
    }

    public void loadConfig() {
        GlobalConfigLoaderService globalConfigLoaderService = new GlobalConfigLoaderService(this);
        globalConfig = globalConfigLoaderService.load();
    }

    private void loadDatabase() {
        var databaseConfig = globalConfig.databaseConfig();

        var dbCredentials = new DbCredentials(
                databaseConfig.type(),
                databaseConfig.host(),
                databaseConfig.user(),
                databaseConfig.password(),
                databaseConfig.name(),
                databaseConfig.port(),
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
                                .addParameterType(Quest.class, new QuestParameterType(this))
                                .addParameterType(QuestConfig.class, new QuestConfigParameterType(this))
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
        pluginManager.registerEvents(new QuestStartListener(this), this);
        pluginManager.registerEvents(new QuestCompleteListener(this), this);
    }

    private void registerObjectiveTypes() {
        objectiveTypeRegistry.registerType(
                ObjectiveTypes.BLOCK_BREAK,
                BlockBreakObjectiveConfig::new,
                BlockBreakObjective.class,
                new BlockBreakObjectiveHandler(this)
        );
        objectiveTypeRegistry.registerType(
                ObjectiveTypes.DELIVER_ITEM,
                DeliverItemObjectiveConfig::new,
                DeliverItemObjective.class,
                new DeliverItemObjectiveHandler(this)
        );
        objectiveTypeRegistry.registerType(
                ObjectiveTypes.KILL_ENTITY,
                KillEntityObjectiveConfig::new,
                KillEntityObjective.class,
                new KillEntityObjectiveHandler(this)
        );

        for (QuestObjectiveHandler<?> handler : objectiveTypeRegistry.getHandlers().values()) {
            if(handler instanceof Listener listener) {
                getServer().getPluginManager().registerEvents(listener, this);
            }
        }
    }

    public void registerActionTypes() {
        actionTypeRegistry.registerType("command", CommandAction::new);
        actionTypeRegistry.registerType("player_command", PlayerCommandAction::new);
        actionTypeRegistry.registerType("give_item", GiveItemAction::new);
        actionTypeRegistry.registerType("message", PlayerMessageAction::new);
        actionTypeRegistry.registerType("action_bar", PlayerActionBarAction::new);
        actionTypeRegistry.registerType("title", PlayerTitleAction::new);
    }

    public void registerActorTypes() {
        actorRegistry.registerType("player", QuestPlayerActor.class);
        actorRegistry.registerType("global", QuestGlobalActor.class);
    }

    public QuestRegistry getQuestRegistry() {
        return questRegistry;
    }

    @Override
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    @Override
    public QuestPersistenceService getQuestPersistenceService() {
        return questPersistenceService;
    }

    @Override
    public QuestLifecycleService getQuestLifecycleService() {
        return this.questLifecycleService;
    }

    public QuestPlayerActorService getQuestPlayerActorService() {
        return this.questPlayerActorService;
    }

    public Translator getTranslator() {
        return translator;
    }

    @Override
    public ActorTypeRegistry getActorRegistry() {
        return actorRegistry;
    }

    @Override
    public ObjectiveTypeRegistry getObjectiveTypeRegistry() {
        return objectiveTypeRegistry;
    }

    public ActionTypeRegistry getActionTypeRegistry() {
        return actionTypeRegistry;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }
}
