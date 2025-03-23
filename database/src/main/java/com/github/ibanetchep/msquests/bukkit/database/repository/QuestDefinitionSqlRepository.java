package com.github.ibanetchep.msquests.bukkit.database.repository;

import com.github.ibanetchep.msquests.bukkit.database.DbAccess;
import com.github.ibanetchep.msquests.core.dto.QuestDefinitionDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDefinitionDTO;
import com.github.ibanetchep.msquests.core.repository.QuestDefinitionRepository;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

import java.util.*;
import java.util.stream.Collectors;

public class QuestDefinitionSqlRepository extends SqlRepository implements QuestDefinitionRepository {

    public QuestDefinitionSqlRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    @Override
    public void upsert(QuestDefinitionDTO dto) {
        dbAccess.getJdbi().useTransaction(handle -> {
            handle.createUpdate("""
                INSERT INTO msquests_definition (
                    id, name, description, tags, rewards, duration
                ) VALUES (
                    :id, :name, :description, :tags, :rewards, :duration
                ) ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    description = VALUES(description),
                    tags = VALUES(tags),
                    rewards = VALUES(rewards)
                """)
                .bind("id", dto.id().toString())
                .bind("name", dto.name())
                .bind("description", dto.description())
                .bind("tags", dto.tags())
                .bind("rewards", dto.rewards())
                .bind("duration", dto.duration())
                .execute();

            Set<UUID> existingObjectiveIds = handle.createQuery(
                    "SELECT id FROM msquests_objective_definition WHERE quest_definition_id = :quest_id"
                )
                .bind("quest_id", dto.id().toString())
                .mapTo(UUID.class)
                .collect(Collectors.toSet());

            Set<UUID> newObjectiveIds = dto.objectives().keySet();
            Set<UUID> objectivesToDelete = new HashSet<>(existingObjectiveIds);
            objectivesToDelete.removeAll(newObjectiveIds);

            // Delete objectives that no longer exist
            if (!objectivesToDelete.isEmpty()) {
                handle.createUpdate("DELETE FROM msquests_objective_definition WHERE id IN (<ids>)")
                    .bindList("ids", objectivesToDelete)
                    .execute();
            }

            // Insert or update objectives
            if (dto.objectives() != null && !dto.objectives().isEmpty()) {
                for (QuestObjectiveDefinitionDTO objective : dto.objectives().values()) {
                    handle.createUpdate("""
                        INSERT INTO msquests_objective_definition (
                            id, objective_type, config, quest_definition_id
                        ) VALUES (
                            :id, :type, :config, :quest_id
                        ) ON DUPLICATE KEY UPDATE
                            objective_type = VALUES(objective_type),
                            config = VALUES(config)
                        """)
                        .bind("id", objective.id().toString())
                        .bind("type", objective.type())
                        .bind("config", objective.config())
                        .bind("quest_id", dto.id().toString())
                        .execute();
                }
            }
        });
    }

    @Override
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
                FROM msquests_definition
                LEFT JOIN msquests_objective_definition ON msquests_objective_definition.quest_definition_id = msquests_definition.id
                """;

        return dbAccess.getJdbi().withHandle(handle -> handle.createQuery(query)
                .registerRowMapper(ConstructorMapper.factory(QuestDefinitionDTO.class, "quest_"))
                .registerRowMapper(ConstructorMapper.factory(QuestObjectiveDefinitionDTO.class, "objective_"))
                .reduceRows(new LinkedHashMap<>(), (map, rowView) -> {
                    QuestDefinitionDTO quest = map.computeIfAbsent(
                            rowView.getColumn("quest_id", UUID.class),
                            id -> rowView.getRow(QuestDefinitionDTO.class)
                    );

                    if (rowView.getColumn("objective_id", UUID.class) != null) {
                        QuestObjectiveDefinitionDTO objective = rowView.getRow(QuestObjectiveDefinitionDTO.class);
                        quest.objectives().put(objective.id(), objective);
                    }

                    return map;
                }));
    }

    @Override
    public void delete(UUID id) {
        dbAccess.getJdbi().useHandle(handle -> {
            handle.createUpdate("DELETE FROM msquests_definition WHERE id = :id")
                .bind("id", id)
                .execute();
        });
    }
}
