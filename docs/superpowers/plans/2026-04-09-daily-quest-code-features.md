# Daily Quest Code Features — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add anti-place-break PDC protection, harvest_crop objective type, and all_quests_complete group action hook.

**Architecture:** Three independent features added to the existing quest plugin. Anti-place-break uses a BlockPlaceEvent listener + PDC tag checked in BlockBreakObjectiveHandler. harvest_crop follows the existing objective pattern (config/objective/handler). all_quests_complete adds a new action list to QuestGroupConfig, triggered when all group quests for an actor are completed.

**Tech Stack:** Java 21, Paper 1.21+ API, MockBukkit 4.41.0, Mockito 5.14.2, JUnit 5

---

## File Map

### Feature A: Anti-Place-Break
- **Create:** `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/listener/BlockPlaceTagListener.java`
- **Modify:** `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/blockbreak/BlockBreakObjectiveHandler.java`
- **Modify:** `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/BukkitQuestsPlugin.java` (register listener)
- **Create:** `bukkit/src/test/java/com/github/ibanetchep/msquests/bukkit/quest/objective/blockbreak/BlockBreakAntiPlaceTest.java`

### Feature B: harvest_crop Objective
- **Create:** `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjectiveConfig.java`
- **Create:** `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjective.java`
- **Create:** `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjectiveHandler.java`
- **Modify:** `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/BukkitQuestsPlugin.java` (register objective type)
- **Create:** `bukkit/src/test/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjectiveHandlerTest.java`

### Feature C: all_quests_complete Hook
- **Modify:** `core/src/main/java/com/github/ibanetchep/msquests/core/dto/QuestGroupConfigActionsDTO.java`
- **Modify:** `core/src/main/java/com/github/ibanetchep/msquests/core/quest/config/group/QuestGroupConfig.java`
- **Modify:** `core/src/main/java/com/github/ibanetchep/msquests/core/mapper/QuestGroupMapper.java`
- **Modify:** `core/src/main/java/com/github/ibanetchep/msquests/core/service/QuestLifecycleService.java`
- **Create:** `core/src/test/java/com/github/ibanetchep/msquests/core/service/QuestLifecycleAllCompleteTest.java`

---

## Task 1: Anti-Place-Break — BlockPlaceTagListener

**Files:**
- Create: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/listener/BlockPlaceTagListener.java`

- [ ] **Step 1: Create the BlockPlaceTagListener**

```java
package com.github.ibanetchep.msquests.bukkit.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class BlockPlaceTagListener implements Listener {

    public static final NamespacedKey PLACED_KEY;

    static {
        // Initialized later via init()
        PLACED_KEY = null;
    }

    private final NamespacedKey placedKey;

    public BlockPlaceTagListener(Plugin plugin) {
        this.placedKey = new NamespacedKey(plugin, "placed");
    }

    public NamespacedKey getPlacedKey() {
        return placedKey;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getState() instanceof TileState tileState) {
            tileState.getPersistentDataContainer().set(placedKey, PersistentDataType.BYTE, (byte) 1);
            tileState.update();
        }
    }
}
```

**Important note:** PDC on blocks only works for TileState blocks (chests, signs, etc.). For regular blocks (stone, dirt, crops), Paper does NOT support block PDC. We need a different approach — use a chunk-level metadata set tracking placed block positions instead.

**Revised approach:** Use Paper's `Chunk` PDC to store a set of placed block coordinates within the chunk. This works for all block types.

Actually, let me reconsider. Paper 1.21+ supports `Block` PDC via `Chunk` custom data. The simplest reliable approach is to use a `Set<BlockPos>` stored in the chunk's PDC, or use Paper's `BlockState` if the block has one. However, the most practical approach for a Minecraft plugin is to use a **custom metadata approach via chunk PDC**.

**Final revised approach:** Track placed blocks using a `Set` stored in chunk PDC as a byte array of relative coordinates.

- [ ] **Step 1 (revised): Create BlockPlaceTagListener with chunk-based tracking**

```java
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

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks player-placed blocks via chunk PDC so that quest objectives
 * can ignore placed-then-broken blocks.
 *
 * Each placed block is stored as a 10-byte entry (x:int, y:int, z:short relative to chunk)
 * in the chunk's persistent data container.
 */
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

    /**
     * Encodes absolute block coordinates into a single long.
     * x (21 bits) | z (21 bits) | y (22 bits) — same layout Minecraft uses internally.
     */
    private static long encodePos(int x, int y, int z) {
        return ((long) x & 0x1FFFFF) << 43 | ((long) z & 0x1FFFFF) << 22 | ((long) y & 0x3FFFFF);
    }
}
```

- [ ] **Step 2: Compile check**

Run: `./gradlew :bukkit:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/listener/BlockPlaceTagListener.java
git commit -m "feat: add BlockPlaceTagListener for anti-place-break tracking"
```

---

## Task 2: Anti-Place-Break — Integrate into BlockBreakObjectiveHandler + Register

**Files:**
- Modify: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/blockbreak/BlockBreakObjectiveHandler.java`
- Modify: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/BukkitQuestsPlugin.java`

- [ ] **Step 1: Add BlockPlaceTagListener field and getter to BukkitQuestsPlugin**

In `BukkitQuestsPlugin`, add a field and register the listener during startup. Add a getter so handlers can access it.

```java
// Field
private BlockPlaceTagListener blockPlaceTagListener;

