# Architecture - MSQuests

This document describes the architecture of MSQuests, a Minecraft quest plugin for Paper, Folia and Spigot 1.21+ (Paper-only code is isolated in `bukkit/platform/`).

---

## Overview

MSQuests is a modular quest system built on a layered architecture with a strict separation between business logic (core) and the platform (Bukkit). The project uses Gradle multi-module and targets Java 21.

```
                 +-----------+
                 |  bukkit   |  Paper plugin, commands, listeners, handlers
                 +-----+-----+
                       |
              +--------+--------+
              |                 |
        +-----+-----+    +-----+-----+
        |   core    |    | database  |  Async SQL persistence
        +-----------+    +-----------+
        Domain, services,    JDBI, HikariCP,
        registries, events    migrations
```

**Module dependencies:**
- `bukkit` depends on `core` and `database`
- `database` depends on `core` (via `api(project(":core"))`)
- `core` has no dependency on the other modules

---

## Modules

### `core/` - Business logic

Root package: `com.github.ibanetchep.msquests.core`

Contains all the platform-independent business logic:

| Package | Responsibility |
|---------|----------------|
| `quest/actor/` | Main entities: `Quest`, `QuestStage`, `QuestActor`, `ActorQuestGroup` |
| `quest/player/` | `PlayerProfile` - player profile with its actors and quests |
| `quest/config/` | Configurations: `QuestConfig`, `QuestStageConfig`, `QuestObjectiveConfig`, `QuestGroupConfig`, `QuestTierConfig` |
| `quest/config/annotation/` | `@ObjectiveType`, `@ConfigField`, `@AtLeastOneOfFields` |
| `quest/config/action/` | `ActionChain` (opaque Artisan chain), `ChainRunner` port, `ChainContext` (bindings) |
| `quest/config/condition/` | `Conditions` (opaque Artisan conditions), `ConditionChecker` port |
| `quest/config/group/` | `QuestGroupConfig` with builder pattern |
| `quest/objective/` | `QuestObjective` interface, `AbstractQuestObjective<C>`, enums `QuestObjectiveStatus`, `Flow` |
| `quest/executor/` | `AtomicQuestExecutor` / `AtomicLocalQuestExecutor` - thread-safe execution per quest |
| `quest/result/` | `QuestStartResult`, `QuestRotateResult` - result enums |
| `service/` | Business services (orchestration) |
| `factory/` | Factories with a type registry |
| `registry/` | In-memory registries (ConcurrentHashMap) |
| `event/` | `EventDispatcher`, `CoreEvent`, domain events |
| `dto/` | DTOs for serialization/persistence |
| `mapper/` | DTO <-> domain mappers |
| `lang/` | `Translator`, `Translatable`, `PlaceholderProvider` |
| `repository/` | Repository interfaces |
| `util/` | `CronUtils`, `JsonSchemaGenerator`, `JsonSchemaValidator` |

### `database/` - Persistence

Root package: `com.github.ibanetchep.msquests.database`

| Class/Package | Responsibility |
|----------------|----------------|
| `DbAccess` | HikariCP connection pool, JDBI instance, single-thread executor |
| `DbCredentials` | Configuration record (H2 or MySQL) |
| `MigrationManager` | Sequential migration system with a `schema_version` table |
| `migrations/` | `CreateTablesMigration` (v1), `AddRotationTableMigration` (v2), `CreateArtisanTablesMigration` (v3, `artisan_quests_*` tables) |
| `SqlTables` | Table names of a layout: `LEGACY` (`msquests_*`) or `ARTISAN` (`artisan_quests_*`) |
| `LegacyDataCopier` | One-time transactional copy from `msquests_*` to `artisan_quests_*` (see *Content source*) |
| `repository/` | `SqlRepository` (async base), SQL implementations for Quest, PlayerProfile, Actor, Rotation; `ArtisanQuestSqlRepository` for `artisan_quests_quest/objective` |
| `RecordRowMapper` | Generic JDBI mapper for Java records |

