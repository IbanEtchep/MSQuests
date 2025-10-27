# TODO

## ZMenu support :

Zmenu support, créer les boutons pour paginer les quetes d'un pool, quetes en cours...

Quest tracker:
- Permet de choisir quelle quête suivre
- Placeholders objectif :
   - %msquests_tracked_quest_name%
   - %msquests_tracked_quest_description%
   - %msquests_tracked_quest_objective_{index}% - affiche l'objectif à la position {index}
   - %msquests_tracked_quest_objective_unfinished% - affiche le premier objectif non accompli

Commandes:

### /msquests actor <actor type> <actor> handle_group <group> 
Peut être attribuée à un NPC. Cette commande permet d'attribuer des quetes d'un groupe à un acteur.
- Si aucune quete n'est en cours, la quete suivante du groupe est jouée (Si possible : ex cooldown), donc dialogue d'intro -> début de la quete
- Si une quete est en cours, vérif d'accomplissement (exemple ramener des items). Si objectif non accompli, rappel des objectifs.
- Si la quete est accomplie, récompense et message de fin.