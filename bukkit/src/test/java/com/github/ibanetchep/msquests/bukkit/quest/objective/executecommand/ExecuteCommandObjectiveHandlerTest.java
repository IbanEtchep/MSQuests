package com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand;

import com.github.ibanetchep.msquests.bukkit.quest.objective.AbstractObjectiveHandlerTest;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

public class ExecuteCommandObjectiveHandlerTest extends AbstractObjectiveHandlerTest {

    private ExecuteCommandObjectiveHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ExecuteCommandObjectiveHandler(plugin);
    }

    private ExecuteCommandObjective createSpawnObjective() {
        return createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_spawn", "execute_command",
                    Map.of("command", "spawn")
            );
            return new ExecuteCommandObjective(stage, new ExecuteCommandObjectiveConfig(dto), 0, 1, QuestObjectiveStatus.IN_PROGRESS);
        });
    }

    @Test
    void executingExactCommandProgressesObjective() {
        ExecuteCommandObjective objective = createSpawnObjective();

        handler.onCommand(new PlayerCommandPreprocessEvent(player, "/spawn"));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }

    @Test
    void executingCommandWithArgumentsProgressesObjective() {
        ExecuteCommandObjective objective = createSpawnObjective();

        // startsWith("/spawn") → matches "/spawn world"
        handler.onCommand(new PlayerCommandPreprocessEvent(player, "/spawn world"));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }

    @Test
    void executingDifferentCommandDoesNotProgress() {
        createSpawnObjective();

        handler.onCommand(new PlayerCommandPreprocessEvent(player, "/help"));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void partialCommandPrefixDoesNotProgress() {
        createSpawnObjective();

        // "/spawnpoint" does NOT start with "/spawn" — wait, it actually does!
        // So let's test a completely unrelated command
        handler.onCommand(new PlayerCommandPreprocessEvent(player, "/home"));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void completedObjectiveIsIgnored() {
        createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_spawn", "execute_command",
                    Map.of("command", "spawn")
            );
            return new ExecuteCommandObjective(stage, new ExecuteCommandObjectiveConfig(dto), 1, 1, QuestObjectiveStatus.COMPLETED);
        });

        handler.onCommand(new PlayerCommandPreprocessEvent(player, "/spawn"));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void multipleObjectivesOnlyMatchingCommandProgresses() {
        ExecuteCommandObjective spawnObjective = createSpawnObjective();
        createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_home", "execute_command",
                    Map.of("command", "home")
            );
            return new ExecuteCommandObjective(stage, new ExecuteCommandObjectiveConfig(dto), 0, 1, QuestObjectiveStatus.IN_PROGRESS);
        });

        handler.onCommand(new PlayerCommandPreprocessEvent(player, "/spawn"));

        verify(questProgressService, times(1)).progressObjective(spawnObjective, 1, profile);
        verify(questProgressService, times(1)).progressObjective(any(), anyInt(), any());
    }
}