**Main schema:**
```
msquests_player_profile (id, name, tracked_quest_id, created_at, updated_at)
msquests_actor          (id, actor_type, created_at, updated_at)
msquests_quest          (id, quest_key, group_key, status, actor_id FK, timestamps)
msquests_objective      (objective_key, quest_id FK, status, type, progress) -- composite PK
msquests_rotation       (actor_id, group_key, rotated_at)

-- v3, used with content_source: artisan (the msquests_* tables stay untouched)
artisan_quests_profile   (like msquests_player_profile)
artisan_quests_actor     (like msquests_actor)
artisan_quests_quest     (id, quest_uid, quest_key, group_key, status, actor_id FK, timestamps)
artisan_quests_objective (quest_id FK, stage_key, objective_key, status, type, progress) -- composite PK
artisan_quests_rotation  (actor_id, group_uid, rotated_at)
artisan_quests_data_copy (one row once the copy is done)
```

`quest_key` / `group_key` of `artisan_quests_quest` are only the last recorded
names (logs, support): a row is resolved by `quest_uid`.

### `bukkit/` - Paper integration

Root package: `com.github.ibanetchep.msquests.bukkit`

| Package | Responsibility |
|---------|----------------|
| `BukkitQuestsPlugin` | Entry point, implements `MSQuestsPlatform`, wires all the components |
| `command/` | Lamp commands: `QuestCommand`, `QuestAdminCommand`, custom parameter types |
| `listener/` | `ServerLoadListener` (config loading), `PlayerJoinListener` (player loading) |
| `event/` | `BukkitEventDispatcher` + Bukkit events mirroring the core events |
| `quest/objective/` | 10 objective handlers (one sub-package per type) |
| `quest/actor/` | `BukkitQuestPlayerActor`, `BukkitQuestGlobalActor` |
| `config/` | `GlobalConfig`, `GlobalConfigLoaderService` |
| `repository/` | `QuestConfigYamlRepository` (Jackson YAML, `quests/*.yml`) |
| `document/` | `quest_group` Documents: `QuestGroupDocumentReader`/`Writer`, `QuestGroupDocumentRepository`, `LegacyContentConverter`, `MigrationMapFile` |
| `migration/` | `ContentMigration` (`/msquests migrate`) |
| `service/` | `QuestPlayerService` (player loading) |
| `lang/` | `BukkitTranslator` (file kept up to date by BoostedYAML, keys served by Artisan's translations, `msquests` bundle) |
| `text/` | `PlaceholderEngine`, `MessageBuilder` |
| `placeholder/` | `QuestPlaceholders` (Artisan namespace `quests`) |
| `placeholderapi/` | `QuestsPlaceholderExpansion` (PAPI alias `msquests`) |
| `zmenu/` | zMenu integration (quest group pagination) |
| `artisan/` | Artisan integration (`editor/` project, `quest_group` kind, `quests:*` actions, `ArtisanChainRunner`, `ArtisanConditionChecker`, data sources, menu commands) |

---

## Artisan integration

**Required** (`depend: [Artisan]` in `plugin.yml`, Artisan API 2.10.0+). At the very
start of `onEnable`, `ArtisanIntegration.resolve` loads `ArtisanAPI` from the
`ServicesManager`; if it is missing or too old, the plugin disables itself with a
SEVERE message, before opening the database. Once the services are wired, `MsQuestsArtisanModule`
is registered and declares:

- **an example template** `daily` (Artisan 2.6.0, `api.getTemplates()`, files in
  `bukkit/src/main/resources/artisan-templates/daily/`): an `example_daily` group of three
  daily quests, which the editor offers to copy into any project (**+ → From
  a module…**). On first start, Artisan copies it once into the `quests` module folder
  (`plugins/Artisan/modules/quests/`, `EditorProject`); if deleted, it does not come back.
  Bump `TEMPLATE_VERSION` on every change to these files: the editor then offers the
  update to unmodified copies;
- **the module folder** `quests` (Artisan 2.8.0, `api.getProjectRoots().moduleFolder`):
  the editor files it under the installed Quests module, not among the admin's modules. It
  has the name of the former ordinary project `quests`, which therefore becomes the module
  folder as is, without a move. The module's former project, `plugins/MSQuests/editor/`, is moved there
  once if it contains groups and the folder does not exist yet. A folder already there
  changes key (`addon:quests` instead of its `.artisan/project-id`): its earlier Versions and
  Drafts are no longer attached to it;
- **the `quest_group` Document kind** (`quest_groups/` folder, `quest_group/v1` schema):
  one Document per group, named after the group key, in **any Artisan
  project** (`ArtisanProjectFiles` on `api.getProject()`), not only the module's. A group's
  action chains run in the project that holds its Document, so that its menu
  references resolve there. Quests are loaded from there with
  `content_source: artisan` (see *Content source*); otherwise from `quests/*.yml`.
  `onReload` (publish, `/artisan reload`) reloads the quests when these Documents have
  changed (SHA-256 fingerprint), and does nothing otherwise;
- **its editor view** `artisan-editor/ui.js` (Artisan 2.3.0+, otherwise a warning and no
  view): the repository's `editor/` folder (TypeScript + React, against `@artisan/editor-sdk`, taken
  from `editor/vendor/` until the SDK is published) builds it into
  `bukkit/src/main/resources/artisan-editor/` (`cd editor && npm ci && npm run build`, output
  committed: the Gradle build does not need Node). Artisan serves it to the editor, which
  loads it for the `quest_group` kind: group view, then quest view (stages, objectives,
  `run_command` rewards, texts in the Translations panel). `EDITOR_SDK` must equal the
  SDK version the build prints;
