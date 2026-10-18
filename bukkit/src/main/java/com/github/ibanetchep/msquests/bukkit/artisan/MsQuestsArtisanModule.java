package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.placeholder.QuestPlaceholders;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ContributableObjective;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionStrategy;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.quest.result.QuestRotateResult;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import com.github.ibanetchep.msquests.core.registry.ActorResolver;
import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.ArtisanModule;
import net.artisanmc.modules.api.action.ActionDeclaration;
import net.artisanmc.modules.api.command.CommandDeclaration;
import net.artisanmc.modules.api.datasource.DataReloadEvent;
import net.artisanmc.modules.api.datasource.DataSourceDeclaration;
import net.artisanmc.modules.api.datasource.DataSourceField;
import net.artisanmc.modules.api.datasource.FieldKind;
import net.artisanmc.modules.api.datasource.PlayerRequiredException;
import net.artisanmc.modules.api.datasource.Stability;
import net.artisanmc.modules.api.document.DocumentKindDeclaration;
import net.artisanmc.modules.api.editor.EditorBundleDeclaration;
import net.artisanmc.modules.api.template.TemplateDeclaration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * MSQuests inside Artisan:
 * <ul>
 *   <li>an example template, {@code daily} ({@link #TEMPLATE}): quests any project can copy from
 *       the editor, seeded once into the ordinary project {@code quests} ({@link EditorProject});</li>
 *   <li>the {@code quest_group} Document kind ({@code quest_groups/}, {@code quest_group/v1}): one
 *       Document per group, which the quests load from with {@code content_source: artisan}
 *       ({@code /msquests migrate} converts {@code quests/*.yml} into them);</li>
 *   <li>its editor view, {@code artisan-editor/ui.js} built from {@code editor/}, which the Artisan
 *       editor loads for the {@code quest_group} kind;</li>
 *   <li>the {@code quests:*} actions ({@link QuestActions});</li>
 *   <li>the {@code %quests_*%} placeholders ({@code QuestPlaceholders}), also given to
 *       PlaceholderAPI when it is installed;</li>
 *   <li>three data sources and the two commands menu buttons used before the actions existed.</li>
 * </ul>
 *
 * <p>Every fetcher serves from the in-memory registries only: they run on the main thread
 * at menu render time, so no repository call is allowed here.
 *
 * <h2>Parameters</h2>
 * <ul>
 *   <li>{@code player} — target a player other than the viewer (by name, must be online).
 *       Absent → the viewer.</li>
 *   <li>{@code actor} — which actor's quests to read: {@code player} (default),
 *       {@code global}, or any other registered actor type.</li>
 *   <li>{@code group} — restrict to one quest group ({@code msquests:quests} and
 *       {@code msquests:active}).</li>
 * </ul>
 */
public final class MsQuestsArtisanModule implements ArtisanModule {

    static final String GROUPS = "msquests:groups";
    static final String QUESTS = "msquests:quests";
    static final String ACTIVE = "msquests:active";

    static final String GROUP_DIR = "quest_groups";
    static final String GROUP_SCHEMA = "quest_group/v1";

    /** Built by {@code editor/} ({@code npm run build}); the SDK version its build prints. */
    static final String EDITOR_BUNDLE = "artisan-editor/ui.js";
    static final String EDITOR_SDK = "1.4.0";

    /** The example quests: raise {@link #TEMPLATE_VERSION} whenever its files change. */
    static final String TEMPLATE = "daily";
    static final int TEMPLATE_VERSION = 1;
    static final String TEMPLATE_ROOT = "artisan-templates/daily";
    static final List<String> TEMPLATE_FILES = List.of("quest_groups/example_daily.yaml");

    private final BukkitQuestsPlugin plugin;
    private final ActorResolver actors;
    private @Nullable ArtisanAPI api;

    public MsQuestsArtisanModule(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
        this.actors = new ActorResolver(plugin.getQuestActorRegistry());
    }

    @Override
    public String getId() {
        return "msquests";
    }

    @Override
    public String getSchemaVersion() {
        return "msquests/v1";
    }

    @Override
    public void onEnable(ArtisanAPI api) {
        this.api = api;

        api.getDocumentKinds().register(new DocumentKindDeclaration(
                QuestActions.GROUP_KIND, GROUP_DIR, List.of(GROUP_SCHEMA), null));
        moveLegacyProject();
        registerTemplate(api);
        registerEditorBundle(api);

        QuestActions actions = new QuestActions(plugin);
        for (ActionDeclaration declaration : actions.declarations()) {
            api.getActionRegistry().register(declaration);
        }

        registerPlaceholders(api);

        // STATIC: the key-set is the loaded configuration, enumerable without a player,
        // so a menu can place "the daily group" on a specific slot. Only the per-actor
        // field values vary at render time.
        api.getDataSources().register(new DataSourceDeclaration(
                GROUPS,
                (player, params) -> groupRows(player, params),
                List.of(
                        new DataSourceField("key", FieldKind.STRING, "Group key"),
                        new DataSourceField("name", FieldKind.STRING, null),
                        new DataSourceField("description", FieldKind.STRING, null),
                        new DataSourceField("actor_type", FieldKind.STRING, "PLAYER / GLOBAL / …"),
                        new DataSourceField("max_active", FieldKind.INTEGER, null),
                        new DataSourceField("max_per_period", FieldKind.INTEGER, null),
                        new DataSourceField("rotatable", FieldKind.BOOLEAN, null),
                        new DataSourceField("period_end", FieldKind.INTEGER,
                                "End of the current period, epoch millis — render with {period_end.until()}"),
                        new DataSourceField("active_count", FieldKind.INTEGER, "Quests in progress for the actor"),
                        new DataSourceField("completed_count", FieldKind.INTEGER, null),
                        new DataSourceField("can_rotate", FieldKind.BOOLEAN, null),
                        new DataSourceField("quests", FieldKind.LIST, "Nested msquests:quests rows")),
                Stability.STATIC,
                "key"));

        api.getDataSources().register(new DataSourceDeclaration(
                QUESTS,
                (player, params) -> questRows(player, params),
                List.of(
                        new DataSourceField("id", FieldKind.STRING, "group:key — unique across groups"),
                        new DataSourceField("group", FieldKind.STRING, null),
                        new DataSourceField("key", FieldKind.STRING, null),
                        new DataSourceField("name", FieldKind.STRING, null),
                        new DataSourceField("description", FieldKind.STRING, null),
                        new DataSourceField("duration", FieldKind.INTEGER, "Seconds"),
                        new DataSourceField("tier", FieldKind.STRING, null),
                        new DataSourceField("tier_name", FieldKind.STRING, null),
                        new DataSourceField("assigned", FieldKind.BOOLEAN,
                                "Handed to the actor for the current period — filter on it "
                                        + "to show only the player's quests"),
                        new DataSourceField("status_key", FieldKind.STRING,
                                "Current attempt: NOT_STARTED / IN_PROGRESS / COMPLETED / FAILED"),
                        new DataSourceField("status", FieldKind.STRING, "Translated status"),
                        new DataSourceField("can_start", FieldKind.BOOLEAN,
                                "May the actor start it right now"),
                        new DataSourceField("start_blocker", FieldKind.STRING,
                                "Empty when can_start, else the QuestStartResult reason "
                                        + "(MAX_ACTIVE_REACHED, CONDITIONS_NOT_MET, …)"),
                        new DataSourceField("start_blocker_label", FieldKind.STRING, "Translated reason"),
                        new DataSourceField("objective_name", FieldKind.STRING, "Current objective"),
                        new DataSourceField("objective_progress", FieldKind.INTEGER, "Raw — the menu formats it"),
                        new DataSourceField("objective_target", FieldKind.INTEGER, null),
                        new DataSourceField("progress_ratio", FieldKind.NUMBER, "0..1"),
                        new DataSourceField("stages", FieldKind.LIST,
                                "Configured tree: key, name, flow, index, completed, active, "
                                        + "progress_ratio, objectives[] — player progress laid over it")),
                Stability.STATIC,
                "id"));

        // DYNAMIC: runtime instances, per actor and per period — pagination only.
        api.getDataSources().register(new DataSourceDeclaration(
                ACTIVE,
                (player, params) -> activeRows(player, params),
                List.of(
                        new DataSourceField("id", FieldKind.STRING, "Quest instance UUID"),
                        new DataSourceField("quest_key", FieldKind.STRING, null),
                        new DataSourceField("group", FieldKind.STRING, null),
                        new DataSourceField("name", FieldKind.STRING, null),
                        new DataSourceField("description", FieldKind.STRING, null),
                        new DataSourceField("assigned", FieldKind.BOOLEAN,
                                "Handed to the actor for the current period — filter on it "
                                        + "to show only the player's quests"),
                        new DataSourceField("status_key", FieldKind.STRING, null),
                        new DataSourceField("status", FieldKind.STRING, null),
                        new DataSourceField("tracked", FieldKind.BOOLEAN, "Is the viewer's tracked quest"),
                        new DataSourceField("stage_index", FieldKind.INTEGER, null),
                        new DataSourceField("stage_count", FieldKind.INTEGER, null),
                        new DataSourceField("stage_name", FieldKind.STRING, null),
                        new DataSourceField("stage_flow", FieldKind.STRING, "PARALLEL / SEQUENTIAL"),
                        new DataSourceField("progress_ratio", FieldKind.NUMBER, "0..1"),
                        new DataSourceField("objectives", FieldKind.LIST, "Flattened across stages"),
                        new DataSourceField("stages", FieldKind.LIST, "Same tree as msquests:quests"),
                        new DataSourceField("expires_at", FieldKind.INTEGER, "Epoch millis, or 0")),
                Stability.DYNAMIC,
                null));

        api.getCommands().register(new CommandDeclaration("msquests:track",
                (player, args) -> track(player, argv(args))));
        api.getCommands().register(new CommandDeclaration("msquests:rotate",
                (player, args) -> rotate(player, argv(args))));
        api.getCommands().register(new CommandDeclaration("msquests:contribute",
                (player, args) -> contribute(player, argv(args))));
    }

    /**
     * {@code plugins/MSQuests/editor/}, the project MSQuests registered itself until quests lived
     * in ordinary projects, becomes the project {@code quests} once, on the first start that finds
     * quest groups in it. Before the template, so the seed then finds that project and leaves it.
     */
    private void moveLegacyProject() {
        File target = plugin.getArtisanIntegration().questsProject();
        try {
            if (EditorProject.moveLegacy(plugin.getDataFolder(), target)) {
                plugin.getLogger().info("plugins/MSQuests/" + EditorProject.LEGACY_DIR
                        + "/ moved to " + target + ": it is now an ordinary Artisan project.");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not move plugins/MSQuests/" + EditorProject.LEGACY_DIR
                    + "/ to " + target + "; move it by hand, its quest groups are not loaded meanwhile", e);
        }
    }

    /**
     * The example quests, offered to every project in the editor and copied once into a new
     * project {@code quests} on the first start. A failure leaves the quests running without it.
     */
    private void registerTemplate(ArtisanAPI api) {
        boolean french = "fr".equals(EditorProject.defaultLang(plugin.getGlobalConfig().language()));
        try {
            api.getTemplates().register(TemplateDeclaration
                    .fromResources(getClass().getClassLoader(), TEMPLATE_ROOT, TEMPLATE, TEMPLATE_VERSION, TEMPLATE_FILES)
                    .title(french ? "Quêtes journalières" : "Daily quests")
                    .seed(EditorProject.PROJECT_ID)
                    .build());
        } catch (IllegalArgumentException | IllegalStateException e) {
            plugin.getLogger().log(Level.WARNING, "The example quests are not available", e);
        }
    }

    /**
     * Hands the editor view to Artisan, which serves it to the editor. Artisan before 2.3.0
     * has no editor bundles: the quests then run without a view in the editor.
     */
    private void registerEditorBundle(ArtisanAPI api) {
        if (!api.isCompatibleWith("2.3.0")) {
            plugin.getLogger().warning("Artisan " + api.getApiVersion()
                    + " cannot serve the quest editor view; it needs Artisan API 2.3.0 or later.");
            return;
        }
        try {
            api.getEditorBundles().register(
                    EditorBundleDeclaration.fromResource(getClass().getClassLoader(), EDITOR_BUNDLE, EDITOR_SDK));
        } catch (IllegalArgumentException | IllegalStateException e) {
            plugin.getLogger().log(Level.WARNING, "The quest editor view is not available", e);
        }
    }

    private void registerPlaceholders(ArtisanAPI api) {
        QuestPlaceholders placeholders = plugin.getQuestPlaceholders();
        try {
            api.getPlaceholders().register(QuestPlaceholders.NAMESPACE,
                    (player, key) -> placeholders.resolve(player != null ? player.getUniqueId() : null, key), true);
        } catch (IllegalStateException | IllegalArgumentException e) {
            plugin.getLogger().warning("The %" + QuestPlaceholders.NAMESPACE + "_*% placeholders are not available: "
                    + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (api != null) {
            api.getPlaceholders().unregister(QuestPlaceholders.NAMESPACE);
        }
        this.api = null;
    }

    /**
     * Fires when Artisan reloads its projects (a publish, {@code /artisan reload}). With
     * {@code content_source: artisan} the quests are read from this module's
     * {@code quest_group} Documents, and reload when those changed; otherwise nothing to do.
     */
    @Override
    public void onReload() {
        plugin.onArtisanProjectsReloaded();
    }

    /** Tells Artisan menus to re-fetch — call after a quest config reload. */
    public void signalDataReload() {
        if (api == null) return;
        api.getEvents().publish(new DataReloadEvent(GROUPS));
        api.getEvents().publish(new DataReloadEvent(QUESTS));
        api.getEvents().publish(new DataReloadEvent(ACTIVE));
    }

    // --------------------------------------------------------------- fetchers

    private List<Map<String, Object>> groupRows(@Nullable Player player, Map<String, ?> params) {
        QuestActor actor = resolveActor(player, params);
        Translator translator = plugin.getTranslator();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (QuestGroupConfig group : orderedGroups()) {
            rows.add(QuestRows.groupRow(
                    group, actorGroupOf(actor, group), startCheck(actor, group, player), translator));
        }
        return rows;
    }

    private List<Map<String, Object>> questRows(@Nullable Player player, Map<String, ?> params) {
        QuestActor actor = resolveActor(player, params);
        Translator translator = plugin.getTranslator();
        String groupFilter = string(params.get("group"));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (QuestGroupConfig group : orderedGroups()) {
            if (groupFilter != null && !groupFilter.equals(group.getKey())) continue;
            ActorQuestGroup actorGroup = actorGroupOf(actor, group);
            Function<QuestConfig, QuestStartResult> startCheck = startCheck(actor, group, player);
            for (QuestConfig config : group.getOrderedQuests()) {
                rows.add(QuestRows.questRow(group, config, actorGroup, startCheck, translator));
            }
        }
        return rows;
    }

    /**
     * Curries {@code canStartQuest} on the resolved actor, so each row can say whether the
     * player may start it and what blocks it otherwise. Null without an actor: startability
     * is a property of a player facing a quest, not of the quest.
     *
     * <p>The player profile is what carries the quest conditions, so it must be the profile
     * of whoever the rows describe — {@code null} simply skips condition evaluation.
     */
    private @Nullable Function<QuestConfig, QuestStartResult> startCheck(@Nullable QuestActor actor,
                                                                         QuestGroupConfig group,
                                                                         @Nullable Player player) {
        if (actor == null) return null;
        QuestDistributionStrategy strategy = group.getDistributionStrategy();
        PlayerProfile profile = player != null
                ? plugin.getPlayerProfileRegistry().getPlayerProfile(player.getUniqueId())
                : null;
        return config -> plugin.getQuestDistributionService()
                .canStartQuest(actor, config, strategy, profile);
    }

    private List<Map<String, Object>> activeRows(@Nullable Player player, Map<String, ?> params) {
        // The `player` param names a target, so an editor preview (no viewer) is
        // perfectly able to read this source — it just has to say whom. Throwing on
        // a null VIEWER alone would make the source un-previewable for good.
        UUID targetId = targetId(player, params);
        QuestActor actor = actors.resolve(targetId, string(params.get("actor")));
        if (actor == null) {
            throw new PlayerRequiredException(string(params.get("player")) != null
                    // Named but unresolved: online-only, and worth saying so rather
                    // than returning an empty list the caller has to interpret.
                    ? "player '" + string(params.get("player")) + "' is not online"
                    : "msquests:active needs a player — pass the `player` parameter to preview it");
        }

        String groupFilter = string(params.get("group"));
        PlayerProfile profile = targetId != null
                ? plugin.getPlayerProfileRegistry().getPlayerProfile(targetId)
                : null;
        UUID tracked = profile != null ? profile.getTrackedQuestId() : null;
        Translator translator = plugin.getTranslator();

        return actor.getQuests().values().stream()
                .filter(Quest::isActive)
                .filter(quest -> groupFilter == null
                        || (quest.getQuestGroup() != null && groupFilter.equals(quest.getQuestGroup().getKey())))
                .sorted(Comparator.comparing(Quest::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(quest -> quest.getId().toString()))
                .map(quest -> QuestRows.activeRow(quest, tracked, translator))
                .toList();
    }

    // --------------------------------------------------------------- commands

    private void track(Player player, List<String> argv) {
        if (argv.isEmpty()) return;
        Quest quest = findQuest(player, argv.get(0));
        if (quest == null) return;

        PlayerProfile profile = plugin.getPlayerProfileRegistry().getPlayerProfile(player.getUniqueId());
        if (profile == null) return;

        boolean untrack = quest.getId().equals(profile.getTrackedQuestId());
        profile.setTrackedQuestId(untrack ? null : quest.getId());
        plugin.getPlayerProfileService().saveProfile(profile);

        if (plugin.getTrackingBossBarService().isEnabled()) {
            if (untrack) plugin.getTrackingBossBarService().hideBossBar(player);
            else plugin.getTrackingBossBarService().showBossBar(player);
        }

        player.sendMessage(untrack
                ? MessageBuilder.translatable(TranslationKey.QUEST_COMMAND_UNTRACKED).toComponent()
                : MessageBuilder.translatable(TranslationKey.QUEST_COMMAND_TRACKED)
                        .applyPlaceholderResolver(quest).toComponent());
    }

    private void rotate(Player player, List<String> argv) {
        if (argv.isEmpty()) return;
        Quest quest = findQuest(player, argv.get(0));
        if (quest == null) return;

        QuestActor actor = quest.getActor();
        if (actor == null) return;

        QuestRotateResult result = plugin.getQuestLifecycleService().rotateQuest(actor, quest);
        player.sendMessage(MessageBuilder.translatable(result).toComponent());
    }

    /**
     * Hands over to every in-progress {@link ContributableObjective} of the quest whatever the
     * player can give — items from the inventory, money from a bank — and reports each one.
     */
    private void contribute(Player player, List<String> argv) {
        if (argv.isEmpty()) return;
        Quest quest = findQuest(player, argv.get(0));
        if (quest == null) return;

        PlayerProfile profile = plugin.getPlayerProfileRegistry().getPlayerProfile(player.getUniqueId());
        for (QuestObjective objective : quest.getActiveObjectives()) {
            if (!(objective instanceof ContributableObjective contributable) || objective.isCompleted()) continue;

            ContributableObjective.Contribution[] contribution = new ContributableObjective.Contribution[1];
            boolean allowed = plugin.getQuestProgressService().contributeNow(objective, () -> {
                contribution[0] = contributable.contribute(player);
                return contribution[0].amount();
            }, profile);
            if (!allowed) continue; // refused by another plugin: nothing was taken, nothing to report

            MessageBuilder message = MessageBuilder.translatable(contribution[0].messageKey());
            contribution[0].placeholders().forEach(message::placeholder);
            player.sendMessage(message.toComponent());
        }
    }

    /**
     * Accepts either an instance id (the {@code id} of an {@code msquests:active} row) or a
     * {@code group:key} config ref (the {@code id} of an {@code msquests:quests} row), and
     * looks it up across every actor the player belongs to — so a guild quest resolves the
     * same way a personal one does.
     */
    private @Nullable Quest findQuest(Player player, String ref) {
        UUID playerId = player.getUniqueId();
        List<Quest> candidates = plugin.getQuestActorRegistry().getActors().values().stream()
                .filter(actor -> actor.isMember(playerId))
                .flatMap(actor -> actor.getQuests().values().stream())
                .filter(Quest::isActive)
                .toList();

        for (Quest quest : candidates) {
            if (quest.getId().toString().equalsIgnoreCase(ref)) return quest;
        }
        int separator = ref.indexOf(':');
        String group = separator > 0 ? ref.substring(0, separator) : null;
        String key = separator > 0 ? ref.substring(separator + 1) : ref;
        for (Quest quest : candidates) {
            QuestGroupConfig questGroup = quest.getQuestGroup();
            boolean groupMatches = group == null || (questGroup != null && group.equals(questGroup.getKey()));
            if (groupMatches && key.equals(quest.getQuestConfig().getKey())) return quest;
        }
        return null;
    }

    // ---------------------------------------------------------------- helpers

    private List<QuestGroupConfig> orderedGroups() {
        return plugin.getQuestConfigRegistry().getQuestGroupConfigs().values().stream()
                .sorted(Comparator.comparing(QuestGroupConfig::getKey))
                .toList();
    }

    /** Null when the actor does not carry this group (its actor type differs). */
    private @Nullable ActorQuestGroup actorGroupOf(@Nullable QuestActor actor, QuestGroupConfig group) {
        return actor != null ? actor.getActorQuestGroup(group) : null;
    }

    private @Nullable QuestActor resolveActor(@Nullable Player player, Map<String, ?> params) {
        return actors.resolve(targetId(player, params), string(params.get("actor")));
    }

    /**
     * The {@code player} param names an online player; anything else (offline, unknown)
     * resolves to no actor and the rows degrade to neutral values. Loading an offline
     * actor would mean hitting the database from a render-time fetcher.
     */
    private @Nullable UUID targetId(@Nullable Player player, Map<String, ?> params) {
        String name = string(params.get("player"));
        if (name != null) {
            Player target = Bukkit.getPlayerExact(name);
            return target != null ? target.getUniqueId() : null;
        }
        return player != null ? player.getUniqueId() : null;
    }

    private static @Nullable String string(@Nullable Object value) {
        if (!(value instanceof String text) || text.isBlank()) return null;
        return text;
    }

    private static List<String> argv(Map<String, ?> args) {
        return args.get("argv") instanceof List<?> list
                ? list.stream().map(String::valueOf).toList()
                : List.of();
    }
}
