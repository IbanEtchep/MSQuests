import { createModuleI18n as me, fieldText as he, newTextKey as R, injectModuleStyles as ye, useProject as fe, useContentLang as ge, Button as P, selectDocumentItem as be, textValue as A, updateDocument as ve, useUiLang as qe, ActionChainEditor as ke, ItemInput as xe, MaterialInput as Ne, SDK_VERSION as je } from "@artisan/editor-sdk";
import { jsxs as p, jsx as i, Fragment as z } from "react/jsx-runtime";
import { useState as J, useEffect as we } from "react";
const _e = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"]
}, Ae = /^\d+$/;
function U(e, a, n) {
  const t = String(n).padStart(2, "0");
  return e === "fr" ? n === 0 ? `${a} h` : `${a} h ${t}` : `${String(a).padStart(2, "0")}:${t}`;
}
function $(e, a, n) {
  return Ae.test(e) && Number(e) >= a && Number(e) <= n;
}
function Ee(e) {
  return e.trim().split(/\s+/).length === 5;
}
function Te(e, a) {
  const n = a === "fr", t = e.trim().split(/\s+/);
  if (t.length !== 5) return null;
  const [r, s, c, y, d] = t;
  if (y !== "*") return null;
  const u = /^\*\/(\d+)$/;
  if (c === "*" && d === "*") {
    if ($(r, 0, 59) && $(s, 0, 23))
      return n ? `tous les jours à ${U(a, +s, +r)}` : `every day at ${U(a, +s, +r)}`;
    const h = u.exec(s);
    if ($(r, 0, 59) && h)
      return n ? `toutes les ${h[1]} h, à la minute ${r}` : `every ${h[1]} hours, at minute ${r}`;
    if ($(r, 0, 59) && s === "*")
      return n ? `toutes les heures, à la minute ${r}` : `every hour, at minute ${r}`;
    const m = u.exec(r);
    return m && s === "*" ? n ? `toutes les ${m[1]} minutes` : `every ${m[1]} minutes` : null;
  }
  if (!$(r, 0, 59) || !$(s, 0, 23)) return null;
  const o = U(a, +s, +r);
  if (c === "*" && $(d, 0, 7)) {
    const h = _e[n ? "fr" : "en"][Number(d) % 7];
    return n ? `chaque ${h} à ${o}` : `every ${h} at ${o}`;
  }
  return d === "*" && $(c, 1, 31) ? n ? `le ${c === "1" ? "1er" : c} de chaque mois à ${o}` : `on day ${c} of each month at ${o}` : null;
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
  rewards: "When the quest is completed",
  rewardsHint: "Run when the quest is completed: any action Artisan or an installed plugin declares, with conditions; a plugin without an action (guild XP…) is reached with a command. Write what the player wins in the description.",
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
  rewards: "À la fin de la quête",
  rewardsHint: "Exécutées quand la quête est terminée : toute action d'Artisan ou d'un plugin installé, avec conditions ; un plugin sans action (XP de guilde…) passe par une commande. Écrivez ce que gagne le joueur dans la description.",
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
}, Z = { en: ee.kindLabel, fr: te.kindLabel }, $e = me({ en: ee, fr: te }), ae = "quest_group", ne = "quest_groups", V = "quest_group/v1", Q = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
function H(e = Date.now()) {
  let a = "", n = e;
  for (let s = 0; s < 10; s++)
    a = Q[n % 32] + a, n = Math.floor(n / 32);
  const t = new Uint8Array(16);
  crypto.getRandomValues(t);
  let r = "";
  for (let s = 0; s < 16; s++) r += Q[t[s] % 32];
  return a + r;
}
function M(e, a) {
  const n = new Set(a);
  if (!n.has(e)) return e;
  for (let t = 2; ; t++) if (!n.has(`${e}_${t}`)) return `${e}_${t}`;
}
function Ce(e, a) {
  const n = new Set(a), t = new Uint8Array(4);
  for (; ; ) {
    crypto.getRandomValues(t);
    const r = Array.from(t, (c) => Q[c % 32].toLowerCase()).join(""), s = `${G(e)}_${r}`;
    if (!n.has(s)) return s;
  }
}
function ie(e) {
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
function I(e, ...a) {
  const n = { ...e };
  for (const t of a) delete n[t];
  return n;
}
function Se(e, a) {
  const n = {}, t = [];
  function r(d) {
    if (d && typeof d == "object" && !Array.isArray(d)) {
      const h = R(), m = {};
      for (const [b, v] of Object.entries(d)) m[b] = w(v);
      return n[h] = m, { key: h };
    }
    const u = typeof d == "string" ? /^\{i18n:(.+)\}$/.exec(d) : null;
    if (u) {
      const h = a.translations?.[u[1]];
      return h && (n[u[1]] = { ...h }), { key: u[1] };
    }
    const o = R();
    return n[o] = d == null || d === "" ? {} : { [a.lang]: w(d) }, { key: o };
  }
  const s = Object.entries(_(e.tiers)).map(([d, u]) => {
    const o = _(u);
    return { key: d, title: r(o.title), rest: I(o, "title") };
  }), c = F(e.quests).map((d, u) => {
    const o = _(d), h = F(o.stages).map((m, b) => {
      const v = _(m), l = w(v.flow).toLowerCase();
      return {
        key: w(v.key) || `stage_${b + 1}`,
        title: r(v.title),
        flow: l === "parallel" || l === "sequential" ? l : "",
        objectives: F(v.objectives).map((f, q) => {
          const k = _(f);
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
      uid: w(o.uid) || H(),
      tier: w(o.tier),
      title: r(o.title),
      description: r(o.description),
      duration: w(o.duration),
      stages: h,
      rest: De(o)
    };
  }), y = {
    id: w(e.id),
    projectId: a.projectId,
    uid: w(e.uid) || H(),
    title: r(e.title),
    description: r(e.description),
    actorType: w(e.actor_type) || "player",
    reset: _(e.reset),
    limits: _(e.limits),
    rotation: _(e.rotation),
    distribution: _(e.distribution),
    tiers: s,
    quests: c,
    rest: I(
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
  return e.schema !== void 0 && e.schema !== V && t.push(`schema: ${String(e.schema)}`), { doc: y, i18nKeys: n, unsupported: t };
}
function Oe(e) {
  return typeof e == "string" ? e === "" : Object.values(e).every((a) => a === "");
}
function C(e) {
  const a = {};
  for (const [n, t] of Object.entries(e))
    t == null || t === "" || Array.isArray(t) && t.length === 0 || typeof t == "object" && !Array.isArray(t) && Object.keys(t).length === 0 || (a[n] = t);
  return a;
}
const Re = [
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
  const n = (u) => he(a, u.key), t = (u) => {
    const o = n(u);
    return Oe(o) ? void 0 : o;
  }, r = {};
  for (const u of e.tiers) r[u.key] = { ...u.rest, title: n(u.title) };
  const s = e.quests.map(
    (u) => C({
      key: u.key,
      uid: u.uid,
      tier: u.tier,
      title: n(u.title),
      description: t(u.description),
      duration: u.duration,
      ...u.rest,
      stages: u.stages.map(
        (o) => C({
          key: o.key,
          title: t(o.title),
          flow: o.flow,
          ...o.rest,
          objectives: o.objectives.map((h) => ({ key: h.key, type: h.type, ...h.params }))
        })
      )
    })
  ), c = {
    ...e.rest,
    id: e.id,
    schema: V,
    uid: e.uid,
    title: n(e.title),
    description: t(e.description),
    actor_type: e.actorType,
    reset: C(e.reset),
    limits: C(e.limits),
    rotation: C(e.rotation),
    distribution: C(e.distribution),
    tiers: r,
    quests: s
  }, y = {};
  for (const u of Re) u in c && (y[u] = c[u]);
  for (const u of Object.keys(c)) u in y || (y[u] = c[u]);
  const d = C(y);
  return d.quests = s, d;
}
function* Le(e) {
  const a = `${ne}.${e.id}`;
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
  const n = /* @__PURE__ */ new Set();
  for (let t = 0; t < e.quests.length; t++) {
    const r = e.quests[t], s = n.has(r.uid) ? `${r.uid}_${t}` : r.uid;
    n.add(r.uid);
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
function De(e) {
  const a = I(e, "key", "uid", "tier", "title", "description", "duration", "stages", "reward_display");
  if (a.rewards !== void 0) {
    const n = _(a.on);
    n.complete === void 0 && (a.on = { ...n, complete: a.rewards }), delete a.rewards;
  }
  return a;
}
function Y(e) {
  return !!e && typeof e == "object" && !Array.isArray(e) && "type" in e;
}
function Pe(e) {
  const a = Array.isArray(e) ? e : e == null ? [] : [e];
  return {
    chain: a.filter((n) => !Y(n)),
    legacy: a.filter(Y)
  };
}
function X(e, a) {
  const n = [...e, ...a];
  return n.length ? n : void 0;
}
function Ke(e) {
  const a = String(e.type), n = e.command ?? e.message ?? e.title ?? e.material;
  return typeof n == "string" ? `${a} · ${n}` : a;
}
const Me = [
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
  for (const n of K(e)?.params ?? [])
    n.kind === "int" && (a[n.name] = 1);
  return a;
}
function oe(e) {
  return e.amount ?? e.distance;
}
function le(e, a) {
  const n = e[a.name];
  if (n && typeof n == "object" && !Array.isArray(n)) return n;
  const t = a.legacy ? e[a.legacy] : void 0;
  return typeof t == "string" && t !== "" ? { material: t } : {};
}
function ze(e, a, n, t) {
  a.legacy && delete e[a.legacy];
  const r = !n || Object.keys(n).length === 0;
  r ? delete e[a.name] : e[a.name] = n, r || t === void 0 ? delete e.match : e.match = t;
}
function He(e) {
  const a = e.title;
  if (typeof a == "string" && a.trim() !== "") return a.replace(/<[^>]*>/g, "");
  const n = e.material ?? e.item;
  return typeof n == "string" ? n : "";
}
function ce(e, a) {
  const n = K(e);
  return n ? n.params.filter((t) => t.kind !== "int").map((t) => t.kind === "item" ? He(le(a, t)) : a[t.name]).filter((t) => typeof t == "string" && t !== "").join(" = ") : Object.keys(a).map((t) => a[t]).filter((t) => typeof t == "string" && t !== "").join(" = ");
}
function Ge(e, a) {
  return (K(e)?.params ?? []).filter((n) => n.required && (a[n.name] === void 0 || a[n.name] === "")).map((n) => n.name);
}
const Ue = [
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
], Fe = `
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
`, Qe = /^(\d+|(\d+[dhms])+)$/, Be = ["actor_load", "period_reset", "quest_complete"], Ve = (() => {
  try {
    return Intl.supportedValuesOf?.("timeZone") ?? [];
  } catch {
    return [];
  }
})();
function We({ projectId: e, documentId: a, document: n, item: t }) {
  ye("msquests", Fe);
  const r = n, s = fe(e), c = ge(), y = $e(), [d, u] = J(t ?? null);
  we(() => {
    u(t ?? null);
  }, [t]);
  const o = (v) => {
    u(v), be({ documentId: a, item: v });
  }, m = { projectId: e, project: s, lang: c, t: y, edit: (v, l) => {
    const f = structuredClone(r);
    v(f), ve({ projectId: e, kind: ae, id: a, doc: f, texts: l });
  } }, b = d ? r.quests.findIndex((v) => v.uid === d) : -1;
  return b >= 0 ? /* @__PURE__ */ p("div", { className: "aq", children: [
    /* @__PURE__ */ p(P, { variant: "ghost", size: "sm", className: "aq-back", onClick: () => o(null), children: [
      "← ",
      y("back")
    ] }),
    /* @__PURE__ */ i(et, { ctx: m, group: r, index: b })
  ] }) : /* @__PURE__ */ i("div", { className: "aq", children: /* @__PURE__ */ i(Ye, { ctx: m, group: r, onOpen: o }) });
}
function j({
  label: e,
  children: a,
  hint: n
}) {
  return (
    // biome-ignore lint/a11y/noLabelWithoutControl: the control is the child
    /* @__PURE__ */ p("label", { className: "aq-field", children: [
      /* @__PURE__ */ i("span", { className: "field-label", children: e }),
      a,
      n
    ] })
  );
}
function L({
  ctx: e,
  label: a,
  node: n,
  multiline: t
}) {
  const r = A(e.project, n.key, e.lang), s = (c) => e.edit(() => {
  }, [{ key: n.key, lang: e.lang, value: c }]);
  return /* @__PURE__ */ i(j, { label: `${a} · ${e.lang}`, children: t ? /* @__PURE__ */ i("textarea", { className: "textarea", value: r, onChange: (c) => s(c.target.value) }) : /* @__PURE__ */ i("input", { className: "input", value: r, onChange: (c) => s(c.target.value) }) });
}
function Ze(e) {
  return typeof e == "number" ? String(e) : typeof e == "string" ? e : "";
}
function O({
  label: e,
  value: a,
  onChange: n
}) {
  return /* @__PURE__ */ i(j, { label: e, children: /* @__PURE__ */ i(
    "input",
    {
      className: "input",
      type: "number",
      min: 0,
      step: 1,
      value: Ze(a),
      onChange: (t) => {
        const r = t.target.value.trim();
        n(r === "" ? void 0 : Math.max(0, Math.trunc(Number(r))));
      }
    }
  ) });
}
function N(e, a, n) {
  n === void 0 || n === "" ? delete e[a] : e[a] = n;
}
function B(e, a, n) {
  if (n < 0 || n >= e.length) return;
  const [t] = e.splice(a, 1);
  e.splice(n, 0, t);
}
function D({
  label: e,
  onClick: a,
  disabled: n,
  children: t
}) {
  return /* @__PURE__ */ i(
    "button",
    {
      type: "button",
      className: "aq-icon-btn",
      title: e,
      "aria-label": e,
      onClick: a,
      disabled: n,
      children: t
    }
  );
}
function W({
  t: e,
  index: a,
  count: n,
  onMove: t,
  onRemove: r
}) {
  return /* @__PURE__ */ p(z, { children: [
    /* @__PURE__ */ i(D, { label: e("moveUp"), disabled: a === 0, onClick: () => t(a - 1), children: "↑" }),
    /* @__PURE__ */ i(
      D,
      {
        label: e("moveDown"),
        disabled: a === n - 1,
        onClick: () => t(a + 1),
        children: "↓"
      }
    ),
    /* @__PURE__ */ i(D, { label: e("remove"), onClick: r, children: "×" })
  ] });
}
function Ye({
  ctx: e,
  group: a,
  onOpen: n
}) {
  const { t, edit: r, project: s, lang: c } = e, y = qe(), d = typeof a.reset.cron == "string" ? a.reset.cron : "", u = d ? Te(d, y) : null, o = typeof a.reset.timezone == "string" ? a.reset.timezone : "", h = Array.isArray(a.distribution.triggers) ? a.distribution.triggers : [], m = a.distribution.tiers ?? {}, b = Object.keys(a.rest).filter((l) => l !== "on"), v = () => {
    const l = R(), f = H();
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
          description: { key: R() },
          duration: "",
          stages: [ue(1)],
          rest: {}
        });
      },
      [{ key: l, lang: c, value: t("newQuestTitle") }]
    ), n(f);
  };
  return /* @__PURE__ */ p(z, { children: [
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ i("span", { className: "aq-h", children: t("group") }),
      /* @__PURE__ */ i(L, { ctx: e, label: t("title"), node: a.title }),
      /* @__PURE__ */ i(L, { ctx: e, label: t("description"), node: a.description, multiline: !0 }),
      /* @__PURE__ */ i(j, { label: t("actorType"), children: /* @__PURE__ */ p(
        "select",
        {
          className: "select",
          value: a.actorType,
          onChange: (l) => r((f) => {
            f.actorType = l.target.value;
          }),
          children: [
            /* @__PURE__ */ i("option", { value: "player", children: t("actor.player") }),
            /* @__PURE__ */ i("option", { value: "global", children: t("actor.global") }),
            !["player", "global"].includes(a.actorType) && /* @__PURE__ */ i("option", { value: a.actorType, children: a.actorType })
          ]
        }
      ) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ i("span", { className: "aq-h", children: t("reset") }),
      /* @__PURE__ */ i(
        j,
        {
          label: t("cron"),
          hint: d ? Ee(d) ? /* @__PURE__ */ p("span", { className: "aq-ok", children: [
            u ?? t("cronRaw"),
            o ? `, ${o}` : ""
          ] }) : /* @__PURE__ */ i("span", { className: "aq-err", children: t("cronInvalid") }) : /* @__PURE__ */ i("span", { className: "aq-hint", children: t("noReset") }),
          children: /* @__PURE__ */ i(
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
        /* @__PURE__ */ i(
          "input",
          {
            className: "input",
            list: "aq-timezones",
            value: o,
            placeholder: t("timezoneServer"),
            onChange: (l) => r((f) => N(f.reset, "timezone", l.target.value.trim()))
          }
        ),
        /* @__PURE__ */ i("datalist", { id: "aq-timezones", children: Ve.map((l) => /* @__PURE__ */ i("option", { value: l }, l)) })
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ i("span", { className: "aq-h", children: t("distribution") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ i(j, { label: t("strategy"), children: /* @__PURE__ */ p(
          "select",
          {
            className: "select",
            value: typeof a.distribution.strategy == "string" ? a.distribution.strategy.toLowerCase() : "",
            onChange: (l) => r((f) => N(f.distribution, "strategy", l.target.value)),
            children: [
              /* @__PURE__ */ i("option", { value: "", children: "—" }),
              ["none", "sequential", "random"].map((l) => /* @__PURE__ */ i("option", { value: l, children: t(`strategy.${l}`) }, l))
            ]
          }
        ) }),
        /* @__PURE__ */ i(
          O,
          {
            label: t("amount"),
            value: a.distribution.amount,
            onChange: (l) => r((f) => N(f.distribution, "amount", l))
          }
        )
      ] }),
      /* @__PURE__ */ i("span", { className: "field-label", children: t("triggers") }),
      Be.map((l) => /* @__PURE__ */ p("label", { className: "aq-check", children: [
        /* @__PURE__ */ i(
          "input",
          {
            type: "checkbox",
            checked: h.includes(l),
            onChange: (f) => r((q) => {
              const k = Array.isArray(q.distribution.triggers) ? q.distribution.triggers : [], E = f.target.checked ? [...k, l] : k.filter((g) => g !== l);
              N(q.distribution, "triggers", E.length ? E : void 0);
            })
          }
        ),
        t(`trigger.${l}`)
      ] }, l)),
      a.tiers.length > 0 && /* @__PURE__ */ p(z, { children: [
        /* @__PURE__ */ i("span", { className: "field-label", children: t("perTier") }),
        /* @__PURE__ */ i("div", { className: "aq-row", children: a.tiers.map((l) => /* @__PURE__ */ i(
          O,
          {
            label: A(s, l.title.key, c) || l.key,
            value: m[l.key],
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
      /* @__PURE__ */ i("span", { className: "aq-h", children: t("limits") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ i(
          O,
          {
            label: t("maxActive"),
            value: a.limits.max_active,
            onChange: (l) => r((f) => N(f.limits, "max_active", l))
          }
        ),
        /* @__PURE__ */ i(
          O,
          {
            label: t("maxPerPeriod"),
            value: a.limits.max_per_period,
            onChange: (l) => r((f) => N(f.limits, "max_per_period", l))
          }
        )
      ] }),
      /* @__PURE__ */ i("span", { className: "aq-h", children: t("rotation") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ p("label", { className: "aq-check", children: [
          /* @__PURE__ */ i(
            "input",
            {
              type: "checkbox",
              checked: a.rotation.enabled === !0,
              onChange: (l) => r((f) => N(f.rotation, "enabled", l.target.checked ? !0 : void 0))
            }
          ),
          t("rotationEnabled")
        ] }),
        /* @__PURE__ */ i(
          O,
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
        /* @__PURE__ */ i("span", { className: "aq-h", children: t("tiers") }),
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
      a.tiers.map((l, f) => /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ i(j, { label: t("tierKey"), children: /* @__PURE__ */ i(
          "input",
          {
            className: "input",
            defaultValue: l.key,
            onBlur: (q) => {
              const k = G(q.target.value);
              k !== l.key && r((E) => {
                const g = M(
                  k,
                  E.tiers.map((T) => T.key)
                );
                E.tiers[f].key = g;
                for (const T of E.quests) T.tier === l.key && (T.tier = g);
                const x = E.distribution.tiers;
                x && l.key in x && (x[g] = x[l.key], delete x[l.key]);
              });
            }
          }
        ) }),
        /* @__PURE__ */ i(L, { ctx: e, label: t("title"), node: l.title }),
        /* @__PURE__ */ i(
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
      a.quests.length === 0 && /* @__PURE__ */ i("span", { className: "aq-hint", children: t("noQuests") }),
      /* @__PURE__ */ i("div", { className: "aq-list", children: a.quests.map((l, f) => /* @__PURE__ */ p("div", { className: "aq-item", children: [
        /* @__PURE__ */ p(
          "button",
          {
            type: "button",
            className: "aq-item-main",
            onClick: () => n(l.uid),
            title: t("open"),
            children: [
              /* @__PURE__ */ i("span", { className: "aq-item-title", children: A(s, l.title.key, c) || l.key }),
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
        /* @__PURE__ */ i(
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
      b.length > 0 && /* @__PURE__ */ i("span", { className: "aq-hint", children: t("kept", { fields: b.join(", ") }) })
    ] }),
    /* @__PURE__ */ i(Je, { ctx: e, group: a })
  ] });
}
function de({
  ctx: e,
  value: a,
  onChange: n
}) {
  const { chain: t, legacy: r } = Pe(a);
  return /* @__PURE__ */ p("div", { className: "aq-chain", children: [
    /* @__PURE__ */ i(
      ke,
      {
        projectId: e.projectId,
        value: t,
        onChange: (s) => n(X(s, r))
      }
    ),
    r.length > 0 && /* @__PURE__ */ p("div", { className: "aq-legacy", children: [
      /* @__PURE__ */ i("span", { className: "aq-hint", children: e.t("legacySteps") }),
      r.map((s, c) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: a step has no identity
        /* @__PURE__ */ p("div", { className: "aq-legacy-row", children: [
          /* @__PURE__ */ i("code", { children: Ke(s) }),
          /* @__PURE__ */ i(
            D,
            {
              label: e.t("remove"),
              onClick: () => n(X(t, r.filter((y, d) => d !== c))),
              children: "×"
            }
          )
        ] }, c)
      ))
    ] })
  ] });
}
function Xe(e) {
  return Array.isArray(e) ? e.length : e == null ? 0 : 1;
}
function Je({ ctx: e, group: a }) {
  const { t: n, edit: t } = e, r = a.rest.on && typeof a.rest.on == "object" ? a.rest.on : {};
  return /* @__PURE__ */ p("section", { className: "aq-section", children: [
    /* @__PURE__ */ i("span", { className: "aq-h", children: n("hooks") }),
    /* @__PURE__ */ i("span", { className: "aq-hint", children: n("hooksHint") }),
    Me.map((s) => {
      const c = Xe(r[s]);
      return /* @__PURE__ */ p("details", { className: "aq-fold", children: [
        /* @__PURE__ */ p("summary", { children: [
          /* @__PURE__ */ i("span", { children: n(`hook.${s}`) }),
          c > 0 && /* @__PURE__ */ i("span", { className: "aq-badge", title: n("actionCount", { count: c }), children: c })
        ] }),
        /* @__PURE__ */ i(
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
    title: { key: R() },
    flow: "parallel",
    objectives: [],
    rest: {}
  };
}
function et({ ctx: e, group: a, index: n }) {
  const { t, edit: r } = e, s = a.quests[n], c = (o, h) => r((m) => o(m.quests[n]), h), y = a.quests.some((o, h) => h !== n && o.key === s.key), d = s.duration !== "" && !Qe.test(s.duration.replace(/\s/g, "").toLowerCase()), u = Object.keys(s.rest).filter((o) => o !== "on");
  return /* @__PURE__ */ p(z, { children: [
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ i("span", { className: "aq-h", children: t("general") }),
      /* @__PURE__ */ i(L, { ctx: e, label: t("title"), node: s.title }),
      /* @__PURE__ */ i(L, { ctx: e, label: t("description"), node: s.description, multiline: !0 }),
      /* @__PURE__ */ p("div", { className: "aq-row aq-top", children: [
        /* @__PURE__ */ i(j, { label: t("tier"), children: /* @__PURE__ */ p(
          "select",
          {
            className: "select",
            value: s.tier,
            onChange: (o) => c((h) => {
              h.tier = o.target.value;
            }),
            children: [
              /* @__PURE__ */ i("option", { value: "", children: t("noTier") }),
              a.tiers.map((o) => /* @__PURE__ */ i("option", { value: o.key, children: A(e.project, o.title.key, e.lang) || o.key }, o.key)),
              s.tier && !a.tiers.some((o) => o.key === s.tier) && /* @__PURE__ */ i("option", { value: s.tier, children: s.tier })
            ]
          }
        ) }),
        /* @__PURE__ */ i(
          j,
          {
            label: t("duration"),
            hint: /* @__PURE__ */ i("span", { className: d ? "aq-err" : "aq-hint", children: t(d ? "durationInvalid" : "durationHint") }),
            children: /* @__PURE__ */ i(
              "input",
              {
                className: "input",
                value: s.duration,
                placeholder: "24h",
                onChange: (o) => c((h) => {
                  h.duration = o.target.value.trim();
                })
              }
            )
          }
        )
      ] }),
      /* @__PURE__ */ p("details", { className: "aq-fold", open: y || void 0, children: [
        /* @__PURE__ */ i("summary", { children: t("more") }),
        /* @__PURE__ */ i(
          j,
          {
            label: t("key"),
            hint: /* @__PURE__ */ i("span", { className: y ? "aq-err" : "aq-hint", children: t(y ? "keyTaken" : "questKeyHint") }),
            children: /* @__PURE__ */ i(
              "input",
              {
                className: "input aq-mono",
                value: s.key,
                onChange: (o) => c((h) => {
                  h.key = ie(o.target.value);
                }),
                onBlur: () => {
                  const o = G(s.key);
                  o !== s.key && c((h) => {
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
              const h = M(
                `stage_${o.stages.length + 1}`,
                o.stages.map((m) => m.key)
              );
              o.stages.push({ ...ue(o.stages.length + 1), key: h });
            }),
            children: [
              "+ ",
              t("addStage")
            ]
          }
        )
      ] }),
      /* @__PURE__ */ i("ol", { className: "aq-stages", children: s.stages.map((o, h) => /* @__PURE__ */ i(
        tt,
        {
          ctx: e,
          quest: s,
          stage: o,
          si: h,
          editQuest: c
        },
        `${o.key}-${h}`
      )) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ i("span", { className: "aq-h", children: t("rewards") }),
      /* @__PURE__ */ i("span", { className: "aq-hint", children: t("rewardsHint") }),
      /* @__PURE__ */ i(
        de,
        {
          ctx: e,
          value: _(s.rest.on).complete,
          onChange: (o) => c((h) => {
            const m = { ..._(h.rest.on) };
            N(m, "complete", o), N(h.rest, "on", Object.keys(m).length > 0 ? m : void 0);
          })
        }
      ),
      u.length > 0 && /* @__PURE__ */ i("span", { className: "aq-hint", children: t("kept", { fields: u.join(", ") }) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ i("span", { className: "aq-h", children: t("preview") }),
      /* @__PURE__ */ i(rt, { ctx: e, quest: s })
    ] })
  ] });
}
function tt({
  ctx: e,
  quest: a,
  stage: n,
  si: t,
  editQuest: r
}) {
  const { t: s } = e, [c, y] = J(null), d = (m) => r((b) => m(b.stages[t])), u = A(e.project, n.title.key, e.lang), o = n.flow || "parallel", h = a.stages.flatMap((m) => m.objectives.map((b) => b.key));
  return /* @__PURE__ */ p("li", { className: "aq-stage", children: [
    /* @__PURE__ */ p("div", { className: "aq-stage-head", children: [
      /* @__PURE__ */ i("span", { className: "aq-stage-n", children: t + 1 }),
      /* @__PURE__ */ i(
        "input",
        {
          className: "input aq-stage-title",
          value: u,
          placeholder: s("stageTitle"),
          "aria-label": s("stage", { n: t + 1 }),
          onChange: (m) => e.edit(() => {
          }, [{ key: n.title.key, lang: e.lang, value: m.target.value }])
        }
      ),
      /* @__PURE__ */ i(
        W,
        {
          t: s,
          index: t,
          count: a.stages.length,
          onMove: (m) => r((b) => B(b.stages, t, m)),
          onRemove: () => r((m) => {
            m.stages.splice(t, 1);
          })
        }
      )
    ] }),
    n.objectives.length > 1 && /* @__PURE__ */ i("div", { className: "aq-seg", role: "radiogroup", "aria-label": s("flow"), children: ["parallel", "sequential"].map((m) => /* @__PURE__ */ i(
      "button",
      {
        type: "button",
        role: "radio",
        "aria-checked": o === m,
        className: `chip${o === m ? " is-active" : ""}`,
        onClick: () => d((b) => {
          b.flow = m;
        }),
        children: s(`flow.${m}`)
      },
      m
    )) }),
    n.objectives.length === 0 && /* @__PURE__ */ i("span", { className: "aq-hint", children: s("noObjectives") }),
    /* @__PURE__ */ i("ul", { className: "aq-objectives", children: n.objectives.map((m, b) => /* @__PURE__ */ i(
      at,
      {
        t: s,
        projectId: e.projectId,
        n: o === "sequential" ? `${t + 1}.${b + 1}` : "•",
        objective: m,
        open: c === m.key,
        onToggle: () => y(c === m.key ? null : m.key),
        keyTaken: h.filter((v) => v === m.key).length > 1,
        index: b,
        count: n.objectives.length,
        onMove: (v) => d((l) => B(l.objectives, b, v)),
        onChange: (v) => d((l) => v(l.objectives[b])),
        onRename: (v) => {
          c === m.key && y(v);
        },
        onRemove: () => d((v) => {
          v.objectives.splice(b, 1);
        })
      },
      `${m.key}-${b}`
    )) }),
    /* @__PURE__ */ p(
      P,
      {
        size: "sm",
        variant: "ghost",
        className: "aq-add",
        onClick: () => {
          const m = Ce("block_break", h);
          r((b) => {
            b.stages[t].objectives.push({
              key: m,
              type: "block_break",
              params: se("block_break")
            });
          }), y(m);
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
function at({
  t: e,
  projectId: a,
  n,
  objective: t,
  open: r,
  onToggle: s,
  keyTaken: c,
  index: y,
  count: d,
  onMove: u,
  onChange: o,
  onRename: h,
  onRemove: m
}) {
  const b = K(t.type), v = b ? b.params : Object.keys(t.params).map((g) => ({ name: g, kind: "text" })), l = oe(t.params), f = ce(t.type, t.params), q = Ge(t.type, t.params), k = c || q.length > 0, E = (g) => g.kind === "item" ? /* @__PURE__ */ i(
    nt,
    {
      t: e,
      projectId: a,
      spec: g,
      params: t.params,
      onChange: (x) => o((T) => x(T.params))
    },
    g.name
  ) : /* @__PURE__ */ i(
    it,
    {
      t: e,
      spec: g,
      value: t.params[g.name],
      onChange: (x) => o((T) => N(T.params, g.name, x))
    },
    g.name
  );
  return /* @__PURE__ */ p("li", { className: `aq-obj${r ? " is-open" : ""}${k ? " has-problem" : ""}`, children: [
    /* @__PURE__ */ p("div", { className: "aq-obj-line", children: [
      /* @__PURE__ */ p("button", { type: "button", className: "aq-obj-main", onClick: s, "aria-expanded": r, children: [
        /* @__PURE__ */ i("span", { className: "aq-obj-n", children: n }),
        /* @__PURE__ */ i("span", { className: "aq-obj-icon", "aria-hidden": !0, children: b?.icon ?? "?" }),
        /* @__PURE__ */ p("span", { className: "aq-obj-text", children: [
          pe(e, t.type),
          l !== void 0 && /* @__PURE__ */ p("strong", { children: [
            " · ",
            String(l)
          ] }),
          f ? ` × ${f}` : l !== void 0 ? ` × ${e("any")}` : ""
        ] }),
        k && /* @__PURE__ */ i(
          "span",
          {
            className: "aq-obj-dot",
            title: c ? e("objectiveKeyTaken") : e("missing", {
              fields: q.map((g) => e(`param.${g}`)).join(", ")
            })
          }
        )
      ] }),
      /* @__PURE__ */ i(W, { t: e, index: y, count: d, onMove: u, onRemove: m })
    ] }),
    r && /* @__PURE__ */ p("div", { className: "aq-obj-form", children: [
      /* @__PURE__ */ i(j, { label: e("type"), children: /* @__PURE__ */ p(
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
            !b && /* @__PURE__ */ i("option", { value: t.type, children: t.type })
          ]
        }
      ) }),
      /* @__PURE__ */ i("div", { className: "aq-row aq-wrap", children: v.filter((g) => !g.advanced).map(E) }),
      q.length > 0 && /* @__PURE__ */ i("span", { className: "aq-warn", children: e("missing", {
        fields: q.map((g) => e(`param.${g}`)).join(", ")
      }) }),
      /* @__PURE__ */ p("details", { className: "aq-fold", open: c || void 0, children: [
        /* @__PURE__ */ i("summary", { children: e("more") }),
        v.filter((g) => g.advanced).map(E),
        /* @__PURE__ */ i(
          j,
          {
            label: e("key"),
            hint: /* @__PURE__ */ i("span", { className: c ? "aq-err" : "aq-hint", children: e(c ? "objectiveKeyTaken" : "objectiveKeyHint") }),
            children: /* @__PURE__ */ i(
              "input",
              {
                className: "input aq-mono",
                value: t.key,
                onChange: (g) => {
                  const x = ie(g.target.value);
                  o((T) => {
                    T.key = x;
                  }), h(x);
                },
                onBlur: () => {
                  const g = G(t.key);
                  g !== t.key && (o((x) => {
                    x.key = g;
                  }), h(g));
                }
              }
            )
          }
        )
      ] })
    ] })
  ] });
}
function nt({
  t: e,
  projectId: a,
  spec: n,
  params: t,
  onChange: r
}) {
  const s = le(t, n), c = Array.isArray(t.match) ? t.match : void 0, y = (d, u) => r((o) => ze(o, n, d, u));
  return /* @__PURE__ */ i(
    j,
    {
      label: e(`param.${n.name}`),
      hint: Object.keys(s).length === 0 ? /* @__PURE__ */ i("span", { className: "aq-hint", children: e("anyItem") }) : void 0,
      children: /* @__PURE__ */ i(
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
  value: n,
  onChange: t
}) {
  const r = e(`param.${a.name}`), s = a.required && (n === void 0 || n === ""), c = typeof n == "string" || typeof n == "number" ? String(n) : "";
  if (a.kind === "int")
    return /* @__PURE__ */ i(O, { label: r, value: n, onChange: t });
  const y = s ? /* @__PURE__ */ i("span", { className: "aq-warn", children: e("required") }) : a.name === "itemKey" ? /* @__PURE__ */ i("span", { className: "aq-hint", children: e("itemKeyHint") }) : void 0;
  if (a.kind === "material")
    return /* @__PURE__ */ i(j, { label: r, hint: y, children: /* @__PURE__ */ i(
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
    /* @__PURE__ */ i(
      "input",
      {
        className: `input${d ? " aq-mono" : ""}`,
        list: d ? "aq-entities" : void 0,
        value: c,
        placeholder: d ? e("any") : "",
        onChange: (u) => t(d ? u.target.value.toUpperCase().replace(/\s+/g, "_") : u.target.value)
      }
    ),
    d && /* @__PURE__ */ i("datalist", { id: "aq-entities", children: Ue.map((u) => /* @__PURE__ */ i("option", { value: u }, u)) })
  ] });
}
function rt({ ctx: e, quest: a }) {
  const { t: n, project: t, lang: r } = e, s = (c) => {
    const y = ce(c.type, c.params), d = oe(c.params);
    return `${pe(n, c.type)}${y ? ` · ${y}` : ""}${d !== void 0 ? ` · 0/${String(d)}` : ""}`;
  };
  return /* @__PURE__ */ p("div", { className: "aq-preview", children: [
    /* @__PURE__ */ i("span", { className: "aq-preview-title", children: A(t, a.title.key, r) || a.key }),
    A(t, a.description.key, r) && /* @__PURE__ */ i("span", { className: "aq-preview-line", children: A(t, a.description.key, r) }),
    a.stages.map((c, y) => (
      // biome-ignore lint/suspicious/noArrayIndexKey: stages are listed in order
      /* @__PURE__ */ p("span", { className: "aq-preview-stage", children: [
        a.stages.length > 1 && /* @__PURE__ */ i("span", { className: "aq-preview-line", children: A(t, c.title.key, r) || n("stage", { n: y + 1 }) }),
        c.objectives.map((d) => /* @__PURE__ */ p("span", { className: "aq-preview-line", children: [
          "• ",
          s(d)
        ] }, d.key))
      ] }, y)
    ))
  ] });
}
const st = {
  kind: ae,
  dir: ne,
  schemaVersion: V,
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: Z.en,
  labels: Z,
  component: We,
  title: (e, { project: a, lang: n }) => A(a, e.title.key, n),
  create: ({ id: e, projectId: a }) => ({
    id: e,
    projectId: a,
    uid: H(),
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
    items: (e, { project: a, lang: n }) => e.quests.map((t) => ({
      id: t.uid,
      label: A(a, t.title.key, n) || t.key,
      detail: t.tier || void 0
    })),
    summary: (e) => `quest_group · ${e.quests.length}`
  }
}, ot = {
  id: "msquests",
  displayName: "Quests",
  schemaVersion: "quest_group/v1",
  sdk: je,
  sidebar: { icon: () => null, label: "Quests", to: "/m/msquests" },
  // Quests are Documents: they are edited from the project, no screen of their own.
  routes: [],
  documents: [st]
}, ut = { ...ot, sdk: "1.3.0" };
export {
  ut as default
};
