package com.github.ibanetchep.msquests.bukkit.suite;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.artisan.EditorProject;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.config.action.ActionChain;
import com.github.ibanetchep.msquests.core.quest.config.action.ChainContext;
import com.github.ibanetchep.msquests.core.quest.config.action.ChainRunner;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runs the quests' chains (rewards, group hooks) with the suite's engine ({@link ChainExecutor}:
 * Artisan's {@code api.getActions()} or the local runtime), once for each online member of the
 * actor, in the scope of the project holding the group ({@code {i18n:key}} reads its {@code lang/}).
 *
 * <p>The lifecycle calls it from whatever thread it runs on (a virtual thread for completions);
 * members and bindings are read on the server thread, as the MSQuests actions did, and the engine
 * then runs each chain on the player's thread. Besides the {@link ChainContext} bindings, each
 * run binds {@code tracked}: is the quest the one this member tracks.
 */
public final class MemberChainRunner implements ChainRunner {

    private final BukkitQuestsPlugin plugin;
    private final ChainExecutor executor;

    public MemberChainRunner(BukkitQuestsPlugin plugin, ChainExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
    }

    @Override
    public void run(ActionChain chain, ChainContext context) {
        if (chain.isEmpty()) return;
        if (Bukkit.isPrimaryThread()) {
            runForMembers(chain, context);
        } else {
            plugin.runSync(() -> runForMembers(chain, context));
        }
    }

    private void runForMembers(ActionChain chain, ChainContext context) {
        Map<String, Object> args = context.args(plugin.getTranslator());
        Quest quest = context.quest();
        String project = projectOf(context);
        for (PlayerProfile profile : context.actor().getProfiles()) {
            Player player = Bukkit.getPlayer(profile.getId());
            if (player == null) continue;
            Map<String, Object> playerArgs = new LinkedHashMap<>(args);
            playerArgs.put("tracked", quest != null && quest.getId().equals(profile.getTrackedQuestId()));
            executor.run(player, chain.steps(), playerArgs, project);
        }
    }

    /**
     * The project the chain runs in, so its menu references resolve: the one holding the group's
     * Document, else the module's own (a group read from {@code quests/*.yml}).
     */
    static String projectOf(ChainContext context) {
        String source = context.group() != null ? context.group().getSourceProject() : null;
        return source != null ? source : EditorProject.PROJECT_ID;
    }
}
