# Design : Systeme de conditions generique + Bossbar de tracking

## Contexte

Le plugin a un systeme de conditions sur les objectifs (`QuestObjectiveCondition`, `WorldCondition`, `BiomeCondition`) qui est specifique et limite. On veut un systeme de conditions **generique** inspire de zMenu, reutilisable partout (actions, objectifs, quetes), avec `placeholder` et `permission` comme types. En parallele, on ajoute une bossbar permanente pour la quete trackee, configurable dans la config globale.

## 1. Systeme de conditions

### Interface core

```java
public interface Condition {
    boolean test(PlayerProfile profile);
}
```

Remplace `QuestObjectiveCondition` (meme signature).

### Types de conditions

**`PlaceholderCondition`** :
```yaml
conditions:
  - type: placeholder
    placeholder: "%player_world%"
    action: EQUALS_STRING
    value: "world_nether"
```

Operateurs supportes :
- Numeriques : `EQUAL_TO`, `SUPERIOR`, `SUPERIOR_OR_EQUAL`, `LOWER`, `LOWER_OR_EQUAL`
- Strings : `EQUALS_STRING`, `EQUALS_IGNORECASE_STRING`, `CONTAINS_STRING`, `DIFFERENT_STRING`

**`PermissionCondition`** :
```yaml
conditions:
  - type: permission
    permission: "quest.vip"
```

### Ou les conditions s'appliquent

Meme format YAML `conditions: [...]` utilisable a 3 endroits :

- **Actions** — skip l'action si la condition echoue
- **Objectifs** — remplace le systeme actuel (conditions de progression)
- **Quetes** — conditions de demarrage (nouveau, dans la config de la quete)

Toutes les conditions d'une liste doivent passer (logique AND).

### Factory

`ConditionFactory` remplace `QuestObjectiveConditionFactory`. Meme pattern register/build avec les types `placeholder` et `permission`.

## 2. Bossbar de tracking

### Config globale (`config.yml`)

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

Gere un `Map<UUID, BossBar>` (joueur -> bossbar).

Cycle de vie :
- **Track** -> cree et affiche la bossbar
- **Progression** de la quete trackee -> met a jour texte + progress
- **Completion objectif** -> met a jour vers le prochain objectif actif
- **Completion quete / Untrack / Deconnexion** -> retire la bossbar
- **Connexion** -> restaure si quete trackee existe

Le message est resolu avec les placeholders du premier objectif actif de la quete trackee.

## 3. Eviter le doublon de bossbars

L'action `boss_bar` existante sur `objective_progress` peut etre conditionnee pour ne s'afficher que si la quete n'est PAS trackee :

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

Pas de type `tracked` dedie — on passe par les placeholders existants.

## 4. Suppressions

- `QuestObjectiveCondition` -> remplace par `Condition`
- `BukkitObjectiveCondition` -> supprime
- `WorldCondition`, `BiomeCondition` -> supprimes (remplaces par `PlaceholderCondition`)
- `QuestObjectiveConditionFactory` -> remplace par `ConditionFactory`
- `QuestObjectiveConditionConfigDTO` -> remplace par `ConditionConfigDTO`

## 5. Fichiers impactes

### Core
- `core/.../quest/condition/Condition.java` — nouvelle interface
- `core/.../factory/ConditionFactory.java` — nouvelle factory (remplace `QuestObjectiveConditionFactory`)
- `core/.../dto/ConditionConfigDTO.java` — nouveau DTO (remplace `QuestObjectiveConditionConfigDTO`)
- `core/.../quest/config/QuestObjectiveConfig.java` — changer type conditions
- `core/.../quest/config/QuestConfig.java` — ajouter conditions de demarrage
- `core/.../quest/config/action/QuestAction.java` — ajouter conditions
- `core/.../service/QuestProgressService.java` — evaluer conditions des actions
- `core/.../service/QuestDistributionService.java` — evaluer conditions de demarrage

### Bukkit
- `bukkit/.../condition/PlaceholderCondition.java` — nouveau
- `bukkit/.../condition/PermissionCondition.java` — nouveau
- `bukkit/.../service/TrackingBossBarService.java` — nouveau
- `bukkit/.../BukkitQuestsPlugin.java` — wiring
- `bukkit/.../command/QuestCommand.java` — notifier TrackingBossBarService sur track/untrack
- `bukkit/.../listener/PlayerJoinListener.java` — restaurer bossbar

### Suppressions
- `core/.../quest/condition/QuestObjectiveCondition.java`
- `core/.../factory/QuestObjectiveConditionFactory.java`
- `core/.../dto/QuestObjectiveConditionConfigDTO.java`
- `bukkit/.../quest/condition/BukkitObjectiveCondition.java`
- `bukkit/.../quest/condition/impl/WorldCondition.java`
- `bukkit/.../quest/condition/impl/BiomeCondition.java`

## 6. Config d'exemple complete

```yaml
# config.yml
tracking:
  bossbar:
    enabled: true
    message: '%objective_name% - %objective_progress%'
    color: WHITE
    style: PROGRESS
    show_progress: true

# daily.yml (groupe)
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

# quete avec conditions
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

- `./gradlew build` — compilation + tests passent
- Tests unitaires pour `PlaceholderCondition` (operateurs numeriques et string)
- Tests unitaires pour `PermissionCondition`
- Test integration : bossbar apparait au track, se met a jour a la progression, disparait au untrack
- Test integration : action boss_bar temporaire skippee quand quete trackee (via condition placeholder)
```
