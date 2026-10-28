import { createModuleI18n as Ie, fieldText as Me, newTextKey as $, textValue as A, injectModuleStyles as De, useProject as He, useContentLang as Pe, Button as K, selectDocumentItem as Ke, SortableList as ne, updateDocument as Ge, useUiLang as Qe, MiniMessageInput as ve, ActionChainEditor as Ue, ItemInput as ze, MaterialInput as Be, SDK_VERSION as Fe } from "@artisan/editor-sdk";
import { jsxs as h, jsx as s, Fragment as z } from "react/jsx-runtime";
import { useState as ie, useEffect as Ve } from "react";
const We = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"]
}, Ze = /^\d+$/;
function J(e, t, i) {
  const n = String(i).padStart(2, "0");
  return e === "fr" ? i === 0 ? `${t} h` : `${t} h ${n}` : `${String(t).padStart(2, "0")}:${n}`;
}
function E(e, t, i) {
  return Ze.test(e) && Number(e) >= t && Number(e) <= i;
}
function ce(e) {
  return e.trim().split(/\s+/).length === 5;
}
function Ye(e, t) {
  const i = t === "fr", n = e.trim().split(/\s+/);
  if (n.length !== 5) return null;
  const [a, r, o, m, d] = n;
  if (m !== "*") return null;
  const c = /^\*\/(\d+)$/;
  if (o === "*" && d === "*") {
    if (E(a, 0, 59) && E(r, 0, 23))
      return i ? `tous les jours à ${J(t, +r, +a)}` : `every day at ${J(t, +r, +a)}`;
    const u = c.exec(r);
    if (E(a, 0, 59) && u)
      return i ? `toutes les ${u[1]} h, à la minute ${a}` : `every ${u[1]} hours, at minute ${a}`;
    if (E(a, 0, 59) && r === "*")
      return i ? `toutes les heures, à la minute ${a}` : `every hour, at minute ${a}`;
    const v = c.exec(a);
    return v && r === "*" ? i ? `toutes les ${v[1]} minutes` : `every ${v[1]} minutes` : null;
  }
  if (!E(a, 0, 59) || !E(r, 0, 23)) return null;
  const l = J(t, +r, +a);
  if (o === "*" && E(d, 0, 7)) {
    const u = We[i ? "fr" : "en"][Number(d) % 7];
    return i ? `chaque ${u} à ${l}` : `every ${u} at ${l}`;
  }
  return d === "*" && E(o, 1, 31) ? i ? `le ${o === "1" ? "1er" : o} de chaque mois à ${l}` : `on day ${o} of each month at ${l}` : null;
}
function D(e, t, i) {
  const n = /* @__PURE__ */ new Set();
  for (const a of e.split(",")) {
    const r = /^(\*|(\d+)(?:-(\d+))?)(?:\/(\d+))?$/.exec(a);
    if (!r) return null;
    const o = r[4] ? Number(r[4]) : 1, m = r[1] === "*" ? t : Number(r[2]), d = r[1] === "*" ? i : r[3] ? Number(r[3]) : r[4] ? i : m;
    if (o < 1 || m < t || d > i || m > d) return null;
    for (let c = m; c <= d; c += o) n.add(c);
  }
  return n;
}
function Xe(e, t) {
  const i = e.trim().split(/\s+/);
  if (i.length !== 5) return null;
  const n = D(i[0], 0, 59), a = D(i[1], 0, 23), r = D(i[2], 1, 31), o = D(i[3], 1, 12), m = D(i[4], 0, 7);
  if (!n || !a || !r || !o || !m) return null;
  m.has(7) && m.add(0);
  const d = i[2] === "*", c = i[4] === "*", l = (f) => d && c ? !0 : d ? m.has(f.getDay()) : c ? r.has(f.getDate()) : r.has(f.getDate()) || m.has(f.getDay()), u = new Date(t.getTime());
  u.setSeconds(0, 0), u.setMinutes(u.getMinutes() + 1);
  const v = t.getTime() + 366 * 24 * 3600 * 1e3;
  for (; u.getTime() <= v; ) {
    if (!o.has(u.getMonth() + 1) || !l(u)) {
      u.setDate(u.getDate() + 1), u.setHours(0, 0, 0, 0);
      continue;
    }
    if (!a.has(u.getHours())) {
      u.setHours(u.getHours() + 1, 0, 0, 0);
      continue;
    }
    if (n.has(u.getMinutes())) return u;
    u.setMinutes(u.getMinutes() + 1);
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
}, de = { en: G.kindLabel, fr: Z.kindLabel }, Je = { en: G.collection, fr: Z.collection }, et = Ie({ en: G, fr: Z });
function qe(e) {
  const t = e === "fr" ? Z : G;
  return (i, n) => {
    const a = t[i] ?? G[i] ?? i;
    return n ? a.replace(/\{\{(\w+)\}\}/g, (r, o) => String(n[o] ?? r)) : a;
  };
}
const ae = "quest_group", ke = "quest_groups", tt = "quests", re = "quest_group/v1", te = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
function V(e = Date.now()) {
  let t = "", i = e;
  for (let r = 0; r < 10; r++)
    t = te[i % 32] + t, i = Math.floor(i / 32);
  const n = new Uint8Array(16);
  crypto.getRandomValues(n);
  let a = "";
  for (let r = 0; r < 16; r++) a += te[n[r] % 32];
  return t + a;
}
function B(e, t) {
  const i = new Set(t);
  if (!i.has(e)) return e;
  for (let n = 2; ; n++) if (!i.has(`${e}_${n}`)) return `${e}_${n}`;
}
function nt(e, t) {
  const i = new Set(t), n = new Uint8Array(4);
  for (; ; ) {
    crypto.getRandomValues(n);
    const a = Array.from(n, (o) => te[o % 32].toLowerCase()).join(""), r = `${Y(e)}_${a}`;
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
function ee(e) {
  return Array.isArray(e) ? e : [];
}
function N(e) {
  return typeof e == "string" || typeof e == "number" || typeof e == "boolean" ? String(e) : "";
}
function H(e, ...t) {
  const i = { ...e };
  for (const n of t) delete i[n];
  return i;
}
function it(e, t) {
  const i = {}, n = [];
  function a(d) {
    if (d && typeof d == "object" && !Array.isArray(d)) {
      const u = $(), v = {};
      for (const [f, g] of Object.entries(d)) v[f] = N(g);
      return i[u] = v, { key: u };
    }
    const c = typeof d == "string" ? /^\{i18n:(.+)\}$/.exec(d) : null;
    if (c) {
      const u = t.translations?.[c[1]];
      return u && (i[c[1]] = { ...u }), { key: c[1] };
    }
    const l = $();
    return i[l] = d == null || d === "" ? {} : { [t.lang]: N(d) }, { key: l };
  }
  const r = Object.entries(j(e.tiers)).map(([d, c]) => {
    const l = j(c);
    return { key: d, title: a(l.title), rest: H(l, "title") };
  }), o = ee(e.quests).map((d, c) => {
    const l = j(d), u = ee(l.stages).map((v, f) => {
      const g = j(v), q = N(g.flow).toLowerCase();
      return {
        key: N(g.key) || `stage_${f + 1}`,
        title: a(g.title),
        flow: q === "parallel" || q === "sequential" ? q : "",
        objectives: ee(g.objectives).map((p, b) => {
          const y = j(p);
          return {
            key: N(y.key) || `objective_${b + 1}`,
            type: N(y.type),
            params: H(y, "key", "type")
          };
        }),
        rest: H(g, "key", "title", "flow", "objectives")
      };
    });
    return {
      key: N(l.key) || `quest_${c + 1}`,
      uid: N(l.uid) || V(),
      tier: N(l.tier),
      title: a(l.title),
      description: a(l.description),
      duration: N(l.duration),
      stages: u,
      rest: at(l)
    };
  }), m = {
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
    quests: o,
    rest: _e(H(
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
  return e.schema !== void 0 && e.schema !== re && n.push(`schema: ${String(e.schema)}`), { doc: m, i18nKeys: i, unsupported: n };
}
function _e(e) {
  return e.on !== void 0 && (e.actions == null && (e.actions = e.on), delete e.on), e;
}
function at(e) {
  const t = _e(
    H(e, "key", "uid", "tier", "title", "description", "duration", "stages", "reward_display")
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
function T(e) {
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
    const l = i(c);
    return rt(l) ? void 0 : l;
  }, a = {};
  for (const c of e.tiers) a[c.key] = { ...c.rest, title: i(c.title) };
  const r = e.quests.map(
    (c) => T({
      key: c.key,
      uid: c.uid,
      tier: c.tier,
      title: i(c.title),
      description: n(c.description),
      duration: c.duration,
      ...c.rest,
      stages: c.stages.map(
        (l) => T({
          key: l.key,
          title: n(l.title),
          flow: l.flow,
          ...l.rest,
          objectives: l.objectives.map((u) => ({ key: u.key, type: u.type, ...u.params }))
        })
      )
    })
  ), o = {
    ...e.rest,
    id: e.id,
    schema: re,
    uid: e.uid,
    title: i(e.title),
    description: n(e.description),
    actor_type: e.actorType,
    reset: T(e.reset),
    limits: T(e.limits),
    rotation: T(e.rotation),
    distribution: T(e.distribution),
    tiers: a,
    quests: r
  }, m = {};
  for (const c of st) c in o && (m[c] = o[c]);
  for (const c of Object.keys(o)) c in m || (m[c] = o[c]);
  const d = T(m);
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
    const o = `${t}.quests.${r}`;
    yield {
      lText: a.title,
      key: `${o}.title`,
      path: `quests[${n}].title`,
      kind: "quest.title"
    }, yield {
      lText: a.description,
      key: `${o}.description`,
      path: `quests[${n}].description`,
      kind: "quest.description"
    };
    const m = /* @__PURE__ */ new Set();
    for (let d = 0; d < a.stages.length; d++) {
      const c = a.stages[d], l = m.has(c.key) ? `${c.key}_${d}` : c.key;
      m.add(c.key), yield {
        lText: c.title,
        key: `${o}.stages.${l}.title`,
        path: `quests[${n}].stages[${d}].title`,
        kind: "quest.stage"
      };
    }
  }
}
function ue(e) {
  return !!e && typeof e == "object" && !Array.isArray(e) && "type" in e;
}
function ct(e) {
  const t = Array.isArray(e) ? e : e == null ? [] : [e];
  return {
    chain: t.filter((i) => !ue(i)),
    legacy: t.filter(ue)
  };
}
function pe(e, t) {
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
function se(e) {
  return e.amount ?? e.distance;
}
function oe(e, t) {
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
function Ae(e, t) {
  const i = L(e);
  return i ? i.params.filter((n) => n.kind !== "int").map((n) => n.kind === "item" ? Ne(oe(t, n)) : t[n.name]).filter((n) => typeof n == "string" && n !== "").join(" = ") : Object.keys(t).map((n) => t[n]).filter((n) => typeof n == "string" && n !== "").join(" = ");
}
function ht(e, t) {
  return (L(e)?.params ?? []).filter((i) => i.required && (t[i.name] === void 0 || t[i.name] === "")).map((i) => i.name);
}
const yt = [
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
], ft = "#f59e0b";
function Ee(e, t) {
  return L(t) ? e(`objective.${t}`) : t;
}
function W(e, t) {
  const i = Ae(t.type, t.params), n = se(t.params);
  return `${Ee(e, t.type)}${i ? ` · ${i}` : ""}${n !== void 0 ? ` · 0/${String(n)}` : ""}`;
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
  return e.quests.map((r, o) => {
    const m = A(t, r.title.key, i);
    return {
      id: r.uid,
      label: Te(m) || r.key,
      detail: r.tier || void 0,
      caption: vt(n, r),
      marker: a ? String(o + 1) : void 0,
      hover: {
        title: m || r.key,
        text: A(t, r.description.key, i) || void 0,
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
    (r, o) => r.objectives.map((m) => {
      const d = W(e, m);
      return a ? `<gray>${Te(A(i, r.title.key, n)) || e("stage", { n: o + 1 })} ·</gray> ${d}` : d;
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
function At({ projectId: e, documentId: t, document: i, item: n }) {
  De("msquests", xt);
  const a = i, r = He(e), o = Pe(), m = et(), [d, c] = ie(n ?? null);
  Ve(() => {
    c(n ?? null);
  }, [n]);
  const l = (g) => {
    c(g), Ke({ documentId: t, item: g });
  }, v = { projectId: e, project: r, lang: o, t: m, edit: (g, q) => {
    const p = structuredClone(a);
    g(p), Ge({ projectId: e, kind: ae, id: t, doc: p, texts: q });
  } }, f = d ? a.quests.findIndex((g) => g.uid === d) : -1;
  return f >= 0 ? /* @__PURE__ */ h("div", { className: "aq", children: [
    /* @__PURE__ */ h(K, { variant: "ghost", size: "sm", className: "aq-back", onClick: () => l(null), children: [
      "← ",
      m("back")
    ] }),
    /* @__PURE__ */ s(Rt, { ctx: v, group: a, index: f })
  ] }) : /* @__PURE__ */ s("div", { className: "aq", children: /* @__PURE__ */ s(Tt, { ctx: v, group: a, onOpen: l }) });
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
function P({
  ctx: e,
  label: t,
  node: i,
  multiline: n
}) {
  const a = A(e.project, i.key, e.lang), r = (o) => e.edit(() => {
  }, [{ key: i.key, lang: e.lang, value: o }]);
  return /* @__PURE__ */ s(w, { label: `${t} · ${e.lang}`, children: /* @__PURE__ */ s(ve, { value: a, onChange: r, multiline: n, "aria-label": t }) });
}
function Et(e) {
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
      value: Et(t),
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
function le(e, t, i) {
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
function Ce(e) {
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
  const { t: n, edit: a, project: r, lang: o } = e, m = Qe(), d = typeof t.reset.cron == "string" ? t.reset.cron : "", c = d ? Ye(d, m) : null, l = typeof t.reset.timezone == "string" ? t.reset.timezone : "", u = d && ce(d) ? Nt(d, m) : null, v = Array.isArray(t.distribution.triggers) ? t.distribution.triggers : [], f = t.distribution.tiers ?? {}, g = Object.keys(t.rest).filter((p) => p !== "actions"), q = () => {
    const p = $(), b = V();
    a(
      (y) => {
        y.quests.push({
          key: B(
            "quest",
            y.quests.map((k) => k.key)
          ),
          uid: b,
          tier: "",
          title: { key: p },
          description: { key: $() },
          duration: "",
          stages: [Re(1)],
          rest: {}
        });
      },
      [{ key: p, lang: o, value: n("newQuestTitle") }]
    ), i(b);
  };
  return /* @__PURE__ */ h(z, { children: [
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ s("span", { className: "aq-h", children: n("group") }),
      /* @__PURE__ */ s(P, { ctx: e, label: n("title"), node: t.title }),
      /* @__PURE__ */ s(P, { ctx: e, label: n("description"), node: t.description, multiline: !0 }),
      /* @__PURE__ */ s(w, { label: n("actorType"), children: /* @__PURE__ */ h(
        "select",
        {
          className: "select",
          value: t.actorType,
          onChange: (p) => a((b) => {
            b.actorType = p.target.value;
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
          hint: d ? ce(d) ? /* @__PURE__ */ h("span", { className: "aq-ok", children: [
            c ?? n("cronRaw"),
            l && l !== wt ? `, ${l}` : u ? ` · ${n("nextRun", { when: u })}` : ""
          ] }) : /* @__PURE__ */ s("span", { className: "aq-err", children: n("cronInvalid") }) : /* @__PURE__ */ s("span", { className: "aq-hint", children: n("noReset") }),
          children: /* @__PURE__ */ s(
            "input",
            {
              className: "input",
              value: d,
              placeholder: "0 4 * * *",
              onChange: (p) => a((b) => _(b.reset, "cron", p.target.value))
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
            onChange: (p) => a((b) => _(b.distribution, "strategy", p.target.value)),
            children: [
              /* @__PURE__ */ s("option", { value: "", children: "—" }),
              ["none", "sequential", "random"].map((p) => /* @__PURE__ */ s("option", { value: p, children: n(`strategy.${p}`) }, p))
            ]
          }
        ) }),
        /* @__PURE__ */ s(
          R,
          {
            label: n("amount"),
            value: t.distribution.amount,
            onChange: (p) => a((b) => _(b.distribution, "amount", p))
          }
        )
      ] }),
      /* @__PURE__ */ s("span", { className: "field-label", children: n("triggers") }),
      jt.map((p) => /* @__PURE__ */ h("label", { className: "aq-check", children: [
        /* @__PURE__ */ s(
          "input",
          {
            type: "checkbox",
            checked: v.includes(p),
            onChange: (b) => a((y) => {
              const k = Array.isArray(y.distribution.triggers) ? y.distribution.triggers : [], x = b.target.checked ? [...k, p] : k.filter((I) => I !== p);
              _(y.distribution, "triggers", x.length ? x : void 0);
            })
          }
        ),
        n(`trigger.${p}`)
      ] }, p)),
      t.tiers.length > 0 && /* @__PURE__ */ h(z, { children: [
        /* @__PURE__ */ s("span", { className: "field-label", children: n("perTier") }),
        /* @__PURE__ */ s("div", { className: "aq-row", children: t.tiers.map((p) => /* @__PURE__ */ s(
          R,
          {
            label: A(r, p.title.key, o) || p.key,
            value: f[p.key],
            onChange: (b) => a((y) => {
              const k = { ...y.distribution.tiers ?? {} };
              _(k, p.key, b), _(
                y.distribution,
                "tiers",
                Object.keys(k).length ? k : void 0
              );
            })
          },
          p.key
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
            onChange: (p) => a((b) => _(b.limits, "max_active", p))
          }
        ),
        /* @__PURE__ */ s(
          R,
          {
            label: n("questsPerPeriod"),
            placeholder: n("noLimit"),
            value: t.limits.max_per_period,
            onChange: (p) => a((b) => _(b.limits, "max_per_period", p))
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
              onChange: (p) => a((b) => _(b.rotation, "enabled", p.target.checked ? !0 : void 0))
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
            onChange: (p) => a((b) => _(b.rotation, "max_per_period", p))
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
            onClick: () => a((p) => {
              p.tiers.push({
                key: B(
                  "tier",
                  p.tiers.map((b) => b.key)
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
      t.tiers.map((p, b) => /* @__PURE__ */ h("div", { className: "aq-row", children: [
        /* @__PURE__ */ s(w, { label: n("tierKey"), children: /* @__PURE__ */ s(
          "input",
          {
            className: "input",
            defaultValue: p.key,
            onBlur: (y) => {
              const k = Y(y.target.value);
              k !== p.key && a((x) => {
                const I = B(
                  k,
                  x.tiers.map((U) => U.key)
                );
                x.tiers[b].key = I;
                for (const U of x.quests) U.tier === p.key && (U.tier = I);
                const M = x.distribution.tiers;
                M && p.key in M && (M[I] = M[p.key], delete M[p.key]);
              });
            }
          }
        ) }),
        /* @__PURE__ */ s(P, { ctx: e, label: n("title"), node: p.title }),
        /* @__PURE__ */ s(
          O,
          {
            label: n("remove"),
            onClick: () => a((y) => {
              y.tiers.splice(b, 1);
            }),
            children: "×"
          }
        )
      ] }, p.key))
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
        ne,
        {
          ids: t.quests.map((p) => p.uid),
          onMove: (p, b) => a((y) => le(y.quests, p, b)),
          className: "aq-list",
          itemClassName: "aq-item",
          children: (p, b) => {
            const y = t.quests[p];
            return /* @__PURE__ */ h(z, { children: [
              b,
              /* @__PURE__ */ h(
                "button",
                {
                  type: "button",
                  className: "aq-item-main",
                  onClick: () => i(y.uid),
                  title: n("open"),
                  children: [
                    /* @__PURE__ */ s("span", { className: "aq-item-title", children: A(r, y.title.key, o) || y.key }),
                    /* @__PURE__ */ h("span", { className: "aq-item-sub", children: [
                      n("questSummary", {
                        stages: y.stages.length,
                        objectives: y.stages.reduce((k, x) => k + x.objectives.length, 0)
                      }),
                      " · ",
                      y.key,
                      y.tier ? ` · ${y.tier}` : "",
                      y.duration ? ` · ${y.duration}` : ""
                    ] })
                  ]
                }
              ),
              /* @__PURE__ */ s(
                O,
                {
                  label: n("remove"),
                  onClick: () => a((k) => {
                    k.quests.splice(p, 1);
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
    /* @__PURE__ */ s(St, { ctx: e, group: t })
  ] });
}
function Ct({
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
        onChange: (r) => i(pe(r, a))
      }
    ),
    a.length > 0 && /* @__PURE__ */ h("div", { className: "aq-legacy", children: [
      /* @__PURE__ */ s("span", { className: "aq-hint", children: e.t("legacySteps") }),
      a.map((r, o) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: a step has no identity
        /* @__PURE__ */ h("div", { className: "aq-legacy-row", children: [
          /* @__PURE__ */ s("code", { children: dt(r) }),
          /* @__PURE__ */ s(
            O,
            {
              label: e.t("remove"),
              onClick: () => i(pe(n, a.filter((m, d) => d !== o))),
              children: "×"
            }
          )
        ] }, o)
      ))
    ] })
  ] });
}
function Se({
  ctx: e,
  title: t,
  hint: i,
  hooks: n,
  label: a,
  value: r,
  onChange: o,
  footer: m
}) {
  const { t: d } = e, c = j(r), [l, u] = ie([]), v = n.filter((g) => c[g] != null || l.includes(g)), f = n.filter((g) => !v.includes(g));
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
              u((q) => q.filter((p) => p !== g)), o(g, void 0);
            },
            children: "×"
          }
        )
      ] }),
      /* @__PURE__ */ s(Ct, { ctx: e, value: c[g], onChange: (q) => o(g, q) })
    ] }, g)),
    f.length > 0 && /* @__PURE__ */ h(
      "select",
      {
        className: "select aq-event-add",
        "aria-label": d("addEvent"),
        value: "",
        onChange: (g) => {
          const q = f.find((p) => p === g.target.value);
          q && u((p) => [...p, q]);
        },
        children: [
          /* @__PURE__ */ s("option", { value: "", children: d("addEvent") }),
          f.map((g) => /* @__PURE__ */ s("option", { value: g, children: a(g) }, g))
        ]
      }
    ),
    m
  ] });
}
function St({ ctx: e, group: t }) {
  const { t: i, edit: n } = e;
  return /* @__PURE__ */ s(
    Se,
    {
      ctx: e,
      title: i("hooks"),
      hint: i("hooksHint"),
      hooks: ut,
      label: (a) => i(`hook.${a}`),
      value: t.rest.actions,
      onChange: (a, r) => n((o) => {
        const m = { ...j(o.rest.actions) };
        _(m, a, r), _(o.rest, "actions", Object.keys(m).length ? m : void 0);
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
  const { t: n, edit: a } = e, r = t.quests[i], o = (l, u) => a((v) => l(v.quests[i]), u), m = t.quests.some((l, u) => u !== i && l.key === r.key), d = r.duration !== "" && !_t.test(r.duration.replace(/\s/g, "").toLowerCase()), c = Object.keys(r.rest).filter((l) => l !== "actions");
  return /* @__PURE__ */ h(z, { children: [
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ s("span", { className: "aq-h", children: n("general") }),
      /* @__PURE__ */ s(P, { ctx: e, label: n("title"), node: r.title }),
      /* @__PURE__ */ s(P, { ctx: e, label: n("description"), node: r.description, multiline: !0 }),
      /* @__PURE__ */ h("div", { className: "aq-row aq-top", children: [
        /* @__PURE__ */ s(w, { label: n("tier"), children: /* @__PURE__ */ h(
          "select",
          {
            className: "select",
            value: r.tier,
            onChange: (l) => o((u) => {
              u.tier = l.target.value;
            }),
            children: [
              /* @__PURE__ */ s("option", { value: "", children: n("noTier") }),
              t.tiers.map((l) => /* @__PURE__ */ s("option", { value: l.key, children: A(e.project, l.title.key, e.lang) || l.key }, l.key)),
              r.tier && !t.tiers.some((l) => l.key === r.tier) && /* @__PURE__ */ s("option", { value: r.tier, children: r.tier })
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
                onChange: (l) => o((u) => {
                  u.duration = l.target.value.trim();
                })
              }
            )
          }
        )
      ] }),
      /* @__PURE__ */ h("details", { className: "aq-fold", open: m || void 0, children: [
        /* @__PURE__ */ s("summary", { children: n("more") }),
        /* @__PURE__ */ s(
          w,
          {
            label: n("key"),
            hint: /* @__PURE__ */ s("span", { className: m ? "aq-err" : "aq-hint", children: n(m ? "keyTaken" : "questKeyHint") }),
            children: /* @__PURE__ */ s(
              "input",
              {
                className: "input aq-mono",
                value: r.key,
                onChange: (l) => o((u) => {
                  u.key = xe(l.target.value);
                }),
                onBlur: () => {
                  const l = Y(r.key);
                  l !== r.key && o((u) => {
                    u.key = l;
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
            onClick: () => o((l) => {
              const u = B(
                `stage_${l.stages.length + 1}`,
                l.stages.map((v) => v.key)
              );
              l.stages.push({ ...Re(l.stages.length + 1), key: u });
            }),
            children: [
              "+ ",
              n("addStage")
            ]
          }
        )
      ] }),
      /* @__PURE__ */ s(
        ne,
        {
          ids: Ce(r.stages.map((l) => l.key)),
          onMove: (l, u) => o((v) => le(v.stages, l, u)),
          className: "aq-stages",
          children: (l, u) => /* @__PURE__ */ s(
            $t,
            {
              ctx: e,
              quest: r,
              stage: r.stages[l],
              si: l,
              handle: u,
              editQuest: o
            }
          )
        }
      )
    ] }),
    /* @__PURE__ */ s(
      Se,
      {
        ctx: e,
        title: n("questEvents"),
        hint: n("questEventsHint"),
        hooks: pt,
        label: (l) => n(`questHook.${l}`),
        value: r.rest.actions,
        onChange: (l, u) => o((v) => {
          const f = { ...j(v.rest.actions) };
          _(f, l, u), _(v.rest, "actions", Object.keys(f).length > 0 ? f : void 0);
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
  const { t: o } = e, [m, d] = ie(null), c = (f) => r((g) => f(g.stages[n])), l = A(e.project, i.title.key, e.lang), u = i.flow || "parallel", v = t.stages.flatMap((f) => f.objectives.map((g) => g.key));
  return /* @__PURE__ */ h("div", { className: "aq-stage", children: [
    /* @__PURE__ */ h("div", { className: "aq-stage-head", children: [
      a,
      /* @__PURE__ */ s("span", { className: "aq-stage-n", children: n + 1 }),
      /* @__PURE__ */ s("div", { className: "aq-stage-title", children: /* @__PURE__ */ s(
        ve,
        {
          value: l,
          placeholder: o("stageTitle"),
          "aria-label": o("stage", { n: n + 1 }),
          onChange: (f) => e.edit(() => {
          }, [{ key: i.title.key, lang: e.lang, value: f }])
        }
      ) }),
      /* @__PURE__ */ s(
        O,
        {
          label: o("remove"),
          onClick: () => r((f) => {
            f.stages.splice(n, 1);
          }),
          children: "×"
        }
      )
    ] }),
    i.objectives.length > 1 && /* @__PURE__ */ s("div", { className: "aq-seg", role: "radiogroup", "aria-label": o("flow"), children: ["parallel", "sequential"].map((f) => /* @__PURE__ */ s(
      "button",
      {
        type: "button",
        role: "radio",
        "aria-checked": u === f,
        className: `chip${u === f ? " is-active" : ""}`,
        onClick: () => c((g) => {
          g.flow = f;
        }),
        children: o(`flow.${f}`)
      },
      f
    )) }),
    i.objectives.length === 0 && /* @__PURE__ */ s("span", { className: "aq-hint", children: o("noObjectives") }),
    /* @__PURE__ */ s(
      ne,
      {
        ids: Ce(i.objectives.map((f) => f.key)),
        onMove: (f, g) => c((q) => le(q.objectives, f, g)),
        className: "aq-objectives",
        children: (f, g) => {
          const q = i.objectives[f];
          return /* @__PURE__ */ s(
            Ot,
            {
              t: o,
              projectId: e.projectId,
              n: u === "sequential" ? `${n + 1}.${f + 1}` : "•",
              objective: q,
              open: m === q.key,
              onToggle: () => d(m === q.key ? null : q.key),
              keyTaken: v.filter((p) => p === q.key).length > 1,
              handle: g,
              onChange: (p) => c((b) => p(b.objectives[f])),
              onRename: (p) => {
                m === q.key && d(p);
              },
              onRemove: () => c((p) => {
                p.objectives.splice(f, 1);
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
          const f = nt("block_break", v);
          r((g) => {
            g.stages[n].objectives.push({
              key: f,
              type: "block_break",
              params: we("block_break")
            });
          }), d(f);
        },
        children: [
          "+ ",
          o("addObjective")
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
  keyTaken: o,
  handle: m,
  onChange: d,
  onRename: c,
  onRemove: l
}) {
  const u = L(n.type), v = u ? u.params : Object.keys(n.params).map((y) => ({ name: y, kind: "text" })), f = se(n.params), g = Ae(n.type, n.params), q = ht(n.type, n.params), p = o || q.length > 0, b = (y) => y.kind === "item" ? /* @__PURE__ */ s(
    Lt,
    {
      t: e,
      projectId: t,
      spec: y,
      params: n.params,
      onChange: (k) => d((x) => k(x.params))
    },
    y.name
  ) : /* @__PURE__ */ s(
    It,
    {
      t: e,
      spec: y,
      value: n.params[y.name],
      onChange: (k) => d((x) => _(x.params, y.name, k))
    },
    y.name
  );
  return /* @__PURE__ */ h("div", { className: `aq-obj${a ? " is-open" : ""}${p ? " has-problem" : ""}`, children: [
    /* @__PURE__ */ h("div", { className: "aq-obj-line", children: [
      m,
      /* @__PURE__ */ h("button", { type: "button", className: "aq-obj-main", onClick: r, "aria-expanded": a, children: [
        /* @__PURE__ */ s("span", { className: "aq-obj-n", children: i }),
        /* @__PURE__ */ s("span", { className: "aq-obj-icon", "aria-hidden": !0, children: u?.icon ?? "?" }),
        /* @__PURE__ */ h("span", { className: "aq-obj-text", children: [
          Ee(e, n.type),
          f !== void 0 && /* @__PURE__ */ h("strong", { children: [
            " · ",
            String(f)
          ] }),
          g ? ` × ${g}` : f !== void 0 ? ` × ${e("any")}` : ""
        ] }),
        p && /* @__PURE__ */ s(
          "span",
          {
            className: "aq-obj-dot",
            title: o ? e("objectiveKeyTaken") : e("missing", {
              fields: q.map((y) => e(`param.${y}`)).join(", ")
            })
          }
        )
      ] }),
      /* @__PURE__ */ s(O, { label: e("remove"), onClick: l, children: "×" })
    ] }),
    a && /* @__PURE__ */ h("div", { className: "aq-obj-form", children: [
      /* @__PURE__ */ s(w, { label: e("type"), children: /* @__PURE__ */ h(
        "select",
        {
          className: "select",
          value: n.type,
          onChange: (y) => d((k) => {
            k.type = y.target.value, k.params = we(k.type);
          }),
          children: [
            je.map((y) => /* @__PURE__ */ h("option", { value: y.type, children: [
              y.icon,
              " ",
              e(`objective.${y.type}`)
            ] }, y.type)),
            !u && /* @__PURE__ */ s("option", { value: n.type, children: n.type })
          ]
        }
      ) }),
      /* @__PURE__ */ s("div", { className: "aq-row aq-wrap", children: v.filter((y) => !y.advanced).map(b) }),
      q.length > 0 && /* @__PURE__ */ s("span", { className: "aq-warn", children: e("missing", {
        fields: q.map((y) => e(`param.${y}`)).join(", ")
      }) }),
      /* @__PURE__ */ h("details", { className: "aq-fold", open: o || void 0, children: [
        /* @__PURE__ */ s("summary", { children: e("more") }),
        v.filter((y) => y.advanced).map(b),
        /* @__PURE__ */ s(
          w,
          {
            label: e("key"),
            hint: /* @__PURE__ */ s("span", { className: o ? "aq-err" : "aq-hint", children: e(o ? "objectiveKeyTaken" : "objectiveKeyHint") }),
            children: /* @__PURE__ */ s(
              "input",
              {
                className: "input aq-mono",
                value: n.key,
                onChange: (y) => {
                  const k = xe(y.target.value);
                  d((x) => {
                    x.key = k;
                  }), c(k);
                },
                onBlur: () => {
                  const y = Y(n.key);
                  y !== n.key && (d((k) => {
                    k.key = y;
                  }), c(y));
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
  const r = oe(n, i), o = Array.isArray(n.match) ? n.match : void 0, m = (d, c) => a((l) => mt(l, i, d, c));
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
          match: o,
          onChange: (d) => m(d, o),
          onMatchChange: (d) => m(r, d)
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
  const a = e(`param.${t.name}`), r = t.required && (i === void 0 || i === ""), o = typeof i == "string" || typeof i == "number" ? String(i) : "";
  if (t.kind === "int")
    return /* @__PURE__ */ s(R, { label: a, value: i, onChange: n });
  const m = r ? /* @__PURE__ */ s("span", { className: "aq-warn", children: e("required") }) : t.name === "itemKey" ? /* @__PURE__ */ s("span", { className: "aq-hint", children: e("itemKeyHint") }) : void 0;
  if (t.kind === "material")
    return /* @__PURE__ */ s(w, { label: a, hint: m, children: /* @__PURE__ */ s(
      Be,
      {
        value: o,
        placeholder: e("any"),
        "aria-label": a,
        onChange: (c) => n(c.toUpperCase().replace(/\s+/g, "_"))
      }
    ) });
  const d = t.kind === "entity";
  return /* @__PURE__ */ h(w, { label: a, hint: m, children: [
    /* @__PURE__ */ s(
      "input",
      {
        className: `input${d ? " aq-mono" : ""}`,
        list: d ? "aq-entities" : void 0,
        value: o,
        placeholder: d ? e("any") : "",
        onChange: (c) => n(d ? c.target.value.toUpperCase().replace(/\s+/g, "_") : c.target.value)
      }
    ),
    d && /* @__PURE__ */ s("datalist", { id: "aq-entities", children: yt.map((c) => /* @__PURE__ */ s("option", { value: c }, c)) })
  ] });
}
function Mt({ ctx: e, quest: t }) {
  const { t: i, project: n, lang: a } = e, r = (o) => W(i, o);
  return /* @__PURE__ */ h("div", { className: "aq-preview", children: [
    /* @__PURE__ */ s("span", { className: "aq-preview-title", children: A(n, t.title.key, a) || t.key }),
    A(n, t.description.key, a) && /* @__PURE__ */ s("span", { className: "aq-preview-line", children: A(n, t.description.key, a) }),
    t.stages.map((o, m) => (
      // biome-ignore lint/suspicious/noArrayIndexKey: stages are listed in order
      /* @__PURE__ */ h("span", { className: "aq-preview-stage", children: [
        t.stages.length > 1 && /* @__PURE__ */ s("span", { className: "aq-preview-line", children: A(n, o.title.key, a) || i("stage", { n: m + 1 }) }),
        o.objectives.map((d) => /* @__PURE__ */ h("span", { className: "aq-preview-line", children: [
          "• ",
          r(d)
        ] }, d.key))
      ] }, m)
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
const Ht = {
  kind: ae,
  dir: ke,
  schemaVersion: re,
  // Created in the module's folder only (SDK 1.8); a group already elsewhere still loads.
  projects: [tt],
  // Data with no links: one "Quests" entry and page instead of a node per group (SDK 1.9).
  collection: { labels: Je },
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: de.en,
  labels: de,
  icon: Dt,
  component: At,
  title: (e, { project: t, lang: i }) => A(t, e.title.key, i),
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
    accent: ft,
    // The group's quests, one selectable row each: the Properties panel opens the one clicked.
    items: (e, { project: t, lang: i }) => bt(e, t, i),
    summary: (e, { lang: t }) => kt(e, t)
  }
}, me = "msquests:groups", he = "msquests:quests", Pt = "msquests:active", ye = {
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
}, fe = { en: "Available", fr: "Disponible" };
function Kt(e, t, i) {
  if (t !== me && t !== he && t !== Pt) return null;
  const n = e.documents?.[ae];
  if (!n || n.length === 0) return null;
  const a = e.defaultLang ?? "en", r = i?.serverRows, o = i?.params?.group || void 0, m = [...n].sort((c, l) => c.id < l.id ? -1 : c.id > l.id ? 1 : 0);
  if (t === me) {
    const c = Q(r, "key");
    return m.map((l) => Gt(l, c.get(l.id), a));
  }
  if (t === he) {
    const c = Q(r, "id");
    return m.filter((l) => !o || l.id === o).flatMap((l) => l.quests.map((u) => $e(l, u, c.get(`${l.id}:${u.key}`), a)));
  }
  if (!r) return null;
  const d = new Map(n.map((c) => [c.id, c]));
  return r.flatMap((c) => {
    const l = d.get(S(c.group));
    if (!l) return [c];
    const u = l.quests.find((v) => v.key === S(c.quest_key));
    return u ? [Ut(u, c, a)] : [];
  });
}
function Gt(e, t, i) {
  const n = Q(X(t?.quests), "key");
  return {
    key: e.id,
    name: e.title,
    description: e.description,
    actor_type: e.actorType,
    max_active: be(e.limits.max_active, 1),
    max_per_period: be(e.limits.max_per_period, 0),
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
    duration: Ft(t.duration),
    tier: t.tier,
    tier_name: a ? a.title : "",
    assigned: i?.assigned ?? !1,
    status_key: i?.status_key ?? "NOT_STARTED",
    status: i?.status ?? fe[n.slice(0, 2)] ?? fe.en,
    can_start: i?.can_start ?? !1,
    start_blocker: i?.start_blocker ?? "",
    start_blocker_label: i?.start_blocker_label ?? "",
    objective_name: i?.objective_name ?? "",
    objective_progress: i?.objective_progress ?? 0,
    objective_target: i?.objective_target ?? 0,
    progress_ratio: i?.progress_ratio ?? 0,
    stages: Oe(t, X(i?.stages), n)
  };
}
function Oe(e, t, i) {
  const n = Q(t, "key");
  return e.stages.map((a, r) => {
    const o = n.get(a.key), m = Q(X(o?.objectives), "key");
    return {
      key: a.key,
      name: a.title,
      flow: Le(a),
      index: r,
      completed: o?.completed ?? !1,
      active: o?.active ?? !1,
      progress_ratio: o?.progress_ratio ?? 0,
      objectives: a.objectives.map((d) => Qt(d, m.get(d.key), i))
    };
  });
}
function Qt(e, t, i) {
  const n = Bt(e), a = t && t.type === e.type && t.target === n;
  return {
    key: e.key,
    name: a ? t.name : zt(e, i),
    type: e.type,
    progress: t?.progress ?? 0,
    target: n,
    progress_ratio: t?.progress_ratio ?? 0,
    completed: t?.completed ?? !1,
    status_key: t?.status_key ?? "PENDING"
  };
}
function Ut(e, t, i) {
  const n = Oe(e, X(t.stages), i), a = n.find((o) => o.active === !0), r = a ? e.stages.find((o) => o.key === a.key) : void 0;
  return {
    ...t,
    name: e.title,
    description: e.description,
    stage_count: e.stages.length,
    stage_name: r ? r.title : t.stage_name ?? "",
    stage_flow: r ? Le(r) : t.stage_flow ?? "",
    objectives: n.flatMap((o) => o.objectives),
    stages: n
  };
}
function zt(e, t) {
  const n = (ye[t.slice(0, 2)] ?? ye.en)[e.type];
  if (!n) return e.type;
  const a = e.params, r = {
    amount: String(a.amount ?? ""),
    distance: String(a.distance ?? ""),
    command: S(a.command),
    value: S(a.value),
    // Bukkit's translation keys, as the objective configs write them (`<lang:…>`).
    crop: F("block", a.crop),
    entity_type: F("entity", a.entity_type),
    material: e.type === "block_break" ? F("block", a.material) : ge(e),
    fish_type: ge(e)
  };
  return n.replace(/%([a-z_]+)%/g, (o, m) => r[m] ?? "");
}
function F(e, t) {
  const i = S(t).toLowerCase().replace(/^minecraft:/, "");
  return i === "" ? "" : `<lang:${e}.minecraft.${i}>`;
}
function ge(e) {
  const t = L(e.type)?.params.find((r) => r.kind === "item");
  if (!t) return "";
  const i = oe(e.params, t), n = i.title;
  if (typeof n == "string" && n.trim() !== "") return n;
  const a = S(i.material);
  return /^(minecraft:)?[a-z_0-9]+$/i.test(a) && !a.includes(":") ? F("item", a) : a || Ne(i);
}
function Bt(e) {
  const t = se(e.params);
  if (t === void 0) return 1;
  const i = Number(t);
  return Number.isFinite(i) ? i : 1;
}
function Le(e) {
  return e.flow === "" ? "PARALLEL" : e.flow.toUpperCase();
}
function Ft(e) {
  const t = e.trim().toLowerCase().replace(/ /g, "");
  if (t === "") return 0;
  if (/^\d+$/.test(t)) return Number(t);
  const i = /^(?:(\d+)d)?(?:(\d+)h)?(?:(\d+)m)?(?:(\d+)s)?$/.exec(t);
  if (!i) return 0;
  const [, n, a, r, o] = i;
  return Number(n ?? 0) * 86400 + Number(a ?? 0) * 3600 + Number(r ?? 0) * 60 + Number(o ?? 0);
}
function Q(e, t) {
  return new Map((e ?? []).map((i) => [S(i[t]), i]));
}
function X(e) {
  return Array.isArray(e) ? e.filter((t) => t && typeof t == "object") : [];
}
function be(e, t) {
  return typeof e == "number" && Number.isFinite(e) ? e : t;
}
function S(e) {
  return typeof e == "string" ? e : "";
}
const Vt = {
  id: "msquests",
  displayName: "Quests",
  schemaVersion: "quest_group/v1",
  sdk: Fe,
  sidebar: { icon: () => null, label: "Quests", to: "/m/msquests" },
  // Quests are Documents: they are edited from the project, no screen of their own.
  routes: [],
  documents: [Ht],
  // The quest sources previewed from the draft, joined to the player's state on the server.
  localPreviewRows: Kt
}, Xt = { ...Vt, sdk: "1.10.0" };
export {
  Xt as default
};
