# Conditions System + Tracking Bossbar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the objective-specific condition system with a generic one (placeholder + permission), add conditions to actions and quests, and add a permanent bossbar for tracked quests.

**Architecture:** New `Condition` interface replaces `QuestObjectiveCondition`. `ConditionFactory` builds conditions from YAML config. Conditions are evaluated before action execution, objective progress, and quest start. A `TrackingBossBarService` manages a persistent bossbar per player for their tracked quest.

**Tech Stack:** Java 21, Paper 1.21+, Adventure API (BossBar), PlaceholderAPI, BoostedYAML

---

## File Structure

### Core (new)
- `core/.../quest/condition/Condition.java` — generic condition interface
- `core/.../dto/ConditionConfigDTO.java` — DTO for condition YAML config

### Core (modify)
- `core/.../factory/QuestObjectiveConditionFactory.java` — rename to `ConditionFactory`, generalize
- `core/.../quest/config/QuestObjectiveConfig.java` — change `List<QuestObjectiveCondition>` to `List<Condition>`
- `core/.../quest/config/QuestConfig.java` — add `List<Condition> conditions`
- `core/.../quest/config/action/QuestAction.java` — add `List<Condition> conditions`
- `core/.../dto/QuestActionDTO.java` — add `List<ConditionConfigDTO> conditions`
- `core/.../factory/QuestActionFactory.java` — resolve conditions on actions
- `core/.../factory/QuestObjectiveFactory.java` — update condition resolution
- `core/.../service/QuestProgressService.java` — filter actions by conditions
- `core/.../service/QuestDistributionService.java` — evaluate quest conditions
- `core/.../mapper/QuestConfigMapper.java` — map quest-level conditions

### Core (delete)
- `core/.../quest/condition/QuestObjectiveCondition.java`
- `core/.../dto/QuestObjectiveConditionConfigDTO.java`

### Bukkit (new)
- `bukkit/.../quest/condition/PlaceholderCondition.java`
- `bukkit/.../quest/condition/PermissionCondition.java`
- `bukkit/.../service/TrackingBossBarService.java`
- `bukkit/.../config/TrackingBossBarConfig.java`

### Bukkit (modify)
- `bukkit/.../BukkitQuestsPlugin.java` — register new conditions, wire TrackingBossBarService
- `bukkit/.../command/QuestCommand.java` — notify TrackingBossBarService on track/untrack
- `bukkit/.../listener/PlayerJoinListener.java` — restore bossbar on join
- `bukkit/.../service/GlobalConfigLoaderService.java` — load tracking.bossbar config
- `bukkit/.../config/GlobalConfig.java` — add TrackingBossBarConfig
- `bukkit/.../quest/objective/BukkitQuestObjectiveHandler.java` — use `Condition` instead of `QuestObjectiveCondition`

### Bukkit (delete)
- `bukkit/.../quest/condition/BukkitObjectiveCondition.java`
- `bukkit/.../quest/condition/impl/WorldCondition.java`
- `bukkit/.../quest/condition/impl/BiomeCondition.java`

### Config
- `core/src/main/resources/config.yml` — add tracking.bossbar section

---

## Task 1: Core Condition interface + DTO + Factory

**Files:**
- Create: `core/src/main/java/com/github/ibanetchep/msquests/core/quest/condition/Condition.java`
- Create: `core/src/main/java/com/github/ibanetchep/msquests/core/dto/ConditionConfigDTO.java`
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/factory/QuestObjectiveConditionFactory.java` (rename to ConditionFactory)
- Delete: `core/src/main/java/com/github/ibanetchep/msquests/core/quest/condition/QuestObjectiveCondition.java`
- Delete: `core/src/main/java/com/github/ibanetchep/msquests/core/dto/QuestObjectiveConditionConfigDTO.java`

- [ ] **Step 1: Create `Condition` interface**

```java
// core/src/main/java/com/github/ibanetchep/msquests/core/quest/condition/Condition.java
package com.github.ibanetchep.msquests.core.quest.condition;

import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;

