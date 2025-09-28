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
import com.github.ibanetchep.msquests.bukkit.listener.*;
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
import com.github.ibanetchep.msquests.bukkit.service.QuestPlayerService;
import com.github.ibanetchep.msquests.core.cache.PlayerProfileCache;
import com.github.ibanetchep.msquests.core.cache.QuestActorCache;
import com.github.ibanetchep.msquests.core.cache.QuestCache;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.factory.QuestActionFactory;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.mapper.QuestConfigMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestMapper;
import com.github.ibanetchep.msquests.core.platform.MSQuestsPlatform;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicLocalObjectiveExecutor;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicLocalQuestExecutor;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicObjectiveExecutor;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicQuestExecutor;
import com.github.ibanetchep.msquests.core.registry.*;
import com.github.ibanetchep.msquests.core.repository.PlayerProfileRepository;
import com.github.ibanetchep.msquests.core.service.*;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.DbCredentials;
import com.github.ibanetchep.msquests.database.repository.ActorSqlRepository;
import com.github.ibanetchep.msquests.database.repository.PlayerProfileSqlRepository;
import com.github.ibanetchep.msquests.database.repository.QuestSqlRepository;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.BukkitLampConfig;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class MSQuestsPlugin extends JavaPlugin implements MSQuestsPlatform {

    private EventDispatcher eventDispatcher;

    private QuestFactory questFactory;
    private QuestObjectiveFactory questObjectiveFactory;
    private QuestActionFactory questActionFactory;

    private PlayerProfileCache playerProfileCache;
    private ActorTypeRegistry actorTypeRegistry;
    private QuestConfigRegistry questConfigRegistry;
    private QuestActorCache questActorCache;
    private QuestCache questCache;

    private QuestConfigService questConfigService;
    private PlayerProfileService playerProfileService;
    private QuestService questService;
    private QuestActorService questActorService;
    private QuestLifecycleService questLifecycleService;
    private QuestPlayerService questPlayerService;

    private GlobalConfig globalConfig;

    private Translator translator;

    private DbAccess dbAccess;
    private FoliaLib foliaLib;


    @Override
    public void onEnable() {
        foliaLib = new FoliaLib(this);
        eventDispatcher = new BukkitEventDispatcher(this);

        questConfigRegistry = new QuestConfigRegistry();
        questCache = new QuestCache();
        actorTypeRegistry = new ActorTypeRegistry();
        questObjectiveFactory = new QuestObjectiveFactory();
        questActionFactory = new QuestActionFactory();
        playerProfileCache = new PlayerProfileCache();
        questActorCache = new QuestActorCache();

        registerObjectiveTypes();
        registerActionTypes();
        registerActorTypes();

        loadConfig();
        loadDatabase();
        loadTranslator();

        questFactory = new QuestFactory(questObjectiveFactory);
        QuestMapper questMapper = new QuestMapper();

        QuestConfigYamlRepository questConfigRepository = new QuestConfigYamlRepository(Path.of(getDataFolder().toPath() + "/quests"));
        ActorSqlRepository actorRepository = new ActorSqlRepository(dbAccess);
        QuestSqlRepository questRepository = new QuestSqlRepository(dbAccess);
        PlayerProfileRepository playerProfileRepository = new PlayerProfileSqlRepository(dbAccess);

        QuestConfigMapper questConfigMapper = new QuestConfigMapper(questObjectiveFactory, questActionFactory);
        QuestGroupMapper questGroupMapper = new QuestGroupMapper(questConfigMapper);

        AtomicQuestExecutor atomicQuestExecutor = new AtomicLocalQuestExecutor();
        AtomicObjectiveExecutor atomicObjectiveExecutor = new AtomicLocalObjectiveExecutor(atomicQuestExecutor);

        questService = new QuestService(getLogger(), questConfigRegistry, questRepository, questFactory, questCache, questMapper);
        questActorService = new QuestActorService(getLogger(), actorRepository, questActorCache, playerProfileCache, questService);
        questConfigService = new QuestConfigService(getLogger(), questConfigRegistry, questConfigRepository, questGroupMapper);
        playerProfileService = new PlayerProfileService(getLogger(), playerProfileRepository, playerProfileCache, questActorCache);

        questLifecycleService = new QuestLifecycleService(eventDispatcher, questService, questFactory, questCache, atomicQuestExecutor, atomicObjectiveExecutor);
        questPlayerService = new QuestPlayerService(questActorService, playerProfileService);

        registerListeners();
        registerCommands();

        getScheduler().runTimer(() -> questService.saveDirtyQuests(), 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        dbAccess.closePool();
    }

    public void loadConfig() {
        GlobalConfigLoaderService globalConfigLoaderService = new GlobalConfigLoaderService(this);
        globalConfig = globalConfigLoaderService.load();
    }

    public void loadTranslator() {
        this.translator = new Translator(
                new File(getDataFolder(), "lang"),
                getGlobalConfig().language(),
                locale -> getResource("lang/" + locale + ".yml"),
                getLogger()
        );

        translator.load();
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
                                .addParameterType(QuestGroupConfig.class, new QuestGroupParameterType(this))
                                .addParameterType(Quest.class, new QuestParameterType(this))
                                .addParameterType(QuestConfig.class, new QuestConfigParameterType(this))
                )
                .suggestionProviders(providers -> {
                    providers.addProviderForAnnotation(QuestActorType.class, actorType -> {
                        return context -> actorTypeRegistry.getAllActorTypes().keySet();
                    });
                })
                .build();

        BukkitLampConfig.builder(this).disableBrigadier().build();

        lamp.register(new QuestAdminCommand(this));
    }

    public void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ServerLoadListener(this), this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new QuestStartListener(this), this);
        pluginManager.registerEvents(new QuestCompleteListener(this), this);
        pluginManager.registerEvents(new QuestProgressListener(this), this);
    }

    private void registerObjectiveTypes() {
        questObjectiveFactory.registerType(
                ObjectiveTypes.BLOCK_BREAK,
                BlockBreakObjectiveConfig::new,
                BlockBreakObjective::new
        );
        questObjectiveFactory.registerType(
                ObjectiveTypes.DELIVER_ITEM,
                DeliverItemObjectiveConfig::new,
                DeliverItemObjective::new
        );
        questObjectiveFactory.registerType(
                ObjectiveTypes.KILL_ENTITY,
                KillEntityObjectiveConfig::new,
                KillEntityObjective::new
        );

        getServer().getPluginManager().registerEvents(new BlockBreakObjectiveHandler(this), this);
        getServer().getPluginManager().registerEvents(new DeliverItemObjectiveHandler(this), this);
        getServer().getPluginManager().registerEvents(new KillEntityObjectiveHandler(this), this);
    }

    public void registerActionTypes() {
        questActionFactory.registerType("command", dto -> new CommandAction(dto, this));
        questActionFactory.registerType("player_command", dto -> new PlayerCommandAction(dto, this));
        questActionFactory.registerType("give_item", dto -> new GiveItemAction(dto, this));
        questActionFactory.registerType("message", dto -> new PlayerMessageAction(dto, this));
        questActionFactory.registerType("action_bar", dto -> new PlayerActionBarAction(dto, this));
        questActionFactory.registerType("title", dto -> new PlayerTitleAction(dto, this));
        questActionFactory.registerType("boss_bar", dto -> new PlayerBossBarAction(dto, this));
    }

    public void registerActorTypes() {
        actorTypeRegistry.registerType("player", QuestPlayerActor.class);
        actorTypeRegistry.registerType("global", QuestGlobalActor.class);
    }

    public QuestConfigRegistry getQuestConfigRegistry() {
        return questConfigRegistry;
    }

    @Override
    public PlayerProfileCache getPlayerProfileRegistry() {
        return playerProfileCache;
    }

    @Override
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public QuestService getQuestService() {
        return questService;
    }

    public QuestActorService getQuestActorService() {
        return questActorService;
    }

    @Override
    public QuestLifecycleService getQuestLifecycleService() {
        return this.questLifecycleService;
    }

    @Override
    public QuestActorCache getQuestActorRegistry() {
        return questActorCache;
    }

    public QuestPlayerService getQuestPlayerService() {
        return this.questPlayerService;
    }

    public QuestConfigService getQuestConfigService() {
        return this.questConfigService;
    }

    public PlayerProfileService getPlayerProfileService() {
        return this.playerProfileService;
    }

    public Translator getTranslator() {
        return translator;
    }

    @Override
    public ActorTypeRegistry getActorTypeRegistry() {
        return actorTypeRegistry;
    }

    @Override
    public QuestObjectiveFactory getObjectiveTypeRegistry() {
        return questObjectiveFactory;
    }

    public QuestActionFactory getQuestActionFactory() {
        return questActionFactory;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }
}
