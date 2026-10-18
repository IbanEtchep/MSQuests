import { createModuleI18n as ke, fieldText as xe, newTextKey as O, textValue as E, injectModuleStyles as je, useProject as Ne, useContentLang as we, Button as H, selectDocumentItem as _e, updateDocument as Ae, useUiLang as Ee, MiniMessageInput as re, ActionChainEditor as Te, ItemInput as $e, MaterialInput as Re, SDK_VERSION as Ce } from "@artisan/editor-sdk";
import { jsxs as m, jsx as a, Fragment as U } from "react/jsx-runtime";
import { useState as Y, useEffect as Se } from "react";
const Oe = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"]
}, Le = /^\d+$/;
function F(e, n, i) {
  const t = String(i).padStart(2, "0");
  return e === "fr" ? i === 0 ? `${n} h` : `${n} h ${t}` : `${String(n).padStart(2, "0")}:${t}`;
}
function $(e, n, i) {
  return Le.test(e) && Number(e) >= n && Number(e) <= i;
}
function te(e) {
  return e.trim().split(/\s+/).length === 5;
}
function Ie(e, n) {
  const i = n === "fr", t = e.trim().split(/\s+/);
  if (t.length !== 5) return null;
  const [r, s, c, h, l] = t;
  if (h !== "*") return null;
  const d = /^\*\/(\d+)$/;
  if (c === "*" && l === "*") {
    if ($(r, 0, 59) && $(s, 0, 23))
      return i ? `tous les jours à ${F(n, +s, +r)}` : `every day at ${F(n, +s, +r)}`;
    const p = d.exec(s);
    if ($(r, 0, 59) && p)
      return i ? `toutes les ${p[1]} h, à la minute ${r}` : `every ${p[1]} hours, at minute ${r}`;
    if ($(r, 0, 59) && s === "*")
      return i ? `toutes les heures, à la minute ${r}` : `every hour, at minute ${r}`;
    const y = d.exec(r);
    return y && s === "*" ? i ? `toutes les ${y[1]} minutes` : `every ${y[1]} minutes` : null;
  }
  if (!$(r, 0, 59) || !$(s, 0, 23)) return null;
  const o = F(n, +s, +r);
  if (c === "*" && $(l, 0, 7)) {
    const p = Oe[i ? "fr" : "en"][Number(l) % 7];
    return i ? `chaque ${p} à ${o}` : `every ${p} at ${o}`;
  }
  return l === "*" && $(c, 1, 31) ? i ? `le ${c === "1" ? "1er" : c} de chaque mois à ${o}` : `on day ${c} of each month at ${o}` : null;
}
function I(e, n, i) {
  const t = /* @__PURE__ */ new Set();
  for (const r of e.split(",")) {
    const s = /^(\*|(\d+)(?:-(\d+))?)(?:\/(\d+))?$/.exec(r);
    if (!s) return null;
    const c = s[4] ? Number(s[4]) : 1, h = s[1] === "*" ? n : Number(s[2]), l = s[1] === "*" ? i : s[3] ? Number(s[3]) : s[4] ? i : h;
    if (c < 1 || h < n || l > i || h > l) return null;
    for (let d = h; d <= l; d += c) t.add(d);
  }
  return t;
}
function De(e, n) {
  const i = e.trim().split(/\s+/);
  if (i.length !== 5) return null;
  const t = I(i[0], 0, 59), r = I(i[1], 0, 23), s = I(i[2], 1, 31), c = I(i[3], 1, 12), h = I(i[4], 0, 7);
  if (!t || !r || !s || !c || !h) return null;
  h.has(7) && h.add(0);
  const l = i[2] === "*", d = i[4] === "*", o = (g) => l && d ? !0 : l ? h.has(g.getDay()) : d ? s.has(g.getDate()) : s.has(g.getDate()) || h.has(g.getDay()), p = new Date(n.getTime());
  p.setSeconds(0, 0), p.setMinutes(p.getMinutes() + 1);
  const y = n.getTime() + 366 * 24 * 3600 * 1e3;
  for (; p.getTime() <= y; ) {
    if (!c.has(p.getMonth() + 1) || !o(p)) {
      p.setDate(p.getDate() + 1), p.setHours(0, 0, 0, 0);
      continue;
    }
    if (!r.has(p.getHours())) {
      p.setHours(p.getHours() + 1, 0, 0, 0);
      continue;
    }
    if (t.has(p.getMinutes())) return p;
    p.setMinutes(p.getMinutes() + 1);
  }
  return null;
}
const z = {
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
}, X = {
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
}, ne = { en: z.kindLabel, fr: X.kindLabel }, Me = ke({ en: z, fr: X });
function se(e) {
  const n = e === "fr" ? X : z;
  return (i, t) => {
    const r = n[i] ?? z[i] ?? i;
    return t ? r.replace(/\{\{(\w+)\}\}/g, (s, c) => String(t[c] ?? s)) : r;
  };
}
const oe = "quest_group", le = "quest_groups", J = "quest_group/v1", W = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
function Q(e = Date.now()) {
  let n = "", i = e;
  for (let s = 0; s < 10; s++)
    n = W[i % 32] + n, i = Math.floor(i / 32);
  const t = new Uint8Array(16);
  crypto.getRandomValues(t);
  let r = "";
  for (let s = 0; s < 16; s++) r += W[t[s] % 32];
  return n + r;
}
function G(e, n) {
  const i = new Set(n);
  if (!i.has(e)) return e;
  for (let t = 2; ; t++) if (!i.has(`${e}_${t}`)) return `${e}_${t}`;
}
function He(e, n) {
  const i = new Set(n), t = new Uint8Array(4);
  for (; ; ) {
    crypto.getRandomValues(t);
    const r = Array.from(t, (c) => W[c % 32].toLowerCase()).join(""), s = `${B(e)}_${r}`;
    if (!i.has(s)) return s;
  }
}
function ce(e) {
  return e.toLowerCase().replace(/[^a-z0-9_]/g, "_");
}
function B(e) {
  return e.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().replace(/[^a-z0-9_]+/g, "_").replace(/^_+|_+$/g, "") || "quest";
}
function w(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? { ...e } : {};
}
function V(e) {
  return Array.isArray(e) ? e : [];
}
function A(e) {
  return typeof e == "string" || typeof e == "number" || typeof e == "boolean" ? String(e) : "";
}
function D(e, ...n) {
  const i = { ...e };
  for (const t of n) delete i[t];
  return i;
}
function Pe(e, n) {
  const i = {}, t = [];
  function r(l) {
    if (l && typeof l == "object" && !Array.isArray(l)) {
      const p = O(), y = {};
      for (const [g, f] of Object.entries(l)) y[g] = A(f);
      return i[p] = y, { key: p };
    }
    const d = typeof l == "string" ? /^\{i18n:(.+)\}$/.exec(l) : null;
    if (d) {
      const p = n.translations?.[d[1]];
      return p && (i[d[1]] = { ...p }), { key: d[1] };
    }
    const o = O();
    return i[o] = l == null || l === "" ? {} : { [n.lang]: A(l) }, { key: o };
  }
  const s = Object.entries(w(e.tiers)).map(([l, d]) => {
    const o = w(d);
    return { key: l, title: r(o.title), rest: D(o, "title") };
  }), c = V(e.quests).map((l, d) => {
    const o = w(l), p = V(o.stages).map((y, g) => {
      const f = w(y), k = A(f.flow).toLowerCase();
      return {
        key: A(f.key) || `stage_${g + 1}`,
        title: r(f.title),
        flow: k === "parallel" || k === "sequential" ? k : "",
        objectives: V(f.objectives).map((u, v) => {
          const q = w(u);
          return {
            key: A(q.key) || `objective_${v + 1}`,
            type: A(q.type),
            params: D(q, "key", "type")
          };
        }),
        rest: D(f, "key", "title", "flow", "objectives")
      };
    });
    return {
      key: A(o.key) || `quest_${d + 1}`,
      uid: A(o.uid) || Q(),
      tier: A(o.tier),
      title: r(o.title),
      description: r(o.description),
      duration: A(o.duration),
      stages: p,
      rest: Ke(o)
    };
  }), h = {
    id: A(e.id),
    projectId: n.projectId,
    uid: A(e.uid) || Q(),
    title: r(e.title),
    description: r(e.description),
    actorType: A(e.actor_type) || "player",
    reset: w(e.reset),
    limits: w(e.limits),
    rotation: w(e.rotation),
    distribution: w(e.distribution),
    tiers: s,
    quests: c,
    rest: de(D(
      w(e),
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
  return e.schema !== void 0 && e.schema !== J && t.push(`schema: ${String(e.schema)}`), { doc: h, i18nKeys: i, unsupported: t };
}
function de(e) {
  return e.on !== void 0 && (e.actions == null && (e.actions = e.on), delete e.on), e;
}
function Ke(e) {
  const n = de(
    D(e, "key", "uid", "tier", "title", "description", "duration", "stages", "reward_display")
  );
  if (n.rewards !== void 0) {
    const i = w(n.actions);
    i.complete == null && (n.actions = { ...i, complete: n.rewards }), delete n.rewards;
  }
  return n;
}
function Ge(e) {
  return typeof e == "string" ? e === "" : Object.values(e).every((n) => n === "");
}
function R(e) {
  const n = {};
  for (const [i, t] of Object.entries(e))
    t == null || t === "" || Array.isArray(t) && t.length === 0 || typeof t == "object" && !Array.isArray(t) && Object.keys(t).length === 0 || (n[i] = t);
  return n;
}
const Ue = [
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
function ze(e, n) {
  const i = (d) => xe(n, d.key), t = (d) => {
    const o = i(d);
    return Ge(o) ? void 0 : o;
  }, r = {};
  for (const d of e.tiers) r[d.key] = { ...d.rest, title: i(d.title) };
  const s = e.quests.map(
    (d) => R({
      key: d.key,
      uid: d.uid,
      tier: d.tier,
      title: i(d.title),
      description: t(d.description),
      duration: d.duration,
      ...d.rest,
      stages: d.stages.map(
        (o) => R({
          key: o.key,
          title: t(o.title),
          flow: o.flow,
          ...o.rest,
          objectives: o.objectives.map((p) => ({ key: p.key, type: p.type, ...p.params }))
        })
      )
    })
  ), c = {
    ...e.rest,
    id: e.id,
    schema: J,
    uid: e.uid,
    title: i(e.title),
    description: t(e.description),
    actor_type: e.actorType,
    reset: R(e.reset),
    limits: R(e.limits),
    rotation: R(e.rotation),
    distribution: R(e.distribution),
    tiers: r,
    quests: s
  }, h = {};
  for (const d of Ue) d in c && (h[d] = c[d]);
  for (const d of Object.keys(c)) d in h || (h[d] = c[d]);
  const l = R(h);
  return l.quests = s, l;
}
function* Qe(e) {
  const n = `${le}.${e.id}`;
  yield { lText: e.title, key: `${n}.title`, path: "title", kind: "quest_group.title" }, yield {
    lText: e.description,
    key: `${n}.description`,
    path: "description",
    kind: "quest_group.description"
  };
  for (const t of e.tiers)
    yield {
      lText: t.title,
      key: `${n}.tiers.${t.key}.title`,
      path: `tiers.${t.key}.title`,
      kind: "quest_group.tier"
    };
  const i = /* @__PURE__ */ new Set();
  for (let t = 0; t < e.quests.length; t++) {
    const r = e.quests[t], s = i.has(r.uid) ? `${r.uid}_${t}` : r.uid;
    i.add(r.uid);
    const c = `${n}.quests.${s}`;
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
    const h = /* @__PURE__ */ new Set();
    for (let l = 0; l < r.stages.length; l++) {
      const d = r.stages[l], o = h.has(d.key) ? `${d.key}_${l}` : d.key;
      h.add(d.key), yield {
        lText: d.title,
        key: `${c}.stages.${o}.title`,
        path: `quests[${t}].stages[${l}].title`,
        kind: "quest.stage"
      };
    }
  }
}
function ie(e) {
  return !!e && typeof e == "object" && !Array.isArray(e) && "type" in e;
}
function Be(e) {
  const n = Array.isArray(e) ? e : e == null ? [] : [e];
  return {
    chain: n.filter((i) => !ie(i)),
    legacy: n.filter(ie)
  };
}
function ae(e, n) {
  const i = [...e, ...n];
  return i.length ? i : void 0;
}
function Fe(e) {
  const n = String(e.type), i = e.command ?? e.message ?? e.title ?? e.material;
  return typeof i == "string" ? `${n} · ${i}` : n;
}
const Ve = [
  "distribution",
  "actor_load",
  "quest_start",
  "objective_progress",
  "objective_complete",
  "quest_complete",
  "period_complete"
], We = ["start", "objective_progress", "objective_complete", "complete"], C = { name: "amount", kind: "int", required: !0 }, ue = [
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
function P(e) {
  return ue.find((n) => n.type === e);
}
function pe(e) {
  const n = {};
  for (const i of P(e)?.params ?? [])
    i.kind === "int" && (n[i.name] = 1);
  return n;
}
function me(e) {
  return e.amount ?? e.distance;
}
function he(e, n) {
  const i = e[n.name];
  if (i && typeof i == "object" && !Array.isArray(i)) return i;
  const t = n.legacy ? e[n.legacy] : void 0;
  return typeof t == "string" && t !== "" ? { material: t } : {};
}
function Ze(e, n, i, t) {
  n.legacy && delete e[n.legacy];
  const r = !i || Object.keys(i).length === 0;
  r ? delete e[n.name] : e[n.name] = i, r || t === void 0 ? delete e.match : e.match = t;
}
function Ye(e) {
  const n = e.title;
  if (typeof n == "string" && n.trim() !== "") return n.replace(/<[^>]*>/g, "");
  const i = e.material ?? e.item;
  return typeof i == "string" ? i : "";
}
function ye(e, n) {
  const i = P(e);
  return i ? i.params.filter((t) => t.kind !== "int").map((t) => t.kind === "item" ? Ye(he(n, t)) : n[t.name]).filter((t) => typeof t == "string" && t !== "").join(" = ") : Object.keys(n).map((t) => n[t]).filter((t) => typeof t == "string" && t !== "").join(" = ");
}
function Xe(e, n) {
  return (P(e)?.params ?? []).filter((i) => i.required && (n[i.name] === void 0 || n[i.name] === "")).map((i) => i.name);
}
const Je = [
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
], et = "#f59e0b";
function fe(e, n) {
  return P(n) ? e(`objective.${n}`) : n;
}
function ge(e, n) {
  const i = ye(n.type, n.params), t = me(n.params);
  return `${fe(e, n.type)}${i ? ` · ${i}` : ""}${t !== void 0 ? ` · 0/${String(t)}` : ""}`;
}
function ve(e) {
  return e.replace(/<[^<>]*>/g, "").trim();
}
function tt(e) {
  const n = e.distribution?.strategy;
  return typeof n == "string" && n.toLowerCase() === "sequential";
}
function nt(e, n, i) {
  const t = se(i), r = tt(e);
  return e.quests.map((s, c) => {
    const h = E(n, s.title.key, i);
    return {
      id: s.uid,
      label: ve(h) || s.key,
      detail: s.tier || void 0,
      marker: r ? String(c + 1) : void 0,
      hover: {
        title: h || s.key,
        text: E(n, s.description.key, i) || void 0,
        lines: it(t, s, n, i)
      }
    };
  });
}
function it(e, n, i, t) {
  const r = n.stages.length > 1;
  return n.stages.flatMap(
    (s, c) => s.objectives.map((h) => {
      const l = ge(e, h);
      return r ? `<gray>${ve(E(i, s.title.key, t)) || e("stage", { n: c + 1 })} ·</gray> ${l}` : l;
    })
  );
}
function at(e, n) {
  const i = se(n), t = typeof e.distribution?.strategy == "string" ? e.distribution.strategy.toLowerCase() : "", r = i("questCount", { count: e.quests.length });
  return ["none", "sequential", "random"].includes(t) ? `${r} · ${i(`strategy.${t}`).toLowerCase()}` : r;
}
const rt = `
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
`, st = /^(\d+|(\d+[dhms])+)$/, ot = ["actor_load", "period_reset", "quest_complete"], lt = (() => {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
  } catch {
    return "";
  }
})();
function ct(e, n) {
  const i = De(e, /* @__PURE__ */ new Date());
  if (!i) return null;
  try {
    return new Intl.DateTimeFormat(n, {
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
function dt({ projectId: e, documentId: n, document: i, item: t }) {
  je("msquests", rt);
  const r = i, s = Ne(e), c = we(), h = Me(), [l, d] = Y(t ?? null);
  Se(() => {
    d(t ?? null);
  }, [t]);
  const o = (f) => {
    d(f), _e({ documentId: n, item: f });
  }, y = { projectId: e, project: s, lang: c, t: h, edit: (f, k) => {
    const u = structuredClone(r);
    f(u), Ae({ projectId: e, kind: oe, id: n, doc: u, texts: k });
  } }, g = l ? r.quests.findIndex((f) => f.uid === l) : -1;
  return g >= 0 ? /* @__PURE__ */ m("div", { className: "aq", children: [
    /* @__PURE__ */ m(H, { variant: "ghost", size: "sm", className: "aq-back", onClick: () => o(null), children: [
      "← ",
      h("back")
    ] }),
    /* @__PURE__ */ a(yt, { ctx: y, group: r, index: g })
  ] }) : /* @__PURE__ */ a("div", { className: "aq", children: /* @__PURE__ */ a(pt, { ctx: y, group: r, onOpen: o }) });
}
function _({
  label: e,
  children: n,
  hint: i
}) {
  return (
    // biome-ignore lint/a11y/noLabelWithoutControl: the control is the child
    /* @__PURE__ */ m("label", { className: "aq-field", children: [
      /* @__PURE__ */ a("span", { className: "field-label", children: e }),
      n,
      i
    ] })
  );
}
function M({
  ctx: e,
  label: n,
  node: i,
  multiline: t
}) {
  const r = E(e.project, i.key, e.lang), s = (c) => e.edit(() => {
  }, [{ key: i.key, lang: e.lang, value: c }]);
  return /* @__PURE__ */ a(_, { label: `${n} · ${e.lang}`, children: /* @__PURE__ */ a(re, { value: r, onChange: s, multiline: t, "aria-label": n }) });
}
function ut(e) {
  return typeof e == "number" ? String(e) : typeof e == "string" ? e : "";
}
function S({
  label: e,
  value: n,
  onChange: i,
  placeholder: t
}) {
  return /* @__PURE__ */ a(_, { label: e, children: /* @__PURE__ */ a(
    "input",
    {
      className: "input",
      type: "number",
      min: 0,
      step: 1,
      value: ut(n),
      placeholder: t,
      onChange: (r) => {
        const s = r.target.value.trim();
        i(s === "" ? void 0 : Math.max(0, Math.trunc(Number(s))));
      }
    }
  ) });
}
function N(e, n, i) {
  i === void 0 || i === "" ? delete e[n] : e[n] = i;
}
function Z(e, n, i) {
  if (i < 0 || i >= e.length) return;
  const [t] = e.splice(n, 1);
  e.splice(i, 0, t);
}
function L({
  label: e,
  onClick: n,
  disabled: i,
  children: t
}) {
  return /* @__PURE__ */ a(
    "button",
    {
      type: "button",
      className: "aq-icon-btn",
      title: e,
      "aria-label": e,
      onClick: n,
      disabled: i,
      children: t
    }
  );
}
function ee({
  t: e,
  index: n,
  count: i,
  onMove: t,
  onRemove: r
}) {
  return /* @__PURE__ */ m(U, { children: [
    /* @__PURE__ */ a(L, { label: e("moveUp"), disabled: n === 0, onClick: () => t(n - 1), children: "↑" }),
    /* @__PURE__ */ a(
      L,
      {
        label: e("moveDown"),
        disabled: n === i - 1,
        onClick: () => t(n + 1),
        children: "↓"
      }
    ),
    /* @__PURE__ */ a(L, { label: e("remove"), onClick: r, children: "×" })
  ] });
}
function pt({
  ctx: e,
  group: n,
  onOpen: i
}) {
  const { t, edit: r, project: s, lang: c } = e, h = Ee(), l = typeof n.reset.cron == "string" ? n.reset.cron : "", d = l ? Ie(l, h) : null, o = typeof n.reset.timezone == "string" ? n.reset.timezone : "", p = l && te(l) ? ct(l, h) : null, y = Array.isArray(n.distribution.triggers) ? n.distribution.triggers : [], g = n.distribution.tiers ?? {}, f = Object.keys(n.rest).filter((u) => u !== "actions"), k = () => {
    const u = O(), v = Q();
    r(
      (q) => {
        q.quests.push({
          key: G(
            "quest",
            q.quests.map((x) => x.key)
          ),
          uid: v,
          tier: "",
          title: { key: u },
          description: { key: O() },
          duration: "",
          stages: [qe(1)],
          rest: {}
        });
      },
      [{ key: u, lang: c, value: t("newQuestTitle") }]
    ), i(v);
  };
  return /* @__PURE__ */ m(U, { children: [
    /* @__PURE__ */ m("section", { className: "aq-section", children: [
      /* @__PURE__ */ a("span", { className: "aq-h", children: t("group") }),
      /* @__PURE__ */ a(M, { ctx: e, label: t("title"), node: n.title }),
      /* @__PURE__ */ a(M, { ctx: e, label: t("description"), node: n.description, multiline: !0 }),
      /* @__PURE__ */ a(_, { label: t("actorType"), children: /* @__PURE__ */ m(
        "select",
        {
          className: "select",
          value: n.actorType,
          onChange: (u) => r((v) => {
            v.actorType = u.target.value;
          }),
          children: [
            /* @__PURE__ */ a("option", { value: "player", children: t("actor.player") }),
            /* @__PURE__ */ a("option", { value: "global", children: t("actor.global") }),
            !["player", "global"].includes(n.actorType) && /* @__PURE__ */ a("option", { value: n.actorType, children: n.actorType })
          ]
        }
      ) })
    ] }),
    /* @__PURE__ */ m("section", { className: "aq-section", children: [
      /* @__PURE__ */ a("span", { className: "aq-h", children: t("reset") }),
      /* @__PURE__ */ a(
        _,
        {
          label: t("cron"),
          hint: l ? te(l) ? /* @__PURE__ */ m("span", { className: "aq-ok", children: [
            d ?? t("cronRaw"),
            o && o !== lt ? `, ${o}` : p ? ` · ${t("nextRun", { when: p })}` : ""
          ] }) : /* @__PURE__ */ a("span", { className: "aq-err", children: t("cronInvalid") }) : /* @__PURE__ */ a("span", { className: "aq-hint", children: t("noReset") }),
          children: /* @__PURE__ */ a(
            "input",
            {
              className: "input",
              value: l,
              placeholder: "0 4 * * *",
              onChange: (u) => r((v) => N(v.reset, "cron", u.target.value))
            }
          )
        }
      )
    ] }),
    /* @__PURE__ */ m("section", { className: "aq-section", children: [
      /* @__PURE__ */ a("span", { className: "aq-h", children: t("distribution") }),
      /* @__PURE__ */ m("div", { className: "aq-row", children: [
        /* @__PURE__ */ a(_, { label: t("strategy"), children: /* @__PURE__ */ m(
          "select",
          {
            className: "select",
            value: typeof n.distribution.strategy == "string" ? n.distribution.strategy.toLowerCase() : "",
            onChange: (u) => r((v) => N(v.distribution, "strategy", u.target.value)),
            children: [
              /* @__PURE__ */ a("option", { value: "", children: "—" }),
              ["none", "sequential", "random"].map((u) => /* @__PURE__ */ a("option", { value: u, children: t(`strategy.${u}`) }, u))
            ]
          }
        ) }),
        /* @__PURE__ */ a(
          S,
          {
            label: t("amount"),
            value: n.distribution.amount,
            onChange: (u) => r((v) => N(v.distribution, "amount", u))
          }
        )
      ] }),
      /* @__PURE__ */ a("span", { className: "field-label", children: t("triggers") }),
      ot.map((u) => /* @__PURE__ */ m("label", { className: "aq-check", children: [
        /* @__PURE__ */ a(
          "input",
          {
            type: "checkbox",
            checked: y.includes(u),
            onChange: (v) => r((q) => {
              const x = Array.isArray(q.distribution.triggers) ? q.distribution.triggers : [], b = v.target.checked ? [...x, u] : x.filter((j) => j !== u);
              N(q.distribution, "triggers", b.length ? b : void 0);
            })
          }
        ),
        t(`trigger.${u}`)
      ] }, u)),
      n.tiers.length > 0 && /* @__PURE__ */ m(U, { children: [
        /* @__PURE__ */ a("span", { className: "field-label", children: t("perTier") }),
        /* @__PURE__ */ a("div", { className: "aq-row", children: n.tiers.map((u) => /* @__PURE__ */ a(
          S,
          {
            label: E(s, u.title.key, c) || u.key,
            value: g[u.key],
            onChange: (v) => r((q) => {
              const x = { ...q.distribution.tiers ?? {} };
              N(x, u.key, v), N(
                q.distribution,
                "tiers",
                Object.keys(x).length ? x : void 0
              );
            })
          },
          u.key
        )) })
      ] })
    ] }),
    /* @__PURE__ */ m("section", { className: "aq-section", children: [
      /* @__PURE__ */ a("span", { className: "aq-h", children: t("limits") }),
      /* @__PURE__ */ m("div", { className: "aq-row", children: [
        /* @__PURE__ */ a(
          S,
          {
            label: t("maxActive"),
            value: n.limits.max_active,
            onChange: (u) => r((v) => N(v.limits, "max_active", u))
          }
        ),
        /* @__PURE__ */ a(
          S,
          {
            label: t("questsPerPeriod"),
            placeholder: t("noLimit"),
            value: n.limits.max_per_period,
            onChange: (u) => r((v) => N(v.limits, "max_per_period", u))
          }
        )
      ] }),
      /* @__PURE__ */ a("span", { className: "aq-h", children: t("rotation") }),
      /* @__PURE__ */ m("div", { className: "aq-row", children: [
        /* @__PURE__ */ m("label", { className: "aq-check", children: [
          /* @__PURE__ */ a(
            "input",
            {
              type: "checkbox",
              checked: n.rotation.enabled === !0,
              onChange: (u) => r((v) => N(v.rotation, "enabled", u.target.checked ? !0 : void 0))
            }
          ),
          t("rotationEnabled")
        ] }),
        n.rotation.enabled === !0 ? /* @__PURE__ */ a(
          S,
          {
            label: t("rotationsPerPeriod"),
            placeholder: t("noLimit"),
            value: n.rotation.max_per_period,
            onChange: (u) => r((v) => N(v.rotation, "max_per_period", u))
          }
        ) : null
      ] })
    ] }),
    /* @__PURE__ */ m("section", { className: "aq-section", children: [
      /* @__PURE__ */ m("div", { className: "aq-head", children: [
        /* @__PURE__ */ a("span", { className: "aq-h", children: t("tiers") }),
        /* @__PURE__ */ m(
          H,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => r((u) => {
              u.tiers.push({
                key: G(
                  "tier",
                  u.tiers.map((v) => v.key)
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
      n.tiers.map((u, v) => /* @__PURE__ */ m("div", { className: "aq-row", children: [
        /* @__PURE__ */ a(_, { label: t("tierKey"), children: /* @__PURE__ */ a(
          "input",
          {
            className: "input",
            defaultValue: u.key,
            onBlur: (q) => {
              const x = B(q.target.value);
              x !== u.key && r((b) => {
                const j = G(
                  x,
                  b.tiers.map((K) => K.key)
                );
                b.tiers[v].key = j;
                for (const K of b.quests) K.tier === u.key && (K.tier = j);
                const T = b.distribution.tiers;
                T && u.key in T && (T[j] = T[u.key], delete T[u.key]);
              });
            }
          }
        ) }),
        /* @__PURE__ */ a(M, { ctx: e, label: t("title"), node: u.title }),
        /* @__PURE__ */ a(
          L,
          {
            label: t("remove"),
            onClick: () => r((q) => {
              q.tiers.splice(v, 1);
            }),
            children: "×"
          }
        )
      ] }, u.key))
    ] }),
    /* @__PURE__ */ m("section", { className: "aq-section", children: [
      /* @__PURE__ */ m("div", { className: "aq-head", children: [
        /* @__PURE__ */ m("span", { className: "aq-h", children: [
          t("quests"),
          " · ",
          n.quests.length
        ] }),
        /* @__PURE__ */ m(H, { size: "sm", variant: "primary", onClick: k, children: [
          "+ ",
          t("addQuest")
        ] })
      ] }),
      n.quests.length === 0 && /* @__PURE__ */ a("span", { className: "aq-hint", children: t("noQuests") }),
      /* @__PURE__ */ a("div", { className: "aq-list", children: n.quests.map((u, v) => /* @__PURE__ */ m("div", { className: "aq-item", children: [
        /* @__PURE__ */ m(
          "button",
          {
            type: "button",
            className: "aq-item-main",
            onClick: () => i(u.uid),
            title: t("open"),
            children: [
              /* @__PURE__ */ a("span", { className: "aq-item-title", children: E(s, u.title.key, c) || u.key }),
              /* @__PURE__ */ m("span", { className: "aq-item-sub", children: [
                t("questSummary", {
                  stages: u.stages.length,
                  objectives: u.stages.reduce((q, x) => q + x.objectives.length, 0)
                }),
                " · ",
                u.key,
                u.tier ? ` · ${u.tier}` : "",
                u.duration ? ` · ${u.duration}` : ""
              ] })
            ]
          }
        ),
        /* @__PURE__ */ a(
          ee,
          {
            t,
            index: v,
            count: n.quests.length,
            onMove: (q) => r((x) => Z(x.quests, v, q)),
            onRemove: () => r((q) => {
              q.quests.splice(v, 1);
            })
          }
        )
      ] }, u.uid)) }),
      f.length > 0 && /* @__PURE__ */ a("span", { className: "aq-hint", children: t("kept", { fields: f.join(", ") }) })
    ] }),
    /* @__PURE__ */ a(ht, { ctx: e, group: n })
  ] });
}
function mt({
  ctx: e,
  value: n,
  onChange: i
}) {
  const { chain: t, legacy: r } = Be(n);
  return /* @__PURE__ */ m("div", { className: "aq-chain", children: [
    /* @__PURE__ */ a(
      Te,
      {
        projectId: e.projectId,
        value: t,
        onChange: (s) => i(ae(s, r))
      }
    ),
    r.length > 0 && /* @__PURE__ */ m("div", { className: "aq-legacy", children: [
      /* @__PURE__ */ a("span", { className: "aq-hint", children: e.t("legacySteps") }),
      r.map((s, c) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: a step has no identity
        /* @__PURE__ */ m("div", { className: "aq-legacy-row", children: [
          /* @__PURE__ */ a("code", { children: Fe(s) }),
          /* @__PURE__ */ a(
            L,
            {
              label: e.t("remove"),
              onClick: () => i(ae(t, r.filter((h, l) => l !== c))),
              children: "×"
            }
          )
        ] }, c)
      ))
    ] })
  ] });
}
function be({
  ctx: e,
  title: n,
  hint: i,
  hooks: t,
  label: r,
  value: s,
  onChange: c,
  footer: h
}) {
  const { t: l } = e, d = w(s), [o, p] = Y([]), y = t.filter((f) => d[f] != null || o.includes(f)), g = t.filter((f) => !y.includes(f));
  return /* @__PURE__ */ m("section", { className: "aq-section", children: [
    /* @__PURE__ */ a("span", { className: "aq-h", children: n }),
    /* @__PURE__ */ a("span", { className: "aq-hint", children: i }),
    y.map((f) => /* @__PURE__ */ m("div", { className: "aq-event", children: [
      /* @__PURE__ */ m("div", { className: "aq-head", children: [
        /* @__PURE__ */ a("span", { className: "aq-event-name", children: r(f) }),
        /* @__PURE__ */ a(
          L,
          {
            label: l("removeEvent"),
            onClick: () => {
              p((k) => k.filter((u) => u !== f)), c(f, void 0);
            },
            children: "×"
          }
        )
      ] }),
      /* @__PURE__ */ a(mt, { ctx: e, value: d[f], onChange: (k) => c(f, k) })
    ] }, f)),
    g.length > 0 && /* @__PURE__ */ m(
      "select",
      {
        className: "select aq-event-add",
        "aria-label": l("addEvent"),
        value: "",
        onChange: (f) => {
          const k = g.find((u) => u === f.target.value);
          k && p((u) => [...u, k]);
        },
        children: [
          /* @__PURE__ */ a("option", { value: "", children: l("addEvent") }),
          g.map((f) => /* @__PURE__ */ a("option", { value: f, children: r(f) }, f))
        ]
      }
    ),
    h
  ] });
}
function ht({ ctx: e, group: n }) {
  const { t: i, edit: t } = e;
  return /* @__PURE__ */ a(
    be,
    {
      ctx: e,
      title: i("hooks"),
      hint: i("hooksHint"),
      hooks: Ve,
      label: (r) => i(`hook.${r}`),
      value: n.rest.actions,
      onChange: (r, s) => t((c) => {
        const h = { ...w(c.rest.actions) };
        N(h, r, s), N(c.rest, "actions", Object.keys(h).length ? h : void 0);
      })
    },
    n.uid
  );
}
function qe(e) {
  return {
    key: `stage_${e}`,
    title: { key: O() },
    flow: "parallel",
    objectives: [],
    rest: {}
  };
}
function yt({ ctx: e, group: n, index: i }) {
  const { t, edit: r } = e, s = n.quests[i], c = (o, p) => r((y) => o(y.quests[i]), p), h = n.quests.some((o, p) => p !== i && o.key === s.key), l = s.duration !== "" && !st.test(s.duration.replace(/\s/g, "").toLowerCase()), d = Object.keys(s.rest).filter((o) => o !== "actions");
  return /* @__PURE__ */ m(U, { children: [
    /* @__PURE__ */ m("section", { className: "aq-section", children: [
      /* @__PURE__ */ a("span", { className: "aq-h", children: t("general") }),
      /* @__PURE__ */ a(M, { ctx: e, label: t("title"), node: s.title }),
      /* @__PURE__ */ a(M, { ctx: e, label: t("description"), node: s.description, multiline: !0 }),
      /* @__PURE__ */ m("div", { className: "aq-row aq-top", children: [
        /* @__PURE__ */ a(_, { label: t("tier"), children: /* @__PURE__ */ m(
          "select",
          {
            className: "select",
            value: s.tier,
            onChange: (o) => c((p) => {
              p.tier = o.target.value;
            }),
            children: [
              /* @__PURE__ */ a("option", { value: "", children: t("noTier") }),
              n.tiers.map((o) => /* @__PURE__ */ a("option", { value: o.key, children: E(e.project, o.title.key, e.lang) || o.key }, o.key)),
              s.tier && !n.tiers.some((o) => o.key === s.tier) && /* @__PURE__ */ a("option", { value: s.tier, children: s.tier })
            ]
          }
        ) }),
        /* @__PURE__ */ a(
          _,
          {
            label: t("duration"),
            hint: /* @__PURE__ */ a("span", { className: l ? "aq-err" : "aq-hint", children: t(l ? "durationInvalid" : "durationHint") }),
            children: /* @__PURE__ */ a(
              "input",
              {
                className: "input",
                value: s.duration,
                placeholder: "24h",
                onChange: (o) => c((p) => {
                  p.duration = o.target.value.trim();
                })
              }
            )
          }
        )
      ] }),
      /* @__PURE__ */ m("details", { className: "aq-fold", open: h || void 0, children: [
        /* @__PURE__ */ a("summary", { children: t("more") }),
        /* @__PURE__ */ a(
          _,
          {
            label: t("key"),
            hint: /* @__PURE__ */ a("span", { className: h ? "aq-err" : "aq-hint", children: t(h ? "keyTaken" : "questKeyHint") }),
            children: /* @__PURE__ */ a(
              "input",
              {
                className: "input aq-mono",
                value: s.key,
                onChange: (o) => c((p) => {
                  p.key = ce(o.target.value);
                }),
                onBlur: () => {
                  const o = B(s.key);
                  o !== s.key && c((p) => {
                    p.key = o;
                  });
                }
              }
            )
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ m("section", { className: "aq-section", children: [
      /* @__PURE__ */ m("div", { className: "aq-head", children: [
        /* @__PURE__ */ m("span", { className: "aq-h", children: [
          t("stages"),
          " · ",
          s.stages.length
        ] }),
        /* @__PURE__ */ m(
          H,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => c((o) => {
              const p = G(
                `stage_${o.stages.length + 1}`,
                o.stages.map((y) => y.key)
              );
              o.stages.push({ ...qe(o.stages.length + 1), key: p });
            }),
            children: [
              "+ ",
              t("addStage")
            ]
          }
        )
      ] }),
      /* @__PURE__ */ a("ol", { className: "aq-stages", children: s.stages.map((o, p) => /* @__PURE__ */ a(
        ft,
        {
          ctx: e,
          quest: s,
          stage: o,
          si: p,
          editQuest: c
        },
        `${o.key}-${p}`
      )) })
    ] }),
    /* @__PURE__ */ a(
      be,
      {
        ctx: e,
        title: t("questEvents"),
        hint: t("questEventsHint"),
        hooks: We,
        label: (o) => t(`questHook.${o}`),
        value: s.rest.actions,
        onChange: (o, p) => c((y) => {
          const g = { ...w(y.rest.actions) };
          N(g, o, p), N(y.rest, "actions", Object.keys(g).length > 0 ? g : void 0);
        }),
        footer: d.length > 0 && /* @__PURE__ */ a("span", { className: "aq-hint", children: t("kept", { fields: d.join(", ") }) })
      },
      s.uid
    ),
    /* @__PURE__ */ m("section", { className: "aq-section", children: [
      /* @__PURE__ */ a("span", { className: "aq-h", children: t("preview") }),
      /* @__PURE__ */ a(qt, { ctx: e, quest: s })
    ] })
  ] });
}
function ft({
  ctx: e,
  quest: n,
  stage: i,
  si: t,
  editQuest: r
}) {
  const { t: s } = e, [c, h] = Y(null), l = (y) => r((g) => y(g.stages[t])), d = E(e.project, i.title.key, e.lang), o = i.flow || "parallel", p = n.stages.flatMap((y) => y.objectives.map((g) => g.key));
  return /* @__PURE__ */ m("li", { className: "aq-stage", children: [
    /* @__PURE__ */ m("div", { className: "aq-stage-head", children: [
      /* @__PURE__ */ a("span", { className: "aq-stage-n", children: t + 1 }),
      /* @__PURE__ */ a("div", { className: "aq-stage-title", children: /* @__PURE__ */ a(
        re,
        {
          value: d,
          placeholder: s("stageTitle"),
          "aria-label": s("stage", { n: t + 1 }),
          onChange: (y) => e.edit(() => {
          }, [{ key: i.title.key, lang: e.lang, value: y }])
        }
      ) }),
      /* @__PURE__ */ a(
        ee,
        {
          t: s,
          index: t,
          count: n.stages.length,
          onMove: (y) => r((g) => Z(g.stages, t, y)),
          onRemove: () => r((y) => {
            y.stages.splice(t, 1);
          })
        }
      )
    ] }),
    i.objectives.length > 1 && /* @__PURE__ */ a("div", { className: "aq-seg", role: "radiogroup", "aria-label": s("flow"), children: ["parallel", "sequential"].map((y) => /* @__PURE__ */ a(
      "button",
      {
        type: "button",
        role: "radio",
        "aria-checked": o === y,
        className: `chip${o === y ? " is-active" : ""}`,
        onClick: () => l((g) => {
          g.flow = y;
        }),
        children: s(`flow.${y}`)
      },
      y
    )) }),
    i.objectives.length === 0 && /* @__PURE__ */ a("span", { className: "aq-hint", children: s("noObjectives") }),
    /* @__PURE__ */ a("ul", { className: "aq-objectives", children: i.objectives.map((y, g) => /* @__PURE__ */ a(
      gt,
      {
        t: s,
        projectId: e.projectId,
        n: o === "sequential" ? `${t + 1}.${g + 1}` : "•",
        objective: y,
        open: c === y.key,
        onToggle: () => h(c === y.key ? null : y.key),
        keyTaken: p.filter((f) => f === y.key).length > 1,
        index: g,
        count: i.objectives.length,
        onMove: (f) => l((k) => Z(k.objectives, g, f)),
        onChange: (f) => l((k) => f(k.objectives[g])),
        onRename: (f) => {
          c === y.key && h(f);
        },
        onRemove: () => l((f) => {
          f.objectives.splice(g, 1);
        })
      },
      `${y.key}-${g}`
    )) }),
    /* @__PURE__ */ m(
      H,
      {
        size: "sm",
        variant: "ghost",
        className: "aq-add",
        onClick: () => {
          const y = He("block_break", p);
          r((g) => {
            g.stages[t].objectives.push({
              key: y,
              type: "block_break",
              params: pe("block_break")
            });
          }), h(y);
        },
        children: [
          "+ ",
          s("addObjective")
        ]
      }
    )
  ] });
}
function gt({
  t: e,
  projectId: n,
  n: i,
  objective: t,
  open: r,
  onToggle: s,
  keyTaken: c,
  index: h,
  count: l,
  onMove: d,
  onChange: o,
  onRename: p,
  onRemove: y
}) {
  const g = P(t.type), f = g ? g.params : Object.keys(t.params).map((b) => ({ name: b, kind: "text" })), k = me(t.params), u = ye(t.type, t.params), v = Xe(t.type, t.params), q = c || v.length > 0, x = (b) => b.kind === "item" ? /* @__PURE__ */ a(
    vt,
    {
      t: e,
      projectId: n,
      spec: b,
      params: t.params,
      onChange: (j) => o((T) => j(T.params))
    },
    b.name
  ) : /* @__PURE__ */ a(
    bt,
    {
      t: e,
      spec: b,
      value: t.params[b.name],
      onChange: (j) => o((T) => N(T.params, b.name, j))
    },
    b.name
  );
  return /* @__PURE__ */ m("li", { className: `aq-obj${r ? " is-open" : ""}${q ? " has-problem" : ""}`, children: [
    /* @__PURE__ */ m("div", { className: "aq-obj-line", children: [
      /* @__PURE__ */ m("button", { type: "button", className: "aq-obj-main", onClick: s, "aria-expanded": r, children: [
        /* @__PURE__ */ a("span", { className: "aq-obj-n", children: i }),
        /* @__PURE__ */ a("span", { className: "aq-obj-icon", "aria-hidden": !0, children: g?.icon ?? "?" }),
        /* @__PURE__ */ m("span", { className: "aq-obj-text", children: [
          fe(e, t.type),
          k !== void 0 && /* @__PURE__ */ m("strong", { children: [
            " · ",
            String(k)
          ] }),
          u ? ` × ${u}` : k !== void 0 ? ` × ${e("any")}` : ""
        ] }),
        q && /* @__PURE__ */ a(
          "span",
          {
            className: "aq-obj-dot",
            title: c ? e("objectiveKeyTaken") : e("missing", {
              fields: v.map((b) => e(`param.${b}`)).join(", ")
            })
          }
        )
      ] }),
      /* @__PURE__ */ a(ee, { t: e, index: h, count: l, onMove: d, onRemove: y })
    ] }),
    r && /* @__PURE__ */ m("div", { className: "aq-obj-form", children: [
      /* @__PURE__ */ a(_, { label: e("type"), children: /* @__PURE__ */ m(
        "select",
        {
          className: "select",
          value: t.type,
          onChange: (b) => o((j) => {
            j.type = b.target.value, j.params = pe(j.type);
          }),
          children: [
            ue.map((b) => /* @__PURE__ */ m("option", { value: b.type, children: [
              b.icon,
              " ",
              e(`objective.${b.type}`)
            ] }, b.type)),
            !g && /* @__PURE__ */ a("option", { value: t.type, children: t.type })
          ]
        }
      ) }),
      /* @__PURE__ */ a("div", { className: "aq-row aq-wrap", children: f.filter((b) => !b.advanced).map(x) }),
      v.length > 0 && /* @__PURE__ */ a("span", { className: "aq-warn", children: e("missing", {
        fields: v.map((b) => e(`param.${b}`)).join(", ")
      }) }),
      /* @__PURE__ */ m("details", { className: "aq-fold", open: c || void 0, children: [
        /* @__PURE__ */ a("summary", { children: e("more") }),
        f.filter((b) => b.advanced).map(x),
        /* @__PURE__ */ a(
          _,
          {
            label: e("key"),
            hint: /* @__PURE__ */ a("span", { className: c ? "aq-err" : "aq-hint", children: e(c ? "objectiveKeyTaken" : "objectiveKeyHint") }),
            children: /* @__PURE__ */ a(
              "input",
              {
                className: "input aq-mono",
                value: t.key,
                onChange: (b) => {
                  const j = ce(b.target.value);
                  o((T) => {
                    T.key = j;
                  }), p(j);
                },
                onBlur: () => {
                  const b = B(t.key);
                  b !== t.key && (o((j) => {
                    j.key = b;
                  }), p(b));
                }
              }
            )
          }
        )
      ] })
    ] })
  ] });
}
function vt({
  t: e,
  projectId: n,
  spec: i,
  params: t,
  onChange: r
}) {
  const s = he(t, i), c = Array.isArray(t.match) ? t.match : void 0, h = (l, d) => r((o) => Ze(o, i, l, d));
  return /* @__PURE__ */ a(
    _,
    {
      label: e(`param.${i.name}`),
      hint: Object.keys(s).length === 0 ? /* @__PURE__ */ a("span", { className: "aq-hint", children: e("anyItem") }) : void 0,
      children: /* @__PURE__ */ a(
        $e,
        {
          projectId: n,
          value: s,
          match: c,
          onChange: (l) => h(l, c),
          onMatchChange: (l) => h(s, l)
        }
      )
    }
  );
}
function bt({
  t: e,
  spec: n,
  value: i,
  onChange: t
}) {
  const r = e(`param.${n.name}`), s = n.required && (i === void 0 || i === ""), c = typeof i == "string" || typeof i == "number" ? String(i) : "";
  if (n.kind === "int")
    return /* @__PURE__ */ a(S, { label: r, value: i, onChange: t });
  const h = s ? /* @__PURE__ */ a("span", { className: "aq-warn", children: e("required") }) : n.name === "itemKey" ? /* @__PURE__ */ a("span", { className: "aq-hint", children: e("itemKeyHint") }) : void 0;
  if (n.kind === "material")
    return /* @__PURE__ */ a(_, { label: r, hint: h, children: /* @__PURE__ */ a(
      Re,
      {
        value: c,
        placeholder: e("any"),
        "aria-label": r,
        onChange: (d) => t(d.toUpperCase().replace(/\s+/g, "_"))
      }
    ) });
  const l = n.kind === "entity";
  return /* @__PURE__ */ m(_, { label: r, hint: h, children: [
    /* @__PURE__ */ a(
      "input",
      {
        className: `input${l ? " aq-mono" : ""}`,
        list: l ? "aq-entities" : void 0,
        value: c,
        placeholder: l ? e("any") : "",
        onChange: (d) => t(l ? d.target.value.toUpperCase().replace(/\s+/g, "_") : d.target.value)
      }
    ),
    l && /* @__PURE__ */ a("datalist", { id: "aq-entities", children: Je.map((d) => /* @__PURE__ */ a("option", { value: d }, d)) })
  ] });
}
function qt({ ctx: e, quest: n }) {
  const { t: i, project: t, lang: r } = e, s = (c) => ge(i, c);
  return /* @__PURE__ */ m("div", { className: "aq-preview", children: [
    /* @__PURE__ */ a("span", { className: "aq-preview-title", children: E(t, n.title.key, r) || n.key }),
    E(t, n.description.key, r) && /* @__PURE__ */ a("span", { className: "aq-preview-line", children: E(t, n.description.key, r) }),
    n.stages.map((c, h) => (
      // biome-ignore lint/suspicious/noArrayIndexKey: stages are listed in order
      /* @__PURE__ */ m("span", { className: "aq-preview-stage", children: [
        n.stages.length > 1 && /* @__PURE__ */ a("span", { className: "aq-preview-line", children: E(t, c.title.key, r) || i("stage", { n: h + 1 }) }),
        c.objectives.map((l) => /* @__PURE__ */ m("span", { className: "aq-preview-line", children: [
          "• ",
          s(l)
        ] }, l.key))
      ] }, h)
    ))
  ] });
}
const kt = {
  kind: oe,
  dir: le,
  schemaVersion: J,
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: ne.en,
  labels: ne,
  component: dt,
  title: (e, { project: n, lang: i }) => E(n, e.title.key, i),
  create: ({ id: e, projectId: n }) => ({
    id: e,
    projectId: n,
    uid: Q(),
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
  parse: (e, n) => Pe(e, n),
  serialize: (e, n) => ze(e, n),
  ltextSites: (e) => Qe(e),
  board: {
    size: { width: 240, height: 48 },
    accent: et,
    // The group's quests, one selectable row each: the Properties panel opens the one clicked.
    items: (e, { project: n, lang: i }) => nt(e, n, i),
    summary: (e, { lang: n }) => at(e, n)
  }
}, xt = {
  id: "msquests",
  displayName: "Quests",
  schemaVersion: "quest_group/v1",
  sdk: Ce,
  sidebar: { icon: () => null, label: "Quests", to: "/m/msquests" },
  // Quests are Documents: they are edited from the project, no screen of their own.
  routes: [],
  documents: [kt]
}, _t = { ...xt, sdk: "1.5.0" };
export {
  _t as default
};
