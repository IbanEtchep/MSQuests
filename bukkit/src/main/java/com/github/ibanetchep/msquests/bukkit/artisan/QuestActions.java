package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.quest.result.QuestRotateResult;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import com.github.ibanetchep.msquests.core.registry.ActorResolver;
import com.github.ibanetchep.msquests.suite.ActionArg;
import com.github.ibanetchep.msquests.suite.ActionCall;
import com.github.ibanetchep.msquests.suite.ActionSpec;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The {@code quests:*} actions the quests' chains may call (and, with Artisan, its menus, commands
 * and dialogs). Declared in MSQuests' own words ({@link ActionSpec}): {@code ArtisanIntegration}
 * registers them in Artisan's registry, {@code LocalSuiteRuntime} in the local engine's. Each one names
 * a group by a {@code document_ref} to a {@code quest_group} Document, so the editor offers a
 * picker, draws an edge and follows a rename, and a quest by its key inside that group.
 *
 * <p>The group value is the group key: a {@code quest_group} Document is named after the group it
 * holds, and a group still read from {@code quests/*.yml} has the same key. A qualified reference
 * from another project ({@code quests:daily}) resolves to its last segment.
 *
 * <p>Handlers run on the clicking player's thread and act on the actor of the group's actor type
 * the player belongs to (the player, a global actor, a guild...). Bad data is reported with
 * {@link ActionCall#warn} and the chain goes on.
 *
 * <p>Two more actions have no core equivalent and are the display side of the quests' own chains:
 * {@code quests:action_bar} and {@code quests:boss_bar} (its progress is the chain's
 * {@code progress_ratio} binding: the objective's in an objective hook, else the quest's).
 */
public final class QuestActions {

    static final String START = "quests:start";
    static final String ABANDON = "quests:abandon";
    static final String TRACK = "quests:track";
    static final String ROTATE = "quests:rotate";
    static final String COMPLETE = "quests:complete";
    static final String DISTRIBUTE = "quests:distribute";
    static final String ACTION_BAR = "quests:action_bar";
    static final String BOSS_BAR = "quests:boss_bar";

    public static final String GROUP_KIND = "quest_group";

    private final BukkitQuestsPlugin plugin;
    private final ActorResolver actors;
    private final QuestBossBars bossBars;

    public QuestActions(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
        this.actors = new ActorResolver(plugin.getQuestActorRegistry());
        this.bossBars = new QuestBossBars(plugin);
    }

    public List<ActionSpec> declarations() {
        return List.of(
                new ActionSpec(START, questArgs(), null, this::start),
                new ActionSpec(ABANDON, questArgs(), null, this::abandon),
                new ActionSpec(TRACK, questArgs(), null, this::track),
                new ActionSpec(ROTATE, questArgs(), null, this::rotate),
                new ActionSpec(COMPLETE, questArgs(), null, this::complete),
                new ActionSpec(DISTRIBUTE, List.of(
                        groupArg(),
                        ActionArg.integer("amount")
                                .withMin(1.0)
                                .withHelp("Quests to hand out. Empty: the group's distribution settings.")
                ), "group", this::distribute),
                new ActionSpec(ACTION_BAR, List.of(
                        ActionArg.ltext("text").required()
                ), "text", this::actionBar),
                new ActionSpec(BOSS_BAR, List.of(
                        ActionArg.ltext("text").required(),
                        ActionArg.enumOf("color", names(BossBar.Color.values()))
                                .withDefault("white"),
                        ActionArg.enumOf("style", names(BossBar.Overlay.values()))
                                .withDefault("progress"),
                        ActionArg.bool("show_progress")
                                .withDefault(true)
                                .withHelp("Fill the bar with the progress of the objective (objective hooks) or of the quest."),
                        ActionArg.integer("duration")
                                .withMin(1.0)
                                .withDefault(5)
                                .withHelp("Seconds the bar stays after the last update.")
                ), "text", this::bossBar));
    }

    private static List<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(value -> value.name().toLowerCase(Locale.ROOT)).toList();
    }

    private static List<ActionArg> questArgs() {
        return List.of(
                groupArg(),
                ActionArg.string("quest").required()
                        .withHelp("Quest key inside the group, e.g. mine_stone_64."));
    }

    private static ActionArg groupArg() {
        return ActionArg.documentRef("group", GROUP_KIND).required();
    }

    // ---------------------------------------------------------------- handlers

    /** Starts the quest for the player's actor, under the group's rules (limits, conditions). */
    private void start(ActionCall call) {
        Target target = target(START, call, true);
        if (target == null || target.config == null) return;

        Player player = call.getPlayer();
        PlayerProfile profile = plugin.getPlayerProfileRegistry().getPlayerProfile(player.getUniqueId());
        QuestStartResult result = plugin.getQuestLifecycleService().startQuest(
                target.actor, target.config, target.group.getDistributionStrategy(), profile);
        player.sendMessage(MessageBuilder.translatable(result).toComponent());
    }

    /**
     * Abandons the actor's active instance of the quest: it ends failed and stays counted in its
     * period ({@code QuestLifecycleService#abandonQuest}); a tracked quest is untracked.
     */
    private void abandon(ActionCall call) {
        Quest quest = activeQuest(ABANDON, call);
        if (quest == null) return;

        Player player = call.getPlayer();
        PlayerProfile profile = plugin.getPlayerProfileRegistry().getPlayerProfile(player.getUniqueId());
        if (profile != null && quest.getId().equals(profile.getTrackedQuestId())) {
            profile.setTrackedQuestId(null);
            plugin.getPlayerProfileService().saveProfile(profile);
            if (plugin.getTrackingBossBarService().isEnabled()) {
                plugin.getTrackingBossBarService().hideBossBar(player);
            }
        }

        plugin.getQuestLifecycleService().abandonQuest(quest);
        player.sendMessage(MessageBuilder.translatable(TranslationKey.QUEST_COMMAND_ABANDONED)
                .applyPlaceholderResolver(quest).toComponent());
    }

    /** Toggles tracking of the actor's active instance of the quest (same as {@code msquests:track}). */
    private void track(ActionCall call) {
        Quest quest = activeQuest(TRACK, call);
        if (quest == null) return;

        Player player = call.getPlayer();
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

    /** Swaps the actor's active instance of the quest for another one of the group. */
    private void rotate(ActionCall call) {
        Quest quest = activeQuest(ROTATE, call);
        if (quest == null || quest.getActor() == null) return;

        QuestRotateResult result = plugin.getQuestLifecycleService().rotateQuest(quest.getActor(), quest);
        call.getPlayer().sendMessage(MessageBuilder.translatable(result).toComponent());
    }

    /** Completes the actor's active instance of the quest, its rewards included. */
    private void complete(ActionCall call) {
        Quest quest = activeQuest(COMPLETE, call);
        if (quest == null) return;

        plugin.getQuestLifecycleService().completeQuest(quest);
    }

    /**
     * Hands quests of the group to the actor: {@code amount} with the group's strategy, or the
     * group's own distribution settings (and its distribution actions) when no amount is given.
     */
    private void distribute(ActionCall call) {
        Target target = target(DISTRIBUTE, call, false);
        if (target == null) return;

        Integer amount = integer(call.getArgs().get("amount"));
        if (amount == null) {
            plugin.getQuestLifecycleService().triggerDistribution(target.actor, target.group);
        } else if (amount > 0) {
            plugin.getQuestLifecycleService().distributeQuests(
                    target.actor, target.group, target.group.getDistributionStrategy(), amount);
        }
    }

    private void actionBar(ActionCall call) {
        call.getPlayer().sendActionBar(text(call));
    }

    private void bossBar(ActionCall call) {
        Map<String, Object> args = call.getArgs();
        BossBar.Color color = enumValue(BossBar.Color.class, args.get("color"), BossBar.Color.WHITE, call);
        BossBar.Overlay style = enumValue(BossBar.Overlay.class, args.get("style"), BossBar.Overlay.PROGRESS, call);
        boolean showProgress = !"false".equalsIgnoreCase(String.valueOf(args.get("show_progress")).trim());
        Integer seconds = integer(args.get("duration"));

        float progress = 1.0f;
        if (showProgress && call.getContextArgs().get("progress_ratio") instanceof Number ratio) {
            progress = (float) Math.clamp(ratio.doubleValue(), 0.0, 1.0);
        }
        bossBars.show(call.getPlayer(), text(call), progress, color, style, seconds != null ? seconds : 5);
    }

    private static Component text(ActionCall call) {
        return MiniMessage.miniMessage().deserialize(call.text(call.getArgs().get("text")));
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, @Nullable Object value, E fallback, ActionCall call) {
        if (value == null) return fallback;
        try {
            return Enum.valueOf(type, value.toString().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            call.warn(BOSS_BAR + ": unknown " + type.getSimpleName().toLowerCase(Locale.ROOT) + " '" + value + "'");
            return fallback;
        }
    }

    // ---------------------------------------------------------------- resolution

    private record Target(QuestGroupConfig group, QuestActor actor, @Nullable QuestConfig config) {
    }

    /**
     * The group, the player's actor for it and, when {@code withQuest}, the quest config; null
     * (after a warning when the data is wrong) when any of them is missing.
     */
    private @Nullable Target target(String action, ActionCall call, boolean withQuest) {
        Map<String, Object> args = call.getArgs();
        String groupKey = groupKey(call.text(args.get("group")));
        if (groupKey == null) {
            call.warn(action + ": no group given");
            return null;
        }
        QuestGroupConfig group = plugin.getQuestConfigRegistry().getQuestGroupConfigs().get(groupKey);
        if (group == null) {
            call.warn(action + ": unknown quest group '" + groupKey + "'");
            return null;
        }

        QuestActor actor = actors.resolve(call.getPlayer().getUniqueId(), group.getActorType());
        if (actor == null) {
            // Not loaded yet, or the player belongs to no actor of this type (no guild): nothing to do.
            return null;
        }

        if (!withQuest) return new Target(group, actor, null);

        String questKey = blankToNull(call.text(args.get("quest")));
        QuestConfig config = questKey == null ? null : group.getQuestConfigs().get(questKey);
        if (config == null) {
            call.warn(action + ": unknown quest '" + questKey + "' in group '" + groupKey + "'");
            return null;
        }
        return new Target(group, actor, config);
    }

    /** The actor's in-progress instance of the quest, or null. */
    private @Nullable Quest activeQuest(String action, ActionCall call) {
        Target target = target(action, call, true);
        if (target == null) return null;
        String groupKey = target.group.getKey();
        String questKey = target.config.getKey();
        return target.actor.getQuests().values().stream()
                .filter(Quest::isActive)
                .filter(quest -> quest.getQuestGroup() != null && groupKey.equals(quest.getQuestGroup().getKey()))
                .filter(quest -> questKey.equals(quest.getQuestConfig().getKey()))
                .findFirst()
                .orElse(null);
    }

    /** {@code daily} or a qualified {@code project:daily} → {@code daily}. */
    static @Nullable String groupKey(@Nullable String ref) {
        String value = blankToNull(ref);
        if (value == null) return null;
        int separator = value.lastIndexOf(':');
        return blankToNull(separator >= 0 ? value.substring(separator + 1) : value);
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static @Nullable Integer integer(@Nullable Object value) {
        if (value instanceof Number number) return number.intValue();
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
