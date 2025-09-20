package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupDTO;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.ChainedQuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.PoolQuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;

import java.util.List;

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
    public QuestGroupDTO toDto(QuestGroupConfig entity) {
        if (entity == null) {
            return null;
        }

        List<QuestConfigDTO> questDtos = entity.getOrderedQuests().stream()
                .map(questConfigMapper::toDto)
                .toList();

        int maxActiveQuests = 0;
        int maxPerPeriod = 0;
        String periodSwitchCron = null;

        if (entity instanceof PoolQuestGroupConfig pool) {
            maxActiveQuests = pool.getMaxActiveQuests();
            maxPerPeriod = pool.getMaxPerPeriod();
            periodSwitchCron = pool.getPeriodSwitchCron();
        }

        return new QuestGroupDTO(
                entity.getKey(),
                entity.getName(),
                entity.getDescription(),
                questDtos,
                entity.getType(),
                maxActiveQuests,
                maxPerPeriod,
                periodSwitchCron,
                entity.getStartAt(),
                entity.getEndAt()
        );
    }

    /**
     * Convert a QuestGroupDTO to a QuestGroup entity
     * @param dto The QuestGroupDTO to convert
     * @return The converted QuestGroup entity
     */
    public QuestGroupConfig toEntity(QuestGroupDTO dto) {
        if (dto == null) {
            return null;
        }

        QuestGroupConfig questGroupConfig;

        switch (dto.type()) {
            case POOL:
                questGroupConfig = new PoolQuestGroupConfig(
                        dto.key(),
                        dto.name(),
                        dto.description(),
                        dto.startAt(),
                        dto.endAt(),
                        dto.periodSwitchCron(),
                        dto.maxActiveQuests(),
                        dto.maxPerPeriod()
                );
                break;
            case CHAINED:
                questGroupConfig = new ChainedQuestGroupConfig(
                        dto.key(),
                        dto.name(),
                        dto.description(),
                        dto.startAt(),
                        dto.endAt()
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown quest group type: " + dto.type());
        }

        for (QuestConfigDTO questConfigDTO : dto.quests()) {
            QuestConfig questConfig = questConfigMapper.toEntity(questConfigDTO);
            questGroupConfig.addQuest(questConfig);
        }

        return questGroupConfig;
    }
}