// In onEnable(), after plugin manager is available:
blockPlaceTagListener = new BlockPlaceTagListener(this);
getServer().getPluginManager().registerEvents(blockPlaceTagListener, this);

// Getter
public BlockPlaceTagListener getBlockPlaceTagListener() {
    return blockPlaceTagListener;
}
```

- [ ] **Step 2: Modify BlockBreakObjectiveHandler to check placed tag**

Replace `onBlockBreak` in `BlockBreakObjectiveHandler.java`:

```java
@EventHandler
public void onBlockBreak(BlockBreakEvent event) {
    if (plugin.getBlockPlaceTagListener().isPlaced(event.getBlock())) {
        return;
    }

    Player player = event.getPlayer();
    PlayerProfile profile = getPlayerProfile(player.getUniqueId());

    for (BlockBreakObjective objective : getEligibleObjectives(profile)) {
        if (event.getBlock().getType() == objective.getObjectiveConfig().getMaterial()) {
            plugin.getQuestProgressService().progressObjective(objective, 1, profile);
        }
    }
}
```

- [ ] **Step 3: Compile check**

Run: `./gradlew :bukkit:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/blockbreak/BlockBreakObjectiveHandler.java
git add bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/BukkitQuestsPlugin.java
git commit -m "feat: integrate anti-place-break check into BlockBreakObjectiveHandler"
```

---

## Task 3: Anti-Place-Break — Tests

**Files:**
- Create: `bukkit/src/test/java/com/github/ibanetchep/msquests/bukkit/quest/objective/blockbreak/BlockBreakAntiPlaceTest.java`

- [ ] **Step 1: Write tests for anti-place-break**

```java
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
```

- [ ] **Step 2: Run tests**

Run: `./gradlew :bukkit:test --tests "*.BlockBreakAntiPlaceTest"`
Expected: All tests PASS

Note: The existing `BlockBreakObjectiveHandlerTest` must also be updated to mock `blockPlaceTagListener` in its `setUp`. Add:
```java
@BeforeEach
void setUp() {
    BlockPlaceTagListener blockPlaceTagListener = mock(BlockPlaceTagListener.class);
    when(plugin.getBlockPlaceTagListener()).thenReturn(blockPlaceTagListener);
    when(blockPlaceTagListener.isPlaced(any())).thenReturn(false);
    handler = new BlockBreakObjectiveHandler(plugin);
}
```

- [ ] **Step 3: Run all existing BlockBreak tests to verify no regression**

Run: `./gradlew :bukkit:test --tests "*.BlockBreakObjectiveHandlerTest"`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add bukkit/src/test/java/com/github/ibanetchep/msquests/bukkit/quest/objective/blockbreak/BlockBreakAntiPlaceTest.java
git add bukkit/src/test/java/com/github/ibanetchep/msquests/bukkit/quest/objective/blockbreak/BlockBreakObjectiveHandlerTest.java
git commit -m "test: add anti-place-break tests and fix existing BlockBreak tests"
```

---

## Task 4: HarvestCrop — ObjectiveConfig

