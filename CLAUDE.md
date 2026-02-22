# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew build          # Build all modules
./gradlew clean build    # Clean rebuild
./gradlew shadowJar      # Build deployable plugin JAR (bukkit module)
./gradlew test           # Run all tests
./gradlew :core:test     # Run tests in a specific module
```

The shadow JAR is automatically copied to `docker/plugins/` on build.

## Project Structure

This is a Gradle multi-module project targeting Paper 1.21+ (Minecraft plugin):

- **`core/`** — Platform-agnostic quest logic: domain model, services, registries, factories, DTOs, mappers, event dispatching abstraction
- **`bukkit/`** — Paper/Bukkit integration: plugin entry point, commands, listeners, Bukkit-specific objective handlers, actions, PlaceholderAPI
- **`database/`** — SQL persistence: JDBI + HikariCP, schema migrations, async repositories (MySQL or H2)

Root package: `com.github.ibanetchep.msquests`

## Architecture

### Layered Design

The platform abstraction (`MSQuestsPlatform`) decouples core logic from Bukkit. `BukkitQuestsPlugin` implements this interface and wires everything together at startup.

**Core event flow:** Bukkit listeners call core services → services dispatch core events via `EventDispatcher` → `BukkitEventDispatcher` translates them to Bukkit events.

### Key Patterns

**Registries (in-memory store):** `QuestRegistry`, `QuestConfigRegistry`, `QuestActorRegistry`, `PlayerProfileRegistry`, `ActorTypeRegistry`. `QuestRegistry` has dirty tracking to avoid unnecessary DB writes.

**Factories:** `QuestFactory`, `QuestObjectiveFactory`, `QuestActionFactory` — create domain objects from config or DTOs.

**Services orchestrate everything:**
- `QuestLifecycleService` — start, complete, expire quests
- `QuestProgressService` — advance objective progress, trigger stage transitions
- `QuestDistributionService` — assign quests to actors (player or global)
- `QuestConfigService` — load/reload YAML quest configs
- `QuestActorService`, `PlayerProfileService`, `QuestService` — CRUD via repositories

**Persistence is async:** All repository methods return `CompletableFuture`. Repositories in `database/` use JDBI. `QuestRegistry.markDirty()` flags quests that need to be persisted.

### Quest Lifecycle

`QuestConfig` (YAML template) → `Quest` (runtime instance, owned by a `QuestActor`) → `QuestStage` with `QuestObjective`s → objectives progress → stage completes → next stage activates → final stage completes quest → actions execute.

Two actor types: `QuestPlayerActor` (per-player) and `QuestGlobalActor` (server-wide).

### Adding a New Objective Type

1. Create a class in `core/.../quest/objective/` extending `QuestObjective`
2. Create a handler in `bukkit/.../quest/objective/` extending the Bukkit handler base; register its Bukkit listener
3. Register the type in the `ActorTypeRegistry` or the objective factory as appropriate
4. Add the YAML deserialization mapping in `QuestObjectiveFactory`

### Adding a New Action Type

1. Create a class in `core/.../quest/action/` (or `bukkit/.../quest/action/`)
2. Register it in `QuestActionFactory`

## Configuration

- `core/src/main/resources/config.yml` — database settings, language, actor type action hooks
- Quest YAML configs live in `bukkit/src/main/resources/quests/` and are loaded via `QuestConfigYamlRepository`
- Language files: `core/src/main/resources/lang/`

## Testing

- JUnit 5 with TestContainers (MySQL integration tests) and MockBukkit
- Tests for repositories require Docker (MySQL container spun up automatically)

## Key Dependencies

- **Lamp** — command framework (annotation-driven, see `QuestCommand`, `QuestAdminCommand`)
- **BoostedYAML** — YAML config parsing
- **Jackson** — JSON/YAML for DTOs and serialization
- **JDBI 3** — SQL query mapping
- **FoliaLib** — Folia-compatible scheduler abstraction (use instead of `Bukkit.getScheduler()`)
- **PlaceholderAPI** — optional; expansion registered in `QuestsPlaceholderExpansion`
