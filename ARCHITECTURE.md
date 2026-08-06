# Architecture - MSQuests

Ce document decrit l'architecture du projet MSQuests, un plugin Minecraft Paper 1.21+ de gestion de quetes.

---

## Vue d'ensemble

MSQuests est un systeme de quetes modulaire construit sur une architecture en couches avec separation stricte entre la logique metier (core) et la plateforme (Bukkit). Le projet utilise Gradle multi-modules et cible Java 21.

```
                 +-----------+
                 |  bukkit   |  Plugin Paper, commandes, listeners, handlers
                 +-----+-----+
                       |
              +--------+--------+
              |                 |
        +-----+-----+    +-----+-----+
        |   core    |    | database  |  Persistance SQL async
        +-----------+    +-----------+
        Domaine, services,    JDBI, HikariCP,
        registries, events    migrations
```

**Dependance entre modules :**
- `bukkit` depend de `core` et `database`
- `database` depend de `core` (via `api(project(":core"))`)
- `core` n'a aucune dependance vers les autres modules

---

## Modules

### `core/` - Logique metier

Package racine : `com.github.ibanetchep.msquests.core`

Contient toute la logique metier independante de la plateforme :

| Package | Responsabilite |
|---------|----------------|
| `quest/actor/` | Entites principales : `Quest`, `QuestStage`, `QuestActor`, `ActorQuestGroup` |
| `quest/player/` | `PlayerProfile` - profil joueur avec ses acteurs et quetes |
| `quest/config/` | Configurations : `QuestConfig`, `QuestStageConfig`, `QuestObjectiveConfig`, `QuestGroupConfig`, `QuestTierConfig` |
| `quest/config/annotation/` | `@ObjectiveType`, `@ActionType`, `@ConfigField`, `@AtLeastOneOfFields` |
| `quest/config/group/` | `QuestGroupConfig` avec builder pattern |
| `quest/objective/` | Interface `QuestObjective`, `AbstractQuestObjective<C>`, enums `QuestObjectiveStatus`, `Flow` |
| `quest/condition/` | `QuestObjectiveCondition` - conditions sur les objectifs |
| `quest/action/` | `QuestAction` - actions de recompense |
| `quest/executor/` | `AtomicQuestExecutor` / `AtomicLocalQuestExecutor` - execution thread-safe par quete |
| `quest/result/` | `QuestStartResult`, `QuestRotateResult` - enums de resultats |
| `service/` | Services metier (orchestration) |
| `factory/` | Factories avec registre de types |
| `registry/` | Registres en memoire (ConcurrentHashMap) |
| `event/` | `EventDispatcher`, `CoreEvent`, evenements du domaine |
| `dto/` | DTOs pour serialisation/persistance |
| `mapper/` | Mappers DTO <-> domaine |
| `lang/` | `Translator`, `Translatable`, `PlaceholderProvider` |
| `repository/` | Interfaces des repositories |
| `util/` | `CronUtils`, `JsonSchemaGenerator`, `JsonSchemaValidator` |

### `database/` - Persistance

Package racine : `com.github.ibanetchep.msquests.database`

| Classe/Package | Responsabilite |
|----------------|----------------|
| `DbAccess` | Pool de connexions HikariCP, instance JDBI, executor single-thread |
| `DbCredentials` | Record de configuration (H2 ou MySQL) |
| `MigrationManager` | Systeme de migrations sequentielles avec table `schema_version` |
| `migrations/` | `CreateTablesMigration` (v1), `AddRotationTableMigration` (v2) |
| `repository/` | `SqlRepository` (base async), implementations SQL pour Quest, PlayerProfile, Actor, Rotation |
| `RecordRowMapper` | Mapper JDBI generique pour Java records |

**Schema principal :**
```
msquests_player_profile (id, name, tracked_quest_id, created_at, updated_at)
msquests_actor          (id, actor_type, created_at, updated_at)
msquests_quest          (id, quest_key, group_key, status, actor_id FK, timestamps)
msquests_objective      (objective_key, quest_id FK, status, type, progress) -- PK composite
msquests_rotation       (actor_id, group_key, rotated_at)
```

