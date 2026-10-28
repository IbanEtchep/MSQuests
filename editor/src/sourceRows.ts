import { KIND, type Objective, type Quest, type QuestGroup, type Stage } from "./model";
import { itemLabel, itemValue, objectiveSpec, objectiveTarget } from "./objectives";

/**
 * Rows of the module's data sources computed from the draft (`FrontendModule.localPreviewRows`),
 * so a quest menu previews the quests being edited instead of the deployed ones.
 *
 * The three sources are mixed: their definition fields (titles, objectives, limits) come
 * from the quest groups of the project, their per-player fields (status, progress) from the
 * server. `ctx.serverRows` (Artisan editor SDK 1.12) carries the server's rows: they are
 * matched by key and keep their player fields, the draft replaces the rest. A quest the
 * draft adds gets the neutral state the server gives a quest nobody started; a quest it
 * removes disappears. An older editor passes no `ctx`: groups and quests then show the
 * draft with that neutral state, and `msquests:active` keeps the server's rows.
 *
 * Row shapes mirror `QuestRows` in the plugin; `sourceRows.test.ts` and `QuestRowsTest`
 * read the same vectors (`testdata/sources/`), so the two cannot drift apart unnoticed.
 */

export const GROUPS = "msquests:groups";
export const QUESTS = "msquests:quests";
export const ACTIVE = "msquests:active";

export interface PreviewContext {
  serverRows?: Record<string, unknown>[];
  params?: Record<string, string>;
}

type Row = Record<string, unknown>;

interface ProjectLike {
  defaultLang?: string;
  documents?: Record<string, unknown[]>;
}

/** The default objective names of the plugin's `lang/` files (`objective.<type>`). */
const OBJECTIVE_NAMES: Record<string, Record<string, string>> = {
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
    travel: "Travel %distance% blocks",
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
    travel: "Parcourir %distance% blocs",
  },
};

/** `zmenu.quest.status.available`: the status a quest nobody started shows. */
const AVAILABLE: Record<string, string> = { en: "Available", fr: "Disponible" };

export function questsLocalRows(project: unknown, ref: string, ctx?: PreviewContext): Row[] | null {
  if (ref !== GROUPS && ref !== QUESTS && ref !== ACTIVE) return null;
  const groups = (project as ProjectLike).documents?.[KIND] as QuestGroup[] | undefined;
  // No quest group in this project: another project may hold them, or the server alone.
  if (!groups || groups.length === 0) return null;
  const lang = (project as ProjectLike).defaultLang ?? "en";
  const server = ctx?.serverRows;
  const groupFilter = ctx?.params?.group || undefined;
  const ordered = [...groups].sort((a, b) => (a.id < b.id ? -1 : a.id > b.id ? 1 : 0));

  if (ref === GROUPS) {
    const byKey = indexBy(server, "key");
    return ordered.map((g) => groupRow(g, byKey.get(g.id), lang));
  }
  if (ref === QUESTS) {
    const byId = indexBy(server, "id");
    return ordered
      .filter((g) => !groupFilter || g.id === groupFilter)
      .flatMap((g) => g.quests.map((q) => questRow(g, q, byId.get(`${g.id}:${q.key}`), lang)));
  }
  // An instance only exists on the server: without its rows there is nothing to show.
  if (!server) return null;
  const byGroup = new Map(groups.map((g) => [g.id, g]));
  return server.flatMap((row) => {
    const group = byGroup.get(str(row.group));
    // A group this project does not hold is left as the server sent it.
    if (!group) return [row];
    const quest = group.quests.find((q) => q.key === str(row.quest_key));
    return quest ? [activeRow(quest, row, lang)] : [];
  });
}

// ------------------------------------------------------------------ groups

function groupRow(group: QuestGroup, server: Row | undefined, lang: string): Row {
  const byQuest = indexBy(asRows(server?.quests), "key");
  return {
    key: group.id,
    name: group.title,
    description: group.description,
    actor_type: group.actorType,
    max_active: int(group.limits.max_active, 1),
    max_per_period: int(group.limits.max_per_period, 0),
    rotatable: group.rotation.enabled === true,
    // The period follows the server's clock and zone: its value stands until a deploy.
    period_end: server?.period_end ?? 0,
    active_count: server?.active_count ?? 0,
    completed_count: server?.completed_count ?? 0,
    can_rotate: server?.can_rotate ?? false,
    quests: group.quests.map((q) => questRow(group, q, byQuest.get(q.key), lang)),
  };
}

// ------------------------------------------------------------------ quests

function questRow(group: QuestGroup, quest: Quest, server: Row | undefined, lang: string): Row {
  const tier = group.tiers.find((t) => t.key === quest.tier);
  return {
    id: `${group.id}:${quest.key}`,
    group: group.id,
    key: quest.key,
    name: quest.title,
    description: quest.description,
    duration: durationSeconds(quest.duration),
    tier: quest.tier,
    tier_name: tier ? tier.title : "",
    assigned: server?.assigned ?? false,
    status_key: server?.status_key ?? "NOT_STARTED",
    status: server?.status ?? AVAILABLE[lang.slice(0, 2)] ?? AVAILABLE.en,
    can_start: server?.can_start ?? false,
    start_blocker: server?.start_blocker ?? "",
    start_blocker_label: server?.start_blocker_label ?? "",
    objective_name: server?.objective_name ?? "",
    objective_progress: server?.objective_progress ?? 0,
    objective_target: server?.objective_target ?? 0,
    progress_ratio: server?.progress_ratio ?? 0,
    stages: stageRows(quest, asRows(server?.stages), lang),
  };
}

