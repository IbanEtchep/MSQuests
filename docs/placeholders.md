# MSQuests - Placeholders Documentation

## Overview

MSQuests provides PlaceholderAPI integration to display quest information in various places like scoreboards, chat messages, holograms, and more.

All placeholders use the identifier `msquests` and follow the format: `%msquests_<placeholder>%`

## Prerequisites

- **PlaceholderAPI** must be installed on your server
- Player must have an active **tracked quest** for most placeholders to work

---

## Available Placeholders

### Quest Information

#### `%msquests_tracked_quest_name%`
Returns the raw name of the currently tracked quest.

**Example output:** `Main Story Quest`

---

#### `%msquests_tracked_quest%`
Returns the formatted quest information using the configured translation template.

**Example output:** `Quest: Main Story Quest (Stage 2/5)`

---

### Stage Information

#### `%msquests_tracked_quest_stage%`
Returns the formatted current stage information using the configured translation template.

**Example output:** `Stage: Collect Resources`

---

#### `%msquests_tracked_quest_stage_count%`
Returns the total number of stages in the tracked quest.

**Example output:** `5`

**Usage example:**
```
Progress: %msquests_tracked_quest_stage_index%/%msquests_tracked_quest_stage_count%
```

---

#### `%msquests_tracked_quest_stage_index%`
Returns the index (position) of the current stage in the quest.

**Example output:** `2`

**Note:** Index starts at 0 for the first stage.

---

### Objective Information

#### `%msquests_tracked_quest_objective_1%` through `%msquests_tracked_quest_objective_9%`
Returns the formatted information for a specific objective by its position (1-9).

**Example output:** `Collect 10 Oak Wood [5/10]`

**Usage:**
- `%msquests_tracked_quest_objective_1%` - First objective
- `%msquests_tracked_quest_objective_2%` - Second objective
- `%msquests_tracked_quest_objective_3%` - Third objective
- ... and so on up to 9

**Returns:** `null` if the objective doesn't exist at that position.

---

#### `%msquests_tracked_quest_objective_firstactive%`
Returns the formatted information for the first active (incomplete) objective in the current stage.

**Example output:** `Defeat 5 Zombies [2/5]`

---

#### `%msquests_tracked_quest_objective_firstactive_index%`
Returns the index position of the first active objective.

**Example output:** `0`

**Note:** Index starts at 0 for the first objective.

---

#### `%msquests_tracked_quest_stage_objective_count%`
Returns the total number of objectives in the current stage.

**Example output:** `3`

---

## Null Returns

Most placeholders will return `null` (display nothing) in the following cases:
- Player has no tracked quest
- Quest has no current stage
- Requested objective doesn't exist
- Player profile is not found

This allows for clean conditional displays in plugins that support placeholder conditions.

---

## Usage Examples

### Scoreboard Example
```yaml
scoreboard:
  lines:
    - "&e&lActive Quest"
    - "&7%msquests_tracked_quest_name%"
    - ""
    - "&6Stage: &f%msquests_tracked_quest_stage_index%/%msquests_tracked_quest_stage_count%"
    - ""
    - "&aObjectives:"
    - "&7- %msquests_tracked_quest_objective_1%"
    - "&7- %msquests_tracked_quest_objective_2%"
    - "&7- %msquests_tracked_quest_objective_3%"
```

### Chat Format Example
```yaml
chat-format: "&8[&6Quest&8] &7Current: &f%msquests_tracked_quest_name% &8(&e%msquests_tracked_quest_stage_index%/%msquests_tracked_quest_stage_count%&8)"
```

### Tab List Example
```yaml
header: "&e&lYour Quest: &f%msquests_tracked_quest_name%"
footer: "&7Next objective: &a%msquests_tracked_quest_objective_firstactive%"
```

---

## Notes

- All placeholders are case-insensitive
- Formatting and colors depend on your translation configuration
- Placeholders update automatically when quest progress changes
- Maximum of 9 numbered objectives can be displayed directly (1-9)

---

## Translation Keys

The following placeholders use translation keys that can be customized:
- `tracked_quest` → `PLACEHOLDER_QUEST`
- `tracked_quest_stage` → `PLACEHOLDER_STAGE`
- `tracked_quest_objective_*` → `PLACEHOLDER_OBJECTIVE`

Refer to your language configuration file to customize the output format.