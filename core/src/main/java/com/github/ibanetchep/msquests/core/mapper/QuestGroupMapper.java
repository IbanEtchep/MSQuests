package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupDTO;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionMode;
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
    public QuestGroupDTO toDTO(QuestGroupConfig entity) {
        if (entity == null) {
            return null;
        }

        List<QuestConfigDTO> questDtos = entity.getOrderedQuests().stream()
                .map(questConfigMapper::toDTO)
                .toList();

        return new QuestGroupDTO(
                entity.getKey(),
                entity.getName(),
                entity.getDescription(),
                questDtos,
                entity.getDistributionMode().toString(),
                entity.getMaxActive(),
                entity.getMaxPerPeriod(),
                entity.getResetCron(),
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

        QuestDistributionMode distributionMode = QuestDistributionMode.SEQUENTIAL;
        if (dto.distributionMode() != null) {
            distributionMode = QuestDistributionMode.valueOf(dto.distributionMode().toUpperCase());
        }

        QuestGroupConfig questGroupConfig = new QuestGroupConfig.Builder(dto.key(), dto.name(), dto.description())
                .distributionMode(distributionMode)
                .maxActive(dto.maxActive())
                .maxPerPeriod(dto.maxPerPeriod())
                .resetCron(dto.resetCron())
                .startAt(dto.startAt())
                .endAt(dto.endAt())
                .build();

        for (QuestConfigDTO questConfigDTO : dto.quests()) {
            QuestConfig questConfig = questConfigMapper.toEntity(questConfigDTO);
            questGroupConfig.addQuest(questConfig);
        }

        return questGroupConfig;
    }
}
