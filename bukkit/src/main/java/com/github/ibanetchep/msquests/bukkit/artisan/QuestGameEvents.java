package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.core.event.CoreEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestAbandonedEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestCompletedEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestExpiredEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestObjectiveCompletedEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestStageCompletedEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestStartedEvent;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import net.artisanmc.modules.api.gameevent.GameEventDeclaration;
import net.artisanmc.modules.api.gameevent.GameEventFrequency;
import net.artisanmc.modules.api.gameevent.GameEventRegistry;
import net.artisanmc.modules.api.model.ModelField;
import net.artisanmc.modules.api.model.ModelFieldType;
import net.artisanmc.modules.api.scheduler.TaskScheduler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The quests' Game events (Artisan ADR {@code triggers-and-variables} §2): a Trigger, a
 * conversation or another module reacts to a quest starting, an objective or a stage completing,
 * a quest completing, expiring or being abandoned, without a hook in the quest group.
 *
 * <p>A quest belongs to an actor (a player, the server, a guild): the event fires once for each
 * online member, on the thread that owns that player. The context names the quest by the keys an
 * author writes ({@code group}, {@code quest}, {@code stage}, {@code objective}), never by its uid.
 * Nothing is computed or scheduled while no listener observes the event.
 */
public final class QuestGameEvents implements Consumer<CoreEvent> {

    public static final String STARTED = "quests:started";
    public static final String OBJECTIVE_COMPLETED = "quests:objective_completed";
    public static final String STAGE_COMPLETED = "quests:stage_completed";
    public static final String COMPLETED = "quests:completed";
    public static final String EXPIRED = "quests:expired";
    public static final String ABANDONED = "quests:abandoned";

    /** The Artisan API that brings Game events. */
    public static final String MIN_API_VERSION = "2.16.0";

    private static final String GROUP = "quests";

    private final GameEventRegistry registry;
    private final TaskScheduler scheduler;
    private final Function<UUID, Player> players;
    private final BooleanSupplier onServerThread;
    private final Consumer<Runnable> toServerThread;

    /**
     * @param players the online player for a profile id, or null; read on the server thread
     * @param onServerThread whether the calling thread may read the online players
     * @param toServerThread runs a task there (the next tick)
     */
    public QuestGameEvents(
            GameEventRegistry registry,
            TaskScheduler scheduler,
            Function<UUID, Player> players,
            BooleanSupplier onServerThread,
            Consumer<Runnable> toServerThread
    ) {
        this.registry = registry;
        this.scheduler = scheduler;
        this.players = players;
        this.onServerThread = onServerThread;
        this.toServerThread = toServerThread;
    }

    /** The declarations, registered from the module's {@code onEnable} so they leave with it. */
    public static List<GameEventDeclaration> declarations() {
        List<ModelField> quest = List.of(
                new ModelField("group", ModelFieldType.string()),
                new ModelField("quest", ModelFieldType.string()),
                new ModelField("actor_type", ModelFieldType.string()));
        List<ModelField> stage = with(quest, new ModelField("stage", ModelFieldType.string()));
        List<ModelField> objective = with(stage,
                new ModelField("objective", ModelFieldType.string()),
                new ModelField("objective_type", ModelFieldType.string()));
        return List.of(
                new GameEventDeclaration(STARTED, GROUP, GameEventFrequency.RARE, quest),
                new GameEventDeclaration(OBJECTIVE_COMPLETED, GROUP, GameEventFrequency.NORMAL, objective),
                new GameEventDeclaration(STAGE_COMPLETED, GROUP, GameEventFrequency.RARE, stage),
                new GameEventDeclaration(COMPLETED, GROUP, GameEventFrequency.RARE, quest),
                new GameEventDeclaration(EXPIRED, GROUP, GameEventFrequency.RARE, quest),
                new GameEventDeclaration(ABANDONED, GROUP, GameEventFrequency.RARE, quest));
    }

    private static List<ModelField> with(List<ModelField> base, ModelField... more) {
        List<ModelField> fields = new ArrayList<>(base);
        fields.addAll(List.of(more));
        return List.copyOf(fields);
    }

    @Override
    public void accept(CoreEvent event) {
        Fired fired = toFired(event);
        if (fired == null || !registry.isObserved(fired.name())) return;
        if (onServerThread.getAsBoolean()) {
            deliver(fired);
        } else {
            toServerThread.accept(() -> deliver(fired));
        }
    }

    /**
     * The event to fire, its context read now: the quest may change (rotation, reload) before
     * the members' threads run.
     */
    static @Nullable Fired toFired(CoreEvent event) {
        if (event instanceof CoreQuestStartedEvent e) return quest(STARTED, e.getQuest());
        if (event instanceof CoreQuestCompletedEvent e) return quest(COMPLETED, e.getQuest());
        if (event instanceof CoreQuestExpiredEvent e) return quest(EXPIRED, e.getQuest());
        if (event instanceof CoreQuestAbandonedEvent e) return quest(ABANDONED, e.getQuest());
        if (event instanceof CoreQuestStageCompletedEvent e) return stage(STAGE_COMPLETED, e.getStage());
        if (event instanceof CoreQuestObjectiveCompletedEvent e) {
            QuestObjective objective = e.getObjective();
            Fired fired = stage(OBJECTIVE_COMPLETED, objective.getStage());
            fired.context().put("objective", objective.getObjectiveConfig().getKey());
            fired.context().put("objective_type", objective.getType());
            return fired;
        }
        return null;
    }

    private static Fired quest(String name, Quest quest) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("group", quest.getQuestGroup().getKey());
        context.put("quest", quest.getQuestConfig().getKey());
        context.put("actor_type", quest.getActor().getActorType());
        return new Fired(name, quest.getActor(), context);
    }

    private static Fired stage(String name, QuestStage stage) {
        Fired fired = quest(name, stage.getQuest());
        fired.context().put("stage", stage.getKey());
        return fired;
    }

    private void deliver(Fired fired) {
        Map<String, Object> context = Map.copyOf(fired.context());
        for (PlayerProfile profile : fired.actor().getProfiles()) {
            Player player = players.apply(profile.getId());
            if (player == null) continue;
            if (scheduler.isOwnedByCurrentThread(player)) {
                registry.fire(fired.name(), player, context);
            } else {
                scheduler.runFor(player, () -> {
                    if (player.isOnline()) registry.fire(fired.name(), player, context);
                });
            }
        }
    }

    record Fired(String name, QuestActor actor, Map<String, Object> context) {}
}
