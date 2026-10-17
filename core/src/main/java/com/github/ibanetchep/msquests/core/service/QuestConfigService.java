package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.condition.ConditionChecker;
import com.github.ibanetchep.msquests.core.quest.config.condition.Conditions;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.repository.QuestConfigLoad;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestConfigService {

    private final Logger logger;
    private final QuestConfigRegistry questRegistry;
    private final QuestConfigRepository questConfigRepository;
    private final QuestGroupMapper questGroupMapper;
    private final ConditionChecker conditionChecker;

    public QuestConfigService(
            Logger logger,
            QuestConfigRegistry questRegistry,
            QuestConfigRepository questConfigRepository,
            QuestGroupMapper questGroupMapper
    ) {
        this(logger, questRegistry, questConfigRepository, questGroupMapper, ConditionChecker.ALWAYS);
    }

    /** @param conditionChecker checks the loaded requirements and objective conditions, to warn about bad ones */
    public QuestConfigService(
            Logger logger,
            QuestConfigRegistry questRegistry,
            QuestConfigRepository questConfigRepository,
            QuestGroupMapper questGroupMapper,
            ConditionChecker conditionChecker
    ) {
        this.conditionChecker = conditionChecker;
        this.logger = logger;
        this.questRegistry = questRegistry;
        this.questConfigRepository = questConfigRepository;
        this.questGroupMapper = questGroupMapper;
    }

    /**
     * Startup load: every group that parses is registered, broken files are logged and skipped,
     * so one bad file does not leave the server without quests.
     */
    public CompletableFuture<Void> loadQuestGroups() {
        return questConfigRepository.loadAllGroups()
                .thenAccept(load -> {
                    List<String> failures = new ArrayList<>(load.failures());
                    List<QuestGroupConfig> groups = mapGroups(load, failures);
                    failures.forEach(f -> logger.severe("Quest group skipped: " + f));
                    publish(groups);
                })
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to load quest groups", e);
                    return null;
                });
    }

    /**
     * Admin reload: the new state replaces the current one only if every file parses and maps.
     * Otherwise nothing changes and the returned future fails with the list of problems, so the
     * server keeps running the configs it had instead of losing the broken groups.
     */
    public CompletableFuture<Void> reloadQuestGroups() {
        return questConfigRepository.loadAllGroups().thenAccept(load -> {
            List<String> failures = new ArrayList<>(load.failures());
            List<QuestGroupConfig> groups = mapGroups(load, failures);

            if (!failures.isEmpty()) {
                failures.forEach(f -> logger.severe("Quest reload refused: " + f));
                throw new CompletionException(new QuestConfigReloadException(failures));
            }

            publish(groups);
        });
    }

    private List<QuestGroupConfig> mapGroups(QuestConfigLoad load, List<String> failures) {
        List<QuestGroupConfig> groups = new ArrayList<>();
        for (QuestGroupConfigDTO dto : load.groups().values()) {
            try {
                groups.add(questGroupMapper.toEntity(dto));
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "Failed to build quest group " + dto.key(), e);
                failures.add("group '" + dto.key() + "': " + e.getMessage());
            }
        }
        return groups;
    }

    private void publish(List<QuestGroupConfig> groups) {
        groups.forEach(this::warnDuplicateObjectiveKeys);
        groups.forEach(this::warnInvalidConditions);
        questRegistry.replaceQuestGroupConfigs(groups);
        for (QuestGroupConfig group : groups) {
            logger.info("Loaded group " + group.getKey() + " with " + group.getQuestConfigs().size() + " quests.");
        }
    }

    /**
     * Progress is stored per (quest, objective key), without the stage: two objectives sharing a
     * key in different stages share one database row, and the later stage inherits the earlier
     * one's progress. Only warned, not refused, so an existing server keeps loading its quests.
     */
    private void warnDuplicateObjectiveKeys(QuestGroupConfig group) {
        for (QuestConfig quest : group.getQuestConfigs().values()) {
            Set<String> seen = new HashSet<>();
            Set<String> duplicates = new LinkedHashSet<>();
            for (QuestStageConfig stage : quest.getStages().values()) {
                for (String objectiveKey : stage.getObjectives().keySet()) {
                    if (!seen.add(objectiveKey)) {
                        duplicates.add(objectiveKey);
                    }
                }
            }
            if (!duplicates.isEmpty()) {
                logger.severe("Quest '" + group.getKey() + "/" + quest.getKey() + "' reuses objective key(s) "
                        + duplicates + " in several stages. Their progress is saved in the same row and will mix up;"
                        + " give every objective of a quest a distinct key.");
            }
        }
    }

    /**
     * A condition the platform would reject always fails (a quest that can never start, an
     * objective that never counts): warned at load rather than refused, like a duplicate key.
     */
    private void warnInvalidConditions(QuestGroupConfig group) {
        for (QuestConfig quest : group.getQuestConfigs().values()) {
            String path = group.getKey() + "/" + quest.getKey();
            warnInvalid(path + " requirements", quest.getRequirements());
            for (QuestStageConfig stage : quest.getStages().values()) {
                for (QuestObjectiveConfig objective : stage.getObjectives().values()) {
                    warnInvalid(path + " objective " + objective.getKey() + " conditions", objective.getConditions());
                }
            }
        }
    }

    private void warnInvalid(String where, Conditions conditions) {
        if (conditions.isEmpty()) return;
        for (String problem : conditionChecker.validate(conditions)) {
            logger.warning("Quest " + where + ": " + problem + " (this condition never holds).");
        }
    }

    /** Thrown when a reload is refused; carries one human-readable line per problem. */
    public static class QuestConfigReloadException extends RuntimeException {
        private final List<String> failures;

        public QuestConfigReloadException(List<String> failures) {
            super(failures.size() + " quest config problem(s): " + String.join("; ", failures));
            this.failures = List.copyOf(failures);
        }

        public List<String> getFailures() {
            return failures;
        }
    }

}
