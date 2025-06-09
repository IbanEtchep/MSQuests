package com.github.ibanetchep.msquests.core.manager;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.mapper.QuestConfigMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestEntryMapper;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.registry.ActorTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;
import com.github.ibanetchep.msquests.core.repository.ActorRepository;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.core.strategy.ActorStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class QuestManager {

    private final Logger logger;

    private final Map<String, QuestConfig> questConfigs = new ConcurrentHashMap<>();
    private final Map<UUID, QuestActor> actors = new ConcurrentHashMap<>();
    private final Map<UUID, Quest> quests = new ConcurrentHashMap<>();

    private final QuestConfigRepository questConfigRepository;
    private final ActorRepository actorRepository;
    private final QuestRepository questRepository;

    private final QuestConfigMapper questConfigMapper;
    private final QuestEntryMapper questEntryMapper;

    private final ActorTypeRegistry actorTypeRegistry;
    private final ObjectiveTypeRegistry objectiveTypeRegistry;

    private final Map<String, Class<? extends QuestActor>> actorTypes = new ConcurrentHashMap<>();
    private final Map<Class<? extends QuestActor>, ActorStrategy> actorStrategies = new ConcurrentHashMap<>();

    public QuestManager(
            Logger logger,
            QuestConfigRepository questConfigRepository,
            ActorRepository actorRepository,
            QuestRepository questRepository,
            QuestConfigMapper questConfigMapper,
            QuestEntryMapper questEntryMapper,
            ActorTypeRegistry actorTypeRegistry,
            ObjectiveTypeRegistry objectiveTypeRegistry
    ) {
        this.logger = logger;
        this.questConfigRepository = questConfigRepository;
        this.actorRepository = actorRepository;
        this.questRepository = questRepository;
        this.questConfigMapper = questConfigMapper;
        this.questEntryMapper = questEntryMapper;
        this.actorTypeRegistry = actorTypeRegistry;
        this.objectiveTypeRegistry = objectiveTypeRegistry;

        loadQuestConfigs();
    }

    public void loadQuestConfigs() {
        questConfigs.clear();
        questConfigRepository.getAll().thenAccept(questConfigDtos -> {
            for (QuestConfigDTO questConfigDTO : questConfigDtos.values()) {
                QuestConfig questConfig = questConfigMapper.toEntity(questConfigDTO);
                questConfigs.put(questConfig.getKey(), questConfig);
            }

            logger.info("Loaded " + questConfigs.size() + " quest configs");
        });
    }

    public void loadActor(String type, UUID uuid) {
        actors.remove(uuid);

        actorRepository.get(uuid).thenAccept(actorDTO -> {
            if (actorDTO == null) {
                actorDTO = new QuestActorDTO(type, UUID.randomUUID());
                actorRepository.add(actorDTO);
            }

            QuestActor actor = actorTypeRegistry.createActor(actorDTO);
            actors.put(actor.getId(), actor);

            loadQuests(actor);
        });
    }

    private void loadQuests(QuestActor actor) {
        questRepository.getAllByActor(actor.getId()).thenAccept(questEntryDtos -> {
            for (QuestDTO questDTO : questEntryDtos.values()) {
                QuestConfig questConfig = questConfigs.get(questDTO.questKey());
                Quest quest = questEntryMapper.toEntity(questDTO, actor, questConfig);
                quests.put(quest.getId(), quest);
            }
        });
    }

    public void saveQuestConfig(QuestConfig questConfig) {
        this.questConfigs.put(questConfig.getKey(), questConfig);
        QuestConfigDTO questConfigDTO = questConfigMapper.toDto(questConfig);
        questConfigRepository.upsert(questConfigDTO);
    }

    public List<Quest> getActiveQuests(UUID playerId) {
        return quests.values().stream()
                .filter(quest -> quest.getActor().isActor(playerId) && quest.getStatus() == QuestStatus.IN_PROGRESS)
                .toList();
    }
    @SuppressWarnings("unchecked")
    public <T extends QuestObjective<?>> List<T> getObjectivesByType(UUID playerId, String objectiveType) {
        return getActiveQuests(playerId).stream()
                .flatMap(quest -> quest.getObjectives().values().stream())
                .filter(objective -> objectiveType.equals(objective.getType()))
                .map(objective -> (T) objective)
                .toList();
    }
}
