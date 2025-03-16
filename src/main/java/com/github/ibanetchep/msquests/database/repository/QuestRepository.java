package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.dto.QuestDTO;
import com.github.ibanetchep.msquests.database.dto.QuestObjectiveDTO;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class QuestRepository extends Repository {

    public QuestRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    public Map<UUID, QuestDTO> getAllByActor(UUID actorUniqueId) {
        String query = """
        SELECT
            qe.unique_id qe_unique_id,
            qe.quest_id qe_quest_id,
            qe.actor_id qe_actor_id,
            qe.status qe_status,
            qe.started_at qe_started_at,
            qe.completed_at qe_completed_at,
            qe.expires_at qe_expires_at,
            qe.created_at qe_created_at,
            qe.updated_at qe_updated_at,
            qoe.unique_id qoe_unique_id,
            qoe.quest_entry_id qoe_quest_entry_id,
            qoe.objective_id qoe_objective_id,
            qoe.progress qoe_progress,
            qoe.objective_status qoe_objective_status,
            qoe.started_at qoe_started_at,
            qoe.completed_at qoe_completed_at,
            qoe.created_at qoe_created_at,
            qoe.updated_at qoe_updated_at
        FROM msquests_quest_entries qe
        LEFT JOIN msquests_quest_objective_entries qoe ON qoe.quest_entry_id = qe.unique_id
        WHERE qe.actor_id = ?
        """;

        return getJdbi().withHandle(handle -> handle
                .createQuery(query)
                .bind(0, actorUniqueId)
                .registerRowMapper(BeanMapper.factory(QuestDTO.class, "qe_"))
                .registerRowMapper(BeanMapper.factory(QuestObjectiveDTO.class, "qoe_"))
                .reduceRows(new LinkedHashMap<UUID, QuestDTO>(),
                        (map, rowView) -> {
                            QuestDTO entry = map.computeIfAbsent(
                                    rowView.getColumn("qe_unique_id", UUID.class),
                                    id -> rowView.getRow(QuestDTO.class)
                            );

                            if (rowView.getColumn("qoe_unique_id", UUID.class) != null) {
                                QuestObjectiveDTO objective = rowView.getRow(QuestObjectiveDTO.class);
                                entry.objectives().put(objective.uniqueId(), objective);
                            }

                            return map;
                        }
                ));
    }
}
