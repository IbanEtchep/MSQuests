package com.github.ibanetchep.msquests.manager;

import com.github.ibanetchep.msquests.MSQuestsPlugin;
import com.github.ibanetchep.msquests.database.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.database.dto.QuestDTO;
import com.github.ibanetchep.msquests.database.dto.QuestDefinitionDTO;
import com.github.ibanetchep.msquests.database.repository.ActorRepository;
import com.github.ibanetchep.msquests.database.repository.QuestDefinitionRepository;
import com.github.ibanetchep.msquests.database.repository.QuestRepository;
import com.github.ibanetchep.msquests.mapper.QuestDefinitionMapper;
import com.github.ibanetchep.msquests.mapper.QuestEntryMapper;
import com.github.ibanetchep.msquests.model.actor.QuestActor;
import com.github.ibanetchep.msquests.model.quest.Quest;
import com.github.ibanetchep.msquests.model.quest.QuestDefinition;
import com.github.ibanetchep.msquests.model.quest.definition.BlockBreakObjectiveDefinition;
import com.github.ibanetchep.msquests.registry.ObjectiveDefinitionTypeRegistry;
import com.github.ibanetchep.msquests.strategy.ActorStrategy;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager extends AbstractManager {

    private final Map<UUID, QuestDefinition> questDefinitions = new ConcurrentHashMap<>();
    private final Map<UUID, QuestActor> actors = new ConcurrentHashMap<>();
    private final Map<UUID, Quest> quests = new ConcurrentHashMap<>();

    private final QuestDefinitionRepository questDefinitionRepository;
    private final ActorRepository actorRepository;
    private final QuestRepository questRepository;

    private final QuestDefinitionMapper questDefinitionMapper;
    private final ObjectiveDefinitionTypeRegistry objectiveTypeRegistry;

    private final Map<String, Class<? extends QuestActor>> actorTypes = new ConcurrentHashMap<>();
    private final Map<Class<? extends QuestActor>, ActorStrategy> actorStrategies = new ConcurrentHashMap<>();

    public QuestManager(MSQuestsPlugin plugin) {
        super(plugin);
        questDefinitionRepository = new QuestDefinitionRepository(plugin.getDbAccess());
        actorRepository = new ActorRepository(plugin.getDbAccess());
        questRepository = new QuestRepository(plugin.getDbAccess());

        objectiveTypeRegistry = new ObjectiveDefinitionTypeRegistry();
        registerObjectiveTypes();
        questDefinitionMapper = new QuestDefinitionMapper(objectiveTypeRegistry);
    }

    private void registerObjectiveTypes() {
        objectiveTypeRegistry.registerType(BlockBreakObjectiveDefinition.class);
        // Ajouter d'autres types d'objectifs ici
    }

    public void loadQuestDefinitions() {
        Map<UUID, QuestDefinitionDTO> questDefinitionDtos = questDefinitionRepository.getAll();

        for (QuestDefinitionDTO questDefinitionDTO : questDefinitionDtos.values()) {
            QuestDefinition questDefinition = questDefinitionMapper.toEntity(questDefinitionDTO);
            questDefinitions.put(questDefinition.getId(), questDefinition);
        }
    }

    public void loadActor(String type, String referenceId) {
        QuestActorDTO actorDTO = actorRepository.getByReferenceId(type, referenceId);

        if (actorDTO == null) {
            actorDTO = new QuestActorDTO(type, UUID.randomUUID(), referenceId);
            actorRepository.add(actorDTO);
        }

        QuestActor actor = plugin.getActorRegistry().createActor(actorDTO);
        actors.put(actor.getId(), actor);

        loadQuests(actor);
    }

    private void loadQuests(QuestActor actor) {
        Map<UUID, QuestDTO> questEntryDtos = questRepository.getAllByActor(actor.getId());

        for (QuestDTO questEntryDTO : questEntryDtos.values()) {
            QuestDefinition questDefinition = questDefinitions.get(questEntryDTO.questId());
            Quest quest = QuestEntryMapper.toEntity(questEntryDTO, actor, questDefinition);
            quests.put(quest.getId(), quest);
        }
    }
}
