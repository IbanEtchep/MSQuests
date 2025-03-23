package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.dto.QuestDefinitionDTO;

import java.util.Map;
import java.util.UUID;

public interface QuestDefinitionRepository {

    void upsert(QuestDefinitionDTO dto);

    Map<UUID, QuestDefinitionDTO> getAll();

    void delete(UUID id);
}
