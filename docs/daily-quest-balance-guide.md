# Guide d'equilibrage des quetes journalieres

## Vue d'ensemble

| Tier | Quetes/jour | XP/quete | XP total | Temps cible |
|------|------------|---------|---------|------------|
| Easy | 3 | 150 | 450 | 5-10 min |
| Medium | 2 | 500 | 1000 | 10-20 min |
| Hard | 1 | 1500 | 1500 | 20-30 min |
| **Total** | **6** | | **2950** | **~60-90 min** |
| Bonus all complete | | configurable | 1000 | |
| **Grand total** | | | **3950** | |

## Comparaison XP quetes vs actions

Un joueur qui farm 2-3h par actions passives gagne ~1000-1500 XP (avec nerf).
Un joueur qui fait toutes ses dailies en ~1h-1h30 gagne ~3950 XP.
**Ratio : les quetes sont ~3-4x plus efficaces que le farm passif.**

## Distribution du pool

| Tier | Pool | Ratio pool/distribue |
|------|------|---------------------|
| Easy | ~40 | 13:1 |
| Medium | ~30 | 15:1 |
| Hard | ~25 | 25:1 |
| **Total** | **~95** | |

---

## Guidelines par categorie

### Minage - Blocs communs
Stone, cobblestone, dirt, sand, gravel, netherrack, granite, diorite, andesite, deepslate, clay

| Tier | Quantite | Justification |
|------|----------|---------------|
| Easy | 64-128 | ~5 min avec pioche efficacite |
| Medium | 256-512 | ~15 min, necessite un bon spot |
| Hard | **Non utilise** | Les blocs communs ne sont pas un defi |

### Minage - Minerais communs
Coal, copper, iron, redstone, lapis, nether quartz

| Tier | Quantite | Justification |
|------|----------|---------------|
| Easy | 16-32 | Trouvable en minant normalement |
| Medium | 32-64 | Necessite un strip-mine dedie |
| Hard | 64-128 | Long meme avec fortune |

### Minage - Minerais rares
Gold, diamond, emerald, ancient debris

| Tier | Quantite | Justification |
|------|----------|---------------|
| Easy | **Non utilise** | Trop aleatoire pour une quete facile |
| Medium | gold 16-32, diamond 4-8 | Demande du strip-mine cible |
| Hard | diamond 16-32, emerald 8-16, ancient debris 4-8 | Vrai investissement de temps |

### Mobs - Communs
Zombie, skeleton, spider, creeper, drowned

| Tier | Quantite | Justification |
|------|----------|---------------|
| Easy | 10-20 | Une nuit de combat suffit |
| Medium | 30-50 | Necessite de chercher des spawns |
| Hard | **Non utilise** | Les mobs communs ne sont pas un defi |

### Mobs - Rares/Dangereux
Blaze, enderman, wither skeleton, guardian, witch, phantom

| Tier | Quantite | Justification |
|------|----------|---------------|
| Easy | **Non utilise** | Trop dangereux/rare pour easy |
| Medium | 10-15 | Necessite d'aller au bon endroit |
| Hard | 20-40 | Vrai expedition (nether fortress, end, etc.) |

### Peche

| Tier | Quantite | Justification |
|------|----------|---------------|
| Easy | 10-16 | 5-10 min de peche tranquille |
| Medium | 24-32 | 15-20 min de peche |
| Hard | 48-64 | Session de peche dediee |

### Elevage (Breeding)

| Tier | Quantite | Justification |
|------|----------|---------------|
| Easy | 5-10 | Quelques animaux a la ferme |
| Medium | 15-20 | Necessite un elevage organise |
| Hard | **Non utilise** | Le breeding est repetitif, pas difficile |

### Farming (Recoltes)
Wheat, carrots, potatoes, beetroot, nether wart, melon, pumpkin

| Tier | Quantite | Justification |
|------|----------|---------------|
| Easy | 32-64 | Petite ferme |
| Medium | 64-128 | Grande ferme, plusieurs crops |
| Hard | **Non utilise** | Le farming est repetitif, pas vraiment hard |

### Woodcutting (Buches)
Oak, birch, spruce, dark oak, jungle, acacia, mangrove, cherry, crimson/warped stem

| Tier | Quantite | Justification |
|------|----------|---------------|
| Easy | 64-128 | Foret a cote de la base |
| Medium | 128-256 | Session de bucheronnage |
| Hard | 128-256 | Bois rares (nether, mangrove, cherry) |

---

## Principes de design

1. **Easy = completable sans preparation** : le joueur peut le faire en jouant normalement
2. **Medium = necessite une activite dediee** : le joueur doit se concentrer sur la tache
3. **Hard = necessite une expedition** : le joueur doit se preparer et se deplacer
4. **Pas de blocs communs en hard** : miner 1024 stone n'est pas "hard", c'est juste long
5. **Les ressources rares sont reservees aux tiers superieurs** : pas de diamond en easy
6. **Le temps cible est roi** : ajuster les quantites pour que le temps corresponde au tier
7. **La protection anti-place-break est active** : les joueurs ne peuvent pas tricher en posant/cassant

## Bonus toutes quetes completees

Configure via le hook `all_quests_complete` dans les actions du groupe.
Valeur recommandee : 1000 XP (soit ~34% de bonus par rapport aux quetes seules).