**Files:**
- Create: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjectiveConfig.java`

- [ ] **Step 1: Create HarvestCropObjectiveConfig**

```java
package com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import org.bukkit.Material;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.HARVEST_CROP)
public class HarvestCropObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "crop", required = true)
    private Material crop;

    @ConfigField(name = "amount", required = true)
    private int amount = 1;

    public HarvestCropObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
        if (dto.params().containsKey("crop")) {
            crop = Material.valueOf(dto.params().get("crop").toString().toUpperCase());
        }
        if (dto.params().containsKey("amount")) {
            amount = (int) dto.params().get("amount");
        }
    }

    public Material getCrop() {
        return crop;
    }

    public int getAmount() {
        return amount;
    }

    /**
     * Returns true if this crop type requires age checking (wheat, carrots, etc.).
     * Melon and pumpkin are fruit blocks, not ageable — no age check needed.
     */
    public boolean requiresAgeCheck() {
        return crop != Material.MELON && crop != Material.PUMPKIN;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "crop", "<lang:" + crop.translationKey() + ">",
                "amount", String.valueOf(amount)
        );
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of(
                        "crop", crop.name(),
                        "amount", amount
                )
        );
    }
}
```

- [ ] **Step 2: Compile check**

Run: `./gradlew :bukkit:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjectiveConfig.java
git commit -m "feat: add HarvestCropObjectiveConfig"
```

---

## Task 5: HarvestCrop — Objective class

**Files:**
- Create: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjective.java`

- [ ] **Step 1: Create HarvestCropObjective**

```java
package com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

public class HarvestCropObjective extends AbstractQuestObjective<HarvestCropObjectiveConfig> {

    public HarvestCropObjective(QuestStage questStage, HarvestCropObjectiveConfig objectiveConfig, int progress, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, objectiveConfig.getAmount(), status);
    }
}
```

- [ ] **Step 2: Compile check**

Run: `./gradlew :bukkit:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjective.java
git commit -m "feat: add HarvestCropObjective class"
```

---

## Task 6: HarvestCrop — Handler

**Files:**
- Create: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjectiveHandler.java`

- [ ] **Step 1: Create HarvestCropObjectiveHandler**

```java
package com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.BukkitQuestObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class HarvestCropObjectiveHandler extends BukkitQuestObjectiveHandler<HarvestCropObjective> implements Listener {

    public HarvestCropObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.HARVEST_CROP;
    }

    @Override
    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void destroy() {}

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Anti-place-break check
        if (plugin.getBlockPlaceTagListener().isPlaced(block)) {
            return;
        }

        Player player = event.getPlayer();
        PlayerProfile profile = getPlayerProfile(player.getUniqueId());

        for (HarvestCropObjective objective : getEligibleObjectives(profile)) {
            HarvestCropObjectiveConfig config = objective.getObjectiveConfig();

            if (block.getType() != config.getCrop()) {
                continue;
            }

            if (config.requiresAgeCheck()) {
                if (!(block.getBlockData() instanceof Ageable ageable)) {
                    continue;
                }
                if (ageable.getAge() != ageable.getMaximumAge()) {
                    continue;
                }
            }

            plugin.getQuestProgressService().progressObjective(objective, 1, profile);
        }
    }
}
```

- [ ] **Step 2: Register in BukkitQuestsPlugin.registerObjectiveTypes()**

Add this line in the `registerObjectiveTypes()` method:

```java
questObjectiveFactory.register(HarvestCropObjectiveConfig.class, HarvestCropObjective.class, new HarvestCropObjectiveHandler(this));
```

Add the imports:
```java
import com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop.HarvestCropObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop.HarvestCropObjectiveConfig;
import com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop.HarvestCropObjectiveHandler;
```

- [ ] **Step 3: Compile check**

Run: `./gradlew :bukkit:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjectiveHandler.java
git add bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/BukkitQuestsPlugin.java
git commit -m "feat: add HarvestCropObjectiveHandler and register objective type"
```

---

## Task 7: HarvestCrop — Tests

**Files:**
- Create: `bukkit/src/test/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjectiveHandlerTest.java`

- [ ] **Step 1: Write tests**

```java
package com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop;

