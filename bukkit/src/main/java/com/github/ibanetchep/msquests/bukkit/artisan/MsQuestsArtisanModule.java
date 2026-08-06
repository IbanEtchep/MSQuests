package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionStrategy;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.quest.result.QuestRotateResult;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import com.github.ibanetchep.msquests.core.registry.ActorResolver;
import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.ArtisanModule;
import net.artisanmc.modules.api.CommandDeclaration;
import net.artisanmc.modules.api.DataReloadEvent;
import net.artisanmc.modules.api.DataSourceDeclaration;
import net.artisanmc.modules.api.DataSourceField;
import net.artisanmc.modules.api.FieldKind;
import net.artisanmc.modules.api.PlayerRequiredException;
import net.artisanmc.modules.api.Stability;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Exposes MSQuests to Artisan menus: three data sources and the two commands their
 * buttons need. Read-only on the config side — quest authoring stays in {@code quests/*.yml}
 * for now (Artisan config screens are the next step).
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
                null,
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
                        new DataSourceField("rewards", FieldKind.LIST, "type, name"),
                        new DataSourceField("stages", FieldKind.LIST,
                                "Configured tree: key, name, flow, index, completed, active, "
                                        + "progress_ratio, objectives[] — player progress laid over it")),
                null,
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
                null,
                Stability.DYNAMIC,
                null));

        api.getCommands().register(new CommandDeclaration("msquests:track", (player, args) -> {
            track(player, argv(args));
            return kotlin.Unit.INSTANCE;
        }));
        api.getCommands().register(new CommandDeclaration("msquests:rotate", (player, args) -> {
            rotate(player, argv(args));
            return kotlin.Unit.INSTANCE;
        }));
    }

    @Override
    public void onDisable() {
        this.api = null;
    }

    /**
     * No-op: this fires when Artisan reloads its own bundle, which owns no MSQuests state.
     * Quest configs reload through {@code /quest admin reload}, which calls
     * {@link #signalDataReload()} instead.
     */
    @Override
    public void onReload() {}

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