- **the `quests:*` actions** (`QuestActions`), usable in any Artisan chain:

| Action | Arguments | Effect |
|---|---|---|
| `quests:start` | `group`, `quest` | Starts the quest for the player's actor, with the group's rules (limits, requirements) |
| `quests:abandon` | `group`, `quest` | Abandons the active instance: it becomes `FAILED` and still counts in the period; if tracked, it no longer is |
| `quests:track` | `group`, `quest` | Tracks / untracks the active instance (like `msquests:track`) |
| `quests:rotate` | `group`, `quest` | Swaps the active instance for another one of the group |
| `quests:complete` | `group`, `quest` | Completes the active instance, rewards included |
| `quests:distribute` | `group` (primary), `amount?` | Distributes `amount` quests, otherwise applies the group's distribution |
| `quests:action_bar` | `text` (primary, ltext) | Shows the text in the action bar (Artisan's core has none) |
| `quests:boss_bar` | `text` (primary), `color`, `style`, `show_progress`, `duration` | Per-player boss bar, updated in place, hidden `duration` s after the last call; filled with the chain's `progress_ratio` |

`group` is a `document_ref` to `quest_group`: the editor offers a picker, draws
the edge and follows a rename. Its value is the group key (a qualified reference
`project:daily` is reduced to `daily`), so it also works for a group still read from
`quests/*.yml`, but the picker stays empty as long as no `quest_group` Document exists.
`quest` is the quest key in the group. Both go through `ActionRuntime.text`,
so the `{key}` of an iterated line is interpolated. The targeted actor is the one of the actor type
of the group that the player belongs to; unknown data logs a warning and the chain
continues.

### Rewards, hooks and requirements: Artisan chains

A quest's hooks (`actions.start`, `objective_progress`, `objective_complete`, `complete`,
each run after the group's hook for the same event) and the 7 group hooks (`actions` key; former names `on` and `rewards`, still read) (`actions.quest_start`, `quest_complete`,
`objective_progress`, `objective_complete`, `quest_distribution`, `actor_load`,
`all_period_quests_complete`) are Artisan chains (structured wire: `{action: args}`,
`if/do/else`). The core carries them as an opaque `ActionChain` and runs them through the
`ChainRunner` port; `ArtisanChainRunner` passes them to `ModuleActionsApi.run` in the
`quests` project, **once per online member of the actor** (a global or guild group therefore runs
one console command per member), after a hop to the main thread if needed.

Bindings (`ChainContext#args`): `actor`, `actor_name`, `group`, `group_title`, `quest`,
`quest_title`, `objective`, `objective_title`, `progress`, `amount`, `progress_ratio`,
`objectives` (each objective rendered with the group's `objective_line`, default
`<newline> - {objective_name}`), `tracked` (per player), plus all the former
MSQuests placeholders (`quest_name`, `objective_progress`, `quest_tier_name`...).

There is no longer a list of rewards to display: what the player wins is written in the
quest description or in the menu item. The legacy conversion appends the `name` of the
former actions to the end of the description, and `%quest_rewards%` (zMenu) is empty. A quest's `requirements` and an
objective's `conditions` are Artisan conditions (`Conditions`), tested through `ConditionChecker` /
`ArtisanConditionChecker` (`ConditionsApi.test`); an offline player or a call off
its thread is false. They are validated (`ConditionsApi.validate`) on every
load, and errors are logged.