### `bukkit/` - Integration Paper

Package racine : `com.github.ibanetchep.msquests.bukkit`

| Package | Responsabilite |
|---------|----------------|
| `BukkitQuestsPlugin` | Point d'entree, implemente `MSQuestsPlatform`, cablage de tous les composants |
| `command/` | Commandes Lamp : `QuestCommand`, `QuestAdminCommand`, parameter types custom |
| `listener/` | `ServerLoadListener` (chargement configs), `PlayerJoinListener` (chargement joueur) |
| `event/` | `BukkitEventDispatcher` + evenements Bukkit miroirs des core events |
| `quest/objective/` | 10 handlers d'objectifs (un sous-package par type) |
| `quest/action/` | 7 actions Bukkit (commandes, messages, titres, items, bossbar) |
| `quest/actor/` | `BukkitQuestPlayerActor`, `BukkitQuestGlobalActor` |
| `quest/condition/` | `WorldCondition`, `BiomeCondition` |
| `config/` | `GlobalConfig`, `GlobalConfigLoaderService` |
| `repository/` | `QuestConfigYamlRepository` (Jackson YAML) |
| `service/` | `QuestPlayerService` (chargement joueurs) |
| `lang/` | `BukkitTranslator` |
| `text/` | `PlaceholderEngine`, `MessageBuilder` |
| `placeholderapi/` | `QuestsPlaceholderExpansion` |
| `zmenu/` | Integration zMenu (pagination de groupes de quetes) |
| `artisan/` | Integration Artisan (data sources + commandes de menu) |

---

## Integration Artisan

Optionnelle (softdepend). Quand le plugin Artisan est present, `ArtisanIntegration`
enregistre `MsQuestsArtisanModule` via le `ServicesManager`, ce qui expose les quetes
aux menus de l'editeur web Artisan. Sans Artisan, rien ne change.

| Data source | Stability | Contenu |
|---|---|---|
| `msquests:groups` | STATIC (`key`) | Groupes de quetes + compteurs de l'acteur, `quests[]` imbriquees |
| `msquests:quests` | STATIC (`id` = `group:key`) | Catalogue complet : config + `rewards[]` + `stages[] > objectives[]`, avec la progression de l'acteur superposee |
| `msquests:active` | DYNAMIC | Instances en cours de l'acteur, objectif par objectif |

Trois parametres, communs aux trois sources :

- `player` — cibler un autre joueur que celui qui ouvre le menu (par nom, doit etre en ligne)
- `actor` — quel acteur lire : `player` (defaut), `global`, ou tout autre type enregistre
- `group` — restreindre a un groupe (`msquests:quests`, `msquests:active`)

`ActorResolver` (dans `core/registry/`) resout (joueur, type d'acteur) via
`QuestActor#isMember`, jamais par identite : un futur acteur guilde fonctionne sans
toucher a l'integration. Il vit dans `core` parce qu'il ne doit rien a Artisan —
commandes, placeholders et menus posent la meme question.

De meme, `ActorQuestGroup#getCurrentAttempt(questKey)` repond a « la tentative courante
de l'acteur sur cette quete » (instance active, sinon la plus recente non-expiree de la
periode). C'est la question que toutes les surfaces d'affichage posent ; elle est
resolue une fois, dans le domaine.

**Catalogue = config + progression superposee.** `msquests:quests` est keyee sur les
**configs**, pas sur les instances : toutes les quetes d'un groupe apparaissent, demarrees
ou non, et l'etat du joueur est pose par-dessus. Une quete jamais commencee expose donc
quand meme ses stages, ses objectifs (avec leur cible) et ses recompenses, a progression
zero. C'est ce qui permet un menu « rankup » qui montre l'arbre complet et l'avancement.

