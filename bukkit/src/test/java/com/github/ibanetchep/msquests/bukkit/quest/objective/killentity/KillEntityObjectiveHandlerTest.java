package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.bukkit.quest.objective.AbstractObjectiveHandlerTest;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class KillEntityObjectiveHandlerTest extends AbstractObjectiveHandlerTest {

    private KillEntityObjectiveHandler handler;

    @BeforeEach
    void setUp() {
        handler = new KillEntityObjectiveHandler(plugin);
    }

    private KillEntityObjective createZombieObjective(int amount) {
        return createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_zombie", "kill_entity",
                    Map.of("entity_type", "ZOMBIE", "amount", amount)
            );
            return new KillEntityObjective(stage, new KillEntityObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });
    }

    private EntityDeathEvent mockDeathEvent(EntityType entityType, boolean killedByPlayer) {
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getType()).thenReturn(entityType);
        when(entity.getKiller()).thenReturn(killedByPlayer ? player : null);

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        when(event.getEntity()).thenReturn(entity);
        return event;
    }

    @Test
    void killingCorrectEntityProgressesObjective() {
        KillEntityObjective objective = createZombieObjective(5);

        handler.onKillEntity(mockDeathEvent(EntityType.ZOMBIE, true));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }

    @Test
    void killingWrongEntityTypeDoesNotProgress() {
        createZombieObjective(5);

        handler.onKillEntity(mockDeathEvent(EntityType.SKELETON, true));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void entityDiedWithoutPlayerKillerDoesNotProgress() {
        createZombieObjective(5);

        handler.onKillEntity(mockDeathEvent(EntityType.ZOMBIE, false));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void completedObjectiveIsIgnored() {
        createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_zombie", "kill_entity",
                    Map.of("entity_type", "ZOMBIE", "amount", 1)
            );
            return new KillEntityObjective(stage, new KillEntityObjectiveConfig(dto), 1, QuestObjectiveStatus.COMPLETED);
        });

        handler.onKillEntity(mockDeathEvent(EntityType.ZOMBIE, true));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void multipleObjectivesOnlyMatchingOnesProgress() {
        KillEntityObjective zombieObjective = createZombieObjective(3);
        createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_creeper", "kill_entity",
                    Map.of("entity_type", "CREEPER", "amount", 2)
            );
            return new KillEntityObjective(stage, new KillEntityObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });

        handler.onKillEntity(mockDeathEvent(EntityType.ZOMBIE, true));

        verify(questProgressService, times(1)).progressObjective(zombieObjective, 1, profile);
        verify(questProgressService, times(1)).progressObjective(any(), anyInt(), any());
    }
}
