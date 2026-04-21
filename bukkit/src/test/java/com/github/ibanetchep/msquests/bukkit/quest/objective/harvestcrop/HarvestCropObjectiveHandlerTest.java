package com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop;

import com.github.ibanetchep.msquests.bukkit.quest.objective.AbstractObjectiveHandlerTest;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

public class HarvestCropObjectiveHandlerTest extends AbstractObjectiveHandlerTest {

    private HarvestCropObjectiveHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HarvestCropObjectiveHandler(plugin);
    }

    private HarvestCropObjective createWheatObjective(int amount) {
        return createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_wheat", "harvest_crop",
                    Map.of("crop", "WHEAT", "amount", amount)
            );
            return new HarvestCropObjective(stage, new HarvestCropObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });
    }

    @Test
    void harvestingFullyGrownWheatProgressesObjective() {
        HarvestCropObjective objective = createWheatObjective(10);

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.WHEAT);

        Ageable ageable = mock(Ageable.class);
        when(ageable.getAge()).thenReturn(7);
        when(ageable.getMaximumAge()).thenReturn(7);
        when(block.getBlockData()).thenReturn(ageable);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }

    @Test
    void harvestingImmatureWheatDoesNotProgress() {
        createWheatObjective(10);

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.WHEAT);

        Ageable ageable = mock(Ageable.class);
        when(ageable.getAge()).thenReturn(3);
        when(ageable.getMaximumAge()).thenReturn(7);
        when(block.getBlockData()).thenReturn(ageable);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void harvestingMelonDoesNotRequireAgeCheck() {
        HarvestCropObjective objective = createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_melon", "harvest_crop",
                    Map.of("crop", "MELON", "amount", 5)
            );
            return new HarvestCropObjective(stage, new HarvestCropObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.MELON);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }

    @Test
    void harvestingPumpkinDoesNotRequireAgeCheck() {
        HarvestCropObjective objective = createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_pumpkin", "harvest_crop",
                    Map.of("crop", "PUMPKIN", "amount", 5)
            );
            return new HarvestCropObjective(stage, new HarvestCropObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.PUMPKIN);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }

    @Test
    void wrongCropTypeDoesNotProgress() {
        createWheatObjective(10);

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.CARROTS);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void harvestingPlayerPlacedMatureCropProgresses() {
        HarvestCropObjective objective = createWheatObjective(10);

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.WHEAT);

        Ageable ageable = mock(Ageable.class);
        when(ageable.getAge()).thenReturn(7);
        when(ageable.getMaximumAge()).thenReturn(7);
        when(block.getBlockData()).thenReturn(ageable);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }
}
