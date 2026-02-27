package com.github.ibanetchep.msquests.bukkit.quest.objective.fishing;

import com.github.ibanetchep.msquests.bukkit.quest.objective.AbstractObjectiveHandlerTest;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

public class FishingObjectiveHandlerTest extends AbstractObjectiveHandlerTest {

    private FishingObjectiveHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FishingObjectiveHandler(plugin);
    }

    private PlayerFishEvent mockFishEvent(Material caughtMaterial, PlayerFishEvent.State state) {
        ItemStack itemStack = mock(ItemStack.class);
        when(itemStack.getType()).thenReturn(caughtMaterial);

        Item caughtItem = mock(Item.class);
        when(caughtItem.getItemStack()).thenReturn(itemStack);

        PlayerFishEvent event = mock(PlayerFishEvent.class);
        when(event.getState()).thenReturn(state);
        when(event.getCaught()).thenReturn(caughtItem);
        when(event.getPlayer()).thenReturn(player);

        return event;
    }

    private PlayerFishEvent mockFishEvent(Material caughtMaterial) {
        return mockFishEvent(caughtMaterial, PlayerFishEvent.State.CAUGHT_FISH);
    }

    private FishingObjective createCodObjective() {
        return createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_cod", "fishing",
                    Map.of("fish_type", "COD", "amount", 5)
            );
            return new FishingObjective(stage, new FishingObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });
    }

    @Test
    void catchingCorrectFishTypeProgressesObjective() {
        FishingObjective objective = createCodObjective();

        handler.onPlayerFish(mockFishEvent(Material.COD));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }

    @Test
    void catchingWrongFishTypeDoesNotProgress() {
        createCodObjective();

        handler.onPlayerFish(mockFishEvent(Material.SALMON));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void nullFishTypeObjectiveMatchesAnyFish() {
        FishingObjective objective = createObjective(stage -> {
            // No fish_type → getFishType() returns null → matches any catch
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_any_fish", "fishing",
                    Map.of("amount", 3)
            );
            return new FishingObjective(stage, new FishingObjectiveConfig(dto), 0, QuestObjectiveStatus.IN_PROGRESS);
        });

        handler.onPlayerFish(mockFishEvent(Material.SALMON));

        verify(questProgressService).progressObjective(objective, 1, profile);
    }

    @Test
    void nonCaughtFishStateDoesNotProgress() {
        createCodObjective();

        handler.onPlayerFish(mockFishEvent(Material.COD, PlayerFishEvent.State.FISHING));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void caughtEntityIsNotItemDoesNotProgress() {
        createCodObjective();

        PlayerFishEvent event = mock(PlayerFishEvent.class);
        when(event.getState()).thenReturn(PlayerFishEvent.State.CAUGHT_FISH);
        when(event.getCaught()).thenReturn(null); // not an Item
        when(event.getPlayer()).thenReturn(player);

        handler.onPlayerFish(event);

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }

    @Test
    void completedObjectiveIsIgnored() {
        createObjective(stage -> {
            QuestObjectiveConfigDTO dto = new QuestObjectiveConfigDTO(
                    "obj_cod", "fishing",
                    Map.of("fish_type", "COD", "amount", 1)
            );
            return new FishingObjective(stage, new FishingObjectiveConfig(dto), 1, QuestObjectiveStatus.COMPLETED);
        });

        handler.onPlayerFish(mockFishEvent(Material.COD));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }
}
