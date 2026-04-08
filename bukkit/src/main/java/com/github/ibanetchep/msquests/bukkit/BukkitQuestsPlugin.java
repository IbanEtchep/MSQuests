package com.github.ibanetchep.msquests.bukkit;

import com.github.ibanetchep.msquests.bukkit.command.QuestAdminCommand;
import com.github.ibanetchep.msquests.bukkit.command.QuestCommand;
import com.github.ibanetchep.msquests.bukkit.command.annotations.QuestActorType;
import com.github.ibanetchep.msquests.bukkit.command.parametertypes.QuestActorParameterType;
import com.github.ibanetchep.msquests.bukkit.command.parametertypes.QuestConfigParameterType;
import com.github.ibanetchep.msquests.bukkit.command.parametertypes.QuestGroupConfigParameterType;
import com.github.ibanetchep.msquests.bukkit.command.parametertypes.QuestParameterType;
import com.github.ibanetchep.msquests.bukkit.config.GlobalConfig;
import com.github.ibanetchep.msquests.bukkit.event.BukkitEventDispatcher;
import com.github.ibanetchep.msquests.bukkit.lang.BukkitTranslator;
import com.github.ibanetchep.msquests.bukkit.listener.*;
import com.github.ibanetchep.msquests.bukkit.placeholderapi.QuestsPlaceholderExpansion;
import com.github.ibanetchep.msquests.bukkit.zmenu.ZMenuIntegration;
import com.github.ibanetchep.msquests.bukkit.quest.action.*;
import com.github.ibanetchep.msquests.bukkit.quest.condition.PlaceholderCondition;
import com.github.ibanetchep.msquests.bukkit.quest.condition.PermissionCondition;
import com.github.ibanetchep.msquests.core.factory.ConditionFactory;
import com.github.ibanetchep.msquests.bukkit.quest.actor.BukkitQuestGlobalActor;
import com.github.ibanetchep.msquests.bukkit.quest.actor.BukkitQuestPlayerActor;
import com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak.BlockBreakObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak.BlockBreakObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak.BlockBreakObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem.DeliverItemObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem.DeliverItemObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem.DeliverItemObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand.ExecuteCommandObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand.ExecuteCommandObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand.ExecuteCommandObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.fishing.FishingObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.fishing.FishingObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.fishing.FishingObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.placeholder.PlaceholderObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.placeholder.PlaceholderObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.placeholder.PlaceholderObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.breedanimal.BreedAnimalObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.breedanimal.BreedAnimalObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.breedanimal.BreedAnimalObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.craftitem.CraftItemObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.craftitem.CraftItemObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.craftitem.CraftItemObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop.HarvestCropObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop.HarvestCropObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop.HarvestCropObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.killentity.KillEntityObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.killentity.KillEntityObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.killentity.KillEntityObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.travel.TravelObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.travel.TravelObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.travel.TravelObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.repository.QuestConfigYamlRepository;
import com.github.ibanetchep.msquests.bukkit.service.GlobalConfigLoaderService;
import com.github.ibanetchep.msquests.bukkit.service.QuestPlayerService;
import com.github.ibanetchep.msquests.core.service.CronDistributionService;
import com.github.ibanetchep.msquests.core.service.QuestDistributionService;
import com.github.ibanetchep.msquests.core.service.QuestProgressService;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestActorRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.factory.QuestActionFactory;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.mapper.QuestConfigMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestMapper;
import com.github.ibanetchep.msquests.core.platform.MSQuestsPlatform;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicLocalQuestExecutor;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicQuestExecutor;
import com.github.ibanetchep.msquests.core.registry.*;
import com.github.ibanetchep.msquests.core.repository.PlayerProfileRepository;
import com.github.ibanetchep.msquests.core.service.*;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.DbCredentials;
import com.github.ibanetchep.msquests.database.repository.ActorSqlRepository;
import com.github.ibanetchep.msquests.database.repository.PlayerProfileSqlRepository;
import com.github.ibanetchep.msquests.database.repository.QuestSqlRepository;
import com.github.ibanetchep.msquests.database.repository.RotationSqlRepository;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BukkitQuestsPlugin extends JavaPlugin implements MSQuestsPlatform {

    private EventDispatcher eventDispatcher;

    private QuestFactory questFactory;
    private QuestObjectiveFactory questObjectiveFactory;
    private QuestActionFactory questActionFactory;

    private QuestRegistry questRegistry;
    private PlayerProfileRegistry playerProfileRegistry;
    private ActorTypeRegistry actorTypeRegistry;
    private QuestConfigRegistry questConfigRegistry;
    private QuestActorRegistry questActorRegistry;

    private QuestConfigService questConfigService;
    private PlayerProfileService playerProfileService;
    private QuestService questService;
    private QuestActorService questActorService;
    private QuestLifecycleService questLifecycleService;
    private QuestProgressService questProgressService;
    private QuestPlayerService questPlayerService;
    private QuestDistributionService questDistributionService;
    private CronDistributionService cronDistributionService;

    private GlobalConfig globalConfig;

    private BukkitTranslator translator;

    private DbAccess dbAccess;
    private FoliaLib foliaLib;


    @Override
    public void onEnable() {
        foliaLib = new FoliaLib(this);
        eventDispatcher = new BukkitEventDispatcher(this);

        questConfigRegistry = new QuestConfigRegistry();
        questRegistry = new QuestRegistry();
        actorTypeRegistry = new ActorTypeRegistry();
        playerProfileRegistry = new PlayerProfileRegistry();
        questActorRegistry = new QuestActorRegistry();

        ConditionFactory conditionFactory = buildConditionFactory();
        questActionFactory = new QuestActionFactory(conditionFactory);
        questObjectiveFactory = new QuestObjectiveFactory(conditionFactory);

        registerObjectiveTypes();
        registerActionTypes();
        registerActorTypes();

        loadConfig();
        loadDatabase();
        loadTranslator();

        questFactory = new QuestFactory(questObjectiveFactory);
        QuestMapper questMapper = new QuestMapper();

        QuestConfigYamlRepository questConfigRepository = new QuestConfigYamlRepository(Path.of(getDataFolder().toPath() + "/quests"), getLogger());
        ActorSqlRepository actorRepository = new ActorSqlRepository(dbAccess);
        QuestSqlRepository questRepository = new QuestSqlRepository(dbAccess);
        RotationSqlRepository rotationRepository = new RotationSqlRepository(dbAccess);
        PlayerProfileRepository playerProfileRepository = new PlayerProfileSqlRepository(dbAccess);

        QuestConfigMapper questConfigMapper = new QuestConfigMapper(questObjectiveFactory, questActionFactory, conditionFactory);
        QuestGroupMapper questGroupMapper = new QuestGroupMapper(questConfigMapper, questActionFactory);

        AtomicQuestExecutor atomicQuestExecutor = new AtomicLocalQuestExecutor(questRegistry);

        questService = new QuestService(getLogger(), questConfigRegistry, questRepository, rotationRepository, questFactory, questRegistry, questMapper);
        questConfigService = new QuestConfigService(getLogger(), questConfigRegistry, questConfigRepository, questGroupMapper);
        playerProfileService = new PlayerProfileService(getLogger(), playerProfileRepository, playerProfileRegistry, questActorRegistry);
        questDistributionService = new QuestDistributionService();
        questLifecycleService = new QuestLifecycleService(eventDispatcher, questService, questFactory, questRegistry, questConfigRegistry, atomicQuestExecutor, questDistributionService, rotationRepository);
        questActorService = new QuestActorService(getLogger(), actorRepository, questActorRegistry, playerProfileRegistry, questService, questLifecycleService, this);
        questPlayerService = new QuestPlayerService(questActorService, playerProfileService);
        questProgressService = new QuestProgressService(questLifecycleService, questService, eventDispatcher);

        registerListeners();
        registerCommands();
        registerExpansions();

        cronDistributionService = new CronDistributionService(questConfigRegistry, questActorRegistry, questLifecycleService);

        getScheduler().runTimer(questProgressService::flushPendingProgress, 1, 1, TimeUnit.SECONDS);
        getScheduler().runTimer(this::refreshActors, 1, 1, TimeUnit.MINUTES);
        getScheduler().runTimer(task -> cronDistributionService.tick(), 30, 30, TimeUnit.SECONDS);

        UUID globalActorUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        BukkitQuestGlobalActor actor = new BukkitQuestGlobalActor(globalActorUUID, "default");
        questActorService.loadActor(actor);
    }

    private void refreshActors(Object task) {
        for (QuestActor actor : questActorRegistry.getActors().values()) {
            questLifecycleService.refreshActor(actor);
        }
    }

    @Override
    public void onDisable() {
        if (questProgressService != null) {
            questProgressService.flushPendingProgress().join();
        }
        if (dbAccess != null) {
            dbAccess.closePool();
        }
    }

    public void loadConfig() {
        GlobalConfigLoaderService globalConfigLoaderService = new GlobalConfigLoaderService(this);
        globalConfig = globalConfigLoaderService.load();
    }

    public void loadTranslator() {
        this.translator = new BukkitTranslator(
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
                                .addParameterType(QuestGroupConfig.class, new QuestGroupConfigParameterType(this))
                                .addParameterType(Quest.class, new QuestParameterType(this))
                                .addParameterType(QuestConfig.class, new QuestConfigParameterType(this))
                )
                .suggestionProviders(providers -> {
                    providers.addProviderForAnnotation(QuestActorType.class, actorType -> {
                        return context -> actorTypeRegistry.getAllActorTypes().keySet();
                    });
                })
                .build();

        BukkitLampConfig.builder(this).build();

        lamp.register(new QuestAdminCommand(this));
        lamp.register(new QuestCommand(this));
    }

    public void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ServerLoadListener(this), this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
    }

    private ConditionFactory buildConditionFactory() {
        ConditionFactory factory = new ConditionFactory();
        factory.register("placeholder", PlaceholderCondition::new);
        factory.register("permission", PermissionCondition::new);
        return factory;
    }

    private void registerObjectiveTypes() {
        questObjectiveFactory.register(BlockBreakObjectiveConfig.class, BlockBreakObjective.class, new BlockBreakObjectiveHandler(this));
        questObjectiveFactory.register(BreedAnimalObjectiveConfig.class, BreedAnimalObjective.class, new BreedAnimalObjectiveHandler(this));
        questObjectiveFactory.register(CraftItemObjectiveConfig.class, CraftItemObjective.class, new CraftItemObjectiveHandler(this));
        questObjectiveFactory.register(HarvestCropObjectiveConfig.class, HarvestCropObjective.class, new HarvestCropObjectiveHandler(this));
        questObjectiveFactory.register(DeliverItemObjectiveConfig.class, DeliverItemObjective.class, new DeliverItemObjectiveHandler(this));
        questObjectiveFactory.register(KillEntityObjectiveConfig.class, KillEntityObjective.class, new KillEntityObjectiveHandler(this));
        questObjectiveFactory.register(ExecuteCommandObjectiveConfig.class, ExecuteCommandObjective.class, new ExecuteCommandObjectiveHandler(this));
        questObjectiveFactory.register(FishingObjectiveConfig.class, FishingObjective.class, new FishingObjectiveHandler(this));
        questObjectiveFactory.register(PlaceholderObjectiveConfig.class, PlaceholderObjective.class, new PlaceholderObjectiveHandler(this));
        questObjectiveFactory.register(TravelObjectiveConfig.class, TravelObjective.class, new TravelObjectiveHandler(this));
    }

    public void registerActionTypes() {
        questActionFactory.register(CommandAction.class, dto -> new CommandAction(dto, this));
        questActionFactory.register(PlayerCommandAction.class, dto -> new PlayerCommandAction(dto, this));
        questActionFactory.register(GiveItemAction.class, dto -> new GiveItemAction(dto, this));
        questActionFactory.register(PlayerMessageAction.class, dto -> new PlayerMessageAction(dto, this));
        questActionFactory.register(PlayerActionBarAction.class, dto -> new PlayerActionBarAction(dto, this));
        questActionFactory.register(PlayerTitleAction.class, dto -> new PlayerTitleAction(dto, this));
        questActionFactory.register(PlayerBossBarAction.class, dto -> new PlayerBossBarAction(dto, this));
    }

    public void registerActorTypes() {
        actorTypeRegistry.registerType("player", BukkitQuestPlayerActor.class);
        actorTypeRegistry.registerType("global", BukkitQuestGlobalActor.class);
    }

    public void registerExpansions() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new QuestsPlaceholderExpansion(playerProfileRegistry, questConfigRegistry).register();
        }
        if (Bukkit.getPluginManager().getPlugin("zMenu") != null) {
            new ZMenuIntegration(this).register();
        }
    }

    public QuestConfigRegistry getQuestConfigRegistry() {
        return questConfigRegistry;
    }

    @Override
    public PlayerProfileRegistry getPlayerProfileRegistry() {
        return playerProfileRegistry;
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
    public QuestActorRegistry getQuestActorRegistry() {
        return questActorRegistry;
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

    public BukkitTranslator getTranslator() {
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

    @Override
    public void runSync(Runnable task) {
        getScheduler().runNextTick(t -> task.run());
    }

    public QuestProgressService getQuestProgressService() {
        return questProgressService;
    }

    public QuestDistributionService getQuestDistributionService() {
        return questDistributionService;
    }

}
