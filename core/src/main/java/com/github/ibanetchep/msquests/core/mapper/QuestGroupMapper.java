package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.*;
import com.github.ibanetchep.msquests.core.factory.QuestActionFactory;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestTierConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.DistributionConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.DistributionTrigger;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionStrategy;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;

import java.util.*;
import java.util.stream.Collectors;

public class QuestGroupMapper {

    private final QuestConfigMapper questConfigMapper;
    private final QuestActionFactory questActionFactory;

    public QuestGroupMapper(QuestConfigMapper questConfigMapper, QuestActionFactory questActionFactory) {
        this.questConfigMapper = questConfigMapper;
        this.questActionFactory = questActionFactory;
    }

    /**
     * Convert a QuestGroup entity to a QuestGroupDTO
     * @param entity The QuestGroup entity to convert
     * @return The converted QuestGroupDTO
     */
    public QuestGroupConfigDTO toDTO(QuestGroupConfig entity) {
        if (entity == null) {
            return null;
        }

        List<QuestConfigDTO> questDtos = entity.getOrderedQuests().stream()
                .map(questConfigMapper::toDTO)
                .toList();

        Map<String, QuestTierConfigDTO> tierDtos = null;
        if (!entity.getTiers().isEmpty()) {
            tierDtos = new HashMap<>();
            for (Map.Entry<String, QuestTierConfig> entry : entity.getTiers().entrySet()) {
                tierDtos.put(entry.getKey(), new QuestTierConfigDTO(entry.getValue().getName()));
            }
        }

        DistributionConfigDTO distributionDto = null;
        if (entity.hasDistribution()) {
            DistributionConfig dc = entity.getDistributionConfig();
            distributionDto = new DistributionConfigDTO(
                    dc.getStrategy().name(),
                    dc.getAmount(),
                    dc.getTriggers().stream().map(t -> t.name().toLowerCase()).toList()
            );
        }

        return new QuestGroupConfigDTO(
                entity.getKey(),
                entity.getName(),
                entity.getDescription(),
                questDtos,
                entity.getMaxActive(),
                entity.getMaxPerPeriod(),
                entity.getResetCron(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getActorType(),
                new QuestGroupConfigActionsDTO(
                        entity.getQuestStartActions().stream().map(QuestAction::toDTO).toList(),
                        entity.getQuestCompleteActions().stream().map(QuestAction::toDTO).toList(),
                        entity.getObjectiveProgressActions().stream().map(QuestAction::toDTO).toList(),
                        entity.getObjectiveCompleteActions().stream().map(QuestAction::toDTO).toList(),
                        entity.getQuestDistributionActions().stream().map(QuestAction::toDTO).toList(),
                        entity.getActorLoadActions().stream().map(QuestAction::toDTO).toList(),
                        entity.getAllQuestsCompleteActions().stream().map(QuestAction::toDTO).toList()
                ),
                tierDtos,
                entity.getTierDistribution(),
                entity.isRotatable(),
                entity.getMaxRotationsPerPeriod(),
                distributionDto
        );
    }

    /**
     * Convert a QuestGroupDTO to a QuestGroup entity
     * @param dto The QuestGroupDTO to convert
     * @return The converted QuestGroup entity
     */
    public QuestGroupConfig toEntity(QuestGroupConfigDTO dto) {
        if (dto == null) {
            return null;
        }

        List<QuestAction> questStartActions = dto.actions().questStart().stream()
                .map(questActionFactory::createAction)
                .toList();

        List<QuestAction> questCompleteActions = dto.actions().questComplete().stream()
                .map(questActionFactory::createAction)
                .toList();

        List<QuestAction> objectiveProgressActions = dto.actions().objectiveProgress().stream()
                .map(questActionFactory::createAction)
                .toList();

        List<QuestAction> objectiveCompleteActions = dto.actions().objectiveComplete().stream()
                .map(questActionFactory::createAction)
                .toList();

        List<QuestAction> questDistributionActions = dto.actions().questDistribution() != null
                ? dto.actions().questDistribution().stream().map(questActionFactory::createAction).toList()
                : List.of();

        List<QuestAction> actorLoadActions = dto.actions().actorLoad() != null
                ? dto.actions().actorLoad().stream().map(questActionFactory::createAction).toList()
                : List.of();

        List<QuestAction> allQuestsCompleteActions = dto.actions().allQuestsComplete() != null
                ? dto.actions().allQuestsComplete().stream().map(questActionFactory::createAction).toList()
                : List.of();

        Map<String, QuestTierConfig> tiers = null;
        if (dto.tiers() != null) {
            tiers = new HashMap<>();
            for (Map.Entry<String, QuestTierConfigDTO> entry : dto.tiers().entrySet()) {
                tiers.put(entry.getKey(), new QuestTierConfig(entry.getKey(), entry.getValue().name()));
            }
        }

        DistributionConfig distributionConfig = null;
        if (dto.distribution() != null) {
            DistributionConfigDTO d = dto.distribution();
            QuestDistributionStrategy strategy = QuestDistributionStrategy.valueOf(d.strategy().toUpperCase());
            int amount = d.amount() != null ? d.amount() : 1;
            Set<DistributionTrigger> triggers = d.triggers() != null
                    ? d.triggers().stream().map(t -> DistributionTrigger.valueOf(t.toUpperCase())).collect(Collectors.toSet())
                    : Set.of();
            distributionConfig = new DistributionConfig(strategy, amount, triggers);
        }

        QuestGroupConfig questGroupConfig = new QuestGroupConfig.Builder(dto.key(), dto.name(), dto.description(), dto.actorType())
                .maxActive(dto.maxActive())
                .maxPerPeriod(dto.maxPerPeriod())
                .resetCron(dto.resetCron())
                .startAt(dto.startAt())
                .endAt(dto.endAt())
                .questStartActions(questStartActions)
                .questCompleteActions(questCompleteActions)
                .objectiveProgressActions(objectiveProgressActions)
                .objectiveCompleteActions(objectiveCompleteActions)
                .questDistributionActions(questDistributionActions)
                .actorLoadActions(actorLoadActions)
                .allQuestsCompleteActions(allQuestsCompleteActions)
                .tiers(tiers)
                .tierDistribution(dto.tierDistribution())
                .rotatable(dto.rotatable() != null && dto.rotatable())
                .maxRotationsPerPeriod(dto.maxRotationsPerPeriod())
                .distributionConfig(distributionConfig)
                .build();

        for (QuestConfigDTO questConfigDTO : dto.quests()) {
            QuestConfig questConfig = questConfigMapper.toEntity(questConfigDTO);
            questGroupConfig.addQuest(questConfig);
        }

        return questGroupConfig;
    }
}