import com.github.ibanetchep.msquests.bukkit.listener.BlockPlaceTagListener;
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
    private BlockPlaceTagListener blockPlaceTagListener;

    @BeforeEach
    void setUp() {
        blockPlaceTagListener = mock(BlockPlaceTagListener.class);
        when(plugin.getBlockPlaceTagListener()).thenReturn(blockPlaceTagListener);
        when(blockPlaceTagListener.isPlaced(any())).thenReturn(false);
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
        // No Ageable mock needed — melon skips age check

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
    void placedBlockDoesNotProgress() {
        createWheatObjective(10);

        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.WHEAT);
        when(blockPlaceTagListener.isPlaced(block)).thenReturn(true);

        handler.onBlockBreak(new BlockBreakEvent(block, player));

        verify(questProgressService, never()).progressObjective(any(), anyInt(), any());
    }
}
```

- [ ] **Step 2: Run tests**

Run: `./gradlew :bukkit:test --tests "*.HarvestCropObjectiveHandlerTest"`
Expected: All 6 tests PASS

- [ ] **Step 3: Commit**

```bash
git add bukkit/src/test/java/com/github/ibanetchep/msquests/bukkit/quest/objective/harvestcrop/HarvestCropObjectiveHandlerTest.java
git commit -m "test: add HarvestCropObjectiveHandler tests"
```

---

## Task 8: all_quests_complete — DTO + QuestGroupConfig

**Files:**
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/dto/QuestGroupConfigActionsDTO.java`
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/quest/config/group/QuestGroupConfig.java`

- [ ] **Step 1: Add allQuestsComplete to QuestGroupConfigActionsDTO**

Replace the record in `QuestGroupConfigActionsDTO.java`:

```java
package com.github.ibanetchep.msquests.core.dto;

import java.util.List;

public record QuestGroupConfigActionsDTO(
        List<QuestActionDTO> questStart,
        List<QuestActionDTO> questComplete,
        List<QuestActionDTO> objectiveProgress,
        List<QuestActionDTO> objectiveComplete,
        List<QuestActionDTO> questDistribution,
        List<QuestActionDTO> actorLoad,
        List<QuestActionDTO> allQuestsComplete
) {
}
```

- [ ] **Step 2: Add allQuestsCompleteActions to QuestGroupConfig**

In `QuestGroupConfig.java`, add the field, builder method, and getter following the existing pattern:

Field (after `actorLoadActions`):
```java
private final List<QuestAction> allQuestsCompleteActions;
```

In constructor (after `this.actorLoadActions = builder.actorLoadActions;`):
```java
this.allQuestsCompleteActions = builder.allQuestsCompleteActions;
```

Getter (after `getActorLoadActions()`):
```java
public List<QuestAction> getAllQuestsCompleteActions() {
    return Collections.unmodifiableList(allQuestsCompleteActions);
}
```

In Builder, field (after `actorLoadActions`):
```java
private List<QuestAction> allQuestsCompleteActions;
```

In Builder constructor (after `this.actorLoadActions = new ArrayList<>();`):
```java
this.allQuestsCompleteActions = new ArrayList<>();
```

Builder method (after `actorLoadActions` method):
```java
public Builder allQuestsCompleteActions(List<QuestAction> allQuestsCompleteActions) {
    this.allQuestsCompleteActions = allQuestsCompleteActions;
    return this;
}
```

- [ ] **Step 3: Compile check**

Run: `./gradlew :core:compileJava`
Expected: FAIL — `QuestGroupMapper` and other callers need updating (expected, handled in next task)

- [ ] **Step 4: Commit**

```bash
git add core/src/main/java/com/github/ibanetchep/msquests/core/dto/QuestGroupConfigActionsDTO.java
git add core/src/main/java/com/github/ibanetchep/msquests/core/quest/config/group/QuestGroupConfig.java
git commit -m "feat: add allQuestsComplete field to QuestGroupConfigActionsDTO and QuestGroupConfig"
```

---

## Task 9: all_quests_complete — Mapper

**Files:**
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/mapper/QuestGroupMapper.java`

- [ ] **Step 1: Update toDTO() in QuestGroupMapper**

In the `toDTO()` method, update the `QuestGroupConfigActionsDTO` constructor call to include the new field. Change line 69-76 to:

