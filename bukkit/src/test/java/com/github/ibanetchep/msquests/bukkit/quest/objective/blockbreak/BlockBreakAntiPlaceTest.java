package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.bukkit.listener.BlockPlaceTagListener;
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

public class BlockBreakAntiPlaceTest extends AbstractObjectiveHandlerTest {

    private BlockBreakObjectiveHandler handler;
    private BlockPlaceTagListener blockPlaceTagListener;

    @BeforeEach
    void setUp() {
        blockPlaceTagListener = mock(BlockPlaceTagListener.class);
        when(plugin.getBlockPlaceTagListener()).thenReturn(blockPlaceTagListener);
        handler = new BlockBreakObjectiveHandler(plugin);
    }

    @Test
    void placedBlockDoesNotProgressObjective() {
        createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_stone", "block_break",
                    Map.of("material", "STONE", "amount", 10)
            );
            return new BlockBreakObjective(stage, new BlockBreakObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.STONE);
        when(blockPlaceTagListener.isPlaced(block)).thenReturn(true);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void naturalBlockProgressesObjective() {
        BlockBreakObjective objective = createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_stone", "block_break",
                    Map.of("material", "STONE", "amount", 10)
            );
            return new BlockBreakObjective(stage, new BlockBreakObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.STONE);
        when(blockPlaceTagListener.isPlaced(block)).thenReturn(false);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }
}
