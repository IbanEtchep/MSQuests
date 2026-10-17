import { createModuleI18n as le, fieldText as ce, newTextKey as A, injectModuleStyles as de, useProject as ue, useContentLang as me, Button as z, textValue as _, updateDocument as pe, useUiLang as he, SDK_VERSION as ye } from "@artisan/editor-sdk";
import { jsxs as p, jsx as n, Fragment as I } from "react/jsx-runtime";
import { useState as fe } from "react";
const ve = {
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
  fr: ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"]
}, be = /^\d+$/;
function F(e, t, i) {
  const a = String(i).padStart(2, "0");
  return e === "fr" ? i === 0 ? `${t} h` : `${t} h ${a}` : `${String(t).padStart(2, "0")}:${a}`;
}
function j(e, t, i) {
  return be.test(e) && Number(e) >= t && Number(e) <= i;
}
function ke(e) {
  return e.trim().split(/\s+/).length === 5;
}
function ge(e, t) {
  const i = t === "fr", a = e.trim().split(/\s+/);
  if (a.length !== 5) return null;
  const [s, c, u, h, o] = a;
  if (h !== "*") return null;
  const l = /^\*\/(\d+)$/;
  if (u === "*" && o === "*") {
    if (j(s, 0, 59) && j(c, 0, 23))
      return i ? `tous les jours à ${F(t, +c, +s)}` : `every day at ${F(t, +c, +s)}`;
    const m = l.exec(c);
    if (j(s, 0, 59) && m)
      return i ? `toutes les ${m[1]} h, à la minute ${s}` : `every ${m[1]} hours, at minute ${s}`;
    if (j(s, 0, 59) && c === "*")
      return i ? `toutes les heures, à la minute ${s}` : `every hour, at minute ${s}`;
    const f = l.exec(s);
    return f && c === "*" ? i ? `toutes les ${f[1]} minutes` : `every ${f[1]} minutes` : null;
  }
  if (!j(s, 0, 59) || !j(c, 0, 23)) return null;
  const r = F(t, +c, +s);
  if (u === "*" && j(o, 0, 7)) {
    const m = ve[i ? "fr" : "en"][Number(o) % 7];
    return i ? `chaque ${m} à ${r}` : `every ${m} at ${r}`;
  }
  return o === "*" && j(u, 1, 31) ? i ? `le ${u === "1" ? "1er" : u} de chaque mois à ${r}` : `on day ${u} of each month at ${r}` : null;
}
const Z = {
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
  tier: "Tier",
  duration: "Time limit",
  durationHint: "24h, 1h30m, 90s… empty: none",
  durationInvalid: "Write a duration such as 24h, 1h30m or 90s.",
  stages: "Stages",
  stage: "Stage {{n}}",
  addStage: "Add a stage",
  flow: "Objectives",
  "flow.default": "Default",
  "flow.parallel": "In any order",
  "flow.sequential": "One after the other",
  objectives: "Objectives",
  addObjective: "Add an objective",
  type: "Type",
  required: "Required",
  rewards: "Rewards",
  addReward: "Add a command",
  rewardCommand: "Console command ({player} is the player)",
  rewardOther: "{{type}} action, kept as is",
  hooks: "{{count}} group hooks are kept as they are (edited in a later version).",
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
  "param.itemKey": "Custom item",
  "param.entity_type": "Entity",
  "param.distance": "Distance (blocks)",
  "param.command": "Command",
  "param.placeholder": "Placeholder",
  "param.value": "Value",
  any: "any"
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
  tier: "Tier",
  duration: "Durée limite",
  durationHint: "24h, 1h30m, 90s… vide : aucune",
  durationInvalid: "Écrivez une durée comme 24h, 1h30m ou 90s.",
  stages: "Étapes",
  stage: "Étape {{n}}",
  addStage: "Ajouter une étape",
  flow: "Objectifs",
  "flow.default": "Par défaut",
  "flow.parallel": "Dans n'importe quel ordre",
  "flow.sequential": "L'un après l'autre",
  objectives: "Objectifs",
  addObjective: "Ajouter un objectif",
  type: "Type",
  required: "Obligatoire",
  rewards: "Récompenses",
  addReward: "Ajouter une commande",
  rewardCommand: "Commande console ({player} est le joueur)",
  rewardOther: "Action {{type}}, conservée telle quelle",
  hooks: "{{count}} crochets du groupe sont conservés tels quels (édités dans une prochaine version).",
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
  "param.itemKey": "Objet personnalisé",
  "param.entity_type": "Créature",
  "param.distance": "Distance (blocs)",
  "param.command": "Commande",
  "param.placeholder": "Placeholder",
  "param.value": "Valeur",
  any: "n'importe lequel"
}, H = { en: Z.kindLabel, fr: X.kindLabel }, qe = le({ en: Z, fr: X }), ee = "quest_group", te = "quest_groups", V = "quest_group/v1", W = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
function L(e = Date.now()) {
  let t = "", i = e;
  for (let c = 0; c < 10; c++)
    t = W[i % 32] + t, i = Math.floor(i / 32);
  const a = new Uint8Array(16);
  crypto.getRandomValues(a);
  let s = "";
  for (let c = 0; c < 16; c++) s += W[a[c] % 32];
  return t + s;
}
function E(e, t) {
  const i = new Set(t);
  if (!i.has(e)) return e;
  for (let a = 2; ; a++) if (!i.has(`${e}_${a}`)) return `${e}_${a}`;
}
function ae(e) {
  return e.toLowerCase().replace(/[^a-z0-9_]/g, "_");
}
function B(e) {
  return e.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().replace(/[^a-z0-9_]+/g, "_").replace(/^_+|_+$/g, "") || "quest";
}
function N(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? { ...e } : {};
}
function U(e) {
  return Array.isArray(e) ? e : [];
}
function x(e) {
  return typeof e == "string" || typeof e == "number" || typeof e == "boolean" ? String(e) : "";
}
function Q(e, ...t) {
  const i = { ...e };
  for (const a of t) delete i[a];
  return i;
}
function xe(e, t) {
  const i = {}, a = [];
  function s(o) {
    if (o && typeof o == "object" && !Array.isArray(o)) {
      const m = A(), f = {};
      for (const [k, b] of Object.entries(o)) f[k] = x(b);
      return i[m] = f, { key: m };
    }
    const l = typeof o == "string" ? /^\{i18n:(.+)\}$/.exec(o) : null;
    if (l) {
      const m = t.translations?.[l[1]];
      return m && (i[l[1]] = { ...m }), { key: l[1] };
    }
    const r = A();
    return i[r] = o == null || o === "" ? {} : { [t.lang]: x(o) }, { key: r };
  }
  const c = Object.entries(N(e.tiers)).map(([o, l]) => {
    const r = N(l);
    return { key: o, title: s(r.title), rest: Q(r, "title") };
  }), u = U(e.quests).map((o, l) => {
    const r = N(o), m = U(r.stages).map((f, k) => {
      const b = N(f), D = x(b.flow).toLowerCase();
      return {
        key: x(b.key) || `stage_${k + 1}`,
        title: s(b.title),
        flow: D === "parallel" || D === "sequential" ? D : "",
        objectives: U(b.objectives).map((d, y) => {
          const v = N(d);
          return {
            key: x(v.key) || `objective_${y + 1}`,
            type: x(v.type),
            params: Q(v, "key", "type")
          };
        }),
        rest: Q(b, "key", "title", "flow", "objectives")
      };
    });
    return {
      key: x(r.key) || `quest_${l + 1}`,
      uid: x(r.uid) || L(),
      tier: x(r.tier),
      title: s(r.title),
      description: s(r.description),
      duration: x(r.duration),
      stages: m,
      rest: Q(r, "key", "uid", "tier", "title", "description", "duration", "stages")
    };
  }), h = {
    id: x(e.id),
    projectId: t.projectId,
    uid: x(e.uid) || L(),
    title: s(e.title),
    description: s(e.description),
    actorType: x(e.actor_type) || "player",
    reset: N(e.reset),
    limits: N(e.limits),
    rotation: N(e.rotation),
    distribution: N(e.distribution),
    tiers: c,
    quests: u,
    rest: Q(
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
    )
  };
  return e.schema !== void 0 && e.schema !== V && a.push(`schema: ${String(e.schema)}`), { doc: h, i18nKeys: i, unsupported: a };
}
function we(e) {
  return typeof e == "string" ? e === "" : Object.values(e).every((t) => t === "");
}
function C(e) {
  const t = {};
  for (const [i, a] of Object.entries(e))
    a == null || a === "" || Array.isArray(a) && a.length === 0 || typeof a == "object" && !Array.isArray(a) && Object.keys(a).length === 0 || (t[i] = a);
  return t;
}
const Ne = [
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
function je(e, t) {
  const i = (l) => ce(t, l.key), a = (l) => {
    const r = i(l);
    return we(r) ? void 0 : r;
  }, s = {};
  for (const l of e.tiers) s[l.key] = { ...l.rest, title: i(l.title) };
  const c = e.quests.map(
    (l) => C({
      key: l.key,
      uid: l.uid,
      tier: l.tier,
      title: i(l.title),
      description: a(l.description),
      duration: l.duration,
      ...l.rest,
      stages: l.stages.map(
        (r) => C({
          key: r.key,
          title: a(r.title),
          flow: r.flow,
          ...r.rest,
          objectives: r.objectives.map((m) => ({ key: m.key, type: m.type, ...m.params }))
        })
      )
    })
  ), u = {
    ...e.rest,
    id: e.id,
    schema: V,
    uid: e.uid,
    title: i(e.title),
    description: a(e.description),
    actor_type: e.actorType,
    reset: C(e.reset),
    limits: C(e.limits),
    rotation: C(e.rotation),
    distribution: C(e.distribution),
    tiers: s,
    quests: c
  }, h = {};
  for (const l of Ne) l in u && (h[l] = u[l]);
  for (const l of Object.keys(u)) l in h || (h[l] = u[l]);
  const o = C(h);
  return o.quests = c, o;
}
function* _e(e) {
  const t = `${te}.${e.id}`;
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
    const s = e.quests[a], c = i.has(s.uid) ? `${s.uid}_${a}` : s.uid;
    i.add(s.uid);
    const u = `${t}.quests.${c}`;
    yield {
      lText: s.title,
      key: `${u}.title`,
      path: `quests[${a}].title`,
      kind: "quest.title"
    }, yield {
      lText: s.description,
      key: `${u}.description`,
      path: `quests[${a}].description`,
      kind: "quest.description"
    };
    const h = /* @__PURE__ */ new Set();
    for (let o = 0; o < s.stages.length; o++) {
      const l = s.stages[o], r = h.has(l.key) ? `${l.key}_${o}` : l.key;
      h.add(l.key), yield {
        lText: l.title,
        key: `${u}.stages.${r}.title`,
        path: `quests[${a}].stages[${o}].title`,
        kind: "quest.stage"
      };
    }
  }
}
function J(e) {
  return Array.isArray(e) ? e : e && typeof e == "object" ? [e] : [];
}
function $e() {
  return { run_command: { command: "", as: "console" } };
}
function Y(e) {
  const t = e.run_command;
  if (typeof t == "string") return t;
  if (t && typeof t == "object") {
    const i = t.command;
    return typeof i == "string" ? i : "";
  }
  return e.type === "command" || e.type === "player_command" ? typeof e.command == "string" ? e.command : "" : null;
}
function Ce(e, t) {
  if (e.type === "command" || e.type === "player_command") return { ...e, command: t };
  const i = e.run_command;
  return { ...e, run_command: { ...i && typeof i == "object" ? i : { as: "console" }, command: t } };
}
function Te(e) {
  return typeof e.type == "string" ? e.type : Object.keys(e).find((t) => t !== "if" && t !== "do" && t !== "else") ?? "if";
}
const T = { name: "amount", kind: "int", required: !0 }, ne = [
  { type: "block_break", params: [{ name: "material", kind: "material" }, T] },
  { type: "craft_item", params: [{ name: "material", kind: "material" }, T] },
  { type: "harvest_crop", params: [{ name: "crop", kind: "material", required: !0 }, T] },
  { type: "fishing", params: [{ name: "fish_type", kind: "material" }, T] },
  {
    type: "deliver_item",
    params: [{ name: "material", kind: "material" }, T, { name: "itemKey", kind: "text" }]
  },
  { type: "kill_entity", params: [{ name: "entity_type", kind: "entity" }, T] },
  { type: "breed_animal", params: [{ name: "entity_type", kind: "entity" }, T] },
  { type: "travel", params: [{ name: "distance", kind: "int", required: !0 }] },
  { type: "execute_command", params: [{ name: "command", kind: "text", required: !0 }] },
  {
    type: "placeholder",
    params: [
      { name: "placeholder", kind: "text", required: !0 },
      { name: "value", kind: "text", required: !0 }
    ]
  }
];
function G(e) {
  return ne.find((t) => t.type === e);
}
function ie(e) {
  const t = {};
  for (const i of G(e)?.params ?? [])
    i.kind === "int" && (t[i.name] = 1);
  return t;
}
const Se = `
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
`, Ae = /^(\d+|(\d+[dhms])+)$/, Oe = ["actor_load", "period_reset", "quest_complete"], Re = (() => {
  try {
    return Intl.supportedValuesOf?.("timeZone") ?? [];
  } catch {
    return [];
  }
})();
function ze({ projectId: e, documentId: t, document: i }) {
  de("msquests", Se);
  const a = i, s = ue(e), c = me(), u = qe(), [h, o] = fe(null), r = { project: s, lang: c, t: u, edit: (f, k) => {
    const b = structuredClone(a);
    f(b), pe({ projectId: e, kind: ee, id: t, doc: b, texts: k });
  } }, m = h ? a.quests.findIndex((f) => f.uid === h) : -1;
  return m >= 0 ? /* @__PURE__ */ p("div", { className: "aq", children: [
    /* @__PURE__ */ p(z, { variant: "ghost", size: "sm", className: "aq-back", onClick: () => o(null), children: [
      "← ",
      u("back")
    ] }),
    /* @__PURE__ */ n(Me, { ctx: r, group: a, index: m })
  ] }) : /* @__PURE__ */ n("div", { className: "aq", children: /* @__PURE__ */ n(Pe, { ctx: r, group: a, onOpen: o }) });
}
function g({
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
function O({
  ctx: e,
  label: t,
  node: i,
  multiline: a
}) {
  const s = _(e.project, i.key, e.lang), c = (u) => e.edit(() => {
  }, [{ key: i.key, lang: e.lang, value: u }]);
  return /* @__PURE__ */ n(g, { label: `${t} · ${e.lang}`, children: a ? /* @__PURE__ */ n("textarea", { className: "textarea", value: s, onChange: (u) => c(u.target.value) }) : /* @__PURE__ */ n("input", { className: "input", value: s, onChange: (u) => c(u.target.value) }) });
}
function De(e) {
  return typeof e == "number" ? String(e) : typeof e == "string" ? e : "";
}
function S({
  label: e,
  value: t,
  onChange: i
}) {
  return /* @__PURE__ */ n(g, { label: e, children: /* @__PURE__ */ n(
    "input",
    {
      className: "input",
      type: "number",
      min: 0,
      step: 1,
      value: De(t),
      onChange: (a) => {
        const s = a.target.value.trim();
        i(s === "" ? void 0 : Math.max(0, Math.trunc(Number(s))));
      }
    }
  ) });
}
function w(e, t, i) {
  i === void 0 || i === "" ? delete e[t] : e[t] = i;
}
function re(e, t, i) {
  if (i < 0 || i >= e.length) return;
  const [a] = e.splice(t, 1);
  e.splice(i, 0, a);
}
function R({
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
function se({
  t: e,
  index: t,
  count: i,
  onMove: a,
  onRemove: s
}) {
  return /* @__PURE__ */ p(I, { children: [
    /* @__PURE__ */ n(R, { label: e("moveUp"), disabled: t === 0, onClick: () => a(t - 1), children: "↑" }),
    /* @__PURE__ */ n(
      R,
      {
        label: e("moveDown"),
        disabled: t === i - 1,
        onClick: () => a(t + 1),
        children: "↓"
      }
    ),
    /* @__PURE__ */ n(R, { label: e("remove"), onClick: s, children: "×" })
  ] });
}
function Pe({
  ctx: e,
  group: t,
  onOpen: i
}) {
  const { t: a, edit: s, project: c, lang: u } = e, h = he(), o = typeof t.reset.cron == "string" ? t.reset.cron : "", l = o ? ge(o, h) : null, r = typeof t.reset.timezone == "string" ? t.reset.timezone : "", m = Array.isArray(t.distribution.triggers) ? t.distribution.triggers : [], f = t.distribution.tiers ?? {}, k = t.rest.on && typeof t.rest.on == "object" ? Object.keys(t.rest.on).length : 0, b = Object.keys(t.rest).filter((d) => d !== "on"), D = () => {
    const d = A(), y = L();
    s(
      (v) => {
        v.quests.push({
          key: E(
            "quest",
            v.quests.map((q) => q.key)
          ),
          uid: y,
          tier: "",
          title: { key: d },
          description: { key: A() },
          duration: "",
          stages: [oe(1)],
          rest: {}
        });
      },
      [{ key: d, lang: u, value: a("newQuestTitle") }]
    ), i(y);
  };
  return /* @__PURE__ */ p(I, { children: [
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("group") }),
      /* @__PURE__ */ n(O, { ctx: e, label: a("title"), node: t.title }),
      /* @__PURE__ */ n(O, { ctx: e, label: a("description"), node: t.description, multiline: !0 }),
      /* @__PURE__ */ n(g, { label: a("actorType"), children: /* @__PURE__ */ p(
        "select",
        {
          className: "select",
          value: t.actorType,
          onChange: (d) => s((y) => {
            y.actorType = d.target.value;
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
        g,
        {
          label: a("cron"),
          hint: o ? ke(o) ? /* @__PURE__ */ p("span", { className: "aq-ok", children: [
            l ?? a("cronRaw"),
            r ? `, ${r}` : ""
          ] }) : /* @__PURE__ */ n("span", { className: "aq-err", children: a("cronInvalid") }) : /* @__PURE__ */ n("span", { className: "aq-hint", children: a("noReset") }),
          children: /* @__PURE__ */ n(
            "input",
            {
              className: "input",
              value: o,
              placeholder: "0 4 * * *",
              onChange: (d) => s((y) => w(y.reset, "cron", d.target.value))
            }
          )
        }
      ),
      /* @__PURE__ */ p(g, { label: a("timezone"), children: [
        /* @__PURE__ */ n(
          "input",
          {
            className: "input",
            list: "aq-timezones",
            value: r,
            placeholder: a("timezoneServer"),
            onChange: (d) => s((y) => w(y.reset, "timezone", d.target.value.trim()))
          }
        ),
        /* @__PURE__ */ n("datalist", { id: "aq-timezones", children: Re.map((d) => /* @__PURE__ */ n("option", { value: d }, d)) })
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("distribution") }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(g, { label: a("strategy"), children: /* @__PURE__ */ p(
          "select",
          {
            className: "select",
            value: typeof t.distribution.strategy == "string" ? t.distribution.strategy.toLowerCase() : "",
            onChange: (d) => s((y) => w(y.distribution, "strategy", d.target.value)),
            children: [
              /* @__PURE__ */ n("option", { value: "", children: "—" }),
              ["none", "sequential", "random"].map((d) => /* @__PURE__ */ n("option", { value: d, children: a(`strategy.${d}`) }, d))
            ]
          }
        ) }),
        /* @__PURE__ */ n(
          S,
          {
            label: a("amount"),
            value: t.distribution.amount,
            onChange: (d) => s((y) => w(y.distribution, "amount", d))
          }
        )
      ] }),
      /* @__PURE__ */ n("span", { className: "field-label", children: a("triggers") }),
      Oe.map((d) => /* @__PURE__ */ p("label", { className: "aq-check", children: [
        /* @__PURE__ */ n(
          "input",
          {
            type: "checkbox",
            checked: m.includes(d),
            onChange: (y) => s((v) => {
              const q = Array.isArray(v.distribution.triggers) ? v.distribution.triggers : [], $ = y.target.checked ? [...q, d] : q.filter((P) => P !== d);
              w(v.distribution, "triggers", $.length ? $ : void 0);
            })
          }
        ),
        a(`trigger.${d}`)
      ] }, d)),
      t.tiers.length > 0 && /* @__PURE__ */ p(I, { children: [
        /* @__PURE__ */ n("span", { className: "field-label", children: a("perTier") }),
        /* @__PURE__ */ n("div", { className: "aq-row", children: t.tiers.map((d) => /* @__PURE__ */ n(
          S,
          {
            label: _(c, d.title.key, u) || d.key,
            value: f[d.key],
            onChange: (y) => s((v) => {
              const q = { ...v.distribution.tiers ?? {} };
              w(q, d.key, y), w(
                v.distribution,
                "tiers",
                Object.keys(q).length ? q : void 0
              );
            })
          },
          d.key
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
            onChange: (d) => s((y) => w(y.limits, "max_active", d))
          }
        ),
        /* @__PURE__ */ n(
          S,
          {
            label: a("maxPerPeriod"),
            value: t.limits.max_per_period,
            onChange: (d) => s((y) => w(y.limits, "max_per_period", d))
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
              onChange: (d) => s((y) => w(y.rotation, "enabled", d.target.checked ? !0 : void 0))
            }
          ),
          a("rotationEnabled")
        ] }),
        /* @__PURE__ */ n(
          S,
          {
            label: a("maxPerPeriod"),
            value: t.rotation.max_per_period,
            onChange: (d) => s((y) => w(y.rotation, "max_per_period", d))
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ n("span", { className: "aq-h", children: a("tiers") }),
        /* @__PURE__ */ p(
          z,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => s((d) => {
              d.tiers.push({
                key: E(
                  "tier",
                  d.tiers.map((y) => y.key)
                ),
                title: { key: A() },
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
      t.tiers.map((d, y) => /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(g, { label: a("tierKey"), children: /* @__PURE__ */ n(
          "input",
          {
            className: "input",
            defaultValue: d.key,
            onBlur: (v) => {
              const q = B(v.target.value);
              q !== d.key && s(($) => {
                const P = E(
                  q,
                  $.tiers.map((K) => K.key)
                );
                $.tiers[y].key = P;
                for (const K of $.quests) K.tier === d.key && (K.tier = P);
                const M = $.distribution.tiers;
                M && d.key in M && (M[P] = M[d.key], delete M[d.key]);
              });
            }
          }
        ) }),
        /* @__PURE__ */ n(O, { ctx: e, label: a("title"), node: d.title }),
        /* @__PURE__ */ n(
          R,
          {
            label: a("remove"),
            onClick: () => s((v) => {
              v.tiers.splice(y, 1);
            }),
            children: "×"
          }
        )
      ] }, d.key))
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ p("span", { className: "aq-h", children: [
          a("quests"),
          " · ",
          t.quests.length
        ] }),
        /* @__PURE__ */ p(z, { size: "sm", variant: "primary", onClick: D, children: [
          "+ ",
          a("addQuest")
        ] })
      ] }),
      t.quests.length === 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: a("noQuests") }),
      /* @__PURE__ */ n("div", { className: "aq-list", children: t.quests.map((d, y) => /* @__PURE__ */ p("div", { className: "aq-item", children: [
        /* @__PURE__ */ p(
          "button",
          {
            type: "button",
            className: "aq-item-main",
            onClick: () => i(d.uid),
            title: a("open"),
            children: [
              /* @__PURE__ */ n("span", { className: "aq-item-title", children: _(c, d.title.key, u) || d.key }),
              /* @__PURE__ */ p("span", { className: "aq-item-sub", children: [
                d.key,
                d.tier ? ` · ${d.tier}` : "",
                d.duration ? ` · ${d.duration}` : ""
              ] })
            ]
          }
        ),
        /* @__PURE__ */ n(
          se,
          {
            t: a,
            index: y,
            count: t.quests.length,
            onMove: (v) => s((q) => re(q.quests, y, v)),
            onRemove: () => s((v) => {
              v.quests.splice(y, 1);
            })
          }
        )
      ] }, d.uid)) }),
      k > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: a("hooks", { count: k }) }),
      b.length > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: a("kept", { fields: b.join(", ") }) })
    ] })
  ] });
}
function oe(e) {
  return { key: `stage_${e}`, title: { key: A() }, flow: "", objectives: [], rest: {} };
}
function Me({ ctx: e, group: t, index: i }) {
  const { t: a, edit: s } = e, c = t.quests[i], u = (r, m) => s((f) => r(f.quests[i]), m), h = t.quests.some((r, m) => m !== i && r.key === c.key), o = c.duration !== "" && !Ae.test(c.duration.replace(/\s/g, "").toLowerCase()), l = J(c.rest.rewards);
  return /* @__PURE__ */ p(I, { children: [
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n(O, { ctx: e, label: a("title"), node: c.title }),
      /* @__PURE__ */ n(O, { ctx: e, label: a("description"), node: c.description, multiline: !0 }),
      /* @__PURE__ */ p("div", { className: "aq-row", children: [
        /* @__PURE__ */ n(
          g,
          {
            label: a("key"),
            hint: h && /* @__PURE__ */ n("span", { className: "aq-err", children: a("keyTaken") }),
            children: /* @__PURE__ */ n(
              "input",
              {
                className: "input",
                value: c.key,
                onChange: (r) => u((m) => {
                  m.key = ae(r.target.value);
                }),
                onBlur: () => {
                  const r = B(c.key);
                  r !== c.key && u((m) => {
                    m.key = r;
                  });
                }
              }
            )
          }
        ),
        /* @__PURE__ */ n(g, { label: a("tier"), children: /* @__PURE__ */ p(
          "select",
          {
            className: "select",
            value: c.tier,
            onChange: (r) => u((m) => {
              m.tier = r.target.value;
            }),
            children: [
              /* @__PURE__ */ n("option", { value: "", children: a("noTier") }),
              t.tiers.map((r) => /* @__PURE__ */ n("option", { value: r.key, children: _(e.project, r.title.key, e.lang) || r.key }, r.key)),
              c.tier && !t.tiers.some((r) => r.key === c.tier) && /* @__PURE__ */ n("option", { value: c.tier, children: c.tier })
            ]
          }
        ) })
      ] }),
      /* @__PURE__ */ n(
        g,
        {
          label: a("duration"),
          hint: /* @__PURE__ */ n("span", { className: o ? "aq-err" : "aq-hint", children: a(o ? "durationInvalid" : "durationHint") }),
          children: /* @__PURE__ */ n(
            "input",
            {
              className: "input",
              value: c.duration,
              placeholder: "24h",
              onChange: (r) => u((m) => {
                m.duration = r.target.value.trim();
              })
            }
          )
        }
      )
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ n("span", { className: "aq-h", children: a("stages") }),
        /* @__PURE__ */ p(
          z,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => u((r) => {
              const m = E(
                `stage_${r.stages.length + 1}`,
                r.stages.map((f) => f.key)
              );
              r.stages.push({ ...oe(r.stages.length + 1), key: m });
            }),
            children: [
              "+ ",
              a("addStage")
            ]
          }
        )
      ] }),
      c.stages.map((r, m) => /* @__PURE__ */ n(
        Qe,
        {
          ctx: e,
          quest: c,
          stage: r,
          si: m,
          editQuest: u
        },
        `${r.key}-${m}`
      ))
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ p("div", { className: "aq-head", children: [
        /* @__PURE__ */ n("span", { className: "aq-h", children: a("rewards") }),
        /* @__PURE__ */ p(
          z,
          {
            size: "sm",
            variant: "ghost",
            onClick: () => u((r) => {
              r.rest.rewards = [...l, $e()];
            }),
            children: [
              "+ ",
              a("addReward")
            ]
          }
        )
      ] }),
      l.map((r, m) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: an action has no identity; its inputs are controlled
        /* @__PURE__ */ p("div", { className: "aq-row", children: [
          Y(r) !== null ? /* @__PURE__ */ n(g, { label: a("rewardCommand"), children: /* @__PURE__ */ n(
            "input",
            {
              className: "input",
              value: Y(r) ?? "",
              placeholder: "give {player} diamond 1",
              onChange: (f) => u((k) => {
                const b = J(k.rest.rewards);
                b[m] = Ce(b[m], f.target.value), k.rest.rewards = b;
              })
            }
          ) }) : /* @__PURE__ */ n("span", { className: "aq-hint", children: a("rewardOther", { type: Te(r) }) }),
          /* @__PURE__ */ n(
            R,
            {
              label: a("remove"),
              onClick: () => u((f) => {
                const k = [...l];
                k.splice(m, 1), w(f.rest, "rewards", k.length ? k : void 0);
              }),
              children: "×"
            }
          )
        ] }, m)
      )),
      Object.keys(c.rest).filter((r) => r !== "rewards").length > 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: a("kept", {
        fields: Object.keys(c.rest).filter((r) => r !== "rewards").join(", ")
      }) })
    ] }),
    /* @__PURE__ */ p("section", { className: "aq-section", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: a("preview") }),
      /* @__PURE__ */ n(Ie, { ctx: e, quest: c })
    ] })
  ] });
}
function Qe({
  ctx: e,
  quest: t,
  stage: i,
  si: a,
  editQuest: s
}) {
  const { t: c } = e, u = (h) => s((o) => h(o.stages[a]));
  return /* @__PURE__ */ p("div", { className: "aq-card", children: [
    /* @__PURE__ */ p("div", { className: "aq-head", children: [
      /* @__PURE__ */ n("span", { className: "aq-h", children: c("stage", { n: a + 1 }) }),
      /* @__PURE__ */ n("span", { children: /* @__PURE__ */ n(
        se,
        {
          t: c,
          index: a,
          count: t.stages.length,
          onMove: (h) => s((o) => re(o.stages, a, h)),
          onRemove: () => s((h) => {
            h.stages.splice(a, 1);
          })
        }
      ) })
    ] }),
    /* @__PURE__ */ n(O, { ctx: e, label: c("title"), node: i.title }),
    /* @__PURE__ */ n(g, { label: c("flow"), children: /* @__PURE__ */ p(
      "select",
      {
        className: "select",
        value: i.flow,
        onChange: (h) => u((o) => {
          o.flow = h.target.value;
        }),
        children: [
          /* @__PURE__ */ n("option", { value: "", children: c("flow.default") }),
          /* @__PURE__ */ n("option", { value: "parallel", children: c("flow.parallel") }),
          /* @__PURE__ */ n("option", { value: "sequential", children: c("flow.sequential") })
        ]
      }
    ) }),
    i.objectives.length === 0 && /* @__PURE__ */ n("span", { className: "aq-hint", children: c("noObjectives") }),
    i.objectives.map((h, o) => /* @__PURE__ */ n(
      Ee,
      {
        t: c,
        objective: h,
        taken: t.stages.flatMap((l) => l.objectives).filter((l) => l !== h).map((l) => l.key),
        onChange: (l) => u((r) => l(r.objectives[o])),
        onRemove: () => u((l) => {
          l.objectives.splice(o, 1);
        })
      },
      `${h.key}-${o}`
    )),
    /* @__PURE__ */ p(
      z,
      {
        size: "sm",
        variant: "ghost",
        onClick: () => s((h) => {
          const o = h.stages.flatMap((l) => l.objectives.map((r) => r.key));
          h.stages[a].objectives.push({
            key: E("objective", o),
            type: "block_break",
            params: ie("block_break")
          });
        }),
        children: [
          "+ ",
          c("addObjective")
        ]
      }
    )
  ] });
}
function Ee({
  t: e,
  objective: t,
  taken: i,
  onChange: a,
  onRemove: s
}) {
  const c = G(t.type), u = c ? c.params : Object.keys(t.params).map((o) => ({ name: o, kind: "text" })), h = i.includes(t.key);
  return /* @__PURE__ */ p("div", { className: "aq-sub", children: [
    /* @__PURE__ */ p("div", { className: "aq-row", children: [
      /* @__PURE__ */ n(g, { label: e("type"), children: /* @__PURE__ */ p(
        "select",
        {
          className: "select",
          value: t.type,
          onChange: (o) => a((l) => {
            l.type = o.target.value, l.params = ie(l.type);
          }),
          children: [
            ne.map((o) => /* @__PURE__ */ n("option", { value: o.type, children: e(`objective.${o.type}`) }, o.type)),
            !c && /* @__PURE__ */ n("option", { value: t.type, children: t.type })
          ]
        }
      ) }),
      /* @__PURE__ */ n(g, { label: e("key"), hint: h && /* @__PURE__ */ n("span", { className: "aq-err", children: e("keyTaken") }), children: /* @__PURE__ */ n(
        "input",
        {
          className: "input",
          value: t.key,
          onChange: (o) => a((l) => {
            l.key = ae(o.target.value);
          }),
          onBlur: () => {
            const o = B(t.key);
            o !== t.key && a((l) => {
              l.key = o;
            });
          }
        }
      ) }),
      /* @__PURE__ */ n(R, { label: e("remove"), onClick: s, children: "×" })
    ] }),
    /* @__PURE__ */ n("div", { className: "aq-row", children: u.map((o) => /* @__PURE__ */ n(
      Ke,
      {
        t: e,
        spec: o,
        value: t.params[o.name],
        onChange: (l) => a((r) => w(r.params, o.name, l))
      },
      o.name
    )) })
  ] });
}
function Ke({
  t: e,
  spec: t,
  value: i,
  onChange: a
}) {
  const s = e(`param.${t.name}`), u = t.required && (i === void 0 || i === "") ? /* @__PURE__ */ n("span", { className: "aq-warn", children: e("required") }) : void 0;
  if (t.kind === "int")
    return /* @__PURE__ */ n(S, { label: s, value: i, onChange: a });
  const h = t.kind === "material" || t.kind === "entity";
  return /* @__PURE__ */ n(g, { label: s, hint: u, children: /* @__PURE__ */ n(
    "input",
    {
      className: "input",
      value: typeof i == "string" || typeof i == "number" ? String(i) : "",
      placeholder: h ? e("any") : "",
      onChange: (o) => a(h ? o.target.value.toUpperCase().replace(/\s+/g, "_") : o.target.value)
    }
  ) });
}
function Ie({ ctx: e, quest: t }) {
  const { t: i, project: a, lang: s } = e, c = (u) => {
    const h = G(u.type), o = h?.params.filter((m) => m.kind !== "int" && u.params[m.name] !== void 0 && u.params[m.name] !== "").map((m) => String(u.params[m.name])).join(" "), l = u.params.amount ?? u.params.distance;
    return `${h ? i(`objective.${u.type}`) : u.type}${o ? ` · ${o}` : ""}${l !== void 0 ? ` · 0/${String(l)}` : ""}`;
  };
  return /* @__PURE__ */ p("div", { className: "aq-preview", children: [
    /* @__PURE__ */ n("span", { className: "aq-preview-title", children: _(a, t.title.key, s) || t.key }),
    _(a, t.description.key, s) && /* @__PURE__ */ n("span", { className: "aq-preview-line", children: _(a, t.description.key, s) }),
    t.stages.flatMap((u) => u.objectives).map((u) => /* @__PURE__ */ p("span", { className: "aq-preview-line", children: [
      "• ",
      c(u)
    ] }, u.key))
  ] });
}
const Le = {
  kind: ee,
  dir: te,
  schemaVersion: V,
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: H.en,
  labels: H,
  component: ze,
  title: (e, { project: t, lang: i }) => _(t, e.title.key, i),
  create: ({ id: e, projectId: t }) => ({
    id: e,
    projectId: t,
    uid: L(),
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
  parse: (e, t) => xe(e, t),
  serialize: (e, t) => je(e, t),
  ltextSites: (e) => _e(e),
  board: {
    summary: (e) => `quest_group · ${e.quests.length}`
  }
}, Fe = {
  id: "msquests",
  displayName: "Quests",
  schemaVersion: "quest_group/v1",
  sdk: ye,
  sidebar: { icon: () => null, label: "Quests", to: "/m/msquests" },
  // Quests are Documents: they are edited from the project, no screen of their own.
  routes: [],
  documents: [Le]
}, Ge = { ...Fe, sdk: "1.0.0" };
export {
  Ge as default
};
