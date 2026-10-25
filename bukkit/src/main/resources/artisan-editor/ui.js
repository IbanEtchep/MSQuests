import { createModuleI18n as xe, fieldText as je, newTextKey as C, textValue as A, injectModuleStyles as Ne, useProject as we, useContentLang as _e, Button as M, selectDocumentItem as Ae, SortableList as Y, updateDocument as Ee, useUiLang as Te, MiniMessageInput as se, ActionChainEditor as $e, ItemInput as Se, MaterialInput as Ce, SDK_VERSION as Re } from "@artisan/editor-sdk";
import { jsxs as h, jsx as r, Fragment as G } from "react/jsx-runtime";
import { useState as X, useEffect as Oe } from "react";
const Le = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"]
}, Ie = /^\d+$/;
function V(e, n, i) {
  const t = String(i).padStart(2, "0");
  return e === "fr" ? i === 0 ? `${n} h` : `${n} h ${t}` : `${String(n).padStart(2, "0")}:${t}`;
}
function E(e, n, i) {
  return Ie.test(e) && Number(e) >= n && Number(e) <= i;
}
function ne(e) {
  return e.trim().split(/\s+/).length === 5;
}
function De(e, n) {
  const i = n === "fr", t = e.trim().split(/\s+/);
  if (t.length !== 5) return null;
  const [a, s, l, y, c] = t;
  if (y !== "*") return null;
  const d = /^\*\/(\d+)$/;
  if (l === "*" && c === "*") {
    if (E(a, 0, 59) && E(s, 0, 23))
      return i ? `tous les jours à ${V(n, +s, +a)}` : `every day at ${V(n, +s, +a)}`;
    const p = d.exec(s);
    if (E(a, 0, 59) && p)
      return i ? `toutes les ${p[1]} h, à la minute ${a}` : `every ${p[1]} hours, at minute ${a}`;
    if (E(a, 0, 59) && s === "*")
      return i ? `toutes les heures, à la minute ${a}` : `every hour, at minute ${a}`;
    const b = d.exec(a);
    return b && s === "*" ? i ? `toutes les ${b[1]} minutes` : `every ${b[1]} minutes` : null;
  }
  if (!E(a, 0, 59) || !E(s, 0, 23)) return null;
  const o = V(n, +s, +a);
  if (l === "*" && E(c, 0, 7)) {
    const p = Le[i ? "fr" : "en"][Number(c) % 7];
    return i ? `chaque ${p} à ${o}` : `every ${p} at ${o}`;
  }
  return c === "*" && E(l, 1, 31) ? i ? `le ${l === "1" ? "1er" : l} de chaque mois à ${o}` : `on day ${l} of each month at ${o}` : null;
}
function I(e, n, i) {
  const t = /* @__PURE__ */ new Set();
  for (const a of e.split(",")) {
    const s = /^(\*|(\d+)(?:-(\d+))?)(?:\/(\d+))?$/.exec(a);
    if (!s) return null;
    const l = s[4] ? Number(s[4]) : 1, y = s[1] === "*" ? n : Number(s[2]), c = s[1] === "*" ? i : s[3] ? Number(s[3]) : s[4] ? i : y;
    if (l < 1 || y < n || c > i || y > c) return null;
    for (let d = y; d <= c; d += l) t.add(d);
  }
  return t;
}
function He(e, n) {
  const i = e.trim().split(/\s+/);
  if (i.length !== 5) return null;
  const t = I(i[0], 0, 59), a = I(i[1], 0, 23), s = I(i[2], 1, 31), l = I(i[3], 1, 12), y = I(i[4], 0, 7);
  if (!t || !a || !s || !l || !y) return null;
  y.has(7) && y.add(0);
  const c = i[2] === "*", d = i[4] === "*", o = (f) => c && d ? !0 : c ? y.has(f.getDay()) : d ? s.has(f.getDate()) : s.has(f.getDate()) || y.has(f.getDay()), p = new Date(n.getTime());
  p.setSeconds(0, 0), p.setMinutes(p.getMinutes() + 1);
  const b = n.getTime() + 366 * 24 * 3600 * 1e3;
  for (; p.getTime() <= b; ) {
    if (!l.has(p.getMonth() + 1) || !o(p)) {
      p.setDate(p.getDate() + 1), p.setHours(0, 0, 0, 0);
      continue;
    }
    if (!a.has(p.getHours())) {
      p.setHours(p.getHours() + 1, 0, 0, 0);
      continue;
    }
    if (t.has(p.getMinutes())) return p;
    p.setMinutes(p.getMinutes() + 1);
  }
  return null;
}
const U = {
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
}, J = {
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
}, ie = { en: U.kindLabel, fr: J.kindLabel }, Me = xe({ en: U, fr: J });
function oe(e) {
  const n = e === "fr" ? J : U;
  return (i, t) => {
    const a = n[i] ?? U[i] ?? i;
    return t ? a.replace(/\{\{(\w+)\}\}/g, (s, l) => String(t[l] ?? s)) : a;
  };
}
const le = "quest_group", ce = "quest_groups", ee = "quest_group/v1", Z = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
function Q(e = Date.now()) {
  let n = "", i = e;
  for (let s = 0; s < 10; s++)
    n = Z[i % 32] + n, i = Math.floor(i / 32);
  const t = new Uint8Array(16);
  crypto.getRandomValues(t);
  let a = "";
  for (let s = 0; s < 16; s++) a += Z[t[s] % 32];
  return n + a;
}
function z(e, n) {
  const i = new Set(n);
  if (!i.has(e)) return e;
  for (let t = 2; ; t++) if (!i.has(`${e}_${t}`)) return `${e}_${t}`;
}
function Pe(e, n) {
  const i = new Set(n), t = new Uint8Array(4);
  for (; ; ) {
    crypto.getRandomValues(t);
    const a = Array.from(t, (l) => Z[l % 32].toLowerCase()).join(""), s = `${F(e)}_${a}`;
    if (!i.has(s)) return s;
  }
}
function de(e) {
  return e.toLowerCase().replace(/[^a-z0-9_]/g, "_");
}
function F(e) {
  return e.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().replace(/[^a-z0-9_]+/g, "_").replace(/^_+|_+$/g, "") || "quest";
}
function N(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? { ...e } : {};
}
function W(e) {
  return Array.isArray(e) ? e : [];
}
function _(e) {
  return typeof e == "string" || typeof e == "number" || typeof e == "boolean" ? String(e) : "";
}
function D(e, ...n) {
  const i = { ...e };
  for (const t of n) delete i[t];
  return i;
}
function Ke(e, n) {
  const i = {}, t = [];
  function a(c) {
    if (c && typeof c == "object" && !Array.isArray(c)) {
      const p = C(), b = {};
      for (const [f, g] of Object.entries(c)) b[f] = _(g);
      return i[p] = b, { key: p };
    }
    const d = typeof c == "string" ? /^\{i18n:(.+)\}$/.exec(c) : null;
    if (d) {
      const p = n.translations?.[d[1]];
      return p && (i[d[1]] = { ...p }), { key: d[1] };
    }
    const o = C();
    return i[o] = c == null || c === "" ? {} : { [n.lang]: _(c) }, { key: o };
  }
  const s = Object.entries(N(e.tiers)).map(([c, d]) => {
    const o = N(d);
    return { key: c, title: a(o.title), rest: D(o, "title") };
  }), l = W(e.quests).map((c, d) => {
    const o = N(c), p = W(o.stages).map((b, f) => {
      const g = N(b), q = _(g.flow).toLowerCase();
      return {
        key: _(g.key) || `stage_${f + 1}`,
        title: a(g.title),
        flow: q === "parallel" || q === "sequential" ? q : "",
        objectives: W(g.objectives).map((u, v) => {
          const m = N(u);
          return {
            key: _(m.key) || `objective_${v + 1}`,
            type: _(m.type),
            params: D(m, "key", "type")
          };
        }),
        rest: D(g, "key", "title", "flow", "objectives")
      };
    });
    return {
      key: _(o.key) || `quest_${d + 1}`,
      uid: _(o.uid) || Q(),
      tier: _(o.tier),
      title: a(o.title),
      description: a(o.description),
      duration: _(o.duration),
      stages: p,
      rest: Ge(o)
    };
  }), y = {
    id: _(e.id),
    projectId: n.projectId,
    uid: _(e.uid) || Q(),
    title: a(e.title),
    description: a(e.description),
    actorType: _(e.actor_type) || "player",
    reset: N(e.reset),
    limits: N(e.limits),
    rotation: N(e.rotation),
    distribution: N(e.distribution),
    tiers: s,
    quests: l,
    rest: ue(D(
      N(e),
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
  return e.schema !== void 0 && e.schema !== ee && t.push(`schema: ${String(e.schema)}`), { doc: y, i18nKeys: i, unsupported: t };
}
function ue(e) {
  return e.on !== void 0 && (e.actions == null && (e.actions = e.on), delete e.on), e;
}
function Ge(e) {
  const n = ue(
    D(e, "key", "uid", "tier", "title", "description", "duration", "stages", "reward_display")
  );
  if (n.rewards !== void 0) {
    const i = N(n.actions);
    i.complete == null && (n.actions = { ...i, complete: n.rewards }), delete n.rewards;
  }
  return n;
}
function ze(e) {
  return typeof e == "string" ? e === "" : Object.values(e).every((n) => n === "");
}
function T(e) {
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
function Qe(e, n) {
  const i = (d) => je(n, d.key), t = (d) => {
    const o = i(d);
    return ze(o) ? void 0 : o;
  }, a = {};
  for (const d of e.tiers) a[d.key] = { ...d.rest, title: i(d.title) };
  const s = e.quests.map(
    (d) => T({
      key: d.key,
      uid: d.uid,
      tier: d.tier,
      title: i(d.title),
      description: t(d.description),
      duration: d.duration,
      ...d.rest,
      stages: d.stages.map(
        (o) => T({
          key: o.key,
          title: t(o.title),
          flow: o.flow,
          ...o.rest,
          objectives: o.objectives.map((p) => ({ key: p.key, type: p.type, ...p.params }))
        })
      )
    })
  ), l = {
    ...e.rest,
    id: e.id,
    schema: ee,
    uid: e.uid,
    title: i(e.title),
    description: t(e.description),
    actor_type: e.actorType,
    reset: T(e.reset),
    limits: T(e.limits),
    rotation: T(e.rotation),
    distribution: T(e.distribution),
    tiers: a,
    quests: s
  }, y = {};
  for (const d of Ue) d in l && (y[d] = l[d]);
  for (const d of Object.keys(l)) d in y || (y[d] = l[d]);
  const c = T(y);
  return c.quests = s, c;
}
function* Be(e) {
  const n = `${ce}.${e.id}`;
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
    const a = e.quests[t], s = i.has(a.uid) ? `${a.uid}_${t}` : a.uid;
    i.add(a.uid);
    const l = `${n}.quests.${s}`;
    yield {
      lText: a.title,
      key: `${l}.title`,
      path: `quests[${t}].title`,
      kind: "quest.title"
    }, yield {
      lText: a.description,
      key: `${l}.description`,
      path: `quests[${t}].description`,
      kind: "quest.description"
    };
    const y = /* @__PURE__ */ new Set();
    for (let c = 0; c < a.stages.length; c++) {
      const d = a.stages[c], o = y.has(d.key) ? `${d.key}_${c}` : d.key;
      y.add(d.key), yield {
        lText: d.title,
        key: `${l}.stages.${o}.title`,
        path: `quests[${t}].stages[${c}].title`,
        kind: "quest.stage"
      };
    }
  }
}
function ae(e) {
  return !!e && typeof e == "object" && !Array.isArray(e) && "type" in e;
}
function Fe(e) {
  const n = Array.isArray(e) ? e : e == null ? [] : [e];
  return {
    chain: n.filter((i) => !ae(i)),
    legacy: n.filter(ae)
  };
}
function re(e, n) {
  const i = [...e, ...n];
  return i.length ? i : void 0;
}
function Ve(e) {
  const n = String(e.type), i = e.command ?? e.message ?? e.title ?? e.material;
  return typeof i == "string" ? `${n} · ${i}` : n;
}
const We = [
  "distribution",
  "actor_load",
  "quest_start",
  "objective_progress",
  "objective_complete",
  "quest_complete",
  "period_complete"
], Ze = ["start", "objective_progress", "objective_complete", "complete"], $ = { name: "amount", kind: "int", required: !0 }, pe = [
  { type: "block_break", icon: "⛏", params: [{ name: "material", kind: "material" }, $] },
  {
    type: "craft_item",
    icon: "⚒",
    params: [{ name: "item", kind: "item", legacy: "material" }, $]
  },
  {
    type: "harvest_crop",
    icon: "🌾",
    params: [{ name: "crop", kind: "material", required: !0 }, $]
  },
  {
    type: "fishing",
    icon: "🎣",
    params: [{ name: "item", kind: "item", legacy: "fish_type" }, $]
  },
  {
    type: "deliver_item",
    icon: "📦",
    params: [
      { name: "item", kind: "item", legacy: "material" },
      $,
      { name: "itemKey", kind: "text", advanced: !0 }
    ]
  },
  { type: "kill_entity", icon: "⚔", params: [{ name: "entity_type", kind: "entity" }, $] },
  { type: "breed_animal", icon: "♥", params: [{ name: "entity_type", kind: "entity" }, $] },
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
  return pe.find((n) => n.type === e);
}
function me(e) {
  const n = {};
  for (const i of P(e)?.params ?? [])
    i.kind === "int" && (n[i.name] = 1);
  return n;
}
function he(e) {
  return e.amount ?? e.distance;
}
function ye(e, n) {
  const i = e[n.name];
  if (i && typeof i == "object" && !Array.isArray(i)) return i;
  const t = n.legacy ? e[n.legacy] : void 0;
  return typeof t == "string" && t !== "" ? { material: t } : {};
}
function Ye(e, n, i, t) {
  n.legacy && delete e[n.legacy];
  const a = !i || Object.keys(i).length === 0;
  a ? delete e[n.name] : e[n.name] = i, a || t === void 0 ? delete e.match : e.match = t;
}
function Xe(e) {
  const n = e.title;
  if (typeof n == "string" && n.trim() !== "") return n.replace(/<[^>]*>/g, "");
  const i = e.material ?? e.item;
  return typeof i == "string" ? i : "";
}
function fe(e, n) {
  const i = P(e);
  return i ? i.params.filter((t) => t.kind !== "int").map((t) => t.kind === "item" ? Xe(ye(n, t)) : n[t.name]).filter((t) => typeof t == "string" && t !== "").join(" = ") : Object.keys(n).map((t) => n[t]).filter((t) => typeof t == "string" && t !== "").join(" = ");
}
function Je(e, n) {
  return (P(e)?.params ?? []).filter((i) => i.required && (n[i.name] === void 0 || n[i.name] === "")).map((i) => i.name);
}
const et = [
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
], tt = "#f59e0b";
function ge(e, n) {
  return P(n) ? e(`objective.${n}`) : n;
}
function B(e, n) {
  const i = fe(n.type, n.params), t = he(n.params);
  return `${ge(e, n.type)}${i ? ` · ${i}` : ""}${t !== void 0 ? ` · 0/${String(t)}` : ""}`;
}
function ve(e) {
  return e.replace(/<[^<>]*>/g, "").trim();
}
function nt(e) {
  const n = e.distribution?.strategy;
  return typeof n == "string" && n.toLowerCase() === "sequential";
}
function it(e, n, i) {
  const t = oe(i), a = nt(e);
  return e.quests.map((s, l) => {
    const y = A(n, s.title.key, i);
    return {
      id: s.uid,
      label: ve(y) || s.key,
      detail: s.tier || void 0,
      caption: at(t, s),
      marker: a ? String(l + 1) : void 0,
      hover: {
        title: y || s.key,
        text: A(n, s.description.key, i) || void 0,
        lines: rt(t, s, n, i)
      }
    };
  });
}
function at(e, n) {
  const i = n.stages.flatMap((s) => s.objectives), t = i[0];
  if (!t) return;
  const a = i.length - 1;
  return a > 0 ? `${B(e, t)} +${String(a)}` : B(e, t);
}
function rt(e, n, i, t) {
  const a = n.stages.length > 1;
  return n.stages.flatMap(
    (s, l) => s.objectives.map((y) => {
      const c = B(e, y);
      return a ? `<gray>${ve(A(i, s.title.key, t)) || e("stage", { n: l + 1 })} ·</gray> ${c}` : c;
    })
  );
}
function st(e, n) {
  const i = oe(n), t = typeof e.distribution?.strategy == "string" ? e.distribution.strategy.toLowerCase() : "", a = i("questCount", { count: e.quests.length });
  return ["none", "sequential", "random"].includes(t) ? `${a} · ${i(`strategy.${t}`).toLowerCase()}` : a;
}
const ot = `
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
`, lt = /^(\d+|(\d+[dhms])+)$/, ct = ["actor_load", "period_reset", "quest_complete"], dt = (() => {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
  } catch {
    return "";
  }
})();
function ut(e, n) {
  const i = He(e, /* @__PURE__ */ new Date());
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
function pt({ projectId: e, documentId: n, document: i, item: t }) {
  Ne("msquests", ot);
  const a = i, s = we(e), l = _e(), y = Me(), [c, d] = X(t ?? null);
  Oe(() => {
    d(t ?? null);
  }, [t]);
  const o = (g) => {
    d(g), Ae({ documentId: n, item: g });
  }, b = { projectId: e, project: s, lang: l, t: y, edit: (g, q) => {
    const u = structuredClone(a);
    g(u), Ee({ projectId: e, kind: le, id: n, doc: u, texts: q });
  } }, f = c ? a.quests.findIndex((g) => g.uid === c) : -1;
  return f >= 0 ? /* @__PURE__ */ h("div", { className: "aq", children: [
    /* @__PURE__ */ h(M, { variant: "ghost", size: "sm", className: "aq-back", onClick: () => o(null), children: [
      "← ",
      y("back")
    ] }),
    /* @__PURE__ */ r(gt, { ctx: b, group: a, index: f })
  ] }) : /* @__PURE__ */ r("div", { className: "aq", children: /* @__PURE__ */ r(ht, { ctx: b, group: a, onOpen: o }) });
}
function w({
  label: e,
  children: n,
  hint: i
}) {
  return (
    // biome-ignore lint/a11y/noLabelWithoutControl: the control is the child
    /* @__PURE__ */ h("label", { className: "aq-field", children: [
      /* @__PURE__ */ r("span", { className: "field-label", children: e }),
      n,
      i
    ] })
  );
}
function H({
  ctx: e,
  label: n,
  node: i,
  multiline: t
}) {
  const a = A(e.project, i.key, e.lang), s = (l) => e.edit(() => {
  }, [{ key: i.key, lang: e.lang, value: l }]);
  return /* @__PURE__ */ r(w, { label: `${n} · ${e.lang}`, children: /* @__PURE__ */ r(se, { value: a, onChange: s, multiline: t, "aria-label": n }) });
}
function mt(e) {
  return typeof e == "number" ? String(e) : typeof e == "string" ? e : "";
}
function S({
  label: e,
  value: n,
  onChange: i,
  placeholder: t
}) {
  return /* @__PURE__ */ r(w, { label: e, children: /* @__PURE__ */ r(
    "input",
    {
      className: "input",
      type: "number",
      min: 0,
      step: 1,
      value: mt(n),
      placeholder: t,
      onChange: (a) => {
        const s = a.target.value.trim();
        i(s === "" ? void 0 : Math.max(0, Math.trunc(Number(s))));
      }
    }
  ) });
}
function j(e, n, i) {
  i === void 0 || i === "" ? delete e[n] : e[n] = i;
}
function te(e, n, i) {
  if (i < 0 || i >= e.length) return;
  const [t] = e.splice(n, 1);
  e.splice(i, 0, t);
}
function R({
  label: e,
  onClick: n,
  disabled: i,
  children: t
}) {
  return /* @__PURE__ */ r(
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
function be(e) {
  const n = /* @__PURE__ */ new Set();
  return e.map((i, t) => {
    const a = n.has(i) ? `${i}#${t}` : i;
    return n.add(i), a;
  });
}
function ht({
  ctx: e,
  group: n,
  onOpen: i
}) {
  const { t, edit: a, project: s, lang: l } = e, y = Te(), c = typeof n.reset.cron == "string" ? n.reset.cron : "", d = c ? De(c, y) : null, o = typeof n.reset.timezone == "string" ? n.reset.timezone : "", p = c && ne(c) ? ut(c, y) : null, b = Array.isArray(n.distribution.triggers) ? n.distribution.triggers : [], f = n.distribution.tiers ?? {}, g = Object.keys(n.rest).filter((u) => u !== "actions"), q = () => {
    const u = C(), v = Q();
    a(
      (m) => {
        m.quests.push({
          key: z(
            "quest",
            m.quests.map((k) => k.key)
          ),
          uid: v,
          tier: "",
          title: { key: u },
          description: { key: C() },
          duration: "",
          stages: [ke(1)],
          rest: {}
        });
      },
      [{ key: u, lang: l, value: t("newQuestTitle") }]
    ), i(v);
  };
  return /* @__PURE__ */ h(G, { children: [
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ r("span", { className: "aq-h", children: t("group") }),
      /* @__PURE__ */ r(H, { ctx: e, label: t("title"), node: n.title }),
      /* @__PURE__ */ r(H, { ctx: e, label: t("description"), node: n.description, multiline: !0 }),
      /* @__PURE__ */ r(w, { label: t("actorType"), children: /* @__PURE__ */ h(
        "select",
        {
          className: "select",
          value: n.actorType,
          onChange: (u) => a((v) => {
            v.actorType = u.target.value;
          }),
          children: [
            /* @__PURE__ */ r("option", { value: "player", children: t("actor.player") }),
            /* @__PURE__ */ r("option", { value: "global", children: t("actor.global") }),
            !["player", "global"].includes(n.actorType) && /* @__PURE__ */ r("option", { value: n.actorType, children: n.actorType })
          ]
        }
      ) })
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ r("span", { className: "aq-h", children: t("reset") }),
      /* @__PURE__ */ r(
        w,
        {
          label: t("cron"),
          hint: c ? ne(c) ? /* @__PURE__ */ h("span", { className: "aq-ok", children: [
            d ?? t("cronRaw"),
            o && o !== dt ? `, ${o}` : p ? ` · ${t("nextRun", { when: p })}` : ""
          ] }) : /* @__PURE__ */ r("span", { className: "aq-err", children: t("cronInvalid") }) : /* @__PURE__ */ r("span", { className: "aq-hint", children: t("noReset") }),
          children: /* @__PURE__ */ r(
            "input",
            {
              className: "input",
              value: c,
              placeholder: "0 4 * * *",
              onChange: (u) => a((v) => j(v.reset, "cron", u.target.value))
            }
          )
        }
      )
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ r("span", { className: "aq-h", children: t("distribution") }),
      /* @__PURE__ */ h("div", { className: "aq-row", children: [
        /* @__PURE__ */ r(w, { label: t("strategy"), children: /* @__PURE__ */ h(
          "select",
          {
            className: "select",
            value: typeof n.distribution.strategy == "string" ? n.distribution.strategy.toLowerCase() : "",
            onChange: (u) => a((v) => j(v.distribution, "strategy", u.target.value)),
            children: [
              /* @__PURE__ */ r("option", { value: "", children: "—" }),
              ["none", "sequential", "random"].map((u) => /* @__PURE__ */ r("option", { value: u, children: t(`strategy.${u}`) }, u))
            ]
          }
        ) }),
        /* @__PURE__ */ r(
          S,
          {
            label: t("amount"),
            value: n.distribution.amount,
            onChange: (u) => a((v) => j(v.distribution, "amount", u))
          }
        )
      ] }),
      /* @__PURE__ */ r("span", { className: "field-label", children: t("triggers") }),
      ct.map((u) => /* @__PURE__ */ h("label", { className: "aq-check", children: [
        /* @__PURE__ */ r(
          "input",
          {
            type: "checkbox",
            checked: b.includes(u),
            onChange: (v) => a((m) => {
              const k = Array.isArray(m.distribution.triggers) ? m.distribution.triggers : [], x = v.target.checked ? [...k, u] : k.filter((O) => O !== u);
              j(m.distribution, "triggers", x.length ? x : void 0);
            })
          }
        ),
        t(`trigger.${u}`)
      ] }, u)),
      n.tiers.length > 0 && /* @__PURE__ */ h(G, { children: [
        /* @__PURE__ */ r("span", { className: "field-label", children: t("perTier") }),
        /* @__PURE__ */ r("div", { className: "aq-row", children: n.tiers.map((u) => /* @__PURE__ */ r(
          S,
          {
            label: A(s, u.title.key, l) || u.key,
            value: f[u.key],
            onChange: (v) => a((m) => {
              const k = { ...m.distribution.tiers ?? {} };
              j(k, u.key, v), j(
                m.distribution,
                "tiers",
                Object.keys(k).length ? k : void 0
              );
            })
          },
          u.key
        )) })
      ] })
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ r("span", { className: "aq-h", children: t("limits") }),
      /* @__PURE__ */ h("div", { className: "aq-row", children: [
        /* @__PURE__ */ r(
          S,
          {
            label: t("maxActive"),
            value: n.limits.max_active,
            onChange: (u) => a((v) => j(v.limits, "max_active", u))
          }
        ),
        /* @__PURE__ */ r(
          S,
          {
            label: t("questsPerPeriod"),
            placeholder: t("noLimit"),
            value: n.limits.max_per_period,
            onChange: (u) => a((v) => j(v.limits, "max_per_period", u))
          }
        )
      ] }),
      /* @__PURE__ */ r("span", { className: "aq-h", children: t("rotation") }),
      /* @__PURE__ */ h("div", { className: "aq-row", children: [
        /* @__PURE__ */ h("label", { className: "aq-check", children: [
          /* @__PURE__ */ r(
            "input",
            {
              type: "checkbox",
              checked: n.rotation.enabled === !0,
              onChange: (u) => a((v) => j(v.rotation, "enabled", u.target.checked ? !0 : void 0))
            }
          ),
          t("rotationEnabled")
        ] }),
        n.rotation.enabled === !0 ? /* @__PURE__ */ r(
          S,
          {
            label: t("rotationsPerPeriod"),
            placeholder: t("noLimit"),
            value: n.rotation.max_per_period,
            onChange: (u) => a((v) => j(v.rotation, "max_per_period", u))
          }
        ) : null
      ] })
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ h("div", { className: "aq-head", children: [
        /* @__PURE__ */ r("span", { className: "aq-h", children: t("tiers") }),
        /* @__PURE__ */ h(
          M,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => a((u) => {
              u.tiers.push({
                key: z(
                  "tier",
                  u.tiers.map((v) => v.key)
                ),
                title: { key: C() },
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
      n.tiers.map((u, v) => /* @__PURE__ */ h("div", { className: "aq-row", children: [
        /* @__PURE__ */ r(w, { label: t("tierKey"), children: /* @__PURE__ */ r(
          "input",
          {
            className: "input",
            defaultValue: u.key,
            onBlur: (m) => {
              const k = F(m.target.value);
              k !== u.key && a((x) => {
                const O = z(
                  k,
                  x.tiers.map((K) => K.key)
                );
                x.tiers[v].key = O;
                for (const K of x.quests) K.tier === u.key && (K.tier = O);
                const L = x.distribution.tiers;
                L && u.key in L && (L[O] = L[u.key], delete L[u.key]);
              });
            }
          }
        ) }),
        /* @__PURE__ */ r(H, { ctx: e, label: t("title"), node: u.title }),
        /* @__PURE__ */ r(
          R,
          {
            label: t("remove"),
            onClick: () => a((m) => {
              m.tiers.splice(v, 1);
            }),
            children: "×"
          }
        )
      ] }, u.key))
    ] }),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ h("div", { className: "aq-head", children: [
        /* @__PURE__ */ h("span", { className: "aq-h", children: [
          t("quests"),
          " · ",
          n.quests.length
        ] }),
        /* @__PURE__ */ h(M, { size: "sm", variant: "primary", onClick: q, children: [
          "+ ",
          t("addQuest")
        ] })
      ] }),
      n.quests.length === 0 && /* @__PURE__ */ r("span", { className: "aq-hint", children: t("noQuests") }),
      /* @__PURE__ */ r(
        Y,
        {
          ids: n.quests.map((u) => u.uid),
          onMove: (u, v) => a((m) => te(m.quests, u, v)),
          className: "aq-list",
          itemClassName: "aq-item",
          children: (u, v) => {
            const m = n.quests[u];
            return /* @__PURE__ */ h(G, { children: [
              v,
              /* @__PURE__ */ h(
                "button",
                {
                  type: "button",
                  className: "aq-item-main",
                  onClick: () => i(m.uid),
                  title: t("open"),
                  children: [
                    /* @__PURE__ */ r("span", { className: "aq-item-title", children: A(s, m.title.key, l) || m.key }),
                    /* @__PURE__ */ h("span", { className: "aq-item-sub", children: [
                      t("questSummary", {
                        stages: m.stages.length,
                        objectives: m.stages.reduce((k, x) => k + x.objectives.length, 0)
                      }),
                      " · ",
                      m.key,
                      m.tier ? ` · ${m.tier}` : "",
                      m.duration ? ` · ${m.duration}` : ""
                    ] })
                  ]
                }
              ),
              /* @__PURE__ */ r(
                R,
                {
                  label: t("remove"),
                  onClick: () => a((k) => {
                    k.quests.splice(u, 1);
                  }),
                  children: "×"
                }
              )
            ] });
          }
        }
      ),
      g.length > 0 && /* @__PURE__ */ r("span", { className: "aq-hint", children: t("kept", { fields: g.join(", ") }) })
    ] }),
    /* @__PURE__ */ r(ft, { ctx: e, group: n })
  ] });
}
function yt({
  ctx: e,
  value: n,
  onChange: i
}) {
  const { chain: t, legacy: a } = Fe(n);
  return /* @__PURE__ */ h("div", { className: "aq-chain", children: [
    /* @__PURE__ */ r(
      $e,
      {
        projectId: e.projectId,
        value: t,
        onChange: (s) => i(re(s, a))
      }
    ),
    a.length > 0 && /* @__PURE__ */ h("div", { className: "aq-legacy", children: [
      /* @__PURE__ */ r("span", { className: "aq-hint", children: e.t("legacySteps") }),
      a.map((s, l) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: a step has no identity
        /* @__PURE__ */ h("div", { className: "aq-legacy-row", children: [
          /* @__PURE__ */ r("code", { children: Ve(s) }),
          /* @__PURE__ */ r(
            R,
            {
              label: e.t("remove"),
              onClick: () => i(re(t, a.filter((y, c) => c !== l))),
              children: "×"
            }
          )
        ] }, l)
      ))
    ] })
  ] });
}
function qe({
  ctx: e,
  title: n,
  hint: i,
  hooks: t,
  label: a,
  value: s,
  onChange: l,
  footer: y
}) {
  const { t: c } = e, d = N(s), [o, p] = X([]), b = t.filter((g) => d[g] != null || o.includes(g)), f = t.filter((g) => !b.includes(g));
  return /* @__PURE__ */ h("section", { className: "aq-section", children: [
    /* @__PURE__ */ r("span", { className: "aq-h", children: n }),
    /* @__PURE__ */ r("span", { className: "aq-hint", children: i }),
    b.map((g) => /* @__PURE__ */ h("div", { className: "aq-event", children: [
      /* @__PURE__ */ h("div", { className: "aq-head", children: [
        /* @__PURE__ */ r("span", { className: "aq-event-name", children: a(g) }),
        /* @__PURE__ */ r(
          R,
          {
            label: c("removeEvent"),
            onClick: () => {
              p((q) => q.filter((u) => u !== g)), l(g, void 0);
            },
            children: "×"
          }
        )
      ] }),
      /* @__PURE__ */ r(yt, { ctx: e, value: d[g], onChange: (q) => l(g, q) })
    ] }, g)),
    f.length > 0 && /* @__PURE__ */ h(
      "select",
      {
        className: "select aq-event-add",
        "aria-label": c("addEvent"),
        value: "",
        onChange: (g) => {
          const q = f.find((u) => u === g.target.value);
          q && p((u) => [...u, q]);
        },
        children: [
          /* @__PURE__ */ r("option", { value: "", children: c("addEvent") }),
          f.map((g) => /* @__PURE__ */ r("option", { value: g, children: a(g) }, g))
        ]
      }
    ),
    y
  ] });
}
function ft({ ctx: e, group: n }) {
  const { t: i, edit: t } = e;
  return /* @__PURE__ */ r(
    qe,
    {
      ctx: e,
      title: i("hooks"),
      hint: i("hooksHint"),
      hooks: We,
      label: (a) => i(`hook.${a}`),
      value: n.rest.actions,
      onChange: (a, s) => t((l) => {
        const y = { ...N(l.rest.actions) };
        j(y, a, s), j(l.rest, "actions", Object.keys(y).length ? y : void 0);
      })
    },
    n.uid
  );
}
function ke(e) {
  return {
    key: `stage_${e}`,
    title: { key: C() },
    flow: "parallel",
    objectives: [],
    rest: {}
  };
}
function gt({ ctx: e, group: n, index: i }) {
  const { t, edit: a } = e, s = n.quests[i], l = (o, p) => a((b) => o(b.quests[i]), p), y = n.quests.some((o, p) => p !== i && o.key === s.key), c = s.duration !== "" && !lt.test(s.duration.replace(/\s/g, "").toLowerCase()), d = Object.keys(s.rest).filter((o) => o !== "actions");
  return /* @__PURE__ */ h(G, { children: [
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ r("span", { className: "aq-h", children: t("general") }),
      /* @__PURE__ */ r(H, { ctx: e, label: t("title"), node: s.title }),
      /* @__PURE__ */ r(H, { ctx: e, label: t("description"), node: s.description, multiline: !0 }),
      /* @__PURE__ */ h("div", { className: "aq-row aq-top", children: [
        /* @__PURE__ */ r(w, { label: t("tier"), children: /* @__PURE__ */ h(
          "select",
          {
            className: "select",
            value: s.tier,
            onChange: (o) => l((p) => {
              p.tier = o.target.value;
            }),
            children: [
              /* @__PURE__ */ r("option", { value: "", children: t("noTier") }),
              n.tiers.map((o) => /* @__PURE__ */ r("option", { value: o.key, children: A(e.project, o.title.key, e.lang) || o.key }, o.key)),
              s.tier && !n.tiers.some((o) => o.key === s.tier) && /* @__PURE__ */ r("option", { value: s.tier, children: s.tier })
            ]
          }
        ) }),
        /* @__PURE__ */ r(
          w,
          {
            label: t("duration"),
            hint: /* @__PURE__ */ r("span", { className: c ? "aq-err" : "aq-hint", children: t(c ? "durationInvalid" : "durationHint") }),
            children: /* @__PURE__ */ r(
              "input",
              {
                className: "input",
                value: s.duration,
                placeholder: "24h",
                onChange: (o) => l((p) => {
                  p.duration = o.target.value.trim();
                })
              }
            )
          }
        )
      ] }),
      /* @__PURE__ */ h("details", { className: "aq-fold", open: y || void 0, children: [
        /* @__PURE__ */ r("summary", { children: t("more") }),
        /* @__PURE__ */ r(
          w,
          {
            label: t("key"),
            hint: /* @__PURE__ */ r("span", { className: y ? "aq-err" : "aq-hint", children: t(y ? "keyTaken" : "questKeyHint") }),
            children: /* @__PURE__ */ r(
              "input",
              {
                className: "input aq-mono",
                value: s.key,
                onChange: (o) => l((p) => {
                  p.key = de(o.target.value);
                }),
                onBlur: () => {
                  const o = F(s.key);
                  o !== s.key && l((p) => {
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
          t("stages"),
          " · ",
          s.stages.length
        ] }),
        /* @__PURE__ */ h(
          M,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => l((o) => {
              const p = z(
                `stage_${o.stages.length + 1}`,
                o.stages.map((b) => b.key)
              );
              o.stages.push({ ...ke(o.stages.length + 1), key: p });
            }),
            children: [
              "+ ",
              t("addStage")
            ]
          }
        )
      ] }),
      /* @__PURE__ */ r(
        Y,
        {
          ids: be(s.stages.map((o) => o.key)),
          onMove: (o, p) => l((b) => te(b.stages, o, p)),
          className: "aq-stages",
          children: (o, p) => /* @__PURE__ */ r(
            vt,
            {
              ctx: e,
              quest: s,
              stage: s.stages[o],
              si: o,
              handle: p,
              editQuest: l
            }
          )
        }
      )
    ] }),
    /* @__PURE__ */ r(
      qe,
      {
        ctx: e,
        title: t("questEvents"),
        hint: t("questEventsHint"),
        hooks: Ze,
        label: (o) => t(`questHook.${o}`),
        value: s.rest.actions,
        onChange: (o, p) => l((b) => {
          const f = { ...N(b.rest.actions) };
          j(f, o, p), j(b.rest, "actions", Object.keys(f).length > 0 ? f : void 0);
        }),
        footer: d.length > 0 && /* @__PURE__ */ r("span", { className: "aq-hint", children: t("kept", { fields: d.join(", ") }) })
      },
      s.uid
    ),
    /* @__PURE__ */ h("section", { className: "aq-section", children: [
      /* @__PURE__ */ r("span", { className: "aq-h", children: t("preview") }),
      /* @__PURE__ */ r(xt, { ctx: e, quest: s })
    ] })
  ] });
}
function vt({
  ctx: e,
  quest: n,
  stage: i,
  si: t,
  handle: a,
  editQuest: s
}) {
  const { t: l } = e, [y, c] = X(null), d = (f) => s((g) => f(g.stages[t])), o = A(e.project, i.title.key, e.lang), p = i.flow || "parallel", b = n.stages.flatMap((f) => f.objectives.map((g) => g.key));
  return /* @__PURE__ */ h("div", { className: "aq-stage", children: [
    /* @__PURE__ */ h("div", { className: "aq-stage-head", children: [
      a,
      /* @__PURE__ */ r("span", { className: "aq-stage-n", children: t + 1 }),
      /* @__PURE__ */ r("div", { className: "aq-stage-title", children: /* @__PURE__ */ r(
        se,
        {
          value: o,
          placeholder: l("stageTitle"),
          "aria-label": l("stage", { n: t + 1 }),
          onChange: (f) => e.edit(() => {
          }, [{ key: i.title.key, lang: e.lang, value: f }])
        }
      ) }),
      /* @__PURE__ */ r(
        R,
        {
          label: l("remove"),
          onClick: () => s((f) => {
            f.stages.splice(t, 1);
          }),
          children: "×"
        }
      )
    ] }),
    i.objectives.length > 1 && /* @__PURE__ */ r("div", { className: "aq-seg", role: "radiogroup", "aria-label": l("flow"), children: ["parallel", "sequential"].map((f) => /* @__PURE__ */ r(
      "button",
      {
        type: "button",
        role: "radio",
        "aria-checked": p === f,
        className: `chip${p === f ? " is-active" : ""}`,
        onClick: () => d((g) => {
          g.flow = f;
        }),
        children: l(`flow.${f}`)
      },
      f
    )) }),
    i.objectives.length === 0 && /* @__PURE__ */ r("span", { className: "aq-hint", children: l("noObjectives") }),
    /* @__PURE__ */ r(
      Y,
      {
        ids: be(i.objectives.map((f) => f.key)),
        onMove: (f, g) => d((q) => te(q.objectives, f, g)),
        className: "aq-objectives",
        children: (f, g) => {
          const q = i.objectives[f];
          return /* @__PURE__ */ r(
            bt,
            {
              t: l,
              projectId: e.projectId,
              n: p === "sequential" ? `${t + 1}.${f + 1}` : "•",
              objective: q,
              open: y === q.key,
              onToggle: () => c(y === q.key ? null : q.key),
              keyTaken: b.filter((u) => u === q.key).length > 1,
              handle: g,
              onChange: (u) => d((v) => u(v.objectives[f])),
              onRename: (u) => {
                y === q.key && c(u);
              },
              onRemove: () => d((u) => {
                u.objectives.splice(f, 1);
              })
            }
          );
        }
      }
    ),
    /* @__PURE__ */ h(
      M,
      {
        size: "sm",
        variant: "ghost",
        className: "aq-add",
        onClick: () => {
          const f = Pe("block_break", b);
          s((g) => {
            g.stages[t].objectives.push({
              key: f,
              type: "block_break",
              params: me("block_break")
            });
          }), c(f);
        },
        children: [
          "+ ",
          l("addObjective")
        ]
      }
    )
  ] });
}
function bt({
  t: e,
  projectId: n,
  n: i,
  objective: t,
  open: a,
  onToggle: s,
  keyTaken: l,
  handle: y,
  onChange: c,
  onRename: d,
  onRemove: o
}) {
  const p = P(t.type), b = p ? p.params : Object.keys(t.params).map((m) => ({ name: m, kind: "text" })), f = he(t.params), g = fe(t.type, t.params), q = Je(t.type, t.params), u = l || q.length > 0, v = (m) => m.kind === "item" ? /* @__PURE__ */ r(
    qt,
    {
      t: e,
      projectId: n,
      spec: m,
      params: t.params,
      onChange: (k) => c((x) => k(x.params))
    },
    m.name
  ) : /* @__PURE__ */ r(
    kt,
    {
      t: e,
      spec: m,
      value: t.params[m.name],
      onChange: (k) => c((x) => j(x.params, m.name, k))
    },
    m.name
  );
  return /* @__PURE__ */ h("div", { className: `aq-obj${a ? " is-open" : ""}${u ? " has-problem" : ""}`, children: [
    /* @__PURE__ */ h("div", { className: "aq-obj-line", children: [
      y,
      /* @__PURE__ */ h("button", { type: "button", className: "aq-obj-main", onClick: s, "aria-expanded": a, children: [
        /* @__PURE__ */ r("span", { className: "aq-obj-n", children: i }),
        /* @__PURE__ */ r("span", { className: "aq-obj-icon", "aria-hidden": !0, children: p?.icon ?? "?" }),
        /* @__PURE__ */ h("span", { className: "aq-obj-text", children: [
          ge(e, t.type),
          f !== void 0 && /* @__PURE__ */ h("strong", { children: [
            " · ",
            String(f)
          ] }),
          g ? ` × ${g}` : f !== void 0 ? ` × ${e("any")}` : ""
        ] }),
        u && /* @__PURE__ */ r(
          "span",
          {
            className: "aq-obj-dot",
            title: l ? e("objectiveKeyTaken") : e("missing", {
              fields: q.map((m) => e(`param.${m}`)).join(", ")
            })
          }
        )
      ] }),
      /* @__PURE__ */ r(R, { label: e("remove"), onClick: o, children: "×" })
    ] }),
    a && /* @__PURE__ */ h("div", { className: "aq-obj-form", children: [
      /* @__PURE__ */ r(w, { label: e("type"), children: /* @__PURE__ */ h(
        "select",
        {
          className: "select",
          value: t.type,
          onChange: (m) => c((k) => {
            k.type = m.target.value, k.params = me(k.type);
          }),
          children: [
            pe.map((m) => /* @__PURE__ */ h("option", { value: m.type, children: [
              m.icon,
              " ",
              e(`objective.${m.type}`)
            ] }, m.type)),
            !p && /* @__PURE__ */ r("option", { value: t.type, children: t.type })
          ]
        }
      ) }),
      /* @__PURE__ */ r("div", { className: "aq-row aq-wrap", children: b.filter((m) => !m.advanced).map(v) }),
      q.length > 0 && /* @__PURE__ */ r("span", { className: "aq-warn", children: e("missing", {
        fields: q.map((m) => e(`param.${m}`)).join(", ")
      }) }),
      /* @__PURE__ */ h("details", { className: "aq-fold", open: l || void 0, children: [
        /* @__PURE__ */ r("summary", { children: e("more") }),
        b.filter((m) => m.advanced).map(v),
        /* @__PURE__ */ r(
          w,
          {
            label: e("key"),
            hint: /* @__PURE__ */ r("span", { className: l ? "aq-err" : "aq-hint", children: e(l ? "objectiveKeyTaken" : "objectiveKeyHint") }),
            children: /* @__PURE__ */ r(
              "input",
              {
                className: "input aq-mono",
                value: t.key,
                onChange: (m) => {
                  const k = de(m.target.value);
                  c((x) => {
                    x.key = k;
                  }), d(k);
                },
                onBlur: () => {
                  const m = F(t.key);
                  m !== t.key && (c((k) => {
                    k.key = m;
                  }), d(m));
                }
              }
            )
          }
        )
      ] })
    ] })
  ] });
}
function qt({
  t: e,
  projectId: n,
  spec: i,
  params: t,
  onChange: a
}) {
  const s = ye(t, i), l = Array.isArray(t.match) ? t.match : void 0, y = (c, d) => a((o) => Ye(o, i, c, d));
  return /* @__PURE__ */ r(
    w,
    {
      label: e(`param.${i.name}`),
      hint: Object.keys(s).length === 0 ? /* @__PURE__ */ r("span", { className: "aq-hint", children: e("anyItem") }) : void 0,
      children: /* @__PURE__ */ r(
        Se,
        {
          projectId: n,
          value: s,
          match: l,
          onChange: (c) => y(c, l),
          onMatchChange: (c) => y(s, c)
        }
      )
    }
  );
}
function kt({
  t: e,
  spec: n,
  value: i,
  onChange: t
}) {
  const a = e(`param.${n.name}`), s = n.required && (i === void 0 || i === ""), l = typeof i == "string" || typeof i == "number" ? String(i) : "";
  if (n.kind === "int")
    return /* @__PURE__ */ r(S, { label: a, value: i, onChange: t });
  const y = s ? /* @__PURE__ */ r("span", { className: "aq-warn", children: e("required") }) : n.name === "itemKey" ? /* @__PURE__ */ r("span", { className: "aq-hint", children: e("itemKeyHint") }) : void 0;
  if (n.kind === "material")
    return /* @__PURE__ */ r(w, { label: a, hint: y, children: /* @__PURE__ */ r(
      Ce,
      {
        value: l,
        placeholder: e("any"),
        "aria-label": a,
        onChange: (d) => t(d.toUpperCase().replace(/\s+/g, "_"))
      }
    ) });
  const c = n.kind === "entity";
  return /* @__PURE__ */ h(w, { label: a, hint: y, children: [
    /* @__PURE__ */ r(
      "input",
      {
        className: `input${c ? " aq-mono" : ""}`,
        list: c ? "aq-entities" : void 0,
        value: l,
        placeholder: c ? e("any") : "",
        onChange: (d) => t(c ? d.target.value.toUpperCase().replace(/\s+/g, "_") : d.target.value)
      }
    ),
    c && /* @__PURE__ */ r("datalist", { id: "aq-entities", children: et.map((d) => /* @__PURE__ */ r("option", { value: d }, d)) })
  ] });
}
function xt({ ctx: e, quest: n }) {
  const { t: i, project: t, lang: a } = e, s = (l) => B(i, l);
  return /* @__PURE__ */ h("div", { className: "aq-preview", children: [
    /* @__PURE__ */ r("span", { className: "aq-preview-title", children: A(t, n.title.key, a) || n.key }),
    A(t, n.description.key, a) && /* @__PURE__ */ r("span", { className: "aq-preview-line", children: A(t, n.description.key, a) }),
    n.stages.map((l, y) => (
      // biome-ignore lint/suspicious/noArrayIndexKey: stages are listed in order
      /* @__PURE__ */ h("span", { className: "aq-preview-stage", children: [
        n.stages.length > 1 && /* @__PURE__ */ r("span", { className: "aq-preview-line", children: A(t, l.title.key, a) || i("stage", { n: y + 1 }) }),
        l.objectives.map((c) => /* @__PURE__ */ h("span", { className: "aq-preview-line", children: [
          "• ",
          s(c)
        ] }, c.key))
      ] }, y)
    ))
  ] });
}
const jt = {
  kind: le,
  dir: ce,
  schemaVersion: ee,
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: ie.en,
  labels: ie,
  component: pt,
  title: (e, { project: n, lang: i }) => A(n, e.title.key, i),
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
  parse: (e, n) => Ke(e, n),
  serialize: (e, n) => Qe(e, n),
  ltextSites: (e) => Be(e),
  board: {
    size: { width: 300, height: 48 },
    accent: tt,
    // The group's quests, one selectable row each: the Properties panel opens the one clicked.
    items: (e, { project: n, lang: i }) => it(e, n, i),
    summary: (e, { lang: n }) => st(e, n)
  }
}, Nt = {
  id: "msquests",
  displayName: "Quests",
  schemaVersion: "quest_group/v1",
  sdk: Re,
  sidebar: { icon: () => null, label: "Quests", to: "/m/msquests" },
  // Quests are Documents: they are edited from the project, no screen of their own.
  routes: [],
  documents: [jt]
}, Et = { ...Nt, sdk: "1.10.0" };
export {
  Et as default
};
