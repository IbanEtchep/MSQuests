package com.github.ibanetchep.msquests.bukkit.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The marks of a loaded chunk live in memory and are written through to the chunk's
 * container, so a break reads a set instead of decoding the whole array, and an unload
 * loses nothing.
 */
class BlockPlaceTagListenerTest {

    private ServerMock server;
    private PlayerMock player;
    private WorldMock world;
    private BlockPlaceTagListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        world = server.addSimpleWorld("world");
        Plugin plugin = mock(Plugin.class);
        when(plugin.getName()).thenReturn("MSQuests");
        listener = new BlockPlaceTagListener(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void aPlacedBlockIsRememberedAcrossAnUnloadAndForgottenWhenBroken() {
        Block block = world.getBlockAt(5, 64, 7);
        block.setType(Material.STONE);
        Block other = world.getBlockAt(6, 64, 7);

        listener.onBlockPlace(new BlockPlaceEvent(block, block.getState(), other, new ItemStack(Material.STONE), player, true, EquipmentSlot.HAND));
        assertTrue(listener.isPlaced(block));
        assertFalse(listener.isPlaced(other));
        assertEquals(1, listener.cachedChunks());
        long[] stored = block.getChunk().getPersistentDataContainer().get(listener.getPlacedKey(), PersistentDataType.LONG_ARRAY);
        assertNotNull(stored, "written through to the chunk");
        assertEquals(1, stored.length);

        listener.onChunkUnload(new ChunkUnloadEvent(block.getChunk(), true));
        assertEquals(0, listener.cachedChunks());
        assertTrue(listener.isPlaced(block), "re-read from the chunk after the unload");
        assertEquals(1, listener.cachedChunks());

        listener.onBlockBreak(new BlockBreakEvent(block, player));
        assertFalse(listener.isPlaced(block));
        assertNull(block.getChunk().getPersistentDataContainer().get(listener.getPlacedKey(), PersistentDataType.LONG_ARRAY),
                "an empty set removes the key");
    }
}
