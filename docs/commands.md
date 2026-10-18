# Commandes des quetes

## Permissions:
Toutes les commandes msquests sont reservées aux admins.
Permission: **msquests.admin**

### Commandes globales
- /msquests reload
- /msquests stats — groupes, quêtes et progression chargés (à comparer avant et après une migration)

### Migration vers les Documents Artisan
- /msquests migrate [--dry-run] — convertit `quests/*.yml` en Documents `quest_group` en mémoire et écrit
  seulement le rapport `migration/report-<date>.txt`
- /msquests migrate --confirm — écrit aussi `plugins/Artisan/projects/quests/quest_groups/*.yaml` (jamais par-dessus un Document existant)
  et `migration/map.yaml`

Ensuite `content_source: artisan` dans `config.yml` et redémarrage : la progression est copiée dans
`artisan_quests_*` à ce démarrage. Voir `ARCHITECTURE.md`, *Source du contenu*.

### Quest item management
Ces commandes permettent de définir facilement des items qui peuvent être ensuite utilisés dans des quêtes afin d'éviter
de les écrire manuellement dans la config.
- /msquests item set <key>
- /msquests item get <key>

### Player quest management
Commandes qui permettent de gérer les acteurs et leurs quetes.
- /msquests actor <actortype> <actorId> list <group_key>
- /msquests actor <actortype> <actorId> start <group_key> <quest_key>
- /msquests actor <actortype> <actorId> complete <group_key> <quest_key>
- /msquests actor <actortype> <actorId> remove <group_key> <quest_key>