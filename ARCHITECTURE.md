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
| `quest/config/annotation/` | `@ObjectiveType`, `@ConfigField`, `@AtLeastOneOfFields` |
| `quest/config/action/` | `ActionChain` (chaine Artisan opaque), port `ChainRunner`, `ChainContext` (bindings) |
| `quest/config/condition/` | `Conditions` (conditions Artisan opaques), port `ConditionChecker` |
| `quest/config/group/` | `QuestGroupConfig` avec builder pattern |
| `quest/objective/` | Interface `QuestObjective`, `AbstractQuestObjective<C>`, enums `QuestObjectiveStatus`, `Flow` |
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
| `migrations/` | `CreateTablesMigration` (v1), `AddRotationTableMigration` (v2), `CreateArtisanTablesMigration` (v3, tables `artisan_quests_*`) |
| `SqlTables` | Noms des tables d'un layout : `LEGACY` (`msquests_*`) ou `ARTISAN` (`artisan_quests_*`) |
| `LegacyDataCopier` | Copie unique et transactionnelle de `msquests_*` vers `artisan_quests_*` (voir *Source du contenu*) |
| `repository/` | `SqlRepository` (base async), implementations SQL pour Quest, PlayerProfile, Actor, Rotation ; `ArtisanQuestSqlRepository` pour `artisan_quests_quest/objective` |
| `RecordRowMapper` | Mapper JDBI generique pour Java records |

**Schema principal :**
```
msquests_player_profile (id, name, tracked_quest_id, created_at, updated_at)
msquests_actor          (id, actor_type, created_at, updated_at)
msquests_quest          (id, quest_key, group_key, status, actor_id FK, timestamps)
msquests_objective      (objective_key, quest_id FK, status, type, progress) -- PK composite
msquests_rotation       (actor_id, group_key, rotated_at)

-- v3, utilisees avec content_source: artisan (les msquests_* restent intactes)
artisan_quests_profile   (comme msquests_player_profile)
artisan_quests_actor     (comme msquests_actor)
artisan_quests_quest     (id, quest_uid, quest_key, group_key, status, actor_id FK, timestamps)
artisan_quests_objective (quest_id FK, stage_key, objective_key, status, type, progress) -- PK composite
artisan_quests_rotation  (actor_id, group_uid, rotated_at)
artisan_quests_data_copy (une ligne une fois la copie faite)
```

`quest_key` / `group_key` de `artisan_quests_quest` ne sont que les derniers noms
enregistres (logs, support) : une ligne est resolue par `quest_uid`.

### `bukkit/` - Integration Paper

Package racine : `com.github.ibanetchep.msquests.bukkit`

