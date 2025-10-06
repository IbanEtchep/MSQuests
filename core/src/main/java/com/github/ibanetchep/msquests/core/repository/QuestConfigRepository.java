package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface QuestConfigRepository {

    CompletableFuture<Map<String, QuestGroupConfigDTO>> getAllGroups();

}
