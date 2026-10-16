package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestMapper;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.repository.QuestConfigLoad;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.core.repository.RotationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code /quest admin reload} re-reads the configs, then rebuilds every loaded actor from the
 * database. Neither step may leave a half state behind: no moment without groups, no quest
 * counted twice, no rotation counted twice, and a broken file must not wipe a working group.
 */
class QuestReloadTest {

    private static final Logger LOGGER = Logger.getLogger(QuestReloadTest.class.getName());

    private QuestConfigRegistry configRegistry;
    private QuestRegistry questRegistry;
    private QuestRepository questRepository;
    private RotationRepository rotationRepository;
    private QuestService questService;
    private TestActor actor;
    private UUID storedQuestId;

    @BeforeEach
    void setUp() {
        configRegistry = new QuestConfigRegistry();
        configRegistry.replaceQuestGroupConfigs(List.of(dailyGroup()));

        questRegistry = new QuestRegistry();
        questRepository = mock(QuestRepository.class);
        rotationRepository = mock(RotationRepository.class);
        when(rotationRepository.countInPeriod(any(), eq("daily"), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(2));

        storedQuestId = UUID.randomUUID();
        long created = System.currentTimeMillis();
        when(questRepository.getAllByActor(any())).thenAnswer(invocation -> CompletableFuture.completedFuture(Map.of(
                storedQuestId,
                new QuestDTO(storedQuestId, "mine_stone", "daily", UUID.randomUUID(),
                        QuestStatus.IN_PROGRESS, null, created, created, Map.of())
        )));

        questService = new QuestService(
                LOGGER, configRegistry, questRepository, rotationRepository,
                new QuestFactory(mock(QuestObjectiveFactory.class)), questRegistry, mock(QuestMapper.class)
        );
        actor = new TestActor();
    }

    @Test
    void reloadingAnActorDoesNotStackItsQuests() {
        questService.loadQuests(actor).join();
        questService.loadQuests(actor).join();
        questService.loadQuests(actor).join();

        ActorQuestGroup group = actor.getActorQuestGroup(configRegistry.getQuestGroupConfigs().get("daily"));
        assertEquals(1, actor.getQuests().size());
        assertEquals(1, group.getAllQuestsForKey("mine_stone").size());
        assertEquals(1, group.getInProgressCount());
    }

    @Test
    void reloadingAnActorDoesNotStackItsRotations() {
        questService.loadQuests(actor).join();
        questService.loadQuests(actor).join();

        ActorQuestGroup group = actor.getActorQuestGroup(configRegistry.getQuestGroupConfigs().get("daily"));
        assertEquals(2, group.getRotationsInPeriod());
    }

    /** After a config reload the actor must point at the new group object, not the old one. */
    @Test
    void reloadedQuestsUseTheNewGroupConfig() {
        questService.loadQuests(actor).join();

        QuestGroupConfig reloaded = dailyGroup();
        configRegistry.replaceQuestGroupConfigs(List.of(reloaded));
        questService.loadQuests(actor).join();

        Quest quest = actor.getQuests().get(storedQuestId);
        assertSame(reloaded, quest.getQuestGroup());
        assertSame(reloaded, actor.getActorQuestGroup(reloaded).getGroupConfig());
        assertSame(quest, questRegistry.getQuest(storedQuestId));
    }

    /** A quest started while the database read was running must not vanish from the actor. */
    @Test
    void aQuestStartedDuringTheReadIsKept() {
        QuestGroupConfig daily = configRegistry.getQuestGroupConfigs().get("daily");
        Quest startedMeanwhile = new Quest(UUID.randomUUID(), daily.getQuestConfigs().get("mine_stone"), actor,
                QuestStatus.IN_PROGRESS, null, Date.from(Instant.now().plusSeconds(1)), new Date());
        actor.addQuest(startedMeanwhile);

        questService.loadQuests(actor).join();

        assertTrue(actor.getQuests().containsKey(startedMeanwhile.getId()));
        assertTrue(actor.getQuests().containsKey(storedQuestId));
    }

    @Test
    void aRefusedReloadKeepsThePreviousGroups() {
        QuestGroupConfig before = configRegistry.getQuestGroupConfigs().get("daily");
        QuestConfigService service = configService(new QuestConfigLoad(Map.of(), List.of("daily.yml: bad indentation")));

        CompletionException error = assertThrows(CompletionException.class, () -> service.reloadQuestGroups().join());

        assertInstanceOf(QuestConfigService.QuestConfigReloadException.class, error.getCause());
        assertSame(before, configRegistry.getQuestGroupConfigs().get("daily"));
    }

    @Test
    void aStartupLoadSkipsOnlyTheBrokenFiles() {
        QuestGroupConfig weekly = new QuestGroupConfig.Builder("weekly", "Weekly", "", "player").build();
        QuestGroupMapper mapper = mock(QuestGroupMapper.class);
        QuestGroupConfigDTO weeklyDto = mock(QuestGroupConfigDTO.class);
        when(mapper.toEntity(weeklyDto)).thenReturn(weekly);
        QuestConfigRepository repository = () -> CompletableFuture.completedFuture(
                new QuestConfigLoad(Map.of("weekly", weeklyDto), List.of("daily.yml: bad indentation")));

        new QuestConfigService(LOGGER, configRegistry, repository, mapper).loadQuestGroups().join();

        assertEquals(Map.of("weekly", weekly), configRegistry.getQuestGroupConfigs());
    }

    // ----------------------------------------------------------------- helpers

    private QuestConfigService configService(QuestConfigLoad load) {
        QuestConfigRepository repository = () -> CompletableFuture.completedFuture(load);
        return new QuestConfigService(LOGGER, configRegistry, repository, mock(QuestGroupMapper.class));
    }

    private static QuestGroupConfig dailyGroup() {
        QuestGroupConfig group = new QuestGroupConfig.Builder("daily", "Daily", "", "player")
                .maxActive(6)
                .rotatable(true)
                .build();
        QuestConfig quest = new QuestConfig("mine_stone", "Mine", "", 0L);
        quest.setGroup(group);
        group.addQuest(quest);
        return group;
    }

    private static final class TestActor extends QuestActor {
        TestActor() {
            super(UUID.randomUUID(), "Steve");
        }

        @Override
        public String getActorType() {
            return "player";
        }

        @Override
        public boolean isMember(UUID playerId) {
            return id.equals(playerId);
        }
    }
}