Cela suppose que la cible d'un objectif soit lisible sans instance : `QuestObjectiveConfig`
declare `getTarget()`, et `AbstractQuestObjective` la lit de la, au lieu de la recevoir en
argument de constructeur. Catalogue et instance live ne peuvent donc pas diverger.

Deux commandes appelables depuis un bouton de menu : `msquests:track <id>` (toggle) et
`msquests:rotate <id>`, ou `<id>` est soit l'UUID d'une instance, soit un `group:key`.

**Brut plutot que pre-formate.** La mise en forme appartient au menu. Les lignes
portent des nombres (`objective_progress` / `objective_target`), des listes
(`rewards[]`) et des instants absolus en epoch millis (`period_end`,
`expires_at`) — jamais de chaine deja composee. Cote Artisan, le Template
recompose ce qu'il veut :

```
{objective_progress}/{objective_target}
{rewards.pluck("name").join("
")}
{period_end.until()}
```

`rewards_lore`, `countdown`, `period_end` formate et `expires_in` ont ete retires
le 2026-08-05 : ils n'existaient que parce que le runtime Artisan ne savait pas
joindre une liste ni formater une duree (ADR `expression-parity`).

**Contrainte** : les fetchers tournent sur le main thread au rendu du menu. Ils lisent
uniquement les registres memoire — jamais de repository, jamais d'I/O. Un acteur non
charge (joueur hors ligne) donne des valeurs neutres, pas une attente.

Build : `artisan-core-api` est `compileOnly` (fourni par le plugin Artisan, jamais shade).
Le produire avec `cd <Artisan>/plugin && ./gradlew publishToMavenLocal`.

---

## Modele de domaine

### Hierarchie des entites

```
QuestGroupConfig (template de groupe)
  |-- QuestConfig (template de quete)
        |-- QuestStageConfig
              |-- QuestObjectiveConfig

QuestActor (joueur ou global)
  |-- ActorQuestGroup (suivi par groupe)
  |-- Quest (instance runtime)
        |-- QuestStage
              |-- QuestObjective (progression trackee)
```

### Cycle de vie d'une quete

```
QuestConfig         Quest              QuestStage           QuestObjective
(YAML template) --> (IN_PROGRESS) ---> (PARALLEL/SEQ) ----> (PENDING)
                                                             (IN_PROGRESS)
                                                             (COMPLETED)
                    (COMPLETED)  <--- all stages done  <--- all objectives done
                    (EXPIRED)    <--- duration/cron expiry
```

1. **Distribution** : `QuestDistributionService` selectionne les quetes candidates (strategie SEQUENTIAL ou RANDOM avec tiers)
2. **Demarrage** : `QuestLifecycleService.startQuest()` valide les preconditions (`QuestStartResult`) et cree l'instance
3. **Progression** : Les handlers Bukkit detectent les evenements de jeu -> `QuestProgressService.progressObjective()` avec batching
4. **Completion objectif** : Quand `progress >= target`, l'objectif est complete, les actions d'objectif s'executent
5. **Completion stage** : Quand tous les objectifs du stage sont completes, le stage suivant s'active
6. **Completion quete** : Quand le dernier stage est complete, les actions de recompense s'executent
7. **Expiration** : `QuestLifecycleService.expireQuests()` verifie `shouldExpire()` (duree ou cron)
8. **Rotation** : `rotateQuest()` remplace une quete active par une autre candidate du meme groupe

### Types d'acteurs

- **`QuestPlayerActor`** : Quetes individuelles par joueur
- **`QuestGlobalActor`** : Quetes partagees server-wide (tous les joueurs contribuent)

Les acteurs possedent des `ActorQuestGroup` qui trackent les quetes par groupe et les rotations par periode.

---

## Couche Services

Les services orchestrent toute la logique metier. Ils operent sur les registres en memoire et delegent la persistance aux repositories async.