| Package | Responsabilite |
|---------|----------------|
| `BukkitQuestsPlugin` | Point d'entree, implemente `MSQuestsPlatform`, cablage de tous les composants |
| `command/` | Commandes Lamp : `QuestCommand`, `QuestAdminCommand`, parameter types custom |
| `listener/` | `ServerLoadListener` (chargement configs), `PlayerJoinListener` (chargement joueur) |
| `event/` | `BukkitEventDispatcher` + evenements Bukkit miroirs des core events |
| `quest/objective/` | 10 handlers d'objectifs (un sous-package par type) |
| `quest/actor/` | `BukkitQuestPlayerActor`, `BukkitQuestGlobalActor` |
| `config/` | `GlobalConfig`, `GlobalConfigLoaderService` |
| `repository/` | `QuestConfigYamlRepository` (Jackson YAML, `quests/*.yml`) |
| `document/` | Documents `quest_group` : `QuestGroupDocumentReader`/`Writer`, `QuestGroupDocumentRepository`, `LegacyContentConverter`, `MigrationMapFile` |
| `migration/` | `ContentMigration` (`/msquests migrate`) |
| `service/` | `QuestPlayerService` (chargement joueurs) |
| `lang/` | `BukkitTranslator` (fichier tenu a jour par BoostedYAML, cles servies par les traductions d'Artisan, bundle `msquests`) |
| `text/` | `PlaceholderEngine`, `MessageBuilder` |
| `placeholder/` | `QuestPlaceholders` (namespace Artisan `quests`) |
| `placeholderapi/` | `QuestsPlaceholderExpansion` (alias PAPI `msquests`) |
| `zmenu/` | Integration zMenu (pagination de groupes de quetes) |
| `artisan/` | Integration Artisan (projet `editor/`, kind `quest_group`, actions `quests:*`, `ArtisanChainRunner`, `ArtisanConditionChecker`, data sources, commandes de menu) |

---

## Integration Artisan

**Obligatoire** (`required: true` dans `paper-plugin.yml`, API Artisan 2.6.0+). Au tout
debut de `onEnable`, `ArtisanIntegration.resolve` charge `ArtisanAPI` depuis le
`ServicesManager` ; absente ou trop ancienne, le plugin se desactive avec un message
SEVERE, avant d'ouvrir la base. Une fois les services cables, `MsQuestsArtisanModule`
est enregistre et declare :

- **un modele d'exemple** `daily` (Artisan 2.6.0, `api.getTemplates()`, fichiers dans
  `bukkit/src/main/resources/artisan-templates/daily/`) : un groupe `example_daily` de trois
  quetes journalieres, que l'editeur propose de copier dans n'importe quel projet (**+ → Depuis
  un module…**). Au premier demarrage, Artisan le copie une seule fois dans un nouveau projet
  ordinaire `quests` (`plugins/Artisan/projects/quests/`, `EditorProject`) ; supprime, il ne
  revient pas. Monter `TEMPLATE_VERSION` a chaque changement de ces fichiers : l'editeur propose
  alors la mise a jour aux copies non modifiees. L'ancien projet du module,
  `plugins/MSQuests/editor/`, est deplace une fois vers `plugins/Artisan/projects/quests/` s'il
  contient des groupes et que ce projet n'existe pas encore ;
- **le kind de Document `quest_group`** (dossier `quest_groups/`, schema `quest_group/v1`) :
  un Document par groupe, nomme d'apres la cle du groupe, dans **n'importe quel projet**
  Artisan (`ArtisanProjectFiles` sur `api.getProject()`), pas seulement celui du module. Les
  chaines d'actions d'un groupe s'executent dans le projet qui porte son Document, pour que ses
  references de menu s'y resolvent. Les quetes s'y chargent avec
  `content_source: artisan` (voir *Source du contenu*) ; sinon depuis `quests/*.yml`.
  `onReload` (publication, `/artisan reload`) recharge les quetes quand ces Documents ont
  change (empreinte SHA-256), et ne fait rien sinon ;
- **sa vue editeur** `artisan-editor/ui.js` (Artisan 2.3.0+, sinon un avertissement et pas de
  vue) : le dossier `editor/` du depot (TypeScript + React, contre `@artisan/editor-sdk`, pris
  dans `editor/vendor/` tant que le SDK n'est pas publie) la construit dans
  `bukkit/src/main/resources/artisan-editor/` (`cd editor && npm ci && npm run build`, sortie
  versionnee : le build Gradle n'a pas besoin de Node). Artisan la sert a l'editeur, qui la
  charge pour le kind `quest_group` : vue du groupe puis d'une quete (etapes, objectifs,
  recompenses `run_command`, textes dans le panneau Traductions). `EDITOR_SDK` doit egaler la
  version du SDK que le build affiche ;
- **les actions `quests:*`** (`QuestActions`), utilisables dans toute chaine Artisan :

| Action | Arguments | Effet |
|---|---|---|
| `quests:start` | `group`, `quest` | Demarre la quete pour l'acteur du joueur, avec les regles du groupe (limites, requirements) |
| `quests:abandon` | `group`, `quest` | Abandonne l'instance active : elle passe `FAILED` et reste comptee dans la periode ; suivie, elle ne l'est plus |
| `quests:track` | `group`, `quest` | Suit / ne suit plus l'instance active (comme `msquests:track`) |
| `quests:rotate` | `group`, `quest` | Echange l'instance active contre une autre du groupe |
| `quests:complete` | `group`, `quest` | Termine l'instance active, recompenses comprises |
| `quests:distribute` | `group` (primaire), `amount?` | Distribue `amount` quetes, sinon applique la distribution du groupe |
| `quests:action_bar` | `text` (primaire, ltext) | Affiche le texte en action bar (le core d'Artisan n'en a pas) |
| `quests:boss_bar` | `text` (primaire), `color`, `style`, `show_progress`, `duration` | Boss bar par joueur, mise a jour en place, masquee `duration` s apres le dernier appel ; remplie avec `progress_ratio` de la chaine |

`group` est un `document_ref` vers `quest_group` : l'editeur propose un selecteur, trace
l'arete et suit un renommage. Sa valeur est la cle du groupe (une reference qualifiee
`projet:daily` est reduite a `daily`), donc elle vaut aussi pour un groupe encore lu depuis
`quests/*.yml`, mais le selecteur reste vide tant qu'aucun Document `quest_group` n'existe.
`quest` est la cle de la quete dans le groupe. Les deux passent par `ActionRuntime.text`,
donc `{key}` d'une ligne iteree est interpole. L'acteur vise est celui du type d'acteur
du groupe auquel le joueur appartient ; une donnee inconnue logge un warning et la chaine
continue.

