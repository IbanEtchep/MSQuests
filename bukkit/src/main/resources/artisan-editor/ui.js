import { createModuleI18n as me, fieldText as he, newTextKey as O, injectModuleStyles as ye, useProject as fe, useContentLang as ge, Button as P, selectDocumentItem as be, textValue as _, updateDocument as ve, useUiLang as qe, ActionChainEditor as ke, ItemInput as xe, MaterialInput as Ne, SDK_VERSION as je } from "@artisan/editor-sdk";
import { jsxs as p, jsx as n, Fragment as H } from "react/jsx-runtime";
import { useState as J, useEffect as we } from "react";
const _e = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"]
}, Ae = /^\d+$/;
function U(e, a, i) {
  const t = String(i).padStart(2, "0");
  return e === "fr" ? i === 0 ? `${a} h` : `${a} h ${t}` : `${String(a).padStart(2, "0")}:${t}`;
}
function $(e, a, i) {
  return Ae.test(e) && Number(e) >= a && Number(e) <= i;
}
function Ee(e) {
  return e.trim().split(/\s+/).length === 5;
}
function Te(e, a) {
  const i = a === "fr", t = e.trim().split(/\s+/);
  if (t.length !== 5) return null;
  const [r, s, c, y, d] = t;
  if (y !== "*") return null;
  const u = /^\*\/(\d+)$/;
  if (c === "*" && d === "*") {
    if ($(r, 0, 59) && $(s, 0, 23))
      return i ? `tous les jours à ${U(a, +s, +r)}` : `every day at ${U(a, +s, +r)}`;
    const m = u.exec(s);
    if ($(r, 0, 59) && m)
      return i ? `toutes les ${m[1]} h, à la minute ${r}` : `every ${m[1]} hours, at minute ${r}`;
    if ($(r, 0, 59) && s === "*")
      return i ? `toutes les heures, à la minute ${r}` : `every hour, at minute ${r}`;
    const h = u.exec(r);
    return h && s === "*" ? i ? `toutes les ${h[1]} minutes` : `every ${h[1]} minutes` : null;
  }
  if (!$(r, 0, 59) || !$(s, 0, 23)) return null;
  const o = U(a, +s, +r);
  if (c === "*" && $(d, 0, 7)) {
    const m = _e[i ? "fr" : "en"][Number(d) % 7];
    return i ? `chaque ${m} à ${o}` : `every ${m} at ${o}`;
  }
  return d === "*" && $(c, 1, 31) ? i ? `le ${c === "1" ? "1er" : c} de chaque mois à ${o}` : `on day ${c} of each month at ${o}` : null;
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
  "param.item": "Item",
  "param.crop": "Crop",
  "param.fish_type": "Fish",
  "param.amount": "Amount",
  "param.itemKey": "Custom item key",
  "param.entity_type": "Entity",
  "param.distance": "Distance (blocks)",
  "param.command": "Command",
  "param.placeholder": "Placeholder",
  "param.value": "Value",
  any: "any",
  anyItem: "Empty: any item counts."
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
  "param.item": "Item",
  "param.crop": "Culture",
  "param.fish_type": "Poisson",
  "param.amount": "Quantité",
  "param.itemKey": "Clé d'objet personnalisé",
  "param.entity_type": "Créature",
  "param.distance": "Distance (blocs)",
  "param.command": "Commande",
  "param.placeholder": "Placeholder",
  "param.value": "Valeur",
  any: "n'importe lequel",
  anyItem: "Vide : n’importe quel item compte."
}, Z = { en: ee.kindLabel, fr: te.kindLabel }, $e = me({ en: ee, fr: te }), ae = "quest_group", ie = "quest_groups", V = "quest_group/v1", Q = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
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
function Ce(e, a) {
  const i = new Set(a), t = new Uint8Array(4);
  for (; ; ) {
    crypto.getRandomValues(t);
    const r = Array.from(t, (c) => Q[c % 32].toLowerCase()).join(""), s = `${G(e)}_${r}`;
    if (!i.has(s)) return s;
  }
}
function ne(e) {
  return e.toLowerCase().replace(/[^a-z0-9_]/g, "_");
}
function G(e) {
  return e.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().replace(/[^a-z0-9_]+/g, "_").replace(/^_+|_+$/g, "") || "quest";
}
function T(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? { ...e } : {};
}
function F(e) {
  return Array.isArray(e) ? e : [];
}
function w(e) {
  return typeof e == "string" || typeof e == "number" || typeof e == "boolean" ? String(e) : "";
}
function I(e, ...a) {
  const i = { ...e };
  for (const t of a) delete i[t];
  return i;
}
function Se(e, a) {
  const i = {}, t = [];
  function r(d) {
    if (d && typeof d == "object" && !Array.isArray(d)) {
      const m = O(), h = {};
      for (const [b, v] of Object.entries(d)) h[b] = w(v);
      return i[m] = h, { key: m };
    }
    const u = typeof d == "string" ? /^\{i18n:(.+)\}$/.exec(d) : null;
    if (u) {
      const m = a.translations?.[u[1]];
      return m && (i[u[1]] = { ...m }), { key: u[1] };
    }
    const o = O();
    return i[o] = d == null || d === "" ? {} : { [a.lang]: w(d) }, { key: o };
  }
  const s = Object.entries(T(e.tiers)).map(([d, u]) => {
    const o = T(u);
    return { key: d, title: r(o.title), rest: I(o, "title") };
  }), c = F(e.quests).map((d, u) => {
    const o = T(d), m = F(o.stages).map((h, b) => {
      const v = T(h), l = w(v.flow).toLowerCase();
      return {
        key: w(v.key) || `stage_${b + 1}`,
        title: r(v.title),
        flow: l === "parallel" || l === "sequential" ? l : "",
        objectives: F(v.objectives).map((f, q) => {
          const k = T(f);
          return {
            key: w(k.key) || `objective_${q + 1}`,
            type: w(k.type),
            params: I(k, "key", "type")
          };
        }),
        rest: I(v, "key", "title", "flow", "objectives")
      };
    });
    return {
      key: w(o.key) || `quest_${u + 1}`,
      uid: w(o.uid) || z(),
      tier: w(o.tier),
      title: r(o.title),
      description: r(o.description),
      duration: w(o.duration),
      stages: m,
      rest: I(o, "key", "uid", "tier", "title", "description", "duration", "stages")
    };
  }), y = {
    id: w(e.id),
    projectId: a.projectId,
    uid: w(e.uid) || z(),
    title: r(e.title),
    description: r(e.description),
    actorType: w(e.actor_type) || "player",
    reset: T(e.reset),
    limits: T(e.limits),
    rotation: T(e.rotation),
    distribution: T(e.distribution),
    tiers: s,
    quests: c,
    rest: I(
      T(e),
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
  return e.schema !== void 0 && e.schema !== V && t.push(`schema: ${String(e.schema)}`), { doc: y, i18nKeys: i, unsupported: t };
}
function Re(e) {
  return typeof e == "string" ? e === "" : Object.values(e).every((a) => a === "");
}
function C(e) {
  const a = {};
  for (const [i, t] of Object.entries(e))
    t == null || t === "" || Array.isArray(t) && t.length === 0 || typeof t == "object" && !Array.isArray(t) && Object.keys(t).length === 0 || (a[i] = t);
  return a;
}
const Oe = [
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
function Ie(e, a) {
  const i = (u) => he(a, u.key), t = (u) => {
    const o = i(u);
    return Re(o) ? void 0 : o;
  }, r = {};
  for (const u of e.tiers) r[u.key] = { ...u.rest, title: i(u.title) };
  const s = e.quests.map(
    (u) => C({
      key: u.key,
      uid: u.uid,
      tier: u.tier,
      title: i(u.title),
      description: t(u.description),
      duration: u.duration,
      ...u.rest,
      stages: u.stages.map(
        (o) => C({
          key: o.key,
          title: t(o.title),
          flow: o.flow,
          ...o.rest,
          objectives: o.objectives.map((m) => ({ key: m.key, type: m.type, ...m.params }))
        })
      )
    })
  ), c = {
    ...e.rest,
    id: e.id,
    schema: V,
    uid: e.uid,
    title: i(e.title),
    description: t(e.description),
    actor_type: e.actorType,
    reset: C(e.reset),
    limits: C(e.limits),
    rotation: C(e.rotation),
    distribution: C(e.distribution),
    tiers: r,
    quests: s
  }, y = {};
  for (const u of Oe) u in c && (y[u] = c[u]);
  for (const u of Object.keys(c)) u in y || (y[u] = c[u]);
  const d = C(y);
  return d.quests = s, d;
}
function* Le(e) {
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
    const c = `${a}.quests.${s}`;
    yield {
      lText: r.title,
      key: `${c}.title`,
      path: `quests[${t}].title`,
      kind: "quest.title"
    }, yield {
      lText: r.description,
      key: `${c}.description`,
      path: `quests[${t}].description`,
      kind: "quest.description"
    };
    const y = /* @__PURE__ */ new Set();
    for (let d = 0; d < r.stages.length; d++) {
      const u = r.stages[d], o = y.has(u.key) ? `${u.key}_${d}` : u.key;
      y.add(u.key), yield {
        lText: u.title,
        key: `${c}.stages.${o}.title`,
        path: `quests[${t}].stages[${d}].title`,
        kind: "quest.stage"
      };
    }
  }
}
function Y(e) {
  return !!e && typeof e == "object" && !Array.isArray(e) && "type" in e;
}
function De(e) {
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
function Pe(e) {
  const a = String(e.type), i = e.command ?? e.message ?? e.title ?? e.material;
  return typeof i == "string" ? `${a} · ${i}` : a;
}
const Ke = [
  "distribution",
  "actor_load",
  "quest_start",
  "objective_progress",
  "objective_complete",
  "quest_complete",
  "period_complete"
], S = { name: "amount", kind: "int", required: !0 }, re = [
  { type: "block_break", icon: "⛏", params: [{ name: "material", kind: "material" }, S] },
  {
    type: "craft_item",
    icon: "⚒",
    params: [{ name: "item", kind: "item", legacy: "material" }, S]
  },
  {
    type: "harvest_crop",
    icon: "🌾",
    params: [{ name: "crop", kind: "material", required: !0 }, S]
  },
  {
    type: "fishing",
    icon: "🎣",
    params: [{ name: "item", kind: "item", legacy: "fish_type" }, S]
  },
  {
    type: "deliver_item",
    icon: "📦",
    params: [
      { name: "item", kind: "item", legacy: "material" },
      S,
      { name: "itemKey", kind: "text", advanced: !0 }
    ]
  },
  { type: "kill_entity", icon: "⚔", params: [{ name: "entity_type", kind: "entity" }, S] },
  { type: "breed_animal", icon: "♥", params: [{ name: "entity_type", kind: "entity" }, S] },
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
  return re.find((a) => a.type === e);
}
function se(e) {
  const a = {};
  for (const i of K(e)?.params ?? [])
    i.kind === "int" && (a[i.name] = 1);
  return a;
}
function oe(e) {
  return e.amount ?? e.distance;
}
function le(e, a) {
  const i = e[a.name];
  if (i && typeof i == "object" && !Array.isArray(i)) return i;
  const t = a.legacy ? e[a.legacy] : void 0;
  return typeof t == "string" && t !== "" ? { material: t } : {};
}
function Me(e, a, i, t) {
  a.legacy && delete e[a.legacy];
  const r = !i || Object.keys(i).length === 0;
  r ? delete e[a.name] : e[a.name] = i, r || t === void 0 ? delete e.match : e.match = t;
}
function He(e) {
  const a = e.title;
  if (typeof a == "string" && a.trim() !== "") return a.replace(/<[^>]*>/g, "");
  const i = e.material ?? e.item;
  return typeof i == "string" ? i : "";
}
function ce(e, a) {
  const i = K(e);
  return i ? i.params.filter((t) => t.kind !== "int").map((t) => t.kind === "item" ? He(le(a, t)) : a[t.name]).filter((t) => typeof t == "string" && t !== "").join(" = ") : Object.keys(a).map((t) => a[t]).filter((t) => typeof t == "string" && t !== "").join(" = ");
}
function ze(e, a) {
  return (K(e)?.params ?? []).filter((i) => i.required && (a[i.name] === void 0 || a[i.name] === "")).map((i) => i.name);
}
const Ge = [
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
], Ue = `
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
`, Fe = /^(\d+|(\d+[dhms])+)$/, Qe = ["actor_load", "period_reset", "quest_complete"], Be = (() => {
  try {
    return Intl.supportedValuesOf?.("timeZone") ?? [];
  } catch {
    return [];
  }
})();
function Ve({ projectId: e, documentId: a, document: i, item: t }) {
  ye("msquests", Ue);
  const r = i, s = fe(e), c = ge(), y = $e(), [d, u] = J(t ?? null);
  we(() => {
    u(t ?? null);
  }, [t]);
  const o = (v) => {
    u(v), be({ documentId: a, item: v });
  }, h = { projectId: e, project: s, lang: c, t: y, edit: (v, l) => {
    const f = structuredClone(r);
    v(f), ve({ projectId: e, kind: ae, id: a, doc: f, texts: l });
  } }, b = d ? r.quests.findIndex((v) => v.uid === d) : -1;
  return b >= 0 ? /* @__PURE__ */ p("div", { className: "aq", children: [
    /* @__PURE__ */ p(P, { variant: "ghost", size: "sm", className: "aq-back", onClick: () => o(null), children: [
      "← ",
      y("back")
    ] }),
    /* @__PURE__ */ n(Je, { ctx: h, group: r, index: b })
  ] }) : /* @__PURE__ */ n("div", { className: "aq", children: /* @__PURE__ */ n(Ze, { ctx: h, group: r, onOpen: o }) });
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
  const r = _(e.project, i.key, e.lang), s = (c) => e.edit(() => {
  }, [{ key: i.key, lang: e.lang, value: c }]);
  return /* @__PURE__ */ n(j, { label: `${a} · ${e.lang}`, children: t ? /* @__PURE__ */ n("textarea", { className: "textarea", value: r, onChange: (c) => s(c.target.value) }) : /* @__PURE__ */ n("input", { className: "input", value: r, onChange: (c) => s(c.target.value) }) });
}
function We(e) {
  return typeof e == "number" ? String(e) : typeof e == "string" ? e : "";
}
function R({
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
      value: We(a),
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
function D({
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
    /* @__PURE__ */ n(D, { label: e("moveUp"), disabled: a === 0, onClick: () => t(a - 1), children: "↑" }),
    /* @__PURE__ */ n(
      D,
      {
        label: e("moveDown"),
        disabled: a === i - 1,
        onClick: () => t(a + 1),
        children: "↓"
      }
    ),
    /* @__PURE__ */ n(D, { label: e("remove"), onClick: r, children: "×" })
  ] });
}
function Ze({
  ctx: e,
  group: a,
  onOpen: i
}) {
  const { t, edit: r, project: s, lang: c } = e, y = qe(), d = typeof a.reset.cron == "string" ? a.reset.cron : "", u = d ? Te(d, y) : null, o = typeof a.reset.timezone == "string" ? a.reset.timezone : "", m = Array.isArray(a.distribution.triggers) ? a.distribution.triggers : [], h = a.distribution.tiers ?? {}, b = Object.keys(a.rest).filter((l) => l !== "on"), v = () => {
    const l = O(), f = z();
    r(
      (q) => {
        q.quests.push({
          key: M(
            "quest",
            q.quests.map((k) => k.key)
          ),
          uid: f,
          tier: "",
          title: { key: l },
          description: { key: O() },
          duration: "",
          stages: [ue(1)],
          rest: {}
        });
      },
      [{ key: l, lang: c, value: t("newQuestTitle") }]
    ), i(f);
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
          onChange: (l) => r((f) => {
            f.actorType = l.target.value;
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
          hint: d ? Ee(d) ? /* @__PURE__ */ p("span", { className: "aq-ok", children: [
            u ?? t("cronRaw"),
            o ? `, ${o}` : ""
          ] }) : /* @__PURE__ */ n("span", { className: "aq-err", children: t("cronInvalid") }) : /* @__PURE__ */ n("span", { className: "aq-hint", children: t("noReset") }),
          children: /* @__PURE__ */ n(
            "input",
            {
              className: "input",
              value: d,
              placeholder: "0 4 * * *",
              onChange: (l) => r((f) => N(f.reset, "cron", l.target.value))
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
            value: o,
            placeholder: t("timezoneServer"),
            onChange: (l) => r((f) => N(f.reset, "timezone", l.target.value.trim()))
          }
        ),
        /* @__PURE__ */ n("datalist", { id: "aq-timezones", children: Be.map((l) => /* @__PURE__ */ n("option", { value: l }, l)) })
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
            onChange: (l) => r((f) => N(f.distribution, "strategy", l.target.value)),
            children: [
              /* @__PURE__ */ n("option", { value: "", children: "—" }),
              ["none", "sequential", "random"].map((l) => /* @__PURE__ */ n("option", { value: l, children: t(`strategy.${l}`) }, l))
            ]
          }
        ) }),
        /* @__PURE__ */ n(
          R,
          {
            label: t("amount"),
            value: a.distribution.amount,
            onChange: (l) => r((f) => N(f.distribution, "amount", l))
          }
        )
      ] }),
      /* @__PURE__ */ n("span", { className: "field-label", children: t("triggers") }),
      Qe.map((l) => /* @__PURE__ */ p("label", { className: "aq-check", children: [
        /* @__PURE__ */ n(
          "input",
          {
            type: "checkbox",
            checked: m.includes(l),
            onChange: (f) => r((q) => {
              const k = Array.isArray(q.distribution.triggers) ? q.distribution.triggers : [], A = f.target.checked ? [...k, l] : k.filter((g) => g !== l);
              N(q.distribution, "triggers", A.length ? A : void 0);
            })
          }
        ),
        t(`trigger.${l}`)
      ] }, l)),
      a.tiers.length > 0 && /* @__PURE__ */ p(H, { children: [
        /* @__PURE__ */ n("span", { className: "field-label", children: t("perTier") }),
        /* @__PURE__ */ n("div", { className: "aq-row", children: a.tiers.map((l) => /* @__PURE__ */ n(
          R,
          {
            label: _(s, l.title.key, c) || l.key,
            value: h[l.key],
            onChange: (f) => r((q) => {
              const k = { ...q.distribution.tiers ?? {} };
              N(k, l.key, f), N(
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
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("limits") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(
          R,
          {
            label: t("maxActive"),
            value: a.limits.max_active,
            onChange: (l) => r((f) => N(f.limits, "max_active", l))
          }
        ),
        /* @__PURE__ */ n(
          R,
          {
            label: t("maxPerPeriod"),
            value: a.limits.max_per_period,
            onChange: (l) => r((f) => N(f.limits, "max_per_period", l))
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
              onChange: (l) => r((f) => N(f.rotation, "enabled", l.target.checked ? !0 : void 0))
            }
          ),
          t("rotationEnabled")
        ] }),
        /* @__PURE__ */ n(
          R,
          {
            label: t("maxPerPeriod"),
            value: a.rotation.max_per_period,
            onChange: (l) => r((f) => N(f.rotation, "max_per_period", l))
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ n("span", { className: "aq-h", children: t("tiers") }),
        /* @__PURE__ */ p(
          P,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => r((l) => {
              l.tiers.push({
                key: M(
                  "tier",
                  l.tiers.map((f) => f.key)
                ),
                title: { key: O() },
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
      a.tiers.map((l, f) => /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(j, { label: t("tierKey"), children: /* @__PURE__ */ n(
          "input",
          {
            className: "input",
            defaultValue: l.key,
            onBlur: (q) => {
              const k = G(q.target.value);
              k !== l.key && r((A) => {
                const g = M(
                  k,
                  A.tiers.map((E) => E.key)
                );
                A.tiers[f].key = g;
                for (const E of A.quests) E.tier === l.key && (E.tier = g);
                const x = A.distribution.tiers;
                x && l.key in x && (x[g] = x[l.key], delete x[l.key]);
              });
            }
          }
        ) }),
        /* @__PURE__ */ n(L, { ctx: e, label: t("title"), node: l.title }),
        /* @__PURE__ */ n(
          D,
          {
            label: t("remove"),
            onClick: () => r((q) => {
              q.tiers.splice(f, 1);
            }),
            children: "×"
          }
        )
      ] }, l.key))
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ p("span", { className: "aq-h", children: [
          t("quests"),
          " · ",
          a.quests.length
        ] }),
        /* @__PURE__ */ p(P, { size: "sm", variant: "primary", onClick: v, children: [
          "+ ",
          t("addQuest")
        ] })
      ] }),
      a.quests.length === 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: t("noQuests") }),
      /* @__PURE__ */ n("div", { className: "aq-list", children: a.quests.map((l, f) => /* @__PURE__ */ p("div", { className: "aq-item", children: [
        /* @__PURE__ */ p(
          "button",
          {
            type: "button",
            className: "aq-item-main",
            onClick: () => i(l.uid),
            title: t("open"),
            children: [
              /* @__PURE__ */ n("span", { className: "aq-item-title", children: _(s, l.title.key, c) || l.key }),
              /* @__PURE__ */ p("span", { className: "aq-item-sub", children: [
                t("questSummary", {
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
            t,
            index: f,
            count: a.quests.length,
            onMove: (q) => r((k) => B(k.quests, f, q)),
            onRemove: () => r((q) => {
              q.quests.splice(f, 1);
            })
          }
        )
      ] }, l.uid)) }),
      b.length > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: t("kept", { fields: b.join(", ") }) })
    ] }),
    /* @__PURE__ */ n(Xe, { ctx: e, group: a })
  ] });
}
function de({
  ctx: e,
  value: a,
  onChange: i
}) {
  const { chain: t, legacy: r } = De(a);
  return /* @__PURE__ */ p("div", { className: "aq-chain", children: [
    /* @__PURE__ */ n(
      ke,
      {
        projectId: e.projectId,
        value: t,
        onChange: (s) => i(X(s, r))
      }
    ),
    r.length > 0 && /* @__PURE__ */ p("div", { className: "aq-legacy", children: [
      /* @__PURE__ */ n("span", { className: "aq-hint", children: e.t("legacySteps") }),
      r.map((s, c) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: a step has no identity
        /* @__PURE__ */ p("div", { className: "aq-legacy-row", children: [
          /* @__PURE__ */ n("code", { children: Pe(s) }),
          /* @__PURE__ */ n(
            D,
            {
              label: e.t("remove"),
              onClick: () => i(X(t, r.filter((y, d) => d !== c))),
              children: "×"
            }
          )
        ] }, c)
      ))
    ] })
  ] });
}
function Ye(e) {
  return Array.isArray(e) ? e.length : e == null ? 0 : 1;
}
function Xe({ ctx: e, group: a }) {
  const { t: i, edit: t } = e, r = a.rest.on && typeof a.rest.on == "object" ? a.rest.on : {};
  return /* @__PURE__ */ p("section", { className: "aq-section", children: [
    /* @__PURE__ */ n("span", { className: "aq-h", children: i("hooks") }),
    /* @__PURE__ */ n("span", { className: "aq-hint", children: i("hooksHint") }),
    Ke.map((s) => {
      const c = Ye(r[s]);
      return /* @__PURE__ */ p("details", { className: "aq-fold", children: [
        /* @__PURE__ */ p("summary", { children: [
          /* @__PURE__ */ n("span", { children: i(`hook.${s}`) }),
          c > 0 && /* @__PURE__ */ n("span", { className: "aq-badge", title: i("actionCount", { count: c }), children: c })
        ] }),
        /* @__PURE__ */ n(
          de,
          {
            ctx: e,
            value: r[s],
            onChange: (y) => t((d) => {
              const u = { ...d.rest.on ?? {} };
              N(u, s, y), N(d.rest, "on", Object.keys(u).length ? u : void 0);
            })
          }
        )
      ] }, s);
    })
  ] });
}
function ue(e) {
  return {
    key: `stage_${e}`,
    title: { key: O() },
    flow: "parallel",
    objectives: [],
    rest: {}
  };
}
function Je({ ctx: e, group: a, index: i }) {
  const { t, edit: r } = e, s = a.quests[i], c = (o, m) => r((h) => o(h.quests[i]), m), y = a.quests.some((o, m) => m !== i && o.key === s.key), d = s.duration !== "" && !Fe.test(s.duration.replace(/\s/g, "").toLowerCase()), u = Object.keys(s.rest).filter((o) => o !== "rewards");
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
            onChange: (o) => c((m) => {
              m.tier = o.target.value;
            }),
            children: [
              /* @__PURE__ */ n("option", { value: "", children: t("noTier") }),
              a.tiers.map((o) => /* @__PURE__ */ n("option", { value: o.key, children: _(e.project, o.title.key, e.lang) || o.key }, o.key)),
              s.tier && !a.tiers.some((o) => o.key === s.tier) && /* @__PURE__ */ n("option", { value: s.tier, children: s.tier })
            ]
          }
        ) }),
        /* @__PURE__ */ n(
          j,
          {
            label: t("duration"),
            hint: /* @__PURE__ */ n("span", { className: d ? "aq-err" : "aq-hint", children: t(d ? "durationInvalid" : "durationHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input",
                value: s.duration,
                placeholder: "24h",
                onChange: (o) => c((m) => {
                  m.duration = o.target.value.trim();
                })
              }
            )
          }
        )
      ] }),
      /* @__PURE__ */ p("details", { className: "aq-fold", open: y || void 0, children: [
        /* @__PURE__ */ n("summary", { children: t("more") }),
        /* @__PURE__ */ n(
          j,
          {
            label: t("key"),
            hint: /* @__PURE__ */ n("span", { className: y ? "aq-err" : "aq-hint", children: t(y ? "keyTaken" : "questKeyHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input aq-mono",
                value: s.key,
                onChange: (o) => c((m) => {
                  m.key = ne(o.target.value);
                }),
                onBlur: () => {
                  const o = G(s.key);
                  o !== s.key && c((m) => {
                    m.key = o;
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
          P,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => c((o) => {
              const m = M(
                `stage_${o.stages.length + 1}`,
                o.stages.map((h) => h.key)
              );
              o.stages.push({ ...ue(o.stages.length + 1), key: m });
            }),
            children: [
              "+ ",
              t("addStage")
            ]
          }
        )
      ] }),
      /* @__PURE__ */ n("ol", { className: "aq-stages", children: s.stages.map((o, m) => /* @__PURE__ */ n(
        et,
        {
          ctx: e,
          quest: s,
          stage: o,
          si: m,
          editQuest: c
        },
        `${o.key}-${m}`
      )) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("rewards") }),
      /* @__PURE__ */ n("span", { className: "aq-hint", children: t("rewardsHint") }),
      /* @__PURE__ */ n(
        de,
        {
          ctx: e,
          value: s.rest.rewards,
          onChange: (o) => c((m) => N(m.rest, "rewards", o))
        }
      ),
      u.length > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: t("kept", { fields: u.join(", ") }) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("preview") }),
      /* @__PURE__ */ n(nt, { ctx: e, quest: s })
    ] })
  ] });
}
function et({
  ctx: e,
  quest: a,
  stage: i,
  si: t,
  editQuest: r
}) {
  const { t: s } = e, [c, y] = J(null), d = (h) => r((b) => h(b.stages[t])), u = _(e.project, i.title.key, e.lang), o = i.flow || "parallel", m = a.stages.flatMap((h) => h.objectives.map((b) => b.key));
  return /* @__PURE__ */ p("li", { className: "aq-stage", children: [
    /* @__PURE__ */ p("div", { className: "aq-stage-head", children: [
      /* @__PURE__ */ n("span", { className: "aq-stage-n", children: t + 1 }),
      /* @__PURE__ */ n(
        "input",
        {
          className: "input aq-stage-title",
          value: u,
          placeholder: s("stageTitle"),
          "aria-label": s("stage", { n: t + 1 }),
          onChange: (h) => e.edit(() => {
          }, [{ key: i.title.key, lang: e.lang, value: h.target.value }])
        }
      ),
      /* @__PURE__ */ n(
        W,
        {
          t: s,
          index: t,
          count: a.stages.length,
          onMove: (h) => r((b) => B(b.stages, t, h)),
          onRemove: () => r((h) => {
            h.stages.splice(t, 1);
          })
        }
      )
    ] }),
    i.objectives.length > 1 && /* @__PURE__ */ n("div", { className: "aq-seg", role: "radiogroup", "aria-label": s("flow"), children: ["parallel", "sequential"].map((h) => /* @__PURE__ */ n(
      "button",
      {
        type: "button",
        role: "radio",
        "aria-checked": o === h,
        className: `chip${o === h ? " is-active" : ""}`,
        onClick: () => d((b) => {
          b.flow = h;
        }),
        children: s(`flow.${h}`)
      },
      h
    )) }),
    i.objectives.length === 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: s("noObjectives") }),
    /* @__PURE__ */ n("ul", { className: "aq-objectives", children: i.objectives.map((h, b) => /* @__PURE__ */ n(
      tt,
      {
        t: s,
        projectId: e.projectId,
        n: o === "sequential" ? `${t + 1}.${b + 1}` : "•",
        objective: h,
        open: c === h.key,
        onToggle: () => y(c === h.key ? null : h.key),
        keyTaken: m.filter((v) => v === h.key).length > 1,
        index: b,
        count: i.objectives.length,
        onMove: (v) => d((l) => B(l.objectives, b, v)),
        onChange: (v) => d((l) => v(l.objectives[b])),
        onRename: (v) => {
          c === h.key && y(v);
        },
        onRemove: () => d((v) => {
          v.objectives.splice(b, 1);
        })
      },
      `${h.key}-${b}`
    )) }),
    /* @__PURE__ */ p(
      P,
      {
        size: "sm",
        variant: "ghost",
        className: "aq-add",
        onClick: () => {
          const h = Ce("block_break", m);
          r((b) => {
            b.stages[t].objectives.push({
              key: h,
              type: "block_break",
              params: se("block_break")
            });
          }), y(h);
        },
        children: [
          "+ ",
          s("addObjective")
        ]
      }
    )
  ] });
}
function pe(e, a) {
  return K(a) ? e(`objective.${a}`) : a;
}
function tt({
  t: e,
  projectId: a,
  n: i,
  objective: t,
  open: r,
  onToggle: s,
  keyTaken: c,
  index: y,
  count: d,
  onMove: u,
  onChange: o,
  onRename: m,
  onRemove: h
}) {
  const b = K(t.type), v = b ? b.params : Object.keys(t.params).map((g) => ({ name: g, kind: "text" })), l = oe(t.params), f = ce(t.type, t.params), q = ze(t.type, t.params), k = c || q.length > 0, A = (g) => g.kind === "item" ? /* @__PURE__ */ n(
    at,
    {
      t: e,
      projectId: a,
      spec: g,
      params: t.params,
      onChange: (x) => o((E) => x(E.params))
    },
    g.name
  ) : /* @__PURE__ */ n(
    it,
    {
      t: e,
      spec: g,
      value: t.params[g.name],
      onChange: (x) => o((E) => N(E.params, g.name, x))
    },
    g.name
  );
  return /* @__PURE__ */ p("li", { className: `aq-obj${r ? " is-open" : ""}${k ? " has-problem" : ""}`, children: [
    /* @__PURE__ */ p("div", { className: "aq-obj-line", children: [
      /* @__PURE__ */ p("button", { type: "button", className: "aq-obj-main", onClick: s, "aria-expanded": r, children: [
        /* @__PURE__ */ n("span", { className: "aq-obj-n", children: i }),
        /* @__PURE__ */ n("span", { className: "aq-obj-icon", "aria-hidden": !0, children: b?.icon ?? "?" }),
        /* @__PURE__ */ p("span", { className: "aq-obj-text", children: [
          pe(e, t.type),
          l !== void 0 && /* @__PURE__ */ p("strong", { children: [
            " · ",
            String(l)
          ] }),
          f ? ` × ${f}` : l !== void 0 ? ` × ${e("any")}` : ""
        ] }),
        k && /* @__PURE__ */ n(
          "span",
          {
            className: "aq-obj-dot",
            title: c ? e("objectiveKeyTaken") : e("missing", {
              fields: q.map((g) => e(`param.${g}`)).join(", ")
            })
          }
        )
      ] }),
      /* @__PURE__ */ n(W, { t: e, index: y, count: d, onMove: u, onRemove: h })
    ] }),
    r && /* @__PURE__ */ p("div", { className: "aq-obj-form", children: [
      /* @__PURE__ */ n(j, { label: e("type"), children: /* @__PURE__ */ p(
        "select",
        {
          className: "select",
          value: t.type,
          onChange: (g) => o((x) => {
            x.type = g.target.value, x.params = se(x.type);
          }),
          children: [
            re.map((g) => /* @__PURE__ */ p("option", { value: g.type, children: [
              g.icon,
              " ",
              e(`objective.${g.type}`)
            ] }, g.type)),
            !b && /* @__PURE__ */ n("option", { value: t.type, children: t.type })
          ]
        }
      ) }),
      /* @__PURE__ */ n("div", { className: "aq-row aq-wrap", children: v.filter((g) => !g.advanced).map(A) }),
      q.length > 0 && /* @__PURE__ */ n("span", { className: "aq-warn", children: e("missing", {
        fields: q.map((g) => e(`param.${g}`)).join(", ")
      }) }),
      /* @__PURE__ */ p("details", { className: "aq-fold", open: c || void 0, children: [
        /* @__PURE__ */ n("summary", { children: e("more") }),
        v.filter((g) => g.advanced).map(A),
        /* @__PURE__ */ n(
          j,
          {
            label: e("key"),
            hint: /* @__PURE__ */ n("span", { className: c ? "aq-err" : "aq-hint", children: e(c ? "objectiveKeyTaken" : "objectiveKeyHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input aq-mono",
                value: t.key,
                onChange: (g) => {
                  const x = ne(g.target.value);
                  o((E) => {
                    E.key = x;
                  }), m(x);
                },
                onBlur: () => {
                  const g = G(t.key);
                  g !== t.key && (o((x) => {
                    x.key = g;
                  }), m(g));
                }
              }
            )
          }
        )
      ] })
    ] })
  ] });
}
function at({
  t: e,
  projectId: a,
  spec: i,
  params: t,
  onChange: r
}) {
  const s = le(t, i), c = Array.isArray(t.match) ? t.match : void 0, y = (d, u) => r((o) => Me(o, i, d, u));
  return /* @__PURE__ */ n(
    j,
    {
      label: e(`param.${i.name}`),
      hint: Object.keys(s).length === 0 ? /* @__PURE__ */ n("span", { className: "aq-hint", children: e("anyItem") }) : void 0,
      children: /* @__PURE__ */ n(
        xe,
        {
          projectId: a,
          value: s,
          match: c,
          onChange: (d) => y(d, c),
          onMatchChange: (d) => y(s, d)
        }
      )
    }
  );
}
function it({
  t: e,
  spec: a,
  value: i,
  onChange: t
}) {
  const r = e(`param.${a.name}`), s = a.required && (i === void 0 || i === ""), c = typeof i == "string" || typeof i == "number" ? String(i) : "";
  if (a.kind === "int")
    return /* @__PURE__ */ n(R, { label: r, value: i, onChange: t });
  const y = s ? /* @__PURE__ */ n("span", { className: "aq-warn", children: e("required") }) : a.name === "itemKey" ? /* @__PURE__ */ n("span", { className: "aq-hint", children: e("itemKeyHint") }) : void 0;
  if (a.kind === "material")
    return /* @__PURE__ */ n(j, { label: r, hint: y, children: /* @__PURE__ */ n(
      Ne,
      {
        value: c,
        placeholder: e("any"),
        "aria-label": r,
        onChange: (u) => t(u.toUpperCase().replace(/\s+/g, "_"))
      }
    ) });
  const d = a.kind === "entity";
  return /* @__PURE__ */ p(j, { label: r, hint: y, children: [
    /* @__PURE__ */ n(
      "input",
      {
        className: `input${d ? " aq-mono" : ""}`,
        list: d ? "aq-entities" : void 0,
        value: c,
        placeholder: d ? e("any") : "",
        onChange: (u) => t(d ? u.target.value.toUpperCase().replace(/\s+/g, "_") : u.target.value)
      }
    ),
    d && /* @__PURE__ */ n("datalist", { id: "aq-entities", children: Ge.map((u) => /* @__PURE__ */ n("option", { value: u }, u)) })
  ] });
}
function nt({ ctx: e, quest: a }) {
  const { t: i, project: t, lang: r } = e, s = (c) => {
    const y = ce(c.type, c.params), d = oe(c.params);
    return `${pe(i, c.type)}${y ? ` · ${y}` : ""}${d !== void 0 ? ` · 0/${String(d)}` : ""}`;
  };
  return /* @__PURE__ */ p("div", { className: "aq-preview", children: [
    /* @__PURE__ */ n("span", { className: "aq-preview-title", children: _(t, a.title.key, r) || a.key }),
    _(t, a.description.key, r) && /* @__PURE__ */ n("span", { className: "aq-preview-line", children: _(t, a.description.key, r) }),
    a.stages.map((c, y) => (
      // biome-ignore lint/suspicious/noArrayIndexKey: stages are listed in order
      /* @__PURE__ */ p("span", { className: "aq-preview-stage", children: [
        a.stages.length > 1 && /* @__PURE__ */ n("span", { className: "aq-preview-line", children: _(t, c.title.key, r) || i("stage", { n: y + 1 }) }),
        c.objectives.map((d) => /* @__PURE__ */ p("span", { className: "aq-preview-line", children: [
          "• ",
          s(d)
        ] }, d.key))
      ] }, y)
    ))
  ] });
}
const rt = {
  kind: ae,
  dir: ie,
  schemaVersion: V,
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: Z.en,
  labels: Z,
  component: Ve,
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
  parse: (e, a) => Se(e, a),
  serialize: (e, a) => Ie(e, a),
  ltextSites: (e) => Le(e),
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
}, st = {
  id: "msquests",
  displayName: "Quests",
  schemaVersion: "quest_group/v1",
  sdk: je,
  sidebar: { icon: () => null, label: "Quests", to: "/m/msquests" },
  // Quests are Documents: they are edited from the project, no screen of their own.
  routes: [],
  documents: [rt]
}, dt = { ...st, sdk: "1.3.0" };
export {
  dt as default
};