| Service | Responsabilite |
|---------|----------------|
| `QuestLifecycleService` | Start, complete, expire, rotate, distribute, refresh des quetes |
| `QuestProgressService` | Progression des objectifs avec batching (`PendingObjectiveProgress`) et flush |
| `QuestDistributionService` | Selection de candidates, validation des preconditions (`canStartQuest`) |
| `QuestConfigService` | Chargement/reload des configs depuis le repository |
| `QuestService` | CRUD quetes via repository, gestion du registre |
| `QuestActorService` | Chargement des acteurs, liaison avec profils |
| `PlayerProfileService` | CRUD profils joueurs, liaison avec acteurs |
| `QuestPlayerService` (bukkit) | Chargement complet d'un joueur (acteur + profil + quetes) |

---

## Systeme d'evenements

Architecture event-driven a deux niveaux :

```
Handler Bukkit (BlockBreakEvent, etc.)
    |
    v
Core Service (QuestProgressService)
    |
    v  dispatch()
EventDispatcher (interface core)
    |
    v  (implementation)
BukkitEventDispatcher
    |
    v  callEvent()
Bukkit PluginManager (ObjectiveProgressEvent, QuestCompleteEvent, etc.)
```

**Evenements core (cancellables) :**
- `CoreQuestStartEvent` / `CoreQuestStartedEvent`
- `CoreQuestCompletedEvent`
- `CoreQuestObjectiveProgressEvent` / `CoreQuestObjectiveProgressedEvent`
- `CoreQuestObjectiveCompletedEvent`

Chaque evenement core a son miroir Bukkit dans `bukkit/event/`.

---

## Registres

Stockage en memoire avec `ConcurrentHashMap` pour la thread safety :

| Registre | Cle | Valeur | Role |
|----------|-----|--------|------|
| `QuestConfigRegistry` | `String` (group key) | `QuestGroupConfig` | Configs des groupes charges depuis YAML |
| `QuestRegistry` | `UUID` | `Quest` | Toutes les quetes actives en memoire |
| `QuestActorRegistry` | `UUID` | `QuestActor` | Acteurs charges (joueurs connectes + globaux) |
| `PlayerProfileRegistry` | `UUID` | `PlayerProfile` | Profils joueurs connectes |
| `ActorTypeRegistry` | `String` (type name) | `Class<? extends QuestActor>` | Types d'acteurs enregistres |

---

## Factories

Pattern registre + factory pour le polymorphisme :

| Factory | Annotation | Enregistrement | Creation |
|---------|------------|----------------|----------|
| `QuestFactory` | - | - | `Quest` depuis config, DTO, ou parametres |
| `QuestObjectiveFactory` | `@ObjectiveType` | `register(configClass, objectiveClass, handler)` | `QuestObjectiveConfig` et `QuestObjective` depuis DTO |
| `QuestActionFactory` | `@ActionType` | `register(actionClass, factory)` | `QuestAction` depuis DTO |
| `QuestObjectiveConditionFactory` | - | `register(type, factory)` | `QuestObjectiveCondition` depuis DTO |

Les factories utilisent la validation JSON Schema (generee depuis `@ConfigField`) pour valider les configurations.

---

## Persistance

### Pattern async

Tous les repositories retournent `CompletableFuture`. L'execution se fait sur un `ExecutorService` single-thread (via `DbAccess.getSingleThreadExecutor()`).

```java
// SqlRepository base
protected <T> CompletableFuture<T> supplyAsync(Callable<T> callable) {
    return CompletableFuture.supplyAsync(() -> callable.call(), executor);
}
```

### Progression batchee

`QuestProgressService` accumule les progressions dans une map `PendingObjectiveProgress` et les flush periodiquement ou quand un objectif atteint sa target. Cela evite des ecritures DB a chaque micro-evenement.

### Execution atomique

`AtomicLocalQuestExecutor` utilise un `ReentrantLock` par quete (identifiee par UUID) pour garantir qu'une seule operation modifie une quete a la fois. L'execution se fait sur des virtual threads.

---

## Types d'objectifs

Chaque type d'objectif suit un pattern en 3 classes + registration :

