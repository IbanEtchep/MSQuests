# Design: Generic conditions system + tracking bossbar

## Context

The plugin has a conditions system on objectives (`QuestObjectiveCondition`, `WorldCondition`, `BiomeCondition`) that is specific and limited. We want a **generic** conditions system inspired by zMenu, reusable everywhere (actions, objectives, quests), with `placeholder` and `permission` as types. In parallel, we add a permanent bossbar for the tracked quest, configurable in the global config.

## 1. Conditions system

### Core interface

```java
public interface Condition {
    boolean test(PlayerProfile profile);
}
```

Replaces `QuestObjectiveCondition` (same signature).

### Condition types

**`PlaceholderCondition`** :
```yaml
conditions:
  - type: placeholder
    placeholder: "%player_world%"
    action: EQUALS_STRING
    value: "world_nether"
```

Supported operators:
- Numeric: `EQUAL_TO`, `SUPERIOR`, `SUPERIOR_OR_EQUAL`, `LOWER`, `LOWER_OR_EQUAL`
- Strings : `EQUALS_STRING`, `EQUALS_IGNORECASE_STRING`, `CONTAINS_STRING`, `DIFFERENT_STRING`

**`PermissionCondition`** :
```yaml
conditions:
  - type: permission
    permission: "quest.vip"
```

### Where conditions apply

The same YAML format `conditions: [...]` can be used in 3 places:

- **Actions** — skip the action if the condition fails
- **Objectives** — replaces the current system (progress conditions)
- **Quests** — start conditions (new, in the quest config)

All the conditions in a list must pass (AND logic).

### Factory

`ConditionFactory` replaces `QuestObjectiveConditionFactory`. Same register/build pattern with the `placeholder` and `permission` types.

## 2. Tracking bossbar

### Global config (`config.yml`)

```yaml
tracking:
  bossbar:
    enabled: true
    message: '%objective_name% - %objective_progress%'
    color: WHITE
    style: PROGRESS
    show_progress: true
```

### Service `TrackingBossBarService`

Manages a `Map<UUID, BossBar>` (player -> bossbar).

Lifecycle:
- **Track** -> creates and shows the bossbar
- **Progress** of the tracked quest -> updates text + progress
- **Objective completion** -> updates to the next active objective
- **Quest completion / Untrack / Disconnect** -> removes the bossbar
- **Join** -> restores if a tracked quest exists

The message is resolved with the placeholders of the first active objective of the tracked quest.

## 3. Avoiding duplicate bossbars

The existing `boss_bar` action on `objective_progress` can be made conditional so it only shows if the quest is NOT tracked:

```yaml
objective_progress:
  - type: boss_bar
    conditions:
      - type: placeholder
        placeholder: "%msquests_tracked_name%"
        action: DIFFERENT_STRING
        value: "%quest_name%"
    params:
      message: '%objective_name% - %objective_progress%'
```

No dedicated `tracked` type — we go through the existing placeholders.

## 4. Removals

- `QuestObjectiveCondition` -> replaced by `Condition`
- `BukkitObjectiveCondition` -> removed
- `WorldCondition`, `BiomeCondition` -> removed (replaced by `PlaceholderCondition`)
- `QuestObjectiveConditionFactory` -> replaced by `ConditionFactory`
- `QuestObjectiveConditionConfigDTO` -> replaced by `ConditionConfigDTO`

## 5. Impacted files

### Core
- `core/.../quest/condition/Condition.java` — new interface
- `core/.../factory/ConditionFactory.java` — new factory (replaces `QuestObjectiveConditionFactory`)
- `core/.../dto/ConditionConfigDTO.java` — new DTO (replaces `QuestObjectiveConditionConfigDTO`)
- `core/.../quest/config/QuestObjectiveConfig.java` — change conditions type
- `core/.../quest/config/QuestConfig.java` — add start conditions
- `core/.../quest/config/action/QuestAction.java` — add conditions
- `core/.../service/QuestProgressService.java` — evaluate action conditions
- `core/.../service/QuestDistributionService.java` — evaluate start conditions

### Bukkit
- `bukkit/.../condition/PlaceholderCondition.java` — new
- `bukkit/.../condition/PermissionCondition.java` — new
- `bukkit/.../service/TrackingBossBarService.java` — new
- `bukkit/.../BukkitQuestsPlugin.java` — wiring
- `bukkit/.../command/QuestCommand.java` — notify TrackingBossBarService on track/untrack
- `bukkit/.../listener/PlayerJoinListener.java` — restore bossbar

### Removals
- `core/.../quest/condition/QuestObjectiveCondition.java`
- `core/.../factory/QuestObjectiveConditionFactory.java`
- `core/.../dto/QuestObjectiveConditionConfigDTO.java`
- `bukkit/.../quest/condition/BukkitObjectiveCondition.java`
- `bukkit/.../quest/condition/impl/WorldCondition.java`
- `bukkit/.../quest/condition/impl/BiomeCondition.java`

## 6. Full example config

```yaml
# config.yml
tracking:
  bossbar:
    enabled: true
    message: '%objective_name% - %objective_progress%'
    color: WHITE
    style: PROGRESS
    show_progress: true

# daily.yml (group)
actions:
  objective_progress:
    - type: boss_bar
      conditions:
        - type: placeholder
          placeholder: "%msquests_tracked_name%"
          action: DIFFERENT_STRING
          value: "%quest_name%"
      params:
        message: '%objective_name% - %objective_progress%'

# quest with conditions
quests:
  nether_mining:
    conditions:
      - type: permission
        permission: "quest.nether"
    stages:
      stage_1:
        objectives:
          mine_netherrack:
            type: block_break
            conditions:
              - type: placeholder
                placeholder: "%player_world%"
                action: EQUALS_STRING
                value: "world_nether"
            params:
              block: NETHERRACK
              amount: 64

## 7. Verification

- `./gradlew build` — compilation + tests pass
- Unit tests for `PlaceholderCondition` (numeric and string operators)
- Unit tests for `PermissionCondition`
- Integration test: bossbar appears on track, updates on progress, disappears on untrack
- Integration test: temporary boss_bar action skipped when quest is tracked (via placeholder condition)
```
