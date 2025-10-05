package com.github.ibanetchep.msquests.bukkit.config;

import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record GlobalConfig(String language, DatabaseConfig databaseConfig, Map<String, ActorConfig> actorTypes) {

    public ActorConfig actorConfig(String actorType) {
        return actorTypes.getOrDefault(actorType, new ActorConfig());
    }

    public record DatabaseConfig(String type, String host, int port, String name, String user, String password) {}

    public record ActorConfig(List<QuestAction> startActions, List<QuestAction> objectiveProgressActions, List<QuestAction> objectiveCompleteActions, List<QuestAction> completeActions) {
        public ActorConfig() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }
}
