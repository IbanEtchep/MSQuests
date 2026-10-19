# ArtisanAttribution

Asks players how they found the server (web search, a friend, Discord, TikTok…) in an Artisan
menu, stores the answer, and runs the actions you configure for it.

Requires Paper 1.21+ (Folia supported) and **Artisan with API 2.6.0 or later**. Artisan provides
the menu, the database (H2, MySQL or MariaDB, set in `plugins/Artisan/environment.yml`) and the
translations.

## How it works

1. On the first start, ArtisanAttribution copies its menu into a new Artisan project
   `attribution` (once: a deleted menu never comes back). Restyle it in the editor like any menu.
2. When a player joins, the menu opens after `prompt.delay_ticks`. With
   `prompt.repeat_until_answered: true` it opens at every join until they answer; with `false`,
   at one join only.
3. A click on a source stores it (the first answer is kept, even with a double click or several
   servers on one database), says thanks, then runs the source's `actions` and `on_answer`.

The menu lists the sources of `config.yml` through the data source `attribution:sources`: adding
a source to `config.yml` adds a button, no menu edit needed.

## Configuration

See the comments of [`config.yml`](src/main/resources/config.yml). In short:

| Key | Meaning |
|---|---|
| `menu` | The Artisan menu to open, `project:menu` |
| `prompt.on_join` | Open the menu on join |
| `prompt.repeat_until_answered` | Every join until answered (`true`), or one join only (`false`) |
| `prompt.new_players_only` | Leave the players already there at install alone |
| `prompt.delay_ticks` | Delay between the join and the menu |
| `prompt.close_on_answer` | Close the menu after the answer |
| `on_answer` | Actions run for every answer |
| `sources.<id>` | `name`, `description`, `material`, `actions` of one answer |

Texts take `{ en: "...", fr: "..." }`; actions are Artisan action chains, as on a menu button,
with `{source}` and `{source_name}` bound:

```yaml
sources:
  friend:
    name: { en: "A friend told me", fr: "Recommandation d'un ami" }
    material: PLAYER_HEAD
    actions:
      - run_command:
          command: "eco give {player} 100"
          as: console
```

Messages live in `lang/en.yml` and `lang/fr.yml`; override any key in
`plugins/ArtisanAttribution/lang/<code>.yml`, or add a language there.

## In Artisan

| Name | What |
|---|---|
| `attribution:choose` | Action: `- attribution:choose: discord` answers for the player |
| `attribution:sources` | Data source: `id`, `name`, `description`, `material`, `count`, `chosen` |
| `%attribution_source%` | The player's source id, empty before they answer |
| `%attribution_source_name%` | Its name in the player's language |
| `%attribution_answered%` | `true` / `false` |

The placeholders are also given to PlaceholderAPI when it is installed.

## Commands and permissions

| Command | Permission | Does |
|---|---|---|
| `/attribution` | `artisanattribution.command` (everyone) | Opens the menu, if not answered yet |
| `/attribution open <player>` | `artisanattribution.admin` (op) | Opens the menu for an online player |
| `/attribution stats` | `artisanattribution.admin` | Answers per source, across the whole database |
| `/attribution reset <player>` | `artisanattribution.admin` | Forgets their answer: they are asked again |
| `/attribution reload` | `artisanattribution.admin` | Reloads `config.yml` and the lang files |

`artisanattribution.bypass` (nobody by default): never asked on join.

## Data

One table, `attribution_players` (`player_uuid`, `source`, `answered_at`, `prompted_at`,
`prompts`), migrated forward only through `attribution_schema_version`.

## Build

```bash
# Until Artisan API 2.6.0 is on repo.artisanmc.net, publish it from Artisan's v2 branch:
#   cd <artisan>/plugin && ./gradlew :artisan-api:publishToMavenLocal -PapiVersion=2.6.0
./gradlew build    # build/libs/ArtisanAttribution-<version>.jar, tests included
```
