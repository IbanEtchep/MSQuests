import { createModuleI18n as pe, fieldText as me, newTextKey as R, injectModuleStyles as he, useProject as ye, useContentLang as fe, Button as D, textValue as A, updateDocument as ge, useUiLang as be, ActionChainEditor as ve, MaterialInput as qe, SDK_VERSION as ke } from "@artisan/editor-sdk";
import { jsxs as p, jsx as n, Fragment as H } from "react/jsx-runtime";
import { useState as J } from "react";
const xe = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"]
}, Ne = /^\d+$/;
function U(e, t, i) {
  const a = String(i).padStart(2, "0");
  return e === "fr" ? i === 0 ? `${t} h` : `${t} h ${a}` : `${String(t).padStart(2, "0")}:${a}`;
}
function E(e, t, i) {
  return Ne.test(e) && Number(e) >= t && Number(e) <= i;
}
function je(e) {
  return e.trim().split(/\s+/).length === 5;
}
function we(e, t) {
  const i = t === "fr", a = e.trim().split(/\s+/);
  if (a.length !== 5) return null;
  const [r, s, d, f, u] = a;
  if (f !== "*") return null;
  const c = /^\*\/(\d+)$/;
  if (d === "*" && u === "*") {
    if (E(r, 0, 59) && E(s, 0, 23))
      return i ? `tous les jours à ${U(t, +s, +r)}` : `every day at ${U(t, +s, +r)}`;
    const h = c.exec(s);
    if (E(r, 0, 59) && h)
      return i ? `toutes les ${h[1]} h, à la minute ${r}` : `every ${h[1]} hours, at minute ${r}`;
    if (E(r, 0, 59) && s === "*")
      return i ? `toutes les heures, à la minute ${r}` : `every hour, at minute ${r}`;
    const m = c.exec(r);
    return m && s === "*" ? i ? `toutes les ${m[1]} minutes` : `every ${m[1]} minutes` : null;
  }
  if (!E(r, 0, 59) || !E(s, 0, 23)) return null;
  const o = U(t, +s, +r);
  if (d === "*" && E(u, 0, 7)) {
    const h = xe[i ? "fr" : "en"][Number(u) % 7];
    return i ? `chaque ${h} à ${o}` : `every ${h} at ${o}`;
  }
  return u === "*" && E(d, 1, 31) ? i ? `le ${d === "1" ? "1er" : d} de chaque mois à ${o}` : `on day ${d} of each month at ${o}` : null;
}
const ee = {
  kindLabel: "Quest groups",
  group: "Group",
  title: "Title",
  description: "Description",
  actorType: "Who plays",
  "actor.player": "Each player",
  "actor.global": "The whole server",
  reset: "Period reset",
  cron: "Schedule (cron)",
  cronInvalid: "A cron has 5 fields: minute hour day month weekday.",
  cronRaw: "Custom schedule",
  timezone: "Time zone",
  timezoneServer: "Server's time zone",
  noReset: "Never resets",
  distribution: "Distribution",
  strategy: "How quests are handed out",
  "strategy.none": "All at once",
  "strategy.sequential": "In list order",
  "strategy.random": "At random",
  amount: "Quests handed out",
  triggers: "Hand out when",
  "trigger.actor_load": "the player joins",
  "trigger.period_reset": "the period resets",
  "trigger.quest_complete": "a quest is completed",
  perTier: "Per tier",
  limits: "Limits",
  maxActive: "Active at once",
  maxPerPeriod: "Per period",
  rotation: "Rotation",
  rotationEnabled: "Rotate quests",
  tiers: "Tiers",
  tierKey: "Key",
  addTier: "Add a tier",
  noTier: "No tier",
  quests: "Quests",
  questCount: "{{count}} quests",
  addQuest: "New quest",
  newQuestTitle: "New quest",
  open: "Edit",
  remove: "Remove",
  moveUp: "Move up",
  moveDown: "Move down",
  back: "Back to the group",
  key: "Key",
  keyTaken: "Another quest of this group has this key.",
  objectiveKeyTaken: "Another objective of this quest has this key: they would share their progress.",
  tier: "Tier",
  duration: "Time limit",
  durationHint: "24h, 1h30m, 90s… empty: none",
  durationInvalid: "Write a duration such as 24h, 1h30m or 90s.",
  stages: "Stages",
  stage: "Stage {{n}}",
  addStage: "Add a stage",
  flow: "Order of the objectives",
  "flow.parallel": "In any order",
  "flow.sequential": "One after the other",
  objectives: "Objectives",
  addObjective: "Add an objective",
  type: "Type",
  required: "Required",
  rewards: "Rewards",
  rewardsHint: "Run when the quest is completed: any action Artisan or an installed plugin declares (guild_level…), with conditions.",
  legacySteps: "Old MSQuests format, kept as is: the server converts it on load, /msquests migrate rewrites it.",
  hooks: "Group events",
  hooksHint: "Actions run on each event of the group, for the player concerned.",
  "hook.distribution": "Quests are handed out",
  "hook.actor_load": "A player is loaded",
  "hook.quest_start": "A quest starts",
  "hook.objective_progress": "An objective progresses",
  "hook.objective_complete": "An objective is completed",
  "hook.quest_complete": "A quest is completed",
  "hook.period_complete": "Every quest of the period is completed",
  actionCount: "Actions: {{count}}",
  questSummary: "Stages: {{stages}} · objectives: {{objectives}}",
  general: "General",
  more: "More options",
  questKeyHint: "Names the quest in commands and placeholders. Progress follows the quest, not its key.",
  objectiveKeyHint: "Progress is stored under this key: changing it resets the progress of the players doing this quest.",
  stageTitle: "Stage title (optional)",
  missing: "To fill: {{fields}}",
  itemKeyHint: "Matched instead of the material, for a custom item. Empty for a vanilla item.",
  kept: "Kept as is: {{fields}}",
  preview: "What the player sees",
  noQuests: "No quest yet.",
  noObjectives: "No objective: the stage completes at once.",
  "objective.block_break": "Break blocks",
  "objective.craft_item": "Craft items",
  "objective.harvest_crop": "Harvest crops",
  "objective.fishing": "Fish",
  "objective.deliver_item": "Deliver items",
  "objective.kill_entity": "Kill mobs",
  "objective.breed_animal": "Breed animals",
  "objective.travel": "Travel",
  "objective.execute_command": "Run a command",
  "objective.placeholder": "Reach a placeholder value",
  "param.material": "Material",
  "param.crop": "Crop",
  "param.fish_type": "Fish",
  "param.amount": "Amount",
  "param.itemKey": "Custom item key",
  "param.entity_type": "Entity",
  "param.distance": "Distance (blocks)",
  "param.command": "Command",
  "param.placeholder": "Placeholder",
  "param.value": "Value",
  any: "any"
}, te = {
  kindLabel: "Groupes de quêtes",
  group: "Groupe",
  title: "Titre",
  description: "Description",
  actorType: "Qui joue",
  "actor.player": "Chaque joueur",
  "actor.global": "Tout le serveur",
  reset: "Réinitialisation de la période",
  cron: "Planification (cron)",
  cronInvalid: "Un cron a 5 champs : minute heure jour mois jour-de-semaine.",
  cronRaw: "Planification personnalisée",
  timezone: "Fuseau horaire",
  timezoneServer: "Fuseau du serveur",
  noReset: "Jamais réinitialisé",
  distribution: "Distribution",
  strategy: "Comment les quêtes sont données",
  "strategy.none": "Toutes d'un coup",
  "strategy.sequential": "Dans l'ordre de la liste",
  "strategy.random": "Au hasard",
  amount: "Quêtes données",
  triggers: "Donner quand",
  "trigger.actor_load": "le joueur se connecte",
  "trigger.period_reset": "la période se réinitialise",
  "trigger.quest_complete": "une quête est terminée",
  perTier: "Par tier",
  limits: "Limites",
  maxActive: "Actives en même temps",
  maxPerPeriod: "Par période",
  rotation: "Rotation",
  rotationEnabled: "Faire tourner les quêtes",
  tiers: "Tiers",
  tierKey: "Clé",
  addTier: "Ajouter un tier",
  noTier: "Aucun tier",
  quests: "Quêtes",
  questCount: "{{count}} quêtes",
  addQuest: "Nouvelle quête",
  newQuestTitle: "Nouvelle quête",
  open: "Modifier",
  remove: "Supprimer",
  moveUp: "Monter",
  moveDown: "Descendre",
  back: "Retour au groupe",
  key: "Clé",
  keyTaken: "Une autre quête du groupe a déjà cette clé.",
  objectiveKeyTaken: "Un autre objectif de la quête a déjà cette clé : ils partageraient leur progression.",
  tier: "Tier",
  duration: "Durée limite",
  durationHint: "24h, 1h30m, 90s… vide : aucune",
  durationInvalid: "Écrivez une durée comme 24h, 1h30m ou 90s.",
  stages: "Étapes",
  stage: "Étape {{n}}",
  addStage: "Ajouter une étape",
  flow: "Ordre des objectifs",
  "flow.parallel": "Dans n'importe quel ordre",
  "flow.sequential": "L'un après l'autre",
  objectives: "Objectifs",
  addObjective: "Ajouter un objectif",
  type: "Type",
  required: "Obligatoire",
  rewards: "Récompenses",
  rewardsHint: "Exécutées quand la quête est terminée : toute action d'Artisan ou d'un plugin installé (guild_level…), avec conditions.",
  legacySteps: "Ancien format MSQuests, conservé tel quel : le serveur le convertit au chargement, /msquests migrate le réécrit.",
  hooks: "Événements du groupe",
  hooksHint: "Actions exécutées à chaque événement du groupe, pour le joueur concerné.",
  "hook.distribution": "Les quêtes sont distribuées",
  "hook.actor_load": "Un joueur est chargé",
  "hook.quest_start": "Une quête commence",
  "hook.objective_progress": "Un objectif progresse",
  "hook.objective_complete": "Un objectif est terminé",
  "hook.quest_complete": "Une quête est terminée",
  "hook.period_complete": "Toutes les quêtes de la période sont terminées",
  actionCount: "Actions : {{count}}",
  questSummary: "Étapes : {{stages}} · objectifs : {{objectives}}",
  general: "Général",
  more: "Plus d'options",
  questKeyHint: "Nomme la quête dans les commandes et placeholders. La progression suit la quête, pas sa clé.",
  objectiveKeyHint: "La progression est stockée sous cette clé : la changer remet à zéro la progression des joueurs qui font cette quête.",
  stageTitle: "Titre de l'étape (facultatif)",
  missing: "À remplir : {{fields}}",
  itemKeyHint: "Comparée à la place du matériau, pour un objet personnalisé. Vide pour un objet vanilla.",
  kept: "Conservé tel quel : {{fields}}",
  preview: "Ce que voit le joueur",
  noQuests: "Aucune quête pour l'instant.",
  noObjectives: "Aucun objectif : l'étape se termine aussitôt.",
  "objective.block_break": "Casser des blocs",
  "objective.craft_item": "Fabriquer des objets",
  "objective.harvest_crop": "Récolter des cultures",
  "objective.fishing": "Pêcher",
  "objective.deliver_item": "Livrer des objets",
  "objective.kill_entity": "Tuer des créatures",
  "objective.breed_animal": "Faire se reproduire des animaux",
  "objective.travel": "Parcourir une distance",
  "objective.execute_command": "Exécuter une commande",
  "objective.placeholder": "Atteindre une valeur de placeholder",
  "param.material": "Matériau",
  "param.crop": "Culture",
  "param.fish_type": "Poisson",
  "param.amount": "Quantité",
  "param.itemKey": "Clé d'objet personnalisé",
  "param.entity_type": "Créature",
  "param.distance": "Distance (blocs)",
  "param.command": "Commande",
  "param.placeholder": "Placeholder",
  "param.value": "Valeur",
  any: "n'importe lequel"
}, Z = { en: ee.kindLabel, fr: te.kindLabel }, _e = pe({ en: ee, fr: te }), ae = "quest_group", ie = "quest_groups", V = "quest_group/v1", Q = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
function z(e = Date.now()) {
  let t = "", i = e;
  for (let s = 0; s < 10; s++)
    t = Q[i % 32] + t, i = Math.floor(i / 32);
  const a = new Uint8Array(16);
  crypto.getRandomValues(a);
  let r = "";
  for (let s = 0; s < 16; s++) r += Q[a[s] % 32];
  return t + r;
}
function P(e, t) {
  const i = new Set(t);
  if (!i.has(e)) return e;
  for (let a = 2; ; a++) if (!i.has(`${e}_${a}`)) return `${e}_${a}`;
}
function Ae(e, t) {
  const i = new Set(t), a = new Uint8Array(4);
  for (; ; ) {
    crypto.getRandomValues(a);
    const r = Array.from(a, (d) => Q[d % 32].toLowerCase()).join(""), s = `${G(e)}_${r}`;
    if (!i.has(s)) return s;
  }
}
function ne(e) {
  return e.toLowerCase().replace(/[^a-z0-9_]/g, "_");
}
function G(e) {
  return e.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().replace(/[^a-z0-9_]+/g, "_").replace(/^_+|_+$/g, "") || "quest";
}
function _(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? { ...e } : {};
}
function F(e) {
  return Array.isArray(e) ? e : [];
}
function w(e) {
  return typeof e == "string" || typeof e == "number" || typeof e == "boolean" ? String(e) : "";
}
function O(e, ...t) {
  const i = { ...e };
  for (const a of t) delete i[a];
  return i;
}
function Te(e, t) {
  const i = {}, a = [];
  function r(u) {
    if (u && typeof u == "object" && !Array.isArray(u)) {
      const h = R(), m = {};
      for (const [b, v] of Object.entries(u)) m[b] = w(v);
      return i[h] = m, { key: h };
    }
    const c = typeof u == "string" ? /^\{i18n:(.+)\}$/.exec(u) : null;
    if (c) {
      const h = t.translations?.[c[1]];
      return h && (i[c[1]] = { ...h }), { key: c[1] };
    }
    const o = R();
    return i[o] = u == null || u === "" ? {} : { [t.lang]: w(u) }, { key: o };
  }
  const s = Object.entries(_(e.tiers)).map(([u, c]) => {
    const o = _(c);
    return { key: u, title: r(o.title), rest: O(o, "title") };
  }), d = F(e.quests).map((u, c) => {
    const o = _(u), h = F(o.stages).map((m, b) => {
      const v = _(m), l = w(v.flow).toLowerCase();
      return {
        key: w(v.key) || `stage_${b + 1}`,
        title: r(v.title),
        flow: l === "parallel" || l === "sequential" ? l : "",
        objectives: F(v.objectives).map((y, q) => {
          const k = _(y);
          return {
            key: w(k.key) || `objective_${q + 1}`,
            type: w(k.type),
            params: O(k, "key", "type")
          };
        }),
        rest: O(v, "key", "title", "flow", "objectives")
      };
    });
    return {
      key: w(o.key) || `quest_${c + 1}`,
      uid: w(o.uid) || z(),
      tier: w(o.tier),
      title: r(o.title),
      description: r(o.description),
      duration: w(o.duration),
      stages: h,
      rest: O(o, "key", "uid", "tier", "title", "description", "duration", "stages")
    };
  }), f = {
    id: w(e.id),
    projectId: t.projectId,
    uid: w(e.uid) || z(),
    title: r(e.title),
    description: r(e.description),
    actorType: w(e.actor_type) || "player",
    reset: _(e.reset),
    limits: _(e.limits),
    rotation: _(e.rotation),
    distribution: _(e.distribution),
    tiers: s,
    quests: d,
    rest: O(
      _(e),
      "id",
      "schema",
      "uid",
      "title",
      "description",
      "actor_type",
      "reset",
      "limits",
      "rotation",
      "distribution",
      "tiers",
      "quests"
    )
  };
  return e.schema !== void 0 && e.schema !== V && a.push(`schema: ${String(e.schema)}`), { doc: f, i18nKeys: i, unsupported: a };
}
function Ee(e) {
  return typeof e == "string" ? e === "" : Object.values(e).every((t) => t === "");
}
function $(e) {
  const t = {};
  for (const [i, a] of Object.entries(e))
    a == null || a === "" || Array.isArray(a) && a.length === 0 || typeof a == "object" && !Array.isArray(a) && Object.keys(a).length === 0 || (t[i] = a);
  return t;
}
const $e = [
  "id",
  "schema",
  "uid",
  "title",
  "description",
  "actor_type",
  "window",
  "reset",
  "limits",
  "rotation",
  "distribution",
  "tiers",
  "on",
  "quests"
];
function Ce(e, t) {
  const i = (c) => me(t, c.key), a = (c) => {
    const o = i(c);
    return Ee(o) ? void 0 : o;
  }, r = {};
  for (const c of e.tiers) r[c.key] = { ...c.rest, title: i(c.title) };
  const s = e.quests.map(
    (c) => $({
      key: c.key,
      uid: c.uid,
      tier: c.tier,
      title: i(c.title),
      description: a(c.description),
      duration: c.duration,
      ...c.rest,
      stages: c.stages.map(
        (o) => $({
          key: o.key,
          title: a(o.title),
          flow: o.flow,
          ...o.rest,
          objectives: o.objectives.map((h) => ({ key: h.key, type: h.type, ...h.params }))
        })
      )
    })
  ), d = {
    ...e.rest,
    id: e.id,
    schema: V,
    uid: e.uid,
    title: i(e.title),
    description: a(e.description),
    actor_type: e.actorType,
    reset: $(e.reset),
    limits: $(e.limits),
    rotation: $(e.rotation),
    distribution: $(e.distribution),
    tiers: r,
    quests: s
  }, f = {};
  for (const c of $e) c in d && (f[c] = d[c]);
  for (const c of Object.keys(d)) c in f || (f[c] = d[c]);
  const u = $(f);
  return u.quests = s, u;
}
function* Se(e) {
  const t = `${ie}.${e.id}`;
  yield { lText: e.title, key: `${t}.title`, path: "title", kind: "quest_group.title" }, yield {
    lText: e.description,
    key: `${t}.description`,
    path: "description",
    kind: "quest_group.description"
  };
  for (const a of e.tiers)
    yield {
      lText: a.title,
      key: `${t}.tiers.${a.key}.title`,
      path: `tiers.${a.key}.title`,
      kind: "quest_group.tier"
    };
  const i = /* @__PURE__ */ new Set();
  for (let a = 0; a < e.quests.length; a++) {
    const r = e.quests[a], s = i.has(r.uid) ? `${r.uid}_${a}` : r.uid;
    i.add(r.uid);
    const d = `${t}.quests.${s}`;
    yield {
      lText: r.title,
      key: `${d}.title`,
      path: `quests[${a}].title`,
      kind: "quest.title"
    }, yield {
      lText: r.description,
      key: `${d}.description`,
      path: `quests[${a}].description`,
      kind: "quest.description"
    };
    const f = /* @__PURE__ */ new Set();
    for (let u = 0; u < r.stages.length; u++) {
      const c = r.stages[u], o = f.has(c.key) ? `${c.key}_${u}` : c.key;
      f.add(c.key), yield {
        lText: c.title,
        key: `${d}.stages.${o}.title`,
        path: `quests[${a}].stages[${u}].title`,
        kind: "quest.stage"
      };
    }
  }
}
function Y(e) {
  return !!e && typeof e == "object" && !Array.isArray(e) && "type" in e;
}
function Re(e) {
  const t = Array.isArray(e) ? e : e == null ? [] : [e];
  return {
    chain: t.filter((i) => !Y(i)),
    legacy: t.filter(Y)
  };
}
function X(e, t) {
  const i = [...e, ...t];
  return i.length ? i : void 0;
}
function Oe(e) {
  const t = String(e.type), i = e.command ?? e.message ?? e.title ?? e.material;
  return typeof i == "string" ? `${t} · ${i}` : t;
}
const Le = [
  "distribution",
  "actor_load",
  "quest_start",
  "objective_progress",
  "objective_complete",
  "quest_complete",
  "period_complete"
], C = { name: "amount", kind: "int", required: !0 }, re = [
  { type: "block_break", icon: "⛏", params: [{ name: "material", kind: "material" }, C] },
  { type: "craft_item", icon: "⚒", params: [{ name: "material", kind: "material" }, C] },
  {
    type: "harvest_crop",
    icon: "🌾",
    params: [{ name: "crop", kind: "material", required: !0 }, C]
  },
  { type: "fishing", icon: "🎣", params: [{ name: "fish_type", kind: "material" }, C] },
  {
    type: "deliver_item",
    icon: "📦",
    params: [
      { name: "material", kind: "material" },
      C,
      { name: "itemKey", kind: "text", advanced: !0 }
    ]
  },
  { type: "kill_entity", icon: "⚔", params: [{ name: "entity_type", kind: "entity" }, C] },
  { type: "breed_animal", icon: "♥", params: [{ name: "entity_type", kind: "entity" }, C] },
  { type: "travel", icon: "➜", params: [{ name: "distance", kind: "int", required: !0 }] },
  {
    type: "execute_command",
    icon: "/",
    params: [{ name: "command", kind: "text", required: !0 }]
  },
  {
    type: "placeholder",
    icon: "%",
    params: [
      { name: "placeholder", kind: "text", required: !0 },
      { name: "value", kind: "text", required: !0 }
    ]
  }
];
function K(e) {
  return re.find((t) => t.type === e);
}
function se(e) {
  const t = {};
  for (const i of K(e)?.params ?? [])
    i.kind === "int" && (t[i.name] = 1);
  return t;
}
function oe(e) {
  return e.amount ?? e.distance;
}
function le(e, t) {
  const i = K(e);
  return (i ? i.params.filter((r) => r.kind !== "int").map((r) => r.name) : Object.keys(t).filter((r) => typeof t[r] == "string")).map((r) => t[r]).filter((r) => typeof r == "string" && r !== "").join(" = ");
}
function Ie(e, t) {
  return (K(e)?.params ?? []).filter((i) => i.required && (t[i.name] === void 0 || t[i.name] === "")).map((i) => i.name);
}
const De = [
  "ALLAY",
  "ARMADILLO",
  "AXOLOTL",
  "BAT",
  "BEE",
  "BLAZE",
  "BOGGED",
  "BREEZE",
  "CAMEL",
  "CAT",
  "CAVE_SPIDER",
  "CHICKEN",
  "COD",
  "COW",
  "CREAKING",
  "CREEPER",
  "DOLPHIN",
  "DONKEY",
  "DROWNED",
  "ELDER_GUARDIAN",
  "ENDER_DRAGON",
  "ENDERMAN",
  "ENDERMITE",
  "EVOKER",
  "FOX",
  "FROG",
  "GHAST",
  "GLOW_SQUID",
  "GOAT",
  "GUARDIAN",
  "HOGLIN",
  "HORSE",
  "HUSK",
  "ILLUSIONER",
  "IRON_GOLEM",
  "LLAMA",
  "MAGMA_CUBE",
  "MOOSHROOM",
  "MULE",
  "OCELOT",
  "PANDA",
  "PARROT",
  "PHANTOM",
  "PIG",
  "PIGLIN",
  "PIGLIN_BRUTE",
  "PILLAGER",
  "PLAYER",
  "POLAR_BEAR",
  "PUFFERFISH",
  "RABBIT",
  "RAVAGER",
  "SALMON",
  "SHEEP",
  "SHULKER",
  "SILVERFISH",
  "SKELETON",
  "SKELETON_HORSE",
  "SLIME",
  "SNIFFER",
  "SNOW_GOLEM",
  "SPIDER",
  "SQUID",
  "STRAY",
  "STRIDER",
  "TADPOLE",
  "TRADER_LLAMA",
  "TROPICAL_FISH",
  "TURTLE",
  "VEX",
  "VILLAGER",
  "VINDICATOR",
  "WANDERING_TRADER",
  "WARDEN",
  "WITCH",
  "WITHER",
  "WITHER_SKELETON",
  "WOLF",
  "ZOGLIN",
  "ZOMBIE",
  "ZOMBIE_HORSE",
  "ZOMBIE_VILLAGER",
  "ZOMBIFIED_PIGLIN"
], Ke = `
.aq { display: flex; flex-direction: column; gap: 14px; padding: 12px; color: var(--text); font-size: 12.5px; }
.aq .input, .aq .select { height: 32px; padding: 5px 10px; font-size: 12.5px; }
.aq .textarea { padding: 6px 10px; font-size: 12.5px; min-height: 56px; resize: vertical; }
.aq-section { display: flex; flex-direction: column; gap: 8px; padding-top: 12px; border-top: 1px solid var(--border-soft); }
.aq-section:first-child { border-top: 0; padding-top: 0; }
.aq-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.aq-h { font-size: 11px; font-weight: 600; letter-spacing: .04em; text-transform: uppercase; color: var(--text-mut); }
.aq-field { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.aq-row { display: flex; gap: 8px; align-items: flex-end; }
.aq-row > * { flex: 1; min-width: 0; }
.aq-hint { color: var(--text-faint); font-size: 11.5px; }
.aq-ok { color: var(--success); font-size: 11.5px; }
.aq-err { color: var(--danger); font-size: 11.5px; }
.aq-warn { color: var(--warning); font-size: 11.5px; }
.aq-check { display: flex; align-items: center; gap: 6px; cursor: pointer; }
.aq-list { display: flex; flex-direction: column; gap: 6px; }
.aq-item { display: flex; align-items: center; gap: 6px; padding: 6px 8px; border: 1px solid var(--border); border-radius: var(--r-md); background: var(--surface); }
.aq-item-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; cursor: pointer; background: none; border: 0; color: inherit; text-align: left; padding: 0; font: inherit; }
.aq-item-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 500; }
.aq-item-sub { color: var(--text-faint); font-size: 11px; font-family: var(--font-mono); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.aq-card { display: flex; flex-direction: column; gap: 8px; padding: 10px; border: 1px solid var(--border); border-radius: var(--r-md); background: var(--surface); }
.aq-sub { display: flex; flex-direction: column; gap: 8px; padding: 8px; border-radius: var(--r-sm); background: var(--surface-inset); }
.aq-icon-btn { width: 24px; height: 24px; display: inline-flex; align-items: center; justify-content: center; border-radius: var(--r-sm); border: 0; background: none; color: var(--text-faint); cursor: pointer; flex: none; }
.aq-icon-btn:hover:not(:disabled) { background: var(--surface-hover); color: var(--text); }
.aq-icon-btn:disabled { opacity: .35; cursor: default; }
.aq-back { align-self: flex-start; }
.aq-preview { padding: 10px; border-radius: var(--r-md); background: var(--surface-inset); font-family: var(--font-mono); font-size: 11.5px; display: flex; flex-direction: column; gap: 2px; }
.aq-preview-title { color: var(--text); font-weight: 600; }
.aq-preview-line { color: var(--text-mut); }
.aq-mono { font-family: var(--font-mono); }
.aq-top { align-items: flex-start; }
.aq-wrap { flex-wrap: wrap; }
.aq-wrap > * { flex: 1 1 140px; }
.aq-badge { margin-left: auto; padding: 1px 7px; border-radius: 999px; background: var(--brand-soft); color: var(--text); font-size: 10.5px; }
.aq-fold > summary { display: flex; align-items: center; gap: 8px; cursor: pointer; color: var(--text-mut); font-size: 11.5px; padding: 4px 0; list-style: none; }
.aq-fold > summary::before { content: "▸"; color: var(--text-faint); transition: transform .12s; }
.aq-fold[open] > summary::before { transform: rotate(90deg); }
.aq-fold > summary::-webkit-details-marker { display: none; }
.aq-fold[open] { display: flex; flex-direction: column; gap: 8px; }
.aq-chain { display: flex; flex-direction: column; gap: 6px; }
.aq-legacy { display: flex; flex-direction: column; gap: 4px; padding: 8px; border: 1px dashed var(--border); border-radius: var(--r-sm); }
.aq-legacy-row { display: flex; align-items: center; gap: 6px; }
.aq-legacy-row code { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 11px; color: var(--text-mut); }
.aq-stages { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 10px; counter-reset: none; }
.aq-stage { display: flex; flex-direction: column; gap: 8px; padding: 10px 10px 8px; border: 1px solid var(--border); border-left: 3px solid var(--brand); border-radius: var(--r-md); background: var(--surface); }
.aq-stage-head { display: flex; align-items: center; gap: 6px; }
.aq-stage-n { flex: none; width: 22px; height: 22px; border-radius: 999px; display: inline-flex; align-items: center; justify-content: center; background: var(--brand); color: var(--surface); font-weight: 700; font-size: 11.5px; }
.aq .aq-stage-title { flex: 1; min-width: 0; height: 28px; font-weight: 600; background: transparent; border-color: transparent; }
.aq .aq-stage-title:hover, .aq .aq-stage-title:focus { border-color: var(--border); background: var(--surface-inset); }
.aq-seg { display: flex; gap: 4px; flex-wrap: wrap; }
.aq-seg .chip { height: 24px; padding: 0 10px; font-size: 11px; cursor: pointer; }
.aq-objectives { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 4px; }
.aq-obj { border-radius: var(--r-sm); background: var(--surface-inset); border: 1px solid transparent; }
.aq-obj.is-open { border-color: var(--border); }
.aq-obj.has-problem:not(.is-open) { border-color: var(--warning); }
.aq-obj-line { display: flex; align-items: center; gap: 2px; padding-right: 4px; }
.aq-obj-main { flex: 1; min-width: 0; display: flex; align-items: center; gap: 8px; padding: 7px 8px; background: none; border: 0; color: inherit; font: inherit; text-align: left; cursor: pointer; }
.aq-obj-main:hover .aq-obj-text { color: var(--text); }
.aq-obj-n { flex: none; min-width: 22px; color: var(--text-faint); font-family: var(--font-mono); font-size: 11px; }
.aq-obj-icon { flex: none; width: 20px; text-align: center; }
.aq-obj-text { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--text-mut); }
.aq-obj-text strong { color: var(--text); font-weight: 600; }
.aq-obj-dot { flex: none; width: 7px; height: 7px; border-radius: 999px; background: var(--warning); }
.aq-obj-form { display: flex; flex-direction: column; gap: 8px; padding: 4px 10px 10px; border-top: 1px solid var(--border-soft); }
.aq-add { align-self: flex-start; }
.aq-preview-stage { display: flex; flex-direction: column; gap: 2px; }
`, Me = /^(\d+|(\d+[dhms])+)$/, Pe = ["actor_load", "period_reset", "quest_complete"], He = (() => {
  try {
    return Intl.supportedValuesOf?.("timeZone") ?? [];
  } catch {
    return [];
  }
})();
function ze({ projectId: e, documentId: t, document: i }) {
  he("msquests", Ke);
  const a = i, r = ye(e), s = fe(), d = _e(), [f, u] = J(null), o = { projectId: e, project: r, lang: s, t: d, edit: (m, b) => {
    const v = structuredClone(a);
    m(v), ge({ projectId: e, kind: ae, id: t, doc: v, texts: b });
  } }, h = f ? a.quests.findIndex((m) => m.uid === f) : -1;
  return h >= 0 ? /* @__PURE__ */ p("div", { className: "aq", children: [
    /* @__PURE__ */ p(D, { variant: "ghost", size: "sm", className: "aq-back", onClick: () => u(null), children: [
      "← ",
      d("back")
    ] }),
    /* @__PURE__ */ n(Be, { ctx: o, group: a, index: h })
  ] }) : /* @__PURE__ */ n("div", { className: "aq", children: /* @__PURE__ */ n(Ue, { ctx: o, group: a, onOpen: u }) });
}
function j({
  label: e,
  children: t,
  hint: i
}) {
  return (
    // biome-ignore lint/a11y/noLabelWithoutControl: the control is the child
    /* @__PURE__ */ p("label", { className: "aq-field", children: [
      /* @__PURE__ */ n("span", { className: "field-label", children: e }),
      t,
      i
    ] })
  );
}
function L({
  ctx: e,
  label: t,
  node: i,
  multiline: a
}) {
  const r = A(e.project, i.key, e.lang), s = (d) => e.edit(() => {
  }, [{ key: i.key, lang: e.lang, value: d }]);
  return /* @__PURE__ */ n(j, { label: `${t} · ${e.lang}`, children: a ? /* @__PURE__ */ n("textarea", { className: "textarea", value: r, onChange: (d) => s(d.target.value) }) : /* @__PURE__ */ n("input", { className: "input", value: r, onChange: (d) => s(d.target.value) }) });
}
function Ge(e) {
  return typeof e == "number" ? String(e) : typeof e == "string" ? e : "";
}
function S({
  label: e,
  value: t,
  onChange: i
}) {
  return /* @__PURE__ */ n(j, { label: e, children: /* @__PURE__ */ n(
    "input",
    {
      className: "input",
      type: "number",
      min: 0,
      step: 1,
      value: Ge(t),
      onChange: (a) => {
        const r = a.target.value.trim();
        i(r === "" ? void 0 : Math.max(0, Math.trunc(Number(r))));
      }
    }
  ) });
}
function N(e, t, i) {
  i === void 0 || i === "" ? delete e[t] : e[t] = i;
}
function B(e, t, i) {
  if (i < 0 || i >= e.length) return;
  const [a] = e.splice(t, 1);
  e.splice(i, 0, a);
}
function I({
  label: e,
  onClick: t,
  disabled: i,
  children: a
}) {
  return /* @__PURE__ */ n(
    "button",
    {
      type: "button",
      className: "aq-icon-btn",
      title: e,
      "aria-label": e,
      onClick: t,
      disabled: i,
      children: a
    }
  );
}
function W({
  t: e,
  index: t,
  count: i,
  onMove: a,
  onRemove: r
}) {
  return /* @__PURE__ */ p(H, { children: [
    /* @__PURE__ */ n(I, { label: e("moveUp"), disabled: t === 0, onClick: () => a(t - 1), children: "↑" }),
    /* @__PURE__ */ n(
      I,
      {
        label: e("moveDown"),
        disabled: t === i - 1,
        onClick: () => a(t + 1),
        children: "↓"
      }
    ),
    /* @__PURE__ */ n(I, { label: e("remove"), onClick: r, children: "×" })
  ] });
}
function Ue({
  ctx: e,
  group: t,
  onOpen: i
}) {
  const { t: a, edit: r, project: s, lang: d } = e, f = be(), u = typeof t.reset.cron == "string" ? t.reset.cron : "", c = u ? we(u, f) : null, o = typeof t.reset.timezone == "string" ? t.reset.timezone : "", h = Array.isArray(t.distribution.triggers) ? t.distribution.triggers : [], m = t.distribution.tiers ?? {}, b = Object.keys(t.rest).filter((l) => l !== "on"), v = () => {
    const l = R(), y = z();
    r(
      (q) => {
        q.quests.push({
          key: P(
            "quest",
            q.quests.map((k) => k.key)
          ),
          uid: y,
          tier: "",
          title: { key: l },
          description: { key: R() },
          duration: "",
          stages: [de(1)],
          rest: {}
        });
      },
      [{ key: l, lang: d, value: a("newQuestTitle") }]
    ), i(y);
  };
  return /* @__PURE__ */ p(H, { children: [
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("group") }),
      /* @__PURE__ */ n(L, { ctx: e, label: a("title"), node: t.title }),
      /* @__PURE__ */ n(L, { ctx: e, label: a("description"), node: t.description, multiline: !0 }),
      /* @__PURE__ */ n(j, { label: a("actorType"), children: /* @__PURE__ */ p(
        "select",
        {
          className: "select",
          value: t.actorType,
          onChange: (l) => r((y) => {
            y.actorType = l.target.value;
          }),
          children: [
            /* @__PURE__ */ n("option", { value: "player", children: a("actor.player") }),
            /* @__PURE__ */ n("option", { value: "global", children: a("actor.global") }),
            !["player", "global"].includes(t.actorType) && /* @__PURE__ */ n("option", { value: t.actorType, children: t.actorType })
          ]
        }
      ) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("reset") }),
      /* @__PURE__ */ n(
        j,
        {
          label: a("cron"),
          hint: u ? je(u) ? /* @__PURE__ */ p("span", { className: "aq-ok", children: [
            c ?? a("cronRaw"),
            o ? `, ${o}` : ""
          ] }) : /* @__PURE__ */ n("span", { className: "aq-err", children: a("cronInvalid") }) : /* @__PURE__ */ n("span", { className: "aq-hint", children: a("noReset") }),
          children: /* @__PURE__ */ n(
            "input",
            {
              className: "input",
              value: u,
              placeholder: "0 4 * * *",
              onChange: (l) => r((y) => N(y.reset, "cron", l.target.value))
            }
          )
        }
      ),
      /* @__PURE__ */ p(j, { label: a("timezone"), children: [
        /* @__PURE__ */ n(
          "input",
          {
            className: "input",
            list: "aq-timezones",
            value: o,
            placeholder: a("timezoneServer"),
            onChange: (l) => r((y) => N(y.reset, "timezone", l.target.value.trim()))
          }
        ),
        /* @__PURE__ */ n("datalist", { id: "aq-timezones", children: He.map((l) => /* @__PURE__ */ n("option", { value: l }, l)) })
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("distribution") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(j, { label: a("strategy"), children: /* @__PURE__ */ p(
          "select",
          {
            className: "select",
            value: typeof t.distribution.strategy == "string" ? t.distribution.strategy.toLowerCase() : "",
            onChange: (l) => r((y) => N(y.distribution, "strategy", l.target.value)),
            children: [
              /* @__PURE__ */ n("option", { value: "", children: "—" }),
              ["none", "sequential", "random"].map((l) => /* @__PURE__ */ n("option", { value: l, children: a(`strategy.${l}`) }, l))
            ]
          }
        ) }),
        /* @__PURE__ */ n(
          S,
          {
            label: a("amount"),
            value: t.distribution.amount,
            onChange: (l) => r((y) => N(y.distribution, "amount", l))
          }
        )
      ] }),
      /* @__PURE__ */ n("span", { className: "field-label", children: a("triggers") }),
      Pe.map((l) => /* @__PURE__ */ p("label", { className: "aq-check", children: [
        /* @__PURE__ */ n(
          "input",
          {
            type: "checkbox",
            checked: h.includes(l),
            onChange: (y) => r((q) => {
              const k = Array.isArray(q.distribution.triggers) ? q.distribution.triggers : [], g = y.target.checked ? [...k, l] : k.filter((x) => x !== l);
              N(q.distribution, "triggers", g.length ? g : void 0);
            })
          }
        ),
        a(`trigger.${l}`)
      ] }, l)),
      t.tiers.length > 0 && /* @__PURE__ */ p(H, { children: [
        /* @__PURE__ */ n("span", { className: "field-label", children: a("perTier") }),
        /* @__PURE__ */ n("div", { className: "aq-row", children: t.tiers.map((l) => /* @__PURE__ */ n(
          S,
          {
            label: A(s, l.title.key, d) || l.key,
            value: m[l.key],
            onChange: (y) => r((q) => {
              const k = { ...q.distribution.tiers ?? {} };
              N(k, l.key, y), N(
                q.distribution,
                "tiers",
                Object.keys(k).length ? k : void 0
              );
            })
          },
          l.key
        )) })
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("limits") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(
          S,
          {
            label: a("maxActive"),
            value: t.limits.max_active,
            onChange: (l) => r((y) => N(y.limits, "max_active", l))
          }
        ),
        /* @__PURE__ */ n(
          S,
          {
            label: a("maxPerPeriod"),
            value: t.limits.max_per_period,
            onChange: (l) => r((y) => N(y.limits, "max_per_period", l))
          }
        )
      ] }),
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("rotation") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ p("label", { className: "aq-check", children: [
          /* @__PURE__ */ n(
            "input",
            {
              type: "checkbox",
              checked: t.rotation.enabled === !0,
              onChange: (l) => r((y) => N(y.rotation, "enabled", l.target.checked ? !0 : void 0))
            }
          ),
          a("rotationEnabled")
        ] }),
        /* @__PURE__ */ n(
          S,
          {
            label: a("maxPerPeriod"),
            value: t.rotation.max_per_period,
            onChange: (l) => r((y) => N(y.rotation, "max_per_period", l))
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ n("span", { className: "aq-h", children: a("tiers") }),
        /* @__PURE__ */ p(
          D,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => r((l) => {
              l.tiers.push({
                key: P(
                  "tier",
                  l.tiers.map((y) => y.key)
                ),
                title: { key: R() },
                rest: {}
              });
            }),
            children: [
              "+ ",
              a("addTier")
            ]
          }
        )
      ] }),
      t.tiers.map((l, y) => /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(j, { label: a("tierKey"), children: /* @__PURE__ */ n(
          "input",
          {
            className: "input",
            defaultValue: l.key,
            onBlur: (q) => {
              const k = G(q.target.value);
              k !== l.key && r((g) => {
                const x = P(
                  k,
                  g.tiers.map((M) => M.key)
                );
                g.tiers[y].key = x;
                for (const M of g.quests) M.tier === l.key && (M.tier = x);
                const T = g.distribution.tiers;
                T && l.key in T && (T[x] = T[l.key], delete T[l.key]);
              });
            }
          }
        ) }),
        /* @__PURE__ */ n(L, { ctx: e, label: a("title"), node: l.title }),
        /* @__PURE__ */ n(
          I,
          {
            label: a("remove"),
            onClick: () => r((q) => {
              q.tiers.splice(y, 1);
            }),
            children: "×"
          }
        )
      ] }, l.key))
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ p("span", { className: "aq-h", children: [
          a("quests"),
          " · ",
          t.quests.length
        ] }),
        /* @__PURE__ */ p(D, { size: "sm", variant: "primary", onClick: v, children: [
          "+ ",
          a("addQuest")
        ] })
      ] }),
      t.quests.length === 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: a("noQuests") }),
      /* @__PURE__ */ n("div", { className: "aq-list", children: t.quests.map((l, y) => /* @__PURE__ */ p("div", { className: "aq-item", children: [
        /* @__PURE__ */ p(
          "button",
          {
            type: "button",
            className: "aq-item-main",
            onClick: () => i(l.uid),
            title: a("open"),
            children: [
              /* @__PURE__ */ n("span", { className: "aq-item-title", children: A(s, l.title.key, d) || l.key }),
              /* @__PURE__ */ p("span", { className: "aq-item-sub", children: [
                a("questSummary", {
                  stages: l.stages.length,
                  objectives: l.stages.reduce((q, k) => q + k.objectives.length, 0)
                }),
                " · ",
                l.key,
                l.tier ? ` · ${l.tier}` : "",
                l.duration ? ` · ${l.duration}` : ""
              ] })
            ]
          }
        ),
        /* @__PURE__ */ n(
          W,
          {
            t: a,
            index: y,
            count: t.quests.length,
            onMove: (q) => r((k) => B(k.quests, y, q)),
            onRemove: () => r((q) => {
              q.quests.splice(y, 1);
            })
          }
        )
      ] }, l.uid)) }),
      b.length > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: a("kept", { fields: b.join(", ") }) })
    ] }),
    /* @__PURE__ */ n(Qe, { ctx: e, group: t })
  ] });
}
function ce({
  ctx: e,
  value: t,
  onChange: i
}) {
  const { chain: a, legacy: r } = Re(t);
  return /* @__PURE__ */ p("div", { className: "aq-chain", children: [
    /* @__PURE__ */ n(
      ve,
      {
        projectId: e.projectId,
        value: a,
        onChange: (s) => i(X(s, r))
      }
    ),
    r.length > 0 && /* @__PURE__ */ p("div", { className: "aq-legacy", children: [
      /* @__PURE__ */ n("span", { className: "aq-hint", children: e.t("legacySteps") }),
      r.map((s, d) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: a step has no identity
        /* @__PURE__ */ p("div", { className: "aq-legacy-row", children: [
          /* @__PURE__ */ n("code", { children: Oe(s) }),
          /* @__PURE__ */ n(
            I,
            {
              label: e.t("remove"),
              onClick: () => i(X(a, r.filter((f, u) => u !== d))),
              children: "×"
            }
          )
        ] }, d)
      ))
    ] })
  ] });
}
function Fe(e) {
  return Array.isArray(e) ? e.length : e == null ? 0 : 1;
}
function Qe({ ctx: e, group: t }) {
  const { t: i, edit: a } = e, r = t.rest.on && typeof t.rest.on == "object" ? t.rest.on : {};
  return /* @__PURE__ */ p("section", { className: "aq-section", children: [
    /* @__PURE__ */ n("span", { className: "aq-h", children: i("hooks") }),
    /* @__PURE__ */ n("span", { className: "aq-hint", children: i("hooksHint") }),
    Le.map((s) => {
      const d = Fe(r[s]);
      return /* @__PURE__ */ p("details", { className: "aq-fold", children: [
        /* @__PURE__ */ p("summary", { children: [
          /* @__PURE__ */ n("span", { children: i(`hook.${s}`) }),
          d > 0 && /* @__PURE__ */ n("span", { className: "aq-badge", title: i("actionCount", { count: d }), children: d })
        ] }),
        /* @__PURE__ */ n(
          ce,
          {
            ctx: e,
            value: r[s],
            onChange: (f) => a((u) => {
              const c = { ...u.rest.on ?? {} };
              N(c, s, f), N(u.rest, "on", Object.keys(c).length ? c : void 0);
            })
          }
        )
      ] }, s);
    })
  ] });
}
function de(e) {
  return {
    key: `stage_${e}`,
    title: { key: R() },
    flow: "parallel",
    objectives: [],
    rest: {}
  };
}
function Be({ ctx: e, group: t, index: i }) {
  const { t: a, edit: r } = e, s = t.quests[i], d = (o, h) => r((m) => o(m.quests[i]), h), f = t.quests.some((o, h) => h !== i && o.key === s.key), u = s.duration !== "" && !Me.test(s.duration.replace(/\s/g, "").toLowerCase()), c = Object.keys(s.rest).filter((o) => o !== "rewards");
  return /* @__PURE__ */ p(H, { children: [
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("general") }),
      /* @__PURE__ */ n(L, { ctx: e, label: a("title"), node: s.title }),
      /* @__PURE__ */ n(L, { ctx: e, label: a("description"), node: s.description, multiline: !0 }),
      /* @__PURE__ */ p("div", { className: "aq-row aq-top", children: [
        /* @__PURE__ */ n(j, { label: a("tier"), children: /* @__PURE__ */ p(
          "select",
          {
            className: "select",
            value: s.tier,
            onChange: (o) => d((h) => {
              h.tier = o.target.value;
            }),
            children: [
              /* @__PURE__ */ n("option", { value: "", children: a("noTier") }),
              t.tiers.map((o) => /* @__PURE__ */ n("option", { value: o.key, children: A(e.project, o.title.key, e.lang) || o.key }, o.key)),
              s.tier && !t.tiers.some((o) => o.key === s.tier) && /* @__PURE__ */ n("option", { value: s.tier, children: s.tier })
            ]
          }
        ) }),
        /* @__PURE__ */ n(
          j,
          {
            label: a("duration"),
            hint: /* @__PURE__ */ n("span", { className: u ? "aq-err" : "aq-hint", children: a(u ? "durationInvalid" : "durationHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input",
                value: s.duration,
                placeholder: "24h",
                onChange: (o) => d((h) => {
                  h.duration = o.target.value.trim();
                })
              }
            )
          }
        )
      ] }),
      /* @__PURE__ */ p("details", { className: "aq-fold", open: f || void 0, children: [
        /* @__PURE__ */ n("summary", { children: a("more") }),
        /* @__PURE__ */ n(
          j,
          {
            label: a("key"),
            hint: /* @__PURE__ */ n("span", { className: f ? "aq-err" : "aq-hint", children: a(f ? "keyTaken" : "questKeyHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input aq-mono",
                value: s.key,
                onChange: (o) => d((h) => {
                  h.key = ne(o.target.value);
                }),
                onBlur: () => {
                  const o = G(s.key);
                  o !== s.key && d((h) => {
                    h.key = o;
                  });
                }
              }
            )
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ p("span", { className: "aq-h", children: [
          a("stages"),
          " · ",
          s.stages.length
        ] }),
        /* @__PURE__ */ p(
          D,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => d((o) => {
              const h = P(
                `stage_${o.stages.length + 1}`,
                o.stages.map((m) => m.key)
              );
              o.stages.push({ ...de(o.stages.length + 1), key: h });
            }),
            children: [
              "+ ",
              a("addStage")
            ]
          }
        )
      ] }),
      /* @__PURE__ */ n("ol", { className: "aq-stages", children: s.stages.map((o, h) => /* @__PURE__ */ n(
        Ve,
        {
          ctx: e,
          quest: s,
          stage: o,
          si: h,
          editQuest: d
        },
        `${o.key}-${h}`
      )) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("rewards") }),
      /* @__PURE__ */ n("span", { className: "aq-hint", children: a("rewardsHint") }),
      /* @__PURE__ */ n(
        ce,
        {
          ctx: e,
          value: s.rest.rewards,
          onChange: (o) => d((h) => N(h.rest, "rewards", o))
        }
      ),
      c.length > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: a("kept", { fields: c.join(", ") }) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("preview") }),
      /* @__PURE__ */ n(Ye, { ctx: e, quest: s })
    ] })
  ] });
}
function Ve({
  ctx: e,
  quest: t,
  stage: i,
  si: a,
  editQuest: r
}) {
  const { t: s } = e, [d, f] = J(null), u = (m) => r((b) => m(b.stages[a])), c = A(e.project, i.title.key, e.lang), o = i.flow || "parallel", h = t.stages.flatMap((m) => m.objectives.map((b) => b.key));
  return /* @__PURE__ */ p("li", { className: "aq-stage", children: [
    /* @__PURE__ */ p("div", { className: "aq-stage-head", children: [
      /* @__PURE__ */ n("span", { className: "aq-stage-n", children: a + 1 }),
      /* @__PURE__ */ n(
        "input",
        {
          className: "input aq-stage-title",
          value: c,
          placeholder: s("stageTitle"),
          "aria-label": s("stage", { n: a + 1 }),
          onChange: (m) => e.edit(() => {
          }, [{ key: i.title.key, lang: e.lang, value: m.target.value }])
        }
      ),
      /* @__PURE__ */ n(
        W,
        {
          t: s,
          index: a,
          count: t.stages.length,
          onMove: (m) => r((b) => B(b.stages, a, m)),
          onRemove: () => r((m) => {
            m.stages.splice(a, 1);
          })
        }
      )
    ] }),
    i.objectives.length > 1 && /* @__PURE__ */ n("div", { className: "aq-seg", role: "radiogroup", "aria-label": s("flow"), children: ["parallel", "sequential"].map((m) => /* @__PURE__ */ n(
      "button",
      {
        type: "button",
        role: "radio",
        "aria-checked": o === m,
        className: `chip${o === m ? " is-active" : ""}`,
        onClick: () => u((b) => {
          b.flow = m;
        }),
        children: s(`flow.${m}`)
      },
      m
    )) }),
    i.objectives.length === 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: s("noObjectives") }),
    /* @__PURE__ */ n("ul", { className: "aq-objectives", children: i.objectives.map((m, b) => /* @__PURE__ */ n(
      We,
      {
        t: s,
        n: o === "sequential" ? `${a + 1}.${b + 1}` : "•",
        objective: m,
        open: d === m.key,
        onToggle: () => f(d === m.key ? null : m.key),
        keyTaken: h.filter((v) => v === m.key).length > 1,
        index: b,
        count: i.objectives.length,
        onMove: (v) => u((l) => B(l.objectives, b, v)),
        onChange: (v) => u((l) => v(l.objectives[b])),
        onRename: (v) => {
          d === m.key && f(v);
        },
        onRemove: () => u((v) => {
          v.objectives.splice(b, 1);
        })
      },
      `${m.key}-${b}`
    )) }),
    /* @__PURE__ */ p(
      D,
      {
        size: "sm",
        variant: "ghost",
        className: "aq-add",
        onClick: () => {
          const m = Ae("block_break", h);
          r((b) => {
            b.stages[a].objectives.push({
              key: m,
              type: "block_break",
              params: se("block_break")
            });
          }), f(m);
        },
        children: [
          "+ ",
          s("addObjective")
        ]
      }
    )
  ] });
}
function ue(e, t) {
  return K(t) ? e(`objective.${t}`) : t;
}
function We({
  t: e,
  n: t,
  objective: i,
  open: a,
  onToggle: r,
  keyTaken: s,
  index: d,
  count: f,
  onMove: u,
  onChange: c,
  onRename: o,
  onRemove: h
}) {
  const m = K(i.type), b = m ? m.params : Object.keys(i.params).map((g) => ({ name: g, kind: "text" })), v = oe(i.params), l = le(i.type, i.params), y = Ie(i.type, i.params), q = s || y.length > 0, k = (g) => /* @__PURE__ */ n(
    Ze,
    {
      t: e,
      spec: g,
      value: i.params[g.name],
      onChange: (x) => c((T) => N(T.params, g.name, x))
    },
    g.name
  );
  return /* @__PURE__ */ p("li", { className: `aq-obj${a ? " is-open" : ""}${q ? " has-problem" : ""}`, children: [
    /* @__PURE__ */ p("div", { className: "aq-obj-line", children: [
      /* @__PURE__ */ p("button", { type: "button", className: "aq-obj-main", onClick: r, "aria-expanded": a, children: [
        /* @__PURE__ */ n("span", { className: "aq-obj-n", children: t }),
        /* @__PURE__ */ n("span", { className: "aq-obj-icon", "aria-hidden": !0, children: m?.icon ?? "?" }),
        /* @__PURE__ */ p("span", { className: "aq-obj-text", children: [
          ue(e, i.type),
          v !== void 0 && /* @__PURE__ */ p("strong", { children: [
            " · ",
            String(v)
          ] }),
          l ? ` × ${l}` : v !== void 0 ? ` × ${e("any")}` : ""
        ] }),
        q && /* @__PURE__ */ n(
          "span",
          {
            className: "aq-obj-dot",
            title: s ? e("objectiveKeyTaken") : e("missing", {
              fields: y.map((g) => e(`param.${g}`)).join(", ")
            })
          }
        )
      ] }),
      /* @__PURE__ */ n(W, { t: e, index: d, count: f, onMove: u, onRemove: h })
    ] }),
    a && /* @__PURE__ */ p("div", { className: "aq-obj-form", children: [
      /* @__PURE__ */ n(j, { label: e("type"), children: /* @__PURE__ */ p(
        "select",
        {
          className: "select",
          value: i.type,
          onChange: (g) => c((x) => {
            x.type = g.target.value, x.params = se(x.type);
          }),
          children: [
            re.map((g) => /* @__PURE__ */ p("option", { value: g.type, children: [
              g.icon,
              " ",
              e(`objective.${g.type}`)
            ] }, g.type)),
            !m && /* @__PURE__ */ n("option", { value: i.type, children: i.type })
          ]
        }
      ) }),
      /* @__PURE__ */ n("div", { className: "aq-row aq-wrap", children: b.filter((g) => !g.advanced).map(k) }),
      y.length > 0 && /* @__PURE__ */ n("span", { className: "aq-warn", children: e("missing", {
        fields: y.map((g) => e(`param.${g}`)).join(", ")
      }) }),
      /* @__PURE__ */ p("details", { className: "aq-fold", open: s || void 0, children: [
        /* @__PURE__ */ n("summary", { children: e("more") }),
        b.filter((g) => g.advanced).map(k),
        /* @__PURE__ */ n(
          j,
          {
            label: e("key"),
            hint: /* @__PURE__ */ n("span", { className: s ? "aq-err" : "aq-hint", children: e(s ? "objectiveKeyTaken" : "objectiveKeyHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input aq-mono",
                value: i.key,
                onChange: (g) => {
                  const x = ne(g.target.value);
                  c((T) => {
                    T.key = x;
                  }), o(x);
                },
                onBlur: () => {
                  const g = G(i.key);
                  g !== i.key && (c((x) => {
                    x.key = g;
                  }), o(g));
                }
              }
            )
          }
        )
      ] })
    ] })
  ] });
}
function Ze({
  t: e,
  spec: t,
  value: i,
  onChange: a
}) {
  const r = e(`param.${t.name}`), s = t.required && (i === void 0 || i === ""), d = typeof i == "string" || typeof i == "number" ? String(i) : "";
  if (t.kind === "int")
    return /* @__PURE__ */ n(S, { label: r, value: i, onChange: a });
  const f = s ? /* @__PURE__ */ n("span", { className: "aq-warn", children: e("required") }) : t.name === "itemKey" ? /* @__PURE__ */ n("span", { className: "aq-hint", children: e("itemKeyHint") }) : void 0;
  if (t.kind === "material")
    return /* @__PURE__ */ n(j, { label: r, hint: f, children: /* @__PURE__ */ n(
      qe,
      {
        value: d,
        placeholder: e("any"),
        "aria-label": r,
        onChange: (c) => a(c.toUpperCase().replace(/\s+/g, "_"))
      }
    ) });
  const u = t.kind === "entity";
  return /* @__PURE__ */ p(j, { label: r, hint: f, children: [
    /* @__PURE__ */ n(
      "input",
      {
        className: `input${u ? " aq-mono" : ""}`,
        list: u ? "aq-entities" : void 0,
        value: d,
        placeholder: u ? e("any") : "",
        onChange: (c) => a(u ? c.target.value.toUpperCase().replace(/\s+/g, "_") : c.target.value)
      }
    ),
    u && /* @__PURE__ */ n("datalist", { id: "aq-entities", children: De.map((c) => /* @__PURE__ */ n("option", { value: c }, c)) })
  ] });
}
function Ye({ ctx: e, quest: t }) {
  const { t: i, project: a, lang: r } = e, s = (d) => {
    const f = le(d.type, d.params), u = oe(d.params);
    return `${ue(i, d.type)}${f ? ` · ${f}` : ""}${u !== void 0 ? ` · 0/${String(u)}` : ""}`;
  };
  return /* @__PURE__ */ p("div", { className: "aq-preview", children: [
    /* @__PURE__ */ n("span", { className: "aq-preview-title", children: A(a, t.title.key, r) || t.key }),
    A(a, t.description.key, r) && /* @__PURE__ */ n("span", { className: "aq-preview-line", children: A(a, t.description.key, r) }),
    t.stages.map((d, f) => (
      // biome-ignore lint/suspicious/noArrayIndexKey: stages are listed in order
      /* @__PURE__ */ p("span", { className: "aq-preview-stage", children: [
        t.stages.length > 1 && /* @__PURE__ */ n("span", { className: "aq-preview-line", children: A(a, d.title.key, r) || i("stage", { n: f + 1 }) }),
        d.objectives.map((u) => /* @__PURE__ */ p("span", { className: "aq-preview-line", children: [
          "• ",
          s(u)
        ] }, u.key))
      ] }, f)
    ))
  ] });
}
const Xe = {
  kind: ae,
  dir: ie,
  schemaVersion: V,
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: Z.en,
  labels: Z,
  component: ze,
  title: (e, { project: t, lang: i }) => A(t, e.title.key, i),
  create: ({ id: e, projectId: t }) => ({
    id: e,
    projectId: t,
    uid: z(),
    // Empty keys: the host mints them at their canonical address.
    title: { key: "" },
    description: { key: "" },
    actorType: "player",
    reset: {},
    limits: {},
    rotation: {},
    distribution: {},
    tiers: [],
    quests: [],
    rest: {}
  }),
  parse: (e, t) => Te(e, t),
  serialize: (e, t) => Ce(e, t),
  ltextSites: (e) => Se(e),
  board: {
    summary: (e) => `quest_group · ${e.quests.length}`
  }
}, Je = {
  id: "msquests",
  displayName: "Quests",
  schemaVersion: "quest_group/v1",
  sdk: ke,
  sidebar: { icon: () => null, label: "Quests", to: "/m/msquests" },
  // Quests are Documents: they are edited from the project, no screen of their own.
  routes: [],
  documents: [Xe]
}, it = { ...Je, sdk: "1.1.0" };
export {
  it as default
};