| Type | Evenement Bukkit | Config specifique |
|------|-----------------|-------------------|
| `block_break` | `BlockBreakEvent` | material |
| `kill_entity` | `EntityDeathEvent` | entity type |
| `fishing` | `PlayerFishEvent` | - |
| `execute_command` | `PlayerCommandPreprocessEvent` | command pattern |
| `craft_item` | `CraftItemEvent` | material |
| `breed_animal` | `EntityBreedEvent` | entity type |
| `deliver_item` | Integration custom | target NPC |
| `harvest_crop` | `BlockBreakEvent` | crop type |
| `placeholder` | Polling scheduler | placeholder, interval, condition |
| `travel` | `PlayerMoveEvent` | distance |

---

## Types d'actions

| Type | Effet |
|------|-------|
| `command` | Execute une commande serveur |
| `player_command` | Execute une commande en tant que joueur |
| `player_message` | Envoie un message chat |
| `player_actionbar` | Affiche un message action bar |
| `player_title` | Affiche un titre/sous-titre |
| `player_bossbar` | Affiche une boss bar |
| `give_item` | Donne un item |

Toutes les actions supportent les placeholders dynamiques via `PlaceholderEngine`.

---

## Configuration

| Fichier | Contenu |
|---------|---------|
| `bukkit/src/main/resources/config.yml` | Config globale (BDD, langue) |
| `bukkit/src/main/resources/quests/*.yml` | Definitions de groupes de quetes (Jackson YAML) |
| `core/src/main/resources/lang/*.yml` | Fichiers de traduction |
| `paper-plugin.yml` | Descripteur plugin Paper |

Les configs de quetes sont chargees par `QuestConfigYamlRepository` avec Jackson (`SNAKE_CASE` naming strategy).

---

## Dependencies cles

| Dependance | Version | Module | Usage |
|------------|---------|--------|-------|
| Paper API | 1.21.8 | bukkit | API serveur Minecraft |
| Lamp | 4.0.0-rc.16 | bukkit | Framework de commandes annotation-driven |
| BoostedYAML | 1.3.7 | bukkit | Chargement config avec auto-update |
| FoliaLib | 0.5.1 | bukkit | Abstraction scheduler compatible Folia |
| PlaceholderAPI | 2.11.6 | bukkit | Placeholders (optionnel) |
| zMenu | 1.1.1.0 | bukkit | Integration menus GUI (optionnel) |
| Jackson | 2.20.0 | core, bukkit | Serialisation JSON/YAML |
| json-schema-validator | 1.5.9 | core, bukkit | Validation des configs d'objectifs/actions |
| cron-utils | 9.2.1 | core | Expressions cron pour reset/expiration |
| JDBI 3 | 3.49.6 | database | Abstraction SQL |
| HikariCP | 5.1.0 | database | Pool de connexions |
| H2 | 2.3.232 | database | BDD embarquee (dev/test) |
| MockBukkit | 4.41.0 | bukkit (test) | Mock du serveur Bukkit |
| Mockito | 5.14.2 | bukkit (test) | Mocking |
| TestContainers | 1.19.7 | database (test) | Container MySQL pour tests d'integration |

---

## Tests

| Type | Module | Outil | Prerequis |
|------|--------|-------|-----------|
| Tests unitaires objectifs | bukkit | MockBukkit + Mockito | - |
| Tests unitaires config | bukkit, core | JUnit 5 | - |
| Tests d'integration SQL | database | TestContainers MySQL | Docker |
| Tests utilitaires | core | JUnit 5 | - |

**Classe de base** : `AbstractObjectiveHandlerTest` fournit le setup MockBukkit, les mocks du plugin/registries, et un helper `createObjective()` qui construit la chaine actor -> quest -> stage -> objective.

---

## Guidelines de developpement

### Principes d'architecture

1. **Core platform-agnostic** : Ne jamais importer de classes Bukkit dans `core/`. Toute interaction plateforme passe par `MSQuestsPlatform` ou `EventDispatcher`.

