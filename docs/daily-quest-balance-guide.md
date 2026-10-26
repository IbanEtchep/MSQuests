# Daily quest balancing guide

## Overview

| Tier | Quests/day | XP/quest | Total XP | $/quest | Total $ | Target time |
|------|------------|---------|---------|--------|--------|------------|
| Easy | 3 | 150 | 450 | 500 | 1500 | 5-10 min |
| Medium | 2 | 500 | 1000 | 1000 | 2000 | 10-20 min |
| Hard | 1 | 1500 | 1500 | 1500 | 1500 | 20-30 min |
| **Total** | **6** | | **2950** | | **5000** | **~60-90 min** |
| Bonus all complete | | | 1000 | | 5000 | |
| **Grand total** | | | **3950** | | **10000** | |

## Quest XP vs action XP

A player who farms for 2-3h through passive actions earns ~1000-1500 XP (with the nerf).
A player who does all their dailies in ~1h-1h30 earns ~3950 XP.
**Ratio: quests are ~3-4x more efficient than passive farming.**

## Pool distribution

| Tier | Pool | Pool/distributed ratio |
|------|------|---------------------|
| Easy | ~40 | 13:1 |
| Medium | ~30 | 15:1 |
| Hard | ~25 | 25:1 |
| **Total** | **~95** | |

---

## Guidelines by category

### Mining - Common blocks
Stone, cobblestone, dirt, sand, gravel, netherrack, granite, diorite, andesite, deepslate, clay

| Tier | Amount | Rationale |
|------|----------|---------------|
| Easy | 64-128 | ~5 min with an efficiency pickaxe |
| Medium | 256-512 | ~15 min, needs a good spot |
| Hard | **Not used** | Common blocks are not a challenge |

### Mining - Common ores
Coal, copper, iron, redstone, lapis, nether quartz

| Tier | Amount | Rationale |
|------|----------|---------------|
| Easy | 16-32 | Found by mining normally |
| Medium | 32-64 | Needs a dedicated strip-mine |
| Hard | 64-128 | Long even with fortune |

### Mining - Rare ores
Gold, diamond, emerald, ancient debris

| Tier | Amount | Rationale |
|------|----------|---------------|
| Easy | **Not used** | Too random for an easy quest |
| Medium | gold 16-32, diamond 4-8 | Requires targeted strip-mining |
| Hard | diamond 16-32, emerald 8-16, ancient debris 4-8 | A real time investment |

### Mobs - Common
Zombie, skeleton, spider, creeper, drowned

| Tier | Amount | Rationale |
|------|----------|---------------|
| Easy | 10-20 | One night of combat is enough |
| Medium | 30-50 | Needs hunting for spawns |
| Hard | **Not used** | Common mobs are not a challenge |

### Mobs - Rare/Dangerous
Blaze, enderman, wither skeleton, guardian, witch, phantom

| Tier | Amount | Rationale |
|------|----------|---------------|
| Easy | **Not used** | Too dangerous/rare for easy |
| Medium | 10-15 | Needs going to the right place |
| Hard | 20-40 | A real expedition (nether fortress, end, etc.) |

### Fishing

| Tier | Amount | Rationale |
|------|----------|---------------|
| Easy | 10-16 | 5-10 min of relaxed fishing |
| Medium | 24-32 | 15-20 min of fishing |
| Hard | 48-64 | A dedicated fishing session |

### Breeding

| Tier | Amount | Rationale |
|------|----------|---------------|
| Easy | 5-10 | A few animals on the farm |
| Medium | 15-20 | Needs an organized breeding setup |
| Hard | **Not used** | Breeding is repetitive, not hard |

### Farming (Crops)
Wheat, carrots, potatoes, beetroot, nether wart, melon, pumpkin

| Tier | Amount | Rationale |
|------|----------|---------------|
| Easy | 32-64 | Small farm |
| Medium | 64-128 | Large farm, several crops |
| Hard | **Not used** | Farming is repetitive, not really hard |

### Woodcutting (Logs)
Oak, birch, spruce, dark oak, jungle, acacia, mangrove, cherry, crimson/warped stem

| Tier | Amount | Rationale |
|------|----------|---------------|
| Easy | 64-128 | Forest next to the base |
| Medium | 128-256 | Woodcutting session |
| Hard | 128-256 | Rare woods (nether, mangrove, cherry) |

---

## Design principles

1. **Easy = completable without preparation**: the player can do it while playing normally
2. **Medium = requires a dedicated activity**: the player must focus on the task
3. **Hard = requires an expedition**: the player must prepare and travel
4. **No common blocks in hard**: mining 1024 stone is not "hard", it is just long
5. **Rare resources are reserved for higher tiers**: no diamond in easy
6. **Target time is king**: adjust amounts so that the time matches the tier
7. **Anti place-break protection is active**: players cannot cheat by placing/breaking

## All-quests-completed bonus

Configured via the `all_period_quests_complete` hook in the group's actions.
Recommended value: 1000 XP (~34% bonus compared to the quests alone).