@FunctionalInterface
public interface Condition {
    boolean test(PlayerProfile profile);
}
```

- [ ] **Step 2: Create `ConditionConfigDTO`**

```java
// core/src/main/java/com/github/ibanetchep/msquests/core/dto/ConditionConfigDTO.java
package com.github.ibanetchep.msquests.core.dto;

import java.util.Map;

public record ConditionConfigDTO(String type, Map<String, Object> params) {}
```

- [ ] **Step 3: Rename `QuestObjectiveConditionFactory` to `ConditionFactory`**

Delete `QuestObjectiveConditionFactory.java`. Create `ConditionFactory.java`:

```java
// core/src/main/java/com/github/ibanetchep/msquests/core/factory/ConditionFactory.java
package com.github.ibanetchep.msquests.core.factory;

import com.github.ibanetchep.msquests.core.dto.ConditionConfigDTO;
import com.github.ibanetchep.msquests.core.quest.condition.Condition;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConditionFactory {

    private final Map<String, Function<Map<String, Object>, Condition>> creators = new HashMap<>();

    public void register(String type, Function<Map<String, Object>, Condition> creator) {
        creators.put(type, creator);
    }

    public Condition build(ConditionConfigDTO dto) {
        Function<Map<String, Object>, Condition> creator = creators.get(dto.type());
        return creator != null ? creator.apply(dto.params()) : null;
    }
}
```

- [ ] **Step 4: Delete old files**

Delete:
- `core/src/main/java/com/github/ibanetchep/msquests/core/quest/condition/QuestObjectiveCondition.java`
- `core/src/main/java/com/github/ibanetchep/msquests/core/dto/QuestObjectiveConditionConfigDTO.java`

- [ ] **Step 5: Update `QuestObjectiveConfig` to use `Condition`**

In `core/src/main/java/com/github/ibanetchep/msquests/core/quest/config/QuestObjectiveConfig.java`, change:

```java
// Replace import
import com.github.ibanetchep.msquests.core.quest.condition.Condition;

// Replace field (line 17)
private List<Condition> conditions = List.of();

// Replace setter/getter parameter types
public void setConditions(List<Condition> conditions)
public List<Condition> getConditions()
```

- [ ] **Step 6: Update `QuestObjectiveFactory` to use `ConditionFactory`**

In `core/src/main/java/com/github/ibanetchep/msquests/core/factory/QuestObjectiveFactory.java`:
- Change constructor param type from `QuestObjectiveConditionFactory` to `ConditionFactory`
- Change field type to `ConditionFactory`
- In `resolveConditions()`: replace `QuestObjectiveConditionConfigDTO` with `ConditionConfigDTO`, replace `QuestObjectiveCondition` with `Condition`

- [ ] **Step 7: Update `BukkitQuestObjectiveHandler` to use `Condition`**

In `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/objective/BukkitQuestObjectiveHandler.java`:
- Replace import of `QuestObjectiveCondition` with `Condition`
- The `getEligibleObjectives` method already calls `c.test(profile)` — the signature is the same.

- [ ] **Step 8: Verify compilation**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (tests may fail due to deleted condition types — that's ok, fixed in next task)

- [ ] **Step 9: Commit**

```
git add -A
git commit -m "Replace QuestObjectiveCondition with generic Condition interface"
```

---

## Task 2: PlaceholderCondition + PermissionCondition

**Files:**
- Create: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/condition/PlaceholderCondition.java`
- Create: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/condition/PermissionCondition.java`
- Delete: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/condition/BukkitObjectiveCondition.java`
- Delete: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/condition/impl/WorldCondition.java`
- Delete: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/condition/impl/BiomeCondition.java`
- Modify: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/BukkitQuestsPlugin.java`

- [ ] **Step 1: Create `PlaceholderCondition`**

```java
// bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/condition/PlaceholderCondition.java
package com.github.ibanetchep.msquests.bukkit.quest.condition;

