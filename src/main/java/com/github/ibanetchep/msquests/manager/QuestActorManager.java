package com.github.ibanetchep.msquests.manager;

import com.github.ibanetchep.msquests.MSQuestsPlugin;
import com.github.ibanetchep.msquests.database.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.database.repository.ActorRepository;
import com.github.ibanetchep.msquests.model.actor.QuestActor;
import com.github.ibanetchep.msquests.strategy.ActorStrategy;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class QuestActorManager extends AbstractManager<UUID, QuestActor> {

    private final ActorRepository actorRepository;
    private final Map<String, Class<? extends QuestActor>> actorTypes = new ConcurrentHashMap<>();
    private final Map<Class<? extends QuestActor>, ActorStrategy> actorStrategies = new ConcurrentHashMap<>();

    public QuestActorManager(MSQuestsPlugin plugin, ActorRepository actorRepository) {
        super(plugin);
        this.actorRepository = actorRepository;
    }

    public List<QuestActor> getPlayerActors(Player player) {
        return getAll().values().stream()
                .filter(actor -> actor.isActor(player))
                .toList();
    }

    @Override
    public QuestActor loadFromDatabase(UUID uniqueId) {
        QuestActorDTO actorDTO = actorRepository.get(uniqueId);

        if(actorDTO == null) {
            return null;
        }

        return plugin.getActorRegistry().createActor(actorDTO);
    }


    public QuestActor loadOrCreateByReferenceId(String type, String referenceId) {
        QuestActorDTO actorDTO = actorRepository.getByReferenceId(type, referenceId);

        if (actorDTO == null) {
            actorDTO = new QuestActorDTO(type, UUID.randomUUID(), referenceId);
            actorRepository.add(actorDTO);
        }

        return plugin.getActorRegistry().createActor(actorDTO);
    }

    @Override
    public Map<UUID, QuestActor> loadAllFromDatabase() {
        Map<UUID, QuestActorDTO> actorDTOs = actorRepository.getAll();

        return actorDTOs.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> plugin.getActorRegistry().createActor(entry.getValue())
                ));
    }

    @Override
    public void deleteFromDatabase(UUID uniqueId) {

    }

    @Override
    public void saveToDatabase(QuestActor actor) {

    }

    @Override
    public void sync(QuestActor actor) {

    }
}
