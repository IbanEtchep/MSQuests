package com.github.ibanetchep.msquests.core.manager;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupDTO;
import com.github.ibanetchep.msquests.core.mapper.QuestConfigMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestEntryMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.quest.*;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.registry.ActorTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;
import com.github.ibanetchep.msquests.core.repository.ActorRepository;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.core.strategy.ActorStrategy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestManager {

    private final Logger logger;

    private final Map<String, QuestGroup> questGroups = new ConcurrentHashMap<>();
    private final Map<UUID, QuestActor> actors = new ConcurrentHashMap<>();

    private final QuestConfigRepository questConfigRepository;
    private final ActorRepository actorRepository;
    private final QuestRepository questRepository;

    private final QuestConfigMapper questConfigMapper;
    private final QuestGroupMapper questGroupMapper;
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
            QuestGroupMapper questGroupMapper,
            QuestEntryMapper questEntryMapper,
            ActorTypeRegistry actorTypeRegistry,
            ObjectiveTypeRegistry objectiveTypeRegistry
    ) {
        this.logger = logger;
        this.questConfigRepository = questConfigRepository;
        this.actorRepository = actorRepository;
        this.questRepository = questRepository;
        this.questConfigMapper = questConfigMapper;
        this.questGroupMapper = questGroupMapper;
        this.questEntryMapper = questEntryMapper;
        this.actorTypeRegistry = actorTypeRegistry;
        this.objectiveTypeRegistry = objectiveTypeRegistry;

        loadQuestGroups();
    }

    public void loadQuestGroups() {
        questGroups.clear();
        questConfigRepository.getAllGroups().thenAccept(questGroupDtos -> {
            for (QuestGroupDTO questGroupDTO : questGroupDtos.values()) {
                QuestGroup questGroup = questGroupMapper.toEntity(questGroupDTO);
                questGroups.put(questGroup.getKey(), questGroup);
                logger.info("Loaded group " + questGroup.getKey() + " with " + questGroup.getQuestConfigs().size() + " quests.");
            }
        }).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to load quest groups", e);
            return null;
        });
    }

    public void loadActor(QuestActor actor) {
        actors.remove(actor.getId());

        actorRepository.get(actor.getId()).thenAccept(actorDTO -> {
            if (actorDTO == null) {
                actorDTO = new QuestActorDTO(actor.getActorType(), UUID.randomUUID());
                actorRepository.add(actorDTO);
            }

            actors.put(actor.getId(), actor);

            loadQuests(actor);
        });
    }

    private void loadQuests(QuestActor actor) {
        questRepository.getAllByActor(actor.getId()).thenAccept(questEntryDtos -> {
            for (QuestDTO questDTO : questEntryDtos.values()) {
                QuestGroup questGroup = questGroups.get(questDTO.groupKey());

                if (questGroup == null) {
                    logger.warning("Could not find group " + questDTO.groupKey() + " for quest " + questDTO.questKey());
                    continue;
                }

                QuestConfig questConfig = questGroup.getQuestConfigs().get(questDTO.questKey());

                if (questConfig == null) {
                    logger.warning("Could not find config for quest " + questDTO.questKey() + " in group " + questDTO.groupKey());
                    continue;
                }

                Quest quest = questEntryMapper.toEntity(questDTO, actor, questConfig);
                actor.addQuest(quest);
            }
        });
    }

    public List<QuestActor> getActors(UUID playerId) {
        return actors.values().stream()
                .filter(actor -> actor.isActor(playerId))
                .toList();
    }

    public Map<UUID, QuestActor> getActors() {
        return Collections.unmodifiableMap(actors);
    }

    public Map<String, QuestGroup> getQuestGroups() {
        return Collections.unmodifiableMap(questGroups);
    }

    public List<Quest> getActiveQuests(UUID playerId) {
        return getActors(playerId).stream()
                .flatMap(actor -> actor.getQuests().values().stream())
                .filter(Quest::isActive)
                .toList();
    }

    public Quest getLastQuest(QuestActor actor, QuestConfig questConfig) {
        return actor.getQuests().values().stream()
                .filter(q -> q.getQuestConfig().getKey().equals(questConfig.getKey()))
                .max(Comparator.comparing(Quest::getStartedAt))
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T extends QuestObjective<?>> List<T> getObjectivesByType(UUID playerId, String objectiveType) {
        return getActiveQuests(playerId).stream()
                .flatMap(quest -> quest.getObjectives().values().stream())
                .filter(objective -> objectiveType.equals(objective.getType()))
                .map(objective -> (T) objective)
                .toList();
    }

    public Collection<QuestActor> getActorsByType(String actorType) {
        return actors.values().stream()
                .filter(actor -> actor.getActorType().equals(actorType))
                .toList();
    }
}
