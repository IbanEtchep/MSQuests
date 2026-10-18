package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.*;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestTierConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.ActionChain;
import com.github.ibanetchep.msquests.core.quest.config.group.DistributionConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.DistributionTrigger;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionStrategy;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class QuestGroupMapper {

    private final QuestConfigMapper questConfigMapper;

    public QuestGroupMapper(QuestConfigMapper questConfigMapper) {
        this.questConfigMapper = questConfigMapper;
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
                        entity.getQuestStartActions().steps(),
                        entity.getQuestCompleteActions().steps(),
                        entity.getObjectiveProgressActions().steps(),
                        entity.getObjectiveCompleteActions().steps(),
                        entity.getQuestDistributionActions().steps(),
                        entity.getActorLoadActions().steps(),
                        entity.getAllPeriodQuestsCompleteActions().steps()
                ),
                tierDtos,
                entity.getTierDistribution(),
                entity.isRotatable(),
                entity.getMaxRotationsPerPeriod(),
                distributionDto,
                QuestGroupConfig.DEFAULT_OBJECTIVE_LINE.equals(entity.getObjectiveLine()) ? null : entity.getObjectiveLine(),
                entity.getUid(),
                entity.getUid() != null ? entity.getResetZone().getId() : null,
                entity.getSourceProject()
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

        QuestGroupConfigActionsDTO actions = dto.actions() != null
                ? dto.actions()
                : new QuestGroupConfigActionsDTO(null, null, null, null, null, null, null);

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

        QuestGroupConfig.Builder builder = new QuestGroupConfig.Builder(dto.key(), dto.name(), dto.description(), dto.actorType());
        if (dto.maxActive() != null) {
            builder.maxActive(dto.maxActive()); // absent: the builder's default, instead of an unboxing NPE
        }
        QuestGroupConfig questGroupConfig = builder
                .maxPerPeriod(dto.maxPerPeriod())
                .resetCron(dto.resetCron())
                .startAt(dto.startAt())
                .endAt(dto.endAt())
                .questStartActions(ActionChain.of(actions.questStart()))
                .questCompleteActions(ActionChain.of(actions.questComplete()))
                .objectiveProgressActions(ActionChain.of(actions.objectiveProgress()))
                .objectiveCompleteActions(ActionChain.of(actions.objectiveComplete()))
                .questDistributionActions(ActionChain.of(actions.questDistribution()))
                .actorLoadActions(ActionChain.of(actions.actorLoad()))
                .allPeriodQuestsCompleteActions(ActionChain.of(actions.allPeriodQuestsComplete()))
                .tiers(tiers)
                .tierDistribution(dto.tierDistribution())
                .rotatable(dto.rotatable() != null && dto.rotatable())
                .maxRotationsPerPeriod(dto.maxRotationsPerPeriod())
                .distributionConfig(distributionConfig)
                .objectiveLine(dto.objectiveLine())
                .uid(dto.uid())
                .sourceProject(dto.sourceProject())
                .resetZone(dto.resetTimezone() != null ? ZoneId.of(dto.resetTimezone()) : null)
                .build();

        for (QuestConfigDTO questConfigDTO : dto.quests()) {
            QuestConfig questConfig = questConfigMapper.toEntity(questConfigDTO);
            questGroupConfig.addQuest(questConfig);
        }

        return questGroupConfig;
    }
}
