package com.github.ibanetchep.msquests.bukkit.listener;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers which blocks players placed, so breaking them does not count for an objective.
 * The marks persist in each chunk's data container as one {@code long[]}; decoding that
 * array into a set on every break would cost O(placed blocks of the chunk) per event, so the
 * set of each loaded chunk is kept in memory, written through on change and dropped when
 * the chunk unloads.
 */
public class BlockPlaceTagListener implements Listener {

    private final NamespacedKey placedKey;
    private final Map<ChunkRef, Set<Long>> placedByChunk = new ConcurrentHashMap<>();

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        transferMarksOnPiston(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        transferMarksOnPiston(event.getBlocks(), event.getDirection().getOppositeFace());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        // The container already holds every change (write-through); the next load re-reads it.
        placedByChunk.remove(ChunkRef.of(event.getChunk()));
    }

    private void transferMarksOnPiston(List<Block> blocks, BlockFace direction) {
        List<Block> playerPlaced = blocks.stream().filter(this::isPlaced).toList();
        playerPlaced.forEach(this::unmarkPlaced);
        playerPlaced.forEach(b -> markPlaced(b.getRelative(direction)));
    }

    public boolean isPlaced(Block block) {
        long key = encodePos(block.getX(), block.getY(), block.getZ());
        return placedIn(block.getChunk()).contains(key);
    }

    private void markPlaced(Block block) {
        Chunk chunk = block.getChunk();
        Set<Long> placed = placedIn(chunk);
        if (placed.add(encodePos(block.getX(), block.getY(), block.getZ()))) {
            saveSet(chunk, placed);
        }
    }

    private void unmarkPlaced(Block block) {
        Chunk chunk = block.getChunk();
        Set<Long> placed = placedIn(chunk);
        if (placed.remove(encodePos(block.getX(), block.getY(), block.getZ()))) {
            saveSet(chunk, placed);
        }
    }

    /** The chunk's set, decoded from its container the first time it is asked for. */
    private Set<Long> placedIn(Chunk chunk) {
        return placedByChunk.computeIfAbsent(ChunkRef.of(chunk), ref -> loadSet(chunk));
    }

    private Set<Long> loadSet(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        long[] array = pdc.get(placedKey, PersistentDataType.LONG_ARRAY);
        Set<Long> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
        if (array == null) return set;
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

    /** How many chunks currently have their marks in memory. */
    int cachedChunks() {
        return placedByChunk.size();
    }

    private static long encodePos(int x, int y, int z) {
        return ((long) x & 0x1FFFFF) << 43 | ((long) z & 0x1FFFFF) << 22 | ((long) y & 0x3FFFFF);
    }

    private record ChunkRef(UUID world, long key) {
        static ChunkRef of(Chunk chunk) {
            return new ChunkRef(chunk.getWorld().getUID(), chunk.getChunkKey());
        }
    }
}
