package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.bukkit.quest.objective.AbstractObjectiveHandlerTest;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

public class BlockBreakObjectiveHandlerTest extends AbstractObjectiveHandlerTest {

    private BlockBreakObjectiveHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BlockBreakObjectiveHandler(plugin);
    }

    private BlockBreakObjective createStoneObjective(int amount) {
        return createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_stone", "block_break",
                    Map.of("material", "STONE", "amount", amount)
            );
            return new BlockBreakObjective(stage, new BlockBreakObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });
    }

    @Test
    void breakingCorrectMaterialProgressesObjective() {
        BlockBreakObjective objective = createStoneObjective(10);

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.STONE);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }

    @Test
    void breakingWrongMaterialDoesNotProgress() {
        createStoneObjective(10);

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.DIRT);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void completedObjectiveIsIgnored() {
        createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_stone", "block_break",
                    Map.of("material", "STONE", "amount", 1)
            );
            // Progress equals target (1) → already completed
            return new BlockBreakObjective(stage, new BlockBreakObjectiveConfig(dto), 1, QuestObjectiveStatus.COMPLETED);
        });

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.STONE);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void nullMaterialConfigMatchesAnyBlock() {
        BlockBreakObjective objective = createObjective(stage -> {
            // No "material" param → getMaterial() returns null
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_any", "block_break",
                    Map.of("amount", 5)
            );
            return new BlockBreakObjective(stage, new BlockBreakObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.GRASS_BLOCK);

        // null == GRASS_BLOCK is false, so no progress expected
        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void playerWithNoActiveQuestDoesNotProgress() {
        // profile is registered but has no actor/quest attached
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.STONE);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }
}