### Recompenses, crochets et requirements : chaines Artisan

Les crochets d'une quete (`actions.start`, `objective_progress`, `objective_complete`, `complete`,
chacun execute apres le crochet du groupe pour le meme evenement) et les 7 crochets du groupe (cle `actions` ; anciens noms `on` et `rewards`, encore lus) (`actions.quest_start`, `quest_complete`,
`objective_progress`, `objective_complete`, `quest_distribution`, `actor_load`,
`all_period_quests_complete`) sont des chaines Artisan (wire structure : `{action: args}`,
`if/do/else`). Le core les porte en `ActionChain` opaque et les execute via le port
`ChainRunner` ; `ArtisanChainRunner` les passe a `ModuleActionsApi.run` dans le projet
`quests`, **une fois par membre en ligne de l'acteur** (un groupe global ou guilde execute
donc une commande console par membre), apres un saut sur le main thread si besoin.

Bindings (`ChainContext#args`) : `actor`, `actor_name`, `group`, `group_title`, `quest`,
`quest_title`, `objective`, `objective_title`, `progress`, `amount`, `progress_ratio`,
`objectives` (chaque objectif rendu avec `objective_line` du groupe, defaut
`<newline> - {objective_name}`), `tracked` (par joueur), plus tous les anciens placeholders
MSQuests (`quest_name`, `objective_progress`, `quest_tier_name`...).

Il n'y a plus de liste de recompenses a afficher : ce que gagne le joueur s'ecrit dans la
description de la quete ou dans l'item du menu. La conversion legacy ajoute les `name` des
anciennes actions a la fin de la description, et `%quest_rewards%` (zMenu) vaut vide. Les `requirements` d'une quete et les `conditions` d'un
objectif sont des conditions Artisan (`Conditions`), testees via `ConditionChecker` /
`ArtisanConditionChecker` (`ConditionsApi.test`) ; un joueur hors ligne ou un appel hors
de son thread vaut faux. Elles sont validees (`ConditionsApi.validate`) a chaque
chargement, les erreurs loggees.

Les actions et conditions maison (`command`, `message`, `title`, `boss_bar`,
`permission`, `placeholder`...) n'existent plus. `LegacyActions` les convertit a la volee
(`quests/*.yml` a chaque chargement, migration vers les Documents, etapes legacy restees
dans un Document) : `command` → `run_command` (console), `message` → `send_message`
(`objective_template` → `objective_line`), `title` → `show_title`, `give_item` →
`give_item`, `action_bar`/`boss_bar` → `quests:*`, `%quest_name%` → `{quest_name}`,
`%msquests_x%` → `%quests_x%`, conditions → `has_permission`, `placeholder_equals`,
`placeholder_match` ou `expression` (seule a lire les bindings). Ecarts notes au rapport de
migration : `give_item` ne fait plus tomber au sol ce qui ne tient pas, condition inconnue
abandonnee.

Placeholders : `QuestPlaceholders` est enregistre dans le `PlaceholderRegistry` d'Artisan
(namespace `quests`, expose a PAPI) ; `%msquests_*%` reste un alias PAPI (C7). Nouvelles
cles `status_<group>:<quest>` et `completed_<group>:<quest>`.

Les data sources et commandes ci-dessous restent (alias `msquests:*`, C7) :