```java
new QuestGroupConfigActionsDTO(
        entity.getQuestStartActions().stream().map(QuestAction::toDTO).toList(),
        entity.getQuestCompleteActions().stream().map(QuestAction::toDTO).toList(),
        entity.getObjectiveProgressActions().stream().map(QuestAction::toDTO).toList(),
        entity.getObjectiveCompleteActions().stream().map(QuestAction::toDTO).toList(),
        entity.getQuestDistributionActions().stream().map(QuestAction::toDTO).toList(),
        entity.getActorLoadActions().stream().map(QuestAction::toDTO).toList(),
        entity.getAllQuestsCompleteActions().stream().map(QuestAction::toDTO).toList()
),
```

- [ ] **Step 2: Update toEntity() in QuestGroupMapper**

After the `actorLoadActions` mapping block (line 115-116), add:

```java
List<QuestAction> allQuestsCompleteActions = dto.actions().allQuestsComplete() != null
        ? dto.actions().allQuestsComplete().stream().map(questActionFactory::createAction).toList()
        : List.of();
```

In the builder chain, after `.actorLoadActions(actorLoadActions)`, add:

```java
.allQuestsCompleteActions(allQuestsCompleteActions)
```

- [ ] **Step 3: Compile check**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL (or minor fixes in test files referencing the DTO — the record constructor signature changed)

- [ ] **Step 4: Fix any test compilation issues**

The `QuestGroupConfigActionsDTO` record now has 7 parameters. Find and fix any tests or code that construct it with 6 parameters. Search for `QuestGroupConfigActionsDTO(` across the codebase and add the 7th `List.of()` parameter.

- [ ] **Step 5: Compile and run all tests**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/com/github/ibanetchep/msquests/core/mapper/QuestGroupMapper.java
git commit -m "feat: map allQuestsComplete actions in QuestGroupMapper"
```

---

## Task 10: all_quests_complete — Trigger in QuestLifecycleService

**Files:**
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/service/QuestLifecycleService.java`

- [ ] **Step 1: Add all-quests-complete check in completeObjective()**

In `QuestLifecycleService.completeObjective()`, inside the `if (quest.shouldComplete())` block (after line 125 `triggerDistribution(...)`), add the all-quests-complete check:

```java
if (quest.shouldComplete()) {
    var questCompleteEvent = new CoreQuestCompletedEvent(quest);
    dispatcher.dispatch(questCompleteEvent);
    quest.setStatus(QuestStatus.COMPLETED);

    groupConfig.getQuestCompleteActions().forEach(a -> a.execute(quest));

    if (groupConfig.hasDistributionTrigger(DistributionTrigger.QUEST_COMPLETE)) {
        triggerDistribution(quest.getActor(), groupConfig);
    }

    // Check if all quests in the group are completed
    checkAllQuestsComplete(quest.getActor(), groupConfig);
}
```

- [ ] **Step 2: Add the checkAllQuestsComplete method**

Add this private method to `QuestLifecycleService`:

```java
private void checkAllQuestsComplete(QuestActor actor, QuestGroupConfig groupConfig) {
    List<QuestAction> actions = groupConfig.getAllQuestsCompleteActions();
    if (actions.isEmpty()) {
        return;
    }

    ActorQuestGroup actorGroup = actor.getActorQuestGroup(groupConfig);
    if (actorGroup == null) {
        return;
    }

    // All quests complete = none still active (in progress) AND at least one completed
    // This covers the case where distribution gave N quests and all N are now done.
    // We check inProgress == 0 (no active quests left) and completedCount > 0
    // (at least some quests were actually done, not just an empty group).
    boolean allCompleted = actorGroup.getInProgressCount() == 0
            && actorGroup.getCompletedCount() > 0;

    if (allCompleted) {
        actions.forEach(a -> a.execute(actor, groupConfig));
    }
}
```

Add the import:
```java
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
```

- [ ] **Step 3: Compile check**

Run: `./gradlew :core:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add core/src/main/java/com/github/ibanetchep/msquests/core/service/QuestLifecycleService.java
git commit -m "feat: trigger all_quests_complete actions when all group quests are done"
```

---

## Task 11: all_quests_complete — Tests

