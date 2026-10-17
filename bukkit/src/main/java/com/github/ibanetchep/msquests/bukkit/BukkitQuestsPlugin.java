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
import com.github.ibanetchep.msquests.bukkit.placeholder.QuestPlaceholders;
import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanChainRunner;
import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanConditionChecker;
import com.github.ibanetchep.msquests.core.quest.config.action.ChainRunner;
import com.github.ibanetchep.msquests.core.quest.config.condition.ConditionChecker;
import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanIntegration;
import com.github.ibanetchep.msquests.bukkit.artisan.EditorProject;
import com.github.ibanetchep.msquests.bukkit.config.ContentSource;
import com.github.ibanetchep.msquests.bukkit.document.MigrationMapFile;
import com.github.ibanetchep.msquests.bukkit.document.QuestGroupDocumentRepository;
import com.github.ibanetchep.msquests.bukkit.migration.ContentMigration;
import com.github.ibanetchep.msquests.bukkit.zmenu.ZMenuIntegration;
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
import com.github.ibanetchep.msquests.bukkit.service.TrackingBossBarService;
import com.github.ibanetchep.msquests.core.service.CronDistributionService;
import com.github.ibanetchep.msquests.core.service.QuestDistributionService;
import com.github.ibanetchep.msquests.core.service.QuestProgressService;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestActorRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
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
import com.github.ibanetchep.msquests.core.migration.MigrationMap;
import com.github.ibanetchep.msquests.core.repository.PlayerProfileRepository;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.core.service.*;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.DbCredentials;
import com.github.ibanetchep.msquests.database.LegacyDataCopier;
import com.github.ibanetchep.msquests.database.SqlTables;
import com.github.ibanetchep.msquests.database.repository.ArtisanQuestSqlRepository;
import com.github.ibanetchep.msquests.database.repository.ActorSqlRepository;
import com.github.ibanetchep.msquests.database.repository.PlayerProfileSqlRepository;
import com.github.ibanetchep.msquests.database.repository.QuestSqlRepository;
import com.github.ibanetchep.msquests.database.repository.RotationSqlRepository;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.BukkitLampConfig;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BukkitQuestsPlugin extends JavaPlugin implements MSQuestsPlatform {

    private EventDispatcher eventDispatcher;

    private QuestFactory questFactory;
    private QuestObjectiveFactory questObjectiveFactory;
    private ChainRunner chainRunner;
    private ConditionChecker conditionChecker;
    private QuestPlaceholders questPlaceholders;

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
    private TrackingBossBarService trackingBossBarService;

    private GlobalConfig globalConfig;

    private BukkitTranslator translator;

    private BlockPlaceTagListener blockPlaceTagListener;

    private DbAccess dbAccess;
    private FoliaLib foliaLib;

    private ArtisanIntegration artisanIntegration;

    private ContentSource contentSource;
    private @Nullable QuestGroupDocumentRepository documentRepository;
    /** Fingerprint of the Documents the loaded groups come from; null until the first load. */
    private volatile @Nullable String loadedFingerprint;


    @Override
    public void onEnable() {
        artisanIntegration = ArtisanIntegration.resolve(this);
        if (artisanIntegration == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        foliaLib = new FoliaLib(this);
        eventDispatcher = new BukkitEventDispatcher(this);

        questConfigRegistry = new QuestConfigRegistry();
        questRegistry = new QuestRegistry();
        actorTypeRegistry = new ActorTypeRegistry();
        playerProfileRegistry = new PlayerProfileRegistry();
        questActorRegistry = new QuestActorRegistry();

        // Quest actions and conditions are Artisan's: chains run by its engine, conditions read by it.
        chainRunner = new ArtisanChainRunner(this, artisanIntegration.getApi().getActions());
        conditionChecker = new ArtisanConditionChecker(artisanIntegration.getApi().getConditions(), getLogger());
        questObjectiveFactory = new QuestObjectiveFactory();

        registerObjectiveTypes();
        registerActorTypes();

        loadConfig();
        if (!loadDatabase()) {
            return;
        }
        loadTranslator();

        questFactory = new QuestFactory(questObjectiveFactory);
        QuestMapper questMapper = new QuestMapper();

        // Read once: the repositories below are bound to it until the next start.
        contentSource = globalConfig.contentSource();
        QuestConfigRepository questConfigRepository;
        QuestRepository questRepository;
        SqlTables tables;
        if (contentSource == ContentSource.ARTISAN) {
            if (!prepareArtisanStorage()) {
                return;
            }
            documentRepository = new QuestGroupDocumentRepository(EditorProject.directory(getDataFolder()).toPath());
            questConfigRepository = documentRepository;
            questRepository = new ArtisanQuestSqlRepository(dbAccess);
            tables = SqlTables.ARTISAN;
        } else {
            questConfigRepository = new QuestConfigYamlRepository(Path.of(getDataFolder().toPath() + "/quests"), getLogger(), this::langText);
            questRepository = new QuestSqlRepository(dbAccess);
            tables = SqlTables.LEGACY;
        }
        getLogger().info("Quests are read from " + (contentSource == ContentSource.ARTISAN
                ? "editor/quest_groups/ and the artisan_quests_* tables." : "quests/ and the msquests_* tables."));
        ActorSqlRepository actorRepository = new ActorSqlRepository(dbAccess, tables);
        RotationSqlRepository rotationRepository = new RotationSqlRepository(dbAccess, tables);
        PlayerProfileRepository playerProfileRepository = new PlayerProfileSqlRepository(dbAccess, tables);

        QuestConfigMapper questConfigMapper = new QuestConfigMapper(questObjectiveFactory);
        QuestGroupMapper questGroupMapper = new QuestGroupMapper(questConfigMapper);

        AtomicQuestExecutor atomicQuestExecutor = new AtomicLocalQuestExecutor(questRegistry);

        questService = new QuestService(getLogger(), questConfigRegistry, questRepository, rotationRepository, questFactory, questRegistry, questMapper);
        questConfigService = new QuestConfigService(getLogger(), questConfigRegistry, questConfigRepository, questGroupMapper, conditionChecker);
        playerProfileService = new PlayerProfileService(getLogger(), playerProfileRepository, playerProfileRegistry, questActorRegistry);
        questDistributionService = new QuestDistributionService(conditionChecker);
        questLifecycleService = new QuestLifecycleService(eventDispatcher, questService, questFactory, questRegistry, questConfigRegistry, atomicQuestExecutor, questDistributionService, rotationRepository, this, chainRunner);
        questActorService = new QuestActorService(getLogger(), actorRepository, questActorRegistry, playerProfileRegistry, questService, questLifecycleService, this);
        questPlayerService = new QuestPlayerService(questActorService, playerProfileService);
        questProgressService = new QuestProgressService(questLifecycleService, questService, eventDispatcher, questRegistry, chainRunner);
        trackingBossBarService = new TrackingBossBarService(globalConfig.trackingBossBar(), playerProfileRegistry);

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

    /**
     * First start with {@code content_source: artisan}: copies {@code msquests_*} into
     * {@code artisan_quests_*} (once) with the uids of {@code migration/map.yaml}. Nothing writes
     * to the old tables at this point, so no progress made before the restart is missed. Refuses
     * to start rather than run with empty progress on a server that has some.
     *
     * @return false when the plugin is being disabled
     */
    private boolean prepareArtisanStorage() {
        LegacyDataCopier copier = new LegacyDataCopier(dbAccess.getJdbi());
        try {
            if (copier.isCopied()) {
                return true;
            }
            MigrationMap map = MigrationMapFile.read(ContentMigration.mapFile(getDataFolder().toPath()));
            if (map == null) {
                if (copier.legacyQuestRows() > 0) {
                    getLogger().severe("content_source is artisan, but the quests were never converted: run"
                            + " /msquests migrate --confirm with content_source: legacy first. Disabling MSQuests;"
                            + " quests/ and the msquests_* tables are untouched.");
                    disableFor("no migration/map.yaml");
                    return false;
                }
                map = MigrationMap.empty(); // a server without saved progress has nothing to copy
            }
            LegacyDataCopier.Result result = copier.copy(map);
            getLogger().info("Saved progress copied into artisan_quests_*: " + result.summary());
            result.notes().forEach(note -> getLogger().warning("Not copied: " + note));
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Copying the saved progress into artisan_quests_* failed and was rolled back;"
                    + " the msquests_* tables are untouched. Set content_source: legacy to run as before.", e);
            disableFor("progress copy failed");
            return false;
        }
    }

    private void disableFor(String reason) {
        getLogger().severe("MSQuests disabled: " + reason + ".");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    /** Startup load of the quest groups, remembering which Documents it read. */
    public CompletableFuture<Void> loadQuestContent() {
        String fingerprint = documentRepository != null ? documentRepository.fingerprint() : null;
        return questConfigService.loadQuestGroups().thenRun(() -> loadedFingerprint = fingerprint);
    }

    /**
     * Replaces the quest groups and rebuilds every loaded actor on them: progress is flushed
     * first, and a refused reload (a broken file) changes nothing.
     */
    public CompletableFuture<Void> reloadQuestContent() {
        String fingerprint = documentRepository != null ? documentRepository.fingerprint() : null;
        questService.clearMissingConfigReports();
        // Write batched progress first: actors are rebuilt from the database just after.
        return questProgressService.flushPendingProgress()
                .thenCompose(v -> questConfigService.reloadQuestGroups())
                .thenRun(() -> loadedFingerprint = fingerprint)
                .thenCompose(v -> questActorService.reloadActors())
                .thenRun(() -> artisanIntegration.signalDataReload());
    }

    /**
     * Artisan reloaded its projects (a publish, {@code /artisan reload}). With
     * {@code content_source: artisan} the groups are reloaded when their Documents changed;
     * a reload of anything else leaves the quests alone.
     */
    public void onArtisanProjectsReloaded() {
        if (documentRepository == null || loadedFingerprint == null) {
            return; // legacy content, or the startup load has not happened yet
        }
        if (documentRepository.fingerprint().equals(loadedFingerprint)) {
            return;
        }
        getLogger().info("Quest group Documents changed, reloading the quests.");
        reloadQuestContent().exceptionally(e -> {
            getLogger().log(Level.SEVERE, "Quest reload refused, the previous quests are still active", e);
            return null;
        });
    }

    private void refreshActors(Object task) {
        for (QuestActor actor : questActorRegistry.getActors().values()) {
            questLifecycleService.refreshActor(actor);
        }
        trackingBossBarService.refreshAll();
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

    /**
     * @return false when the pool could not be opened, in which case the plugin is being
     * disabled and {@code onEnable} must stop: {@code disablePlugin} returns to its caller,
     * so carrying on would wire every service against a null {@code dbAccess}.
     */
    private boolean loadDatabase() {
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
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to connect to database, disabling plugin.", e);
            dbAccess = null;
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
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
        blockPlaceTagListener = new BlockPlaceTagListener(this);
        pluginManager.registerEvents(blockPlaceTagListener, this);
        pluginManager.registerEvents(new ServerLoadListener(this), this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new QuestTrackingListener(trackingBossBarService, questPlayerService), this);
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

    public void registerActorTypes() {
        actorTypeRegistry.registerType("player", BukkitQuestPlayerActor.class);
        actorTypeRegistry.registerType("global", BukkitQuestGlobalActor.class);
    }

    public void registerExpansions() {
        questPlaceholders = new QuestPlaceholders(playerProfileRegistry, questConfigRegistry, questActorRegistry,
                globalConfig.placeholders());
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new QuestsPlaceholderExpansion(questPlaceholders).register(); // %msquests_*%, alias for one version
        }
        if (Bukkit.getPluginManager().getPlugin("zMenu") != null) {
            new ZMenuIntegration(this).register();
        }
        artisanIntegration.register();
    }

    /** Never null once the plugin is enabled: Artisan is a required dependency. */
    public ArtisanIntegration getArtisanIntegration() {
        return artisanIntegration;
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

    public ConditionChecker getConditionChecker() {
        return conditionChecker;
    }

    public QuestPlaceholders getQuestPlaceholders() {
        return questPlaceholders;
    }

    /** Text of a MSQuests lang key, null when the lang files do not have it. */
    public @Nullable String langText(String key) {
        String text = getTranslator().getRaw(key);
        return text == null || text.startsWith("Missing translation: ") ? null : text;
    }

    /** The content source this run was started with (config changes apply at the next start). */
    public ContentSource getContentSource() {
        return contentSource;
    }

    /** Null when the database pool could not be opened. */
    public @Nullable DbAccess getDbAccess() {
        return dbAccess;
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

    public TrackingBossBarService getTrackingBossBarService() {
        return trackingBossBarService;
    }

    public BlockPlaceTagListener getBlockPlaceTagListener() {
        return blockPlaceTagListener;
    }

}
