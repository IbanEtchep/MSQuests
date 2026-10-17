package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.factory.ConditionFactory;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.mapper.QuestMapper;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.core.repository.RotationRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Rows of artisan_quests_quest name their quest by uid: renaming the group Document or the quest
 * key in the editor must not lose the progress, and the stored keys must not be used.
 */
class QuestServiceUidResolutionTest {

    @Test
    void storedQuestFollowsItsUidAcrossRenames() {
        QuestGroupConfig group = new QuestGroupConfig.Builder("daily_renamed", "Daily", "", "player").uid("G1").build();
        QuestConfig quest = new QuestConfig("mine_renamed", "Mine", "", 0);
        quest.setUid("Q1");
        group.addQuest(quest);
        // A legacy-looking group still registered under the old key must not capture the row.
        QuestGroupConfig stale = new QuestGroupConfig.Builder("daily", "Old", "", "player").build();
        stale.addQuest(new QuestConfig("mine", "Old mine", "", 0));
        QuestConfigRegistry configs = new QuestConfigRegistry();
        configs.replaceQuestGroupConfigs(java.util.List.of(group, stale));

        UUID stored = UUID.randomUUID();
        UUID orphan = UUID.randomUUID();
        QuestRepository repository = mock(QuestRepository.class);
        when(repository.getAllByActor(any())).thenReturn(CompletableFuture.completedFuture(Map.of(
                stored, new QuestDTO(stored, "mine", "daily", UUID.randomUUID(), QuestStatus.IN_PROGRESS,
                        null, 1L, 1L, Map.of(), "Q1"),
                orphan, new QuestDTO(orphan, "mine", "daily", UUID.randomUUID(), QuestStatus.IN_PROGRESS,
                        null, null, null, Map.of(), "GONE"))));

        QuestService service = new QuestService(Logger.getAnonymousLogger(), configs, repository,
                mock(RotationRepository.class), new QuestFactory(new QuestObjectiveFactory(new ConditionFactory())),
                new QuestRegistry(), new QuestMapper());
        QuestActor actor = new QuestActor(UUID.randomUUID(), "Steve") {
            @Override
            public String getActorType() {
                return "player";
            }

            @Override
            public boolean isMember(UUID playerId) {
                return id.equals(playerId);
            }
        };

        service.loadQuests(actor).join();

        assertEquals(1, actor.getQuests().size(), "the unknown uid is ignored, not matched by its keys");
        Quest loaded = actor.getQuests().get(stored);
        assertEquals(quest, loaded.getQuestConfig());
        assertEquals("Q1", new QuestMapper().toDTO(loaded).questUid());
        assertTrue(configs.getQuestByUid("GONE") == null);
    }
}
