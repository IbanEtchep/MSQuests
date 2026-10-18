import { createModuleI18n as ye, fieldText as fe, newTextKey as R, injectModuleStyles as ge, useProject as be, useContentLang as ve, Button as P, selectDocumentItem as qe, textValue as A, updateDocument as ke, useUiLang as xe, MiniMessageInput as J, ActionChainEditor as Ne, ItemInput as je, MaterialInput as we, SDK_VERSION as _e } from "@artisan/editor-sdk";
import { jsxs as p, jsx as n, Fragment as z } from "react/jsx-runtime";
import { useState as ee, useEffect as Ae } from "react";
const Ee = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"]
}, Te = /^\d+$/;
function U(e, i, a) {
  const t = String(a).padStart(2, "0");
  return e === "fr" ? a === 0 ? `${i} h` : `${i} h ${t}` : `${String(i).padStart(2, "0")}:${t}`;
}
function $(e, i, a) {
  return Te.test(e) && Number(e) >= i && Number(e) <= a;
}
function $e(e) {
  return e.trim().split(/\s+/).length === 5;
}
function Ce(e, i) {
  const a = i === "fr", t = e.trim().split(/\s+/);
  if (t.length !== 5) return null;
  const [r, s, u, y, c] = t;
  if (y !== "*") return null;
  const d = /^\*\/(\d+)$/;
  if (u === "*" && c === "*") {
    if ($(r, 0, 59) && $(s, 0, 23))
      return a ? `tous les jours à ${U(i, +s, +r)}` : `every day at ${U(i, +s, +r)}`;
    const h = d.exec(s);
    if ($(r, 0, 59) && h)
      return a ? `toutes les ${h[1]} h, à la minute ${r}` : `every ${h[1]} hours, at minute ${r}`;
    if ($(r, 0, 59) && s === "*")
      return a ? `toutes les heures, à la minute ${r}` : `every hour, at minute ${r}`;
    const m = d.exec(r);
    return m && s === "*" ? a ? `toutes les ${m[1]} minutes` : `every ${m[1]} minutes` : null;
  }
  if (!$(r, 0, 59) || !$(s, 0, 23)) return null;
  const o = U(i, +s, +r);
  if (u === "*" && $(c, 0, 7)) {
    const h = Ee[a ? "fr" : "en"][Number(c) % 7];
    return a ? `chaque ${h} à ${o}` : `every ${h} at ${o}`;
  }
  return c === "*" && $(u, 1, 31) ? a ? `le ${u === "1" ? "1er" : u} de chaque mois à ${o}` : `on day ${u} of each month at ${o}` : null;
}
const te = {
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
}, ie = {
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
}, Z = { en: te.kindLabel, fr: ie.kindLabel }, Se = ye({ en: te, fr: ie }), ae = "quest_group", ne = "quest_groups", V = "quest_group/v1", Q = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
function H(e = Date.now()) {
  let i = "", a = e;
  for (let s = 0; s < 10; s++)
    i = Q[a % 32] + i, a = Math.floor(a / 32);
  const t = new Uint8Array(16);
  crypto.getRandomValues(t);
  let r = "";
  for (let s = 0; s < 16; s++) r += Q[t[s] % 32];
  return i + r;
}
function K(e, i) {
  const a = new Set(i);
  if (!a.has(e)) return e;
  for (let t = 2; ; t++) if (!a.has(`${e}_${t}`)) return `${e}_${t}`;
}
function Oe(e, i) {
  const a = new Set(i), t = new Uint8Array(4);
  for (; ; ) {
    crypto.getRandomValues(t);
    const r = Array.from(t, (u) => Q[u % 32].toLowerCase()).join(""), s = `${G(e)}_${r}`;
    if (!a.has(s)) return s;
  }
}
function re(e) {
  return e.toLowerCase().replace(/[^a-z0-9_]/g, "_");
}
function G(e) {
  return e.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().replace(/[^a-z0-9_]+/g, "_").replace(/^_+|_+$/g, "") || "quest";
}
function j(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? { ...e } : {};
}
function F(e) {
  return Array.isArray(e) ? e : [];
}
function _(e) {
  return typeof e == "string" || typeof e == "number" || typeof e == "boolean" ? String(e) : "";
}
function I(e, ...i) {
  const a = { ...e };
  for (const t of i) delete a[t];
  return a;
}
function Re(e, i) {
  const a = {}, t = [];
  function r(c) {
    if (c && typeof c == "object" && !Array.isArray(c)) {
      const h = R(), m = {};
      for (const [b, v] of Object.entries(c)) m[b] = _(v);
      return a[h] = m, { key: h };
    }
    const d = typeof c == "string" ? /^\{i18n:(.+)\}$/.exec(c) : null;
    if (d) {
      const h = i.translations?.[d[1]];
      return h && (a[d[1]] = { ...h }), { key: d[1] };
    }
    const o = R();
    return a[o] = c == null || c === "" ? {} : { [i.lang]: _(c) }, { key: o };
  }
  const s = Object.entries(j(e.tiers)).map(([c, d]) => {
    const o = j(d);
    return { key: c, title: r(o.title), rest: I(o, "title") };
  }), u = F(e.quests).map((c, d) => {
    const o = j(c), h = F(o.stages).map((m, b) => {
      const v = j(m), l = _(v.flow).toLowerCase();
      return {
        key: _(v.key) || `stage_${b + 1}`,
        title: r(v.title),
        flow: l === "parallel" || l === "sequential" ? l : "",
        objectives: F(v.objectives).map((f, q) => {
          const k = j(f);
          return {
            key: _(k.key) || `objective_${q + 1}`,
            type: _(k.type),
            params: I(k, "key", "type")
          };
        }),
        rest: I(v, "key", "title", "flow", "objectives")
      };
    });
    return {
      key: _(o.key) || `quest_${d + 1}`,
      uid: _(o.uid) || H(),
      tier: _(o.tier),
      title: r(o.title),
      description: r(o.description),
      duration: _(o.duration),
      stages: h,
      rest: Ie(o)
    };
  }), y = {
    id: _(e.id),
    projectId: i.projectId,
    uid: _(e.uid) || H(),
    title: r(e.title),
    description: r(e.description),
    actorType: _(e.actor_type) || "player",
    reset: j(e.reset),
    limits: j(e.limits),
    rotation: j(e.rotation),
    distribution: j(e.distribution),
    tiers: s,
    quests: u,
    rest: se(I(
      j(e),
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
    ))
  };
  return e.schema !== void 0 && e.schema !== V && t.push(`schema: ${String(e.schema)}`), { doc: y, i18nKeys: a, unsupported: t };
}
function se(e) {
  return e.on !== void 0 && (e.actions === void 0 && (e.actions = e.on), delete e.on), e;
}
function Ie(e) {
  const i = se(
    I(e, "key", "uid", "tier", "title", "description", "duration", "stages", "reward_display")
  );
  if (i.rewards !== void 0) {
    const a = j(i.actions);
    a.complete === void 0 && (i.actions = { ...a, complete: i.rewards }), delete i.rewards;
  }
  return i;
}
function Le(e) {
  return typeof e == "string" ? e === "" : Object.values(e).every((i) => i === "");
}
function C(e) {
  const i = {};
  for (const [a, t] of Object.entries(e))
    t == null || t === "" || Array.isArray(t) && t.length === 0 || typeof t == "object" && !Array.isArray(t) && Object.keys(t).length === 0 || (i[a] = t);
  return i;
}
const De = [
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
  "actions",
  "quests"
];
function Pe(e, i) {
  const a = (d) => fe(i, d.key), t = (d) => {
    const o = a(d);
    return Le(o) ? void 0 : o;
  }, r = {};
  for (const d of e.tiers) r[d.key] = { ...d.rest, title: a(d.title) };
  const s = e.quests.map(
    (d) => C({
      key: d.key,
      uid: d.uid,
      tier: d.tier,
      title: a(d.title),
      description: t(d.description),
      duration: d.duration,
      ...d.rest,
      stages: d.stages.map(
        (o) => C({
          key: o.key,
          title: t(o.title),
          flow: o.flow,
          ...o.rest,
          objectives: o.objectives.map((h) => ({ key: h.key, type: h.type, ...h.params }))
        })
      )
    })
  ), u = {
    ...e.rest,
    id: e.id,
    schema: V,
    uid: e.uid,
    title: a(e.title),
    description: t(e.description),
    actor_type: e.actorType,
    reset: C(e.reset),
    limits: C(e.limits),
    rotation: C(e.rotation),
    distribution: C(e.distribution),
    tiers: r,
    quests: s
  }, y = {};
  for (const d of De) d in u && (y[d] = u[d]);
  for (const d of Object.keys(u)) d in y || (y[d] = u[d]);
  const c = C(y);
  return c.quests = s, c;
}
function* Me(e) {
  const i = `${ne}.${e.id}`;
  yield { lText: e.title, key: `${i}.title`, path: "title", kind: "quest_group.title" }, yield {
    lText: e.description,
    key: `${i}.description`,
    path: "description",
    kind: "quest_group.description"
  };
  for (const t of e.tiers)
    yield {
      lText: t.title,
      key: `${i}.tiers.${t.key}.title`,
      path: `tiers.${t.key}.title`,
      kind: "quest_group.tier"
    };
  const a = /* @__PURE__ */ new Set();
  for (let t = 0; t < e.quests.length; t++) {
    const r = e.quests[t], s = a.has(r.uid) ? `${r.uid}_${t}` : r.uid;
    a.add(r.uid);
    const u = `${i}.quests.${s}`;
    yield {
      lText: r.title,
      key: `${u}.title`,
      path: `quests[${t}].title`,
      kind: "quest.title"
    }, yield {
      lText: r.description,
      key: `${u}.description`,
      path: `quests[${t}].description`,
      kind: "quest.description"
    };
    const y = /* @__PURE__ */ new Set();
    for (let c = 0; c < r.stages.length; c++) {
      const d = r.stages[c], o = y.has(d.key) ? `${d.key}_${c}` : d.key;
      y.add(d.key), yield {
        lText: d.title,
        key: `${u}.stages.${o}.title`,
        path: `quests[${t}].stages[${c}].title`,
        kind: "quest.stage"
      };
    }
  }
}
function Y(e) {
  return !!e && typeof e == "object" && !Array.isArray(e) && "type" in e;
}
function Ke(e) {
  const i = Array.isArray(e) ? e : e == null ? [] : [e];
  return {
    chain: i.filter((a) => !Y(a)),
    legacy: i.filter(Y)
  };
}
function X(e, i) {
  const a = [...e, ...i];
  return a.length ? a : void 0;
}
function ze(e) {
  const i = String(e.type), a = e.command ?? e.message ?? e.title ?? e.material;
  return typeof a == "string" ? `${i} · ${a}` : i;
}
const He = [
  "distribution",
  "actor_load",
  "quest_start",
  "objective_progress",
  "objective_complete",
  "quest_complete",
  "period_complete"
], S = { name: "amount", kind: "int", required: !0 }, oe = [
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
function M(e) {
  return oe.find((i) => i.type === e);
}
function le(e) {
  const i = {};
  for (const a of M(e)?.params ?? [])
    a.kind === "int" && (i[a.name] = 1);
  return i;
}
function ce(e) {
  return e.amount ?? e.distance;
}
function de(e, i) {
  const a = e[i.name];
  if (a && typeof a == "object" && !Array.isArray(a)) return a;
  const t = i.legacy ? e[i.legacy] : void 0;
  return typeof t == "string" && t !== "" ? { material: t } : {};
}
function Ge(e, i, a, t) {
  i.legacy && delete e[i.legacy];
  const r = !a || Object.keys(a).length === 0;
  r ? delete e[i.name] : e[i.name] = a, r || t === void 0 ? delete e.match : e.match = t;
}
function Ue(e) {
  const i = e.title;
  if (typeof i == "string" && i.trim() !== "") return i.replace(/<[^>]*>/g, "");
  const a = e.material ?? e.item;
  return typeof a == "string" ? a : "";
}
function ue(e, i) {
  const a = M(e);
  return a ? a.params.filter((t) => t.kind !== "int").map((t) => t.kind === "item" ? Ue(de(i, t)) : i[t.name]).filter((t) => typeof t == "string" && t !== "").join(" = ") : Object.keys(i).map((t) => i[t]).filter((t) => typeof t == "string" && t !== "").join(" = ");
}
function Fe(e, i) {
  return (M(e)?.params ?? []).filter((a) => a.required && (i[a.name] === void 0 || i[a.name] === "")).map((a) => a.name);
}
const Qe = [
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
], Be = `
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
.aq .aq-stage-title { flex: 1; min-width: 0; font-weight: 600; }
.aq .aq-stage-title .input { height: 28px; background: transparent; border-color: transparent; }
.aq .aq-stage-title .input:hover, .aq .aq-stage-title .input:focus { border-color: var(--border); background: var(--surface-inset); }
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
`, Ve = /^(\d+|(\d+[dhms])+)$/, We = ["actor_load", "period_reset", "quest_complete"], Ze = (() => {
  try {
    return Intl.supportedValuesOf?.("timeZone") ?? [];
  } catch {
    return [];
  }
})();
function Ye({ projectId: e, documentId: i, document: a, item: t }) {
  ge("msquests", Be);
  const r = a, s = be(e), u = ve(), y = Se(), [c, d] = ee(t ?? null);
  Ae(() => {
    d(t ?? null);
  }, [t]);
  const o = (v) => {
    d(v), qe({ documentId: i, item: v });
  }, m = { projectId: e, project: s, lang: u, t: y, edit: (v, l) => {
    const f = structuredClone(r);
    v(f), ke({ projectId: e, kind: ae, id: i, doc: f, texts: l });
  } }, b = c ? r.quests.findIndex((v) => v.uid === c) : -1;
  return b >= 0 ? /* @__PURE__ */ p("div", { className: "aq", children: [
    /* @__PURE__ */ p(P, { variant: "ghost", size: "sm", className: "aq-back", onClick: () => o(null), children: [
      "← ",
      y("back")
    ] }),
    /* @__PURE__ */ n(it, { ctx: m, group: r, index: b })
  ] }) : /* @__PURE__ */ n("div", { className: "aq", children: /* @__PURE__ */ n(Je, { ctx: m, group: r, onOpen: o }) });
}
function w({
  label: e,
  children: i,
  hint: a
}) {
  return (
    // biome-ignore lint/a11y/noLabelWithoutControl: the control is the child
    /* @__PURE__ */ p("label", { className: "aq-field", children: [
      /* @__PURE__ */ n("span", { className: "field-label", children: e }),
      i,
      a
    ] })
  );
}
function L({
  ctx: e,
  label: i,
  node: a,
  multiline: t
}) {
  const r = A(e.project, a.key, e.lang), s = (u) => e.edit(() => {
  }, [{ key: a.key, lang: e.lang, value: u }]);
  return /* @__PURE__ */ n(w, { label: `${i} · ${e.lang}`, children: /* @__PURE__ */ n(J, { value: r, onChange: s, multiline: t, "aria-label": i }) });
}
function Xe(e) {
  return typeof e == "number" ? String(e) : typeof e == "string" ? e : "";
}
function O({
  label: e,
  value: i,
  onChange: a
}) {
  return /* @__PURE__ */ n(w, { label: e, children: /* @__PURE__ */ n(
    "input",
    {
      className: "input",
      type: "number",
      min: 0,
      step: 1,
      value: Xe(i),
      onChange: (t) => {
        const r = t.target.value.trim();
        a(r === "" ? void 0 : Math.max(0, Math.trunc(Number(r))));
      }
    }
  ) });
}
function N(e, i, a) {
  a === void 0 || a === "" ? delete e[i] : e[i] = a;
}
function B(e, i, a) {
  if (a < 0 || a >= e.length) return;
  const [t] = e.splice(i, 1);
  e.splice(a, 0, t);
}
function D({
  label: e,
  onClick: i,
  disabled: a,
  children: t
}) {
  return /* @__PURE__ */ n(
    "button",
    {
      type: "button",
      className: "aq-icon-btn",
      title: e,
      "aria-label": e,
      onClick: i,
      disabled: a,
      children: t
    }
  );
}
function W({
  t: e,
  index: i,
  count: a,
  onMove: t,
  onRemove: r
}) {
  return /* @__PURE__ */ p(z, { children: [
    /* @__PURE__ */ n(D, { label: e("moveUp"), disabled: i === 0, onClick: () => t(i - 1), children: "↑" }),
    /* @__PURE__ */ n(
      D,
      {
        label: e("moveDown"),
        disabled: i === a - 1,
        onClick: () => t(i + 1),
        children: "↓"
      }
    ),
    /* @__PURE__ */ n(D, { label: e("remove"), onClick: r, children: "×" })
  ] });
}
function Je({
  ctx: e,
  group: i,
  onOpen: a
}) {
  const { t, edit: r, project: s, lang: u } = e, y = xe(), c = typeof i.reset.cron == "string" ? i.reset.cron : "", d = c ? Ce(c, y) : null, o = typeof i.reset.timezone == "string" ? i.reset.timezone : "", h = Array.isArray(i.distribution.triggers) ? i.distribution.triggers : [], m = i.distribution.tiers ?? {}, b = Object.keys(i.rest).filter((l) => l !== "actions"), v = () => {
    const l = R(), f = H();
    r(
      (q) => {
        q.quests.push({
          key: K(
            "quest",
            q.quests.map((k) => k.key)
          ),
          uid: f,
          tier: "",
          title: { key: l },
          description: { key: R() },
          duration: "",
          stages: [me(1)],
          rest: {}
        });
      },
      [{ key: l, lang: u, value: t("newQuestTitle") }]
    ), a(f);
  };
  return /* @__PURE__ */ p(z, { children: [
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("group") }),
      /* @__PURE__ */ n(L, { ctx: e, label: t("title"), node: i.title }),
      /* @__PURE__ */ n(L, { ctx: e, label: t("description"), node: i.description, multiline: !0 }),
      /* @__PURE__ */ n(w, { label: t("actorType"), children: /* @__PURE__ */ p(
        "select",
        {
          className: "select",
          value: i.actorType,
          onChange: (l) => r((f) => {
            f.actorType = l.target.value;
          }),
          children: [
            /* @__PURE__ */ n("option", { value: "player", children: t("actor.player") }),
            /* @__PURE__ */ n("option", { value: "global", children: t("actor.global") }),
            !["player", "global"].includes(i.actorType) && /* @__PURE__ */ n("option", { value: i.actorType, children: i.actorType })
          ]
        }
      ) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("reset") }),
      /* @__PURE__ */ n(
        w,
        {
          label: t("cron"),
          hint: c ? $e(c) ? /* @__PURE__ */ p("span", { className: "aq-ok", children: [
            d ?? t("cronRaw"),
            o ? `, ${o}` : ""
          ] }) : /* @__PURE__ */ n("span", { className: "aq-err", children: t("cronInvalid") }) : /* @__PURE__ */ n("span", { className: "aq-hint", children: t("noReset") }),
          children: /* @__PURE__ */ n(
            "input",
            {
              className: "input",
              value: c,
              placeholder: "0 4 * * *",
              onChange: (l) => r((f) => N(f.reset, "cron", l.target.value))
            }
          )
        }
      ),
      /* @__PURE__ */ p(w, { label: t("timezone"), children: [
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
        /* @__PURE__ */ n("datalist", { id: "aq-timezones", children: Ze.map((l) => /* @__PURE__ */ n("option", { value: l }, l)) })
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("distribution") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(w, { label: t("strategy"), children: /* @__PURE__ */ p(
          "select",
          {
            className: "select",
            value: typeof i.distribution.strategy == "string" ? i.distribution.strategy.toLowerCase() : "",
            onChange: (l) => r((f) => N(f.distribution, "strategy", l.target.value)),
            children: [
              /* @__PURE__ */ n("option", { value: "", children: "—" }),
              ["none", "sequential", "random"].map((l) => /* @__PURE__ */ n("option", { value: l, children: t(`strategy.${l}`) }, l))
            ]
          }
        ) }),
        /* @__PURE__ */ n(
          O,
          {
            label: t("amount"),
            value: i.distribution.amount,
            onChange: (l) => r((f) => N(f.distribution, "amount", l))
          }
        )
      ] }),
      /* @__PURE__ */ n("span", { className: "field-label", children: t("triggers") }),
      We.map((l) => /* @__PURE__ */ p("label", { className: "aq-check", children: [
        /* @__PURE__ */ n(
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
      i.tiers.length > 0 && /* @__PURE__ */ p(z, { children: [
        /* @__PURE__ */ n("span", { className: "field-label", children: t("perTier") }),
        /* @__PURE__ */ n("div", { className: "aq-row", children: i.tiers.map((l) => /* @__PURE__ */ n(
          O,
          {
            label: A(s, l.title.key, u) || l.key,
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
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("limits") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(
          O,
          {
            label: t("maxActive"),
            value: i.limits.max_active,
            onChange: (l) => r((f) => N(f.limits, "max_active", l))
          }
        ),
        /* @__PURE__ */ n(
          O,
          {
            label: t("maxPerPeriod"),
            value: i.limits.max_per_period,
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
              checked: i.rotation.enabled === !0,
              onChange: (l) => r((f) => N(f.rotation, "enabled", l.target.checked ? !0 : void 0))
            }
          ),
          t("rotationEnabled")
        ] }),
        /* @__PURE__ */ n(
          O,
          {
            label: t("maxPerPeriod"),
            value: i.rotation.max_per_period,
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
                key: K(
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
      i.tiers.map((l, f) => /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(w, { label: t("tierKey"), children: /* @__PURE__ */ n(
          "input",
          {
            className: "input",
            defaultValue: l.key,
            onBlur: (q) => {
              const k = G(q.target.value);
              k !== l.key && r((E) => {
                const g = K(
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
          i.quests.length
        ] }),
        /* @__PURE__ */ p(P, { size: "sm", variant: "primary", onClick: v, children: [
          "+ ",
          t("addQuest")
        ] })
      ] }),
      i.quests.length === 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: t("noQuests") }),
      /* @__PURE__ */ n("div", { className: "aq-list", children: i.quests.map((l, f) => /* @__PURE__ */ p("div", { className: "aq-item", children: [
        /* @__PURE__ */ p(
          "button",
          {
            type: "button",
            className: "aq-item-main",
            onClick: () => a(l.uid),
            title: t("open"),
            children: [
              /* @__PURE__ */ n("span", { className: "aq-item-title", children: A(s, l.title.key, u) || l.key }),
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
            count: i.quests.length,
            onMove: (q) => r((k) => B(k.quests, f, q)),
            onRemove: () => r((q) => {
              q.quests.splice(f, 1);
            })
          }
        )
      ] }, l.uid)) }),
      b.length > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: t("kept", { fields: b.join(", ") }) })
    ] }),
    /* @__PURE__ */ n(tt, { ctx: e, group: i })
  ] });
}
function pe({
  ctx: e,
  value: i,
  onChange: a
}) {
  const { chain: t, legacy: r } = Ke(i);
  return /* @__PURE__ */ p("div", { className: "aq-chain", children: [
    /* @__PURE__ */ n(
      Ne,
      {
        projectId: e.projectId,
        value: t,
        onChange: (s) => a(X(s, r))
      }
    ),
    r.length > 0 && /* @__PURE__ */ p("div", { className: "aq-legacy", children: [
      /* @__PURE__ */ n("span", { className: "aq-hint", children: e.t("legacySteps") }),
      r.map((s, u) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: a step has no identity
        /* @__PURE__ */ p("div", { className: "aq-legacy-row", children: [
          /* @__PURE__ */ n("code", { children: ze(s) }),
          /* @__PURE__ */ n(
            D,
            {
              label: e.t("remove"),
              onClick: () => a(X(t, r.filter((y, c) => c !== u))),
              children: "×"
            }
          )
        ] }, u)
      ))
    ] })
  ] });
}
function et(e) {
  return Array.isArray(e) ? e.length : e == null ? 0 : 1;
}
function tt({ ctx: e, group: i }) {
  const { t: a, edit: t } = e, r = j(i.rest.actions);
  return /* @__PURE__ */ p("section", { className: "aq-section", children: [
    /* @__PURE__ */ n("span", { className: "aq-h", children: a("hooks") }),
    /* @__PURE__ */ n("span", { className: "aq-hint", children: a("hooksHint") }),
    He.map((s) => {
      const u = et(r[s]);
      return /* @__PURE__ */ p("details", { className: "aq-fold", children: [
        /* @__PURE__ */ p("summary", { children: [
          /* @__PURE__ */ n("span", { children: a(`hook.${s}`) }),
          u > 0 && /* @__PURE__ */ n("span", { className: "aq-badge", title: a("actionCount", { count: u }), children: u })
        ] }),
        /* @__PURE__ */ n(
          pe,
          {
            ctx: e,
            value: r[s],
            onChange: (y) => t((c) => {
              const d = { ...j(c.rest.actions) };
              N(d, s, y), N(c.rest, "actions", Object.keys(d).length ? d : void 0);
            })
          }
        )
      ] }, s);
    })
  ] });
}
function me(e) {
  return {
    key: `stage_${e}`,
    title: { key: R() },
    flow: "parallel",
    objectives: [],
    rest: {}
  };
}
function it({ ctx: e, group: i, index: a }) {
  const { t, edit: r } = e, s = i.quests[a], u = (o, h) => r((m) => o(m.quests[a]), h), y = i.quests.some((o, h) => h !== a && o.key === s.key), c = s.duration !== "" && !Ve.test(s.duration.replace(/\s/g, "").toLowerCase()), d = Object.keys(s.rest).filter((o) => o !== "actions");
  return /* @__PURE__ */ p(z, { children: [
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("general") }),
      /* @__PURE__ */ n(L, { ctx: e, label: t("title"), node: s.title }),
      /* @__PURE__ */ n(L, { ctx: e, label: t("description"), node: s.description, multiline: !0 }),
      /* @__PURE__ */ p("div", { className: "aq-row aq-top", children: [
        /* @__PURE__ */ n(w, { label: t("tier"), children: /* @__PURE__ */ p(
          "select",
          {
            className: "select",
            value: s.tier,
            onChange: (o) => u((h) => {
              h.tier = o.target.value;
            }),
            children: [
              /* @__PURE__ */ n("option", { value: "", children: t("noTier") }),
              i.tiers.map((o) => /* @__PURE__ */ n("option", { value: o.key, children: A(e.project, o.title.key, e.lang) || o.key }, o.key)),
              s.tier && !i.tiers.some((o) => o.key === s.tier) && /* @__PURE__ */ n("option", { value: s.tier, children: s.tier })
            ]
          }
        ) }),
        /* @__PURE__ */ n(
          w,
          {
            label: t("duration"),
            hint: /* @__PURE__ */ n("span", { className: c ? "aq-err" : "aq-hint", children: t(c ? "durationInvalid" : "durationHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input",
                value: s.duration,
                placeholder: "24h",
                onChange: (o) => u((h) => {
                  h.duration = o.target.value.trim();
                })
              }
            )
          }
        )
      ] }),
      /* @__PURE__ */ p("details", { className: "aq-fold", open: y || void 0, children: [
        /* @__PURE__ */ n("summary", { children: t("more") }),
        /* @__PURE__ */ n(
          w,
          {
            label: t("key"),
            hint: /* @__PURE__ */ n("span", { className: y ? "aq-err" : "aq-hint", children: t(y ? "keyTaken" : "questKeyHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input aq-mono",
                value: s.key,
                onChange: (o) => u((h) => {
                  h.key = re(o.target.value);
                }),
                onBlur: () => {
                  const o = G(s.key);
                  o !== s.key && u((h) => {
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
            onClick: () => u((o) => {
              const h = K(
                `stage_${o.stages.length + 1}`,
                o.stages.map((m) => m.key)
              );
              o.stages.push({ ...me(o.stages.length + 1), key: h });
            }),
            children: [
              "+ ",
              t("addStage")
            ]
          }
        )
      ] }),
      /* @__PURE__ */ n("ol", { className: "aq-stages", children: s.stages.map((o, h) => /* @__PURE__ */ n(
        at,
        {
          ctx: e,
          quest: s,
          stage: o,
          si: h,
          editQuest: u
        },
        `${o.key}-${h}`
      )) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("rewards") }),
      /* @__PURE__ */ n("span", { className: "aq-hint", children: t("rewardsHint") }),
      /* @__PURE__ */ n(
        pe,
        {
          ctx: e,
          value: j(s.rest.actions).complete,
          onChange: (o) => u((h) => {
            const m = { ...j(h.rest.actions) };
            N(m, "complete", o), N(h.rest, "actions", Object.keys(m).length > 0 ? m : void 0);
          })
        }
      ),
      d.length > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: t("kept", { fields: d.join(", ") }) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: t("preview") }),
      /* @__PURE__ */ n(ot, { ctx: e, quest: s })
    ] })
  ] });
}
function at({
  ctx: e,
  quest: i,
  stage: a,
  si: t,
  editQuest: r
}) {
  const { t: s } = e, [u, y] = ee(null), c = (m) => r((b) => m(b.stages[t])), d = A(e.project, a.title.key, e.lang), o = a.flow || "parallel", h = i.stages.flatMap((m) => m.objectives.map((b) => b.key));
  return /* @__PURE__ */ p("li", { className: "aq-stage", children: [
    /* @__PURE__ */ p("div", { className: "aq-stage-head", children: [
      /* @__PURE__ */ n("span", { className: "aq-stage-n", children: t + 1 }),
      /* @__PURE__ */ n("div", { className: "aq-stage-title", children: /* @__PURE__ */ n(
        J,
        {
          value: d,
          placeholder: s("stageTitle"),
          "aria-label": s("stage", { n: t + 1 }),
          onChange: (m) => e.edit(() => {
          }, [{ key: a.title.key, lang: e.lang, value: m }])
        }
      ) }),
      /* @__PURE__ */ n(
        W,
        {
          t: s,
          index: t,
          count: i.stages.length,
          onMove: (m) => r((b) => B(b.stages, t, m)),
          onRemove: () => r((m) => {
            m.stages.splice(t, 1);
          })
        }
      )
    ] }),
    a.objectives.length > 1 && /* @__PURE__ */ n("div", { className: "aq-seg", role: "radiogroup", "aria-label": s("flow"), children: ["parallel", "sequential"].map((m) => /* @__PURE__ */ n(
      "button",
      {
        type: "button",
        role: "radio",
        "aria-checked": o === m,
        className: `chip${o === m ? " is-active" : ""}`,
        onClick: () => c((b) => {
          b.flow = m;
        }),
        children: s(`flow.${m}`)
      },
      m
    )) }),
    a.objectives.length === 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: s("noObjectives") }),
    /* @__PURE__ */ n("ul", { className: "aq-objectives", children: a.objectives.map((m, b) => /* @__PURE__ */ n(
      nt,
      {
        t: s,
        projectId: e.projectId,
        n: o === "sequential" ? `${t + 1}.${b + 1}` : "•",
        objective: m,
        open: u === m.key,
        onToggle: () => y(u === m.key ? null : m.key),
        keyTaken: h.filter((v) => v === m.key).length > 1,
        index: b,
        count: a.objectives.length,
        onMove: (v) => c((l) => B(l.objectives, b, v)),
        onChange: (v) => c((l) => v(l.objectives[b])),
        onRename: (v) => {
          u === m.key && y(v);
        },
        onRemove: () => c((v) => {
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
          const m = Oe("block_break", h);
          r((b) => {
            b.stages[t].objectives.push({
              key: m,
              type: "block_break",
              params: le("block_break")
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
function he(e, i) {
  return M(i) ? e(`objective.${i}`) : i;
}
function nt({
  t: e,
  projectId: i,
  n: a,
  objective: t,
  open: r,
  onToggle: s,
  keyTaken: u,
  index: y,
  count: c,
  onMove: d,
  onChange: o,
  onRename: h,
  onRemove: m
}) {
  const b = M(t.type), v = b ? b.params : Object.keys(t.params).map((g) => ({ name: g, kind: "text" })), l = ce(t.params), f = ue(t.type, t.params), q = Fe(t.type, t.params), k = u || q.length > 0, E = (g) => g.kind === "item" ? /* @__PURE__ */ n(
    rt,
    {
      t: e,
      projectId: i,
      spec: g,
      params: t.params,
      onChange: (x) => o((T) => x(T.params))
    },
    g.name
  ) : /* @__PURE__ */ n(
    st,
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
        /* @__PURE__ */ n("span", { className: "aq-obj-n", children: a }),
        /* @__PURE__ */ n("span", { className: "aq-obj-icon", "aria-hidden": !0, children: b?.icon ?? "?" }),
        /* @__PURE__ */ p("span", { className: "aq-obj-text", children: [
          he(e, t.type),
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
            title: u ? e("objectiveKeyTaken") : e("missing", {
              fields: q.map((g) => e(`param.${g}`)).join(", ")
            })
          }
        )
      ] }),
      /* @__PURE__ */ n(W, { t: e, index: y, count: c, onMove: d, onRemove: m })
    ] }),
    r && /* @__PURE__ */ p("div", { className: "aq-obj-form", children: [
      /* @__PURE__ */ n(w, { label: e("type"), children: /* @__PURE__ */ p(
        "select",
        {
          className: "select",
          value: t.type,
          onChange: (g) => o((x) => {
            x.type = g.target.value, x.params = le(x.type);
          }),
          children: [
            oe.map((g) => /* @__PURE__ */ p("option", { value: g.type, children: [
              g.icon,
              " ",
              e(`objective.${g.type}`)
            ] }, g.type)),
            !b && /* @__PURE__ */ n("option", { value: t.type, children: t.type })
          ]
        }
      ) }),
      /* @__PURE__ */ n("div", { className: "aq-row aq-wrap", children: v.filter((g) => !g.advanced).map(E) }),
      q.length > 0 && /* @__PURE__ */ n("span", { className: "aq-warn", children: e("missing", {
        fields: q.map((g) => e(`param.${g}`)).join(", ")
      }) }),
      /* @__PURE__ */ p("details", { className: "aq-fold", open: u || void 0, children: [
        /* @__PURE__ */ n("summary", { children: e("more") }),
        v.filter((g) => g.advanced).map(E),
        /* @__PURE__ */ n(
          w,
          {
            label: e("key"),
            hint: /* @__PURE__ */ n("span", { className: u ? "aq-err" : "aq-hint", children: e(u ? "objectiveKeyTaken" : "objectiveKeyHint") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input aq-mono",
                value: t.key,
                onChange: (g) => {
                  const x = re(g.target.value);
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
function rt({
  t: e,
  projectId: i,
  spec: a,
  params: t,
  onChange: r
}) {
  const s = de(t, a), u = Array.isArray(t.match) ? t.match : void 0, y = (c, d) => r((o) => Ge(o, a, c, d));
  return /* @__PURE__ */ n(
    w,
    {
      label: e(`param.${a.name}`),
      hint: Object.keys(s).length === 0 ? /* @__PURE__ */ n("span", { className: "aq-hint", children: e("anyItem") }) : void 0,
      children: /* @__PURE__ */ n(
        je,
        {
          projectId: i,
          value: s,
          match: u,
          onChange: (c) => y(c, u),
          onMatchChange: (c) => y(s, c)
        }
      )
    }
  );
}
function st({
  t: e,
  spec: i,
  value: a,
  onChange: t
}) {
  const r = e(`param.${i.name}`), s = i.required && (a === void 0 || a === ""), u = typeof a == "string" || typeof a == "number" ? String(a) : "";
  if (i.kind === "int")
    return /* @__PURE__ */ n(O, { label: r, value: a, onChange: t });
  const y = s ? /* @__PURE__ */ n("span", { className: "aq-warn", children: e("required") }) : i.name === "itemKey" ? /* @__PURE__ */ n("span", { className: "aq-hint", children: e("itemKeyHint") }) : void 0;
  if (i.kind === "material")
    return /* @__PURE__ */ n(w, { label: r, hint: y, children: /* @__PURE__ */ n(
      we,
      {
        value: u,
        placeholder: e("any"),
        "aria-label": r,
        onChange: (d) => t(d.toUpperCase().replace(/\s+/g, "_"))
      }
    ) });
  const c = i.kind === "entity";
  return /* @__PURE__ */ p(w, { label: r, hint: y, children: [
    /* @__PURE__ */ n(
      "input",
      {
        className: `input${c ? " aq-mono" : ""}`,
        list: c ? "aq-entities" : void 0,
        value: u,
        placeholder: c ? e("any") : "",
        onChange: (d) => t(c ? d.target.value.toUpperCase().replace(/\s+/g, "_") : d.target.value)
      }
    ),
    c && /* @__PURE__ */ n("datalist", { id: "aq-entities", children: Qe.map((d) => /* @__PURE__ */ n("option", { value: d }, d)) })
  ] });
}
function ot({ ctx: e, quest: i }) {
  const { t: a, project: t, lang: r } = e, s = (u) => {
    const y = ue(u.type, u.params), c = ce(u.params);
    return `${he(a, u.type)}${y ? ` · ${y}` : ""}${c !== void 0 ? ` · 0/${String(c)}` : ""}`;
  };
  return /* @__PURE__ */ p("div", { className: "aq-preview", children: [
    /* @__PURE__ */ n("span", { className: "aq-preview-title", children: A(t, i.title.key, r) || i.key }),
    A(t, i.description.key, r) && /* @__PURE__ */ n("span", { className: "aq-preview-line", children: A(t, i.description.key, r) }),
    i.stages.map((u, y) => (
      // biome-ignore lint/suspicious/noArrayIndexKey: stages are listed in order
      /* @__PURE__ */ p("span", { className: "aq-preview-stage", children: [
        i.stages.length > 1 && /* @__PURE__ */ n("span", { className: "aq-preview-line", children: A(t, u.title.key, r) || a("stage", { n: y + 1 }) }),
        u.objectives.map((c) => /* @__PURE__ */ p("span", { className: "aq-preview-line", children: [
          "• ",
          s(c)
        ] }, c.key))
      ] }, y)
    ))
  ] });
}
const lt = {
  kind: ae,
  dir: ne,
  schemaVersion: V,
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: Z.en,
  labels: Z,
  component: Ye,
  title: (e, { project: i, lang: a }) => A(i, e.title.key, a),
  create: ({ id: e, projectId: i }) => ({
    id: e,
    projectId: i,
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
  parse: (e, i) => Re(e, i),
  serialize: (e, i) => Pe(e, i),
  ltextSites: (e) => Me(e),
  board: {
    size: { width: 220, height: 48 },
    // The group's quests, one selectable row each: the Properties panel opens the one clicked.
    items: (e, { project: i, lang: a }) => e.quests.map((t) => ({
      id: t.uid,
      label: A(i, t.title.key, a) || t.key,
      detail: t.tier || void 0
    })),
    summary: (e) => `quest_group · ${e.quests.length}`
  }
}, ct = {
  id: "msquests",
  displayName: "Quests",
  schemaVersion: "quest_group/v1",
  sdk: _e,
  sidebar: { icon: () => null, label: "Quests", to: "/m/msquests" },
  // Quests are Documents: they are edited from the project, no screen of their own.
  routes: [],
  documents: [lt]
}, mt = { ...ct, sdk: "1.4.0" };
export {
  mt as default
};
