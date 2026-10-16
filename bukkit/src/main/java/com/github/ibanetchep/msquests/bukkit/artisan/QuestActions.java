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
import net.artisanmc.modules.api.action.ActionContext;
import net.artisanmc.modules.api.action.ActionDeclaration;
import net.artisanmc.modules.api.action.ActionHandler;
import net.artisanmc.modules.api.action.ActionRuntime;
import net.artisanmc.modules.api.model.ModelField;
import net.artisanmc.modules.api.model.ModelFieldType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The {@code quests:*} actions Artisan chains may call (menus, commands, dialogs). Each one names
 * a group by a {@code document_ref} to a {@code quest_group} Document, so the editor offers a
 * picker, draws an edge and follows a rename, and a quest by its key inside that group.
 *
 * <p>The group value is the group key: a {@code quest_group} Document is named after the group it
 * holds, and a group still read from {@code quests/*.yml} has the same key. A qualified reference
 * from another project ({@code quests:daily}) resolves to its last segment.
 *
 * <p>Handlers run on the clicking player's thread and act on the actor of the group's actor type
 * the player belongs to (the player, a global actor, a guild...). Bad data is reported with
 * {@link ActionRuntime#warn} and the chain goes on.
 */
final class QuestActions {

    static final String START = "quests:start";
    static final String TRACK = "quests:track";
    static final String ROTATE = "quests:rotate";
    static final String COMPLETE = "quests:complete";
    static final String DISTRIBUTE = "quests:distribute";

    static final String GROUP_KIND = "quest_group";

    private final BukkitQuestsPlugin plugin;
    private final ActorResolver actors;

    QuestActions(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
        this.actors = new ActorResolver(plugin.getQuestActorRegistry());
    }

    List<ActionDeclaration> declarations() {
        return List.of(
                new ActionDeclaration(START, questArgs(), null, this::start),
                new ActionDeclaration(TRACK, questArgs(), null, this::track),
                new ActionDeclaration(ROTATE, questArgs(), null, this::rotate),
                new ActionDeclaration(COMPLETE, questArgs(), null, this::complete),
                new ActionDeclaration(DISTRIBUTE, List.of(
                        groupArg(),
                        new ModelField("amount", ModelFieldType.integer())
                                .withRange(1.0, null)
                                .withHelp("Quests to hand out. Empty: the group's distribution settings.")
                ), "group", this::distribute));
    }

    private static List<ModelField> questArgs() {
        return List.of(
                groupArg(),
                new ModelField("quest", ModelFieldType.string(), true)
                        .withHelp("Quest key inside the group, e.g. mine_stone_64."));
    }

    private static ModelField groupArg() {
        return new ModelField("group", ModelFieldType.documentRef(GROUP_KIND), true);
    }

    // ---------------------------------------------------------------- handlers

    /** Starts the quest for the player's actor, under the group's rules (limits, conditions). */
    private ActionContext start(ActionContext ctx, Map<String, Object> args, ActionRuntime runtime) {
        Target target = target(START, ctx, args, runtime, true);
        if (target == null || target.config == null) return ctx;

        Player player = ctx.getPlayer();
        PlayerProfile profile = plugin.getPlayerProfileRegistry().getPlayerProfile(player.getUniqueId());
        QuestStartResult result = plugin.getQuestLifecycleService().startQuest(
                target.actor, target.config, target.group.getDistributionStrategy(), profile);
        player.sendMessage(MessageBuilder.translatable(result).toComponent());
        return ctx;
    }

    /** Toggles tracking of the actor's active instance of the quest (same as {@code msquests:track}). */
    private ActionContext track(ActionContext ctx, Map<String, Object> args, ActionRuntime runtime) {
        Quest quest = activeQuest(TRACK, ctx, args, runtime);
        if (quest == null) return ctx;

        Player player = ctx.getPlayer();
        PlayerProfile profile = plugin.getPlayerProfileRegistry().getPlayerProfile(player.getUniqueId());
        if (profile == null) return ctx;

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
        return ctx;
    }

    /** Swaps the actor's active instance of the quest for another one of the group. */
    private ActionContext rotate(ActionContext ctx, Map<String, Object> args, ActionRuntime runtime) {
        Quest quest = activeQuest(ROTATE, ctx, args, runtime);
        if (quest == null || quest.getActor() == null) return ctx;

        QuestRotateResult result = plugin.getQuestLifecycleService().rotateQuest(quest.getActor(), quest);
        ctx.getPlayer().sendMessage(MessageBuilder.translatable(result).toComponent());
        return ctx;
    }

    /** Completes the actor's active instance of the quest, its rewards included. */
    private ActionContext complete(ActionContext ctx, Map<String, Object> args, ActionRuntime runtime) {
        Quest quest = activeQuest(COMPLETE, ctx, args, runtime);
        if (quest == null) return ctx;

        plugin.getQuestLifecycleService().completeQuest(quest);
        return ctx;
    }

    /**
     * Hands quests of the group to the actor: {@code amount} with the group's strategy, or the
     * group's own distribution settings (and its distribution actions) when no amount is given.
     */
    private ActionContext distribute(ActionContext ctx, Map<String, Object> args, ActionRuntime runtime) {
        Target target = target(DISTRIBUTE, ctx, args, runtime, false);
        if (target == null) return ctx;

        Integer amount = integer(args.get("amount"));
        if (amount == null) {
            plugin.getQuestLifecycleService().triggerDistribution(target.actor, target.group);
        } else if (amount > 0) {
            plugin.getQuestLifecycleService().distributeQuests(
                    target.actor, target.group, target.group.getDistributionStrategy(), amount);
        }
        return ctx;
    }

    // ---------------------------------------------------------------- resolution

    private record Target(QuestGroupConfig group, QuestActor actor, @Nullable QuestConfig config) {
    }

    /**
     * The group, the player's actor for it and, when {@code withQuest}, the quest config; null
     * (after a warning when the data is wrong) when any of them is missing.
     */
    private @Nullable Target target(String action, ActionContext ctx, Map<String, Object> args, ActionRuntime runtime, boolean withQuest) {
        String groupKey = groupKey(runtime.text(ctx, args.get("group")));
        if (groupKey == null) {
            runtime.warn(action + ": no group given");
            return null;
        }
        QuestGroupConfig group = plugin.getQuestConfigRegistry().getQuestGroupConfigs().get(groupKey);
        if (group == null) {
            runtime.warn(action + ": unknown quest group '" + groupKey + "'");
            return null;
        }

        QuestActor actor = actors.resolve(ctx.getPlayer().getUniqueId(), group.getActorType());
        if (actor == null) {
            // Not loaded yet, or the player belongs to no actor of this type (no guild): nothing to do.
            return null;
        }

        if (!withQuest) return new Target(group, actor, null);

        String questKey = blankToNull(runtime.text(ctx, args.get("quest")));
        QuestConfig config = questKey == null ? null : group.getQuestConfigs().get(questKey);
        if (config == null) {
            runtime.warn(action + ": unknown quest '" + questKey + "' in group '" + groupKey + "'");
            return null;
        }
        return new Target(group, actor, config);
    }

    /** The actor's in-progress instance of the quest, or null. */
    private @Nullable Quest activeQuest(String action, ActionContext ctx, Map<String, Object> args, ActionRuntime runtime) {
        Target target = target(action, ctx, args, runtime, true);
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
