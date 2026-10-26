# TODO

## ZMenu support :

Zmenu support, create the buttons to paginate the quests of a pool, ongoing quests...

Quest tracker:
- Lets the player choose which quest to track
- Objective placeholders:
   - %msquests_tracked_quest_name%
   - %msquests_tracked_quest_description%
   - %msquests_tracked_quest_objective_{index}% - shows the objective at position {index}
   - %msquests_tracked_quest_objective_unfinished% - shows the first unfinished objective

Commands:

### /msquests actor <actor type> <actor> handle_group <group> 
Can be assigned to an NPC. This command assigns the quests of a group to an actor.
- If no quest is in progress, the next quest of the group is played (if possible: e.g. cooldown), so intro dialogue -> start of the quest
- If a quest is in progress, check for completion (e.g. bringing items). If an objective is not done, remind the objectives.
- If the quest is done, reward and end message.