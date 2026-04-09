# Daily Quest Code Features — Design Spec

**Date:** 2026-04-09
**Scope:** Anti-place-break PDC, harvest_crop objective, all_period_quests_complete hook
**Depends on:** Nothing (prerequisite for daily quest rebalancing spec)

---

## Context

Daily quests need rebalancing: harder difficulties, better XP ratios vs passive actions, farming/woodcutting quests. Before rewriting the quest config, three code features are needed:

1. Anti-place-break protection to prevent players gaming block_break objectives
2. A new `harvest_crop` objective type for farming quests
3. An `all_period_quests_complete` group hook for bonus rewards

## A. Anti-Place-Break (PDC Tag)

### Problem

Players can place and break the same blocks repeatedly to complete `block_break` objectives without actually mining.

### Solution

Tag placed blocks with a PersistentDataContainer key so the `block_break` objective handler can ignore them.

### Design

- **BlockPlaceEvent listener** in the bukkit module:
  - On `BlockPlaceEvent`, tag the block with `NamespacedKey("msquests", "placed")`, type `PersistentDataType.BYTE`, value `1`
- **BlockBreakObjectiveHandler** modification:
  - Before counting a block break toward progress, check if the block has the `placed` tag
  - If tagged, skip the block (don't progress the objective)
  - The tag is naturally removed when the block is broken and re-placed by a different action (e.g., world generation)
- **Applies to:** all `block_break` objectives and the new `harvest_crop` objective

### Edge Cases

- Pistons moving blocks: PDC is preserved on piston-moved blocks in Paper, so the tag follows the block. This is the desired behavior.
- Blocks placed by non-players (dispensers, etc.): not tagged, so they count. This is acceptable.

## B. New Objective Type: `harvest_crop`

### Problem

No objective type exists for farming. `block_break` doesn't verify crop maturity, so players could replant and break immature crops.

### Design

**Objective config:**
```yaml
- key: harvest_wheat
  type: "harvest_crop"
  crop: "WHEAT"      # Material type of the crop block
  amount: 64
```

**Supported crops:**
- Ageable crops: `WHEAT`, `CARROTS`, `POTATOES`, `BEETROOTS`, `NETHER_WART`
- Fruit blocks: `MELON`, `PUMPKIN` (no age check — these are the fruit, not the stem)

**Handler logic:**
- Listens to `BlockBreakEvent`
- For ageable crops: checks `block.getBlockData() instanceof Ageable ageable` and `ageable.getAge() == ageable.getMaximumAge()`
- For melon/pumpkin: just checks material match (no age check)
- Anti-place-break PDC check applies here too
- Progresses objective by 1 per valid harvest

**Files to create (following existing objective pattern):**
1. `HarvestCropObjectiveConfig.java` — `@ObjectiveType(ObjectiveTypes.HARVEST_CROP)`, reads `crop` field from DTO
2. `HarvestCropObjective.java` — extends `AbstractQuestObjective<HarvestCropObjectiveConfig>`
3. `HarvestCropObjectiveHandler.java` — extends `BukkitQuestObjectiveHandler<HarvestCropObjective>`, listens to `BlockBreakEvent`
4. Add `HARVEST_CROP` constant to `ObjectiveTypes.java`
5. Register in `BukkitQuestsPlugin.registerObjectiveTypes()`

## C. Hook: `all_period_quests_complete`

### Problem

No way to reward players for completing all their daily quests. The group action system has hooks for individual quest events but not for "all quests in the group are done."

### Design

**YAML config:**
```yaml
actions:
  all_period_quests_complete:
    - type: message
      message: "<#FFD166>★ <white>Tu as complété toutes tes quêtes du jour ! Bravo !"
    - type: command
      command: "levels addxp %actor_name% 1000"
```

**Detection logic (in `QuestLifecycleService`):**
- After a quest is marked as completed, check: are all quests distributed to this actor in this group now completed?
- Query: get all quests for the actor in the group, check if all have status `COMPLETED`
- If yes, execute the `all_period_quests_complete` actions

**Code changes:**
1. `QuestGroupConfigActionsDTO` — add `allPeriodQuestsComplete` field
2. `QuestGroupConfigYamlRepository` (or mapper) — deserialize `all_period_quests_complete` from YAML
3. `QuestLifecycleService.completeQuest()` — after completing, check if all group quests are done, if so dispatch actions
4. `QuestGroupMapper` — map the new field

### Edge Cases

- Player completes last quest after reconnecting: the check happens at completion time, so this works normally
- Group with no `all_period_quests_complete` actions configured: the field is an empty list, nothing happens (consistent with other optional hooks)

## XP Balance Reference

This spec is a prerequisite for the daily quest rebalancing. For reference, the target XP structure:

| Tier | Quests/day | XP/quest | Total XP |
|------|-----------|---------|---------|
| Easy | 3 | 150 | 450 |
| Medium | 2 | 500 | 1000 |
| Hard | 1 | 1500 | 1500 |
| **Subtotal** | **6** | | **2950** |
| Bonus all complete | — | configurable | ~1000 |

Vs passive actions: ~500-1500 XP for 2-3h of farming. Quests are ~4-5x more efficient.

## Out of Scope

- Rewriting `daily.yml` (separate spec)
- Quest difficulty guidelines document (separate spec)
- New objective types beyond `harvest_crop` (e.g., crafting, enchanting)
