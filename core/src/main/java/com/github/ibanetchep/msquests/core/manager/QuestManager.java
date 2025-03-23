package com.github.ibanetchep.msquests.core.manager;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestDefinitionDTO;
import com.github.ibanetchep.msquests.core.mapper.QuestDefinitionMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestEntryMapper;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestDefinition;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.registry.ActorTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;
import com.github.ibanetchep.msquests.core.repository.ActorRepository;
import com.github.ibanetchep.msquests.core.repository.QuestDefinitionRepository;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.core.scheduler.Scheduler;
import com.github.ibanetchep.msquests.core.strategy.ActorStrategy;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {

    private final Map<UUID, QuestDefinition> questDefinitions = new ConcurrentHashMap<>();
    private final Map<UUID, QuestActor> actors = new ConcurrentHashMap<>();
    private final Map<UUID, Quest> quests = new ConcurrentHashMap<>();

    private final Scheduler scheduler;

    private final QuestDefinitionRepository questDefinitionRepository;
    private final ActorRepository actorRepository;
    private final QuestRepository questRepository;

    private final QuestDefinitionMapper questDefinitionMapper;
    private final QuestEntryMapper questEntryMapper;

    private final ActorTypeRegistry actorTypeRegistry;
    private final ObjectiveTypeRegistry objectiveTypeRegistry;

    private final Map<String, Class<? extends QuestActor>> actorTypes = new ConcurrentHashMap<>();
    private final Map<Class<? extends QuestActor>, ActorStrategy> actorStrategies = new ConcurrentHashMap<>();

    public QuestManager(
            Scheduler scheduler,
            QuestDefinitionRepository questDefinitionRepository,
            ActorRepository actorRepository,
            QuestRepository questRepository,
            QuestDefinitionMapper questDefinitionMapper,
            QuestEntryMapper questEntryMapper,
            ActorTypeRegistry actorTypeRegistry,
            ObjectiveTypeRegistry objectiveTypeRegistry
    ) {
        this.scheduler = scheduler;
        this.questDefinitionRepository = questDefinitionRepository;
        this.actorRepository = actorRepository;
        this.questRepository = questRepository;
        this.questDefinitionMapper = questDefinitionMapper;
        this.questEntryMapper = questEntryMapper;
        this.actorTypeRegistry = actorTypeRegistry;
        this.objectiveTypeRegistry = objectiveTypeRegistry;

        scheduler.runAsyncQueued(this::loadQuestDefinitions);
    }

    public void loadQuestDefinitions() {
        Map<UUID, QuestDefinitionDTO> questDefinitionDtos = questDefinitionRepository.getAll();

        for (QuestDefinitionDTO questDefinitionDTO : questDefinitionDtos.values()) {
            QuestDefinition questDefinition = questDefinitionMapper.toEntity(questDefinitionDTO);
            questDefinitions.put(questDefinition.getId(), questDefinition);
        }
    }

    public void loadActor(String type, UUID uuid) {
        QuestActorDTO actorDTO = actorRepository.get(uuid);

        if (actorDTO == null) {
            actorDTO = new QuestActorDTO(type, UUID.randomUUID());
            actorRepository.add(actorDTO);
        }

        QuestActor actor = actorTypeRegistry.createActor(actorDTO);
        actors.put(actor.getId(), actor);

        loadQuests(actor);
    }

    private void loadQuests(QuestActor actor) {
        Map<UUID, QuestDTO> questEntryDtos = questRepository.getAllByActor(actor.getId());

        for (QuestDTO questEntryDTO : questEntryDtos.values()) {
            QuestDefinition questDefinition = questDefinitions.get(questEntryDTO.questId());
            Quest quest = questEntryMapper.toEntity(questEntryDTO, actor, questDefinition);
            quests.put(quest.getId(), quest);
        }
    }

    public void saveQuestDefinition(QuestDefinition questDefinition) {
        this.questDefinitions.put(questDefinition.getId(), questDefinition);
        scheduler.runAsyncQueued(() -> {
            QuestDefinitionDTO questDefinitionDTO = questDefinitionMapper.toDto(questDefinition);
            questDefinitionRepository.upsert(questDefinitionDTO);
        });
    }
}
