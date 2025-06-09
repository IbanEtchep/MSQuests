# Commandes des quetes

## Permissions:
Toutes les commandes msquests sont reservées aux admins.
Permission: **msquests.admin**

### Commandes globales
- /msquests reload

### Quest item management
Ces commandes permettent de définir facilement des items qui peuvent être ensuite utilisés dans des quêtes afin d'éviter
de les écrire manuellement dans la config.
- /msquests item set <key>
- /msquests item get <key>

### Player quest management
Commandes qui permettent de gérer les acteurs et leurs quetes.
- /msquests actor <actortype> <actorId> list
- /msquests actor <actortype> <actorId> start <id>
- /msquests actor <actortype> <actorId> complete <id>
- /msquests actor <actortype> <actorId> remove <id>