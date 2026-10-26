# Quest commands

## Permissions:
All msquests commands are reserved for admins.
Permission: **msquests.admin**

### Global commands
- /msquests reload
- /msquests stats — loaded groups, quests and progress (to compare before and after a migration)

### Migration to Artisan Documents
- /msquests migrate [--dry-run] — converts `quests/*.yml` into `quest_group` Documents in memory and writes
  only the report `migration/report-<date>.txt`
- /msquests migrate --confirm — also writes `plugins/Artisan/projects/quests/quest_groups/*.yaml` (never over an existing Document)
  and `migration/map.yaml`

Then set `content_source: artisan` in `config.yml` and restart: progress is copied into
`artisan_quests_*` at that startup. See `ARCHITECTURE.md`, *Content source*.

### Quest item management
These commands make it easy to define items that can then be used in quests, so they do not have to
be written manually in the config.
- /msquests item set <key>
- /msquests item get <key>

### Player quest management
Commands to manage actors and their quests.
- /msquests actor <actortype> <actorId> list <group_key>
- /msquests actor <actortype> <actorId> start <group_key> <quest_key>
- /msquests actor <actortype> <actorId> complete <group_key> <quest_key>
- /msquests actor <actortype> <actorId> remove <group_key> <quest_key>