package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface QuestConfigRepository {

    /** Reads every group, reporting the files that failed instead of dropping them silently. */
    CompletableFuture<QuestConfigLoad> loadAllGroups();

    default CompletableFuture<Map<String, QuestGroupConfigDTO>> getAllGroups() {
        return loadAllGroups().thenApply(QuestConfigLoad::groups);
    }

}