function stageRows(quest: Quest, server: Row[], lang: string): Row[] {
  const byKey = indexBy(server, "key");
  return quest.stages.map((stage, index) => {
    const s = byKey.get(stage.key);
    const objectives = indexBy(asRows(s?.objectives), "key");
    return {
      key: stage.key,
      name: stage.title,
      flow: flow(stage),
      index,
      completed: s?.completed ?? false,
      active: s?.active ?? false,
      progress_ratio: s?.progress_ratio ?? 0,
      objectives: stage.objectives.map((o) => objectiveRow(o, objectives.get(o.key), lang)),
    };
  });
}

function objectiveRow(objective: Objective, server: Row | undefined, lang: string): Row {
  const target = targetOf(objective);
  // The server's name is exact (its lang file, the item's translated name): kept while the
  // objective asks the same thing, else rebuilt from the default names.
  const same = server && server.type === objective.type && server.target === target;
  return {
    key: objective.key,
    name: same ? server.name : objectiveName(objective, lang),
    type: objective.type,
    progress: server?.progress ?? 0,
    target,
    progress_ratio: server?.progress_ratio ?? 0,
    completed: server?.completed ?? false,
    status_key: server?.status_key ?? "PENDING",
  };
}

// ----------------------------------------------------------------- actives

function activeRow(quest: Quest, server: Row, lang: string): Row {
  const stages = stageRows(quest, asRows(server.stages), lang);
  const current = stages.find((s) => s.active === true);
  const currentStage = current ? quest.stages.find((s) => s.key === current.key) : undefined;
  return {
    ...server,
    name: quest.title,
    description: quest.description,
    stage_count: quest.stages.length,
    stage_name: currentStage ? currentStage.title : (server.stage_name ?? ""),
    stage_flow: currentStage ? flow(currentStage) : (server.stage_flow ?? ""),
    objectives: stages.flatMap((s) => s.objectives as Row[]),
    stages,
  };
}

// ------------------------------------------------------------------ shared

/** `objective.<type>` of the plugin's default `lang/` file, filled from the objective's fields. */
export function objectiveName(objective: Objective, lang: string): string {
  const names = OBJECTIVE_NAMES[lang.slice(0, 2)] ?? OBJECTIVE_NAMES.en!;
  const template = names[objective.type];
  if (!template) return objective.type;
  const p = objective.params;
  const values: Record<string, string> = {
    amount: String(p.amount ?? ""),
    distance: String(p.distance ?? ""),
    command: str(p.command),
    value: str(p.value),
    // Bukkit's translation keys, as the objective configs write them (`<lang:…>`).
    crop: langKey("block", p.crop),
    entity_type: langKey("entity", p.entity_type),
    material: objective.type === "block_break" ? langKey("block", p.material) : itemName(objective),
    fish_type: itemName(objective),
  };
  return template.replace(/%([a-z_]+)%/g, (_m, name: string) => values[name] ?? "");
}

/** `<lang:block.minecraft.stone>` for `STONE`, "" when absent. */
function langKey(kind: "block" | "entity" | "item", value: unknown): string {
  const id = str(value).toLowerCase().replace(/^minecraft:/, "");
  return id === "" ? "" : `<lang:${kind}.minecraft.${id}>`;
}

/**
 * `ItemTarget.placeholder()`: the item's title, else its vanilla material translated, else
 * the reference as written. A block's item translates as `block.minecraft.*` on the server;
 * the editor cannot tell blocks from items, so it says `item.minecraft.*`.
 */
function itemName(objective: Objective): string {
  const spec = objectiveSpec(objective.type)?.params.find((p) => p.kind === "item");
  if (!spec) return "";
  const item = itemValue(objective.params, spec);
  const title = item.title;
  if (typeof title === "string" && title.trim() !== "") return title;
  const material = str(item.material);
  if (/^(minecraft:)?[a-z_0-9]+$/i.test(material) && !material.includes(":")) {
    return langKey("item", material);
  }
  return material || itemLabel(item);
}

/** `getTarget()` of the objective's config: its amount or distance, 1 for done-or-not types. */
function targetOf(objective: Objective): number {
  const target = objectiveTarget(objective.params);
  if (target === undefined) return 1;
  const n = Number(target);
  return Number.isFinite(n) ? n : 1;
}

function flow(stage: Stage): string {
  return stage.flow === "" ? "PARALLEL" : stage.flow.toUpperCase();
}

/** `QuestDuration.parse`: seconds, or `1d2h30m10s`; 0 when absent or unreadable. */
export function durationSeconds(value: string): number {
  const text = value.trim().toLowerCase().replace(/ /g, "");
  if (text === "") return 0;
  if (/^\d+$/.test(text)) return Number(text);
  const parts = /^(?:(\d+)d)?(?:(\d+)h)?(?:(\d+)m)?(?:(\d+)s)?$/.exec(text);
  if (!parts) return 0;
  const [, d, h, m, s] = parts;
  return Number(d ?? 0) * 86_400 + Number(h ?? 0) * 3_600 + Number(m ?? 0) * 60 + Number(s ?? 0);
}

function indexBy(rows: Row[] | undefined, field: string): Map<string, Row> {
  return new Map((rows ?? []).map((r) => [str(r[field]), r]));
}

function asRows(value: unknown): Row[] {
  return Array.isArray(value) ? (value.filter((v) => v && typeof v === "object") as Row[]) : [];
}

function int(value: unknown, fallback: number): number {
  return typeof value === "number" && Number.isFinite(value) ? value : fallback;
}

function str(value: unknown): string {
  return typeof value === "string" ? value : "";
}
