package com.github.ibanetchep.msquests.bukkit.quest.objective;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.actor.BukkitQuestPlayerActor;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import com.github.ibanetchep.msquests.core.service.QuestProgressService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractObjectiveHandlerTest {

    protected ServerMock server;
    protected PlayerMock player;
    protected PlayerProfile profile;
    protected BukkitQuestsPlugin plugin;
    protected PlayerProfileRegistry playerProfileRegistry;
    protected QuestProgressService questProgressService;

    @BeforeEach
    void setUpBase() {
        server = MockBukkit.mock();
        player = server.addPlayer();

        plugin = mock(BukkitQuestsPlugin.class);
        playerProfileRegistry = new PlayerProfileRegistry();
        questProgressService = mock(QuestProgressService.class);

        profile = new PlayerProfile(player.getUniqueId(), player.getName());
        playerProfileRegistry.registerPlayerProfile(profile);

        when(plugin.getServer()).thenReturn(server);
        when(plugin.getPlayerProfileRegistry()).thenReturn(playerProfileRegistry);
        when(plugin.getQuestProgressService()).thenReturn(questProgressService);
    }

    @AfterEach
    void tearDownBase() {
        MockBukkit.unmock();
    }

    /**
     * Creates an objective attached to an active quest/stage for the test player.
     * The factory receives the already-created QuestStage so it can instantiate
     * the correct objective subtype with it.
     * <p>
     * On the first call a new actor/quest/stage chain is built; subsequent calls
     * reuse the same stage so all objectives belong to the same active quest.
     */
    protected <O extends AbstractQuestObjective<?>> O createObjective(Function<QuestStage, O> factory) {
        QuestStage stage;

        if (profile.getActors().isEmpty()) {
            QuestGroupConfig groupConfig = new QuestGroupConfig.Builder("group", "Group", "desc", "player").build();
            QuestConfig questConfig = new QuestConfig("quest", "Quest", "desc", 3600);
            groupConfig.addQuest(questConfig);

            BukkitQuestPlayerActor actor = new BukkitQuestPlayerActor(player.getUniqueId(), player.getName());
            profile.addActor(actor);

            Quest quest = new Quest(UUID.randomUUID(), questConfig, actor, QuestStatus.IN_PROGRESS, null, new Date(), new Date());
            actor.addQuest(quest);

            QuestStageConfig stageConfig = new QuestStageConfig("stage", "Stage", Flow.PARALLEL);
            stage = new QuestStage(quest, stageConfig);
            quest.addStage(stage);
        } else {
            Quest quest = profile.getActors().values().iterator().next().getQuests().values().iterator().next();
            stage = quest.getStagesList().get(0);
        }

        O objective = factory.apply(stage);
        stage.addObjective(objective);
        return objective;
    }
}
