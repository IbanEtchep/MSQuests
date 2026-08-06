package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/** Core-only doubles (no Bukkit) shared by the Artisan integration tests. */
final class ArtisanFixtures {

    private ArtisanFixtures() {}

    /** Identity translator: a key translates to itself, so assertions stay readable. */
    static final Translator TRANSLATOR = new Translator() {
        @Override
        public String getRaw(String key) {
            return key;
        }

        @Override
        public String getRaw(Translatable translatable) {
            return translatable.getTranslationKey();
        }
    };

    static final class TestObjectiveConfig extends QuestObjectiveConfig {
        private final int target;

        TestObjectiveConfig(String key, String type, int target) {
            super(new QuestObjectiveConfigDTO(key, type, Map.of()));
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

    static final class TestObjective extends AbstractQuestObjective<TestObjectiveConfig> {
        TestObjective(QuestStage stage, TestObjectiveConfig config, int progress) {
            super(stage, config, progress,
                    progress >= config.getTarget() ? QuestObjectiveStatus.COMPLETED : QuestObjectiveStatus.IN_PROGRESS);
        }
    }

    static final class TestActor extends QuestActor {
        private final String actorType;

        TestActor(UUID id, String name, String actorType) {
            super(id, name);
            this.actorType = actorType;
        }

        @Override
        public String getActorType() {
            return actorType;
        }

        @Override
        public boolean isMember(UUID playerId) {
            return switch (actorType) {
                case "global" -> true;
                // Stands in for the future guild actor: membership is not identity.
                case "guild" -> members.contains(playerId);
                default -> id.equals(playerId);
            };
        }

        final java.util.Set<UUID> members = new java.util.HashSet<>();
    }

    static final class TestAction extends QuestAction {
        TestAction(String type, String name) {
            super(new QuestActionDTO(type, name, Map.of(), null));
        }

        @Override
        public void execute(Quest quest) {}

        @Override
        public QuestActionDTO toDTO() {
            return new QuestActionDTO(getType(), getName(), Map.of(), null);
        }
    }

    /** A group with no period bounds — `hasStartedInCurrentPeriod` then only ignores EXPIRED. */
    static QuestGroupConfig group(String key, String actorType) {
        return new QuestGroupConfig.Builder(key, "Group " + key, "desc " + key, actorType)
                .maxActive(6)
                .maxPerPeriod(6)
                .rotatable(true)
                .tiers(Map.of("easy", new com.github.ibanetchep.msquests.core.quest.config.QuestTierConfig("easy", "Facile")))
                .build();
    }

    static final int DEFAULT_TARGET = 64;

    static QuestConfig quest(QuestGroupConfig group, String key, String tier) {
        return quest(group, key, tier, DEFAULT_TARGET);
    }

    /** One stage, one objective — the target lives on the config, as the runtime reads it. */
    static QuestConfig quest(QuestGroupConfig group, String key, String tier, int target) {
        QuestConfig config = new QuestConfig(key, "Quest " + key, "desc " + key, 86400L);
        config.setTier(tier);
        config.setGroup(group);
        QuestStageConfig stage = new QuestStageConfig("stage_1", "Stage 1", Flow.PARALLEL);
        stage.addObjective(new TestObjectiveConfig("obj_1", "block_break", target));
        config.addStage(stage);
        group.addQuest(config);
        return config;
    }

    /** Same as {@link #quest}, but with {@code stageCount} stages keyed {@code stage_0..stage_n}. */
    static QuestConfig multiStageQuest(QuestGroupConfig group, String key, int stageCount) {
        QuestConfig config = new QuestConfig(key, "Quest " + key, "desc " + key, 86400L);
        config.setTier("easy");
        config.setGroup(group);

        for (int i = 0; i < stageCount; i++) {
            QuestStageConfig stage = new QuestStageConfig("stage_" + i, "Stage " + i, Flow.PARALLEL);
            stage.addObjective(new TestObjectiveConfig("obj_" + i, "block_break", DEFAULT_TARGET));
            config.addStage(stage);
        }

        group.addQuest(config);
        return config;
    }

    /**
     * Builds a runtime instance of {@code config} for {@code actor} at {@code progress},
     * and attaches it to the actor (which creates the
     * {@link com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup}).
     */
    static Quest instance(QuestActor actor, QuestConfig config, QuestStatus status, int progress) {
        Date now = new Date();
        Quest quest = new Quest(UUID.randomUUID(), config, actor, status, null, now, now);
        QuestStageConfig stageConfig = config.getStages().get("stage_1");
        QuestStage stage = new QuestStage(quest, stageConfig);
        TestObjectiveConfig objectiveConfig = (TestObjectiveConfig) stageConfig.getObjectives().get("obj_1");
        QuestObjective objective = new TestObjective(stage, objectiveConfig, progress);
        stage.addObjective(objective);
        quest.addStage(stage);
        actor.addQuest(quest);
        return quest;
    }
}