| Data source | Stability | Contenu |
|---|---|---|
| `msquests:groups` | STATIC (`key`) | Groupes de quetes + compteurs de l'acteur, `quests[]` imbriquees |
| `msquests:quests` | STATIC (`id` = `group:key`) | Catalogue complet : config + `stages[] > objectives[]`, avec la progression de l'acteur superposee |
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

Trois commandes appelables depuis un bouton de menu : `msquests:track <id>` (toggle),
`msquests:rotate <id>` et `msquests:contribute <id>`, ou `<id>` est soit l'UUID d'une instance,
soit un `group:key`. `contribute` donne a chaque objectif en cours qui implemente
`ContributableObjective` ce que le joueur peut fournir (items de l'inventaire pour `deliver_item`,
argent pour un objectif de banque fourni par un plugin tiers), jamais plus que ce qui manque. La
progression est ecrite tout de suite (`progressObjectiveNow`), pas mise en batch : ce que le joueur
a donne a deja quitte son inventaire.

**Brut plutot que pre-formate.** La mise en forme appartient au menu. Les lignes
portent des nombres (`objective_progress` / `objective_target`), des listes
(`stages[]`) et des instants absolus en epoch millis (`period_end`,
`expires_at`) — jamais de chaine deja composee. Cote Artisan, le Template
recompose ce qu'il veut :

```
{objective_progress}/{objective_target}
{period_end.until()}
```

`rewards_lore`, `countdown`, `period_end` formate et `expires_in` ont ete retires
le 2026-08-05 : ils n'existaient que parce que le runtime Artisan ne savait pas
joindre une liste ni formater une duree (ADR `expression-parity`).

**Contrainte** : les fetchers tournent sur le main thread au rendu du menu. Ils lisent
uniquement les registres memoire — jamais de repository, jamais d'I/O. Un acteur non
charge (joueur hors ligne) donne des valeurs neutres, pas une attente.

Build : `artisan-core-api` est `compileOnly` (fourni par le plugin Artisan, jamais shade),
version `artisanApiVersion` de `gradle.properties`. Tant que 2.2.0 (API conditions,
artisanmc/artisan#88) n'est pas publiee sur repo.artisanmc.net, la produire depuis `v2`
une fois #88 mergee, ou depuis sa branche :
`cd <artisan>/plugin && ./gradlew :artisan-api:publishToMavenLocal -PapiVersion=2.2.0`.

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
| `fishing` | `PlayerFishEvent` | item (`item` + `match`, legacy `fish_type`) |
| `execute_command` | `PlayerCommandPreprocessEvent` | command pattern |
| `craft_item` | `CraftItemEvent` | item (`item` + `match`, legacy `material`) |
| `breed_animal` | `EntityBreedEvent` | entity type |
| `deliver_item` | `msquests:contribute` (menu) | item (`item` + `match`, legacy `material`), amount — inventaire puis shulkers portees (`deliver_item.include_shulker_boxes` dans config.yml) |
| `harvest_crop` | `BlockBreakEvent` | crop type |

An objective's **item** (`ItemTarget`) is an Artisan item spec: the fields of a menu button's appearance (`material`: vanilla, `item:<captured id>` or `nexo:<id>`…; `title`, `lore`, `enchants`, `custom_model_data`), built and compared by `api.getItems()` (Artisan API 2.4.0). `match` lists the criteria (`material`, `custom_item`, `name`, `lore`, `enchants`, `custom_model_data`, `damage`, `exact`); absent, those the item carries. A legacy bare material is compared on the material alone, and is rewritten as `item` + `match: [material]` on save. No item: any item.
| `placeholder` | Polling scheduler | placeholder, interval, condition |
| `travel` | `PlayerMoveEvent` | distance |

---

## Types d'actions

Plus de types d'action propres : recompenses et crochets sont des chaines Artisan (voir
*Recompenses, crochets et requirements*). Le vocabulaire est celui du core d'Artisan plus
les actions `quests:*`.

------|-------|
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
| `core/src/main/resources/config.yml` | Config globale (BDD, langue, `content_source`) |
| `bukkit/src/main/resources/quests/*.yml` | Definitions de groupes de quetes (Jackson YAML) |
| `core/src/main/resources/lang/*.yml` | Fichiers de traduction |
| `paper-plugin.yml` | Descripteur plugin Paper |

