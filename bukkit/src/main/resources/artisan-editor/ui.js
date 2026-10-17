import { createModuleI18n as pe, fieldText as me, newTextKey as R, injectModuleStyles as he, useProject as ye, useContentLang as fe, Button as D, selectDocumentItem as ge, textValue as _, updateDocument as be, useUiLang as ve, ActionChainEditor as qe, MaterialInput as ke, SDK_VERSION as xe } from "@artisan/editor-sdk";
import { jsxs as p, jsx as n, Fragment as H } from "react/jsx-runtime";
import { useState as J, useEffect as Ne } from "react";
const je = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"]
}, we = /^\d+$/;
function U(e, a, i) {
  const t = String(i).padStart(2, "0");
  return e === "fr" ? i === 0 ? `${a} h` : `${a} h ${t}` : `${String(a).padStart(2, "0")}:${t}`;
}
function E(e, a, i) {
  return we.test(e) && Number(e) >= a && Number(e) <= i;
}
function _e(e) {
  return e.trim().split(/\s+/).length === 5;
}
function Ae(e, a) {
  const i = a === "fr", t = e.trim().split(/\s+/);
  if (t.length !== 5) return null;
  const [r, s, d, f, u] = t;
  if (f !== "*") return null;
  const c = /^\*\/(\d+)$/;
  if (d === "*" && u === "*") {
    if (E(r, 0, 59) && E(s, 0, 23))
      return i ? `tous les jours à ${U(a, +s, +r)}` : `every day at ${U(a, +s, +r)}`;
    const h = c.exec(s);
    if (E(r, 0, 59) && h)
      return i ? `toutes les ${h[1]} h, à la minute ${r}` : `every ${h[1]} hours, at minute ${r}`;
    if (E(r, 0, 59) && s === "*")
      return i ? `toutes les heures, à la minute ${r}` : `every hour, at minute ${r}`;
    const m = c.exec(r);
    return m && s === "*" ? i ? `toutes les ${m[1]} minutes` : `every ${m[1]} minutes` : null;
  }
  if (!E(r, 0, 59) || !E(s, 0, 23)) return null;
  const l = U(a, +s, +r);
  if (d === "*" && E(u, 0, 7)) {
    const h = je[i ? "fr" : "en"][Number(u) % 7];
    return i ? `chaque ${h} à ${l}` : `every ${h} at ${l}`;
  }
  return u === "*" && E(d, 1, 31) ? i ? `le ${d === "1" ? "1er" : d} de chaque mois à ${l}` : `on day ${d} of each month at ${l}` : null;
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
  rewardsHint: "Run when the quest is completed: any action Artisan or an installed plugin declares, with conditions; a plugin without an action (guild XP…) is reached with a command.",
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
  rewardsHint: "Exécutées quand la quête est terminée : toute action d'Artisan ou d'un plugin installé, avec conditions ; un plugin sans action (XP de guilde…) passe par une commande.",
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
}, Z = { en: ee.kindLabel, fr: te.kindLabel }, Te = pe({ en: ee, fr: te }), ae = "quest_group", ie = "quest_groups", V = "quest_group/v1", Q = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
function z(e = Date.now()) {
  let a = "", i = e;
  for (let s = 0; s < 10; s++)
    a = Q[i % 32] + a, i = Math.floor(i / 32);
  const t = new Uint8Array(16);
  crypto.getRandomValues(t);
  let r = "";
  for (let s = 0; s < 16; s++) r += Q[t[s] % 32];
  return a + r;
}
function M(e, a) {
  const i = new Set(a);
  if (!i.has(e)) return e;
  for (let t = 2; ; t++) if (!i.has(`${e}_${t}`)) return `${e}_${t}`;
}
function Ee(e, a) {
  const i = new Set(a), t = new Uint8Array(4);
  for (; ; ) {
    crypto.getRandomValues(t);
    const r = Array.from(t, (d) => Q[d % 32].toLowerCase()).join(""), s = `${G(e)}_${r}`;
    if (!i.has(s)) return s;
  }
}
function ne(e) {
  return e.toLowerCase().replace(/[^a-z0-9_]/g, "_");
}
function G(e) {
  return e.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().replace(/[^a-z0-9_]+/g, "_").replace(/^_+|_+$/g, "") || "quest";
}
function A(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? { ...e } : {};
}
function F(e) {
  return Array.isArray(e) ? e : [];
}
function w(e) {
  return typeof e == "string" || typeof e == "number" || typeof e == "boolean" ? String(e) : "";
}
function O(e, ...a) {
  const i = { ...e };
  for (const t of a) delete i[t];
  return i;
}
function $e(e, a) {
  const i = {}, t = [];
  function r(u) {
    if (u && typeof u == "object" && !Array.isArray(u)) {
      const h = R(), m = {};
      for (const [v, b] of Object.entries(u)) m[v] = w(b);
      return i[h] = m, { key: h };
    }
    const c = typeof u == "string" ? /^\{i18n:(.+)\}$/.exec(u) : null;
    if (c) {
      const h = a.translations?.[c[1]];
      return h && (i[c[1]] = { ...h }), { key: c[1] };
    }
    const l = R();
    return i[l] = u == null || u === "" ? {} : { [a.lang]: w(u) }, { key: l };
  }
  const s = Object.entries(A(e.tiers)).map(([u, c]) => {
    const l = A(c);
    return { key: u, title: r(l.title), rest: O(l, "title") };
  }), d = F(e.quests).map((u, c) => {
    const l = A(u), h = F(l.stages).map((m, v) => {
      const b = A(m), o = w(b.flow).toLowerCase();
      return {
        key: w(b.key) || `stage_${v + 1}`,
        title: r(b.title),
        flow: o === "parallel" || o === "sequential" ? o : "",
        objectives: F(b.objectives).map((y, q) => {
          const k = A(y);
          return {
            key: w(k.key) || `objective_${q + 1}`,
            type: w(k.type),
            params: O(k, "key", "type")
          };
        }),
        rest: O(b, "key", "title", "flow", "objectives")
      };
    });
    return {
      key: w(l.key) || `quest_${c + 1}`,
      uid: w(l.uid) || z(),
      tier: w(l.tier),
      title: r(l.title),
      description: r(l.description),
      duration: w(l.duration),
      stages: h,
      rest: O(l, "key", "uid", "tier", "title", "description", "duration", "stages")
    };
  }), f = {
    id: w(e.id),
    projectId: a.projectId,
    uid: w(e.uid) || z(),
    title: r(e.title),
    description: r(e.description),
    actorType: w(e.actor_type) || "player",
    reset: A(e.reset),
    limits: A(e.limits),
    rotation: A(e.rotation),
    distribution: A(e.distribution),
    tiers: s,
    quests: d,
    rest: O(
      A(e),
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
  return e.schema !== void 0 && e.schema !== V && t.push(`schema: ${String(e.schema)}`), { doc: f, i18nKeys: i, unsupported: t };
}
function Ce(e) {
  return typeof e == "string" ? e === "" : Object.values(e).every((a) => a === "");
}
function $(e) {
  const a = {};
  for (const [i, t] of Object.entries(e))
    t == null || t === "" || Array.isArray(t) && t.length === 0 || typeof t == "object" && !Array.isArray(t) && Object.keys(t).length === 0 || (a[i] = t);
  return a;
}
const Se = [
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
function Re(e, a) {
  const i = (c) => me(a, c.key), t = (c) => {
    const l = i(c);
    return Ce(l) ? void 0 : l;
  }, r = {};
  for (const c of e.tiers) r[c.key] = { ...c.rest, title: i(c.title) };
  const s = e.quests.map(
    (c) => $({
      key: c.key,
      uid: c.uid,
      tier: c.tier,
      title: i(c.title),
      description: t(c.description),
      duration: c.duration,
      ...c.rest,
      stages: c.stages.map(
        (l) => $({
          key: l.key,
          title: t(l.title),
          flow: l.flow,
          ...l.rest,
          objectives: l.objectives.map((h) => ({ key: h.key, type: h.type, ...h.params }))
        })
      )
    })
  ), d = {
    ...e.rest,
    id: e.id,
    schema: V,
    uid: e.uid,
    title: i(e.title),
    description: t(e.description),
    actor_type: e.actorType,
    reset: $(e.reset),
    limits: $(e.limits),
    rotation: $(e.rotation),
    distribution: $(e.distribution),
    tiers: r,
    quests: s
  }, f = {};
  for (const c of Se) c in d && (f[c] = d[c]);
  for (const c of Object.keys(d)) c in f || (f[c] = d[c]);
  const u = $(f);
  return u.quests = s, u;
}
function* Oe(e) {
  const a = `${ie}.${e.id}`;
  yield { lText: e.title, key: `${a}.title`, path: "title", kind: "quest_group.title" }, yield {
    lText: e.description,
    key: `${a}.description`,
    path: "description",
    kind: "quest_group.description"
  };
  for (const t of e.tiers)
    yield {
      lText: t.title,
      key: `${a}.tiers.${t.key}.title`,
      path: `tiers.${t.key}.title`,
      kind: "quest_group.tier"
    };
  const i = /* @__PURE__ */ new Set();
  for (let t = 0; t < e.quests.length; t++) {
    const r = e.quests[t], s = i.has(r.uid) ? `${r.uid}_${t}` : r.uid;
    i.add(r.uid);
    const d = `${a}.quests.${s}`;
    yield {
      lText: r.title,
      key: `${d}.title`,
      path: `quests[${t}].title`,
      kind: "quest.title"
    }, yield {
      lText: r.description,
      key: `${d}.description`,
      path: `quests[${t}].description`,
      kind: "quest.description"
    };
    const f = /* @__PURE__ */ new Set();
    for (let u = 0; u < r.stages.length; u++) {
      const c = r.stages[u], l = f.has(c.key) ? `${c.key}_${u}` : c.key;
      f.add(c.key), yield {
        lText: c.title,
        key: `${d}.stages.${l}.title`,
        path: `quests[${t}].stages[${u}].title`,
        kind: "quest.stage"
      };
    }
  }
}
function Y(e) {
  return !!e && typeof e == "object" && !Array.isArray(e) && "type" in e;
}
function Le(e) {
  const a = Array.isArray(e) ? e : e == null ? [] : [e];
  return {
    chain: a.filter((i) => !Y(i)),
    legacy: a.filter(Y)
  };
}
function X(e, a) {
  const i = [...e, ...a];
  return i.length ? i : void 0;
}
function Ie(e) {
  const a = String(e.type), i = e.command ?? e.message ?? e.title ?? e.material;
  return typeof i == "string" ? `${a} · ${i}` : a;
}
const De = [
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
function P(e) {
  return re.find((a) => a.type === e);
}
function se(e) {
  const a = {};
  for (const i of P(e)?.params ?? [])
    i.kind === "int" && (a[i.name] = 1);
  return a;
}
function oe(e) {
  return e.amount ?? e.distance;
}
function le(e, a) {
  const i = P(e);
  return (i ? i.params.filter((r) => r.kind !== "int").map((r) => r.name) : Object.keys(a).filter((r) => typeof a[r] == "string")).map((r) => a[r]).filter((r) => typeof r == "string" && r !== "").join(" = ");
}
function Pe(e, a) {
  return (P(e)?.params ?? []).filter((i) => i.required && (a[i.name] === void 0 || a[i.name] === "")).map((i) => i.name);
}
const Ke = [
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
], Me = `
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
`, He = /^(\d+|(\d+[dhms])+)$/, ze = ["actor_load", "period_reset", "quest_complete"], Ge = (() => {
  try {
    return Intl.supportedValuesOf?.("timeZone") ?? [];
  } catch {
    return [];
  }
})();
function Ue({ projectId: e, documentId: a, document: i, item: t }) {
  he("msquests", Me);
  const r = i, s = ye(e), d = fe(), f = Te(), [u, c] = J(t ?? null);
  Ne(() => {
    c(t ?? null);
  }, [t]);
  const l = (b) => {
    c(b), ge({ documentId: a, item: b });
  }, m = { projectId: e, project: s, lang: d, t: f, edit: (b, o) => {
    const y = structuredClone(r);
    b(y), be({ projectId: e, kind: ae, id: a, doc: y, texts: o });
  } }, v = u ? r.quests.findIndex((b) => b.uid === u) : -1;
  return v >= 0 ? /* @__PURE__ */ p("div", { className: "aq", children: [
    /* @__PURE__ */ p(D, { variant: "ghost", size: "sm", className: "aq-back", onClick: () => l(null), children: [
      "← ",
      f("back")
    ] }),
    /* @__PURE__ */ n(We, { ctx: m, group: r, index: v })
  ] }) : /* @__PURE__ */ n("div", { className: "aq", children: /* @__PURE__ */ n(Qe, { ctx: m, group: r, onOpen: l }) });
}
function j({
  label: e,
  children: a,
  hint: i
}) {
  return (
    // biome-ignore lint/a11y/noLabelWithoutControl: the control is the child
    /* @__PURE__ */ p("label", { className: "aq-field", children: [
      /* @__PURE__ */ n("span", { className: "field-label", children: e }),
      a,
      i
    ] })
  );
}
function L({
  ctx: e,
  label: a,
  node: i,
  multiline: t
}) {
  const r = _(e.project, i.key, e.lang), s = (d) => e.edit(() => {
  }, [{ key: i.key, lang: e.lang, value: d }]);
  return /* @__PURE__ */ n(j, { label: `${a} · ${e.lang}`, children: t ? /* @__PURE__ */ n("textarea", { className: "textarea", value: r, onChange: (d) => s(d.target.value) }) : /* @__PURE__ */ n("input", { className: "input", value: r, onChange: (d) => s(d.target.value) }) });
}
function Fe(e) {
  return typeof e == "number" ? String(e) : typeof e == "string" ? e : "";
}
function S({
  label: e,
  value: a,
  onChange: i
}) {
  return /* @__PURE__ */ n(j, { label: e, children: /* @__PURE__ */ n(
    "input",
    {
      className: "input",
      type: "number",
      min: 0,
      step: 1,
      value: Fe(a),
      onChange: (t) => {
        const r = t.target.value.trim();
        i(r === "" ? void 0 : Math.max(0, Math.trunc(Number(r))));
      }
    }
  ) });
}
function N(e, a, i) {
  i === void 0 || i === "" ? delete e[a] : e[a] = i;
}
function B(e, a, i) {
  if (i < 0 || i >= e.length) return;
  const [t] = e.splice(a, 1);
  e.splice(i, 0, t);
}
function I({
  label: e,
  onClick: a,
  disabled: i,
  children: t
}) {
  return /* @__PURE__ */ n(
    "button",
    {
      type: "button",
      className: "aq-icon-btn",
      title: e,
      "aria-label": e,
      onClick: a,
      disabled: i,
      children: t
    }
  );
}
function W({
  t: e,
  index: a,
  count: i,
  onMove: t,
  onRemove: r
}) {
  return /* @__PURE__ */ p(H, { children: [
    /* @__PURE__ */ n(I, { label: e("moveUp"), disabled: a === 0, onClick: () => t(a - 1), children: "↑" }),
    /* @__PURE__ */ n(
      I,
      {
        label: e("moveDown"),
        disabled: a === i - 1,
        onClick: () => t(a + 1),
        children: "↓"
      }
    ),
    /* @__PURE__ */ n(I, { label: e("remove"), onClick: r, children: "×" })
  ] });
}
function Qe({
  ctx: e,
  group: a,
  onOpen: i
}) {
  const { t, edit: r, project: s, lang: d } = e, f = ve(), u = typeof a.reset.cron == "string" ? a.reset.cron : "", c = u ? Ae(u, f) : null, l = typeof a.reset.timezone == "string" ? a.reset.timezone : "", h = Array.isArray(a.distribution.triggers) ? a.distribution.triggers : [], m = a.distribution.tiers ?? {}, v = Object.keys(a.rest).filter((o) => o !== "on"), b = () => {
    const o = R(), y = z();
    r(
      (q) => {
        q.quests.push({
          key: M(
            "quest",
            q.quests.map((k) => k.key)
          ),
          uid: y,
          tier: "",
          title: { key: o },
          description: { key: R() },
          duration: "",
          stages: [de(1)],
          rest: {}
        });
      },
      [{ key: o, lang: d, value: t("newQuestTitle") }]
    ), i(y);
  };
  return /* @__PURE__ */ p(H, { children: [
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("group") }),
      /* @__PURE__ */ n(L, { ctx: e, label: t("title"), node: a.title }),
      /* @__PURE__ */ n(L, { ctx: e, label: t("description"), node: a.description, multiline: !0 }),
      /* @__PURE__ */ n(j, { label: t("actorType"), children: /* @__PURE__ */ p(
        "select",
        {
          className: "select",
          value: a.actorType,
          onChange: (o) => r((y) => {
            y.actorType = o.target.value;
          }),
          children: [
            /* @__PURE__ */ n("option", { value: "player", children: t("actor.player") }),
            /* @__PURE__ */ n("option", { value: "global", children: t("actor.global") }),
            !["player", "global"].includes(a.actorType) && /* @__PURE__ */ n("option", { value: a.actorType, children: a.actorType })
          ]
        }
      ) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("reset") }),
      /* @__PURE__ */ n(
        j,
        {
          label: t("cron"),
          hint: u ? _e(u) ? /* @__PURE__ */ p("span", { className: "aq-ok", children: [
            c ?? t("cronRaw"),
            l ? `, ${l}` : ""
          ] }) : /* @__PURE__ */ n("span", { className: "aq-err", children: t("cronInvalid") }) : /* @__PURE__ */ n("span", { className: "aq-hint", children: t("noReset") }),
          children: /* @__PURE__ */ n(
            "input",
            {
              className: "input",
              value: u,
              placeholder: "0 4 * * *",
              onChange: (o) => r((y) => N(y.reset, "cron", o.target.value))
            }
          )
        }
      ),
      /* @__PURE__ */ p(j, { label: t("timezone"), children: [
        /* @__PURE__ */ n(
          "input",
          {
            className: "input",
            list: "aq-timezones",
            value: l,
            placeholder: t("timezoneServer"),
            onChange: (o) => r((y) => N(y.reset, "timezone", o.target.value.trim()))
          }
        ),
        /* @__PURE__ */ n("datalist", { id: "aq-timezones", children: Ge.map((o) => /* @__PURE__ */ n("option", { value: o }, o)) })
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("distribution") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(j, { label: t("strategy"), children: /* @__PURE__ */ p(
          "select",
          {
            className: "select",
            value: typeof a.distribution.strategy == "string" ? a.distribution.strategy.toLowerCase() : "",
            onChange: (o) => r((y) => N(y.distribution, "strategy", o.target.value)),
            children: [
              /* @__PURE__ */ n("option", { value: "", children: "—" }),
              ["none", "sequential", "random"].map((o) => /* @__PURE__ */ n("option", { value: o, children: t(`strategy.${o}`) }, o))
            ]
          }
        ) }),
        /* @__PURE__ */ n(
          S,
          {
            label: t("amount"),
            value: a.distribution.amount,
            onChange: (o) => r((y) => N(y.distribution, "amount", o))
          }
        )
      ] }),
      /* @__PURE__ */ n("span", { className: "field-label", children: t("triggers") }),
      ze.map((o) => /* @__PURE__ */ p("label", { className: "aq-check", children: [
        /* @__PURE__ */ n(
          "input",
          {
            type: "checkbox",
            checked: h.includes(o),
            onChange: (y) => r((q) => {
              const k = Array.isArray(q.distribution.triggers) ? q.distribution.triggers : [], g = y.target.checked ? [...k, o] : k.filter((x) => x !== o);
              N(q.distribution, "triggers", g.length ? g : void 0);
            })
          }
        ),
        t(`trigger.${o}`)
      ] }, o)),
      a.tiers.length > 0 && /* @__PURE__ */ p(H, { children: [
        /* @__PURE__ */ n("span", { className: "field-label", children: t("perTier") }),
        /* @__PURE__ */ n("div", { className: "aq-row", children: a.tiers.map((o) => /* @__PURE__ */ n(
          S,
          {
            label: _(s, o.title.key, d) || o.key,
            value: m[o.key],
            onChange: (y) => r((q) => {
              const k = { ...q.distribution.tiers ?? {} };
              N(k, o.key, y), N(
                q.distribution,
                "tiers",
                Object.keys(k).length ? k : void 0
              );
            })
          },
          o.key
        )) })
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("limits") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(
          S,
          {
            label: t("maxActive"),
            value: a.limits.max_active,
            onChange: (o) => r((y) => N(y.limits, "max_active", o))
          }
        ),
        /* @__PURE__ */ n(
          S,
          {
            label: t("maxPerPeriod"),
            value: a.limits.max_per_period,
            onChange: (o) => r((y) => N(y.limits, "max_per_period", o))
          }
        )
      ] }),
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("rotation") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ p("label", { className: "aq-check", children: [
          /* @__PURE__ */ n(
            "input",
            {
              type: "checkbox",
              checked: a.rotation.enabled === !0,
              onChange: (o) => r((y) => N(y.rotation, "enabled", o.target.checked ? !0 : void 0))
            }
          ),
          t("rotationEnabled")
        ] }),
        /* @__PURE__ */ n(
          S,
          {
            label: t("maxPerPeriod"),
            value: a.rotation.max_per_period,
            onChange: (o) => r((y) => N(y.rotation, "max_per_period", o))
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ n("span", { className: "aq-h", children: t("tiers") }),
        /* @__PURE__ */ p(
          D,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => r((o) => {
              o.tiers.push({
                key: M(
                  "tier",
                  o.tiers.map((y) => y.key)
                ),
                title: { key: R() },
                rest: {}
              });
            }),
            children: [
              "+ ",
              t("addTier")
            ]
          }
        )
      ] }),
      a.tiers.map((o, y) => /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(j, { label: t("tierKey"), children: /* @__PURE__ */ n(
          "input",
          {
            className: "input",
            defaultValue: o.key,
            onBlur: (q) => {
              const k = G(q.target.value);
              k !== o.key && r((g) => {
                const x = M(
                  k,
                  g.tiers.map((K) => K.key)
                );
                g.tiers[y].key = x;
                for (const K of g.quests) K.tier === o.key && (K.tier = x);
                const T = g.distribution.tiers;
                T && o.key in T && (T[x] = T[o.key], delete T[o.key]);
              });
            }
          }
        ) }),
        /* @__PURE__ */ n(L, { ctx: e, label: t("title"), node: o.title }),
        /* @__PURE__ */ n(
          I,
          {
            label: t("remove"),
            onClick: () => r((q) => {
              q.tiers.splice(y, 1);
            }),
            children: "×"
          }
        )
      ] }, o.key))
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ p("span", { className: "aq-h", children: [
          t("quests"),
          " · ",
          a.quests.length
        ] }),
        /* @__PURE__ */ p(D, { size: "sm", variant: "primary", onClick: b, children: [
          "+ ",
          t("addQuest")
        ] })
      ] }),
      a.quests.length === 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: t("noQuests") }),
      /* @__PURE__ */ n("div", { className: "aq-list", children: a.quests.map((o, y) => /* @__PURE__ */ p("div", { className: "aq-item", children: [
        /* @__PURE__ */ p(
          "button",
          {
            type: "button",
            className: "aq-item-main",
            onClick: () => i(o.uid),
            title: t("open"),
            children: [
              /* @__PURE__ */ n("span", { className: "aq-item-title", children: _(s, o.title.key, d) || o.key }),
              /* @__PURE__ */ p("span", { className: "aq-item-sub", children: [
                t("questSummary", {
                  stages: o.stages.length,
                  objectives: o.stages.reduce((q, k) => q + k.objectives.length, 0)
                }),
                " · ",
                o.key,
                o.tier ? ` · ${o.tier}` : "",
                o.duration ? ` · ${o.duration}` : ""
              ] })
            ]
          }
        ),
        /* @__PURE__ */ n(
          W,
          {
            t,
            index: y,
            count: a.quests.length,
            onMove: (q) => r((k) => B(k.quests, y, q)),
            onRemove: () => r((q) => {
              q.quests.splice(y, 1);
            })
          }
        )
      ] }, o.uid)) }),
      v.length > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: t("kept", { fields: v.join(", ") }) })
    ] }),
    /* @__PURE__ */ n(Ve, { ctx: e, group: a })
  ] });
}
function ce({
  ctx: e,
  value: a,
  onChange: i
}) {
  const { chain: t, legacy: r } = Le(a);
  return /* @__PURE__ */ p("div", { className: "aq-chain", children: [
    /* @__PURE__ */ n(
      qe,
      {
        projectId: e.projectId,
        value: t,
        onChange: (s) => i(X(s, r))
      }
    ),
    r.length > 0 && /* @__PURE__ */ p("div", { className: "aq-legacy", children: [
      /* @__PURE__ */ n("span", { className: "aq-hint", children: e.t("legacySteps") }),
      r.map((s, d) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: a step has no identity
        /* @__PURE__ */ p("div", { className: "aq-legacy-row", children: [
          /* @__PURE__ */ n("code", { children: Ie(s) }),
          /* @__PURE__ */ n(
            I,
            {
              label: e.t("remove"),
              onClick: () => i(X(t, r.filter((f, u) => u !== d))),
              children: "×"
            }
          )
        ] }, d)
      ))
    ] })
  ] });
}
function Be(e) {
  return Array.isArray(e) ? e.length : e == null ? 0 : 1;
}
function Ve({ ctx: e, group: a }) {
  const { t: i, edit: t } = e, r = a.rest.on && typeof a.rest.on == "object" ? a.rest.on : {};
  return /* @__PURE__ */ p("section", { className: "aq-section", children: [
    /* @__PURE__ */ n("span", { className: "aq-h", children: i("hooks") }),
    /* @__PURE__ */ n("span", { className: "aq-hint", children: i("hooksHint") }),
    De.map((s) => {
      const d = Be(r[s]);
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
            onChange: (f) => t((u) => {
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
function We({ ctx: e, group: a, index: i }) {
  const { t, edit: r } = e, s = a.quests[i], d = (l, h) => r((m) => l(m.quests[i]), h), f = a.quests.some((l, h) => h !== i && l.key === s.key), u = s.duration !== "" && !He.test(s.duration.replace(/\s/g, "").toLowerCase()), c = Object.keys(s.rest).filter((l) => l !== "rewards");
  return /* @__PURE__ */ p(H, { children: [
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("general") }),
      /* @__PURE__ */ n(L, { ctx: e, label: t("title"), node: s.title }),
      /* @__PURE__ */ n(L, { ctx: e, label: t("description"), node: s.description, multiline: !0 }),
      /* @__PURE__ */ p("div", { className: "aq-row aq-top", children: [
        /* @__PURE__ */ n(j, { label: t("tier"), children: /* @__PURE__ */ p(
          "select",
          {
            className: "select",
            value: s.tier,
            onChange: (l) => d((h) => {
              h.tier = l.target.value;
            }),
            children: [
              /* @__PURE__ */ n("option", { value: "", children: t("noTier") }),
              a.tiers.map((l) => /* @__PURE__ */ n("option", { value: l.key, children: _(e.project, l.title.key, e.lang) || l.key }, l.key)),
              s.tier && !a.tiers.some((l) => l.key === s.tier) && /* @__PURE__ */ n("option", { value: s.tier, children: s.tier })
            ]
          }
        ) }),
        /* @__PURE__ */ n(
          j,
          {
            label: t("duration"),
            hint: /* @__PURE__ */ n("span", { className: u ? "aq-err" : "aq-hint", children: t(u ? "durationInvalid" : "durationHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input",
                value: s.duration,
                placeholder: "24h",
                onChange: (l) => d((h) => {
                  h.duration = l.target.value.trim();
                })
              }
            )
          }
        )
      ] }),
      /* @__PURE__ */ p("details", { className: "aq-fold", open: f || void 0, children: [
        /* @__PURE__ */ n("summary", { children: t("more") }),
        /* @__PURE__ */ n(
          j,
          {
            label: t("key"),
            hint: /* @__PURE__ */ n("span", { className: f ? "aq-err" : "aq-hint", children: t(f ? "keyTaken" : "questKeyHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input aq-mono",
                value: s.key,
                onChange: (l) => d((h) => {
                  h.key = ne(l.target.value);
                }),
                onBlur: () => {
                  const l = G(s.key);
                  l !== s.key && d((h) => {
                    h.key = l;
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
          t("stages"),
          " · ",
          s.stages.length
        ] }),
        /* @__PURE__ */ p(
          D,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => d((l) => {
              const h = M(
                `stage_${l.stages.length + 1}`,
                l.stages.map((m) => m.key)
              );
              l.stages.push({ ...de(l.stages.length + 1), key: h });
            }),
            children: [
              "+ ",
              t("addStage")
            ]
          }
        )
      ] }),
      /* @__PURE__ */ n("ol", { className: "aq-stages", children: s.stages.map((l, h) => /* @__PURE__ */ n(
        Ze,
        {
          ctx: e,
          quest: s,
          stage: l,
          si: h,
          editQuest: d
        },
        `${l.key}-${h}`
      )) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("rewards") }),
      /* @__PURE__ */ n("span", { className: "aq-hint", children: t("rewardsHint") }),
      /* @__PURE__ */ n(
        ce,
        {
          ctx: e,
          value: s.rest.rewards,
          onChange: (l) => d((h) => N(h.rest, "rewards", l))
        }
      ),
      c.length > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: t("kept", { fields: c.join(", ") }) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("preview") }),
      /* @__PURE__ */ n(Je, { ctx: e, quest: s })
    ] })
  ] });
}
function Ze({
  ctx: e,
  quest: a,
  stage: i,
  si: t,
  editQuest: r
}) {
  const { t: s } = e, [d, f] = J(null), u = (m) => r((v) => m(v.stages[t])), c = _(e.project, i.title.key, e.lang), l = i.flow || "parallel", h = a.stages.flatMap((m) => m.objectives.map((v) => v.key));
  return /* @__PURE__ */ p("li", { className: "aq-stage", children: [
    /* @__PURE__ */ p("div", { className: "aq-stage-head", children: [
      /* @__PURE__ */ n("span", { className: "aq-stage-n", children: t + 1 }),
      /* @__PURE__ */ n(
        "input",
        {
          className: "input aq-stage-title",
          value: c,
          placeholder: s("stageTitle"),
          "aria-label": s("stage", { n: t + 1 }),
          onChange: (m) => e.edit(() => {
          }, [{ key: i.title.key, lang: e.lang, value: m.target.value }])
        }
      ),
      /* @__PURE__ */ n(
        W,
        {
          t: s,
          index: t,
          count: a.stages.length,
          onMove: (m) => r((v) => B(v.stages, t, m)),
          onRemove: () => r((m) => {
            m.stages.splice(t, 1);
          })
        }
      )
    ] }),
    i.objectives.length > 1 && /* @__PURE__ */ n("div", { className: "aq-seg", role: "radiogroup", "aria-label": s("flow"), children: ["parallel", "sequential"].map((m) => /* @__PURE__ */ n(
      "button",
      {
        type: "button",
        role: "radio",
        "aria-checked": l === m,
        className: `chip${l === m ? " is-active" : ""}`,
        onClick: () => u((v) => {
          v.flow = m;
        }),
        children: s(`flow.${m}`)
      },
      m
    )) }),
    i.objectives.length === 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: s("noObjectives") }),
    /* @__PURE__ */ n("ul", { className: "aq-objectives", children: i.objectives.map((m, v) => /* @__PURE__ */ n(
      Ye,
      {
        t: s,
        n: l === "sequential" ? `${t + 1}.${v + 1}` : "•",
        objective: m,
        open: d === m.key,
        onToggle: () => f(d === m.key ? null : m.key),
        keyTaken: h.filter((b) => b === m.key).length > 1,
        index: v,
        count: i.objectives.length,
        onMove: (b) => u((o) => B(o.objectives, v, b)),
        onChange: (b) => u((o) => b(o.objectives[v])),
        onRename: (b) => {
          d === m.key && f(b);
        },
        onRemove: () => u((b) => {
          b.objectives.splice(v, 1);
        })
      },
      `${m.key}-${v}`
    )) }),
    /* @__PURE__ */ p(
      D,
      {
        size: "sm",
        variant: "ghost",
        className: "aq-add",
        onClick: () => {
          const m = Ee("block_break", h);
          r((v) => {
            v.stages[t].objectives.push({
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
function ue(e, a) {
  return P(a) ? e(`objective.${a}`) : a;
}
function Ye({
  t: e,
  n: a,
  objective: i,
  open: t,
  onToggle: r,
  keyTaken: s,
  index: d,
  count: f,
  onMove: u,
  onChange: c,
  onRename: l,
  onRemove: h
}) {
  const m = P(i.type), v = m ? m.params : Object.keys(i.params).map((g) => ({ name: g, kind: "text" })), b = oe(i.params), o = le(i.type, i.params), y = Pe(i.type, i.params), q = s || y.length > 0, k = (g) => /* @__PURE__ */ n(
    Xe,
    {
      t: e,
      spec: g,
      value: i.params[g.name],
      onChange: (x) => c((T) => N(T.params, g.name, x))
    },
    g.name
  );
  return /* @__PURE__ */ p("li", { className: `aq-obj${t ? " is-open" : ""}${q ? " has-problem" : ""}`, children: [
    /* @__PURE__ */ p("div", { className: "aq-obj-line", children: [
      /* @__PURE__ */ p("button", { type: "button", className: "aq-obj-main", onClick: r, "aria-expanded": t, children: [
        /* @__PURE__ */ n("span", { className: "aq-obj-n", children: a }),
        /* @__PURE__ */ n("span", { className: "aq-obj-icon", "aria-hidden": !0, children: m?.icon ?? "?" }),
        /* @__PURE__ */ p("span", { className: "aq-obj-text", children: [
          ue(e, i.type),
          b !== void 0 && /* @__PURE__ */ p("strong", { children: [
            " · ",
            String(b)
          ] }),
          o ? ` × ${o}` : b !== void 0 ? ` × ${e("any")}` : ""
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
    t && /* @__PURE__ */ p("div", { className: "aq-obj-form", children: [
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
      /* @__PURE__ */ n("div", { className: "aq-row aq-wrap", children: v.filter((g) => !g.advanced).map(k) }),
      y.length > 0 && /* @__PURE__ */ n("span", { className: "aq-warn", children: e("missing", {
        fields: y.map((g) => e(`param.${g}`)).join(", ")
      }) }),
      /* @__PURE__ */ p("details", { className: "aq-fold", open: s || void 0, children: [
        /* @__PURE__ */ n("summary", { children: e("more") }),
        v.filter((g) => g.advanced).map(k),
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
                  }), l(x);
                },
                onBlur: () => {
                  const g = G(i.key);
                  g !== i.key && (c((x) => {
                    x.key = g;
                  }), l(g));
                }
              }
            )
          }
        )
      ] })
    ] })
  ] });
}
function Xe({
  t: e,
  spec: a,
  value: i,
  onChange: t
}) {
  const r = e(`param.${a.name}`), s = a.required && (i === void 0 || i === ""), d = typeof i == "string" || typeof i == "number" ? String(i) : "";
  if (a.kind === "int")
    return /* @__PURE__ */ n(S, { label: r, value: i, onChange: t });
  const f = s ? /* @__PURE__ */ n("span", { className: "aq-warn", children: e("required") }) : a.name === "itemKey" ? /* @__PURE__ */ n("span", { className: "aq-hint", children: e("itemKeyHint") }) : void 0;
  if (a.kind === "material")
    return /* @__PURE__ */ n(j, { label: r, hint: f, children: /* @__PURE__ */ n(
      ke,
      {
        value: d,
        placeholder: e("any"),
        "aria-label": r,
        onChange: (c) => t(c.toUpperCase().replace(/\s+/g, "_"))
      }
    ) });
  const u = a.kind === "entity";
  return /* @__PURE__ */ p(j, { label: r, hint: f, children: [
    /* @__PURE__ */ n(
      "input",
      {
        className: `input${u ? " aq-mono" : ""}`,
        list: u ? "aq-entities" : void 0,
        value: d,
        placeholder: u ? e("any") : "",
        onChange: (c) => t(u ? c.target.value.toUpperCase().replace(/\s+/g, "_") : c.target.value)
      }
    ),
    u && /* @__PURE__ */ n("datalist", { id: "aq-entities", children: Ke.map((c) => /* @__PURE__ */ n("option", { value: c }, c)) })
  ] });
}
function Je({ ctx: e, quest: a }) {
  const { t: i, project: t, lang: r } = e, s = (d) => {
    const f = le(d.type, d.params), u = oe(d.params);
    return `${ue(i, d.type)}${f ? ` · ${f}` : ""}${u !== void 0 ? ` · 0/${String(u)}` : ""}`;
  };
  return /* @__PURE__ */ p("div", { className: "aq-preview", children: [
    /* @__PURE__ */ n("span", { className: "aq-preview-title", children: _(t, a.title.key, r) || a.key }),
    _(t, a.description.key, r) && /* @__PURE__ */ n("span", { className: "aq-preview-line", children: _(t, a.description.key, r) }),
    a.stages.map((d, f) => (
      // biome-ignore lint/suspicious/noArrayIndexKey: stages are listed in order
      /* @__PURE__ */ p("span", { className: "aq-preview-stage", children: [
        a.stages.length > 1 && /* @__PURE__ */ n("span", { className: "aq-preview-line", children: _(t, d.title.key, r) || i("stage", { n: f + 1 }) }),
        d.objectives.map((u) => /* @__PURE__ */ p("span", { className: "aq-preview-line", children: [
          "• ",
          s(u)
        ] }, u.key))
      ] }, f)
    ))
  ] });
}
const et = {
  kind: ae,
  dir: ie,
  schemaVersion: V,
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: Z.en,
  labels: Z,
  component: Ue,
  title: (e, { project: a, lang: i }) => _(a, e.title.key, i),
  create: ({ id: e, projectId: a }) => ({
    id: e,
    projectId: a,
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
  parse: (e, a) => $e(e, a),
  serialize: (e, a) => Re(e, a),
  ltextSites: (e) => Oe(e),
  board: {
    size: { width: 220, height: 48 },
    // The group's quests, one selectable row each: the Properties panel opens the one clicked.
    items: (e, { project: a, lang: i }) => e.quests.map((t) => ({
      id: t.uid,
      label: _(a, t.title.key, i) || t.key,
      detail: t.tier || void 0
    })),
    summary: (e) => `quest_group · ${e.quests.length}`
  }
}, tt = {
  id: "msquests",
  displayName: "Quests",
  schemaVersion: "quest_group/v1",
  sdk: xe,
  sidebar: { icon: () => null, label: "Quests", to: "/m/msquests" },
  // Quests are Documents: they are edited from the project, no screen of their own.
  routes: [],
  documents: [et]
}, rt = { ...tt, sdk: "1.2.0" };
export {
  rt as default
};
