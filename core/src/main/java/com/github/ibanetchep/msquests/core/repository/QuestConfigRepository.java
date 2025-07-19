package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestGroupDTO;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface QuestConfigRepository {

    CompletableFuture<Map<String, QuestGroupDTO>> getAllGroups();

}