2. **Separation des responsabilites** :
   - **Config** (`QuestConfig`, `QuestObjectiveConfig`) = template immutable charge depuis YAML
   - **Instance** (`Quest`, `QuestObjective`) = etat runtime mutable
   - **DTO** = transport entre couches (persistance, serialisation)
   - **Mapper** = conversion entre domaine et DTO

3. **Registres = source de verite en memoire** : Les registres sont la reference pour l'etat courant. La BDD est la persistance, pas la source de verite a runtime.

4. **Persistence toujours async** : Jamais de `CompletableFuture.join()` ou `.get()` sur le thread principal. Utiliser les callbacks.

### Conventions de code

5. **Thread safety** : Utiliser `ConcurrentHashMap` pour les collections partagees. `AtomicInteger` pour les compteurs. `AtomicQuestExecutor` pour les mutations de quetes.

6. **Nommage des types** :
   - Objectif : `XxxObjective`, `XxxObjectiveConfig`, `XxxObjectiveHandler`
   - Action : `XxxAction` avec `@ActionType("xxx")`
   - Condition : `XxxCondition` avec type string

7. **Annotations obligatoires** :
   - `@ObjectiveType("type_key")` sur les classes `ObjectiveConfig`
   - `@ActionType("type_key")` sur les classes `Action`
   - `@ConfigField` sur les champs de config pour la generation de schema JSON

8. **Enregistrement dans le plugin** : Tout nouveau type (objectif, action, condition, acteur) doit etre enregistre dans `BukkitQuestsPlugin.onEnable()` via la factory correspondante.

### Ajout d'un type d'objectif

1. Creer `XxxObjectiveConfig.java` dans `bukkit/.../quest/objective/xxx/` avec `@ObjectiveType(ObjectiveTypes.XXX)`
2. Creer `XxxObjective.java` extends `AbstractQuestObjective<XxxObjectiveConfig>`
3. Creer `XxxObjectiveHandler.java` extends `BukkitQuestObjectiveHandler<XxxObjective>` avec le listener Bukkit
4. Ajouter la constante dans `ObjectiveTypes.java`
5. Enregistrer dans `BukkitQuestsPlugin.registerObjectiveTypes()`

### Ajout d'un type d'action

1. Creer `XxxAction.java` extends `BukkitQuestAction` avec `@ActionType("xxx")`
2. Enregistrer dans `BukkitQuestsPlugin.registerActionTypes()`

### Ajout d'une condition

1. Creer `XxxCondition.java` extends `BukkitObjectiveCondition`
2. Enregistrer dans `BukkitQuestsPlugin.registerConditionTypes()`

### Tests

9. **Chaque handler d'objectif doit avoir un test** : Etendre `AbstractObjectiveHandlerTest`, utiliser `createObjective()`, appeler les methodes du handler directement, verifier avec `Mockito.verify()` sur `progressObjective`.

10. **Tests d'integration SQL** : Utiliser TestContainers. Les tests necessitent Docker. Lancer avec `./gradlew :database:test`.

11. **Ne pas mocker la BDD dans les tests d'integration** : Les repositories SQL doivent etre testes contre une vraie base (MySQL via TestContainers ou H2).

### Scheduler

12. **Utiliser FoliaLib** : Ne jamais utiliser `Bukkit.getScheduler()`. Toujours passer par `plugin.getScheduler()` (FoliaLib) pour la compatibilite Folia.

### Shadow JAR et relocation

13. **Relocater les nouvelles dependances** : Toute nouvelle dependance runtime dans `bukkit/build.gradle` doit avoir une regle de relocation dans `shadowJar` pour eviter les conflits de classpath avec d'autres plugins.

### Git et build

14. **Verifier le build complet** : `./gradlew clean build` avant de commit. Le shadow JAR est copie automatiquement dans `docker/plugins/`.

15. **Migrations SQL** : Pour tout changement de schema, creer une nouvelle classe `Migration` avec un numero de version incremente, et l'enregistrer dans `MigrationManager`.
