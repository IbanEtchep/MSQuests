package com.github.ibanetchep.msquests.bukkit.listener;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class BlockPlaceTagListener implements Listener {

    private final NamespacedKey placedKey;

    public BlockPlaceTagListener(Plugin plugin) {
        this.placedKey = new NamespacedKey(plugin, "placed_blocks");
    }

    public NamespacedKey getPlacedKey() {
        return placedKey;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        markPlaced(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        unmarkPlaced(event.getBlock());
    }

    public boolean isPlaced(Block block) {
        long key = encodePos(block.getX(), block.getY(), block.getZ());
        Set<Long> placed = loadSet(block.getChunk());
        return placed.contains(key);
    }

    private void markPlaced(Block block) {
        Chunk chunk = block.getChunk();
        Set<Long> placed = loadSet(chunk);
        placed.add(encodePos(block.getX(), block.getY(), block.getZ()));
        saveSet(chunk, placed);
    }

    private void unmarkPlaced(Block block) {
        Chunk chunk = block.getChunk();
        Set<Long> placed = loadSet(chunk);
        if (placed.remove(encodePos(block.getX(), block.getY(), block.getZ()))) {
            saveSet(chunk, placed);
        }
    }

    private Set<Long> loadSet(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        long[] array = pdc.get(placedKey, PersistentDataType.LONG_ARRAY);
        if (array == null) return new HashSet<>();
        Set<Long> set = new HashSet<>(array.length);
        for (long l : array) set.add(l);
        return set;
    }

    private void saveSet(Chunk chunk, Set<Long> set) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        if (set.isEmpty()) {
            pdc.remove(placedKey);
        } else {
            pdc.set(placedKey, PersistentDataType.LONG_ARRAY, set.stream().mapToLong(Long::longValue).toArray());
        }
    }

    private static long encodePos(int x, int y, int z) {
        return ((long) x & 0x1FFFFF) << 43 | ((long) z & 0x1FFFFF) << 22 | ((long) y & 0x3FFFFF);
    }
}