The in-house actions and conditions (`command`, `message`, `title`, `boss_bar`,
`permission`, `placeholder`...) no longer exist. `LegacyActions` converts them on the fly
(`quests/*.yml` on every load, migration to Documents, legacy steps left
in a Document): `command` → `run_command` (console), `message` → `send_message`
(`objective_template` → `objective_line`), `title` → `show_title`, `give_item` →
`give_item`, `action_bar`/`boss_bar` → `quests:*`, `%quest_name%` → `{quest_name}`,
`%msquests_x%` → `%quests_x%`, conditions → `has_permission`, `placeholder_equals`,
`placeholder_match` or `expression` (the only one that reads bindings). Differences noted in the
migration report: `give_item` no longer drops on the ground what does not fit, unknown condition
dropped.

Placeholders: `QuestPlaceholders` is registered in Artisan's `PlaceholderRegistry`
(namespace `quests`, exposed to PAPI); `%msquests_*%` stays as a PAPI alias (C7). New
keys `status_<group>:<quest>` and `completed_<group>:<quest>`.

The data sources and commands below remain (`msquests:*` alias, C7):

| Data source | Stability | Content |
|---|---|---|
| `msquests:groups` | STATIC (`key`) | Quest groups + the actor's counters, nested `quests[]` |
| `msquests:quests` | STATIC (`id` = `group:key`) | Full catalog: config + `stages[] > objectives[]`, with the actor's progress laid over it |
| `msquests:active` | DYNAMIC | The actor's ongoing instances, objective by objective |

Three parameters, common to the three sources:

- `player` — target a player other than the one opening the menu (by name, must be online)
- `actor` — which actor to read: `player` (default), `global`, or any other registered type
- `group` — restrict to one group (`msquests:quests`, `msquests:active`)

`ActorResolver` (in `core/registry/`) resolves (player, actor type) via
`QuestActor#isMember`, never by identity: a future guild actor works without
touching the integration. It lives in `core` because it owes nothing to Artisan —
commands, placeholders and menus ask the same question.

Likewise, `ActorQuestGroup#getCurrentAttempt(questKey)` answers "the actor's current
attempt at this quest" (active instance, otherwise the most recent non-expired one of the
period). It is the question every display surface asks; it is
resolved once, in the domain.

**Catalog = config + progress laid over it.** `msquests:quests` is keyed on the
**configs**, not on the instances: all the quests of a group appear, started
or not, and the player's state is laid over them. A quest never started therefore still
exposes its stages, its objectives (with their target) and its rewards, at zero
progress. This is what allows a "rankup" menu that shows the full tree and the progress.

This assumes an objective's target is readable without an instance: `QuestObjectiveConfig`
declares `getTarget()`, and `AbstractQuestObjective` reads it from there, instead of receiving it as a
constructor argument. Catalog and live instance therefore cannot diverge.

