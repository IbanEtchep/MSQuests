import { createModuleI18n as Ie, fieldText as Me, newTextKey as $, textValue as E, injectModuleStyles as De, useProject as Pe, useContentLang as He, Button as K, selectDocumentItem as Ke, SortableList as ie, updateDocument as Ge, useUiLang as Qe, MiniMessageInput as ve, ActionChainEditor as Ue, ItemInput as ze, MaterialInput as Fe, SDK_VERSION as Be } from "@artisan/editor-sdk";
import { jsxs as h, jsx as s, Fragment as U } from "react/jsx-runtime";
import { useState as ae, useEffect as Ve } from "react";
const We = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"]
}, Ze = /^\d+$/;
function X(e, t, i) {
  const n = String(i).padStart(2, "0");
  return e === "fr" ? i === 0 ? `${t} h` : `${t} h ${n}` : `${String(t).padStart(2, "0")}:${n}`;
}
function T(e, t, i) {
  return Ze.test(e) && Number(e) >= t && Number(e) <= i;
}
function de(e) {
  return e.trim().split(/\s+/).length === 5;
}
function Ye(e, t) {
  const i = t === "fr", n = e.trim().split(/\s+/);
  if (n.length !== 5) return null;
  const [a, r, l, u, d] = n;
  if (u !== "*") return null;
  const c = /^\*\/(\d+)$/;
  if (l === "*" && d === "*") {
    if (T(a, 0, 59) && T(r, 0, 23))
      return i ? `tous les jours à ${X(t, +r, +a)}` : `every day at ${X(t, +r, +a)}`;
    const p = c.exec(r);
    if (T(a, 0, 59) && p)
      return i ? `toutes les ${p[1]} h, à la minute ${a}` : `every ${p[1]} hours, at minute ${a}`;
    if (T(a, 0, 59) && r === "*")
      return i ? `toutes les heures, à la minute ${a}` : `every hour, at minute ${a}`;
    const v = c.exec(a);
    return v && r === "*" ? i ? `toutes les ${v[1]} minutes` : `every ${v[1]} minutes` : null;
  }
  if (!T(a, 0, 59) || !T(r, 0, 23)) return null;
  const o = X(t, +r, +a);
  if (l === "*" && T(d, 0, 7)) {
    const p = We[i ? "fr" : "en"][Number(d) % 7];
    return i ? `chaque ${p} à ${o}` : `every ${p} at ${o}`;
  }
  return d === "*" && T(l, 1, 31) ? i ? `le ${l === "1" ? "1er" : l} de chaque mois à ${o}` : `on day ${l} of each month at ${o}` : null;
}
function D(e, t, i) {
  const n = /* @__PURE__ */ new Set();
  for (const a of e.split(",")) {
    const r = /^(\*|(\d+)(?:-(\d+))?)(?:\/(\d+))?$/.exec(a);
    if (!r) return null;
    const l = r[4] ? Number(r[4]) : 1, u = r[1] === "*" ? t : Number(r[2]), d = r[1] === "*" ? i : r[3] ? Number(r[3]) : r[4] ? i : u;
    if (l < 1 || u < t || d > i || u > d) return null;
    for (let c = u; c <= d; c += l) n.add(c);
  }
  return n;
}
function Xe(e, t) {
  const i = e.trim().split(/\s+/);
  if (i.length !== 5) return null;
  const n = D(i[0], 0, 59), a = D(i[1], 0, 23), r = D(i[2], 1, 31), l = D(i[3], 1, 12), u = D(i[4], 0, 7);
  if (!n || !a || !r || !l || !u) return null;
  u.has(7) && u.add(0);
  const d = i[2] === "*", c = i[4] === "*", o = (y) => d && c ? !0 : d ? u.has(y.getDay()) : c ? r.has(y.getDate()) : r.has(y.getDate()) || u.has(y.getDay()), p = new Date(t.getTime());
  p.setSeconds(0, 0), p.setMinutes(p.getMinutes() + 1);
  const v = t.getTime() + 366 * 24 * 3600 * 1e3;
  for (; p.getTime() <= v; ) {
    if (!l.has(p.getMonth() + 1) || !o(p)) {
      p.setDate(p.getDate() + 1), p.setHours(0, 0, 0, 0);
      continue;
    }
    if (!a.has(p.getHours())) {
      p.setHours(p.getHours() + 1, 0, 0, 0);
      continue;
    }
    if (n.has(p.getMinutes())) return p;
    p.setMinutes(p.getMinutes() + 1);
  }
  return null;
}
const G = {
  kindLabel: "Quest groups",
  collection: "Quests",
  group: "Group",
  title: "Title",
  description: "Description",
  actorType: "Who plays",
  "actor.player": "Each player",
  "actor.global": "The whole server",
  reset: "Period reset",
  cron: "Schedule (cron)",
  cronInvalid: "A cron has 5 fields: minute hour day month weekday.",
  nextRun: "next: {{when}}",
  cronRaw: "Custom schedule",
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
  questsPerPeriod: "Quests per period",
  rotationsPerPeriod: "Rotations per period",
  noLimit: "No limit",
  rotation: "Rotation",
  rotationEnabled: "Rotate quests",
  tiers: "Tiers",
  tierKey: "Key",
  addTier: "Add a tier",
  noTier: "No tier",
  quests: "Quests",
  questCount: "{{count}} quests",
  questCountOne: "1 quest",
  addQuest: "New quest",
  newQuestTitle: "New quest",
  open: "Edit",
  remove: "Remove",
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
  questEvents: "Quest events",
  questEventsHint: "Actions run on an event of this quest, after the group's for the same event: any action Artisan or an installed plugin declares, with conditions; a plugin without an action (guild XP…) is reached with a command. Write what the player wins in the description.",
  "questHook.start": "The quest starts",
  "questHook.objective_progress": "An objective progresses",
  "questHook.objective_complete": "An objective is completed",
  "questHook.complete": "The quest is completed",
  addEvent: "+ Add an event…",
  removeEvent: "Remove this event and its actions",
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
}, Z = {
  kindLabel: "Groupes de quêtes",
  collection: "Quêtes",
  group: "Groupe",
  title: "Titre",
  description: "Description",
  actorType: "Qui joue",
  "actor.player": "Chaque joueur",
  "actor.global": "Tout le serveur",
  reset: "Réinitialisation de la période",
  cron: "Planification (cron)",
  cronInvalid: "Un cron a 5 champs : minute heure jour mois jour-de-semaine.",
  nextRun: "prochaine : {{when}}",
  cronRaw: "Planification personnalisée",
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
  questsPerPeriod: "Quêtes par période",
  rotationsPerPeriod: "Rotations par période",
  noLimit: "Sans limite",
  rotation: "Rotation",
  rotationEnabled: "Faire tourner les quêtes",
  tiers: "Tiers",
  tierKey: "Clé",
  addTier: "Ajouter un tier",
  noTier: "Aucun tier",
  quests: "Quêtes",
  questCount: "{{count}} quêtes",
  questCountOne: "1 quête",
  addQuest: "Nouvelle quête",
  newQuestTitle: "Nouvelle quête",
  open: "Modifier",
  remove: "Supprimer",
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
  questEvents: "Événements de la quête",
  questEventsHint: "Actions exécutées sur un événement de cette quête, après celles du groupe pour le même événement : toute action d'Artisan ou d'un plugin installé, avec conditions ; un plugin sans action (XP de guilde…) passe par une commande. Écrivez ce que gagne le joueur dans la description.",
  "questHook.start": "La quête commence",
  "questHook.objective_progress": "Un objectif progresse",
  "questHook.objective_complete": "Un objectif est terminé",
  "questHook.complete": "La quête est terminée",
  addEvent: "+ Ajouter un événement…",
  removeEvent: "Retirer cet événement et ses actions",
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
}, ue = { en: G.kindLabel, fr: Z.kindLabel }, Je = { en: G.collection, fr: Z.collection }, et = Ie({ en: G, fr: Z });
function qe(e) {
  const t = e === "fr" ? Z : G;
  return (i, n) => {
    const a = t[i] ?? G[i] ?? i;
    return n ? a.replace(/\{\{(\w+)\}\}/g, (r, l) => String(n[l] ?? r)) : a;
  };
}
const re = "quest_group", ke = "quest_groups", tt = "quests", se = "quest_group/v1", ee = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
function V(e = Date.now()) {
  let t = "", i = e;
  for (let r = 0; r < 10; r++)
    t = ee[i % 32] + t, i = Math.floor(i / 32);
  const n = new Uint8Array(16);
  crypto.getRandomValues(n);
  let a = "";
  for (let r = 0; r < 16; r++) a += ee[n[r] % 32];
  return t + a;
}
function z(e, t) {
  const i = new Set(t);
  if (!i.has(e)) return e;
  for (let n = 2; ; n++) if (!i.has(`${e}_${n}`)) return `${e}_${n}`;
}
function nt(e, t) {
  const i = new Set(t), n = new Uint8Array(4);
  for (; ; ) {
    crypto.getRandomValues(n);
    const a = Array.from(n, (l) => ee[l % 32].toLowerCase()).join(""), r = `${Y(e)}_${a}`;
    if (!i.has(r)) return r;
  }
}
function xe(e) {
  return e.toLowerCase().replace(/[^a-z0-9_]/g, "_");
}
function Y(e) {
  return e.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().replace(/[^a-z0-9_]+/g, "_").replace(/^_+|_+$/g, "") || "quest";
}
function j(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? { ...e } : {};
}
function J(e) {
  return Array.isArray(e) ? e : [];
}
function N(e) {
  return typeof e == "string" || typeof e == "number" || typeof e == "boolean" ? String(e) : "";
}
function P(e, ...t) {
  const i = { ...e };
  for (const n of t) delete i[n];
  return i;
}
function it(e, t) {
  const i = {}, n = [];
  function a(d) {
    if (d && typeof d == "object" && !Array.isArray(d)) {
      const p = $(), v = {};
      for (const [y, g] of Object.entries(d)) v[y] = N(g);
      return i[p] = v, { key: p };
    }
    const c = typeof d == "string" ? /^\{i18n:(.+)\}$/.exec(d) : null;
    if (c) {
      const p = t.translations?.[c[1]];
      return p && (i[c[1]] = { ...p }), { key: c[1] };
    }
    const o = $();
    return i[o] = d == null || d === "" ? {} : { [t.lang]: N(d) }, { key: o };
  }
  const r = Object.entries(j(e.tiers)).map(([d, c]) => {
    const o = j(c);
    return { key: d, title: a(o.title), rest: P(o, "title") };
  }), l = J(e.quests).map((d, c) => {
    const o = j(d), p = J(o.stages).map((v, y) => {
      const g = j(v), q = N(g.flow).toLowerCase();
      return {
        key: N(g.key) || `stage_${y + 1}`,
        title: a(g.title),
        flow: q === "parallel" || q === "sequential" ? q : "",
        objectives: J(g.objectives).map((m, b) => {
          const f = j(m);
          return {
            key: N(f.key) || `objective_${b + 1}`,
            type: N(f.type),
            params: P(f, "key", "type")
          };
        }),
        rest: P(g, "key", "title", "flow", "objectives")
      };
    });
    return {
      key: N(o.key) || `quest_${c + 1}`,
      uid: N(o.uid) || V(),
      tier: N(o.tier),
      title: a(o.title),
      description: a(o.description),
      duration: N(o.duration),
      stages: p,
      rest: at(o)
    };
  }), u = {
    id: N(e.id),
    projectId: t.projectId,
    uid: N(e.uid) || V(),
    title: a(e.title),
    description: a(e.description),
    actorType: N(e.actor_type) || "player",
    reset: j(e.reset),
    limits: j(e.limits),
    rotation: j(e.rotation),
    distribution: j(e.distribution),
    tiers: r,
    quests: l,
    rest: _e(P(
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
  return e.schema !== void 0 && e.schema !== se && n.push(`schema: ${String(e.schema)}`), { doc: u, i18nKeys: i, unsupported: n };
}
function _e(e) {
  return e.on !== void 0 && (e.actions == null && (e.actions = e.on), delete e.on), e;
}
function at(e) {
  const t = _e(
    P(e, "key", "uid", "tier", "title", "description", "duration", "stages", "reward_display")
  );
  if (t.rewards !== void 0) {
    const i = j(t.actions);
    i.complete == null && (t.actions = { ...i, complete: t.rewards }), delete t.rewards;
  }
  return t;
}
function rt(e) {
  return typeof e == "string" ? e === "" : Object.values(e).every((t) => t === "");
}
function S(e) {
  const t = {};
  for (const [i, n] of Object.entries(e))
    n == null || n === "" || Array.isArray(n) && n.length === 0 || typeof n == "object" && !Array.isArray(n) && Object.keys(n).length === 0 || (t[i] = n);
  return t;
}
const st = [
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
function ot(e, t) {
  const i = (c) => Me(t, c.key), n = (c) => {
    const o = i(c);
    return rt(o) ? void 0 : o;
  }, a = {};
  for (const c of e.tiers) a[c.key] = { ...c.rest, title: i(c.title) };
  const r = e.quests.map(
    (c) => S({
      key: c.key,
      uid: c.uid,
      tier: c.tier,
      title: i(c.title),
      description: n(c.description),
      duration: c.duration,
      ...c.rest,
      stages: c.stages.map(
        (o) => S({
          key: o.key,
          title: n(o.title),
          flow: o.flow,
          ...o.rest,
          objectives: o.objectives.map((p) => ({ key: p.key, type: p.type, ...p.params }))
        })
      )
    })
  ), l = {
    ...e.rest,
    id: e.id,
    schema: se,
    uid: e.uid,
    title: i(e.title),
    description: n(e.description),
    actor_type: e.actorType,
    reset: S(e.reset),
    limits: S(e.limits),
    rotation: S(e.rotation),
    distribution: S(e.distribution),
    tiers: a,
    quests: r
  }, u = {};
  for (const c of st) c in l && (u[c] = l[c]);
  for (const c of Object.keys(l)) c in u || (u[c] = l[c]);
  const d = S(u);
  return d.quests = r, d;
}
function* lt(e) {
  const t = `${ke}.${e.id}`;
  yield { lText: e.title, key: `${t}.title`, path: "title", kind: "quest_group.title" }, yield {
    lText: e.description,
    key: `${t}.description`,
    path: "description",
    kind: "quest_group.description"
  };
  for (const n of e.tiers)
    yield {
      lText: n.title,
      key: `${t}.tiers.${n.key}.title`,
      path: `tiers.${n.key}.title`,
      kind: "quest_group.tier"
    };
  const i = /* @__PURE__ */ new Set();
  for (let n = 0; n < e.quests.length; n++) {
    const a = e.quests[n], r = i.has(a.uid) ? `${a.uid}_${n}` : a.uid;
    i.add(a.uid);
    const l = `${t}.quests.${r}`;
    yield {
      lText: a.title,
      key: `${l}.title`,
      path: `quests[${n}].title`,
      kind: "quest.title"
    }, yield {
      lText: a.description,
      key: `${l}.description`,
      path: `quests[${n}].description`,
      kind: "quest.description"
    };
    const u = /* @__PURE__ */ new Set();
    for (let d = 0; d < a.stages.length; d++) {
      const c = a.stages[d], o = u.has(c.key) ? `${c.key}_${d}` : c.key;
      u.add(c.key), yield {
        lText: c.title,
        key: `${l}.stages.${o}.title`,
        path: `quests[${n}].stages[${d}].title`,
        kind: "quest.stage"
      };
    }
  }
}
function pe(e) {
  return !!e && typeof e == "object" && !Array.isArray(e) && "type" in e;
}
function ct(e) {
  const t = Array.isArray(e) ? e : e == null ? [] : [e];
  return {
    chain: t.filter((i) => !pe(i)),
    legacy: t.filter(pe)
  };
}
function me(e, t) {
  const i = [...e, ...t];
  return i.length ? i : void 0;
}
function dt(e) {
  const t = String(e.type), i = e.command ?? e.message ?? e.title ?? e.material;
  return typeof i == "string" ? `${t} · ${i}` : t;
}
const ut = [
  "distribution",
  "actor_load",
  "quest_start",
  "objective_progress",
  "objective_complete",
  "quest_complete",
  "period_complete"
], pt = ["start", "objective_progress", "objective_complete", "complete"], C = { name: "amount", kind: "int", required: !0 }, je = [
  { type: "block_break", icon: "⛏", params: [{ name: "material", kind: "material" }, C] },
  {
    type: "craft_item",
    icon: "⚒",
    params: [{ name: "item", kind: "item", legacy: "material" }, C]
  },
  {
    type: "harvest_crop",
    icon: "🌾",
    params: [{ name: "crop", kind: "material", required: !0 }, C]
  },
  {
    type: "fishing",
    icon: "🎣",
    params: [{ name: "item", kind: "item", legacy: "fish_type" }, C]
  },
  {
    type: "deliver_item",
    icon: "📦",
    params: [
      { name: "item", kind: "item", legacy: "material" },
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
function L(e) {
  return je.find((t) => t.type === e);
}
function we(e) {
  const t = {};
  for (const i of L(e)?.params ?? [])
    i.kind === "int" && (t[i.name] = 1);
  return t;
}
function oe(e) {
  return e.amount ?? e.distance;
}
function le(e, t) {
  const i = e[t.name];
  if (i && typeof i == "object" && !Array.isArray(i)) return i;
  const n = t.legacy ? e[t.legacy] : void 0;
  return typeof n == "string" && n !== "" ? { material: n } : {};
}
function mt(e, t, i, n) {
  t.legacy && delete e[t.legacy];
  const a = !i || Object.keys(i).length === 0;
  a ? delete e[t.name] : e[t.name] = i, a || n === void 0 ? delete e.match : e.match = n;
}
function Ne(e) {
  const t = e.title;
  if (typeof t == "string" && t.trim() !== "") return t.replace(/<[^>]*>/g, "");
  const i = e.material ?? e.item;
  return typeof i == "string" ? i : "";
}
function Ee(e, t) {
  const i = L(e);
  return i ? i.params.filter((n) => n.kind !== "int").map((n) => n.kind === "item" ? Ne(le(t, n)) : t[n.name]).filter((n) => typeof n == "string" && n !== "").join(" = ") : Object.keys(t).map((n) => t[n]).filter((n) => typeof n == "string" && n !== "").join(" = ");
}
function ht(e, t) {
  return (L(e)?.params ?? []).filter((i) => i.required && (t[i.name] === void 0 || t[i.name] === "")).map((i) => i.name);
}
const ft = [
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
], yt = "#f59e0b";
function Ae(e, t) {
  return L(t) ? e(`objective.${t}`) : t;
}
function W(e, t) {
  const i = Ee(t.type, t.params), n = oe(t.params);
  return `${Ae(e, t.type)}${i ? ` · ${i}` : ""}${n !== void 0 ? ` · 0/${String(n)}` : ""}`;
}
function Te(e) {
  return e.replace(/<[^<>]*>/g, "").trim();
}
function gt(e) {
  const t = e.distribution?.strategy;
  return typeof t == "string" && t.toLowerCase() === "sequential";
}
function bt(e, t, i) {
  const n = qe(i), a = gt(e);
  return e.quests.map((r, l) => {
    const u = E(t, r.title.key, i);
    return {
      id: r.uid,
      label: Te(u) || r.key,
      detail: r.tier || void 0,
      caption: vt(n, r),
      marker: a ? String(l + 1) : void 0,
      hover: {
        title: u || r.key,
        text: E(t, r.description.key, i) || void 0,
        lines: qt(n, r, t, i)
      }
    };
  });
}
function vt(e, t) {
  const i = t.stages.flatMap((r) => r.objectives), n = i[0];
  if (!n) return;
  const a = i.length - 1;
  return a > 0 ? `${W(e, n)} +${String(a)}` : W(e, n);
}
function qt(e, t, i, n) {
  const a = t.stages.length > 1;
  return t.stages.flatMap(
    (r, l) => r.objectives.map((u) => {
      const d = W(e, u);
      return a ? `<gray>${Te(E(i, r.title.key, n)) || e("stage", { n: l + 1 })} ·</gray> ${d}` : d;
    })
  );
}
function kt(e, t) {
  const i = qe(t), n = typeof e.distribution?.strategy == "string" ? e.distribution.strategy.toLowerCase() : "", a = e.quests.length, r = a === 1 ? i("questCountOne") : i("questCount", { count: a });
  return ["none", "sequential", "random"].includes(n) ? `${r} · ${i(`strategy.${n}`).toLowerCase()}` : r;
}
const xt = `
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
.aq-item { display: flex; align-items: center; gap: 6px; padding: 6px 8px 6px 2px; border: 1px solid var(--border); border-radius: var(--r-md); background: var(--surface); }
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
.aq-event { display: flex; flex-direction: column; gap: 6px; }
.aq-event-name { color: var(--text); font-size: 12px; font-weight: 600; }
.aq-event-add { align-self: flex-start; width: auto; min-width: 240px; }
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
.aq-obj-line { display: flex; align-items: center; gap: 2px; padding: 0 4px 0 2px; }
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
`, _t = /^(\d+|(\d+[dhms])+)$/, jt = ["actor_load", "period_reset", "quest_complete"], wt = (() => {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
  } catch {
    return "";
  }
})();
function Nt(e, t) {
  const i = Xe(e, /* @__PURE__ */ new Date());
  if (!i) return null;
  try {
    return new Intl.DateTimeFormat(t, {
      weekday: "short",
      day: "numeric",
      month: "short",
      hour: "2-digit",
      minute: "2-digit"
    }).format(i);
  } catch {
    return i.toLocaleString();
  }
}
function Et({ projectId: e, documentId: t, document: i, item: n }) {
  De("msquests", xt);
  const a = i, r = Pe(e), l = He(), u = et(), [d, c] = ae(n ?? null);
  Ve(() => {
    c(n ?? null);
  }, [n]);
  const o = (g) => {
    c(g), Ke({ documentId: t, item: g });
  }, v = { projectId: e, project: r, lang: l, t: u, edit: (g, q) => {
    const m = structuredClone(a);
    g(m), Ge({ projectId: e, kind: re, id: t, doc: m, texts: q });
  } }, y = d ? a.quests.findIndex((g) => g.uid === d) : -1;
  return y >= 0 ? /* @__PURE__ */ h("div", { className: "aq", children: [
    /* @__PURE__ */ h(K, { variant: "ghost", size: "sm", className: "aq-back", onClick: () => o(null), children: [
      "← ",
      u("back")
    ] }),
    /* @__PURE__ */ s(Rt, { ctx: v, group: a, index: y })
  ] }) : /* @__PURE__ */ s("div", { className: "aq", children: /* @__PURE__ */ s(Tt, { ctx: v, group: a, onOpen: o }) });
}
function w({
  label: e,
  children: t,
  hint: i
}) {
  return (
    // biome-ignore lint/a11y/noLabelWithoutControl: the control is the child
    /* @__PURE__ */ h("label", { className: "aq-field", children: [
      /* @__PURE__ */ s("span", { className: "field-label", children: e }),
      t,
      i
    ] })
  );
}
function H({
  ctx: e,
  label: t,
  node: i,
  multiline: n
}) {
  const a = E(e.project, i.key, e.lang), r = (l) => e.edit(() => {
  }, [{ key: i.key, lang: e.lang, value: l }]);
  return /* @__PURE__ */ s(w, { label: `${t} · ${e.lang}`, children: /* @__PURE__ */ s(ve, { value: a, onChange: r, multiline: n, "aria-label": t }) });
}
function At(e) {
  return typeof e == "number" ? String(e) : typeof e == "string" ? e : "";
}
function R({
  label: e,
  value: t,
  onChange: i,
  placeholder: n
}) {
  return /* @__PURE__ */ s(w, { label: e, children: /* @__PURE__ */ s(
    "input",
    {
      className: "input",
      type: "number",
      min: 0,
      step: 1,
      value: At(t),
      placeholder: n,
      onChange: (a) => {
        const r = a.target.value.trim();
        i(r === "" ? void 0 : Math.max(0, Math.trunc(Number(r))));
      }
    }
  ) });
}
function _(e, t, i) {
  i === void 0 || i === "" ? delete e[t] : e[t] = i;
}
function ce(e, t, i) {
  if (i < 0 || i >= e.length) return;
  const [n] = e.splice(t, 1);
  e.splice(i, 0, n);
}
function O({
  label: e,
  onClick: t,
  disabled: i,
  children: n
}) {
  return /* @__PURE__ */ s(
    "button",
    {
      type: "button",
      className: "aq-icon-btn",
      title: e,
      "aria-label": e,
      onClick: t,
      disabled: i,
      children: n
    }
  );
}
function Se(e) {
  const t = /* @__PURE__ */ new Set();
  return e.map((i, n) => {
    const a = t.has(i) ? `${i}#${n}` : i;
    return t.add(i), a;
  });
}
function Tt({
  ctx: e,
  group: t,
  onOpen: i
}) {
  const { t: n, edit: a, project: r, lang: l } = e, u = Qe(), d = typeof t.reset.cron == "string" ? t.reset.cron : "", c = d ? Ye(d, u) : null, o = typeof t.reset.timezone == "string" ? t.reset.timezone : "", p = d && de(d) ? Nt(d, u) : null, v = Array.isArray(t.distribution.triggers) ? t.distribution.triggers : [], y = t.distribution.tiers ?? {}, g = Object.keys(t.rest).filter((m) => m !== "actions"), q = () => {
    const m = $(), b = V();
    a(
      (f) => {
        f.quests.push({
          key: z(
            "quest",
            f.quests.map((k) => k.key)
          ),
          uid: b,
          tier: "",
          title: { key: m },
          description: { key: $() },
          duration: "",
          stages: [Re(1)],
          rest: {}
        });
      },
      [{ key: m, lang: l, value: n("newQuestTitle") }]
    ), i(b);
  };
  return /* @__PURE__ */ h(U, { children: [
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ s("span", { className: "aq-h", children: n("group") }),
      /* @__PURE__ */ s(H, { ctx: e, label: n("title"), node: t.title }),
      /* @__PURE__ */ s(H, { ctx: e, label: n("description"), node: t.description, multiline: !0 }),
      /* @__PURE__ */ s(w, { label: n("actorType"), children: /* @__PURE__ */ h(
        "select",
        {
          className: "select",
          value: t.actorType,
          onChange: (m) => a((b) => {
            b.actorType = m.target.value;
          }),
          children: [
            /* @__PURE__ */ s("option", { value: "player", children: n("actor.player") }),
            /* @__PURE__ */ s("option", { value: "global", children: n("actor.global") }),
            !["player", "global"].includes(t.actorType) && /* @__PURE__ */ s("option", { value: t.actorType, children: t.actorType })
          ]
        }
      ) })
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ s("span", { className: "aq-h", children: n("reset") }),
      /* @__PURE__ */ s(
        w,
        {
          label: n("cron"),
          hint: d ? de(d) ? /* @__PURE__ */ h("span", { className: "aq-ok", children: [
            c ?? n("cronRaw"),
            o && o !== wt ? `, ${o}` : p ? ` · ${n("nextRun", { when: p })}` : ""
          ] }) : /* @__PURE__ */ s("span", { className: "aq-err", children: n("cronInvalid") }) : /* @__PURE__ */ s("span", { className: "aq-hint", children: n("noReset") }),
          children: /* @__PURE__ */ s(
            "input",
            {
              className: "input",
              value: d,
              placeholder: "0 4 * * *",
              onChange: (m) => a((b) => _(b.reset, "cron", m.target.value))
            }
          )
        }
      )
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ s("span", { className: "aq-h", children: n("distribution") }),
      /* @__PURE__ */ h("div", { className: "aq-row", children: [
        /* @__PURE__ */ s(w, { label: n("strategy"), children: /* @__PURE__ */ h(
          "select",
          {
            className: "select",
            value: typeof t.distribution.strategy == "string" ? t.distribution.strategy.toLowerCase() : "",
            onChange: (m) => a((b) => _(b.distribution, "strategy", m.target.value)),
            children: [
              /* @__PURE__ */ s("option", { value: "", children: "—" }),
              ["none", "sequential", "random"].map((m) => /* @__PURE__ */ s("option", { value: m, children: n(`strategy.${m}`) }, m))
            ]
          }
        ) }),
        /* @__PURE__ */ s(
          R,
          {
            label: n("amount"),
            value: t.distribution.amount,
            onChange: (m) => a((b) => _(b.distribution, "amount", m))
          }
        )
      ] }),
      /* @__PURE__ */ s("span", { className: "field-label", children: n("triggers") }),
      jt.map((m) => /* @__PURE__ */ h("label", { className: "aq-check", children: [
        /* @__PURE__ */ s(
          "input",
          {
            type: "checkbox",
            checked: v.includes(m),
            onChange: (b) => a((f) => {
              const k = Array.isArray(f.distribution.triggers) ? f.distribution.triggers : [], x = b.target.checked ? [...k, m] : k.filter((I) => I !== m);
              _(f.distribution, "triggers", x.length ? x : void 0);
            })
          }
        ),
        n(`trigger.${m}`)
      ] }, m)),
      t.tiers.length > 0 && /* @__PURE__ */ h(U, { children: [
        /* @__PURE__ */ s("span", { className: "field-label", children: n("perTier") }),
        /* @__PURE__ */ s("div", { className: "aq-row", children: t.tiers.map((m) => /* @__PURE__ */ s(
          R,
          {
            label: E(r, m.title.key, l) || m.key,
            value: y[m.key],
            onChange: (b) => a((f) => {
              const k = { ...f.distribution.tiers ?? {} };
              _(k, m.key, b), _(
                f.distribution,
                "tiers",
                Object.keys(k).length ? k : void 0
              );
            })
          },
          m.key
        )) })
      ] })
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ s("span", { className: "aq-h", children: n("limits") }),
      /* @__PURE__ */ h("div", { className: "aq-row", children: [
        /* @__PURE__ */ s(
          R,
          {
            label: n("maxActive"),
            value: t.limits.max_active,
            onChange: (m) => a((b) => _(b.limits, "max_active", m))
          }
        ),
        /* @__PURE__ */ s(
          R,
          {
            label: n("questsPerPeriod"),
            placeholder: n("noLimit"),
            value: t.limits.max_per_period,
            onChange: (m) => a((b) => _(b.limits, "max_per_period", m))
          }
        )
      ] }),
      /* @__PURE__ */ s("span", { className: "aq-h", children: n("rotation") }),
      /* @__PURE__ */ h("div", { className: "aq-row", children: [
        /* @__PURE__ */ h("label", { className: "aq-check", children: [
          /* @__PURE__ */ s(
            "input",
            {
              type: "checkbox",
              checked: t.rotation.enabled === !0,
              onChange: (m) => a((b) => _(b.rotation, "enabled", m.target.checked ? !0 : void 0))
            }
          ),
          n("rotationEnabled")
        ] }),
        t.rotation.enabled === !0 ? /* @__PURE__ */ s(
          R,
          {
            label: n("rotationsPerPeriod"),
            placeholder: n("noLimit"),
            value: t.rotation.max_per_period,
            onChange: (m) => a((b) => _(b.rotation, "max_per_period", m))
          }
        ) : null
      ] })
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ h("div", { className: "aq-head", children: [
        /* @__PURE__ */ s("span", { className: "aq-h", children: n("tiers") }),
        /* @__PURE__ */ h(
          K,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => a((m) => {
              m.tiers.push({
                key: z(
                  "tier",
                  m.tiers.map((b) => b.key)
                ),
                title: { key: $() },
                rest: {}
              });
            }),
            children: [
              "+ ",
              n("addTier")
            ]
          }
        )
      ] }),
      t.tiers.map((m, b) => /* @__PURE__ */ h("div", { className: "aq-row", children: [
        /* @__PURE__ */ s(w, { label: n("tierKey"), children: /* @__PURE__ */ s(
          "input",
          {
            className: "input",
            defaultValue: m.key,
            onBlur: (f) => {
              const k = Y(f.target.value);
              k !== m.key && a((x) => {
                const I = z(
                  k,
                  x.tiers.map((Q) => Q.key)
                );
                x.tiers[b].key = I;
                for (const Q of x.quests) Q.tier === m.key && (Q.tier = I);
                const M = x.distribution.tiers;
                M && m.key in M && (M[I] = M[m.key], delete M[m.key]);
              });
            }
          }
        ) }),
        /* @__PURE__ */ s(H, { ctx: e, label: n("title"), node: m.title }),
        /* @__PURE__ */ s(
          O,
          {
            label: n("remove"),
            onClick: () => a((f) => {
              f.tiers.splice(b, 1);
            }),
            children: "×"
          }
        )
      ] }, m.key))
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ h("div", { className: "aq-head", children: [
        /* @__PURE__ */ h("span", { className: "aq-h", children: [
          n("quests"),
          " · ",
          t.quests.length
        ] }),
        /* @__PURE__ */ h(K, { size: "sm", variant: "primary", onClick: q, children: [
          "+ ",
          n("addQuest")
        ] })
      ] }),
      t.quests.length === 0 && /* @__PURE__ */ s("span", { className: "aq-hint", children: n("noQuests") }),
      /* @__PURE__ */ s(
        ie,
        {
          ids: t.quests.map((m) => m.uid),
          onMove: (m, b) => a((f) => ce(f.quests, m, b)),
          className: "aq-list",
          itemClassName: "aq-item",
          children: (m, b) => {
            const f = t.quests[m];
            return /* @__PURE__ */ h(U, { children: [
              b,
              /* @__PURE__ */ h(
                "button",
                {
                  type: "button",
                  className: "aq-item-main",
                  onClick: () => i(f.uid),
                  title: n("open"),
                  children: [
                    /* @__PURE__ */ s("span", { className: "aq-item-title", children: E(r, f.title.key, l) || f.key }),
                    /* @__PURE__ */ h("span", { className: "aq-item-sub", children: [
                      n("questSummary", {
                        stages: f.stages.length,
                        objectives: f.stages.reduce((k, x) => k + x.objectives.length, 0)
                      }),
                      " · ",
                      f.key,
                      f.tier ? ` · ${f.tier}` : "",
                      f.duration ? ` · ${f.duration}` : ""
                    ] })
                  ]
                }
              ),
              /* @__PURE__ */ s(
                O,
                {
                  label: n("remove"),
                  onClick: () => a((k) => {
                    k.quests.splice(m, 1);
                  }),
                  children: "×"
                }
              )
            ] });
          }
        }
      ),
      g.length > 0 && /* @__PURE__ */ s("span", { className: "aq-hint", children: n("kept", { fields: g.join(", ") }) })
    ] }),
    /* @__PURE__ */ s(Ct, { ctx: e, group: t })
  ] });
}
function St({
  ctx: e,
  value: t,
  onChange: i
}) {
  const { chain: n, legacy: a } = ct(t);
  return /* @__PURE__ */ h("div", { className: "aq-chain", children: [
    /* @__PURE__ */ s(
      Ue,
      {
        projectId: e.projectId,
        value: n,
        onChange: (r) => i(me(r, a))
      }
    ),
    a.length > 0 && /* @__PURE__ */ h("div", { className: "aq-legacy", children: [
      /* @__PURE__ */ s("span", { className: "aq-hint", children: e.t("legacySteps") }),
      a.map((r, l) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: a step has no identity
        /* @__PURE__ */ h("div", { className: "aq-legacy-row", children: [
          /* @__PURE__ */ s("code", { children: dt(r) }),
          /* @__PURE__ */ s(
            O,
            {
              label: e.t("remove"),
              onClick: () => i(me(n, a.filter((u, d) => d !== l))),
              children: "×"
            }
          )
        ] }, l)
      ))
    ] })
  ] });
}
function Ce({
  ctx: e,
  title: t,
  hint: i,
  hooks: n,
  label: a,
  value: r,
  onChange: l,
  footer: u
}) {
  const { t: d } = e, c = j(r), [o, p] = ae([]), v = n.filter((g) => c[g] != null || o.includes(g)), y = n.filter((g) => !v.includes(g));
  return /* @__PURE__ */ h("section", { className: "aq-section", children: [
    /* @__PURE__ */ s("span", { className: "aq-h", children: t }),
    /* @__PURE__ */ s("span", { className: "aq-hint", children: i }),
    v.map((g) => /* @__PURE__ */ h("div", { className: "aq-event", children: [
      /* @__PURE__ */ h("div", { className: "aq-head", children: [
        /* @__PURE__ */ s("span", { className: "aq-event-name", children: a(g) }),
        /* @__PURE__ */ s(
          O,
          {
            label: d("removeEvent"),
            onClick: () => {
              p((q) => q.filter((m) => m !== g)), l(g, void 0);
            },
            children: "×"
          }
        )
      ] }),
      /* @__PURE__ */ s(St, { ctx: e, value: c[g], onChange: (q) => l(g, q) })
    ] }, g)),
    y.length > 0 && /* @__PURE__ */ h(
      "select",
      {
        className: "select aq-event-add",
        "aria-label": d("addEvent"),
        value: "",
        onChange: (g) => {
          const q = y.find((m) => m === g.target.value);
          q && p((m) => [...m, q]);
        },
        children: [
          /* @__PURE__ */ s("option", { value: "", children: d("addEvent") }),
          y.map((g) => /* @__PURE__ */ s("option", { value: g, children: a(g) }, g))
        ]
      }
    ),
    u
  ] });
}
function Ct({ ctx: e, group: t }) {
  const { t: i, edit: n } = e;
  return /* @__PURE__ */ s(
    Ce,
    {
      ctx: e,
      title: i("hooks"),
      hint: i("hooksHint"),
      hooks: ut,
      label: (a) => i(`hook.${a}`),
      value: t.rest.actions,
      onChange: (a, r) => n((l) => {
        const u = { ...j(l.rest.actions) };
        _(u, a, r), _(l.rest, "actions", Object.keys(u).length ? u : void 0);
      })
    },
    t.uid
  );
}
function Re(e) {
  return {
    key: `stage_${e}`,
    title: { key: $() },
    flow: "parallel",
    objectives: [],
    rest: {}
  };
}
function Rt({ ctx: e, group: t, index: i }) {
  const { t: n, edit: a } = e, r = t.quests[i], l = (o, p) => a((v) => o(v.quests[i]), p), u = t.quests.some((o, p) => p !== i && o.key === r.key), d = r.duration !== "" && !_t.test(r.duration.replace(/\s/g, "").toLowerCase()), c = Object.keys(r.rest).filter((o) => o !== "actions");
  return /* @__PURE__ */ h(U, { children: [
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ s("span", { className: "aq-h", children: n("general") }),
      /* @__PURE__ */ s(H, { ctx: e, label: n("title"), node: r.title }),
      /* @__PURE__ */ s(H, { ctx: e, label: n("description"), node: r.description, multiline: !0 }),
      /* @__PURE__ */ h("div", { className: "aq-row aq-top", children: [
        /* @__PURE__ */ s(w, { label: n("tier"), children: /* @__PURE__ */ h(
          "select",
          {
            className: "select",
            value: r.tier,
            onChange: (o) => l((p) => {
              p.tier = o.target.value;
            }),
            children: [
              /* @__PURE__ */ s("option", { value: "", children: n("noTier") }),
              t.tiers.map((o) => /* @__PURE__ */ s("option", { value: o.key, children: E(e.project, o.title.key, e.lang) || o.key }, o.key)),
              r.tier && !t.tiers.some((o) => o.key === r.tier) && /* @__PURE__ */ s("option", { value: r.tier, children: r.tier })
            ]
          }
        ) }),
        /* @__PURE__ */ s(
          w,
          {
            label: n("duration"),
            hint: /* @__PURE__ */ s("span", { className: d ? "aq-err" : "aq-hint", children: n(d ? "durationInvalid" : "durationHint") }),
            children: /* @__PURE__ */ s(
              "input",
              {
                className: "input",
                value: r.duration,
                placeholder: "24h",
                onChange: (o) => l((p) => {
                  p.duration = o.target.value.trim();
                })
              }
            )
          }
        )
      ] }),
      /* @__PURE__ */ h("details", { className: "aq-fold", open: u || void 0, children: [
        /* @__PURE__ */ s("summary", { children: n("more") }),
        /* @__PURE__ */ s(
          w,
          {
            label: n("key"),
            hint: /* @__PURE__ */ s("span", { className: u ? "aq-err" : "aq-hint", children: n(u ? "keyTaken" : "questKeyHint") }),
            children: /* @__PURE__ */ s(
              "input",
              {
                className: "input aq-mono",
                value: r.key,
                onChange: (o) => l((p) => {
                  p.key = xe(o.target.value);
                }),
                onBlur: () => {
                  const o = Y(r.key);
                  o !== r.key && l((p) => {
                    p.key = o;
                  });
                }
              }
            )
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ h("div", { className: "aq-head", children: [
        /* @__PURE__ */ h("span", { className: "aq-h", children: [
          n("stages"),
          " · ",
          r.stages.length
        ] }),
        /* @__PURE__ */ h(
          K,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => l((o) => {
              const p = z(
                `stage_${o.stages.length + 1}`,
                o.stages.map((v) => v.key)
              );
              o.stages.push({ ...Re(o.stages.length + 1), key: p });
            }),
            children: [
              "+ ",
              n("addStage")
            ]
          }
        )
      ] }),
      /* @__PURE__ */ s(
        ie,
        {
          ids: Se(r.stages.map((o) => o.key)),
          onMove: (o, p) => l((v) => ce(v.stages, o, p)),
          className: "aq-stages",
          children: (o, p) => /* @__PURE__ */ s(
            $t,
            {
              ctx: e,
              quest: r,
              stage: r.stages[o],
              si: o,
              handle: p,
              editQuest: l
            }
          )
        }
      )
    ] }),
    /* @__PURE__ */ s(
      Ce,
      {
        ctx: e,
        title: n("questEvents"),
        hint: n("questEventsHint"),
        hooks: pt,
        label: (o) => n(`questHook.${o}`),
        value: r.rest.actions,
        onChange: (o, p) => l((v) => {
          const y = { ...j(v.rest.actions) };
          _(y, o, p), _(v.rest, "actions", Object.keys(y).length > 0 ? y : void 0);
        }),
        footer: c.length > 0 && /* @__PURE__ */ s("span", { className: "aq-hint", children: n("kept", { fields: c.join(", ") }) })
      },
      r.uid
    ),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ s("span", { className: "aq-h", children: n("preview") }),
      /* @__PURE__ */ s(Mt, { ctx: e, quest: r })
    ] })
  ] });
}
function $t({
  ctx: e,
  quest: t,
  stage: i,
  si: n,
  handle: a,
  editQuest: r
}) {
  const { t: l } = e, [u, d] = ae(null), c = (y) => r((g) => y(g.stages[n])), o = E(e.project, i.title.key, e.lang), p = i.flow || "parallel", v = t.stages.flatMap((y) => y.objectives.map((g) => g.key));
  return /* @__PURE__ */ h("div", { className: "aq-stage", children: [
    /* @__PURE__ */ h("div", { className: "aq-stage-head", children: [
      a,
      /* @__PURE__ */ s("span", { className: "aq-stage-n", children: n + 1 }),
      /* @__PURE__ */ s("div", { className: "aq-stage-title", children: /* @__PURE__ */ s(
        ve,
        {
          value: o,
          placeholder: l("stageTitle"),
          "aria-label": l("stage", { n: n + 1 }),
          onChange: (y) => e.edit(() => {
          }, [{ key: i.title.key, lang: e.lang, value: y }])
        }
      ) }),
      /* @__PURE__ */ s(
        O,
        {
          label: l("remove"),
          onClick: () => r((y) => {
            y.stages.splice(n, 1);
          }),
          children: "×"
        }
      )
    ] }),
    i.objectives.length > 1 && /* @__PURE__ */ s("div", { className: "aq-seg", role: "radiogroup", "aria-label": l("flow"), children: ["parallel", "sequential"].map((y) => /* @__PURE__ */ s(
      "button",
      {
        type: "button",
        role: "radio",
        "aria-checked": p === y,
        className: `chip${p === y ? " is-active" : ""}`,
        onClick: () => c((g) => {
          g.flow = y;
        }),
        children: l(`flow.${y}`)
      },
      y
    )) }),
    i.objectives.length === 0 && /* @__PURE__ */ s("span", { className: "aq-hint", children: l("noObjectives") }),
    /* @__PURE__ */ s(
      ie,
      {
        ids: Se(i.objectives.map((y) => y.key)),
        onMove: (y, g) => c((q) => ce(q.objectives, y, g)),
        className: "aq-objectives",
        children: (y, g) => {
          const q = i.objectives[y];
          return /* @__PURE__ */ s(
            Ot,
            {
              t: l,
              projectId: e.projectId,
              n: p === "sequential" ? `${n + 1}.${y + 1}` : "•",
              objective: q,
              open: u === q.key,
              onToggle: () => d(u === q.key ? null : q.key),
              keyTaken: v.filter((m) => m === q.key).length > 1,
              handle: g,
              onChange: (m) => c((b) => m(b.objectives[y])),
              onRename: (m) => {
                u === q.key && d(m);
              },
              onRemove: () => c((m) => {
                m.objectives.splice(y, 1);
              })
            }
          );
        }
      }
    ),
    /* @__PURE__ */ h(
      K,
      {
        size: "sm",
        variant: "ghost",
        className: "aq-add",
        onClick: () => {
          const y = nt("block_break", v);
          r((g) => {
            g.stages[n].objectives.push({
              key: y,
              type: "block_break",
              params: we("block_break")
            });
          }), d(y);
        },
        children: [
          "+ ",
          l("addObjective")
        ]
      }
    )
  ] });
}
function Ot({
  t: e,
  projectId: t,
  n: i,
  objective: n,
  open: a,
  onToggle: r,
  keyTaken: l,
  handle: u,
  onChange: d,
  onRename: c,
  onRemove: o
}) {
  const p = L(n.type), v = p ? p.params : Object.keys(n.params).map((f) => ({ name: f, kind: "text" })), y = oe(n.params), g = Ee(n.type, n.params), q = ht(n.type, n.params), m = l || q.length > 0, b = (f) => f.kind === "item" ? /* @__PURE__ */ s(
    Lt,
    {
      t: e,
      projectId: t,
      spec: f,
      params: n.params,
      onChange: (k) => d((x) => k(x.params))
    },
    f.name
  ) : /* @__PURE__ */ s(
    It,
    {
      t: e,
      spec: f,
      value: n.params[f.name],
      onChange: (k) => d((x) => _(x.params, f.name, k))
    },
    f.name
  );
  return /* @__PURE__ */ h("div", { className: `aq-obj${a ? " is-open" : ""}${m ? " has-problem" : ""}`, children: [
    /* @__PURE__ */ h("div", { className: "aq-obj-line", children: [
      u,
      /* @__PURE__ */ h("button", { type: "button", className: "aq-obj-main", onClick: r, "aria-expanded": a, children: [
        /* @__PURE__ */ s("span", { className: "aq-obj-n", children: i }),
        /* @__PURE__ */ s("span", { className: "aq-obj-icon", "aria-hidden": !0, children: p?.icon ?? "?" }),
        /* @__PURE__ */ h("span", { className: "aq-obj-text", children: [
          Ae(e, n.type),
          y !== void 0 && /* @__PURE__ */ h("strong", { children: [
            " · ",
            String(y)
          ] }),
          g ? ` × ${g}` : y !== void 0 ? ` × ${e("any")}` : ""
        ] }),
        m && /* @__PURE__ */ s(
          "span",
          {
            className: "aq-obj-dot",
            title: l ? e("objectiveKeyTaken") : e("missing", {
              fields: q.map((f) => e(`param.${f}`)).join(", ")
            })
          }
        )
      ] }),
      /* @__PURE__ */ s(O, { label: e("remove"), onClick: o, children: "×" })
    ] }),
    a && /* @__PURE__ */ h("div", { className: "aq-obj-form", children: [
      /* @__PURE__ */ s(w, { label: e("type"), children: /* @__PURE__ */ h(
        "select",
        {
          className: "select",
          value: n.type,
          onChange: (f) => d((k) => {
            k.type = f.target.value, k.params = we(k.type);
          }),
          children: [
            je.map((f) => /* @__PURE__ */ h("option", { value: f.type, children: [
              f.icon,
              " ",
              e(`objective.${f.type}`)
            ] }, f.type)),
            !p && /* @__PURE__ */ s("option", { value: n.type, children: n.type })
          ]
        }
      ) }),
      /* @__PURE__ */ s("div", { className: "aq-row aq-wrap", children: v.filter((f) => !f.advanced).map(b) }),
      q.length > 0 && /* @__PURE__ */ s("span", { className: "aq-warn", children: e("missing", {
        fields: q.map((f) => e(`param.${f}`)).join(", ")
      }) }),
      /* @__PURE__ */ h("details", { className: "aq-fold", open: l || void 0, children: [
        /* @__PURE__ */ s("summary", { children: e("more") }),
        v.filter((f) => f.advanced).map(b),
        /* @__PURE__ */ s(
          w,
          {
            label: e("key"),
            hint: /* @__PURE__ */ s("span", { className: l ? "aq-err" : "aq-hint", children: e(l ? "objectiveKeyTaken" : "objectiveKeyHint") }),
            children: /* @__PURE__ */ s(
              "input",
              {
                className: "input aq-mono",
                value: n.key,
                onChange: (f) => {
                  const k = xe(f.target.value);
                  d((x) => {
                    x.key = k;
                  }), c(k);
                },
                onBlur: () => {
                  const f = Y(n.key);
                  f !== n.key && (d((k) => {
                    k.key = f;
                  }), c(f));
                }
              }
            )
          }
        )
      ] })
    ] })
  ] });
}
function Lt({
  t: e,
  projectId: t,
  spec: i,
  params: n,
  onChange: a
}) {
  const r = le(n, i), l = Array.isArray(n.match) ? n.match : void 0, u = (d, c) => a((o) => mt(o, i, d, c));
  return /* @__PURE__ */ s(
    w,
    {
      label: e(`param.${i.name}`),
      hint: Object.keys(r).length === 0 ? /* @__PURE__ */ s("span", { className: "aq-hint", children: e("anyItem") }) : void 0,
      children: /* @__PURE__ */ s(
        ze,
        {
          projectId: t,
          value: r,
          match: l,
          onChange: (d) => u(d, l),
          onMatchChange: (d) => u(r, d)
        }
      )
    }
  );
}
function It({
  t: e,
  spec: t,
  value: i,
  onChange: n
}) {
  const a = e(`param.${t.name}`), r = t.required && (i === void 0 || i === ""), l = typeof i == "string" || typeof i == "number" ? String(i) : "";
  if (t.kind === "int")
    return /* @__PURE__ */ s(R, { label: a, value: i, onChange: n });
  const u = r ? /* @__PURE__ */ s("span", { className: "aq-warn", children: e("required") }) : t.name === "itemKey" ? /* @__PURE__ */ s("span", { className: "aq-hint", children: e("itemKeyHint") }) : void 0;
  if (t.kind === "material")
    return /* @__PURE__ */ s(w, { label: a, hint: u, children: /* @__PURE__ */ s(
      Fe,
      {
        value: l,
        placeholder: e("any"),
        "aria-label": a,
        onChange: (c) => n(c.toUpperCase().replace(/\s+/g, "_"))
      }
    ) });
  const d = t.kind === "entity";
  return /* @__PURE__ */ h(w, { label: a, hint: u, children: [
    /* @__PURE__ */ s(
      "input",
      {
        className: `input${d ? " aq-mono" : ""}`,
        list: d ? "aq-entities" : void 0,
        value: l,
        placeholder: d ? e("any") : "",
        onChange: (c) => n(d ? c.target.value.toUpperCase().replace(/\s+/g, "_") : c.target.value)
      }
    ),
    d && /* @__PURE__ */ s("datalist", { id: "aq-entities", children: ft.map((c) => /* @__PURE__ */ s("option", { value: c }, c)) })
  ] });
}
function Mt({ ctx: e, quest: t }) {
  const { t: i, project: n, lang: a } = e, r = (l) => W(i, l);
  return /* @__PURE__ */ h("div", { className: "aq-preview", children: [
    /* @__PURE__ */ s("span", { className: "aq-preview-title", children: E(n, t.title.key, a) || t.key }),
    E(n, t.description.key, a) && /* @__PURE__ */ s("span", { className: "aq-preview-line", children: E(n, t.description.key, a) }),
    t.stages.map((l, u) => (
      // biome-ignore lint/suspicious/noArrayIndexKey: stages are listed in order
      /* @__PURE__ */ h("span", { className: "aq-preview-stage", children: [
        t.stages.length > 1 && /* @__PURE__ */ s("span", { className: "aq-preview-line", children: E(n, l.title.key, a) || i("stage", { n: u + 1 }) }),
        l.objectives.map((d) => /* @__PURE__ */ h("span", { className: "aq-preview-line", children: [
          "• ",
          r(d)
        ] }, d.key))
      ] }, u)
    ))
  ] });
}
function Dt({ className: e }) {
  return /* @__PURE__ */ h(
    "svg",
    {
      xmlns: "http://www.w3.org/2000/svg",
      viewBox: "0 0 24 24",
      fill: "none",
      stroke: "currentColor",
      strokeWidth: 2,
      strokeLinecap: "round",
      strokeLinejoin: "round",
      className: e,
      "aria-hidden": !0,
      children: [
        /* @__PURE__ */ s("path", { d: "M15 12h-5" }),
        /* @__PURE__ */ s("path", { d: "M15 8h-5" }),
        /* @__PURE__ */ s("path", { d: "M19 17V5a2 2 0 0 0-2-2H4" }),
        /* @__PURE__ */ s("path", { d: "M8 21h12a2 2 0 0 0 2-2v-1a1 1 0 0 0-1-1H11a1 1 0 0 0-1 1v1a2 2 0 1 1-4 0V5a2 2 0 1 0-4 0v2a1 1 0 0 0 1 1h3" })
      ]
    }
  );
}
const Pt = {
  kind: re,
  dir: ke,
  schemaVersion: se,
  // Created in the module's folder only (SDK 1.8); a group already elsewhere still loads.
  projects: [tt],
  // Data with no links: one "Quests" entry and page instead of a node per group (SDK 1.9).
  collection: { labels: Je },
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: ue.en,
  labels: ue,
  icon: Dt,
  component: Et,
  title: (e, { project: t, lang: i }) => E(t, e.title.key, i),
  create: ({ id: e, projectId: t }) => ({
    id: e,
    projectId: t,
    uid: V(),
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
  parse: (e, t) => it(e, t),
  serialize: (e, t) => ot(e, t),
  ltextSites: (e) => lt(e),
  board: {
    size: { width: 300, height: 48 },
    accent: yt,
    // The group's quests, one selectable row each: the Properties panel opens the one clicked.
    items: (e, { project: t, lang: i }) => bt(e, t, i),
    summary: (e, { lang: t }) => kt(e, t)
  }
}, he = "msquests:groups", fe = "msquests:quests", Ht = "msquests:active", ye = {
  en: {
    block_break: "Mine %amount% %material%",
    breed_animal: "Breed %amount% %entity_type%",
    craft_item: "Craft %amount% %material%",
    harvest_crop: "Harvest %amount% %crop%",
    deliver_item: "Deliver %amount% %material%",
    kill_entity: "Kill %amount% %entity_type%",
    execute_command: "Run the command %command%",
    fishing: "Catch %amount% %fish_type%",
    placeholder: "Reach %value%",
    travel: "Travel %distance% blocks"
  },
  fr: {
    block_break: "Miner %amount% × %material%",
    breed_animal: "Faire se reproduire %amount% × %entity_type%",
    craft_item: "Fabriquer %amount% × %material%",
    harvest_crop: "Récolter %amount% × %crop%",
    deliver_item: "Livrer %amount% × %material%",
    kill_entity: "Tuer %amount% × %entity_type%",
    execute_command: "Exécuter la commande %command%",
    fishing: "Pêcher %amount% × %fish_type%",
    placeholder: "Atteindre %value%",
    travel: "Parcourir %distance% blocs"
  }
}, ge = { en: "Available", fr: "Disponible" };
function Kt(e, t, i) {
  if (t !== he && t !== fe && t !== Ht) return null;
  const n = e.documents?.[re];
  if (!n || n.length === 0) return null;
  const a = e.defaultLang ?? "en", r = i?.serverRows, l = i?.params?.group || void 0, u = [...n].sort((c, o) => c.id < o.id ? -1 : c.id > o.id ? 1 : 0);
  if (t === he) {
    const c = te(r, "key");
    return u.map((o) => Gt(o, c.get(o.id), a));
  }
  if (t === fe) {
    const c = te(r, "id");
    return u.filter((o) => !l || o.id === l).flatMap((o) => o.quests.map((p) => $e(o, p, c.get(`${o.id}:${p.key}`), a)));
  }
  if (!r) return null;
  const d = new Map(n.map((c) => [c.id, c]));
  return r.flatMap((c) => {
    const o = d.get(A(c.group));
    if (!o) return [c];
    const p = o.quests.find((v) => v.key === A(c.quest_key));
    return p ? [Ft(p, c, a)] : [];
  });
}
function Gt(e, t, i) {
  const n = te(B(t?.quests), "key");
  return {
    key: e.id,
    name: e.title,
    description: e.description,
    actor_type: e.actorType,
    max_active: ne(e.limits.max_active, 1),
    max_per_period: ne(e.limits.max_per_period, 0),
    rotatable: e.rotation.enabled === !0,
    // The period follows the server's clock and zone: its value stands until a deploy.
    period_end: t?.period_end ?? 0,
    active_count: t?.active_count ?? 0,
    completed_count: t?.completed_count ?? 0,
    can_rotate: t?.can_rotate ?? !1,
    quests: e.quests.map((a) => $e(e, a, n.get(a.key), i))
  };
}
function $e(e, t, i, n) {
  const a = e.tiers.find((r) => r.key === t.tier);
  return {
    id: `${e.id}:${t.key}`,
    group: e.id,
    key: t.key,
    name: t.title,
    description: t.description,
    duration: Zt(t.duration),
    tier: t.tier,
    tier_name: a ? a.title : "",
    assigned: i?.assigned ?? !1,
    status_key: i?.status_key ?? "NOT_STARTED",
    status: i?.status ?? ge[n.slice(0, 2)] ?? ge.en,
    can_start: i?.can_start ?? !1,
    start_blocker: i?.start_blocker ?? "",
    start_blocker_label: i?.start_blocker_label ?? "",
    // `reference` in `QuestRows.questRow`: the attempt the server holds for this player.
    ...Oe(t, i?.assigned === !0 ? i : void 0, n)
  };
}
function Oe(e, t, i) {
  const n = t ? Qt(t) : void 0, a = Ut(e, n, i), r = a.find((u) => u.active === !0), l = r ? r.objectives.find((u) => u.done !== !0) : void 0;
  return {
    objective_name: l?.name ?? "",
    objective_progress: l?.progress ?? 0,
    objective_target: l?.target ?? 0,
    progress_ratio: n ? Le(a.map((u) => u.progress_ratio)) : 0,
    stage_index: a.filter((u) => u.completed === !0).length,
    stage_name: r?.name ?? "",
    stage_flow: r?.flow ?? "",
    stages: a.map((u) => ({ ...u, objectives: u.objectives.map(({ done: d, ...c }) => c) }))
  };
}
function Qt(e) {
  const t = /* @__PURE__ */ new Map(), i = [
    ...B(e.objectives),
    ...B(e.stages).flatMap((n) => B(n.objectives))
  ];
  for (const n of i) {
    const a = A(n.status_key);
    t.set(A(n.key), {
      progress: ne(n.progress, 0),
      flag: a === "COMPLETED" || a === "FAILED" ? a : "",
      row: n
    });
  }
  return t;
}
function Ut(e, t, i) {
  const n = e.stages.map((r, l) => {
    const u = r.objectives.map((d) => zt(d, t?.get(d.key), i));
    return {
      key: r.key,
      name: r.title,
      flow: Wt(r),
      index: l,
      // `QuestStage.isCompleted`: all objectives done, so an empty stage is.
      completed: t !== void 0 && u.every((d) => d.done),
      active: !1,
      progress_ratio: t ? Le(u.map((d) => d.progress_ratio)) : 0,
      objectives: u
    };
  });
  if (!t) return n;
  const a = n.find((r) => r.completed !== !0);
  if (a) {
    a.active = !0;
    const r = a.objectives, l = r.find((u) => u.done !== !0);
    for (const u of r)
      u.status_key === "PENDING" && (a.flow !== "SEQUENTIAL" || u === l) && (u.status_key = "IN_PROGRESS");
  }
  return n;
}
function zt(e, t, i) {
  const n = Vt(e), a = t?.progress ?? 0, r = t?.row, l = r && r.type === e.type && r.target === n;
  return {
    key: e.key,
    name: l && typeof r.name == "string" ? r.name : Bt(e, i),
    type: e.type,
    progress: a,
    target: n,
    // `AbstractQuestObjective`: done once the progress reaches the target, whatever the
    // stored flag says; the ratio is clamped, so a lowered target reads 1, not 1.4.
    progress_ratio: t ? n <= 0 ? 1 : Math.min(Math.max(a / n, 0), 1) : 0,
    completed: t !== void 0 && a >= n,
    status_key: t?.flag || "PENDING",
    done: t !== void 0 && a >= n
  };
}
function Ft(e, t, i) {
  const n = Oe(e, t, i), a = n.stages;
  return {
    ...t,
    name: e.title,
    description: e.description,
    stage_index: n.stage_index,
    stage_count: e.stages.length,
    stage_name: n.stage_name,
    stage_flow: n.stage_flow,
    progress_ratio: n.progress_ratio,
    objectives: a.flatMap((r) => r.objectives),
    stages: a
  };
}
function Bt(e, t) {
  const n = (ye[t.slice(0, 2)] ?? ye.en)[e.type];
  if (!n) return e.type;
  const a = e.params, r = {
    amount: String(a.amount ?? ""),
    distance: String(a.distance ?? ""),
    command: A(a.command),
    value: A(a.value),
    // Bukkit's translation keys, as the objective configs write them (`<lang:…>`).
    crop: F("block", a.crop),
    entity_type: F("entity", a.entity_type),
    material: e.type === "block_break" ? F("block", a.material) : be(e),
    fish_type: be(e)
  };
  return n.replace(/%([a-z_]+)%/g, (l, u) => r[u] ?? "");
}
function F(e, t) {
  const i = A(t).toLowerCase().replace(/^minecraft:/, "");
  return i === "" ? "" : `<lang:${e}.minecraft.${i}>`;
}
function be(e) {
  const t = L(e.type)?.params.find((r) => r.kind === "item");
  if (!t) return "";
  const i = le(e.params, t), n = i.title;
  if (typeof n == "string" && n.trim() !== "") return n;
  const a = A(i.material);
  return /^(minecraft:)?[a-z_0-9]+$/i.test(a) && !a.includes(":") ? F("item", a) : a || Ne(i);
}
function Vt(e) {
  const t = oe(e.params);
  if (t === void 0) return 1;
  const i = Number(t);
  return Number.isFinite(i) ? i : 1;
}
function Wt(e) {
  return e.flow === "" ? "PARALLEL" : e.flow.toUpperCase();
}
function Zt(e) {
  const t = e.trim().toLowerCase().replace(/ /g, "");
  if (t === "") return 0;
  if (/^\d+$/.test(t)) return Number(t);
  const i = /^(?:(\d+)d)?(?:(\d+)h)?(?:(\d+)m)?(?:(\d+)s)?$/.exec(t);
  if (!i) return 0;
  const [, n, a, r, l] = i;
  return Number(n ?? 0) * 86400 + Number(a ?? 0) * 3600 + Number(r ?? 0) * 60 + Number(l ?? 0);
}
function te(e, t) {
  return new Map((e ?? []).map((i) => [A(i[t]), i]));
}
function B(e) {
  return Array.isArray(e) ? e.filter((t) => t && typeof t == "object") : [];
}
function Le(e) {
  return e.length === 0 ? 0 : e.reduce((t, i) => t + i, 0) / e.length;
}
function ne(e, t) {
  return typeof e == "number" && Number.isFinite(e) ? e : t;
}
function A(e) {
  return typeof e == "string" ? e : "";
}
const Yt = {
  id: "msquests",
  displayName: "Quests",
  schemaVersion: "quest_group/v1",
  sdk: Be,
  sidebar: { icon: () => null, label: "Quests", to: "/m/msquests" },
  // Quests are Documents: they are edited from the project, no screen of their own.
  routes: [],
  documents: [Pt],
  // The quest sources previewed from the draft, joined to the player's state on the server.
  localPreviewRows: Kt
}, tn = { ...Yt, sdk: "1.10.0" };
export {
  tn as default
};