**Files:**
- Create: `core/src/test/java/com/github/ibanetchep/msquests/core/service/QuestLifecycleAllCompleteTest.java`

- [ ] **Step 1: Write tests for the all_quests_complete hook**

This test needs to verify that when the last quest in a group completes, the `allQuestsCompleteActions` are executed. The test will need to mock the dependencies of `QuestLifecycleService` and set up a scenario with multiple quests in a group.

```java
package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.quest.actor.*;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicQuestExecutor;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.repository.RotationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

public class QuestLifecycleAllCompleteTest {

    private QuestLifecycleService lifecycleService;
    private EventDispatcher dispatcher;
    private QuestService persistenceService;
    private QuestAction allCompleteAction;

    @BeforeEach
    void setUp() {
        dispatcher = mock(EventDispatcher.class);
        persistenceService = mock(QuestService.class);
        when(persistenceService.saveQuest(any())).thenReturn(CompletableFuture.completedFuture(null));

        QuestFactory questFactory = mock(QuestFactory.class);
        QuestRegistry questRegistry = mock(QuestRegistry.class);
        QuestConfigRegistry questConfigRegistry = mock(QuestConfigRegistry.class);
        QuestDistributionService distributionManager = mock(QuestDistributionService.class);
        RotationRepository rotationRepository = mock(RotationRepository.class);

        // AtomicQuestExecutor that runs the action immediately
        AtomicQuestExecutor executor = mock(AtomicQuestExecutor.class);
        when(executor.execute(any(UUID.class), any())).thenAnswer(invocation -> {
            UUID questId = invocation.getArgument(0);
            AtomicQuestExecutor.QuestConsumer consumer = invocation.getArgument(1);
            // We need to find the quest from the consumer — will be set up in test
            return CompletableFuture.completedFuture(null);
        });

        allCompleteAction = mock(QuestAction.class);

        lifecycleService = new QuestLifecycleService(
                dispatcher, persistenceService, questFactory, questRegistry,
                questConfigRegistry, executor, distributionManager, rotationRepository
        );
    }

    // Note: Due to the AtomicQuestExecutor's lambda-based approach, direct unit testing
    // of the internal flow requires either:
    // 1. Making checkAllQuestsComplete package-private for testing
    // 2. Integration-style test that wires up a real AtomicQuestExecutor
    //
    // The implementation agent should determine the best approach based on
    // how AtomicQuestExecutor.execute() works (it wraps the consumer in a
    // synchronized block keyed by quest ID).
    //
    // A practical approach: test via completeObjective() with a real or spy executor
    // that delegates to the actual quest, verifying the action was called.

    @Test
    void allQuestsCompleteActionFiredWhenLastQuestCompletes() {
        // This test structure depends on AtomicQuestExecutor implementation.
        // The implementing agent should adapt this based on the actual executor pattern.
        // Key assertion: verify(allCompleteAction).execute(actor, groupConfig)
        // is called when the last quest transitions to COMPLETED.
    }

    @Test
    void allQuestsCompleteActionNotFiredWhenQuestsStillInProgress() {
        // Key assertion: verify(allCompleteAction, never()).execute(any(), any())
        // when there are still IN_PROGRESS quests in the group.
    }
}
```

**Note to implementing agent:** The test skeleton above needs to be adapted based on how `AtomicQuestExecutor` works. The key logic to test is:
1. When the last quest in a group completes → `allQuestsCompleteActions` are executed
2. When quests are still in progress → actions are NOT executed
3. When no `allQuestsComplete` actions are configured → no error

The simplest approach may be to test `checkAllQuestsComplete` directly by making it package-private, or to use a real `AtomicQuestExecutor` that delegates synchronously in tests.

- [ ] **Step 2: Run tests**

Run: `./gradlew :core:test --tests "*.QuestLifecycleAllCompleteTest"`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add core/src/test/java/com/github/ibanetchep/msquests/core/service/QuestLifecycleAllCompleteTest.java
git commit -m "test: add all_quests_complete hook tests"
```

---

## Task 12: Final validation

- [ ] **Step 1: Run full build**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Verify all tests pass**

Run: `./gradlew test`
Expected: All tests pass, including existing tests (no regression)

- [ ] **Step 3: Final commit if any fixes needed**

If any fixes were made during final validation, commit them.
