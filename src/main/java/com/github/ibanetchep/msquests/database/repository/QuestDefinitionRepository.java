package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.dto.QuestDTO;
import com.github.ibanetchep.msquests.database.dto.QuestDefinitionDTO;
import com.github.ibanetchep.msquests.database.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.database.dto.QuestObjectiveDefinitionDTO;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class QuestDefinitionRepository extends Repository {

    public QuestDefinitionRepository(DbAccess dbAccess) {
        super(dbAccess);
    }


    public Map<UUID, QuestDefinitionDTO> getAll() {
        String query = """
                SELECT
                    msquests_definition.id as quest_id,
                    msquests_definition.name as quest_name,
                    msquests_definition.description as quest_description,
                    msquests_definition.tags as quest_tags,
                    msquests_definition.rewards as quest_rewards,
                    msquests_definition.duration as quest_duration,
                    msquests_definition.created_at as quest_created_at,
                    msquests_definition.updated_at as quest_updated_at,
                    msquests_objective_definition.id as objective_id,
                    msquests_objective_definition.objective_type as objective_type,
                    msquests_objective_definition.config as objective_config
                """;

        return dbAccess.getJdbi().withHandle(handle -> handle.createQuery(query)
                .registerRowMapper(BeanMapper.factory(QuestDefinitionDTO.class, "quest_"))
                .registerRowMapper(BeanMapper.factory(QuestObjectiveDefinitionDTO.class, "objective_"))
                .reduceRows(new LinkedHashMap<UUID, QuestDefinitionDTO>(), (map, rowView) -> {
                    QuestDefinitionDTO quest = rowView.getRow(QuestDefinitionDTO.class);
                    QuestObjectiveDefinitionDTO objective = rowView.getRow(QuestObjectiveDefinitionDTO.class);
                    quest.objectives().put(objective.uniqueId(), objective);
                    map.put(quest.uniqueId(), quest);
                    return map;
                }));
    }
}
