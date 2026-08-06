package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base class for every Bukkit-side action.
 *
 * <p>Actions are fired from wherever the quest lifecycle happens to be: the server thread
 * for a block break, but a virtual thread for anything routed through
 * {@code AtomicLocalQuestExecutor} (objective completion, quest completion, distribution).
 * Since every action ends up calling the Bukkit API — which is unsafe off the server thread
 * on Paper and throws outright on Folia — the hop is done here, once, rather than being left
 * to each call site.
 *
 * <p>Subclasses therefore implement {@code perform} and can assume the main thread; the
 * {@code execute} overloads are final so the guarantee cannot be bypassed by accident.
 */
public abstract class BukkitQuestAction extends QuestAction {

    protected final BukkitQuestsPlugin plugin;

    public BukkitQuestAction(QuestActionDTO rewardDto, BukkitQuestsPlugin plugin) {
        super(rewardDto);
        this.plugin = plugin;
    }

    @Override
    public final void execute(Quest quest) {
        onMainThread(() -> perform(quest));
    }

    @Override
    public final void execute(QuestObjective objective) {
        onMainThread(() -> perform(objective));
    }

    @Override
    public final void execute(QuestActor actor) {
        onMainThread(() -> perform(actor));
    }

    @Override
    public final void execute(QuestActor actor, QuestGroupConfig groupConfig) {
        onMainThread(() -> perform(actor, groupConfig));
    }

    protected abstract void perform(Quest quest);

    protected void perform(QuestObjective objective) {
        perform(objective.getQuest());
    }

    protected void perform(QuestActor actor) {
        // default no-op; override for actor-level events like distribution
    }

    protected void perform(QuestActor actor, QuestGroupConfig groupConfig) {
        perform(actor);
    }

    /**
     * Runs on the server thread, inline when already there so an action fired from a
     * listener keeps its side effects within the same tick as the event that caused it.
     */
    private void onMainThread(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
            return;
        }
        plugin.runSync(task);
    }

    protected Set<Player> getOnlinePlayers(Quest quest) {
        return getOnlinePlayers(quest.getActor());
    }

    protected Set<Player> getOnlinePlayers(QuestActor actor) {
        return actor.getProfiles().stream()
                .map(profile -> Bukkit.getPlayer(profile.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

}