import com.github.ibanetchep.msquests.core.quest.condition.Condition;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class PlaceholderCondition implements Condition {

    private final String placeholder;
    private final String action;
    private final String value;

    public PlaceholderCondition(Map<String, Object> params) {
        this.placeholder = (String) params.get("placeholder");
        this.action = ((String) params.get("action")).toUpperCase();
        this.value = String.valueOf(params.get("value"));
    }

    @Override
    public boolean test(PlayerProfile profile) {
        Player player = Bukkit.getPlayer(profile.getId());
        if (player == null) return false;

        String resolved = PlaceholderAPI.setPlaceholders(player, placeholder);
        String resolvedValue = PlaceholderAPI.setPlaceholders(player, value);

        return switch (action) {
            case "EQUALS_STRING" -> resolved.equals(resolvedValue);
            case "EQUALS_IGNORECASE_STRING" -> resolved.equalsIgnoreCase(resolvedValue);
            case "CONTAINS_STRING" -> resolved.contains(resolvedValue);
            case "DIFFERENT_STRING" -> !resolved.equals(resolvedValue);
            default -> compareNumeric(resolved, resolvedValue);
        };
    }

    private boolean compareNumeric(String resolved, String resolvedValue) {
        try {
            double a = Double.parseDouble(resolved);
            double b = Double.parseDouble(resolvedValue);
            return switch (action) {
                case "EQUAL_TO" -> a == b;
                case "SUPERIOR" -> a > b;
                case "SUPERIOR_OR_EQUAL" -> a >= b;
                case "LOWER" -> a < b;
                case "LOWER_OR_EQUAL" -> a <= b;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: Create `PermissionCondition`**

```java
// bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/condition/PermissionCondition.java
package com.github.ibanetchep.msquests.bukkit.quest.condition;

import com.github.ibanetchep.msquests.core.quest.condition.Condition;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class PermissionCondition implements Condition {

    private final String permission;

    public PermissionCondition(Map<String, Object> params) {
        this.permission = (String) params.get("permission");
    }

    @Override
    public boolean test(PlayerProfile profile) {
        Player player = Bukkit.getPlayer(profile.getId());
        return player != null && player.hasPermission(permission);
    }
}
```

- [ ] **Step 3: Delete old condition files**

Delete:
- `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/condition/BukkitObjectiveCondition.java`
- `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/condition/impl/WorldCondition.java`
- `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/quest/condition/impl/BiomeCondition.java`

- [ ] **Step 4: Update `BukkitQuestsPlugin.buildConditionFactory()`**

Replace the condition factory construction. Find the method that creates `QuestObjectiveConditionFactory` and registers `world`/`biome` conditions. Replace with:

```java
private ConditionFactory buildConditionFactory() {
    ConditionFactory factory = new ConditionFactory();
    factory.register("placeholder", PlaceholderCondition::new);
    factory.register("permission", PermissionCondition::new);
    return factory;
}
```

Update imports: remove `QuestObjectiveConditionFactory`, `WorldCondition`, `BiomeCondition`, `BukkitObjectiveCondition`. Add `ConditionFactory`, `PlaceholderCondition`, `PermissionCondition`.

- [ ] **Step 5: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
git add -A
git commit -m "Add PlaceholderCondition and PermissionCondition, remove old condition types"
```

---

## Task 3: Add conditions to actions

**Files:**
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/dto/QuestActionDTO.java`
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/quest/config/action/QuestAction.java`
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/factory/QuestActionFactory.java`
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/service/QuestProgressService.java`
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/service/QuestLifecycleService.java`

- [ ] **Step 1: Add conditions to `QuestActionDTO`**

```java
// core/src/main/java/com/github/ibanetchep/msquests/core/dto/QuestActionDTO.java
package com.github.ibanetchep.msquests.core.dto;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record QuestActionDTO(
        String type,
        @Nullable String name,
        Map<String, Object> params,
        @Nullable List<ConditionConfigDTO> conditions
) {}
```

- [ ] **Step 2: Add conditions field to `QuestAction`**

In `core/src/main/java/com/github/ibanetchep/msquests/core/quest/config/action/QuestAction.java`, add:

```java
import com.github.ibanetchep.msquests.core.quest.condition.Condition;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import java.util.List;

// Add field after name:
private List<Condition> conditions = List.of();

// Add getter/setter:
public List<Condition> getConditions() {
    return conditions;
}

public void setConditions(List<Condition> conditions) {
    this.conditions = conditions;
}

public boolean testConditions(PlayerProfile profile) {
    return conditions.stream().allMatch(c -> c.test(profile));
}
```

- [ ] **Step 3: Resolve conditions in `QuestActionFactory`**

In `core/src/main/java/com/github/ibanetchep/msquests/core/factory/QuestActionFactory.java`:

Add `ConditionFactory` as constructor dependency. After creating the action in `createAction()`, resolve conditions:

```java
private final ConditionFactory conditionFactory;

public QuestActionFactory(ConditionFactory conditionFactory) {
    this.conditionFactory = conditionFactory;
}

public QuestAction createAction(QuestActionDTO config) {
    RegisteredAction<?> registered = registeredTypes.get(config.type());
    if (registered == null) {
        throw new IllegalArgumentException("Unknown action type: " + config.type());
    }
    JsonSchemaValidator.validate(config.params(), registered.schema());
    QuestAction action = registered.factory().apply(config);

    if (config.conditions() != null) {
        List<Condition> conditions = config.conditions().stream()
                .map(conditionFactory::build)
                .filter(Objects::nonNull)
                .toList();
        action.setConditions(conditions);
    }

    return action;
}
```

- [ ] **Step 4: Filter actions by conditions in `QuestProgressService`**

In `core/src/main/java/com/github/ibanetchep/msquests/core/service/QuestProgressService.java`, line 62:

Replace:
```java
objective.getQuest().getQuestGroup().getObjectiveProgressActions().forEach(a -> a.execute(objective));
```

With:
```java
objective.getQuest().getQuestGroup().getObjectiveProgressActions().stream()
    .filter(a -> profile == null || a.testConditions(profile))
    .forEach(a -> a.execute(objective));
```

- [ ] **Step 5: Filter actions by conditions in `QuestLifecycleService`**

In `completeObjective()` (line 108), replace:
```java
groupConfig.getObjectiveCompleteActions().forEach(a -> a.execute(objective));
```
With:
```java
groupConfig.getObjectiveCompleteActions().stream()
    .filter(a -> profile == null || a.testConditions(profile))
    .forEach(a -> a.execute(objective));
```

Note: `quest_start`, `quest_complete`, `quest_distribution`, and `actor_load` actions don't have a `PlayerProfile` in context for single-player evaluation in all cases. Condition filtering on those hooks will require passing profile where available. For now, focus on `objective_progress` and `objective_complete` where profile is always available.

- [ ] **Step 6: Update YAML deserialization for action conditions**

The `QuestActionDTO` is deserialized by Jackson in `QuestConfigYamlRepository`. Since we added `conditions` as a nullable field, Jackson with SNAKE_CASE should pick it up automatically from the YAML structure:

```yaml
- type: boss_bar
  conditions:
    - type: placeholder
      placeholder: "%msquests_tracked_name%"
      action: DIFFERENT_STRING
      value: "%quest_name%"
  params:
    message: '%objective_name% - %objective_progress%'
```

The `conditions` list will deserialize as `List<ConditionConfigDTO>` if `ConditionConfigDTO` has the right fields. Verify Jackson auto-discovers the record fields.

- [ ] **Step 7: Update `QuestActionFactory` constructor call in `BukkitQuestsPlugin`**

Pass `conditionFactory` to `QuestActionFactory`:
```java
questActionFactory = new QuestActionFactory(conditionFactory);
```

- [ ] **Step 8: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```
git add -A
git commit -m "Add condition support to quest actions"
```

---

## Task 4: Add conditions to quest config (start conditions)

**Files:**
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/quest/config/QuestConfig.java`
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/dto/QuestConfigDTO.java`
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/mapper/QuestConfigMapper.java`
- Modify: `core/src/main/java/com/github/ibanetchep/msquests/core/service/QuestDistributionService.java`

- [ ] **Step 1: Add conditions to `QuestConfig`**

```java
// Add field
private List<Condition> conditions = List.of();

// Add getter/setter
public List<Condition> getConditions() {
    return conditions;
}

public void setConditions(List<Condition> conditions) {
    this.conditions = conditions;
}
```

- [ ] **Step 2: Add conditions to `QuestConfigDTO`**

Add a `@Nullable List<ConditionConfigDTO> conditions` field to the `QuestConfigDTO` record. Check the current fields and add it.

- [ ] **Step 3: Resolve conditions in `QuestConfigMapper`**

In `QuestConfigMapper`, inject `ConditionFactory` via constructor. In `toEntity()`, after creating the `QuestConfig`, resolve conditions:

```java
if (dto.conditions() != null) {
    List<Condition> conditions = dto.conditions().stream()
            .map(conditionFactory::build)
            .filter(Objects::nonNull)
            .toList();
    questConfig.setConditions(conditions);
}
```

- [ ] **Step 4: Evaluate quest conditions in `QuestDistributionService.canStartQuest()`**

Add a `@Nullable PlayerProfile` parameter to `canStartQuest()`. Before returning `SUCCESS`, check:

```java
if (profile != null && !questConfig.getConditions().stream().allMatch(c -> c.test(profile))) {
    return QuestStartResult.CONDITIONS_NOT_MET;
}
```

Add `CONDITIONS_NOT_MET` to `QuestStartResult` enum.

Update all callers of `canStartQuest` to pass profile where available (in `QuestLifecycleService.startQuest` and `distributeQuests`).

- [ ] **Step 5: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
git add -A
git commit -m "Add condition support to quest configs for start conditions"
```

---

## Task 5: Tracking Bossbar Config

**Files:**
- Create: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/config/TrackingBossBarConfig.java`
- Modify: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/config/GlobalConfig.java`
- Modify: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/service/GlobalConfigLoaderService.java`
- Modify: `core/src/main/resources/config.yml`

- [ ] **Step 1: Create `TrackingBossBarConfig`**

```java
// bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/config/TrackingBossBarConfig.java
package com.github.ibanetchep.msquests.bukkit.config;

import net.kyori.adventure.bossbar.BossBar;

public record TrackingBossBarConfig(
        boolean enabled,
        String message,
        BossBar.Color color,
        BossBar.Overlay style,
        boolean showProgress
) {}
```

- [ ] **Step 2: Add to `GlobalConfig`**

Add `TrackingBossBarConfig trackingBossBar` field to the `GlobalConfig` record.

- [ ] **Step 3: Load in `GlobalConfigLoaderService`**

In the `load()` method, after loading database config, add:

```java
boolean bossBarEnabled = config.getBoolean("tracking.bossbar.enabled", false);
String bossBarMessage = config.getString("tracking.bossbar.message", "%objective_name% - %objective_progress%");
BossBar.Color bossBarColor = BossBar.Color.valueOf(config.getString("tracking.bossbar.color", "WHITE").toUpperCase());
BossBar.Overlay bossBarStyle = BossBar.Overlay.valueOf(config.getString("tracking.bossbar.style", "PROGRESS").toUpperCase());
boolean bossBarShowProgress = config.getBoolean("tracking.bossbar.show_progress", true);

TrackingBossBarConfig trackingBossBarConfig = new TrackingBossBarConfig(
        bossBarEnabled, bossBarMessage, bossBarColor, bossBarStyle, bossBarShowProgress
);
```

Pass it to the `GlobalConfig` constructor.

- [ ] **Step 4: Add defaults to `config.yml`**

In `core/src/main/resources/config.yml`, add:

```yaml
tracking:
  bossbar:
    enabled: false
    message: '%objective_name% - %objective_progress%'
    color: WHITE
    style: PROGRESS
    show_progress: true
```

- [ ] **Step 5: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
git add -A
git commit -m "Add tracking bossbar configuration"
```

---

## Task 6: TrackingBossBarService

**Files:**
- Create: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/service/TrackingBossBarService.java`
- Modify: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/BukkitQuestsPlugin.java`
- Modify: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/command/QuestCommand.java`
- Modify: `bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/listener/PlayerJoinListener.java`

- [ ] **Step 1: Create `TrackingBossBarService`**

```java
// bukkit/src/main/java/com/github/ibanetchep/msquests/bukkit/service/TrackingBossBarService.java
package com.github.ibanetchep.msquests.bukkit.service;

import com.github.ibanetchep.msquests.bukkit.config.TrackingBossBarConfig;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrackingBossBarService {

    private final TrackingBossBarConfig config;
    private final PlayerProfileRegistry profileRegistry;
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();

    public TrackingBossBarService(TrackingBossBarConfig config, PlayerProfileRegistry profileRegistry) {
        this.config = config;
        this.profileRegistry = profileRegistry;
    }

    public boolean isEnabled() {
        return config.enabled();
    }

    public void showBossBar(Player player) {
        if (!config.enabled()) return;

        PlayerProfile profile = profileRegistry.getPlayerProfile(player.getUniqueId());
        if (profile == null) return;

        Quest quest = profile.getTrackedQuest();
        if (quest == null) return;

        QuestObjective objective = quest.getFirstActiveObjective();

        Component text = resolveMessage(quest, objective);
        float progress = objective != null
                ? (config.showProgress() ? (float) objective.getProgressRatio() / 100f : 1.0f)
                : 1.0f;

        BossBar existing = bossBars.get(player.getUniqueId());
        if (existing != null) {
            existing.name(text);
            existing.progress(Math.min(1.0f, Math.max(0.0f, progress)));
        } else {
            BossBar bossBar = BossBar.bossBar(text, Math.min(1.0f, Math.max(0.0f, progress)), config.color(), config.style());
            bossBars.put(player.getUniqueId(), bossBar);
            player.showBossBar(bossBar);
        }
    }

    public void updateBossBar(Player player) {
        BossBar existing = bossBars.get(player.getUniqueId());
        if (existing == null) return;

        PlayerProfile profile = profileRegistry.getPlayerProfile(player.getUniqueId());
        if (profile == null) return;

        Quest quest = profile.getTrackedQuest();
        if (quest == null) {
            hideBossBar(player);
            return;
        }

        QuestObjective objective = quest.getFirstActiveObjective();
        Component text = resolveMessage(quest, objective);
        float progress = objective != null
                ? (config.showProgress() ? (float) objective.getProgressRatio() / 100f : 1.0f)
                : 1.0f;

        existing.name(text);
        existing.progress(Math.min(1.0f, Math.max(0.0f, progress)));
    }

    public void hideBossBar(Player player) {
        BossBar bossBar = bossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }

    public void onQuestProgress(QuestObjective objective) {
        Quest quest = objective.getQuest();
        for (PlayerProfile profile : quest.getActor().getProfiles()) {
            if (!quest.getId().equals(profile.getTrackedQuestId())) continue;
            Player player = Bukkit.getPlayer(profile.getId());
            if (player != null) {
                updateBossBar(player);
            }
        }
    }

    public void onQuestComplete(Quest quest) {
        for (PlayerProfile profile : quest.getActor().getProfiles()) {
            if (!quest.getId().equals(profile.getTrackedQuestId())) continue;
            Player player = Bukkit.getPlayer(profile.getId());
            if (player != null) {
                hideBossBar(player);
            }
        }
    }

    private Component resolveMessage(Quest quest, QuestObjective objective) {
        String message = config.message();
        if (objective != null) {
            return MessageBuilder.raw(message)
                    .applyPlaceholderResolver(objective)
                    .toComponent();
        }
        return MessageBuilder.raw(message)
                .applyPlaceholderResolver(quest)
                .toComponent();
    }
}
```

- [ ] **Step 2: Wire in `BukkitQuestsPlugin`**

Add field:
```java
private TrackingBossBarService trackingBossBarService;
```

In `onEnable()`, after global config is loaded and registries are created:
```java
trackingBossBarService = new TrackingBossBarService(globalConfig.trackingBossBar(), playerProfileRegistry);
```

Add getter:
```java
public TrackingBossBarService getTrackingBossBarService() {
    return trackingBossBarService;
}
```

- [ ] **Step 3: Notify on track/untrack in `QuestCommand`**

In `QuestCommand.track()`, after `profile.setTrackedQuestId(quest.getId())`:
```java
Player player = actor.requirePlayer();
if (plugin.getTrackingBossBarService().isEnabled()) {
    plugin.getTrackingBossBarService().showBossBar(player);
}
```

In `QuestCommand.untrack()`, before `profile.setTrackedQuestId(null)`:
```java
Player player = actor.requirePlayer();
if (plugin.getTrackingBossBarService().isEnabled()) {
    plugin.getTrackingBossBarService().hideBossBar(player);
}
```

In `QuestCommand.toggleTrack()`, handle both cases similarly.

- [ ] **Step 4: Restore on join in `PlayerJoinListener`**

After `loadPlayer` completes (in the future callback), show bossbar:
```java
plugin.getQuestPlayerService().loadPlayer(player).thenRun(() -> {
    plugin.getScheduler().runLater(() -> {
        if (plugin.getTrackingBossBarService().isEnabled()) {
            plugin.getTrackingBossBarService().showBossBar(player);
        }
    }, 1, TimeUnit.SECONDS);
});
```

- [ ] **Step 5: Notify on progress in `QuestProgressService`**

This requires the service to be accessible. The cleanest way: dispatch a core event that the Bukkit side listens to. However, `QuestProgressService` already fires `CoreQuestObjectiveProgressedEvent`. We can listen to that in a Bukkit listener.

Create a listener or hook in `BukkitQuestsPlugin` that listens to `CoreQuestObjectiveProgressedEvent` (which is dispatched as a Bukkit event via `BukkitEventDispatcher`). In the listener, call `trackingBossBarService.onQuestProgress(objective)`.

Alternatively, add the call directly after the action execution in `QuestProgressService.flushProgress()` line 62. But since `TrackingBossBarService` is Bukkit-side, the event listener approach is cleaner.

Register a Bukkit listener for the event and call `trackingBossBarService.onQuestProgress()`.

- [ ] **Step 6: Notify on quest complete**

Similarly, listen to `CoreQuestCompletedEvent` (dispatched as Bukkit event) and call `trackingBossBarService.onQuestComplete(quest)`.

- [ ] **Step 7: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```
git add -A
git commit -m "Add TrackingBossBarService for permanent tracked quest bossbar"
```

---

## Task 7: Update test configs and fix tests

**Files:**
- Modify: `bukkit/src/test/resources/quests/group_1.yml` — update condition format
- Modify: any tests referencing old condition types

- [ ] **Step 1: Update test YAML configs**

In `bukkit/src/test/resources/quests/group_1.yml`, replace `world`/`biome` conditions with placeholder conditions:

```yaml
conditions:
  - type: placeholder
    placeholder: "%player_world%"
    action: EQUALS_STRING
    value: "world"
  - type: placeholder
    placeholder: "%player_biome%"
    action: EQUALS_STRING
    value: "soul_sand_valley"
```

- [ ] **Step 2: Fix any broken tests**

Check and fix tests that reference `WorldCondition`, `BiomeCondition`, or `QuestObjectiveCondition`. Update imports and types to use `Condition`.

- [ ] **Step 3: Run full test suite**

Run: `./gradlew test`
Expected: All tests pass

- [ ] **Step 4: Commit**

```
git add -A
git commit -m "Update tests for new condition system"
```

---

## Verification

1. `./gradlew build` — full build passes
2. `./gradlew test` — all tests green
3. Manual test checklist:
   - Configure `tracking.bossbar.enabled: true` in config.yml
   - Track a quest with `/quest track daily <key>` — bossbar appears
   - Progress the tracked quest — bossbar updates
   - Untrack with `/quest untrack` — bossbar disappears
   - Add `conditions` to a boss_bar action in daily.yml — verify it skips when tracked
   - Add placeholder condition to an objective — verify progression is filtered
   - Reconnect — bossbar restores for tracked quest
