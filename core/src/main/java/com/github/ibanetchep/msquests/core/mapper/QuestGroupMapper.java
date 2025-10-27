package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigActionsDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.factory.QuestActionFactory;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;

import java.util.List;

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
                        entity.getObjectiveCompleteActions().stream().map(QuestAction::toDTO).toList()
                )
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

        List<QuestAction> objectiveProgressActions = dto.actions().objectiveComplete().stream()
                .map(questActionFactory::createAction)
                .toList();

        List<QuestAction> objectiveCompleteActions = dto.actions().objectiveComplete().stream()
                .map(questActionFactory::createAction)
                .toList();


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
                .build();

        for (QuestConfigDTO questConfigDTO : dto.quests()) {
            QuestConfig questConfig = questConfigMapper.toEntity(questConfigDTO);
            questGroupConfig.addQuest(questConfig);
        }

        return questGroupConfig;
    }
}
