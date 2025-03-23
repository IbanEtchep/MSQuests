package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;

import java.util.Map;
import java.util.UUID;

public interface QuestRepository {

    Map<UUID, QuestDTO> getAllByActor(UUID actorUniqueId);
}
