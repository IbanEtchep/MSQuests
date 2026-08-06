package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Progress ratios are consumed as boss-bar progress ({@code BossBar#progress} throws
 * outside 0..1) and as display percentages, so they must be normalised at the source.
 */
class QuestProgressRatioTest {

    @Test
    void objectiveRatioIsTheShareOfTheTargetReached() {
        Quest quest = quest(new int[][]{{32, 64}});

        assertEquals(0.5, quest.getObjectives().get(0).getProgressRatio(), 1e-9);
    }

    @Test
    void objectiveRatioNeverExceedsOneEvenIfProgressOvershootsTheTarget() {
        Quest quest = quest(new int[][]{{64, 64}});
        ((AbstractQuestObjective<?>) quest.getObjectives().get(0)).setProgress(200);

        assertEquals(1.0, quest.getObjectives().get(0).getProgressRatio());
    }

    /** A target of 0 used to divide by zero and yield NaN/Infinity. */
    @Test
    void objectiveRatioOfATargetlessObjectiveIsComplete() {
        Quest quest = quest(new int[][]{{0, 0}});

        assertEquals(1.0, quest.getObjectives().get(0).getProgressRatio());
    }

    @Test
    void questRatioIsExpressedInTheSameZeroOneScaleAsItsObjectives() {
        Quest quest = quest(new int[][]{{32, 64}});

        assertEquals(0.5, quest.getProgressRatio(), 1e-9);
    }

    @Test
    void questRatioAveragesItsStages() {
        Quest quest = quest(new int[][]{{64, 64}}, new int[][]{{0, 10}});

        assertEquals(0.5, quest.getProgressRatio(), 1e-9);
    }

    @Test
    void aFullyCompletedQuestReachesExactlyOne() {
        Quest quest = quest(new int[][]{{64, 64}}, new int[][]{{10, 10}});

        assertEquals(1.0, quest.getProgressRatio(), 1e-9);
    }

    @Test
    void aQuestWithoutStagesIsAtZeroRatherThanNaN() {
        Quest quest = quest();

        assertEquals(0.0, quest.getProgressRatio());
    }

    // ----------------------------------------------------------------- helpers

    /** One stage per varargs entry, each entry listing its objectives as {progress, target}. */
    private Quest quest(int[][]... stages) {
        QuestGroupConfig group = new QuestGroupConfig.Builder("g", "Group", "desc", "PLAYER").build();
        QuestConfig config = new QuestConfig("q", "Quest", "desc", 0L);
        config.setGroup(group);
        Date now = new Date();
        Quest quest = new Quest(UUID.randomUUID(), config, new TestActor(), QuestStatus.IN_PROGRESS, null, now, now);

        for (int s = 0; s < stages.length; s++) {
            QuestStageConfig stageConfig = new QuestStageConfig("stage_" + s, "Stage " + s, Flow.PARALLEL);
            config.addStage(stageConfig);
            QuestStage stage = new QuestStage(quest, stageConfig);
            int[][] objectives = stages[s];
            for (int o = 0; o < objectives.length; o++) {
                stage.addObjective(new TestObjective(stage, "obj_" + o, objectives[o][0], objectives[o][1]));
            }
            quest.addStage(stage);
        }
        return quest;
    }

    private static final class TestObjectiveConfig extends QuestObjectiveConfig {
        private final int target;

        TestObjectiveConfig(String key, int target) {
            super(new QuestObjectiveConfigDTO(key, "test", Map.of()));
            this.target = target;
        }

        @Override
        public int getTarget() {
            return target;
        }

        @Override
        public QuestObjectiveConfigDTO toDTO() {
            return new QuestObjectiveConfigDTO(key, type, Map.of());
        }

        @Override
        public Map<String, String> getPlaceholders(Translator translator) {
            return Map.of();
        }
    }

    private static final class TestObjective extends AbstractQuestObjective<TestObjectiveConfig> {
        TestObjective(QuestStage stage, String key, int progress, int target) {
            super(stage, new TestObjectiveConfig(key, target), progress,
                    progress >= target ? QuestObjectiveStatus.COMPLETED : QuestObjectiveStatus.IN_PROGRESS);
        }
    }

    private static final class TestActor extends QuestActor {
        TestActor() {
            super(UUID.randomUUID(), "TestActor");
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
