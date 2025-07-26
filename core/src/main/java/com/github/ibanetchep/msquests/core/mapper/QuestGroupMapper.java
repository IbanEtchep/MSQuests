package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupDTO;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.QuestGroup;

import java.util.HashMap;
import java.util.Map;

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
    public QuestGroupDTO toDto(QuestGroup entity) {
        if (entity == null) {
            return null;
        }

        Map<String, QuestConfigDTO> questDtos = new HashMap<>();
        for (Map.Entry<String, QuestConfig> entry : entity.getQuestConfigs().entrySet()) {
            questDtos.put(entry.getKey(), questConfigMapper.toDto(entry.getValue()));
        }

        return new QuestGroupDTO(
                entity.getKey(),
                entity.getName(),
                questDtos
        );
    }

    /**
     * Convert a QuestGroupDTO to a QuestGroup entity
     * @param dto The QuestGroupDTO to convert
     * @return The converted QuestGroup entity
     */
    public QuestGroup toEntity(QuestGroupDTO dto) {
        if (dto == null) {
            return null;
        }

        QuestGroup questGroup = new QuestGroup(
                dto.key(),
                dto.name(),
                ""
        );

        for (Map.Entry<String, QuestConfigDTO> entry : dto.quests().entrySet()) {
            QuestConfig questConfig = questConfigMapper.toEntity(entry.getValue());
            questGroup.addQuest(questConfig);
        }

        return questGroup;
    }
}