**Editor preview from the draft.** The editor view computes the three sources from the quest
groups being edited (`editor/src/sourceRows.ts`, `FrontendModule.localPreviewRows`), so a quest
menu previews the draft before any deploy. The definition fields (titles, limits, stages,
objectives and their targets) come from the draft; the player's fields (status, progress,
counters, `period_end`) from the server's rows, matched by key (`ctx.serverRows`, Artisan editor
SDK 1.12). A quest the draft adds shows the neutral state, one it removes disappears; an
instance of `msquests:active` whose quest is gone from the draft is dropped. An objective keeps
the server's name while its type and target are unchanged, otherwise it is named from the
default `objective.<type>` texts (an admin's own wording in `lang/` shows after a deploy). The
`group` parameter narrows the rows like the fetcher (`ctx.params`). An older editor passes no
context: groups and quests show the draft at neutral state, `msquests:active` the server.
`testdata/sources/*.json` pins the definition fields both sides must produce:
`QuestSourceVectorsTest` (plugin) and `sourceRows.test.ts` (editor) read the same files.
The bundle still declares SDK 1.10, so it loads on an older Artisan.

Three commands callable from a menu button: `msquests:track <id>` (toggle),
`msquests:rotate <id>` and `msquests:contribute <id>`, where `<id>` is either an instance's UUID
or a `group:key`. `contribute` gives each ongoing objective that implements
`ContributableObjective` what the player can provide (inventory items for `deliver_item`,
money for a bank objective provided by a third-party plugin), never more than what is missing. The
progress is written right away (`progressObjectiveNow`), not batched: what the player
gave has already left their inventory.

**Raw rather than pre-formatted.** Formatting belongs to the menu. Rows
carry numbers (`objective_progress` / `objective_target`), lists
(`stages[]`) and absolute instants in epoch millis (`period_end`,
`expires_at`) — never an already composed string. On the Artisan side, the Template
recomposes what it wants:

```
{objective_progress}/{objective_target}
{period_end.until()}
```

`rewards_lore`, `countdown`, formatted `period_end` and `expires_in` were removed
on 2026-08-05: they only existed because the Artisan runtime could not
join a list or format a duration (ADR `expression-parity`).

**Constraint**: fetchers run on the main thread when the menu renders. They only read
the in-memory registries — never a repository, never I/O. An unloaded
actor (offline player) gives neutral values, not a wait.

Build: `artisan-core-api` is `compileOnly` (provided by the Artisan plugin, never shaded),
version `artisanApiVersion` in `gradle.properties`. Until 2.2.0 (conditions API,
artisanmc/artisan#88) is published on repo.artisanmc.net, produce it from `v2`
once #88 is merged, or from its branch:
`cd <artisan>/plugin && ./gradlew :artisan-api:publishToMavenLocal -PapiVersion=2.2.0`.

---

## Domain model

### Entity hierarchy

```
QuestGroupConfig (group template)
  |-- QuestConfig (quest template)
        |-- QuestStageConfig
              |-- QuestObjectiveConfig

QuestActor (player or global)
  |-- ActorQuestGroup (per-group tracking)
  |-- Quest (runtime instance)
        |-- QuestStage
              |-- QuestObjective (tracked progress)
```

### Quest lifecycle

```
QuestConfig         Quest              QuestStage           QuestObjective
(YAML template) --> (IN_PROGRESS) ---> (PARALLEL/SEQ) ----> (PENDING)
                                                             (IN_PROGRESS)
                                                             (COMPLETED)
                    (COMPLETED)  <--- all stages done  <--- all objectives done
                    (EXPIRED)    <--- duration/cron expiry
```

1. **Distribution**: `QuestDistributionService` selects the candidate quests (SEQUENTIAL or RANDOM strategy with tiers)
2. **Start**: `QuestLifecycleService.startQuest()` validates the preconditions (`QuestStartResult`) and creates the instance
3. **Progress**: Bukkit handlers detect game events -> `QuestProgressService.progressObjective()` with batching
4. **Objective completion**: When `progress >= target`, the objective is completed, the objective actions run
5. **Stage completion**: When all the stage's objectives are completed, the next stage activates
6. **Quest completion**: When the last stage is completed, the reward actions run
7. **Expiration**: `QuestLifecycleService.expireQuests()` checks `shouldExpire()` (duration or cron)
8. **Rotation**: `rotateQuest()` replaces an active quest with another candidate of the same group

### Actor types

- **`QuestPlayerActor`**: Individual quests per player
- **`QuestGlobalActor`**: Quests shared server-wide (all players contribute)

Actors own `ActorQuestGroup`s that track the quests per group and the rotations per period.

---

## Services layer

Services orchestrate all the business logic. They operate on the in-memory registries and delegate persistence to the async repositories.

| Service | Responsibility |
|---------|----------------|
| `QuestLifecycleService` | Start, complete, expire, rotate, distribute, refresh quests |
| `QuestProgressService` | Objective progress with batching (`PendingObjectiveProgress`) and flush |
| `QuestDistributionService` | Candidate selection, precondition validation (`canStartQuest`) |
| `QuestConfigService` | Config loading/reloading from the repository |
| `QuestService` | Quest CRUD via repository, registry management |
| `QuestActorService` | Actor loading, linking with profiles |
| `PlayerProfileService` | Player profile CRUD, linking with actors |
| `QuestPlayerService` (bukkit) | Full loading of a player (actor + profile + quests) |

---

## Event system

Two-level event-driven architecture:

```
Bukkit handler (BlockBreakEvent, etc.)
    |
    v
Core Service (QuestProgressService)
    |
    v  dispatch()
EventDispatcher (core interface)
    |
    v  (implementation)
BukkitEventDispatcher
    |
    v  callEvent()
Bukkit PluginManager (ObjectiveProgressEvent, QuestCompleteEvent, etc.)
```

**Core events (cancellable):**
- `CoreQuestStartEvent` / `CoreQuestStartedEvent`
- `CoreQuestCompletedEvent`
- `CoreQuestObjectiveProgressEvent` / `CoreQuestObjectiveProgressedEvent`
- `CoreQuestObjectiveCompletedEvent`

Each core event has its Bukkit mirror in `bukkit/event/`.

---

## Registries

In-memory storage with `ConcurrentHashMap` for thread safety:

| Registry | Key | Value | Role |
|----------|-----|-------|------|
| `QuestConfigRegistry` | `String` (group key) | `QuestGroupConfig` | Group configs loaded from YAML |
| `QuestRegistry` | `UUID` | `Quest` | All the active quests in memory |
| `QuestActorRegistry` | `UUID` | `QuestActor` | Loaded actors (connected players + global) |
| `PlayerProfileRegistry` | `UUID` | `PlayerProfile` | Connected player profiles |
| `ActorTypeRegistry` | `String` (type name) | `Class<? extends QuestActor>` | Registered actor types |

---

## Factories

Registry + factory pattern for polymorphism:

| Factory | Annotation | Registration | Creation |
|---------|------------|----------------|----------|
| `QuestFactory` | - | - | `Quest` from config, DTO, or parameters |
| `QuestObjectiveFactory` | `@ObjectiveType` | `register(configClass, objectiveClass, handler)` | `QuestObjectiveConfig` and `QuestObjective` from DTO |

The factories use JSON Schema validation (generated from `@ConfigField`) to validate configurations.

---

## Persistence

### Async pattern

All repositories return `CompletableFuture`. Execution happens on a single-thread `ExecutorService` (via `DbAccess.getSingleThreadExecutor()`).

```java
// SqlRepository base
protected <T> CompletableFuture<T> supplyAsync(Callable<T> callable) {
    return CompletableFuture.supplyAsync(() -> callable.call(), executor);
}
```

### Batched progress

`QuestProgressService` accumulates progress in a `PendingObjectiveProgress` map and flushes it periodically or when an objective reaches its target. This avoids a DB write on every micro-event.

### Atomic execution

`AtomicLocalQuestExecutor` uses a `ReentrantLock` per quest (identified by UUID) to guarantee that only one operation modifies a quest at a time. Execution happens on virtual threads.

---

## Objective types

Each objective type follows a 3-class pattern + registration:

| Type | Bukkit event | Specific config |
|------|-----------------|-------------------|
| `block_break` | `BlockBreakEvent` | material |
| `kill_entity` | `EntityDeathEvent` | entity type |
| `fishing` | `PlayerFishEvent` | item (`item` + `match`, legacy `fish_type`) |
| `execute_command` | `PlayerCommandPreprocessEvent` | command pattern |
| `craft_item` | `CraftItemEvent` | item (`item` + `match`, legacy `material`) |
| `breed_animal` | `EntityBreedEvent` | entity type |
| `deliver_item` | `msquests:contribute` (menu) | item (`item` + `match`, legacy `material`), amount — inventory then carried shulkers (`deliver_item.include_shulker_boxes` in config.yml) |
| `harvest_crop` | `BlockBreakEvent` | crop type |

An objective's **item** (`ItemTarget`) is an Artisan item spec: the fields of a menu button's appearance (`material`: vanilla, `item:<captured id>` or `nexo:<id>`…; `title`, `lore`, `enchants`, `custom_model_data`), built and compared by `api.getItems()` (Artisan API 2.4.0). `match` lists the criteria (`material`, `custom_item`, `name`, `lore`, `enchants`, `custom_model_data`, `damage`, `exact`); absent, those the item carries. A legacy bare material is compared on the material alone, and is rewritten as `item` + `match: [material]` on save. No item: any item.
| `placeholder` | Polling scheduler | placeholder, interval, condition |
| `travel` | `PlayerMoveEvent` | distance |

---

## Action types

No more action types of our own: rewards and hooks are Artisan chains (see
*Rewards, hooks and requirements*). The vocabulary is that of Artisan's core plus
the `quests:*` actions.

------|-------|
| `command` | Runs a server command |
| `player_command` | Runs a command as the player |
| `player_message` | Sends a chat message |
| `player_actionbar` | Shows an action bar message |
| `player_title` | Shows a title/subtitle |
| `player_bossbar` | Shows a boss bar |
| `give_item` | Gives an item |

All actions support dynamic placeholders via `PlaceholderEngine`.

---

## Configuration

| File | Content |
|---------|---------|
| `core/src/main/resources/config.yml` | Global config (DB, language, `content_source`) |
| `bukkit/src/main/resources/quests/*.yml` | Quest group definitions (Jackson YAML) |
| `core/src/main/resources/lang/*.yml` | Translation files |
| `plugin.yml` | Plugin descriptor (Paper, Folia, Spigot) |

Quest configs are loaded by `QuestConfigYamlRepository` with Jackson (`SNAKE_CASE` naming strategy),
or by `QuestGroupDocumentRepository` with `content_source: artisan`.

### Content source (`content_source`) and migration

Read at startup only; a change requires a restart (`/msquests reload` points it out).

| `content_source` | Content | Progress |
|---|---|---|
| `legacy` (default) | `quests/*.yml` | `msquests_*`, by `(group_key, quest_key)` |
| `artisan` | `<project>/quest_groups/<group>.yaml` of all Artisan projects (`quest_group/v1` Documents) | `artisan_quests_*`, by `uid` |

A Document carries an immutable `uid` (ULID) for the group and for each quest; the group
key is the file name, renamable like quest keys without losing progress.
`QuestGroupDocumentReader` produces the same `QuestGroupConfigDTO` as the YAML reader (`ltext`
texts resolved in the manifest's `default_lang`, `duration` as `24h`/`90m`, cron with
`reset.timezone`) and rejects the whole file on an error, with the path of the faulty value first:
missing or already taken `uid` (server-wide), repeated objective key in a
quest, etc. A group key is unique on the server, since commands and placeholders
name a group by its key alone: two projects carrying `daily.yaml` are reported with both
paths, and neither loads (a reload is then refused as a whole). Actions and conditions keep the MSQuests format for now (`type: ...`).

Moving a server over (no step touches `quests/*.yml` or `msquests_*`):

1. `/msquests migrate` (dry run): converts in memory, re-reads each produced Document and
   compares it with the original group, counts what the database copy will do, and writes
   `migration/report-<date>.txt`. Nothing else is written.
2. `/msquests migrate --confirm`: writes the missing Documents into the `quests` module folder
   (`plugins/Artisan/modules/quests/`, created if missing; never over
   an existing Document), reloads Artisan's projects, and writes `migration/map.yaml` (`(group, key) -> uid`, `objective -> stage`). Re-run,
   it keeps the same `uid`s.
3. `content_source: artisan` then restart: before any loading, `LegacyDataCopier`
   copies `msquests_*` into `artisan_quests_*` in one transaction (counts verified, otherwise
   rollback), only once. Without `map.yaml` while `msquests_quest` has rows, the
   plugin refuses to start.
4. `/msquests stats` before and after to compare. Rollback: `content_source: legacy`
   or the old jar; progress made in the meantime stays in `artisan_quests_*`.

---

## Key dependencies

| Dependency | Version | Module | Usage |
|------------|---------|--------|-------|
| Paper API | 1.21.8 | bukkit | Minecraft server API |
| Lamp | 4.0.0-rc.16 | bukkit | Annotation-driven command framework |
| BoostedYAML | 1.3.7 | bukkit | Config loading with auto-update |
| FoliaLib | 0.5.1 | bukkit | Folia-compatible scheduler abstraction |
| PlaceholderAPI | 2.11.6 | bukkit | Placeholders (optional) |
| zMenu | 1.1.1.0 | bukkit | GUI menu integration (optional) |
| Jackson | 2.20.0 | core, bukkit | JSON/YAML serialization |
| json-schema-validator | 1.5.9 | core, bukkit | Objective/action config validation |
| cron-utils | 9.2.1 | core | Cron expressions for reset/expiration |
| JDBI 3 | 3.49.6 | database | SQL abstraction |
| HikariCP | 5.1.0 | database | Connection pool |
| H2 | 2.3.232 | database | Embedded DB (dev/test) |
| MockBukkit | 4.41.0 | bukkit (test) | Bukkit server mock |
| Mockito | 5.14.2 | bukkit (test) | Mocking |
| TestContainers | 1.19.7 | database (test) | MySQL container for integration tests |

---

## Tests

| Type | Module | Tool | Prerequisite |
|------|--------|-------|-----------|
| Objective unit tests | bukkit | MockBukkit + Mockito | - |
| Config unit tests | bukkit, core | JUnit 5 | - |
| SQL integration tests | database | TestContainers MySQL | Docker |
| Utility tests | core | JUnit 5 | - |

**Base class**: `AbstractObjectiveHandlerTest` provides the MockBukkit setup, the plugin/registry mocks, and a `createObjective()` helper that builds the actor -> quest -> stage -> objective chain.

---

## Development guidelines

### Architecture principles

1. **Platform-agnostic core**: Never import Bukkit classes in `core/`. All platform interaction goes through `MSQuestsPlatform` or `EventDispatcher`.

2. **Separation of responsibilities**:
   - **Config** (`QuestConfig`, `QuestObjectiveConfig`) = immutable template loaded from YAML
   - **Instance** (`Quest`, `QuestObjective`) = mutable runtime state
   - **DTO** = transport between layers (persistence, serialization)
   - **Mapper** = conversion between domain and DTO

3. **Registries = in-memory source of truth**: The registries are the reference for the current state. The DB is persistence, not the source of truth at runtime.

4. **Persistence always async**: Never `CompletableFuture.join()` or `.get()` on the main thread. Use callbacks.

### Code conventions

5. **Thread safety**: Use `ConcurrentHashMap` for shared collections. `AtomicInteger` for counters. `AtomicQuestExecutor` for quest mutations.

6. **Type naming**:
   - Objective: `XxxObjective`, `XxxObjectiveConfig`, `XxxObjectiveHandler`

7. **Required annotations**:
   - `@ObjectiveType("type_key")` on `ObjectiveConfig` classes
   - `@ConfigField` on config fields for JSON schema generation

8. **Registration in the plugin**: Every new type (objective, actor) must be registered in `BukkitQuestsPlugin.onEnable()` via the matching factory.

### Adding an objective type

1. Create `XxxObjectiveConfig.java` in `bukkit/.../quest/objective/xxx/` with `@ObjectiveType(ObjectiveTypes.XXX)`
2. Create `XxxObjective.java` extends `AbstractQuestObjective<XxxObjectiveConfig>`
3. Create `XxxObjectiveHandler.java` extends `BukkitQuestObjectiveHandler<XxxObjective>` with the Bukkit listener
4. Add the constant in `ObjectiveTypes.java`
5. Register in `BukkitQuestsPlugin.registerObjectiveTypes()`

### Adding an action

An action usable in quest chains is declared on the Artisan side: in Artisan's
core if it is generic, otherwise as a `quests:*` action in `QuestActions`
(`ActionDeclaration` + handler, test in `QuestActionsTest`). No in-house condition:
conditions are Artisan's.

### Tests

9. **Every objective handler must have a test**: Extend `AbstractObjectiveHandlerTest`, use `createObjective()`, call the handler methods directly, verify with `Mockito.verify()` on `progressObjective`.

10. **SQL integration tests**: Use TestContainers. The tests require Docker. Run with `./gradlew :database:test`.

11. **Do not mock the DB in integration tests**: The SQL repositories must be tested against a real database (MySQL via TestContainers or H2).

### Scheduler

12. **Use FoliaLib**: Never use `Bukkit.getScheduler()`. Always go through `plugin.getScheduler()` (FoliaLib) for Folia compatibility.

### Shadow JAR and relocation

13. **Relocate new dependencies**: Every new runtime dependency in `bukkit/build.gradle` must have a relocation rule in `shadowJar` to avoid classpath conflicts with other plugins.

### Git and build

14. **Check the full build**: `./gradlew clean build` before committing. The shadow JAR is copied automatically to `docker/plugins/`.

15. **SQL migrations**: For any schema change, create a new `Migration` class with an incremented version number, and register it in `MigrationManager`.