Les configs de quetes sont chargees par `QuestConfigYamlRepository` avec Jackson (`SNAKE_CASE` naming strategy),
ou par `QuestGroupDocumentRepository` avec `content_source: artisan`.

### Source du contenu (`content_source`) et migration

Lu au demarrage seulement ; un changement demande un redemarrage (`/msquests reload` le signale).

| `content_source` | Contenu | Progression |
|---|---|---|
| `legacy` (defaut) | `quests/*.yml` | `msquests_*`, par `(group_key, quest_key)` |
| `artisan` | `<projet>/quest_groups/<groupe>.yaml` de tous les projets Artisan (Documents `quest_group/v1`) | `artisan_quests_*`, par `uid` |

Un Document porte un `uid` (ULID) immuable pour le groupe et pour chaque quete ; la cle du
groupe est le nom du fichier, renommable comme les cles de quete sans perte de progression.
`QuestGroupDocumentReader` produit le meme `QuestGroupConfigDTO` que le lecteur YAML (textes
`ltext` resolus dans la `default_lang` du manifest, `duration` en `24h`/`90m`, cron avec
`reset.timezone`) et refuse tout le fichier, chemin de la valeur fautive en tete, sur une
erreur : `uid` absent ou deja pris (sur tout le serveur), cle d'objectif repetee dans une
quete, etc. Une cle de groupe est unique sur le serveur, puisque commandes et placeholders
nomment un groupe par sa seule cle : deux projets portant `daily.yaml` sont signales avec les
deux chemins, et aucun des deux ne charge (un rechargement est alors refuse en entier). Actions et conditions gardent pour l'instant le format MSQuests (`type: ...`).

Passage d'un serveur (aucune etape ne touche `quests/*.yml` ni `msquests_*`) :

1. `/msquests migrate` (dry run) : convertit en memoire, relit chaque Document produit et le
   compare au groupe d'origine, compte ce que la copie en base fera, et ecrit
   `migration/report-<date>.txt`. Rien d'autre n'est ecrit.
2. `/msquests migrate --confirm` : ecrit les Documents absents dans le projet Artisan
   ordinaire `quests` (`plugins/Artisan/projects/quests/`, cree s'il manque ; jamais par-dessus
   un Document existant), recharge les projets d'Artisan, et ecrit `migration/map.yaml` (`(groupe, cle) -> uid`, `objectif -> stage`). Relance,
   il garde les memes `uid`.
3. `content_source: artisan` puis redemarrage : avant tout chargement, `LegacyDataCopier`
   copie `msquests_*` dans `artisan_quests_*` en une transaction (comptes verifies, sinon
   rollback), une seule fois. Sans `map.yaml` alors que `msquests_quest` a des lignes, le
   plugin refuse de demarrer.
4. `/msquests stats` avant et apres pour comparer. Retour arriere : `content_source: legacy`
   ou l'ancien jar ; la progression faite entre-temps reste dans `artisan_quests_*`.

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

7. **Annotations obligatoires** :
   - `@ObjectiveType("type_key")` sur les classes `ObjectiveConfig`
   - `@ConfigField` sur les champs de config pour la generation de schema JSON

8. **Enregistrement dans le plugin** : Tout nouveau type (objectif, acteur) doit etre enregistre dans `BukkitQuestsPlugin.onEnable()` via la factory correspondante.

### Ajout d'un type d'objectif

1. Creer `XxxObjectiveConfig.java` dans `bukkit/.../quest/objective/xxx/` avec `@ObjectiveType(ObjectiveTypes.XXX)`
2. Creer `XxxObjective.java` extends `AbstractQuestObjective<XxxObjectiveConfig>`
3. Creer `XxxObjectiveHandler.java` extends `BukkitQuestObjectiveHandler<XxxObjective>` avec le listener Bukkit
4. Ajouter la constante dans `ObjectiveTypes.java`
5. Enregistrer dans `BukkitQuestsPlugin.registerObjectiveTypes()`

### Ajout d'une action

Une action utilisable dans les chaines des quetes se declare cote Artisan : dans le core
d'Artisan si elle est generique, sinon comme action `quests:*` dans `QuestActions`
(`ActionDeclaration` + handler, test dans `QuestActionsTest`). Pas de condition maison :
les conditions sont celles d'Artisan.

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
